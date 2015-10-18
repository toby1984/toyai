package de.codesourcery.toyai.behaviours;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.Entity.EntityType;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.World.ICallbackWithResult;
import de.codesourcery.toyai.entities.Tank;

public class SeekAndDestroy extends AbstractBehaviour
{
    private final Tank entity;
    
    private final float SEEK_RANGE = Tank.TURRET_RANGE*2;
    
    private IBehaviour wrapper;
    
    public SeekAndDestroy(Tank entity) 
    {
        this.entity = entity;
        final String targetParamName = registerParam( getId()+".target" );
        
        final IBehaviour huntingBehaviour = new SelectTargetBehaviour(targetParamName) 
        {
            private Tank currentTarget;
            
            @Override
            protected Object getTarget(IBlackboard bb) 
            {
                if ( currentTarget == null || currentTarget.isDead() ) {
                    currentTarget = bb.getWorld().visitNeighboursWithResult( entity.position , SEEK_RANGE , new Visitor() );
                }
                return currentTarget; 
            }
        }.andThen( new ShootAt( entity , targetParamName ) );
        
        wrapper = huntingBehaviour.or( new Wander(entity,5.0f ) );
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
    
    @Override
    public String toString() 
    {
        return "SeekAndDestroy[ "+wrapper+"]";
    }
    
    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard) 
    {
        return wrapper.tick(deltaSeconds, blackboard);
    }
    
    @Override
    public void onDiscardHook(IBlackboard blackboard) 
    {
        if ( wrapper != null ) {
            wrapper.discard(blackboard);
        }
    }
}