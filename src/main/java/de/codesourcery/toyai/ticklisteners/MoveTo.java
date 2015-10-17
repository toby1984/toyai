package de.codesourcery.toyai.ticklisteners;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.ITickListener;

public class MoveTo implements ITickListener {

    private final MoveableEntity entity;
    private final Vector2 destination = new Vector2();
    private final Vector2 tmp = new Vector2();
    private final Animator animator;
    private Rotate rotate;
    public boolean destinationReached; 
    
    private boolean rotationStarted;
    private boolean rotationDone;
    
    public MoveTo(MoveableEntity e,Vector2 destination) {
        this.entity = e;
        this.destination.set(destination);
        animator = new Animator( e , Entity.MAX_VELOCITY );
    }
    
    @Override
    public boolean tick(float deltaSeconds) 
    {
        tmp.set( destination );
        tmp.sub( entity.position );
        
        if ( ! rotationDone ) 
        {
            if ( ! rotationStarted ) 
            {
                rotate = new Rotate( entity , tmp ); 
                rotationStarted = true;
            }
            
            if ( ! rotate.orientationReached ) 
            {
                rotate.tick( deltaSeconds );
                return true;
            }
            rotationDone = true;
        }
        
        float dst = tmp.len();
        
        if ( dst > 2 ) 
        {
            float acceleration = Entity.MAX_ACCELERATION;
            if ( dst < 5 ) 
            {
                acceleration = acceleration*(dst/5);
            }
            entity.acceleration = acceleration;
            animator.tick( deltaSeconds );
            return true;
        }
        System.err.println("destination reached");
        destinationReached = true;
        onTickListenerRemove();
        return false;
    }
    
    @Override
    public void onTickListenerRemove()
    {
        entity.stopMoving();
    }
}