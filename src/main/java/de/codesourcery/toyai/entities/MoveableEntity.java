package de.codesourcery.toyai.entities;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;

public abstract class MoveableEntity extends Entity
{
    public final Vector3 velocity = new Vector3();
    public float acceleration = 0;
    public final float maxVelocity;

    public MoveableEntity(EntityType type, Entity owner,IBlackboard blackboard,float maxVelocity) {
        super(type, owner,blackboard);
        this.maxVelocity = maxVelocity;
    }

    @Override
	public final boolean isMoving()
    {
        return velocity.len2() != 0 || acceleration != 0;
    }

    public final void stopMoving()
    {
         velocity.setZero();
         acceleration = 0;
    }
}