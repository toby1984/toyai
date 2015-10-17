package de.codesourcery.toyai;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.ticklisteners.Animator;
import de.codesourcery.toyai.ticklisteners.MoveableEntity;

public class Bullet extends MoveableEntity implements ITickListener
{
    public final Vector2 initialPosition;
    public float distanceRemaining;
    private final Animator animator;
    
    private final Vector2 previousPosition = new Vector2();
    
    public Bullet(Entity owner,Vector2 initialPosition,Vector2 heading,float maxRange,float velocity) 
    {
        super(EntityType.BULLET, owner);
        super.position.set( initialPosition );
        
        super.orientation.set( heading );
        super.orientation.nor();
        
        super.boundsDirty = true;
        
        this.initialPosition = new Vector2( initialPosition );
        this.distanceRemaining = maxRange;
        
        this.velocity.set( super.orientation );
        this.velocity.scl( velocity );
        
        this.animator = new Animator( this , velocity );
    }

    @Override
    public boolean tick(float deltaSeconds) 
    {
        previousPosition.set( position );
        animator.tick( deltaSeconds );
        distanceRemaining -= position.dst( previousPosition );
        return distanceRemaining > 0;
    }
}