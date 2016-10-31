package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Grub is a Creature that moves slowly on the ground.
*/

public class Grub extends Creature {
	
	private boolean canShoot = false;
	private boolean onScreen = false;
	private int timeOnScreen = 0;

    public Grub(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }


    public float getMaxSpeed() {
        return 0.05f;
    }
    
    public void wakeUp() {
        if (getState() == STATE_NORMAL && getVelocityX() == 0) {
            setVelocityX(-getMaxSpeed());
        }
        canShoot = true;
    }
    
    public void onScreen(boolean update){
    	onScreen = update;
    }
    
    public boolean isOnScreen(){
    	return onScreen;
    }
    
    public void setShoot(boolean update){
    	canShoot = update;
    }
    
    public boolean getShoot(){
    	return canShoot;
    }
    
    public void updateTimeOnScreen(int increment){
    	timeOnScreen += increment;
    }
    
    public void setTimeOnScreen(int time){
    	timeOnScreen = time;
    }
    
    public int getTimeOnScreen(){
    	return timeOnScreen;
    }

}
