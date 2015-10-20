package de.codesourcery.toyai;

import java.util.Collection;

import com.badlogic.gdx.math.Vector3;

public interface IBlackboard
{
    public Object put(String key,Object object);

    public Object get(String key);

    public float getFloat(String key);

    public default Vector3 getVector3(String key)
    {
        final Object result = get(key);
        if ( result == null ) {
            return null;
        }
        if ( result instanceof Vector3 ) {
            return (Vector3) result;
        }
        if ( result instanceof Entity)
        {
            return ((Entity) result).position;
        }
        throw new RuntimeException("Key '"+key+"' is not a vector but "+result);
    }

    public World getWorld();

    public void removeAllKeys(Collection<String> keys);
}