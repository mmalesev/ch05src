package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.*;

/**
    A Creature is a Sprite that is affected by gravity and can
    die. It has four Animations: moving left, moving right,
    dying on the left, and dying on the right.
*/
public class GrubBullet extends Sprite{

	/**
        Amount of time to go from STATE_DYING to STATE_DEAD.
    */
	private boolean dead = false;
	private float startingPosition;
	private float stepSize = 500;
	
	public GrubBullet(Animation anim, Grub g){
		super(anim);
		
		setX(g.getX());
		setY(g.getY());
	
		setVelocityX(g.getDirection() * g.getMaxSpeed() * 10);
		setVelocityY(0);
		
		startingPosition = g.getX();
	}
	
    public Object clone(Player p) {
        // use reflection to create the correct subclass
    	return new Player_bullet(this.getAnimation(), p);
    }
    
    public boolean expired(){
    	return Math.abs(this.getX() - startingPosition) > stepSize;
    }
    
    public boolean isDead(){
    	return dead;
    }
    
    public void setDead(boolean state){
    	dead = state;
    }
}