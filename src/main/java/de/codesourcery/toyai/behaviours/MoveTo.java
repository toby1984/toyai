package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.decorators.Conditional;
import de.codesourcery.toyai.decorators.DetectObstacle;
import de.codesourcery.toyai.decorators.Obstructed;
import de.codesourcery.toyai.entities.MoveableEntity;

public final class MoveTo extends AbstractBehaviour {

    private final MoveableEntity entity;
    private final String rotParam;
    private final String destinationBBParam;
    private final String velocityBBParam;

    private IBehaviour wrapper;

    public boolean moveFailed;

    public MoveTo(MoveableEntity e,String destinationBBParam,String rotParam,String velocityBBParam)
    {
        this.entity = e;
        this.destinationBBParam = destinationBBParam;
        this.rotParam = rotParam;
        this.velocityBBParam = velocityBBParam;
    }

    protected Vector3 getDestination(IBlackboard bb)
    {
        return bb.getVector3( destinationBBParam );
    }

    private IBehaviour createWrapper(IBlackboard bb)
    {
        // setup stuff
        final String obsParam = registerParam( getId()+".obstacle" );

        final IBehaviour stopAtDestination = new Arrive(entity,destinationBBParam,rotParam,velocityBBParam);

        final IBehaviour condition = new Obstructed( obsParam );
        final IBehaviour avoidObstacle = new AvoidObstacle( entity , rotParam , obsParam );
        final AlignWith aimAt = new AlignWith(entity,destinationBBParam , rotParam );
        final IBehaviour moveToDestination = parallel( aimAt , stopAtDestination);
        final IBehaviour b = new Conditional( condition , avoidObstacle ,moveToDestination );

        return new DetectObstacle( entity , obsParam , b );
    }

    protected static float clamp(float actual,float min,float max)
    {
        return actual < min ? min : ( actual > max ? max : actual );
    }

    @Override
    public String toString() {
        return "MoveTo '"+destinationBBParam+"'";
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        if ( wrapper == null ) {
            wrapper = createWrapper( blackboard );
        }
        return wrapper.tick( deltaSeconds, blackboard );
    }

    @Override
    protected void discardHook(IBlackboard blackboard)
    {
        if ( wrapper != null ) {
            wrapper.discard(blackboard);
        }
    }
}