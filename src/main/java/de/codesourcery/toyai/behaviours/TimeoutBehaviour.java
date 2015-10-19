package de.codesourcery.toyai.behaviours;

import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;

public final class TimeoutBehaviour extends AbstractBehaviour {

    private IBehaviour delegate;
    private float timeRemaining;

    public TimeoutBehaviour(IBehaviour delegate,float timeout) {
        this.delegate = delegate;
        this.timeRemaining = timeout;
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        if ( timeRemaining <= 0 ) {
            return Result.FAILURE;
        }
        final Result result = delegate.tick(deltaSeconds, blackboard);
        if ( result == Result.PENDING )
        {
            timeRemaining -= deltaSeconds;
        }
        return result;
    }

	@Override
	public String toString() {
		return "Timeout";
	}
}
