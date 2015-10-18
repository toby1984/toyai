package de.codesourcery.toyai.behaviours;

import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.entities.Tank;

public class FireTurretBehaviour extends AbstractBehaviour 
{
    private final Tank entity;
    
    public FireTurretBehaviour(Tank entity) {
        this.entity = entity;
    }
    
    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard) 
    {
        if ( entity.isReadyToShoot() ) 
        {
            System.out.println("Firing shot");
            entity.fireShot();
            return Result.SUCCESS;
        }
        return Result.PENDING;
    }
}
