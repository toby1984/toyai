package de.codesourcery.toyai;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity.EntityType;
import de.codesourcery.toyai.behaviours.MoveTo;
import de.codesourcery.toyai.behaviours.Wander;
import de.codesourcery.toyai.entities.Bullet;
import de.codesourcery.toyai.entities.MoveableEntity;
import de.codesourcery.toyai.entities.Player;
import de.codesourcery.toyai.entities.Tank;

public class GameScreen extends JFrame {

	public static final boolean DEBUG_RENDER_SEEK_RANGE = false;
	public static final boolean DEBUG_RENDER_TURRET_RANGE = false;

	public static final boolean DEBUG_RENDER_WHISKERS = false;

    public static final int MAX_X = 640;
    public static final int MAX_Y = 480;

    private final MyPanel panel = new MyPanel();
    private final World world;

    public GameScreen(World world)
    {
        this.world = world;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.setPreferredSize( new Dimension(MAX_X,MAX_Y ) );
        panel.setSize( new Dimension(MAX_X,MAX_Y ) );
        getContentPane().add( panel);
        pack();
    }

    protected static final boolean isLeftButton(MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON1;
    }

    protected static final boolean isMiddleButton(MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON2;
    }

    protected static final boolean isRightButton(MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON3;
    }

    protected final class MyPanel extends JPanel
    {
        private float centerX;
        private float centerY;

        private Entity highlighted = null;
        private Entity selected = null;

        @SuppressWarnings("unused")
        private MoveTo animator = null;
        private Vector3 destination = null;

        private final Point mousePos = new Point();

        private long frameCounter = 0;
        private float totalFrameTime = 0;

        private long previousTime = System.currentTimeMillis();

        public MyPanel()
        {
        	addKeyListener( new KeyAdapter()
        	{
        		@Override
        		public void keyReleased(KeyEvent e)
        		{
        			if ( e.getKeyCode() == KeyEvent.VK_SPACE )
        			{
        				Main.TICK_WORLD = ! Main.TICK_WORLD;
        			}
        		}
			});
            final MouseAdapter adapter = new MouseAdapter() {

                @Override
				public void mouseMoved(java.awt.event.MouseEvent e)
                {
                	mousePos.setLocation( e.getPoint() );

                    highlighted  = world.getEntityAt( viewToModel( e.getPoint() ));
                    if ( highlighted == null ) {
                        setToolTipText( null );
                    } else {
                        setToolTipText( "Entity: "+highlighted);
                    }
                }

                @Override
				public void mouseReleased(java.awt.event.MouseEvent e)
                {
                    if ( selected != null && selected.is( EntityType.TANK ) && isLeftButton( e) )
                    {
                        destination = viewToModel( e.getPoint() );

                        selected.blackboard.put( "ui.target" , destination );
//                        selected.setBehaviour( new ShootAt( (Tank) selected , "ui.target" ) );
                        selected.setBehaviour( new Wander( (MoveableEntity) selected , 3f ) );
//                        selected.setBehaviour( new AimAt( selected , "ui.target") );
//                        selected.setBehaviour( new SeekAndDestroy( (Tank) selected ) );
                    }
                    else if ( isRightButton( e ) )
                    {
                        selected = world.getEntityAt( viewToModel( e.getPoint() ));
                    }
                    else if ( selected != null && selected.type.canMove() && isLeftButton( e ) )
                    {
                        destination = viewToModel( e.getPoint() );
                        selected.blackboard.put( "ui.target" , destination );
                        selected.setBehaviour( new MoveTo( (MoveableEntity) selected , "ui.target" ) );
                    }
                };
            };
            addMouseMotionListener( adapter );
            addMouseListener( adapter );
        }
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            final Graphics2D gfx = (Graphics2D) g;

            centerX = getWidth() / 2f;
            centerY = getHeight() / 2f;

            world.visitEntities( e -> render(e,gfx ));

