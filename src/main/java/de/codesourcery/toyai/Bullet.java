package de.codesourcery.toyai;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.ticklisteners.Animator;

public class Bullet extends MoveableEntity implements ITickListener
{
    public float timeRemaining;
    private final Animator animator;
    public final float damage;
    
    public Bullet(Entity owner,Vector2 initialPosition,Vector2 heading,float maxRange,float velocity,float damage) 
    {
        super(EntityType.BULLET, owner);
        
        this.damage = damage;
        
        super.position.set( initialPosition );
        
        super.setOrientation( heading );
        
        super.boundsDirty = true;
        
        this.timeRemaining = maxRange/velocity;
        
        this.velocity.set( super.getOrientation() );
        this.velocity.scl( velocity );
        
        this.animator = new Animator( this , velocity );
    }

    @Override
    public void onCollision(World world, Entity collidingEntity) 
    {
        world.remove( this );
    }
    
    @Override
    public boolean tick(float deltaSeconds) 
    {
        animator.tick( deltaSeconds );
        timeRemaining -= deltaSeconds;
        return timeRemaining > 0;
    }    
}