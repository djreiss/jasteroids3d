package djr.d3d;
import java.awt.* ;

/**
 * Class <code>world3d</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class world3d {
   public static matrix tempm1 = new matrix();
   public static vector tempv = new vector();
   public static world3d currentWorld = null;
   public object3d objects[] = new object3d[ 5 ];
   public int nobjects = 0;
   public light lights[] = new light[ 2 ];
   public int nlights = 0;
   public Color background = Color.black;
   public int center[] = { 0, 0 }; // Offset from center of screen that drawing occurs...this is center
   public vector pan = new vector(), offset = new vector(), worldRotate = new vector();
   public double tilt = 0; // Rotation (clockwise) of projection onto screen
   public matrix mat = new matrix();
   public boolean recomputeMatrix = true;
   public int x, y, width, height, drawingMode = face.SHADED | face.FLAT | face.BACKFACE_CULLING;
   public vector u = new vector(), v = new vector(), w = new vector(); // Defines frame of camera
   public vertex lookat = new vertex( 0, 0, -1 );
   public double far = 1000, near = 1.0, fov = 45;
   public float atmos_params[][] = null; // Parameters for atmospheric attenuation, if desired

   public world3d( int x, int y, int width, int height ) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public Rectangle bounds() {
      return new Rectangle( x, y, width, height );
   }

   public void setBounds( Rectangle b ) {
      x = b.x;
      y = b.y;
      width = b.width;
      height = b.height;
   }

   public object3d addObject( object3d o ) {
      object3d newobjects[] = null;
      if ( nobjects == objects.length ) {
	 newobjects = new object3d[ nobjects + 5 ];
	 System.arraycopy( objects, 0, newobjects, 0, nobjects );
	 objects = newobjects;
      }
      objects[ nobjects ++ ] = o;
      return o;
   }

   public void removeObject( object3d obj ) {
      for ( int i = 0; i < nobjects; i ++ ) {
	 if ( objects[ i ] == obj ) {
	    objects[ i ] = null;
	    int j = objects.length - i - 1;
	    if ( j > 0 ) {
	       nobjects --;
	       System.arraycopy( objects, i+1, objects, i, j );
	    }
	    return;
	 }
      }
   }

   public light addLight( light l ) {
      light newlights[] = null;
      if ( nlights == lights.length ) {
	 newlights = new light[ nlights + 2 ];
	 System.arraycopy( lights, 0, newlights, 0, nlights );
	 lights = newlights;
      }
      lights[ nlights ++ ] = l;
      addObject( l );
      return l;
   }

   public void removeLight( light l ) {
      for ( int i = 0; i < nlights; i ++ ) {
	 if ( lights[ i ] == l ) {
	    lights[ i ] = null;
	    int j = lights.length - i - 1;
	    if ( j > 0 ) {
	       nlights --;
	       System.arraycopy( lights, i+1, lights, i, j );
	    }
	    removeObject( l );
	    return;
	 }
      }
   }

   public void draw( int pix[], int w, int h ) {
      draw( null, pix, w, h );
   }

   public void draw( Graphics g ) {
      draw( g, null, 0, 0 );
   }

   public void draw( Graphics g, int pix[], int w, int h ) { // One or the other will be null.
      currentWorld = this;
      if ( g != null ) {
	 g.setColor( background );
	 g.fillRect( 0, 0, width, height );
      } else {
	 int count = w * h;
	 int rgb = background.getRGB();
	 pix[ 0 ] = pix[ 1 ] = rgb;
	 int ind = 1;
	 while( ind < count/2 ) {
	    ind *= 2;
	    if ( ind * 2 < count-1 ) System.arraycopy( pix, 0, pix, ind, ind );
	    else System.arraycopy( pix, 0, pix, ind, count-ind );
	 }
      }
      
      matrix myMat = matrix();
      for ( int i = 0; i < nobjects; i++ ) { // Now send in the projection matrix for drawing
	 if ( objects[ i ] != null ) objects[ i ].project( myMat ); 
      }

      // Need to do this first so we can sort by distance from camera:
      quickSort( objects, 0, nobjects-1, false ); // Sort objects in ascending-z order

      for ( int i = 0; i < nobjects; i++ ) {
	 if ( objects[ i ] != null ) objects[ i ].draw( lights, nlights, g, pix, w, h );
      }
   }
   
   public void drawObject( object3d obj, Graphics g, int pix[], int w, int h ) { // Allow drawing of an object not
      currentWorld = this;
      matrix myMat = matrix(); // already in this world.
      obj.project( myMat ); 
      obj.draw( lights, nlights, g, pix, w, h );
   }

   public void setDefaultAttenuation( float worldDepth, float r, float g, float b ) {
      setAttenuation( 0, r, 1.0f, worldDepth, 1.0f, 0.1f );
      setAttenuation( 1, g, 1.0f, worldDepth, 1.0f, 0.1f );
      setAttenuation( 2, b, 1.0f, worldDepth, 1.0f, 0.1f );
   }

   public void setAttenuation( int color, float I_dc, float zf, float zb, float sf, float sb ) {
      if ( atmos_params == null ) atmos_params = new float[3][6];
      float a[][] = atmos_params;
      a[color][0] = I_dc;
      a[color][1] = zf; // First index for atmos_params is for each color (r,b,g) second index gives:
      a[color][2] = zb; // See graphics book, p. 728 for what these params mean.
      a[color][3] = sf; // Good defaults are 0.3, 1.0, 0.0, 1.0, 0.1 ... but set zf to depth of world
      a[color][4] = sb; // Equ. for s0 = sb + ( dist - zb ) * ( sf - sb ) / ( zf - zb )
      a[color][5] = ( sf - sb ) / ( zf - zb );
   }

   public void attenuateColor( float inc[], vertex v ) {
      // Atmospheric attenuation of object: input color and location of object in transformed space
      if ( atmos_params == null ) return; // No attenuation desired
      float a[][] = atmos_params;
      float dist = (float) tempv.set( v.xt - offset.i, v.yt - offset.j, v.zt - offset.k ).length();
      float s0 = 0.0f;
      for ( int i = 0; i < 3; i ++ ) {
	 if ( dist < a[i][1] ) s0 = a[i][3];
	 else if ( dist > a[i][2] ) s0 = a[i][4];
	 else s0 = a[i][4] + ( dist - a[i][2] ) * a[i][5];
	 inc[i] = s0 * inc[i] + ( 1.0f - s0 ) * a[i][0];
      }
   }

   public void pan( double px, double py, double pz ) { // Pan the camera by this angle
      pan.plus( px, py, pz );
      recomputeMatrix = true;
   }
   	
   public void rotateWorld( double px, double py, double pz ) { // Rotate the world's objects about (0,0,0)
      worldRotate.plus( px, py, pz );
      recomputeMatrix = true;
   }
   	
   public void offset( double dx, double dy, double dz ) { // Move the CAMERA by this much.
      offset.plus( dx, dy, dz );
      recomputeMatrix = true;
   }

   public void offsetTo( double x, double y, double z ) {
      offset.set( x, y, z );
      recomputeMatrix = true;      
   }

   public void tilt( double t ) { // Rotate camera about projection axis
      tilt += t;
      recomputeMatrix = true;
   }
   
   public matrix matrix() {
      if ( ! recomputeMatrix ) return mat;
      mat.normalize();      
      mat.rotate( pan.i, pan.j, pan.k );
      lookat.transform( mat );
      mat.normalize();
      mat.rotate( worldRotate.i, worldRotate.j, worldRotate.k ); // Rotate the world if desired

      w.set( -lookat.xt, -lookat.yt, -lookat.zt ).normalize(); // Direction opposite of that towards lookat
      u.set( 0, 1, 0 ).cross( w ).normalize();
      v.set( w ).cross( u ).normalize();
      tempm1.normalize();
      tempm1.M[ 0 ] = u.i; //u.dot( xhat );
      tempm1.M[ 4 ] = v.i; //v.dot( xhat );
      tempm1.M[ 8 ] = w.i; //w.dot( xhat );
      tempm1.M[ 1 ] = u.j; //u.dot( yhat );
      tempm1.M[ 5 ] = v.j; //v.dot( yhat );
      tempm1.M[ 9 ] = w.j; //w.dot( yhat );
      tempm1.M[ 2 ] = u.k; //u.dot( zhat );
      tempm1.M[ 6 ] = v.k; //v.dot( zhat );
      tempm1.M[ 10 ] = w.k; //w.dot( zhat );
      tempv.set( -offset.i, -offset.j, -offset.k ); // "t" vector in notes
      tempm1.M[ 3 ] = u.dot( tempv );
      tempm1.M[ 7 ] = v.dot( tempv );
      tempm1.M[ 11 ] = w.dot( tempv );
      mat.times( tempm1 );

      tempm1.normalize(); // Compute projection matrix
      tempm1.M[ 0 ] = tempm1.M[ 5 ] = 1.0 / Math.tan( fov * Math.PI / 360.0 ); // cot( fov/2 )
      tempm1.M[ 10 ] = ( far + near ) / ( far - near );
      tempm1.M[ 14 ] = ( 2 * far * near ) / ( far - near );
      tempm1.M[ 11 ] = -1;
      tempm1.M[ 15 ] = 0;
      mat.times( tempm1 );

      // At this point we really want to do clipping on the objects into the unit volume -1 <= u,v,w <= 1

      tempm1.normalize();
      tempm1.offset( 1, 1, 1 ); // Translate to center of canonical view volume
      tempm1.scale( width/2, height/2, far-near ); // Scale to size of 3D viewport
      tempm1.offset( x+width/2+center[0], y+width/2+center[1], near ); // Move into correct location on window
      mat.times( tempm1 );
      
      mat.rotate( 0, 0, tilt );
      recomputeMatrix = false;
      return mat;
   }
   
   public object3d shapeClicked( int x, int y ) {
      x = x - ( this.x + width/2 + center[0] );
      y = y - ( this.y + height/2 + center[1] );
      for ( int i = 0; i < nobjects; i++ ) {
	 if ( objects[ i ] != null && objects[ i ].containsPoint( x, y ) ) return objects[ i ];
      }
      return null;
   }

   private static void quickSort( object3d array[], int left, int right, boolean ascending ) {
      if ( array[left] == null || array[left] == null ) return;
      if ( array.length <= 1 ) return;
      int i = left, j = right;
      object3d pivot = array[(left + right) / 2];
      if ( pivot == null ) return;
      double zdist = pivot.getZdist();      
      do {
	 if ( ascending ) {
	    while ( i < right && ( array[i] == null || zdist > array[i].getZdist() ) ) i++;
	    while ( j > left && ( array[j] == null || zdist < array[j].getZdist() ) ) j--;
	 } else {
	    while ( i < right && ( array[i] == null || zdist < array[i].getZdist() ) ) i++;
	    while ( j > left && ( array[j] == null || zdist > array[j].getZdist() ) ) j--;
	 }
	 if ( i < j ) {
	    object3d tmp = array[i];
	    array[i] = array[j];
	    array[j] = tmp;
	 }
	 if ( i <= j ) {
	    i++;
	    j--;
	 }
      } while ( i <= j );
      if ( left < j ) quickSort( array, left, j, ascending ); 
      if ( i < right ) quickSort( array, i, right, ascending );
   }
}
