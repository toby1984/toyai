package de.codesourcery.toyai;

import java.awt.Color;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.Timer;

import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.behaviours.SeekAndDestroy;
import de.codesourcery.toyai.entities.Player;
import de.codesourcery.toyai.entities.Tank;

public class Main
{
    public static final float FPS = 60;
    private static final boolean ADD_BEHAVIOURS = false;
    private static final boolean RANDOM_ORIENTATION = false;
    private static final int BOTS_IN_TEAM1 = 30;
    private static final int BOTS_IN_TEAM2 = 30;

    public static void main(String[] args)
    {
        final World world = new World();

        final Player team1 =new Player("team #1",Color.RED,new Blackboard(world));
        final Player team2 =new Player("team #1",Color.BLUE,new Blackboard(world));
        world.add(team1,team2);

        final Random rnd = new Random(0xdeadbeef);

        final int xLimit = (GameScreen.MAX_X-40)/2;
        final int yLimit = (GameScreen.MAX_Y-40)/2;

        final Consumer<Entity> setRandomPosition = e ->
        {
          e.position.x = -xLimit+ rnd.nextInt( 2*xLimit );
          e.position.y = -yLimit + rnd.nextInt( 2*yLimit);
          final Vector3 tmp = new Vector3();
          
          if ( RANDOM_ORIENTATION ) {
              final double angleRad = rnd.nextFloat()*2*Math.PI;
              Misc.setToRotatedUnitVector( tmp  , (float) angleRad );
              e.setOrientation( tmp );
          }
          e.boundsDirty = true;
        };

        final Consumer<Player> addNewTank = player ->
        {
            final Tank tank = new Tank(player, new Blackboard(world));
            if ( ADD_BEHAVIOURS ) {
//            	tank.setBehaviour( new Wander( tank , 2 , 5f ) );
            	tank.setBehaviour( new SeekAndDestroy( tank ) );
            }
            do {
                setRandomPosition.accept( tank );
            }
            while ( world.collidesWith( tank ) );
            world.add( tank );
        };

        for ( int i = 0 ; i < BOTS_IN_TEAM1 ; i++ )
        {
            addNewTank.accept( team1 );
            team1.incOwnedEntityCount();
        }

        for ( int i = 0 ; i < BOTS_IN_TEAM2 ; i++ )
        {
            addNewTank.accept( team2 );
            team2.incOwnedEntityCount();
        }

        final GameScreen screen = new GameScreen(world);
        screen.setLocationRelativeTo( null );
        screen.setVisible( true );

        final Timer timer = new Timer( (int) (1000f/FPS) , ev -> doTick(world,screen) );
        timer.start();
    }

    @SuppressWarnings("unused")
    private static float t = 0.0f;
    private static final float STEP_TIME_SECONDS = 0.01f;
    private static float previousTime = System.nanoTime();
    private static float accumulator = 0.0f;

    public static boolean TICK_WORLD = true;

    private static void doTick(World world,GameScreen screen)
    {
    	float now = System.nanoTime();
    	if ( TICK_WORLD )
    	{
	        float frameTime = (now - previousTime) / 1000000000f;

	        accumulator += frameTime;

	        while ( accumulator >= STEP_TIME_SECONDS )
	        {
	            world.tick( STEP_TIME_SECONDS );
	            accumulator -= STEP_TIME_SECONDS;
	            t += STEP_TIME_SECONDS;
	        }
    	}
    	previousTime = now;
        screen.render();
    }
}
