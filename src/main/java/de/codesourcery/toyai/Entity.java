package de.codesourcery.toyai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;

import de.codesourcery.toyai.IBehaviour.Result;

public abstract class Entity implements ITickListener
{
    public static long ID_GENERATOR = 0;
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
    
    public static final float ROT_DEG_PER_SECOND = 60f;
    public static final float ROT_RAD_PER_SECOND = (float) Math.toRadians( ROT_DEG_PER_SECOND );
    
    public static final float MAX_VELOCITY = 40f;
    
    public static final float MAX_ACCELERATION = 80f;
    public static final float MAX_DECELARATION = 160f;
    public static final float MAX_ACCELERATION_SQUARED = MAX_ACCELERATION*MAX_ACCELERATION;    
    
    public final long id = ID_GENERATOR++;
    
    private IBehaviour behaviour = IBehaviour.NOP;
    
    public final Vector2 position = new Vector2();
    
    private final Vector2 orientation = new Vector2(0f,1f).nor();
    
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
    
    public Vector2 getOrientation() {
        return orientation;
    }
    
    public void setOrientation(float x,float y) {
        this.orientation.set( x,y).nor();
    }    
    
    public void setOrientation(Vector2 v) {
        this.orientation.set( v ).nor();
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
    
    public void onRemoveFromWorld(World world) {        
    }
    
    public final Entity getRootOwner() 
    {
        if ( owner == null ) {
            return this;
        }
        return owner.getRootOwner();
    }

    @Override
    public boolean tick(float deltaSeconds) 
    {
        if ( behaviour.tick( deltaSeconds ) != Result.PENDING ) {
            behaviour = IBehaviour.NOP;
        }
        tickHook( deltaSeconds );
        return true;
    }
    
    protected void tickHook(float deltaSeconds) {
        
    }
    
    public void setBehaviour(IBehaviour behaviour) 
    {
        if ( this.behaviour != behaviour ) {
            this.behaviour.onCancel();
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
    
    public final boolean hasType(Entity.EntityType t) 
    {
        return this.type == t;
    }
}