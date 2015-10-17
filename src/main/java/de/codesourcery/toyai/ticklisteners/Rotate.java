package de.codesourcery.toyai.ticklisteners;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.ITickListener;
import de.codesourcery.toyai.Misc;

public class Rotate implements ITickListener {

    private static final double EPSILON_ANGLE = (2*Math.PI)/360; // 1°
    
    private final Entity entity;
    private float desiredAngleRad;
    private final Vector3 tmp = new Vector3();
    public boolean orientationReached = false;
    
    public Rotate(Entity entity,Vector2 desiredHeading) 
    {
        this.entity = entity;
        setDesiredHeading( desiredHeading );
    }
    
    public void setDesiredHeading(Vector2 desiredHeading) 
    {
        this.desiredAngleRad = Misc.angleY( desiredHeading  );
        orientationReached = false;
    }
    
    public static boolean isOrientedTowards(Entity entity , Vector2 desiredHeading) 
    {
        final float currentAngleRad = Misc.angleY( entity.orientation );
        final float desiredAngleRad = Misc.angleY( desiredHeading  );
        float deltaAngleRad = desiredAngleRad - currentAngleRad;
        return Math.abs( deltaAngleRad ) <= EPSILON_ANGLE; 
    }
    
    @Override
    public boolean tick(float deltaSeconds) 
    {
        float currentAngleRad = Misc.angleY( entity.orientation );
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
            
            tmp.set( entity.orientation.x , entity.orientation.y , 0 );
            if ( d1 < d2 ) {
                tmp.rotateRad( Misc.Z_AXIS3 , Entity.ROT_RAD_PER_SECOND*deltaSeconds );
            } else {
                tmp.rotateRad( Misc.Z_AXIS3 , -Entity.ROT_RAD_PER_SECOND*deltaSeconds );
            }
            tmp.nor();
            entity.orientation.set( tmp.x , tmp.y );
            return true;
        }
        System.out.println("Orientation reached.");
        orientationReached = true;
        return false;
    }    
}