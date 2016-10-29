package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.95f;

    private boolean onGround;
 
    
    private int health = 20;
    private int maxHealth = 40;
    private int score = 0;
    
  //Logic for health
    public void setHealth(int health) {
    	this.health = health;
    }
    
    public int getHealth() {
    	return this.health;
    }
    
    public int updateHealth(int health, int incr) {
    	if ((health + incr) > 40) {
    		health = 40;
    	} else health = health+incr;
    	return health;
    }
    
    //Logic for score
    public void setScore(int score) {
    	this.score = score;
    }
    
    public int getScore() {
    	return this.score;
    }
    
    public int updateScore(int score) {
    	return score += 1;
    }
    
    
    
    public Player(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }


    public void collideHorizontal() {
        setVelocityX(0);
    }

    
    
    public void collideVertical() {
        // check if collided with ground
        if (getVelocityY() > 0) {
            onGround = true;
        }
        setVelocityY(0);
    }


    public void setY(float y) {
        // check if falling
        if (Math.round(y) > Math.round(getY())) {
            onGround = false;
        }
        super.setY(y);
    }


    public void wakeUp() {
        // do nothing
    }


    /**
        Makes the player jump if the player is on the ground or
        if forceJump is true.
    */
    public void jump(boolean forceJump) {
        if (onGround || forceJump) {
            onGround = false;
            setVelocityY(JUMP_SPEED);
        }
    }


    public float getMaxSpeed() {
        return 0.5f;
    }

}
