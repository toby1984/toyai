package de.codesourcery.toyai.entities;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.ITickListener;
import de.codesourcery.toyai.World;

public class Bullet extends MoveableEntity implements ITickListener
{
	public static final int RADIUS = 3;

	private final Vector3 initialPosition = new Vector3();

    public float timeRemaining;
    public final float damage;

    public Bullet(Entity owner,Vector3 initialPosition,Vector3 heading,float maxRange,float velocity,float damage,IBlackboard bb)
    {
        super(EntityType.BULLET, owner,bb,velocity);

        this.damage = damage;

        setWidth( RADIUS*2 );
        setHeight( RADIUS*2 );

        this.initialPosition.set( initialPosition );
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
    	if ( ! collidingEntity.hasType( EntityType.BULLET ) ) {
    		world.remove( this );
    	}
    }

    @Override
    public boolean tickHook(float deltaSeconds)
    {
        timeRemaining -= deltaSeconds;
        return timeRemaining > 0;
    }

	@Override
	public boolean isAlive() {
		return timeRemaining > 0;
	}
}