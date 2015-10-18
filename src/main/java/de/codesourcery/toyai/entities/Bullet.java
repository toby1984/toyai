package de.codesourcery.toyai.entities;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.ITickListener;
import de.codesourcery.toyai.World;

public class Bullet extends MoveableEntity implements ITickListener
{
    public float timeRemaining;
    public final float damage;
    
    public Bullet(Entity owner,Vector2 initialPosition,Vector2 heading,float maxRange,float velocity,float damage,IBlackboard bb) 
    {
        super(EntityType.BULLET, owner,bb,velocity);
        
        this.damage = damage;
        
        super.position.set( initialPosition );
        
        super.setOrientation( heading );
        
        super.boundsDirty = true;
        
        this.timeRemaining = maxRange/velocity;
        
        this.velocity.set( super.getOrientation() );
        this.velocity.scl( velocity );
    }

    @Override
    public void onCollision(World world, Entity collidingEntity) 
    {
        world.remove( this );
    }
    
    @Override
    public boolean tickHook(float deltaSeconds) 
    {
        timeRemaining -= deltaSeconds;
        return timeRemaining > 0;
    }    
}