package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.*;

/**
    A Creature is a Sprite that is affected by gravity and can
    die. It has four Animations: moving left, moving right,
    dying on the left, and dying on the right.
*/
public class Player_bullet extends Sprite {

	/**
        Amount of time to go from STATE_DYING to STATE_DEAD.
    */
	private boolean dead = false;
	
	public Player_bullet(Animation anim, Player p){
		super(anim);
		
		setX(p.getX());
		setY(p.getY());
	
		setVelocityX(p.getDirection() * p.getMaxSpeed() * 2);
		setVelocityY(0);
	}
	
    public Object clone(Player p) {
        // use reflection to create the correct subclass
    	return new Player_bullet(this.getAnimation(), p);
    }
    
    public boolean isDead(){
    	return dead;
    }
    
    public void setDead(boolean state){
    	dead = state;
    }
}