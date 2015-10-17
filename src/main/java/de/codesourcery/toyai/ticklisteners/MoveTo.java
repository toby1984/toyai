package de.codesourcery.toyai.ticklisteners;

import com.badlogic.gdx.math.Vector2;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBehaviour;
import de.codesourcery.toyai.MoveableEntity;

public class MoveTo implements IBehaviour {

    private final MoveableEntity entity;
    private final Vector2 destination;

    private IBehaviour wrapper;
    
    public boolean moveFailed;

    public MoveTo(MoveableEntity e,Vector2 destination) 
    {
        this.entity = e;
        this.destination = new Vector2(destination);

        // setup stuff
        final Vector2 tmp = new Vector2( destination );        
        tmp.sub( entity.position );

        this.wrapper = 
                new Rotate( entity , tmp )
                .andThen( 
                        new IBehaviour() 
                        {
                            private final Animator animator = new Animator( e , Entity.MAX_VELOCITY );
                            private boolean decelerating = false;

                            @Override
                            public String toString() {
                                return "MoveLinear to "+destination;
                            }

                            @Override
                            public void onCancel() {
                                entity.stopMoving();
                            }

                            @Override
                            public Result tick(float deltaSeconds) 
                            {
                                final float dst = entity.dst( destination );

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
                                            System.out.println("Entity halted");
                                            entity.stopMoving();
                                            return Result.SUCCESS;
                                        }
                                    } 
                                    else 
                                    {
                                        //                                        System.out.println("distance "+dst+" , accelerating @ max."+", current speed: "+speed);
                                        entity.acceleration = Entity.MAX_ACCELERATION;
                                    }
                                    System.out.println("Moving "+entity.id);
                                    return animator.tick( deltaSeconds );        
                                }
                                System.out.println("MoveTo has reached goal");
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
        return "MoveTo "+destination;
    }

    @Override
    public Result tick(float deltaSeconds) 
    {
        return wrapper.tick( deltaSeconds );
    }

    @Override
    public void onCancel() {
        wrapper.onCancel();
    }
}