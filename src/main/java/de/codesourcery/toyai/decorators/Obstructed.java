package de.codesourcery.toyai.decorators;

import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.World.ObstacleTestResult;
import de.codesourcery.toyai.behaviours.AbstractBehaviour;

public final class Obstructed extends AbstractBehaviour 
{
    private final String obstacleTestResultBBParam;
    
    public Obstructed(String obstacleTestResultBBParam) 
    {
        this.obstacleTestResultBBParam = obstacleTestResultBBParam;
    }
    
    private ObstacleTestResult getTestResult(IBlackboard bb) {
        return (ObstacleTestResult) bb.get( obstacleTestResultBBParam );
    }    
    
    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard) 
    {
        if (getTestResult(blackboard).hasObstacle()) 
        {
            return Result.SUCCESS;
        }
        return Result.FAILURE;
    }
    
    @Override
    public String toString() {
        return "IsObstructed '"+obstacleTestResultBBParam+"'";
    }
}