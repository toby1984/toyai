package de.codesourcery.toyai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Misc 
{
    public static final Vector3 Z_AXIS3 = new Vector3( 0 , 0 , -1);
    public static final Vector2 Y_AXIS2 = new Vector2(0,1);

    /**
     * Multiply value in degrees with this constant to get radians.
     */
    public static final float TO_RAD = (float) ( 1d / 180d*Math.PI);
    
    /**
     * Multiply value in radians with this constant to get degrees.
     */
    public static final float TO_DEG = (float) ( 180.0d/Math.PI );
    
    public static float angleY(Vector2 v1) 
    {
        return angle(v1,Y_AXIS2);
    }
    
    public static float angle(Vector2 v1,Vector2 v2) 
    {
        double result = ( Math.atan2(v2.y, v2.x) - Math.atan2(v1.y, v1.x) );
        if ( result < 0 ) 
        {
            result += 2*Math.PI;
        } else if ( result > 2*Math.PI) {
            result -= 2*Math.PI;
        }
        return (float) result;
    }
}
