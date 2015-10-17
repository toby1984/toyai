package de.codesourcery.toyai.ticklisteners;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.Entity;

public class MoveableEntity extends Entity 
{
    public final Vector2 velocity = new Vector2();
    public float acceleration = 0;
    
    public MoveableEntity(EntityType type, Entity owner) {
        super(type, owner);
    }
    
    public boolean isMoving() 
    {
        return velocity.len2() != 0;
    }    
    
    public void stopMoving() 
    {
         velocity.setZero();
         acceleration = 0;
    }
}
