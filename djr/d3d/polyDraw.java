package djr.d3d;

import java.awt.* ;
import java.awt.image.* ;
 
/**
 * Class <code>polyDraw</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class polyDraw {
   public static int w = 0, h = 0, pix[] = null;
   public static int rgb = 0, max = 0, pmin = 99999999, imin = 0, pmax = -9999999, imax = 0;
   public static int ii = 1, jj = 2, iold = 0, jold = 0, tmp, start, end;
   public static boolean done = false;
   public static Polygon poly = null;
   public static polyDraw edge1 = new polyDraw(), edge2 = new polyDraw();
   public static int pattern[] = null, patw = 0, path = 0, patmax = 0;
   public static double mapx[] = null, mapy[] = null;
   public static int colors[] = null;
   public static polyDraw dxedge2 = new polyDraw(), dyedge2 = new polyDraw();
   public static polyDraw aedge2 = new polyDraw(), redge2 = new polyDraw(), gedge2 = new polyDraw();
   public static polyDraw bedge2 = new polyDraw();
   
   public int i, j, i_inc, dx, dy, e;
   public polyDraw dxedge = null, dyedge = null;
   public boolean doPattern = false; // Pattern mapping onto polygon
   public polyDraw aedge = null, redge = null, gedge = null, bedge = null;
   public boolean doColor = false; // Color interpolation from vertices

   private polyDraw() {
      i = j = i_inc = dx = dy = e = 0;
   }
   
   public static void initialize( int[] ppix, int ww, int hh, Polygon pp, Color ccolor ) {
      pix = ppix;
      w = ww;
      h = hh;
      max = w * h;
      poly = pp;
      if ( ccolor != null ) rgb = ccolor.getRGB();
      else rgb = 0;
   }

   public static void gifToPattern( String gifName, Component obs ) {
      Image gif = gif = djr.util.gui.AWTUtils.getImage( obs, gifName );
      djr.util.gui.AWTUtils.waitForImage( gif, obs );
      if ( gif == null ) {
	 System.err.println( "Error in image loading: " + gifName );
	 return;
      }
      patw = gif.getWidth( obs );
      path = gif.getHeight( obs );
      pattern = new int[ patw * path ];
      PixelGrabber grabber = new PixelGrabber( gif, 0, 0, patw, path, pattern, 0, patw );
      try { grabber.grabPixels(); } catch ( InterruptedException e ) { };
      if ( ( grabber.status() & ImageObserver.ABORT ) != 0 ) {
	 System.err.println( "Image fetch aborted or errored: " + gifName );
      }	
   }
   
   public static void setPattern( int[] pat, int ww, int hh, double xx[], double yy[] ) {
      pattern = pat;
      patw = ww;
      path = hh;
      patmax = ww * hh;
      mapx = xx;
      mapy = yy;
   }

   public static void setColors( int[] cs ) {
      colors = cs;
   }
   
   public static final void drawPolygon() {
      if ( poly == null ) return;
      for ( ii = 0; ii < poly.npoints - 1; ii ++ ) { // This is to draw the outline
	 midpointLine( poly.xpoints[ii], poly.ypoints[ii], poly.xpoints[ii+1], poly.ypoints[ii+1] );
      }
      midpointLine( poly.xpoints[0], poly.ypoints[0], poly.xpoints[poly.npoints-1], poly.ypoints[poly.npoints-1] );
   }

   public static final void fillPolygon() {
      if ( poly == null ) return;
      pmin = 999999999;
      pmax = -999999999;
      for ( ii = 0; ii < poly.npoints; ii ++ ) {
	 if ( poly.ypoints[ii] < pmin ) {
	    pmin = poly.ypoints[ii]; // Find the top vertex of the polygon
	    imin = ii;
	 }
	 if ( poly.ypoints[ii] > pmax ) {
	    pmax = poly.ypoints[ii]; // Find the bottom vertex
	    imax = ii;
	 }
      }	
      iold = imin;
      jold = imin;
      if ( imin == 0 ) ii = poly.npoints-1;
      else ii = imin - 1;
      if ( imin == poly.npoints-1 ) jj = 0;
      else jj = imin + 1; // Left side
      edge1.initialize( poly.xpoints[iold], poly.ypoints[iold], poly.xpoints[ii], poly.ypoints[ii] );
      if ( pattern != null ) edge1.setPatternInfo( poly.ypoints[iold], poly.ypoints[ii], 
						   mapx[iold], mapx[ii], mapy[iold], mapy[ii] );
      else edge1.doPattern = false;
      if ( colors != null ) edge1.setColorInfo( poly.ypoints[iold], poly.ypoints[ii], 
						colors[iold], colors[ii] );
      else edge1.doColor = false; // Right side
      edge2.initialize( poly.xpoints[jold], poly.ypoints[jold], poly.xpoints[jj], poly.ypoints[jj] );
      if ( pattern != null ) edge2.setPatternInfo( poly.ypoints[jold], poly.ypoints[jj], 
						   mapx[jold], mapx[jj], mapy[jold], mapy[jj] );
      else edge2.doPattern = false;
      if ( colors != null ) edge2.setColorInfo( poly.ypoints[jold], poly.ypoints[jj], 
						colors[jold], colors[jj] );
      else edge2.doColor = false;
      done = false;
      while ( ! done ) { // Assumes vertices are in order...either clockwise
	 if ( poly.ypoints[ii] == pmax && poly.ypoints[jj] == pmax ) done = true;
	 while ( edge1.j < poly.ypoints[ii] && edge2.j < poly.ypoints[jj] ) { // or counter-clockwise
	    edge1.update();
	    edge2.update();
	    edge1.fill( edge2 );
	 }
	 if ( edge1.j >= poly.ypoints[ii] ) {
	    iold = ii;
	    if ( ii == 0 ) ii = poly.npoints-1;
	    else ii --;
	    edge1.initialize( poly.xpoints[iold], poly.ypoints[iold], poly.xpoints[ii], poly.ypoints[ii] );
	    if ( pattern != null ) edge1.setPatternInfo( poly.ypoints[iold], poly.ypoints[ii], 
							 mapx[iold], mapx[ii], mapy[iold], mapy[ii] );
	    if ( colors != null ) edge1.setColorInfo( poly.ypoints[iold], poly.ypoints[ii], 
						      colors[iold], colors[ii] );
	 }
	 if ( edge2.j >= poly.ypoints[jj] ) {
	    jold = jj;
	    if ( jj == poly.npoints-1 ) jj = 0;
	    else jj ++;
	    edge2.initialize( poly.xpoints[jold], poly.ypoints[jold], poly.xpoints[jj], poly.ypoints[jj] );
	    if ( pattern != null ) edge2.setPatternInfo( poly.ypoints[jold], poly.ypoints[jj], 
							 mapx[jold], mapx[jj], mapy[jold], mapy[jj] );
	    if ( colors != null ) edge2.setColorInfo( poly.ypoints[jold], poly.ypoints[jj], 
						      colors[jold], colors[jj] );
	 }	
      }
   }

   void initialize( int x1, int y1, int x2, int y2 ) {
      j = y1;
      i = x1;
      dx = x2 - x1;
      dy = y2 - y1; // y2 must always be >= y1
      if ( dx >= 0 ) {
	 i_inc = 1;
	 e = dx;
      } else {
	 i_inc = -1;
	 e = 0;
	 dx = -dx;
      }
   }
   
   void setPatternInfo( int y1, int y2, double xx1, double xx2, double yy1, double yy2 ) {
      if ( dxedge == null ) {
	 dxedge = new polyDraw();
	 if ( dyedge == null ) dyedge = new polyDraw();
      }
      dxedge.initialize( (int) ( xx1 * patw ), y1, (int) ( xx2 * patw ), y2 );
      dyedge.initialize( (int) ( yy1 * path ), y1, (int) ( yy2 * path ), y2 );
      doPattern = true;
   }
   
   void setColorInfo( int y1, int y2, int c1, int c2 ) {
      int a1 = ( c1 >> 24 ) & 0xff;
      int r1 = ( c1 >> 16 ) & 0xff;
      int g1 = ( c1 >> 8 ) & 0xff;
      int b1 = c1 & 0xff;
      int a2 = ( c2 >> 24 ) & 0xff;
      int r2 = ( c2 >> 16 ) & 0xff;
      int g2 = ( c2 >> 8 ) & 0xff;
      int b2 = c2 & 0xff;
      if ( aedge == null ) {
	 aedge = new polyDraw();
	 if ( redge == null ) redge = new polyDraw();
	 if ( gedge == null ) gedge = new polyDraw();
	 if ( bedge == null ) bedge = new polyDraw();
      }
      if ( a1 != a2 ) aedge.initialize( a1, y1, a2, y2 ); // Increment color as a func. of scanline
      else {
	 aedge.i = a1;
	 aedge.dx = Integer.MIN_VALUE;
      }
      redge.initialize( r1, y1, r2, y2 ); // using brehensons algorithm (same as for edges of line)
      gedge.initialize( g1, y1, g2, y2 );
      bedge.initialize( b1, y1, b2, y2 );
      doColor = true;
   }
   
   void update() {
      j ++;
      if ( doPattern ) {
	 dxedge.update();
	 dyedge.update();
      }
      if ( doColor ) {
	 if ( aedge.dx != Integer.MIN_VALUE ) aedge.update();
	 redge.update();
	 gedge.update();
	 bedge.update();
      }
      if ( dx == 0 || dy == 0 ) return;
      while ( e >= 0 ) {
      	i += i_inc;
      	e -= dy;
      }
      e += dx;
   }

   void fill( polyDraw e2 ) { // This.j is the scanline (y); this.i is the column (x)
      if ( j < 0 || j >= h ) return;
      int col = j * w;
      int pixel;
      polyDraw bot = this, top = e2;
      if ( i <= e2.i ) { // TOP or BOTTOM (on screen -- so its reversed) of polygon
	 top = this;
	 bot = e2;
      }
      if ( doColor || doPattern ) { // Use brehensons algorithm to increment color along scan line...
	 if ( doPattern ) {
	    dxedge2.initialize( top.dxedge.i, top.i, bot.dxedge.i, bot.i );
	    dyedge2.initialize( top.dyedge.i, top.i, bot.dyedge.i, bot.i );
	 }
	 if ( doColor ) {
	    aedge2.initialize( top.aedge.i, top.i, bot.aedge.i, bot.i );
	    redge2.initialize( top.redge.i, top.i, bot.redge.i, bot.i );
	    gedge2.initialize( top.gedge.i, top.i, bot.gedge.i, bot.i );
	    bedge2.initialize( top.bedge.i, top.i, bot.bedge.i, bot.i );
	 }
	 boolean done1 = false;
	 while ( ! done1 ) {
	    if ( doPattern ) {
	       if ( dxedge2.j < 0 || dxedge2.j >= w ) continue;
	       pixel = dxedge2.i + dyedge2.i * patw;
	       if ( pixel < patmax && pixel >= 0 ) rgb = pattern[ pixel ];
	       pixel = dxedge2.j + col;
	       if ( pixel < max && pixel >= 0 ) pix[ pixel ] = rgb;
	       if ( dxedge2.j == bot.i ) {
		  done1 = true;
	       } else {
		  dxedge2.update();
		  dyedge2.update();
	       }
	    }
	    if ( doColor ) {
	       pixel = redge2.j + col;
	       if ( pixel < max && pixel >= 0 ) {
		  if ( ! doPattern ) {
		     pix[ pixel ] = ( aedge2.i << 24 ) | ( redge2.i << 16 ) | ( gedge2.i << 8 ) | bedge2.i;
		  } else {
		     int a = (int) ( ( ( rgb >> 24 ) & 0xff ) * ( aedge2.i / 255.0 ) );
		     int r = (int) ( ( ( rgb >> 16 ) & 0xff ) * ( redge2.i / 255.0 ) );
		     int g = (int) ( ( ( rgb >>  8 ) & 0xff ) * ( gedge2.i / 255.0 ) );
		     int b = (int) ( ( ( rgb       ) & 0xff ) * ( bedge2.i / 255.0 ) );
		     pix[ pixel ] = ( a << 24 ) | ( r << 16 ) | ( g << 8 ) | b;
		  }
	       }
	       if ( redge2.j == bot.i ) {
		  done1 = true;
	       } else {
		  if ( aedge2.dx != 0 ) aedge2.update();
		  redge2.update();
		  if ( gedge2.dx != 0 ) gedge2.update();
		  if ( bedge2.dx != 0 ) bedge2.update();
	       }
	    }
	 }
      }
      if ( ! doPattern && ! doColor ) { // Just uniform color fill
	 int start = top.i >= 0 ? top.i + j * w : j * w; // Clip to left side of array
	 int end = bot.i < w ? bot.i + j * w : j * w; // Clip to right side
	 if ( start >= max ) return;
	 else if ( start < 0 ) start = 0;
	 if ( end >= max ) end = max - 1;
	 else if ( end < 0 ) return;
	 while ( start <= end ) {
	    pix[ start ++ ] = rgb;
	 }
      }
   }

   static final int DX = 0, DY = 1, INCRE = 2, INCRNE = 3, D = 4, XPOS = 5, INVERT = 6;
   static final int X0 = 7, Y0 = 8, X1 = 9, Y1 = 10, X = 11, Y = 12, DONE = 13, XOUT = 14;
   static final int YOUT = 15; // Define indices into the init arrays 
   static int init1[] = new int[ 16 ]; // giving the actual variables.
   
   static final void midpointLine( int x0, int y0, int x1, int y1 ) {
      initMidpointLine( x0, y0, x1, y1, init1 );
      fill( init1[ XOUT ], init1[ YOUT ] );
      while ( init1[ DONE ] == 0 ) {
	 updateMidpointLine( init1 );
	 fill( init1[ XOUT ], init1[ YOUT ] );
      }
   }

   static final void initMidpointLine( int x0, int y0, int x1, int y1, int[] out ) {
      int tmp;
      if ( y1 < y0 ) { // Make sure dy > 0.
	 tmp = x0;
	 x0 = x1;
	 x1 = tmp;
	 tmp = y0;
	 y0 = y1;
	 y1 = tmp;
      }
      out[ DX ] = x1 - x0;
      out[ DY ] = y1 - y0;
      out[ INVERT ] = 0; // Boolean...need to invert coords if |slope| > 1.
      if ( ( out[ DX ] > 0 && out[ DX ] < out[ DY ] ) || ( out[ DX ] < 0 && out[ DX ] > -out[ DY ] ) ) { // |slope| > 1...just mirror about x=y line
	 tmp = x0;
	 x0 = y0;
	 y0 = tmp;
	 tmp = x1;
	 x1 = y1;
	 y1 = tmp;
	 if ( y1 < y0 ) { // Make sure dy > 0.
	    tmp = x0;
	    x0 = x1;
	    x1 = tmp;
	    tmp = y0;
	    y0 = y1;
	    y1 = tmp;
	 }
	 out[ DX ] = x1 - x0; // Recalculate dx, dy
	 out[ DY ] = y1 - y0;
	 out[ INVERT ] = 1;
      }
      out[ X0 ] = out[ X ] = x0; // Starting and incremented coords, respectively
      out[ Y0 ] = out[ Y ] = y0;
      out[ X1 ] = x1;
      out[ Y1 ] = y1;
      out[ XPOS ] = out[ DX ] > 0 ? 1 : 0; // Boolean...is dx positive?
      out[ INCRE ] = out[ XPOS ] == 1 ? 2 * out[ DY ] : -2 * out[ DY ];
      out[ D ] = out[ INCRE ] - out[ DX ];
      out[ INCRNE ] = out[ XPOS ] == 1 ? 2 * ( out[ DY ] - out[ DX ] ) : 2 * ( -out[ DY ] - out[ DX ] );
      out[ DONE ] = 0; // Boolean...done plotting line?
      out[ XOUT ] = out[ INVERT ] == 0 ? out[ X ] : out[ Y ]; // Switch them if necessary
      out[ YOUT ] = out[ INVERT ] == 0 ? out[ Y ] : out[ X ]; // These are the actual output line coords.
   }
   
   static final void updateMidpointLine( int[] init ) {
      if ( init[ D ] <= 0 ) { // Update d and y if necessary
	 init[ D ] += init[ XPOS ] == 1 ? init[ INCRE ] : -init[ INCRE ];
      } else {
	 init[ D ] += init[ XPOS ] == 1 ? init[ INCRNE ] : -init[ INCRNE ];
	 init[ Y ] ++;
      }
      init[ X ] += init[ XPOS ] == 1 ? 1 : -1; // Update x
      init[ DONE ] = ( init[ XPOS ] == 1 && init[ X ] < init[ X1 ] ) || // Are we past the end of the line?
	 ( init[ XPOS ] == 0 && init[ X ] > init[ X1 ] ) ? 0 : 1;
      init[ XOUT ] = init[ INVERT ] == 0 ? init[ X ] : init[ Y ]; // Switch them if necessary
      init[ YOUT ] = init[ INVERT ] == 0 ? init[ Y ] : init[ X ]; // These are the actual output line coords.
   }

   static final void fill( int x, int y ) {
      if ( x >= 0 && x < w && y >= 0 && y < h ) pix[ x + y * w ] = rgb;
   }
}
