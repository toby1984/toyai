package de.codesourcery.toyai;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.entities.MoveableEntity;

public final class Kinematics
{
	private static final boolean CONSTRAINT_TO_GAMESCREEN = true;

	private static final boolean DEBUG_PRINT_FLIP = false;

	private static final int INITIAL_ARRAY_SIZE = 10;

	private final Vector3 tmp = new Vector3();
	private final Vector3 tmp2 = new Vector3();
	private final Vector3 tmpV = new Vector3();
	private final Vector3 tmpPos = new Vector3();
	private MoveableEntity[] entities = new MoveableEntity[INITIAL_ARRAY_SIZE];

	public void add(MoveableEntity e)
	{
		if ( e == null ) {
			throw new IllegalArgumentException("Entity must not be NULL");
		}
		int i = 0;
		for ( final int len = entities.length ; i < len ; i++ )
		{
			final Entity existing = entities[i];
			if ( existing == e ) {
				System.err.println("Entity already registered: "+e);
				return;
			}
			if ( existing == null ) {
				entities[i] = e;
				return;
			}
		}
		expandArrays();
		entities[i] = e;
	}

	public void remove(MoveableEntity e)
	{
		if ( e == null ) {
			throw new IllegalArgumentException("Entity must not be NULL");
		}
		for ( int i = 0 , len = entities.length ; i < len ; i++ )
		{
			if ( entities[i] == e ) {
				entities[i] = null;
			}
		}
	}

	private void expandArrays()
	{
		final int newLen = entities.length*2;
		entities = expandArray( entities , newLen );
	}

	private static MoveableEntity[] expandArray(MoveableEntity[] existing,int newSize)
	{
		final MoveableEntity[] newArray = new MoveableEntity[newSize];
		System.arraycopy( existing , 0, newArray , 0  ,existing.length );
		return newArray;
	}

	public void tick(float deltaSeconds)
	{
		for ( int i = 0 , len = entities.length ; i < len ; i++ )
		{
			final MoveableEntity entity = entities[i];
			if ( entity != null ) {
				tick( entity , deltaSeconds);
			}
		}
	}

	private void tick(MoveableEntity entity,float deltaSeconds)
	{
	    // update position
		tmpPos.x = entity.position.x + entity.velocity.x * deltaSeconds;
		tmpPos.y = entity.position.y + entity.velocity.y * deltaSeconds;
			
        // update orientation
        float rot = entity.rotationInRadPerSecond*deltaSeconds;
        final Vector3 orientation = entity.getOrientation();
        orientation.rotateRad( Misc.Z_AXIS3 , rot );
        orientation.nor();    
        
		// calculate acceleration
        tmp.set( orientation );
        tmp.nor();
        tmp.scl( entity.getAcceleration() );
        
        tmpV.x = entity.velocity.x + tmp.x*deltaSeconds;
        tmpV.y = entity.velocity.y + tmp.y*deltaSeconds;
        tmpV.scl( 0.995f ); // apply some drag
        
        tmpV.limit( entity.maxVelocity );
        
		if ( CONSTRAINT_TO_GAMESCREEN )
		{
			final float xLimit = GameScreen.MAX_X/2;
			final float yLimit = GameScreen.MAX_Y/2;

			if ( tmpPos.x <= -xLimit ) {
				tmpPos.x = -xLimit+1;

				if ( Math.signum( tmpV.x ) == Math.signum( orientation.x ) ) {
					orientation.x = -orientation.x;
				}
				tmpV.x = -tmpV.x;
				if (DEBUG_PRINT_FLIP) {
					System.err.println("Flipped X");
				}
			}
			else if ( tmpPos.x >= xLimit )
			{
				tmpPos.x = xLimit-1;
				if ( Math.signum( tmpV.x ) == Math.signum( orientation.x ) ) {
					orientation.x = -orientation.x;
				}
				tmpV.x = -tmpV.x;
				if (DEBUG_PRINT_FLIP) {
					System.err.println("Flipped X");
				}
			}

			if ( tmpPos.y <= -yLimit ) {
				tmpPos.y = -yLimit+1;

				if ( Math.signum( tmpV.y ) == Math.signum( orientation.y ) ) {
					orientation.y = -orientation.y;
				}
				tmpV.y = -tmpV.y;
				if (DEBUG_PRINT_FLIP) {
					System.err.println("Flipped Y");
				}
			}
			else if ( tmpPos.y >= yLimit )
			{
				tmpPos.y = yLimit-1;
				if ( Math.signum( tmpV.y ) == Math.signum( orientation.y ) ) {
					orientation.y = -orientation.y;
				}
				tmpV.y = -tmpV.y;
				if (DEBUG_PRINT_FLIP) {
					System.err.println("Flipped Y");
				}
			}
		}

		entity.velocity.set( tmpV );
		entity.position.set( tmpPos );

		entity.boundsDirty = true;
	}

	protected static float clamp(float actual,float min,float max)
	{
		return actual < min ? min : ( actual > max ? max : actual );
	}
}