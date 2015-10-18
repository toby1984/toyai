package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.Misc;

public class Rotate extends AbstractBehaviour {

    private static final double EPSILON_ANGLE = ((2*Math.PI)/360)*0.5; 
    
    private final Vector3 tmp = new Vector3();
    
    private final Entity entity;
    
    private final String orientationVectorBBParam;
    
    public Rotate(Entity entity,String orientationVectorBBParam) 
    {
        this.entity = entity;
        this.orientationVectorBBParam = orientationVectorBBParam;
        System.out.println("new rotate");
    }
    
    @Override
    public String toString() {
        return "Rotate to ";
    }
    
    private Vector2 getDesiredHeading(IBlackboard bb) {
        return bb.getVector( orientationVectorBBParam );
    }
    
    private float getDesiredAngleRad(IBlackboard bb) {
        return Misc.angleY( getDesiredHeading(bb)  );
    }
    
    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard) 
    {
        final Vector2 currentOrientation = entity.getOrientation();
        float currentAngleRad = Misc.angleY( currentOrientation );
        float desiredAngleRad = getDesiredAngleRad(blackboard);
        
        float deltaAngleRad = desiredAngleRad - currentAngleRad;
        
        if ( Math.abs( deltaAngleRad ) > EPSILON_ANGLE ) // delta > 1° 
        {
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
            final float coveredAngle;
            if ( Math.abs( deltaAngleRad ) > 5*Misc.TO_RAD) 
            {
                final float radPerSecond = Entity.ROT_RAD_PER_SECOND;;
                coveredAngle = radPerSecond*deltaSeconds;
//                System.out.println("FAST Rotating "+entity.id+" , time: "+(deltaSeconds*1000)+" ms , from "+currentAngleRad*Misc.TO_DEG+" deg to "+desiredAngleRad*Misc.TO_DEG+" by "+Misc.TO_DEG*coveredAngle+" degrees");
            } else {
                final float radPerSecond = 30*Misc.TO_RAD;
                coveredAngle = radPerSecond*deltaSeconds;
//                System.out.println("SLOW Rotating "+entity.id+", time: "+(deltaSeconds*1000)+" ms ,  from "+currentAngleRad*Misc.TO_DEG+" deg to "+desiredAngleRad*Misc.TO_DEG+" by "+Misc.TO_DEG*coveredAngle+" degrees");
            }
            
            tmp.set( currentOrientation.x , currentOrientation.y , 0 );
            if ( d1 < d2 ) {
                tmp.rotateRad( Misc.Z_AXIS3 , coveredAngle );
            } else {
                tmp.rotateRad( Misc.Z_AXIS3 , -coveredAngle );
            }
            entity.setOrientation( tmp.x , tmp.y );

            return Result.PENDING;
        }
        return Result.SUCCESS;
    }    
}