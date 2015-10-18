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
}