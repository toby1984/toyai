package de.codesourcery.toyai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public final class Tank extends MoveableEntity implements ITickListener 
{
    public static final int MAX_BULLETS_IN_FLIGHT = 4;
    
    public static final float TURRET_RANGE = 100;
    public static final float BULLET_VELOCITY = 200;
    public static final float BULLET_DAMAGE = 20;
    
    public static final float SECONDS_PER_SHOT  = 1;
    
    private final List<Bullet> bulletsInFlight = new ArrayList<>();
    private float health = 100;
    
    private float timeBeforeNextShotReady = 0;
    
    public Tank(Entity owner) 
    {
        super(EntityType.TANK, owner);
    }
    
    @Override
    public void onCollision(World world, Entity collidingEntity) 
    {
        if ( collidingEntity.type == EntityType.BULLET ) 
        {
            float dmg = ((Bullet) collidingEntity).damage;
            health -= dmg;
            System.out.println( this+" was hit by "+collidingEntity+" for "+dmg+", health is now: "+health);
            if ( health <= 0 ) 
            {
                world.remove( this );
            }
        }
    }
    
    public final boolean isAlive() {
        return health > 0;
    }
    
    public final boolean isDead() {
        return health <= 0;
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
    
    public void fireShot(World world,Vector2 destination) 
    {
        if ( ! isReadyToShoot() ) 
        {
            throw new IllegalStateException("Not ready to shoot yet");
        }
        
        final Vector2 initPos = new Vector2( getOrientation() );
        initPos.scl( height*2 );
        initPos.add( position );
        
        final Vector2 orientation = new Vector2(destination);
        orientation.sub( initPos );
        orientation.nor();
        
        final Bullet bullet = new Bullet(this, initPos , orientation , TURRET_RANGE , BULLET_VELOCITY , BULLET_DAMAGE ) 
        {
            @Override
            public void onRemoveFromWorld(World world) {
                System.out.println("Bullet removed from world");
                bulletsInFlight.remove( this );
            }

            @Override
            public void onTickListenerRemove() 
            {
                System.out.println("Bullet expired");
                world.remove( this );
            }
        };
        bulletsInFlight.add( bullet );
        world.add( bullet );
        timeBeforeNextShotReady = SECONDS_PER_SHOT;
    }

    @Override
    public void tickHook(float deltaSeconds) 
    {
        timeBeforeNextShotReady -= deltaSeconds;
    }
    
    @Override
    public String toString() {
        return super.toString()+", health: "+health;
    }
}
