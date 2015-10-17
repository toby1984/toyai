package de.codesourcery.toyai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Entity 
{
    public static enum EntityType 
    {
        TANK 
        {
            public boolean canShoot() {
                return true;
            }            
        },
        PLAYER 
        {
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
    
    public static final float ROT_DEG_PER_SECOND = 45f;
    public static final float ROT_RAD_PER_SECOND = (float) Math.toRadians( ROT_DEG_PER_SECOND );
    
    public static final float MAX_VELOCITY = 60f;
    
    public static final float MAX_ACCELERATION = 20f;
    public static final float MAX_ACCELERATION_SQUARED = MAX_ACCELERATION*MAX_ACCELERATION;    
    
    public final Vector2 position = new Vector2();
    
    public final Vector2 orientation = new Vector2(0f,1f).nor();
    
    public boolean boundsDirty = true;
    
    private final BoundingBox boundingBox = new BoundingBox();
    
    public final float width;
    public final float height;
    
    public final EntityType type;
    public final Entity owner;
    
    public Entity(EntityType type,Entity owner) {
        this.type = type;
        this.owner = owner;
        this.width = 10;
        this.height = 10;
    }
    
    public final float dst(Vector2 p) {
        return position.dst( p );
    }
    
    public final float dst2(Vector2 p) {
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
    public String toString() {
        return "Entity: "+type+" , owner: "+owner+" , bounds: "+getBounds().min+" - "+getBounds().max;
    }
    
    public boolean isMoving() 
    {
        return false;
    }    
    
    public boolean isReadyToShoot() {
        return false;
    }
    
    public void onRemoveFromWorld(World world) {        
    }
    
    public final Entity getRootOwner() 
    {
        if ( owner == null ) {
            return this;
        }
        return owner.getRootOwner();
    }
}