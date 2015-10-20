package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.Misc;
import de.codesourcery.toyai.entities.MoveableEntity;

public final class Rotate extends AbstractBehaviour {

    private static final double EPSILON_ANGLE = ((2*Math.PI)/360)*0.5;

    private final MoveableEntity entity;

    private final String orientationVectorBBParam;

    private final float slowAdjustRadPerSecond;

    public Rotate(MoveableEntity entity,String orientationVectorBBParam)
    {
    	this(entity,orientationVectorBBParam , 15*Misc.TO_RAD );
    }

    public Rotate(MoveableEntity entity,String orientationVectorBBParam,float slowAdjustRadPerSecond)
    {
        this.entity = entity;
        this.orientationVectorBBParam = orientationVectorBBParam;
        this.slowAdjustRadPerSecond = slowAdjustRadPerSecond;
    }

    @Override
    public String toString() {
        return "Rotate to '"+orientationVectorBBParam+"'";
    }

    private Vector3 getDesiredHeading(IBlackboard bb) {
        return bb.getVector3( orientationVectorBBParam );
    }

    private float getDesiredAngleRad(IBlackboard bb) {
        return Misc.angleY( getDesiredHeading(bb)  );
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        final Vector3 currentOrientation = entity.getOrientation();
        
        float currentAngleRad = Misc.angleY( currentOrientation );
        float desiredAngleRad = getDesiredAngleRad(blackboard);

        float deltaAngleRad = desiredAngleRad - currentAngleRad;

        if ( Math.abs( deltaAngleRad ) > EPSILON_ANGLE ) // delta > 1°
        {
//            LOG.log("Angle delta: "+deltaAngleRad*Misc.TO_DEG+" @ speed "+entity.velocity.len() );

            // determine direction to turn, always preferring
            // the smaller turn angle
            final float d1;
            final float d2;
            if ( currentAngleRad < desiredAngleRad )
            {
                d1 = desiredAngleRad - currentAngleRad;
                d2 = (float) (2*Math.PI - d1);
            } else {
                d2 = currentAngleRad-desiredAngleRad;
                d1 = (float) (2*Math.PI - d2);
            }

            // slow down if we're pretty close to the destination angle
            final float radPerSecond;
            if ( Math.abs( deltaAngleRad ) > 5*Misc.TO_RAD)
            {
                radPerSecond = Entity.ROT_RAD_PER_SECOND;;
            } else {
                radPerSecond = slowAdjustRadPerSecond;
            }
            
            entity.setAcceleration( Entity.MAX_ACCELERATION/4f );
            
            if ( d1 < d2 ) {
                entity.rotationInRadPerSecond = radPerSecond;
            } else {
                entity.rotationInRadPerSecond = -radPerSecond;
            }
            return Result.PENDING;
        }
        entity.rotationInRadPerSecond  = 0;
//        LOG.log("aligned to "+currentAngleRad*Misc.TO_DEG+"°");
        return Result.SUCCESS;
    }
}