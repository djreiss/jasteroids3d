package djr.d3d;
import java.awt.* ;
import java.awt.image.* ;
import java.applet.*;
//import gad.awt.image.*;

/**
 * Class <code>world3dApplet</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class world3dApplet extends java.applet.Applet implements Runnable {
   int oldX, oldY;
   Image image = null;
   Graphics offscreen = null;
   object3d clickedShape = null;
   world3d world = null;
   Thread t = null;

   MemoryImageSource source = null;
   int pix[] = null;
   Choice choice = null;
   boolean fastDrawing = false;

   int gifPix[] = null, gifw = 0, gifh = 0;

   public void init() {
      choice = new Choice();
      choice.addItem( "Flat Shaded" );
      choice.addItem( "Wireframe" );
      choice.addItem( "Flat Unshaded" );
      choice.addItem( "Gourad Shaded" );
      choice.addItem( "Fast Drawing" );
      add( choice );
      
      polyDraw.gifToPattern( "images/dave.gif", this );
      gifPix = polyDraw.pattern;
      gifw = polyDraw.patw;
      gifh = polyDraw.path;
      
      world = new world3d( 0, 0, size().width, size().height );
      light l = new light( light.POINT );
      world.addLight( l ).offset( 1000, 0, 1000 );
      l = new light( light.AMBIENT );
      world.addLight( l );
      world.drawingMode = face.GOURAD | face.SHADED | face.BACKFACE_CULLING | face.PATTERN_MAPPING;
      fastDrawing = false;
      world.setDefaultAttenuation( 1000.0f, 0.1f, 0.1f, 0.3f ); // Blue fog; fade completely at dist=1000
      
      // SPECIFY OBJECTS HERE
      world.addObject( new movableObject3d( object3d.CUBE ).setRotVelocity( 25, 0, 0 ).startMoving() ).
	 rotate( 50, 50, 0 ).scale( 10, 10, 40 ).offset( 20, 20, 0 ).setColor( Color.green );
      world.addObject( new movableObject3d( object3d.SPHERE ).setRotVelocity( 0, 30, 0 ).startMoving() ).
	 rotate( -50, 30, 20 ).scale( 20, 30, 15 ).offset( 40, -40, 0 ).setColor( Color.magenta );

      object3d plane = ( new movableObject3d( object3d.PLANE ).setRotVelocity( 20, 0, 0 ).startMoving() ).
	 rotate( 90, 0, 0 ).scale( 40, 40, 40 ).offset( -40, -40, 0 ).setPattern( gifPix, gifw, gifh ).
	 setDrawingMode( face.SHADED | face.FLAT | face.PATTERN_MAPPING | face.BACKFACE_CULLING );
      for ( int i = 0; i < 4; i ++ ) plane.verts[i].map = new double[2];
      plane.verts[1].map[0] = plane.verts[1].map[1] = 0.0;
      plane.verts[0].map[0] = 1.0; plane.verts[0].map[1] = 0.0;
      plane.verts[3].map[0] = 1.0; plane.verts[3].map[1] = 1.0;
      plane.verts[2].map[0] = 0.0; plane.verts[2].map[1] = 1.0;
      world.addObject( plane );

      /*world.addObject( new movableObject3d( object3d.PYRAMID ).setRotVelocity( 0, 3, 0 ) ).
	scale( 20 ).offset( -20, -20, 0 ).setColor( Color.red );
      world.addObject( new object3d( object3d.CONE ) ).scale( 20 ).offset( 20, 20, 0 ).
      setColor( Color.yellow );
      world.addObject( new object3d( object3d.PRISM ) ).scale( 20 ).setColor( Color.blue );
      world.addObject( new object3d( object3d.CYLINDER ) ).scale( 20 ).offset( -20, -20, 0 ).
      setColor( Color.orange );*/
      
      // Draw axes:
      //world.addObject( new object3d( object3d.STICKZ ) ).scale( 50 ).setColor( Color.red );
      //world.addObject( new object3d( object3d.STICKY ) ).scale( 50 ).setColor( Color.green );
      //world.addObject( new object3d( object3d.STICKX ) ).scale( 50 ).setColor( Color.blue );

      world.offset( 0, 0, -150 ); // Move the camera back so we see the objects.

      t = new Thread( this );
      t.start();
   }

   public synchronized void run() {
      while ( true ) {
	 repaint();
	 movableObject3d.moveObjects();
         try { wait(); } catch( Exception e ) {} 
      }
   }

   public synchronized void paint( Graphics g ) {
      Graphics gr = g;
      if ( fastDrawing ) {
	 source = null;
	 if ( offscreen == null ) {
	    try {
	       image = createImage( size().width, size().height );
	       offscreen = image.getGraphics();
	    } catch ( Exception e ) { // double-buffering not available
	       offscreen = null;
	    }
	 }
	 if ( offscreen != null ) gr = offscreen; // double-buffering available
      } else {
	 offscreen = null;
	 if ( source == null ) {
	    if ( pix == null ) pix = new int[ size().width * size().height ];
	    source = new MemoryImageSource( size().width, size().height, ColorModel.getRGBdefault( ),
					    pix, 0, size().width );
	    source.setAnimated( true );
	    source.setFullBufferUpdates( true );
	    image = createImage( source );
	 }
      }
      
      if ( fastDrawing ) world.draw( gr );
      else {
	 world.draw( pix, size().width, size().height );
	 source.newPixels();
      }
      if ( image != null ) {
	 g.drawImage( image, 0, 0, this );
      }
      notifyAll();
   }

   public void update( Graphics g ) {
      paint( g );
   }

   public boolean mouseDown( java.awt.Event evt, int x, int y ) {
      oldX = x;
      oldY = y;
      //clickedShape = world.shapeClicked( x, y );
      return true;
   }

   public boolean mouseDrag( java.awt.Event evt, int x, int y ) {
      boolean altDown = ( evt.modifiers & Event.ALT_MASK ) != 0;
      if ( clickedShape == null ) {
	 if ( altDown ) world.offset( ( x - oldX ), ( y - oldY ), 0 );
	 else if ( evt.metaDown() ) world.offset( ( x - oldX ), 0, ( y - oldY ) * 2.0f );
	 //else if ( evt.controlDown() ) world.rotateWorld( ( oldY - y ) * 0.5f, ( x - oldX ) * 0.5f, 0 );
	 else if ( evt.controlDown() ) world.tilt( ( x - oldX ) * 0.5f );
	 else world.pan( ( oldY - y ) * 0.5f, ( x - oldX ) * 0.5f, 0 );
      } else {
	 if ( altDown ) clickedShape.offset( ( x - oldX ), ( y - oldY ), 0 );
	 else if ( evt.metaDown() ) clickedShape.offset( ( x - oldX ), 0, ( y - oldY ) * 2.0f );
	 else clickedShape.rotate( ( oldY - y ) * 0.5f, ( x - oldX ) * 0.5f, 0 );
      }	

      repaint();
      oldX = x;
      oldY = y;
      return true;
   }

   public boolean action( Event evt, Object what ) {
      fastDrawing = false;
      if ( ( (String) what ).equals( "Wireframe" ) ) world.drawingMode = face.WIREFRAME | face.BACKFACE_CULLING;
      else if ( ( (String) what ).equals( "Flat Unshaded" ) ) world.drawingMode = face.FLAT | face.BACKFACE_CULLING;
      else if ( ( (String) what ).equals( "Flat Shaded" ) ) world.drawingMode = face.FLAT | face.SHADED | face.BACKFACE_CULLING;
      else if ( ( (String) what ).equals( "Gourad Shaded" ) ) world.drawingMode = face.GOURAD | face.SHADED | face.BACKFACE_CULLING;
      else if ( ( (String) what ).equals( "Fast Drawing" ) ) fastDrawing = true;
      return true;
   }
}
