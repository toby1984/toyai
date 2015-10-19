package de.codesourcery.toyai.behaviours;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.Entity.EntityType;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.World.ICallbackWithResult;
import de.codesourcery.toyai.decorators.UntilDead;
import de.codesourcery.toyai.entities.Tank;

public final class SeekAndDestroy extends AbstractBehaviour
{
    private final Tank entity;

    private IBehaviour wrapper;
    private final String targetParamName;

    public SeekAndDestroy(Tank entity)
    {
        this.entity = entity;
        targetParamName = registerParam( getId()+".target" );
        wrapper = forever ( this::createBehaviour );
    }

    private IBehaviour createBehaviour()
    {
        final IBehaviour selectTarget = new SelectTargetBehaviour(targetParamName)
        {
            @Override
            protected Object getTarget(IBlackboard bb)
            {
            	Entity currentTarget = (Entity) getCurrentTarget( bb );
                if ( currentTarget == null || currentTarget.isDead() )
                {
                    currentTarget = bb.getWorld().visitNeighboursWithResult( entity.position , Tank.SEEK_RANGE , new Visitor() );
                }
                return currentTarget;
            }
        };

        final IBehaviour huntingBehaviour = selectTarget.andThen( new UntilDead( new ShootAt( entity , targetParamName ) , targetParamName ) );

        final IBehaviour wanderingBehaviour = new Wander(entity, 2f , 5.0f ).doUntil( selectTarget );
        return huntingBehaviour.or( wanderingBehaviour );
    }

    protected final class Visitor implements ICallbackWithResult<Tank>
    {
        private Tank result;

        @Override
        public boolean visit(Entity obj)
        {
            if ( obj != entity && obj.hasType( EntityType.TANK ) && obj.getRootOwner() != entity.getRootOwner() ) {
                result = (Tank) obj;
                return false;
            }
            return true;
        }

        @Override
        public Tank getResult() {
            return result;
        }
    }

    @Override
    public String toString()
    {
        return "SeekAndDestroy[ "+wrapper+"]";
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        return wrapper.tick(deltaSeconds, blackboard);
    }

    @Override
    public void discardHook(IBlackboard blackboard)
    {
        wrapper.discard(blackboard);
    }
}