package de.codesourcery.toyai.behaviours;

import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.decorators.UntilDead;
import de.codesourcery.toyai.entities.Tank;

public final class ShootAt extends AbstractBehaviour
{
    private final Tank entity;
    private final IBehaviour wrapper;

    private final String targetBlackboardParam;

    public ShootAt(Tank e,String targetBBParam)
    {
        this.entity = e;
        this.targetBlackboardParam = targetBBParam;
        final String rotParam = getId()+".rot";

        final IBehaviour moveIntoRange = new MoveIntoRange( entity , targetBBParam , entity.getEngagementDistance() , rotParam );
        final IBehaviour aimAt = new AlignWith( entity , targetBBParam , rotParam );
        final IBehaviour fireTurret = new FireTurretBehaviour( entity );

        wrapper = moveIntoRange.andThen( aimAt ).andThen( new UntilDead( fireTurret , targetBBParam ) );
    }

    @Override
    protected void discardHook(IBlackboard blackboard) {
        wrapper.discard(blackboard);
    }

    @Override
    public String toString() {
        return "Shoot @ '"+targetBlackboardParam+"' [ "+wrapper+" ]";
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        return wrapper.tick( deltaSeconds, blackboard );
    }
}