package de.codesourcery.toyai.ticklisteners;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.Entity.EntityType;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.Tank;
import de.codesourcery.toyai.World;
import de.codesourcery.toyai.World.ICallbackWithResult;

public class SeekAndDestroy implements IBehaviour 
{
    private final Tank entity;
    private final World world;
    
    private final float SEEK_RANGE = Tank.TURRET_RANGE*2;
    private Tank currentTarget;
    
    private IBehaviour wrapper;
    
    public SeekAndDestroy(World world,Tank entity) {
        this.entity = entity;
        this.world = world;
    }
    
    protected final class Visitor implements ICallbackWithResult<Tank> 
    {
        private Tank result;
        
        @Override
        public boolean visit(Entity obj) 
        {
            if ( obj != entity && obj.hasType( EntityType.TANK ) && obj.getRootOwner() != entity.getRootOwner() ) {
                result = (Tank) obj;
                return false;
            }
            return true;
        }

        @Override
        public Tank getResult() {
            return result;
        }
    }
    
    protected final Random rnd = new Random(System.currentTimeMillis());
    
    protected final class Wanderer implements IBehaviour 
    {
        private IBehaviour wrapped;
        
        public Wanderer() 
        {
            wrapped = createBehaviour();
        }
        
        private IBehaviour createBehaviour() 
        {
            // pick random direction
            final Vector2 v = new Vector2();
            
            final float MAX_X=100;
            final float MAX_Y=100;
            v.set( -MAX_X+rnd.nextFloat()*2*MAX_X , -MAX_Y+rnd.nextFloat()*2*MAX_Y);
            System.out.println( entity+" is wandering to "+v);
            return new MoveTo( entity , v );            
        }
        
        @Override
        public String toString() 
        {
            return "Wandering to "+wrapped;
        }
        
        @Override
        public Result tick(float deltaSeconds)
        {
            final Result result = wrapped.tick( deltaSeconds );
            if ( result == Result.SUCCESS )
            {
                wrapped = createBehaviour();
                return Result.PENDING;
            }
            return result;
        }
    }
    
    @Override
    public String toString() 
    {
        if ( currentTarget != null ) {
            return "SeekAndDestroy[ target: "+currentTarget.id+", "+wrapper+"]";
        }
        return "SeekAndDestroy[ target: NONE, "+wrapper+"]";
    }
    
    @Override
    public Result tick(float deltaSeconds) 
    {
        if ( wrapper != null ) 
        {
            final Result result = wrapper.tick( deltaSeconds );
            if ( result != Result.PENDING ) 
            {
                if ( result == Result.FAILURE ) {
                    System.err.println( wrapper+" failed.");
                }
                wrapper = null;
                currentTarget = null;
            }
        }
        
        if ( wrapper == null || currentTarget == null || currentTarget.isDead() ) 
        {
            currentTarget = world.visitNeighboursWithResult( entity.position , SEEK_RANGE , new Visitor() );
            if ( currentTarget != null ) 
            {
                System.out.println( entity+" is hunting "+currentTarget);
                if ( wrapper != null ) {
                    wrapper.onCancel();
                }
                this.wrapper = new ShootAt( world , entity , currentTarget.position );
            } 
            else 
            {
                this.wrapper = new Wanderer(); 
            }
        } 
        return Result.PENDING;
    }
    
    @Override
    public void onCancel() 
    {
        if ( wrapper != null ) {
            wrapper.onCancel();
        }
    }

}
