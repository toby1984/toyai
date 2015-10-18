package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;

public class AimAt extends AbstractBehaviour 
{
    private final Entity entity;
    private final String targetToTrackBBParam;
    private final String rotationAngleBBParam;
    
    private final Rotate rotate;
    
    public AimAt(Entity entity,String targetToTrackBBParam) 
    {
        this.targetToTrackBBParam = targetToTrackBBParam;
        this.rotationAngleBBParam = registerParam( getId()+".rot" );
        
        this.entity = entity;
        rotate = new Rotate( entity , rotationAngleBBParam );
    }
    
    private Vector2 getDestination(IBlackboard bb) 
    {
        return bb.getVector( targetToTrackBBParam );
    }
    
    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard) 
    {
        final Vector2 destination = getDestination( blackboard );
        Vector2 tmp = blackboard.getVector( rotationAngleBBParam );
        if ( tmp == null ) {
            tmp = new Vector2( destination );
        } else {
            tmp.set( destination );
        }
        tmp.sub( entity.position );
        tmp.nor();
        blackboard.put( rotationAngleBBParam , tmp );       
        return rotate.tick(deltaSeconds, blackboard);
    }
    
    @Override
    protected void onDiscardHook(IBlackboard bb) 
    {
        rotate.discard( bb );
    }
}