package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.Misc;
import de.codesourcery.toyai.entities.MoveableEntity;

public final class AlignWith extends AbstractBehaviour
{
    private final Entity entity;
    private final String targetToTrackBBParam;
    private final String rotationAngleBBParam;

    private final Rotate rotate;

    public AlignWith(MoveableEntity entity,String targetToTrackBBParam,String rotationAngleBBParam)
    {
        this.targetToTrackBBParam = targetToTrackBBParam;
        this.rotationAngleBBParam = rotationAngleBBParam;
        this.entity = entity;
        this.rotate = new Rotate( entity , rotationAngleBBParam );
    }

    private Vector3 getDestination(IBlackboard bb)
    {
        return bb.getVector3( targetToTrackBBParam );
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        final Vector3 destination = getDestination( blackboard );
        Vector3 tmp = blackboard.getVector3( rotationAngleBBParam );
        if ( tmp == null ) {
            tmp = new Vector3( destination );
        } else {
            tmp.set( destination );
        }
        tmp.sub( entity.position );
        blackboard.put( rotationAngleBBParam , tmp );

        LOG.log("Align to destination "+destination+" at angle "+Misc.angleY( tmp )*Misc.TO_DEG+"°" );

        return rotate.tick(deltaSeconds, blackboard);
    }

    @Override
    protected void discardHook(IBlackboard bb)
    {
        rotate.discard( bb );
    }

	@Override
	public String toString() {
		return "AlignWith '"+targetToTrackBBParam+"'";
	}
}