            if ( destination != null )
            {
                final Point mark = modelToView( destination );
                g.setColor( Color.BLACK );
                g.drawLine( mark.x -5  , mark.y , mark.x + 5 , mark.y );
                g.drawLine( mark.x  , mark.y-5 , mark.x , mark.y+5 );
            }

            // draw UI

            // draw mouse position
            gfx.setColor( Color.GRAY );
            gfx.drawLine( 0 , mousePos.y , getWidth() , mousePos.y );
            gfx.drawLine( mousePos.x ,0 , mousePos.x  , getHeight() );

            // draw selection desc
            gfx.setColor( Color.BLACK );

            int y = 15;
            if ( selected != null )
            {
            	gfx.drawString( "Selected: "+selected ,  15 , y );
            	y += 15;

            	final float distance = selected.dst( viewToModel( mousePos ) );
            	gfx.drawString( "Distance: "+distance,  15 , y );
            	y += 15;
            }

            // draw  score
            final List<Player> teams = world.gather( e -> e.is(EntityType.PLAYER ) ).stream().map( p -> (Player) p ).collect( Collectors.toList() );
            final Color old = gfx.getColor();
            for ( int x = 15, i = 0 ; i < teams.size() ; i++ )
            {
            	final Player team = teams.get(i);
            	gfx.setColor( team.color );
            	final String s = team.getName()+" : "+team.getEntityCount()+( (i+1) < teams.size() ? " | " : "");
            	gfx.drawString( s ,  x , y );
            	x += gfx.getFontMetrics().getStringBounds( s , gfx ).getWidth();
            }
            gfx.setColor( old );
            y += 15;

            // draw frame rate
            final long now = System.currentTimeMillis();
            totalFrameTime += (now - previousTime)/1000f;
            previousTime = now;
            frameCounter++;
            final float fps = frameCounter/totalFrameTime;
          	gfx.drawString( "FPS: "+fps,  15 , y );
            y += 15;
        }

        private void render(Entity entity,Graphics2D gfx)
        {
            switch( entity.type )
            {
                case BULLET:
                    renderBullet((Bullet) entity,gfx);
                    break;
                case TANK:
                    renderTank( (Tank) entity,gfx);
                    break;
                default:
                    // $$FALL-THROUGH$$
            }
        }

        private void renderBullet(Bullet entity, Graphics2D gfx)
        {
            final Point p = modelToView( entity.position );

            final Player player = (Player) entity.getRootOwner();
            gfx.setColor( player.color );
            drawFilledCircle(p.x , p.y , Bullet.RADIUS , gfx );
        }

