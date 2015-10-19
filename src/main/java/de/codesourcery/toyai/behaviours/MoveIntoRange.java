package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.entities.MoveableEntity;

public final class MoveIntoRange extends AbstractBehaviour
{
    private final MoveableEntity entity;
    private final float range;
    private final String targetBBParam;

    private final String moveToTargetParam;
    private final MoveTo moveTo;

    public MoveIntoRange(MoveableEntity entity, String targetBBParam, float range,String rotParam)
    {
        this.entity = entity;
        this.range = range*0.9f;
        this.targetBBParam = targetBBParam;
        this.moveToTargetParam = registerParam( getId()+"._target" );
        this.moveTo = new MoveTo( entity , moveToTargetParam , rotParam );
    }

    private Vector3 getTarget(IBlackboard bb)
    {
        return bb.getVector3( targetBBParam );
    }

    @Override
    public String toString() {
        return "MoveIntoRange '"+targetBBParam+"'";
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        final Vector3 target = getTarget( blackboard );
        final float distance = entity.dst( target );
        if ( distance > range)
        {
            Vector3 tmp = blackboard.getVector3( moveToTargetParam );
            if ( tmp == null ) {
                tmp = new Vector3();
                blackboard.put( moveToTargetParam , tmp );
            }
            tmp.set( entity.position );
            tmp.sub( target ).nor();
            tmp.scl( range ); // fuzzyness of 0.9f because MoveTo isn't 100% accurate
            tmp.add( target );
            blackboard.put( moveToTargetParam , tmp );
            return moveTo.tick( deltaSeconds, blackboard );
        }
        entity.stopMoving();
        return Result.SUCCESS;
    }

    @Override
    protected void discardHook(IBlackboard blackboard) {
       moveTo.discard(blackboard);
    }
}