package djr.d3d.anim;
import java.awt.*;
import java.applet.*;
import djr.d3d.*;

/**
 * Class <code>animStrip3d</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class animStrip3d extends movableObject3d {
   public animStrip strip = null;
   public vertex center = null;

   public animStrip3d( String imgName, Applet applet, int nimages, double scale ) {
      strip = new animStrip( imgName, applet, nimages );
      strip.scale = scale;
   }

   public animStrip3d( Image image, Applet applet, int nimages, double scale ) {
      strip = new animStrip( image, applet, nimages );
      strip.scale = scale;      
   }

   public animStrip3d( Image images[], Applet applet, int nimages, double scale ) {
      strip = new animStrip( images, applet, nimages );
      strip.scale = scale;
   }

   public void setCenter( vertex c ) {
      center = new vertex().set( c.xt, c.yt, c.zt );
      addVertex( center );
   }

   public void draw( light lights[], int nlights, Graphics g, int pix[], int w, int h ) {
      strip.setCenter( (int) center.xp, (int) center.yp, strip.scale );
      strip.update( g );
      if ( ! strip.moving ) { // This will be true if strip.loop == false
	 stopMoving();
	 world3d.currentWorld.removeObject( this );
      }
   }

   public double getZdist() {
      return center.wp;
   }

   public movableObject3d stopMoving() {
      strip.moving = false;
      return super.stopMoving();
   }

   public movableObject3d startMoving() {
      strip.moving = true;
      return super.startMoving();
   }
}