        private void renderTank(Tank entity,Graphics2D gfx)
        {
        	final float angleRad;
        	if ( entity.isMoving() ) {
        		angleRad = Misc.angleY( entity.velocity );
        	} else {
        		angleRad = Misc.angleY( entity.getOrientation() );
        	}

            final float tankWidth = entity.getWidth();
			final float tankHeight = entity.getHeight();

			final Vector3 corner0 = new Vector3( -tankWidth/2 ,  tankHeight/2 , 0 );
            final Vector3 corner1 = new Vector3(  tankWidth/2 ,  tankHeight/2 , 0 );
            final Vector3 corner2 = new Vector3(  tankWidth/2 , -tankHeight/2 , 0 );
            final Vector3 corner3 = new Vector3( -tankWidth/2 , -tankHeight/2 , 0 );
            final Vector3 turretHead = new Vector3( 0 , tankHeight*2 , 0 );

            final Matrix4 rot = new Matrix4();
            rot.setToRotationRad( Misc.Z_AXIS3 , angleRad );

            corner0.mul( rot );
            corner1.mul( rot );
            corner2.mul( rot );
            corner3.mul( rot );
            turretHead.mul( rot );

            corner0.add( entity.position.x , entity.position.y , 0 );
            corner1.add( entity.position.x , entity.position.y , 0 );
            corner2.add( entity.position.x , entity.position.y , 0 );
            corner3.add( entity.position.x , entity.position.y , 0 );
            turretHead.add( entity.position.x , entity.position.y , 0 );

            final int[] x = new int[4];
            final int[] y = new int[4];

            x[0] = (int) (centerX + corner0.x );
            y[0] = (int) (centerY - corner0.y );

            x[1] = (int) (centerX + corner1.x );
            y[1] = (int) (centerY - corner1.y );

            x[2] = (int) (centerX + corner2.x );
            y[2] = (int) (centerY - corner2.y );

            x[3] = (int) (centerX + corner3.x );
            y[3] = (int) (centerY - corner3.y );


            if ( entity == highlighted || entity == selected )
            {
                gfx.setColor( Color.GREEN );
            } else {
                gfx.setColor( ((Player) entity.owner).color );
            }

            // draw seek range
            final int viewPosX = (int) (centerX + entity.position.x);
            final int viewPosY = (int) (centerY - entity.position.y);

            if ( DEBUG_RENDER_SEEK_RANGE ) {
            	drawCircle( viewPosX , viewPosY , (int) Tank.SEEK_RANGE , gfx );
            }

            // draw bounds
            gfx.drawPolygon( x , y , 4 );

            // draw turret
            gfx.drawLine( viewPosX  ,
                          viewPosY,
                          (int) (centerX + turretHead.x) ,
                          (int) (centerY - turretHead.y) );

            // draw turret range
            if ( DEBUG_RENDER_TURRET_RANGE ) {
            	gfx.setColor( Color.YELLOW );
            	drawCircle( viewPosX , viewPosY , (int) Tank.TURRET_RANGE , gfx );
            }

            if ( DEBUG_RENDER_WHISKERS )
            {
            	final Vector3 tmp = new Vector3();
            	final float currentOrientation = entity.getOrientationInRad() ;

            	// draw center whisker
            	Misc.setToRotatedUnitVector( tmp , currentOrientation );

            	tmp.scl( entity.getWhiskerConfiguration().centerRayLen ).add( entity.position );
            	Point p = modelToView( tmp );

            	gfx.setColor(Color.CYAN);
            	gfx.drawLine( viewPosX , viewPosY , p.x ,p.y );

            	// draw left whisker
            	Misc.setToRotatedUnitVector( tmp , currentOrientation + entity.getWhiskerConfiguration().whiskerAngleDeg*Misc.TO_RAD );

            	tmp.scl( entity.getWhiskerConfiguration().whiskerRayLen ).add( entity.position );
            	p = modelToView( tmp );

            	gfx.setColor(Color.MAGENTA);
            	gfx.drawLine( viewPosX , viewPosY , p.x ,p.y );

            	// draw right whisker
            	Misc.setToRotatedUnitVector( tmp , currentOrientation - entity.getWhiskerConfiguration().whiskerAngleDeg*Misc.TO_RAD );

            	tmp.scl( entity.getWhiskerConfiguration().whiskerRayLen ).add( entity.position );
            	p = modelToView( tmp );

            	gfx.setColor(Color.MAGENTA);
            	gfx.drawLine( viewPosX , viewPosY , p.x ,p.y );

            }
        }

        private void drawCircle(int x,int y,int radius,Graphics2D gfx)
        {
            gfx.drawArc( x-radius , y-radius, 2*radius,2*radius , 0 , 360 );
        }

        private void drawFilledCircle(int x,int y,int radius,Graphics2D gfx)
        {
            gfx.fillArc( x-radius , y-radius, 2*radius,2*radius , 0 , 360 );
        }

        protected Vector3 viewToModel(Point p) {
            return new Vector3((float) (p.getX() - centerX),(float) (centerY - p.getY()),0);
        }

        protected Point modelToView(Vector3 v) {
            return new Point( (int) (centerX + v.x) , (int) (centerY - v.y) );
        }
    }

    public void render() {
        panel.repaint();
    }
}