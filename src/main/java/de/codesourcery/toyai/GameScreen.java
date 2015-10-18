package de.codesourcery.toyai;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.codesourcery.toyai.Entity.EntityType;
import de.codesourcery.toyai.behaviours.MoveTo;
import de.codesourcery.toyai.behaviours.Wander;
import de.codesourcery.toyai.entities.Bullet;
import de.codesourcery.toyai.entities.MoveableEntity;
import de.codesourcery.toyai.entities.Player;
import de.codesourcery.toyai.entities.Tank;

public class GameScreen extends JFrame {

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
        private Vector2 destination = null;
        
        public MyPanel() 
        {
            final MouseAdapter adapter = new MouseAdapter() {
                
                public void mouseMoved(java.awt.event.MouseEvent e) 
                {
                    highlighted  = world.getEntityAt( screenToModel( e.getPoint() ));
                    if ( highlighted == null ) {
                        setToolTipText( null );
                    } else {
                        setToolTipText( "Entity: "+highlighted);
                    }
                }
                
                public void mouseReleased(java.awt.event.MouseEvent e) 
                {
                    if ( selected != null && selected.is( EntityType.TANK ) && isLeftButton( e) ) 
                    {
                        destination = screenToModel( e.getPoint() );
                        
                        selected.blackboard.put( "ui.target" , destination );
//                        selected.setBehaviour( new ShootAt( (Tank) selected , "ui.target" ) );
                        selected.setBehaviour( new Wander( (MoveableEntity) selected , 3f ) );
//                        selected.setBehaviour( new AimAt( (MoveableEntity) selected , "ui.target") );
                    } 
                    else if ( isRightButton( e ) ) 
                    {
                        selected = world.getEntityAt( screenToModel( e.getPoint() ));
                    } 
                    else if ( selected != null && selected.type.canMove() && isLeftButton( e ) ) 
                    {
                        destination = screenToModel( e.getPoint() );
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
                final Point mark = modelToScreen( destination );
                g.setColor( Color.BLACK );
                g.drawLine( mark.x -5  , mark.y , mark.x + 5 , mark.y );
                g.drawLine( mark.x  , mark.y-5 , mark.x , mark.y+5 );
            }
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
            final Point p = modelToScreen( entity.position );

            final Player player = (Player) entity.getRootOwner();
            gfx.setColor( player.color );
            
            gfx.drawArc( p.x - 2 , p.y - 2 , 4 , 4 , 0 , 360 );
        }
        
        private void renderTank(Tank entity,Graphics2D gfx) 
        {
            final float angleRad = Misc.angleY( entity.getOrientation() );

            final Vector3 corner0 = new Vector3( -entity.width/2 , entity.height/2 , 0 );
            final Vector3 corner1 = new Vector3(  entity.width/2 , entity.height/2 , 0 );
            final Vector3 corner2 = new Vector3(  entity.width/2 , -entity.height/2 , 0 );
            final Vector3 corner3 = new Vector3( -entity.width/2 , -entity.height/2 , 0 );
            final Vector3 turretHead = new Vector3( 0 , entity.height*2 , 0 );
            
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
            
            // draw bounds
            gfx.drawPolygon( x , y , 4 );
            
            // draw turret
            gfx.drawLine( (int) (centerX + entity.position.x) , 
                          (int) (centerY - entity.position.y) , 
                          (int) (centerX + turretHead.x) , 
                          (int) (centerY - turretHead.y) );
        }
        
        protected Vector2 screenToModel(Point p) {
            return new Vector2((float) (p.getX() - centerX),(float) (centerY - p.getY()));
        }
        
        protected Point modelToScreen(Vector2 v) {
            return new Point( (int) (centerX + v.x) , (int) (centerY - v.y) );
        }
        
        protected Point modelToScreen(Vector3 v) {
            return new Point( (int) (centerX + v.x) , (int) (centerY - v.y) );
        }          
    }

    public void render() {
        panel.repaint();
    }    
}