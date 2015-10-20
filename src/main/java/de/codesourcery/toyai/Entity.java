package de.codesourcery.toyai;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import de.codesourcery.toyai.IBehaviour.Result;

public abstract class Entity implements ITickListener
{
    public static long ID_GENERATOR = 0;

    public static final class WhiskerConfiguration
    {
    	public final float centerRayLen;
    	public final float whiskerRayLen;

    	/*
    	 * This is the angle between the center ray
    	 * and the whisker.
    	 */
    	public final float whiskerAngleDeg;

		public WhiskerConfiguration(float centerRayLen, float whiskerRayLen, float whiskerAngleDeg) {
			this.centerRayLen = centerRayLen;
			this.whiskerRayLen = whiskerRayLen;
			this.whiskerAngleDeg = whiskerAngleDeg;
		}

		@Override
		public String toString() {
			return "center_ray: "+centerRayLen+", whiskers: "+whiskerRayLen+", whisker_angle: "+whiskerAngleDeg+"°";
		}
    }

    public static enum EntityType
    {
        TANK
        {
            @Override
			public boolean canShoot() {
                return true;
            }
        },
        PLAYER
        {
            @Override
			public boolean canMove() {
                return false;
            }
        },
        BULLET;

        public boolean canShoot() {
            return false;
        }

        public boolean canMove() {
            return true;
        }
    }

    public static final float ROT_DEG_PER_SECOND = 360f;
    public static final float ROT_RAD_PER_SECOND = ROT_DEG_PER_SECOND * Misc.TO_RAD;

    public static final float MAX_ACCELERATION = 80f;
    public static final float MAX_DECELARATION = 160f;
    public static final float MAX_ACCELERATION_SQUARED = MAX_ACCELERATION*MAX_ACCELERATION;

    public final long id = ID_GENERATOR++;

    private IBehaviour behaviour = IBehaviour.NOP;

    public final Vector3 position = new Vector3();

    private final Vector3 orientation = new Vector3(0f,1f,0);

    public boolean boundsDirty = true;

    private final BoundingBox boundingBox = new BoundingBox();

    private float width;
    private float height;

    public final EntityType type;
    public final Entity owner;

    public final IBlackboard blackboard;

    private WhiskerConfiguration whiskerConfiguration;

    public Entity(EntityType type,Entity owner,IBlackboard blackboard) {
        this.type = type;
        this.owner = owner;
        this.width = 10;
        this.height = 10;
        this.blackboard = blackboard;
        updateWhiskerConfiguration();
    }

    public final WhiskerConfiguration getWhiskerConfiguration() {
		return whiskerConfiguration;
	}

    private void updateWhiskerConfiguration()
    {
    	/*
    	 *   c/|
    	 *   / |a
    	 *A +--+
    	 *   b
    	 *
    	 *  tan A = a/b  => A = atan( a/b )
    	 */
    	final float a = height*3;
    	final float b = width*2;

    	// calculate 90.0 - angle because the whisker ray angle is actually the angle between the center ray
    	// and the whisker, not between the whisker and the X axis
    	float whiskerAngleDeg = 90.0f - (float) (Misc.TO_DEG * Math.atan2( a , b ));
    	float whiskerRayLen = (float) Math.sqrt( a*a + b*b );
		this.whiskerConfiguration = new WhiskerConfiguration( height*2.5f , whiskerRayLen , whiskerAngleDeg );
		System.out.println( width+"x"+height+" => "+whiskerConfiguration);
    }

    public final float getWidth() {
		return width;
	}

    public final float getHeight() {
		return height;
	}

    public final void setWidth(float width) {
		this.width = width;
		updateWhiskerConfiguration();
	}

    public final void setHeight(float height) {
		this.height = height;
		updateWhiskerConfiguration();
	}

    public final float getOrientationInRad() {
        return Misc.angleY( orientation );
    }

    public final Vector3 getOrientation()
    {
        return orientation;
    }

    public final void setOrientation(float x,float y)
    {
    	if ( x == 0 && y == 0 ) {
    		throw new RuntimeException("orientation must not be NULL");
    	}
        this.orientation.set( x,y ,0 ).nor();
    }

    public final void setOrientation(Vector3 v) {
        this.orientation.set( v ).nor();
    }

    public final float dst(Vector3 p) {
        return position.dst( p );
    }

    public final float dst2(Vector3 p) {
        return position.dst2( p );
    }

    public final BoundingBox getBounds()
    {
        if ( boundsDirty )
        {
            boundingBox.min.set( position.x , position.y , -1 ); // need to use different Z coordinates for min and max, otherwise BoundingBox#isValid() will always return false
            boundingBox.max.set( position.x , position.y , 1 );

            boundingBox.min.x -= width/2f;
            boundingBox.min.y -= height/2f;

            boundingBox.max.x += width/2f;
            boundingBox.max.y += height/2f;

            boundingBox.set( boundingBox.min , boundingBox.max );
            boundsDirty = false;
        }
        return boundingBox;
    }

    @Override
    public String toString()
    {
        if ( owner != null ) {
            return "Entity #"+id+": "+type+" , owner: "+owner.id+", behaviour: "+behaviour;
        }
        return "Entity #"+id+": "+type+" , owner: NULL, behaviour: "+behaviour;
    }

    public boolean isMoving()
    {
        return false;
    }

    public boolean isReadyToShoot() {
        return false;
    }

    public final void onRemoveFromWorld(World world)
    {
    	try {
    		onRemoveFromWorldHook(world);
    	}
    	finally
    	{
	    	if ( owner != null )
	    	{
	    		owner.childRemovedFromWorld(this);
	    	}
    	}
    }

    protected void onRemoveFromWorldHook(World world) {

    }

    public void childRemovedFromWorld(Entity child) {

    }

    public final Entity getRootOwner()
    {
        if ( owner == null ) {
            return this;
        }
        return owner.getRootOwner();
    }

    @Override
    public final boolean tick(float deltaSeconds)
    {
        if ( behaviour.tick( deltaSeconds, blackboard ) != Result.PENDING ) {
            behaviour = IBehaviour.NOP;
        }
        return tickHook( deltaSeconds );
    }

    protected boolean tickHook(float deltaSeconds) {
        return true;
    }

    public void setBehaviour(IBehaviour behaviour)
    {
        if ( this.behaviour != behaviour ) {
            this.behaviour.discard(blackboard);
        }
        System.out.println("Setting behaviour "+behaviour+" on "+this);
        this.behaviour = behaviour;
    }

    public IBehaviour getBehaviour() {
        return behaviour;
    }

    public void onCollision(World world,Entity collidingEntity)
    {
    }

    public final boolean is(Entity.EntityType t) {
        return type == t;
    }

    public final boolean hasType(Entity.EntityType t)
    {
        return this.type == t;
    }

    public abstract boolean isAlive();

    public final boolean isDead() {
        return ! isAlive();
    }
}