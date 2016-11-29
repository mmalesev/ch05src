package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import javax.swing.ImageIcon;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;
import com.brackeen.javagamebook.tilegame.sprites.PowerUp.Gas;

/**
    GameManager manages all parts of the game.
*/
public class GameManager extends GameCore {

    public static void main(String[] args) {
        new GameManager().run();
    }

    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;

    private Point pointCache = new Point();
    private TileMap map;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private ResourceManager resourceManager;
    private Sound prizeSound;
    private Sound boopSound;
    private Sound cartoon1Sound;
    private Sound cartoon2Sound;
    private InputManager inputManager;
    private TileMapRenderer renderer;

    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction jump;
    private GameAction exit;
    private GameAction player_shoot;
        
    private int last_bullet = 250; //the time in ms since the last player bullet has been fired
    private int consecutive_bullets = 0;
    
    
    
    private ArrayList<Grub> grubsShooting = new ArrayList<Grub>();

    public void init() {
        super.init();

        // set up input manager
        initInput();

        // start resource manager
        resourceManager = new ResourceManager(
        screen.getFullScreenWindow().getGraphicsConfiguration());

        // load resources
        renderer = new TileMapRenderer();
        renderer.setBackground(
            resourceManager.loadImage("background.png"));

        // load first map
        map = resourceManager.loadNextMap();

        // load sounds
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        prizeSound = soundManager.getSound("sounds/prize.wav");
        boopSound = soundManager.getSound("sounds/boop2.wav");
        cartoon1Sound = soundManager.getSound("sounds/cartoon004.wav");
        cartoon2Sound = soundManager.getSound("sounds/cartoon015.wav");

        // start music
        midiPlayer = new MidiPlayer();
        Sequence sequence =
            midiPlayer.getSequence("sounds/music.midi");
        midiPlayer.play(sequence, true);
        toggleDrumPlayback();
    }


    /**
        Closes any resources used by the GameManager.
    */
    public void stop() {
        super.stop();
        midiPlayer.close();
        soundManager.close();
    }


    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        jump = new GameAction("jump",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        exit = new GameAction("exit", GameAction.DETECT_INITAL_PRESS_ONLY);
        player_shoot = new GameAction("player_action", GameAction.NORMAL);

        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        //inputManager.mapToKey(jump, KeyEvent.VK_UP); //disabled jumping
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(player_shoot, KeyEvent.VK_S);
    }


    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {
            stop();
        }

        Player player = (Player)map.getPlayer();
        if (player.isAlive()) {
            float velocityX = 0;
            if (moveLeft.isPressed()) {
                velocityX-=player.getMaxSpeed();
            }
            if (moveRight.isPressed()) {
                velocityX+=player.getMaxSpeed();
            }
            if (jump.isPressed()) {
                player.jump(false);
            }
            
            //Player shooting
            if(player_shoot.isPressed() && ((Player) player).getcanShoot() == true){
            	if(last_bullet >= 250 ){
            		//creating the animation for a new bullet
            		Animation bullet_animation = new Animation();
        	    	Image bullet_icon = new ImageIcon("images/star1.png").getImage();
        	    	bullet_animation.addFrame(bullet_icon, 10);
        	    	bullet_icon = new ImageIcon("images/star2.png").getImage();
        	    	bullet_animation.addFrame(bullet_icon, 10);
        	    	bullet_icon = new ImageIcon("images/star3.png").getImage();
        	    	bullet_animation.addFrame(bullet_icon, 10);
        	    	
        	    	map.addSprite(new Player_bullet(bullet_animation, player));
        	    	soundManager.play(prizeSound);
        	    	last_bullet = 0;
        	    	consecutive_bullets++;
            	}
            	else{
            		last_bullet += elapsedTime;
            	}
            	
            	if(consecutive_bullets == 10){
            		last_bullet = -750;
            		consecutive_bullets = 0;
            	}
            }
            else{
            	consecutive_bullets = 0;
            	last_bullet = 250;
            }
            
            player.setVelocityX(velocityX);
        }
    }


