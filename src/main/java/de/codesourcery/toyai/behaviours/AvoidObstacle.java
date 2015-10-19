package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.Misc;
import de.codesourcery.toyai.World.ObstacleTestResult;

public class AvoidObstacle extends AbstractBehaviour
{
	private final Entity entity;
	private final ObstacleTestResult testResult = new ObstacleTestResult();
	private final String rotParam;

	private final Rotate rotate;

	public AvoidObstacle(Entity entity)
	{
		this.entity = entity;
		this.rotParam = registerParam( getId()+".rot" );
		this.rotate = new Rotate( entity , rotParam );
	}

	public AvoidObstacle(Entity entity,String rotParam)
	{
		this.entity = entity;
		this.rotParam = rotParam;
		this.rotate = new Rotate( entity , rotParam );
	}

	@Override
	protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
	{
		blackboard.getWorld().checkForObstacle( entity , testResult );

    	/*
    	 * 0b111 =
    	 * 0b1.. = left
    	 * 0b.1. = center
    	 * 0b..1 = right
    	 */
		final float rotInRad = 5*Misc.TO_RAD;
		float angleDeviation = 0;
		switch ( testResult.bitMask )
		{
			case 0b000:
				break;
			case 0b001: // right whisker => turn left (counter-clockwise)
				angleDeviation = -rotInRad;
				LOG.log("Obstacle right");
				break;
			case 0b010: // center whisker => turn left (counter-clockwise)
				angleDeviation = -rotInRad;
				LOG.log("Obstacle center");
				break;
			case 0b011: // right + center whisker => turn left (counter-clockwise)
				angleDeviation = -rotInRad;
				LOG.log("Obstacle right + center");
				break;
			case 0b100: // left whisker => turn right (clockwise)
				angleDeviation = rotInRad;
				break;
			case 0b101: // left + right whisker ... the question is: do we fit into the hole ??
				LOG.log("Obstacle left + right");
				break; // do nothing, we'll assume we fit into the hole...
			case 0b110: // left + center whisker => turn right
				LOG.log("Obstacle left + center");
				angleDeviation = rotInRad;
				break;
			case 0b111:
				LOG.log("Obstacle left + center + right");
				angleDeviation = rotInRad;
				break;
			default:
				throw new RuntimeException("Unreachable code reached");
		}
		Vector3 rot = blackboard.getVector3( rotParam );
		if ( rot == null ) {
			rot = new Vector3();
			blackboard.put( rotParam , rot );
		}

		if ( angleDeviation == 0 )
		{
			rot.set( entity.getOrientation() );
		}
		else
		{
			float newAngle = Misc.angleY( entity.getOrientation() ) + angleDeviation;
			if ( newAngle > 2*Math.PI) {
				newAngle -= 2*Math.PI;
			} else if ( newAngle < 0 ) {
				newAngle += 2*Math.PI;
			}
			Misc.setToRotatedUnitVector( rot , newAngle );
		}
		blackboard.put( rotParam , rot );

		return rotate.tick(deltaSeconds, blackboard);
	}

	@Override
	public String toString() {
		return "DetectObstacle";
	}
}