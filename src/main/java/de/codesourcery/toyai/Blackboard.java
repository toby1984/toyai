package de.codesourcery.toyai;

import java.util.Collection;
import java.util.HashMap;

public final class Blackboard extends HashMap<String,Object> implements IBlackboard
{
    private final World world;

    public Blackboard(World world) {
        this.world = world;
    }

    @Override
    public Object get(String key) {
        return super.get( key );
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void removeAllKeys(Collection<String> keys)
    {
        for ( String key : keys ) {
            remove( key );
        }
    }

	@Override
	public float getFloat(String key)
	{
		return ((Number) get(key)).floatValue();
	}

    @Override
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if ( value == null ) {
            return (T) value;
        }
        try {
            return clazz.cast( value );
        } catch(ClassCastException ex) {
            throw new RuntimeException("Blackboard parameter '"+key+"' is not a "+clazz.getSimpleName()+" but "+value);
        }
    }
}