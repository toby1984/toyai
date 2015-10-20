package de.codesourcery.toyai.entities;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Blackboard;
import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.ITickListener;
import de.codesourcery.toyai.World;

public final class Tank extends MoveableEntity implements ITickListener
{
    public static final int MAX_BULLETS_IN_FLIGHT = 4;

    public static final float TURRET_RANGE = 100;
    public static final float BULLET_VELOCITY = 200;
    public static final float BULLET_DAMAGE = 33;

    public static final float SEEK_RANGE = Tank.TURRET_RANGE*1.5f;

    public static final float SECONDS_PER_SHOT  = 0.5f;

    private final List<Bullet> bulletsInFlight = new ArrayList<>();
    private float health = 99;

    private float timeBeforeNextShotReady = 0;

    public Tank(Entity owner,IBlackboard bb)
    {
        super(EntityType.TANK, owner,bb,80f);
    }

    @Override
    public void onCollision(World world, Entity collidingEntity)
    {
        if ( collidingEntity.type == EntityType.BULLET )
        {
            float dmg = ((Bullet) collidingEntity).damage;
            health -= dmg;
            if ( health <= 0 )
            {
                world.remove( this );
            }
        }
    }

    @Override
	public final boolean isAlive() {
        return health > 0;
    }

    @Override
    public boolean isReadyToShoot()
    {
        if ( timeBeforeNextShotReady >  0 ) {
            return false;
        }
        if ( bulletsInFlight.size() >= MAX_BULLETS_IN_FLIGHT) {
            System.out.println("Too many bullets in flight: "+this);
            return false;
        }
        return true;
    }

    public float getEngagementDistance() {
        return TURRET_RANGE;
    }

    public void fireShot()
    {
        if ( ! isReadyToShoot() )
        {
            throw new IllegalStateException("Not ready to shoot yet");
        }

        final Vector3 initPos = new Vector3( getOrientation() );
        initPos.scl( getHeight()*2 );
        initPos.add( position );

        final Vector3 orientation = new Vector3( getOrientation() );

        final World world = blackboard.getWorld();

        final Bullet bullet = new Bullet(this, initPos , orientation , TURRET_RANGE , BULLET_VELOCITY , BULLET_DAMAGE , new Blackboard( world ) )
        {
            @Override
            public void onRemoveFromWorldHook(World world) {
                bulletsInFlight.remove( this );
            }

            @Override
            public void onTickListenerRemove()
            {
                world.remove( this );
            }
        };
        bulletsInFlight.add( bullet );
        world.add( bullet );
        timeBeforeNextShotReady = SECONDS_PER_SHOT;
    }

    @Override
    public boolean tickHook(float deltaSeconds)
    {
        timeBeforeNextShotReady -= deltaSeconds;
        return true;
    }

    @Override
    public String toString() {
        return super.toString()+", health: "+health;
    }
}