package de.codesourcery.toyai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.ticklisteners.MoveableEntity;

public class Tank extends MoveableEntity implements ITickListener 
{
    public static final int MAX_BULLETS_IN_FLIGHT = 4;
    
    private static final float TURRET_RANGE = 100;
    
    private static final float BULLET_VELOCITY = 200;
    
    public static final float SECONDS_PER_SHOT  = 3;
    
    private final List<Bullet> bulletsInFlight = new ArrayList<>();
    
    private float timeBeforeNextShotReady = 0;
    
    public Tank(Entity owner) 
    {
        super(EntityType.TANK, owner);
    }

    @Override
    public boolean isReadyToShoot() 
    {
        return timeBeforeNextShotReady <= 0 && bulletsInFlight.size() < MAX_BULLETS_IN_FLIGHT;
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
        
        final Vector2 initPos = new Vector2( this.orientation );
        initPos.scl( height*2 );
        initPos.add( position );
        
        final Vector2 orientation = new Vector2(destination);
        orientation.sub( initPos );
        orientation.nor();
        
        final Bullet bullet = new Bullet(this, initPos , orientation , TURRET_RANGE , BULLET_VELOCITY ) 
        {
            @Override
            public void onRemoveFromWorld(World world) {
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
    public boolean tick(float deltaSeconds) 
    {
        timeBeforeNextShotReady -= deltaSeconds;
        return true;
    }
}
