package de.codesourcery.toyai;

import java.awt.Color;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.Timer;

import de.codesourcery.toyai.behaviours.SeekAndDestroy;
import de.codesourcery.toyai.entities.Player;
import de.codesourcery.toyai.entities.Tank;

public class Main 
{
    public static final float FPS = 60;
    
    public static void main(String[] args) 
    {
        final World world = new World();
        
        final Player team1 =new Player("team #1",Color.RED,new Blackboard(world));
        final Player team2 =new Player("team #1",Color.BLUE,new Blackboard(world));
        world.add(team1,team2);
        
        final Random rnd = new Random();
        
        final int xLimit = (GameScreen.MAX_X-40)/2;
        final int yLimit = (GameScreen.MAX_Y-40)/2;
        
        final Consumer<Entity> setRandomPosition = e -> 
        {
          e.position.x = -xLimit+ rnd.nextInt( 2*xLimit );  
          e.position.y = -yLimit + rnd.nextInt( 2*yLimit);  
          e.boundsDirty = true;
        };
        
        final Consumer<Player> addNewTank = player -> 
        {
            final Tank tank = new Tank(player, new Blackboard(world));
//            tank.setBehaviour( new SeekAndDestroy( tank ) );
            do {
                setRandomPosition.accept( tank );
            } 
            while ( world.collidesWith( tank ) );
            world.add( tank );
        };
        
        for ( int i = 0 ; i < 10 ; i++ ) 
        {
            addNewTank.accept( team1 );
            addNewTank.accept( team2 );
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
