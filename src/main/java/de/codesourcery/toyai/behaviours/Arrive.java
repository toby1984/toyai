package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.entities.MoveableEntity;

public final class Arrive extends AbstractBehaviour
{
	private final MoveableEntity entity;
	private final String velocityBBParam;
	private final String destinationBBParam;

	private final IBehaviour wrapper;

	public Arrive(MoveableEntity entity,String destinationBBParam,String rotBBParam,String velocityBBParam)
	{
		this.entity = entity;
		this.destinationBBParam = destinationBBParam;
		this.velocityBBParam = velocityBBParam;

		this.wrapper = new VelocityAdjuster( entity , velocityBBParam , new AlignWith( entity , destinationBBParam , rotBBParam ) );
	}

	@Override
	public String toString()
	{
		return "Arrive";
	}

	@Override
	public void discardHook(IBlackboard blackboard)
	{
		wrapper.discard( blackboard );
		entity.stopMoving();
	}

	private Vector3 getDestination(IBlackboard bb) {
		return bb.getVector3( destinationBBParam );
	}

	@Override
	protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
	{
		final float dst = entity.dst( getDestination(blackboard) );

		/*
		 *
		 *        V(final)^2 - V(initial)u^2
		 *  a =   --------------------------
		 *            2s
		 *
		 *
		 * v is the final velocity,
		 * u is the initial velocity,
		 * t is the time taken,
		 * s is the distance covered.
		 */
		if ( dst > 4 )
		{
			final float arrivalTime = 1f;
			float velocity = dst/arrivalTime;
			if ( velocity < 0.1 )
			{
				velocity = 0;
				blackboard.put( velocityBBParam , velocity );
				entity.stopMoving();
				entity.rotationInRadPerSecond = 0;
				return Result.SUCCESS;
			}
			if ( velocity > entity.maxVelocity )
			{
				velocity = entity.maxVelocity;
			}
//			LOG.log("Setting speed: "+velocity);
			blackboard.put( velocityBBParam , velocity );
			wrapper.tick(deltaSeconds, blackboard);
			return Result.PENDING;
		}
		entity.stopMoving();
		
		blackboard.put( velocityBBParam , 0f );
		final Result result = wrapper.tick(deltaSeconds, blackboard);
		if ( result == Result.SUCCESS ) {
			return Result.SUCCESS;
		}
		return result;
	}
}