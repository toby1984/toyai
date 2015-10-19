package de.codesourcery.toyai;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import de.codesourcery.toyai.Entity.WhiskerConfiguration;
import de.codesourcery.toyai.entities.MoveableEntity;

public class World implements ITickListener
{
    private final List<Entity> entities = new ArrayList<>();
    private final TickContainer tickContainer = new TickContainer();

    private final Kinematics physics = new Kinematics();

    public interface ICallbackWithResult<T>
    {
        public boolean visit(Entity entity);
        public T getResult();
    }

    protected static final class CollisionPair
    {
        public final Entity e1;
        public final Entity e2;

        public CollisionPair(Entity e1, Entity e2)
        {
            this.e1 = e1;
            this.e2 = e2;
        }
    }

    public List<Entity> gather(Predicate<Entity> pred)
    {
    	return entities.stream().filter( pred ).collect( Collectors.toList() );
    }

    public void visitEntities(Consumer<Entity> consumer)
    {
        for (int i = 0 , len = entities.size() ; i < len ; i++)
        {
            consumer.accept( entities.get(i) );
        }
    }

    public <T> T visitEntitiesWithResult(ICallbackWithResult<T> consumer)
    {
        for (int i = 0 , len = entities.size() ; i < len ; i++)
        {
            if ( ! consumer.visit( entities.get(i) ) ) {
            	break;
            }
        }
        return consumer.getResult();
    }

    public TickContainer tickContainer() {
        return tickContainer;
    }

    public void add(Entity e)
    {
        entities.add( e );
        tickContainer.add( e);
        if ( e instanceof MoveableEntity)
        {
            physics.add( (MoveableEntity) e );
        }
    }

    public void add(Entity e1,Entity... other)
    {
        add( e1 );
        if ( other != null )
        {
            for ( Entity e : other ) {
                add( e );
            }
        }
    }

    public void remove(Entity e1)
    {
        if ( entities.remove( e1 ) )
        {
            tickContainer.remove( e1 );
            if ( e1 instanceof MoveableEntity) {
                physics.remove( (MoveableEntity) e1 );
            }
            e1.onRemoveFromWorld(this);
        }
    }

    public boolean collidesWith(Entity entity)
    {
        for (int i = 0 , len = entities.size() ; i < len ; i++)
        {
            final Entity e = entities.get(i);
            if ( e != entity && collide( e , entity ) ) {
                return true;
            }
        }
        return false;
    }

    private boolean collide(Entity e1,Entity e2)
    {
        final BoundingBox b1 = e1.getBounds();
        final BoundingBox b2 = e2.getBounds();
        if ( ! b1.isValid() || ! b2.isValid() ) {
            System.err.println("bb invalid");
        }
        return b1.intersects( b2 );
    }

    public Entity getEntityAt(Vector3 position)
    {
        final Vector3 tmp = new Vector3(position.x,position.y , 0 );
        for (int i = 0 , len = entities.size() ; i < len ; i++) {
            final Entity e = entities.get(i);
            if ( e.getBounds().contains( tmp ) ) {
                return e;
            }
        }
        return null;
    }

    @Override
    public boolean tick(float deltaSeconds)
    {
        // tick entities etc.
        tickContainer.tick( deltaSeconds );

        // run physics
        physics.tick( deltaSeconds );

        // find collisions
        final List<CollisionPair> pair = new ArrayList<>( entities.size() / 2 );
        for ( int i = 0 ; i < entities.size() ; i++ )
        {
            final Entity e1 = entities.get(i);
            final BoundingBox bb1 = e1.getBounds();
            for ( int j = i+1 ; j < entities.size() ; j++ )
            {
                final Entity e2 = entities.get(j);
                if ( bb1.intersects( e2.getBounds() ) )
                {
                    pair.add( new CollisionPair( e1,e2 ) );
                }
            }
        }
        for ( CollisionPair p : pair )
        {
            p.e1.onCollision( this , p.e2 );
            p.e2.onCollision( this , p.e1 );
        }
        return true;
    }

