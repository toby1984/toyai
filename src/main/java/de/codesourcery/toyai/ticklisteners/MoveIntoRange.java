package de.codesourcery.toyai.ticklisteners;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.MoveableEntity;

public class MoveIntoRange implements IBehaviour 
{
    private final MoveableEntity entity;
    private final float range;
    private final Vector2 target; 
    
    private MoveTo moveTo;
    
    public MoveIntoRange(MoveableEntity entity, Vector2 target, float range) 
    {
        this.entity = entity;
        this.range = range;
        this.target = new Vector2(target);
    }
    
    @Override
    public String toString() {
        return "MoveIntoRange "+target;
    }
    
    @Override
    public Result tick(float deltaSeconds) 
    {
        if ( entity.dst( target ) > range) 
        {
            if ( moveTo == null ) 
            {
                Vector2 tmp = new Vector2( entity.position );
                tmp.sub( target );
                tmp.nor();
                tmp.scl( range*0.9f ); // fuzzyness of 0.9f because MoveTo isn't 100% accurate
                tmp.add( target );
                moveTo = new MoveTo( entity , tmp );
            }
            return moveTo.tick( deltaSeconds );
        }
        return Result.SUCCESS;
    }
    
    @Override
    public void onCancel() {
        if ( moveTo != null ) 
        {
            moveTo.onCancel();
        }
    }
}