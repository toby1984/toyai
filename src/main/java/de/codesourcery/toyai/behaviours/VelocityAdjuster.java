package de.codesourcery.toyai.behaviours;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.entities.MoveableEntity;

public class VelocityAdjuster extends AbstractBehaviour
{
	protected static final float EPSILON = 0.1f;

	protected static final float TIME_TO_TARGET_VELOCITY = 0.25f;

	private final MoveableEntity entity;
	private final String velocityBBParam;
	private final IBehaviour delegate;

	public VelocityAdjuster(MoveableEntity entity,String velocityBBParam,IBehaviour delegate) {
		this.entity = entity;
		this.velocityBBParam = velocityBBParam;
		this.delegate = delegate;
	}

	@Override
	protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
	{
		final float currentV = entity.velocity.len();
		float desiredVelocity = blackboard.getFloat( velocityBBParam );
		if ( desiredVelocity > entity.maxVelocity ) {
			desiredVelocity = entity.maxVelocity;
		} else if ( desiredVelocity < 0 ) {
			throw new RuntimeException("Velocity must not be negative");
		}

		if ( Math.abs( currentV - desiredVelocity ) > EPSILON )
		{
			final float deltaV = desiredVelocity - currentV;
			/*
			 *  a = dv/dt
			 */
			float acceleration= deltaV / TIME_TO_TARGET_VELOCITY;
			if ( acceleration > Entity.MAX_ACCELERATION )
			{
				acceleration = Entity.MAX_ACCELERATION;
			}
			else if ( acceleration < -Entity.MAX_ACCELERATION )
			{
				acceleration = -Entity.MAX_ACCELERATION;
			}
			LOG.log("Target speed "+desiredVelocity+" NOT reached, current speed: "+currentV+" => acceleration: "+acceleration);
			entity.setAcceleration( acceleration );
			return delegate.tick(deltaSeconds, blackboard);
		}
		LOG.log("Target speed reached: "+currentV);
		entity.setAcceleration(0);
		return delegate.tick(deltaSeconds, blackboard);
	}

	@Override
	protected void discardHook(IBlackboard bb)
	{
		delegate.discard(bb);
	}
}