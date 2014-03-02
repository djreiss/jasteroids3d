package djr.d3d.anim;
import java.awt.* ;
import java.awt.image.* ;
import java.applet.*;

/**
 * Class <code>animStrip</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class animStrip {
   public static animStrip strips[] = new animStrip[ 5 ];
   public static int nstrips = 0;
   public Image images[] = null;
   public int nframes = 0, current = 0, advance = 1; // Default is to change frame # by one every update
   public SplitImage splitter = null;
   public boolean moving = false, loop = false;
   public double fps = 0, scale = 1; // Frames per second; scale factor (<=0 means none)
   public long lastTime = 0;
   public int x, y, width, height;
   public Applet applet = null;

   public animStrip( String imgname, Applet applet, int nimages ) {
      this.applet = applet;
      this.nframes = nimages;
      Image image = djr.util.gui.AWTUtils.getImage( applet, imgname );
      splitem( image );
   }

   public animStrip( Image image, Applet applet, int nimages ) {
      this.applet = applet;
      this.nframes = nimages;
      splitem( image );
   }

   public animStrip( Image images[], Applet applet, int nimages ) {
      this.applet = applet;
      this.nframes = nimages;
      this.images = images;
      if ( images != null && images[ 0 ] != null ) {
	 this.width = images[ 0 ].getWidth( applet );
	 this.height = images[ 0 ].getHeight( applet );
      }
   }

   public void splitem( Image image ) {
      applet.showStatus( "Initializing animation images..." );
      try {
	 if ( image == null ) throw new Exception( "Image was not loaded." );
	 splitter = new SplitImage( image, 1, nframes ); // Assume its a horizontal strip
	 images = new Image[ nframes ];
	 MediaTracker media = new MediaTracker( applet );
	 ImageProducer producer = null;
	 for ( int i = 0; i < nframes; i ++ ) {
	    producer = splitter.getImageProducer( 0, i );
	    images[ i ] = applet.createImage( producer );
	    media.addImage( images[i], i );
	 }
	 media.waitForAll();
	 height = splitter.getHeight();
	 width = splitter.getWidth();
      } catch( Exception e ) {
	 System.out.println( e );
      }
      applet.showStatus( "" );
   }

   public void update( Graphics g ) {
      if ( images == null ) return;
      if ( ! moving ) return;
      long newTime = System.currentTimeMillis();
      if ( fps != 0 ) advance = (int) ( ( (double) newTime - (double) lastTime ) / 1000.0 * fps );
      current += advance;
      if ( current >= nframes ) {
	 if ( ! loop ) {
	    stopAnimating();
	    return;
	 } else {
	    current = current % nframes;
	 }
      }
      lastTime = newTime;
      if ( images[ current ] == null ) return;
      if ( this.scale <= 0 || this.scale == 1 ) {
	 g.drawImage( images[ current ], this.x-width/2, this.y-height/2, applet );
      } else {
	 int neww = (int) ( this.width * this.scale );
	 int newh = (int) ( this.height * this.scale );
	 g.drawImage( images[ current ], this.x-neww/2, this.y-newh/2, neww, newh, applet );
      }
   }

   public void startAnimating() {
      addStrip( this );
      moving = true;
      lastTime = System.currentTimeMillis();
   }

   public void setCenter( int x, int y, double scale ) {
      this.x = x;
      this.y = y;
      this.scale = scale;
   }

   public void stopAnimating() {
      removeStrip( this );
      moving = false;
      current = 0;
   }

   private static void addStrip( animStrip obj ) {
      animStrip newobj[] = null;
      if ( nstrips == strips.length ) {
	 newobj = new animStrip[ nstrips + 5 ];
	 System.arraycopy( strips, 0, newobj, 0, nstrips );
	 strips = newobj;
      }
      strips[ nstrips++ ] = obj;
   }

   private static void removeStrip( animStrip obj ) {
      for ( int i = 0; i < nstrips; i ++ ) {
	 if ( strips[ i ] == obj ) {
	    strips[ i ] = null;
	    int j = strips.length - i - 1;
	    if ( j > 0 ) {
	       System.arraycopy( strips, i+1, strips, i, j );
	       nstrips --;
	    }
	    return;
	 }
      }
   }

   public static void updateStrips( Graphics g ) {
      for ( int i = nstrips-1; i >= 0; i -- ) {
	 if ( strips[ i ] != null ) strips[ i ].update( g );
      }
   }   
}