    public static final class ObstacleTestResult
    {
    	/*
    	 * 0b111 =
    	 * 0b1.. = left
    	 * 0b.1. = center
    	 * 0b..1 = right
    	 */
    	public int bitMask;

    	public boolean hasObstacle()
    	{
    		return bitMask != 0;
    	}

    	public void setLeftWhisker(Entity e)
    	{
    		if ( e != null ) {
    			bitMask |= 1 << 2;
    		} else {
    			bitMask &= ~(1<<2);
    		}
		}

    	public void setCenter(Entity e)
    	{
    		if ( e != null )
    		{
    			bitMask |= 1 << 1;
    		}
    		else
    		{
    			bitMask &= ~(1<<1);
    		}
		}

    	public void setRightWhisker(Entity e)
    	{
    		if ( e != null ) {
    			bitMask |= 1 << 0;
    		} else {
    			bitMask &= ~(1<<0);
    		}
		}

    	public boolean leftObstacle() {
    		return (bitMask & 1<<2) != 0;
    	}

    	public boolean centerObstacle() {
    		return (bitMask & 1<<1) != 0;
    	}

    	public boolean rightObstacle() {
    		return (bitMask & 1<<0) != 0;
    	}
    }

    public void checkForObstacle(Entity toCheck,ObstacleTestResult out)
    {
    	final Vector3 intersection = new Vector3();
    	final Ray ray = new Ray();

    	final WhiskerConfiguration whiskerConfiguration = toCheck.getWhiskerConfiguration();

    	// check center ray
    	ray.origin.set( toCheck.position );
    	ray.direction.set( toCheck.getOrientation() );
    	out.setCenter( segmentCast(toCheck, ray , intersection, whiskerConfiguration.centerRayLen ) );

    	// left whisker
    	ray.direction.rotate( Misc.Z_AXIS3 , -whiskerConfiguration.whiskerAngleDeg );
    	out.setLeftWhisker( segmentCast(toCheck, ray , intersection, whiskerConfiguration.whiskerRayLen ) );

    	// right whisker
    	ray.direction.set( toCheck.getOrientation() ).rotate( Misc.Z_AXIS3 , whiskerConfiguration.whiskerAngleDeg );
    	out.setRightWhisker( segmentCast(toCheck, ray , intersection,whiskerConfiguration.whiskerRayLen) );
    }

	private Entity segmentCast(Entity toCheck, final Ray ray, final Vector3 intersection, float segmentLength)
	{
		float closestIntersectionSquared = 0;
    	Entity closestMatch = null;

    	final float segmentLengthSquared = segmentLength * segmentLength;
    	final float cutOffSquared = (segmentLength*2f)*(segmentLength*2f);

        for ( int i = 0 , len = entities.size() ; i < len ; i++ )
        {
            final Entity e = entities.get(i);
            if ( e != toCheck && e.dst2( ray.origin ) < cutOffSquared ) // ignore all entities that seem to be too far away
            {
            	if ( Intersector.intersectRayBounds(ray, e.getBounds(), intersection ) )
            	{
            		final float len2 = ray.origin.dst2( intersection );
            		if ( len2 <= segmentLengthSquared && ( closestMatch == null || len2 < closestIntersectionSquared ) )
            		{
            			closestMatch = e;
            			closestIntersectionSquared = len2;
            		}
            	}
            }
        }
        return closestMatch;
	}

    public void visitNeighbours(Vector3 center,float radius,Consumer<Entity> visitor)
    {
        final float dstSquared = radius*radius;
        for ( int i = 0 , len = entities.size() ; i < len ; i++ )
        {
            final Entity e = entities.get(i);
            if ( e.dst2( center ) <= dstSquared ) {
                visitor.accept( e );
            }
        }
    }

    public <T> T visitNeighboursWithResult(Vector3 center,float radius,ICallbackWithResult<T> visitor)
    {
        final float dstSquared = radius*radius;
        for ( int i = 0 , len = entities.size() ; i < len ; i++ )
        {
            final Entity e = entities.get(i);
            if ( e.dst2( center ) <= dstSquared )
            {
                if ( ! visitor.visit( e ) )
                {
                    break;
                }
            }
        }
        return visitor.getResult();
    }
}