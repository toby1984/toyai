package de.codesourcery.toyai;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class World implements ITickListener
{
    private final List<Entity> entities = new ArrayList<>();
    private final TickContainer tickContainer = new TickContainer();
    
    public void visitEntities(Consumer<Entity> consumer) 
    {
        entities.forEach( consumer );
    }
    
    public TickContainer tickContainer() {
        return tickContainer;
    }
    
    public void add(Entity e) 
    {
        entities.add( e );
        if ( e instanceof ITickListener ) 
        {
            tickContainer.add( (ITickListener) e);
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
            System.out.println( "REMOVED from world: "+e1);
            if ( e1 instanceof ITickListener) 
            {
                tickContainer.remove( (ITickListener) e1 );
            }
            e1.onRemoveFromWorld(this);
        }
    }
    
    public boolean collidesWith(Entity entity) 
    {
        for ( Entity e : entities ) 
        {
            if ( e != entity && collide( e , entity ) ) {
                return true;
            }
        }
        return false;
    }
    
    public Entity getEntityAt(Vector2 position) 
    {
        final Vector3 tmp = new Vector3(position.x,position.y , 0 );
        for ( Entity e : entities ) {
            if ( e.getBounds().contains( tmp ) ) {
                return e;
            }
        }
        return null;
    }
    
    private boolean collide(Entity e1,Entity e2) 
    {
        BoundingBox b1 = e1.getBounds();
        BoundingBox b2 = e2.getBounds();
        if ( ! b1.isValid() || ! b2.isValid() ) {
            System.err.println("bb invalid");
        }
        return b1.intersects( b2 );
    }
    
    protected static final class CollisionPair 
    {
        public final Entity e1;
        public final Entity e2;
        
        public CollisionPair(Entity e1, Entity e2) {
            this.e1 = e1;
            this.e2 = e2;
        }
    }

    @Override
    public boolean tick(float deltaSeconds) 
    {
        tickContainer.tick( deltaSeconds );
        
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
    
    public void visitNeighbours(Vector2 center,float radius,Consumer<Entity> visitor) 
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
    
    public interface ICallbackWithResult<T> 
    {
        public boolean visit(Entity entity);
        public T getResult();
    }
    
    public <T> T visitNeighboursWithResult(Vector2 center,float radius,ICallbackWithResult<T> visitor) 
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