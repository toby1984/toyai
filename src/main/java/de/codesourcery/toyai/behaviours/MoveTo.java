package de.codesourcery.toyai.behaviours;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.IBlackboard;
import de.codesourcery.toyai.decorators.Conditional;
import de.codesourcery.toyai.decorators.DetectObstacle;
import de.codesourcery.toyai.decorators.Obstructed;
import de.codesourcery.toyai.entities.MoveableEntity;

public final class MoveTo extends AbstractBehaviour {

    private final MoveableEntity entity;
    private final String rotParam;
    private final String destinationBBParam;

    private IBehaviour wrapper;

    public boolean moveFailed;

    public MoveTo(MoveableEntity e,String destinationBBParam,String rotParam)
    {
        this.entity = e;
        this.destinationBBParam = destinationBBParam;
        this.rotParam = rotParam;
    }

    protected Vector3 getDestination(IBlackboard bb)
    {
        return bb.getVector3( destinationBBParam );
    }

    private IBehaviour createWrapper(IBlackboard bb)
    {
        // setup stuff
        final IBehaviour stopAtDestination = new AbstractBehaviour()
        {
            private boolean decelerating = false;

            @Override
            public String toString() {
                return "MoveLinear to '"+destinationBBParam+"'";
            }

            @Override
            public void discardHook(IBlackboard blackboard)
            {
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
                    final float stoppingTime = 0.25f; 
                    final float speed = entity.velocity.len();
                    
                    float a = ( 2*(dst - speed*stoppingTime) ) / (stoppingTime*stoppingTime);
                    //                                    System.out.println("Stopping distance: "+stoppingDistance);
                    if ( a >= Entity.MAX_DECELARATION )
                    {
                        if ( ! decelerating ) {
                            entity.acceleration = -a;
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
                        decelerating = false;
                    }
                    return Result.PENDING;
                }
                LOG.log("Destination reached");
                decelerating = false;
                entity.stopMoving();
                return Result.SUCCESS;
            }
        };     
        
        final String obsParam = registerParam( getId()+".obstacle" );
        
        final IBehaviour condition = new Obstructed( obsParam );
        final IBehaviour ifTrue = new AvoidObstacle( entity , rotParam , obsParam );
        final AlignWith aimAt = new AlignWith(entity,destinationBBParam , rotParam );
        final IBehaviour ifFalse = parallel( aimAt , stopAtDestination);
        final IBehaviour b = new Conditional( condition , ifTrue ,ifFalse );
        
        return new DetectObstacle( entity , obsParam , b ); 
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