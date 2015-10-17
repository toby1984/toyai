package de.codesourcery.toyai.ticklisteners;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.ITickListener;
import de.codesourcery.toyai.Tank;
import de.codesourcery.toyai.World;

public class ShootAt implements ITickListener
{
    private final Tank entity;
    private final Vector2 target;
    
    private Rotate rotate = null;
    private MoveTo moveTo = null;
    
    private final World world;
    
    public ShootAt(World world, Tank e,Vector2 target) 
    {
        this.entity = e;
        this.target = new Vector2(target);
        this.world = world;
    }

    @Override
    public boolean tick(float deltaSeconds) 
    {
        float distance = entity.dst( target );
        if ( distance > entity.getEngagementDistance() ) 
        {
            // need to move closer
            if ( moveTo == null ) {
                Vector2 tmp = new Vector2( entity.position );
                tmp.sub( target );
                tmp.nor();
                tmp.scl( entity.getEngagementDistance()*0.9f ); // fuzzyness of 0.9f because MoveTo isn't 100% accurate
                tmp.add( target );
                
                moveTo = new MoveTo( entity , tmp ); 
            }
            if ( ! moveTo.destinationReached ) {
                moveTo.tick(deltaSeconds);
                return true;
            }
            moveTo = null;
        }
        
        // rotate towards target
        Vector2 tmp = new Vector2( target );
        tmp.sub( entity.position );
        
        if ( ! Rotate.isOrientedTowards( entity , tmp ) ) 
        { 
            if ( rotate == null ) 
            {
                final Vector2 targetVec = new Vector2(target);
                targetVec.sub( entity.position );
                rotate = new Rotate(entity,targetVec);
            }
            if ( ! rotate.orientationReached ) {
                rotate.tick( deltaSeconds );
                return true;
            }
            rotate = null;
        }        
        
        // shoot
        if ( entity.isReadyToShoot() ) 
        {
            entity.fireShot( world , target );
            return false;
        }
        return true;
    }
    
    @Override
    public void onTickListenerRemove() {
        entity.stopMoving();
    }
}