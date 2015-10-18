package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.entities.MoveableEntity;

public class MoveIntoRange extends AbstractBehaviour
{
    private final MoveableEntity entity;
    private final float range;
    private final String targetBBParam; 
    
    private final String moveToTargetParam; 
    private final MoveTo moveTo;
    
    public MoveIntoRange(MoveableEntity entity, String targetBBParam, float range) 
    {
        this.entity = entity;
        this.range = range;
        this.targetBBParam = targetBBParam;
        this.moveToTargetParam = registerParam( getId()+"._target" );
        this.moveTo = new MoveTo( entity , moveToTargetParam);
    }
    
    private Vector2 getTarget(IBlackboard bb) 
    {
        return bb.getVector( targetBBParam );
    }
    
    @Override
    public String toString() {
        return "MoveIntoRange '"+targetBBParam+"'";
    }
    
    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard) 
    {
        final Vector2 target = getTarget( blackboard );
        final float distance = entity.dst( target );
        if ( distance > range) 
        {
            Vector2 tmp = blackboard.getVector( moveToTargetParam );
            if ( tmp == null ) {
                tmp = new Vector2();
                blackboard.put( moveToTargetParam , tmp ); 
            }
            tmp.set( entity.position );
            tmp.sub( target ).nor();
            tmp.scl( range*0.9f ); // fuzzyness of 0.9f because MoveTo isn't 100% accurate
            tmp.add( target );
            blackboard.put( moveToTargetParam , tmp );
            System.out.println("Out of rang: "+distance);
            return moveTo.tick( deltaSeconds, blackboard );
        }
        System.out.println("Moved into range "+distance);
        entity.stopMoving();
        return Result.SUCCESS;
    }
    
    @Override
    protected void onDiscardHook(IBlackboard blackboard) {
       moveTo.discard(blackboard);
    }
}