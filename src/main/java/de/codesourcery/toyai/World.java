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

    @Override
    public boolean tick(float deltaSeconds) 
    {
        return tickContainer.tick( deltaSeconds );
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
}