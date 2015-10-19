package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.entities.MoveableEntity;

public class MoveTo extends AbstractBehaviour {

    private final MoveableEntity entity;
    private final String destinationBBParam;

    private IBehaviour wrapper;

    public boolean moveFailed;

    public MoveTo(MoveableEntity e,String destinationBBParam)
    {
        this.entity = e;
        this.destinationBBParam = destinationBBParam;
    }

    protected Vector3 getDestination(IBlackboard bb)
    {
        return bb.getVector3( destinationBBParam );
    }

    private IBehaviour createWrapper(IBlackboard bb)
    {
        // setup stuff
        final AlignWith aimAt = new AlignWith(entity,destinationBBParam);

        return aimAt.andThen(
                new AbstractBehaviour()
                {
                    private boolean decelerating = false;

                    @Override
                    public String toString() {
                        return "MoveLinear to '"+destinationBBParam+"'";
                    }

                    @Override
                    public void discardHook(IBlackboard blackboard)
                    {
                        aimAt.discard(blackboard);
                        entity.stopMoving();
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
                        if ( dst > 2 )
                        {
                            final float speed = entity.velocity.len();
                            float stoppingDistance = -speed*speed / (2 * -Entity.MAX_DECELARATION);
                            //                                    System.out.println("Stopping distance: "+stoppingDistance);
                            if ( dst <=  stoppingDistance || decelerating )
                            {
                                if ( ! decelerating ) {
                                    entity.acceleration = -speed*speed/(2*dst);
                                    decelerating = true;
                                }
                                //                                        System.out.println("distance "+dst+" , decelerating @ "+entity.acceleration+", current speed: "+speed+", delta: "+deltaSeconds*1000f);
                                if ( entity.velocity.len() < 0.1 ) {
                                    entity.stopMoving();
                                    return Result.SUCCESS;
                                }
                            }
                            else
                            {
                                //                                        System.out.println("distance "+dst+" , accelerating @ max."+", current speed: "+speed);
                                entity.acceleration = Entity.MAX_ACCELERATION;
                            }
                            return Result.PENDING;
                        }
                        entity.stopMoving();
                        return Result.SUCCESS;
                    }
                });
    }

    protected static float clamp(float actual,float min,float max)
    {
        return actual < min ? min : ( actual > max ? max : actual );
    }

    @Override
    public String toString() {
        return "MoveTo '"+destinationBBParam+"'";
    }

    @Override
    protected Result tickHook(float deltaSeconds, IBlackboard blackboard)
    {
        if ( wrapper == null ) {
            wrapper = createWrapper( blackboard );
        }
        return wrapper.tick( deltaSeconds, blackboard );
    }

    @Override
    protected void discardHook(IBlackboard blackboard)
    {
        if ( wrapper != null ) {
            wrapper.discard(blackboard);
        }
    }
}