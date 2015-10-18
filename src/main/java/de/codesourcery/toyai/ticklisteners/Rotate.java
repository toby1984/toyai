package de.codesourcery.toyai.ticklisteners;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.Misc;

public class Rotate implements IBehaviour {

    private static final double EPSILON_ANGLE = ((2*Math.PI)/360); 
    
    private final Entity entity;
    private float desiredAngleRad;
    private final Vector3 tmp = new Vector3();
    
    private float elapsedTime = 0;
    
    public Rotate(Entity entity,Vector2 desiredHeading) 
    {
        this.entity = entity;
        setDesiredHeading( desiredHeading );
    }
    
    public void setDesiredHeading(Vector2 desiredHeading) 
    {
        this.desiredAngleRad = Misc.angleY( desiredHeading  );
    }
    
    @Override
    public String toString() {
        return "Rotate to "+Math.toDegrees( desiredAngleRad )+" degrees";
    }
    
    public static boolean isOrientedTowards(Entity entity , Vector2 desiredHeading) 
    {
        final float currentAngleRad = Misc.angleY( entity.getOrientation() );
        final float desiredAngleRad = Misc.angleY( desiredHeading  );
        float deltaAngleRad = desiredAngleRad - currentAngleRad;
        return Math.abs( deltaAngleRad ) <= EPSILON_ANGLE; 
    }
    
    @Override
    public Result tick(float deltaSeconds) 
    {
        final Vector2 orientation = entity.getOrientation();
        float currentAngleRad = Misc.angleY( orientation );
        float deltaAngleRad = desiredAngleRad - currentAngleRad;
        
        if ( Math.abs( deltaAngleRad ) > EPSILON_ANGLE ) // delta > 1° 
        {
            elapsedTime += deltaSeconds;
            
            if ( elapsedTime > 4 ) 
            {
                System.err.println("Rotating failed after "+elapsedTime+" seconds");
                return Result.FAILURE;
            }
            
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
            
            final float angleStep = deltaAngleRad > Math.toRadians( 1 ) ? Entity.ROT_RAD_PER_SECOND : (float) Math.toRadians(30);
            tmp.set( orientation.x , orientation.y , 0 );
            if ( d1 < d2 ) {
                tmp.rotateRad( Misc.Z_AXIS3 , angleStep*deltaSeconds );
            } else {
                tmp.rotateRad( Misc.Z_AXIS3 , -angleStep*deltaSeconds );
            }
            entity.setOrientation( tmp.x , tmp.y );
            System.out.println("Rotating "+entity.id+" to "+entity.getOrientation());
            return Result.PENDING;
        }
        System.out.println("Orientation reached.");
        return Result.SUCCESS;
    }    
}