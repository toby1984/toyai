package de.codesourcery.toyai.decorators;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.behaviours.AbstractBehaviour;

public class UntilDead extends AbstractBehaviour {

	private final String targetBBParam;
	private final IBehaviour delegate;

	public UntilDead(IBehaviour delegate,String targetBBParam)
	{
		this.delegate = delegate;
		this.targetBBParam = targetBBParam;
	}

	private Entity getTarget(IBlackboard bb)
	{
		return bb.get( targetBBParam , Entity.class );
	}

	@Override
	protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
	{
		final Entity target = getTarget(blackboard);
		if ( target.isDead() )
		{
			if ( LOG.isDebugEnabled() ) {
				LOG.log( target+" has died.");
			}
			return Result.FAILURE;
		}
		return delegate.tick(deltaSeconds, blackboard);
	}

	@Override
	protected void discardHook(IBlackboard bb)
	{
		delegate.discard( bb );
	}

	@Override
	public String toString() {
		return "UntilDead '"+targetBBParam+"' "+delegate;
	}
}
