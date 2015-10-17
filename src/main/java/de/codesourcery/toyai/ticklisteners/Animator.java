package de.codesourcery.toyai.ticklisteners;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.ITickListener;

public class Animator implements ITickListener {

    private final MoveableEntity entity;
    
    private final Vector2 tmp = new Vector2();
    
    private final float maxVelocity;
    
    public Animator(MoveableEntity e,float maxVelocity) {
        this.entity = e;
        this.maxVelocity = maxVelocity;
    }
    
    @Override
    public boolean tick(float deltaSeconds) 
    {
        tmp.set( entity.orientation );
        tmp.scl( entity.acceleration );
        
        entity.velocity.x += tmp.x*deltaSeconds;
        entity.velocity.y += tmp.y*deltaSeconds;
        
        entity.velocity.limit(maxVelocity);
        
        entity.position.x += entity.velocity.x * deltaSeconds;
        entity.position.y += entity.velocity.y * deltaSeconds;
        
        entity.orientation.set( entity.velocity ); 
        
        entity.boundsDirty = true;
        return true;
    }
}
