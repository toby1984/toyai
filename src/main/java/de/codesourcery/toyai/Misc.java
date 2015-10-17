package de.codesourcery.toyai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Misc 
{
    public static final Vector3 Z_AXIS3 = new Vector3( 0 , 0 , -1);
    public static final Vector2 Y_AXIS2 = new Vector2(0,1);
    
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
        }
        return (float) result;
    }
}
