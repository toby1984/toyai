package de.codesourcery.toyai.ticklisteners;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.Tank;
import de.codesourcery.toyai.World;

public class ShootAt implements IBehaviour
{
    private final Tank entity;
    private final IBehaviour wrapper;
    private final Vector2 target;
    
    public ShootAt(World world, Tank e,Vector2 target) 
    {
        this.entity = e;
        this.target = new Vector2(target);
        
        // need to move closer
        final IBehaviour b1 = new MoveIntoRange( entity , target , entity.getEngagementDistance()*0.9f); // fuzzyness of 0.9f because MoveTo isn't 100% accurate
        
        // rotate towards target
        final Vector2 targetVec = new Vector2(target);
        targetVec.sub( entity.position );
        IBehaviour b2 = new Rotate(entity,targetVec);
        
        // shoot
        final IBehaviour b3 = new IBehaviour() {
            
            private boolean shotFired = false;
            
            private float failureTime;
            
            @Override
            public String toString() {
                return "FireWhenReady";
            }
            
            @Override
            public Result tick(float deltaSeconds) 
            {
                if ( shotFired ) {
                    return Result.SUCCESS;
                }
                if ( entity.isReadyToShoot() ) 
                {
                    System.out.println("Firing shot");
                    entity.fireShot( world , target );
                    shotFired = true;
                    return Result.SUCCESS;
                }
                System.out.println(entity.id+" is waiting to fire...");
                failureTime += deltaSeconds;
                if ( failureTime > 3 ) 
                {
                    System.err.println(entity.id+" failed to fire for more than 3 seconds...");
                    return Result.FAILURE;
                }
                return Result.PENDING;
            }
        };
        wrapper = b1.andThen( b2 ).andThen( b3 );
    }
    
    @Override
    public void onCancel() {
        wrapper.onCancel();
    }
    
    @Override
    public String toString() {
        return "Shoot @ "+target+" [ "+wrapper+" ]";
    }
    
    @Override
    public Result tick(float deltaSeconds) {
        return wrapper.tick( deltaSeconds );
    }
}