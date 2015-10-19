package de.codesourcery.toyai.behaviours;

import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.entities.Tank;

public final class FireTurretBehaviour extends AbstractBehaviour
{
    private final Tank entity;

    public FireTurretBehaviour(Tank entity) {
        this.entity = entity;
    }

	@Override
	public String toString() {
		return "FireTurret";
	}

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        if ( entity.isReadyToShoot() )
        {
            entity.fireShot();
            return Result.SUCCESS;
        }
        return Result.PENDING;
    }
}
