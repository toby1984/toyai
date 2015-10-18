package de.codesourcery.toyai.behaviours;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.Misc;
import de.codesourcery.toyai.entities.MoveableEntity;

public class Wander extends AbstractBehaviour 
{
    protected final Random rnd = new Random(System.currentTimeMillis());

    private final MoveableEntity entity;

    private float timeRemaining;
    private final String rotBBParam;
    private final IBehaviour wrapper;
    private final float timeUntilDirectionChange;

    public Wander(MoveableEntity entity,float timeUntilDirectionChange) 
    {
        this.entity = entity;
        this.rotBBParam = registerParam( getId()+".rot" );
        this.timeUntilDirectionChange = timeUntilDirectionChange;
        this.wrapper = new Rotate( entity , rotBBParam );
    }

    private void setRandomOrientation(IBlackboard bb)
    {
        // pick random direction
        float currentAngleRad = Misc.angleY( entity.getOrientation() );
        float newAngleRad = (float) (currentAngleRad + random() * Math.PI);
        if ( newAngleRad < 0 ) 
        {
            newAngleRad += 2*Math.PI;
        } else if ( newAngleRad > 2*Math.PI ) {
            newAngleRad -= 2*Math.PI;
        }
        
        Vector2 rot = bb.getVector( rotBBParam );
        if ( rot == null ) {
            rot = new Vector2();
            bb.put( rotBBParam, rot );
        }         

        rot.set( Misc.Y_AXIS2 );
        rot.rotate( newAngleRad );
        
        System.out.println("Wander: "+currentAngleRad*Misc.TO_DEG+" => "+newAngleRad*Misc.TO_DEG);
        bb.put( rotBBParam , rot );
        
        timeRemaining = timeUntilDirectionChange;
    }

    private float random() 
    {
        return rnd.nextFloat() - rnd.nextFloat();
    }

    @Override
    public String toString() 
    {
        return "Wander";
    }
    
    @Override
    protected void onDiscardHook(IBlackboard bb) 
    {
        entity.stopMoving();
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        entity.acceleration = Entity.MAX_ACCELERATION;
        
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