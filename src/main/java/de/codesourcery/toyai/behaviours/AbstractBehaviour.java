package de.codesourcery.toyai.behaviours;

import java.util.HashSet;
import java.util.Set;

import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;

public abstract class AbstractBehaviour implements IBehaviour {

    public static long idGenerator = IBehaviour.NOP.getId()+1; // IBehaviour.NOP uses a hard-coded ID, do not clash with it

    private boolean isDiscarded;
    private final long id = newId();

    private final Set<String> paramsToDeleteOnDiscard = new HashSet<>();

    public static long newId() {
        return idGenerator++;
    }

    @Override
	public final long getId()
    {
        return id;
    }

    public final boolean isDiscarded() {
        return isDiscarded;
    }

    protected String registerParam(String key)
    {
        if ( key == null || key.length() == 0 ) {
            throw new IllegalArgumentException("Key must not be NULL/empty");
        }
        paramsToDeleteOnDiscard.add(key);
        return key;
    }

    @Override
    public final void discard(IBlackboard blackboard)
    {
        if ( ! isDiscarded )
        {
            isDiscarded = true;
            if ( ! paramsToDeleteOnDiscard.isEmpty() )
            {
                blackboard.removeAllKeys( paramsToDeleteOnDiscard );
                paramsToDeleteOnDiscard.clear();
            }
            discardHook(blackboard);
        }
    }

    @Override
	public final Result tick(float deltaSeconds, IBlackboard blackboard)
    {
        if ( isDiscarded )
        {
        	System.err.println("Discarded behaviour called: "+this);
            return Result.FAILURE;
        }
        return tickHook(deltaSeconds,blackboard);
    }

    protected abstract Result tickHook(float deltaSeconds, IBlackboard blackboard);

    protected void discardHook(IBlackboard bb) {

    }
}