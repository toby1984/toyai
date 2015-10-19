package de.codesourcery.toyai.behaviours;

import de.codesourcery.toyai.IBlackboard;

public abstract class SelectTargetBehaviour extends AbstractBehaviour {

    public final String targetParamName;

    public SelectTargetBehaviour(String targetParamName) {
        this.targetParamName = targetParamName;
    }

    protected final Object getCurrentTarget(IBlackboard bb)
    {
    	return bb.get( targetParamName );
    }

    protected abstract Object getTarget(IBlackboard bb);

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        Object target = getTarget(blackboard);
        if ( target == null ) {
            return Result.FAILURE;
        }
        blackboard.put( targetParamName , target );
        return Result.SUCCESS;
    }

    @Override
    public String toString() {
    	return "SelectTarget '"+targetParamName+"'";
    }
}