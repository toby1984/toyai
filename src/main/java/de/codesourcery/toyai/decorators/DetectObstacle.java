package de.codesourcery.toyai.decorators;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.World.ObstacleTestResult;
import de.codesourcery.toyai.behaviours.AbstractBehaviour;

public class DetectObstacle extends AbstractBehaviour 
{
    private final String obstacleBBParam;
    private final Entity entity;
    private ObstacleTestResult testResult = new ObstacleTestResult();
    
    private final IBehaviour delegate;
    
    public DetectObstacle(Entity entity,String obstacleParam,IBehaviour delegate) 
    {
        this.entity = entity;
        this.obstacleBBParam = obstacleParam;
        this.delegate = delegate;
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard) 
    {
        blackboard.getWorld().checkForObstacle( entity , testResult );
        blackboard.put( obstacleBBParam , testResult );
        return delegate.tick(deltaSeconds, blackboard);
    }
    
    @Override
    protected void discardHook(IBlackboard bb) {
        delegate.discard(bb);
    }
}