package com.brackeen.javagamebook.tilegame.sprites;

import java.util.ArrayList;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Grub is a Creature that moves slowly on the ground.
*/

public class Grub extends Creature {
	
	private boolean canShoot = false;
	private boolean onScreen = false;
	private int timeOnScreen = 0;
	private float playerInitialPosition;
	private int last_grub_bullet = 0;
	
	private int current_direction = -1;
	
	private ArrayList<GrubBullet> newGrubBullets= new ArrayList<>();

	public void updateLastGrubBullet(int update){
		last_grub_bullet += update;
	}
	
	public void setLastGrubBullet(int update){
		last_grub_bullet = update;
	}
	
	public int getLastGrubBullet(){
		return last_grub_bullet;
	}
	
    public Grub(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
        this.setDirection(-1);
    }

    public ArrayList<GrubBullet> getGrubBullets(){
    	return newGrubBullets;
    }
    
    public void setGrubBullets(ArrayList<GrubBullet> newList){
    	newGrubBullets = newList;
    }
    
    public float getMaxSpeed() {
        return 0.05f;
    }
    
    public void wakeUp() {
        if (getState() == STATE_NORMAL && getVelocityX() == 0) {
            setVelocityX(-getMaxSpeed());
        }
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
    
    public float getPlayerInitialPosition(){
    	return playerInitialPosition;
    }
    
    public void setPlayerInitialPosition(float position){
    	playerInitialPosition = position;
    }
    
    

}
