package de.codesourcery.toyai.ticklisteners;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.MoveableEntity;
import de.codesourcery.toyai.IBehaviour.Result;

public class Animator implements IBehaviour {

    private final MoveableEntity entity;
    
    private final Vector2 tmp = new Vector2();
    private final Vector2 tmpV = new Vector2();
    private final Vector2 tmpPos = new Vector2();
    
    private final float maxVelocity;
    
    public Animator(MoveableEntity e,float maxVelocity) {
        this.entity = e;
        this.maxVelocity = maxVelocity;
    }
    
    @Override
    public Result tick(float deltaSeconds) 
    {
        tmp.set( entity.getOrientation() );
        tmp.scl( entity.acceleration );
        
        tmpV.x = entity.velocity.x + tmp.x*deltaSeconds;
        tmpV.y = entity.velocity.y + tmp.y*deltaSeconds;
        
        tmpV.limit(maxVelocity);
        
        tmpPos.x = entity.position.x + tmpV.x * deltaSeconds;
        tmpPos.y = entity.position.y + tmpV.y * deltaSeconds;
        
        if (  tmpPos.x < -300 ||  tmpPos.x > 300 || 
              tmpPos.y < -220 ||  tmpPos.y > 220 ) 
        {
            System.err.println("MoveTo has wandered out of the screen area");
            entity.stopMoving();
            entity.position.x = clamp( entity.position.x , -299 , 299 );
            entity.position.y = clamp( entity.position.y , -219 , 219 );
            return Result.FAILURE;
        }         
        
        entity.velocity.set( tmpV );
        entity.position.set( tmpPos );
        
        if ( tmpV.len2() > 0.1*0.1 ) {
            entity.setOrientation( tmpV );
        }
        entity.boundsDirty = true;
        return Result.PENDING;
    }
    
    protected static float clamp(float actual,float min,float max)
    {
        return actual < min ? min : ( actual > max ? max : actual );
    }    
}
