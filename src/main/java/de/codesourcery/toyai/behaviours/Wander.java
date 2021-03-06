package de.codesourcery.toyai.behaviours;

import java.util.Random;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.Misc;
import de.codesourcery.toyai.decorators.DetectObstacle;
import de.codesourcery.toyai.entities.MoveableEntity;

public final class Wander extends AbstractBehaviour
{
    protected final Random rnd = new Random(0xdeadbeef);

    private final MoveableEntity entity;

    private float timeRemaining;
    private final String rotBBParam;
    private final IBehaviour wrapper;

    private final float minTimeUntilDirectionChange;
    private final float maxTimeUntilDirectionChange;

    public Wander(MoveableEntity entity,float minTimeUntilDirectionChange,float maxTimeUntilDirectionChange)
    {
        if ( minTimeUntilDirectionChange > maxTimeUntilDirectionChange ) {
            throw new IllegalArgumentException();
        }

        this.entity = entity;
        this.rotBBParam = registerParam( getId()+".rot" );
        final String obstacleParam = registerParam( getId()+".obstacle" );

        this.minTimeUntilDirectionChange = minTimeUntilDirectionChange;
        this.maxTimeUntilDirectionChange = maxTimeUntilDirectionChange;

        this.wrapper = new DetectObstacle(entity, obstacleParam, new AvoidObstacle( entity , rotBBParam , obstacleParam ) );
    }

    private float randomTime() {

        return minTimeUntilDirectionChange+rnd.nextFloat()*(maxTimeUntilDirectionChange-minTimeUntilDirectionChange);
    }

    private void setRandomOrientation(IBlackboard bb)
    {
        // pick random direction
//        float currentAngleRad = Misc.angleY( entity.getOrientation() );
        float newAngleRad = (float) ( rnd.nextFloat() * 2*Math.PI );

        if ( newAngleRad < 0 )
        {
            newAngleRad += 2*Math.PI;
        } else if ( newAngleRad > 2*Math.PI ) {
            newAngleRad -= 2*Math.PI;
        }

        Vector3 rot = bb.getVector3( rotBBParam );
        if ( rot == null ) {
            rot = new Vector3();
            bb.put( rotBBParam, rot );
        }
        Misc.setToRotatedUnitVector( rot , newAngleRad );
        bb.put( rotBBParam , rot );

        timeRemaining = randomTime();
    }

    @Override
    public String toString()
    {
        return "Wander";
    }

    @Override
    protected void discardHook(IBlackboard bb)
    {
    	wrapper.discard( bb );
        entity.stopMoving();
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        entity.setAcceleration( Entity.MAX_ACCELERATION );

        if ( timeRemaining <= 0 ) {
            setRandomOrientation( blackboard );
        }
        final Result result = wrapper.tick(deltaSeconds, blackboard);
        switch ( result )
        {
            case FAILURE:
                return Result.FAILURE;
            case PENDING:
                // $$FALL-THROUGH$$
            case SUCCESS:
                // ok
                break;
            default:
                throw new RuntimeException("Unhandled case: "+result);
        }
        timeRemaining -= deltaSeconds;
        return Result.PENDING;
    }
}