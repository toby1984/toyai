package de.codesourcery.toyai;

import java.awt.Color;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.Timer;

import de.codesourcery.toyai.ticklisteners.SeekAndDestroy;

public class Main 
{

    public static final float FPS = 60;
    
    public static void main(String[] args) 
    {
        final World world = new World();
        
        final Player team1 =new Player("team #1",Color.RED);
        final Player team2 =new Player("team #1",Color.BLUE);
        world.add(team1,team2);
        
        final Random rnd = new Random(0xdeadbeef);
        
        final Consumer<Entity> setRandomPosition = e -> 
        {
          e.position.x = -GameScreen.MAX_X/2 + rnd.nextInt( GameScreen.MAX_X );  
          e.position.y = -GameScreen.MAX_Y/2 + rnd.nextInt( GameScreen.MAX_Y );  
          e.boundsDirty = true;
        };
        
        for ( int i = 0 ; i < 10 ; i++ ) 
        {
            final Tank e1 = new Tank( team1 );
            e1.setBehaviour( new SeekAndDestroy( world , e1 ) );
            do { 
                setRandomPosition.accept( e1 );
            } while ( world.collidesWith( e1 ) );
            team1.add( e1 );
            world.add( e1 );
            
            final Tank e2 = new Tank(team2 );
            e2.setBehaviour( new SeekAndDestroy( world , e2 ) );
            do {
                setRandomPosition.accept( e2 );
            } while ( world.collidesWith( e2 ) );
            team2.add( e2 );
            world.add( e2 );
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
    
    private static void doTick(World world,GameScreen screen) 
    {
        float now = System.nanoTime();
        float frameTime = (now - previousTime)/1000000000f;
        previousTime = now;

        accumulator += frameTime;

        while ( accumulator >= STEP_TIME_SECONDS )
        {
            world.tick( STEP_TIME_SECONDS );
            accumulator -= STEP_TIME_SECONDS;
            t += STEP_TIME_SECONDS;
        }
        screen.render();
    }
}
