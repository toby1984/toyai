package de.codesourcery.toyai;

import java.util.ArrayList;
import java.util.List;

public final class TickContainer implements ITickListener
{
    private final List<ITickListener> listeners = new ArrayList<>();
    
    private final List<ITickListener> toAdd = new ArrayList<>();
    private final List<ITickListener> toRemove = new ArrayList<>();
    
    public void add(ITickListener e) 
    {
        toAdd.add( e );
    }
    
    public void add(ITickListener e1,ITickListener... other) 
    {
        toAdd.add( e1 );
        if ( other != null ) 
        {
            for ( ITickListener e : other ) {
                toAdd.add( e );
            }
        }
    }
    
    public void remove(ITickListener l) {
        this.toRemove.add( l );
    }

    @Override
    public boolean tick(float deltaSeconds) 
    {
        if ( ! toRemove.isEmpty() ) 
        {
            listeners.removeAll( toRemove );
            for ( int i = 0 ; i < toRemove.size() ; i++ ) 
            {
                toRemove.get(i).onTickListenerRemove();
            }
            toRemove.clear();
        }
        if ( ! toAdd.isEmpty() ) {
            listeners.addAll( toAdd );
            toAdd.clear();
        }
        for ( int i = 0,len = listeners.size() ; i < len ; i++ ) 
        {
            final ITickListener listener = listeners.get(i);
            if ( ! listener.tick( deltaSeconds ) ) 
            {
                toRemove.add( listener );
            }
        }
        return true;
    }    
}
