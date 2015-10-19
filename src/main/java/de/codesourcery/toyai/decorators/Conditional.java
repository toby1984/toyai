package de.codesourcery.toyai.decorators;

import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.behaviours.AbstractBehaviour;

public class Conditional extends AbstractBehaviour 
{
    private final IBehaviour condition;
    private final IBehaviour ifTrue;
    private final IBehaviour ifFalse;
    
    public Conditional(IBehaviour condition, IBehaviour ifTrue,IBehaviour ifFalse) 
    {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }
    
    @Override
    protected void discardHook(IBlackboard bb) 
    {
        condition.discard( bb );
        ifTrue.discard( bb );
        ifFalse.discard( bb );
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard) 
    {
        final Result r = condition.tick(deltaSeconds, blackboard);
        switch( r ) 
        {
            case FAILURE:
                return ifFalse.tick(deltaSeconds, blackboard);
            case PENDING:
                return Result.PENDING;
            case SUCCESS:
                return ifTrue.tick(deltaSeconds, blackboard);
            default:
                throw new RuntimeException("Unhandled switch/case: "+r);
        }
    }

}