//Drawing stuff to the screen
    public void draw(Graphics2D g) {
    	Player player2 = (Player)map.getPlayer();
        renderer.draw(g, map,
            screen.getWidth(), screen.getHeight());
        g.setColor(Color.RED);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 24));
        g.drawString("Health: "+player2.getHealth(), 60, 70);
        g.drawString("Score:"+player2.getScore(), 600, 70);
    }


    /**
        Gets the current map.
    */
    public TileMap getMap() {
        return map;
    }


    /**
        Turns on/off drum playback in the midi music (track 1).
    */
    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                !sequencer.getTrackMute(DRUM_TRACK));
        }
    }


    /**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
    */
    public Point getTileCollision(Sprite sprite,
        float newX, float newY)
    {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // get the tile locations
        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
            toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
            toY + sprite.getHeight() - 1);

        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= map.getWidth() ||
                    map.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }

        // no collision found
        return null;
    }


    /**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
    */
    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2) {
            return false;
        }

        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            return false;
        }

        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());

        // check if the two sprites' boundaries intersect
        return (s1x < s2x + s2.getWidth() &&
            s2x < s1x + s1.getWidth() &&
            s1y < s2y + s2.getHeight() &&
            s2y < s1y + s1.getHeight());
    }


    /**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
    */
    public Sprite getSpriteCollision(Sprite sprite) {

        // run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();
            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
                return otherSprite;
            }
        }

        // no collision found
        return null;
    }


    /**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
    */
    public void update(long elapsedTime) {
        Creature player = (Creature)map.getPlayer();
        int health = 0;
        int score  = 0;

        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {
            map = resourceManager.reloadMap();
            return;
        }

        // get keyboard/mouse input
        checkInput(elapsedTime);

        // update player
        updateCreature(player, elapsedTime);
        player.update(elapsedTime);
        
        
        
        while(!grubsShooting.isEmpty()){
        	Grub grub = grubsShooting.get(0);
        	ArrayList<GrubBullet> newGrubBullets = grub.getGrubBullets();
			while(!newGrubBullets.isEmpty()){
	        	map.addSprite(newGrubBullets.get(0));
	        	newGrubBullets.remove(0);
	        }
        	grubsShooting.remove(0);
        }
        
    
        // update other sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                	//If the grub dies
                	if (sprite instanceof Grub) {
                	health = ((Player) player).getHealth();
                	health = ((Player) player).updateHealth(health, 10);
                	((Player) player).setHealth(health);
                	score = ((Player) player).getScore();
                	score = ((Player) player).updateScore(score);
                	((Player) player).setScore(score);
                	
                	}
                    i.remove();
                }
                else {
                    updateCreature(creature, elapsedTime);
                }
            }
            if(sprite instanceof Player_bullet){
            	if(((Player_bullet)sprite).isDead()){
            		i.remove();
            	}
            	if(((Player_bullet)sprite).expired()){
            		((Player_bullet)sprite).setDead(true);
            	}
            }
            if(sprite instanceof GrubBullet){
            	if(((GrubBullet)sprite).isDead()){
            		i.remove();
            	}
            	if(((GrubBullet)sprite).expired()){
            		((GrubBullet)sprite).setDead(true);
            	}
            }
           
            // normal update
            sprite.update(elapsedTime);
            
        }
    }


    /**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
    */
    private void updateCreature(Creature creature,
        long elapsedTime)
    {

    	long time;
    	long timeShoot;
    	
    	if (creature instanceof Player) {
        	Creature player = (Creature)map.getPlayer();
        	int health;
        	
        	if (((Player)player).getcanShoot() == false) {
        		timeShoot = ((Player) player).updateShootTime(elapsedTime);
        		if (timeShoot > 3000) {
        			((Player) player).setcanShoot(true);
        			((Player) player).setShootTime(0);	
        		}
        		
        	}
        	
        	if (((Player)player).getVelocityX() == 0 && ((Player)player).getVelocityY() < 0.1) {
        		
        		time = ((Player) player).updateStationaryTime(elapsedTime);

        		if (time > 1000) {
        			health = ((Player) player).getHealth();
                	health = ((Player) player).updateHealth(health, 5);
                	((Player) player).setHealth(health);
                	((Player) player).setStationaryTime(0); 	
        		}
        	} else
        	{
        		((Player) player).setStationaryTime(0);
        	}
        	
        }
    	
    	
        // apply gravity
        if (!creature.isFlying()) {
            creature.setVelocityY(creature.getVelocityY() +
                GRAVITY * elapsedTime);
        }

        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        
        Point tile =
            getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        }
        else {
            // line up with the tile boundary
            if (dx > 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x) -
                    creature.getWidth());
            }
            else if (dx < 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
        	Creature player = (Creature)map.getPlayer();
        	if((((Player) player).getLastUpdatedPosition() + 64 < player.getX() || ((Player) player).getLastUpdatedPosition() - 64 > player.getX())) {
        		int health;
        		if (((Player) player).getcanShoot() == false) {
        			((Player) player).updateShootCount(1);
        			if (((Player) player).getShootCount() > 10) {
        				((Player) player).setcanShoot(true);
        				((Player) player).setShootCount(0);
        			}
        		}
        		
        		health = ((Player) player).getHealth();
            	health = ((Player) player).updateHealth(health, 1);
            	((Player) player).setHealth(health);
            	((Player) player).setLastUpdatedPosition(player.getX());
        	}
        	if(((Player)player).getHealth() <= 0){
        		player.setState(Creature.STATE_DYING);
        	}
        	
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player)creature, false);
        }
        if (!(creature instanceof Player)){
        	checkBulletCollision(creature);
        }

        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y) -
                    creature.getHeight());
            }
            else if (dy < 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player)creature, canKill);
        }
        Creature player = (Creature)map.getPlayer();
        if (creature instanceof Grub){
        	
        	if(((Grub)creature).isOnScreen()){
        		
        		((Grub) creature).updateTimeOnScreen((int)elapsedTime);
        		if(((Grub) creature).getTimeOnScreen() >= 500){
        			((Grub) creature).setShoot(true);
        			
        		}
        		else if(Math.abs(((Player)player).getX() - ((Grub)creature).getPlayerInitialPosition()) > 64){
        			((Grub) creature).setShoot(true);
        		}
        	}
        	else{
        		((Grub)creature).setShoot(false);
        	}
        	
        	
        	
        	if(((Grub)creature).getShoot()){
        		grubsShooting.add((Grub)creature);
            	if(((Grub)creature).getLastGrubBullet() >= 500){
            		//creating the animation for a new bullet
            		Animation bullet_animation = new Animation();
        	    	Image bullet_icon = new ImageIcon("images/heart1.png").getImage();
        	    	bullet_animation.addFrame(bullet_icon, 100);
        	    	bullet_icon = new ImageIcon("images/heart2.png").getImage();
        	    	bullet_animation.addFrame(bullet_icon, 100);
        	    	//bullet_icon = new ImageIcon("images/heart3.png").getImage();
        	    	//bullet_animation.addFrame(bullet_icon, 100);
        	    	
        	    	ArrayList<GrubBullet> newGrubBullets = ((Grub)creature).getGrubBullets();
        	    	newGrubBullets.add(new GrubBullet(bullet_animation, (Grub)creature));
        	    	((Grub)creature).setGrubBullets(newGrubBullets);
        	    	//soundManager.play(prizeSound);
        	    	((Grub)creature).setLastGrubBullet(0);
            	}
            	else{
            		((Grub)creature).updateLastGrubBullet((int)elapsedTime);
            		
            	}
        	}
        }

    }


    /**
        Checks for Player collision with other Sprites. If
        canKill is true, collisions with Creatures will kill
        them.
    */
    public void checkPlayerCollision(Player player,
        boolean canKill)
    {
        if (!player.isAlive()) {
            return;
        }

        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp)collisionSprite);
        }
        else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if (canKill) {
                // kill the badguy and make player bounce
                soundManager.play(boopSound);
                badguy.setState(Creature.STATE_DYING);
                if(badguy instanceof Grub){
                	((Grub)badguy).setShoot(false);
                }
                player.setY(badguy.getY() - player.getHeight());
                player.jump(true);
            }
            else {
                // player dies!
                player.setState(Creature.STATE_DYING);
            }
            
        }
        if(collisionSprite instanceof GrubBullet && !((GrubBullet)collisionSprite).isDead()){
        	((GrubBullet)collisionSprite).setDead(true);
        	int health = player.getHealth();
        	health = player.updateHealth(health, -5);
        	player.setHealth(health);
        	soundManager.play(cartoon1Sound);
        }
    }

    public void checkBulletCollision(Creature creature){
    	if (!creature.isAlive()) {
            return;
        }
    	
    	 Sprite collisionSprite = getSpriteCollision(creature);
    	 
    	 if (collisionSprite instanceof Player_bullet) {
    		 creature.setState(Creature.STATE_DYING);
    		 ((Player_bullet)collisionSprite).setDead(true);
    		 soundManager.play(cartoon2Sound);
    	 }
    }

    /**
        Gives the player the specified power up and removes it
        from the map.
    */
    public void acquirePowerUp(PowerUp powerUp) {
        
        Creature player = (Creature)map.getPlayer();
    	int health;
        if (powerUp instanceof PowerUp.Star) {
        	// remove it from the map
            map.removeSprite(powerUp);
            // do something here, like give the player points
            soundManager.play(prizeSound);
        }
        else if (powerUp instanceof PowerUp.Exploding) {
           //Health decreases by 10
        	// remove it from the map
            map.removeSprite(powerUp);

           health = ((Player) player).getHealth();
           health = ((Player) player).updateHealth(health, -10);
           ((Player) player).setHealth(health);
        }
        else if (powerUp instanceof PowerUp.Gas) {
            //Do not remove from the map
        	((Player) player).setcanShoot(false); //Player can not shoot
         }
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
        	// remove it from the map
            map.removeSprite(powerUp);
            soundManager.play(prizeSound);
            toggleDrumPlayback();
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
        	// remove it from the map
            map.removeSprite(powerUp);
            soundManager.play(prizeSound,
                new EchoFilter(2000, .7f), false);
            map = resourceManager.loadNextMap();
        }
    }

}
