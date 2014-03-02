package djr.d3d;
import java.awt.*;

/**
 * Class <code>object3d</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class object3d {
   public static int STEPSIZE = 30; // Default step size for lathe
   public static int LATHEANGLE = 360; // Default for lathe...lathe 360 degrees around
   public static final String PLANE = "{{{1,1,0},{-1,1,0},{-1,-1,0},{1,-1,0}}}";
   public static final String STICKX = "{{{0,0,0},{1,0,0}}}";
   public static final String STICKY = "{{{0,0,0},{0,1,0}}}";
   public static final String STICKZ = "{{{0,0,0},{0,0,1}}}";
   public static final String POINT = "{{{0,0,0}}}";
   public static final int SPHERE = 1;
   public static final int CONE = 2;
   public static final int PRISM = 3;
   public static final int CYLINDER = 4;
   public static final int DISC = 5;
   public static final int CUBE = 6;
   public static final int PYRAMID = 7;
   public static object3d currentObject = null;
   
   public vector rot = new vector(), scale = new vector().set( 1, 1, 1 ), offset = new vector();
   public matrix mat = new matrix();
   public boolean recalculateMatrix = true;
   public int nfaces = 0, nverts = 0;
   public vertex verts[] = new vertex[ 4 ];
   public face faces[] = new face[ 5 ];
   public float color[] = { 1.0f, 1.0f, 1.0f };
   public float ambient[] = { 0.2f, 0.2f, 0.2f }, diffuse[] = { 0.6f, 0.6f, 0.6f };
   public float specular[] = { 1.0f, 1.0f, 1.0f }, spec_hilight = 10.0f;
   public int pattern[] = null, patw = 0, path = 0;
   public int drawingMode = 0;

   public object3d() {
   }

   public object3d( int type ) {
      createFrom( type );
   }

   public object3d( String str ) {
      int start = 1, end = 0, count = 0;
      while( start > -1 ) {
	 start = str.indexOf( "{{" , start + 2 );
	 if ( start == -1 ) break;
	 count ++;
      }
      start = 1;
      while( start > -1 ) {
	 start = str.indexOf( "{{" , start );
	 if ( start == -1 ) break;
	 end = str.indexOf( "}}" , start );
	 String faceString = str.substring( start, end + 2 );
	 addFace( new face( faceString ) );
	 start = end + 2;
      }
   }

   public object3d createFrom( int type ) {
      if ( type == CONE ) latheFrom( STEPSIZE, LATHEANGLE, new face( "{{1,0,1},{0,0,0}}" ) );
      else if ( type == PRISM ) extrudeFrom( new face( "{{1,0,1},{0,1,1},{0,0,1}}" ) );
      else if ( type == CYLINDER ) latheFrom( STEPSIZE, LATHEANGLE, new face( "{{1,0,1},{1,0,0}}" ) );
      else if ( type == DISC ) latheFrom( STEPSIZE, LATHEANGLE, new face( "{{1,0,0},{0,0,0}}" ) );
      else if ( type == PYRAMID ) latheFrom( 90, 4, new face( "{{1,0,1},{0,0,0}}" ) );
      else if ( type == CUBE ) extrudeFrom( new face( "{{1,1,1},{-1,1,1},{-1,-1,1},{1,-1,1}}" ) );
      else if ( type == SPHERE ) {
	 double step = STEPSIZE * matrix.RAD_PER_DEGREE;
	 face f = new face();
	 for ( double i = 0; i <= 180; i += STEPSIZE ) {
	    double d = i * matrix.RAD_PER_DEGREE;
	    f.addVertex( new vertex( Math.sin( d ), 0, Math.cos( d ) ) );
	 }
	 latheFrom( STEPSIZE, LATHEANGLE, f );
      }
      return this;
   }

   public object3d extrudeFrom( face f ) {
      return extrudeFrom( f, true );
   }

   public object3d extrudeFrom( face f, boolean faceIsFront ) {
      int EXTRUDE_DISTANCE = 2;
      if ( ! faceIsFront ) EXTRUDE_DISTANCE = -2;
      addFace( f );
      face otherSide = new face();
      vertex v1 = null, v2 = null;
      for ( int i = f.nverts-1; i >= 0; i -- ) {
	 v2 = new vertex( f.verts[i].x, f.verts[i].y, f.verts[i].z - EXTRUDE_DISTANCE );
	 if ( i < f.nverts-1 ) {
	    v1 = new vertex( f.verts[i+1].x, f.verts[i+1].y, f.verts[i+1].z - EXTRUDE_DISTANCE );
	    addFace( new face( f.verts[i+1], f.verts[i], v2, v1 ) );
	 } else {
	    v1 = new vertex( f.verts[0].x, f.verts[0].y, f.verts[0].z - EXTRUDE_DISTANCE );
	    addFace( new face( f.verts[0], f.verts[i], v2, v1 ) );
	 }
	 otherSide.addVertex( v1 );
      }
      if ( v2 != null ) otherSide.addVertex( v2 );
      addFace( otherSide );
      return this;
   }

   public object3d latheFrom( int stepsize, int latheAngle, face f ) {
      // Lathe face nsteps times around z axis, stepping stepsize degrees each step
      int nsteps = Math.abs( latheAngle / stepsize );
      face oldf = new face( f.toString() ), newf = null;
      matrix lm = new matrix();
      for ( int i = 1; i <= nsteps; i ++ ) {
	 oldf.transform( lm );
	 lm.rotate( 0, 0, stepsize );
	 f.transform( lm );
	 for ( int j = 0; j < f.nverts-1; j ++ ) {
	    newf = new face();
	    newf.addVertex( new vertex( f.verts[j+1].xt, f.verts[j+1].yt, f.verts[j+1].zt ) );
	    newf.addVertex( new vertex( f.verts[j].xt, f.verts[j].yt, f.verts[j].zt ) );
	    newf.addVertex( new vertex( oldf.verts[j].xt, oldf.verts[j].yt, oldf.verts[j].zt ) );
	    newf.addVertex( new vertex( oldf.verts[j+1].xt, oldf.verts[j+1].yt, oldf.verts[j+1].zt ) );
	    addFace( newf );
	 }
      }
      return this;
   }

   public object3d addObject( object3d o ) { // Join 2 objects together!
      o.setToTransformed();
      for( int i = 0; i < o.nfaces; i++ ) {
	 addFace( o.faces[ i ] );
      }	
      return this;
   }
   
   public object3d addFace( face f ) {
      face newfaces[] = null;
      if ( nfaces == faces.length ) {
	 newfaces = new face[ nfaces + 5 ];
	 System.arraycopy( faces, 0, newfaces, 0, nfaces );
	 faces = newfaces;
      }
      faces[ nfaces++ ] = f;
      
      for ( int i = 0; i < f.nverts; i ++ ) {
	 vertex v = f.verts[ i ];
	 vertex vv = hasVertex( v );
	 if ( vv != null ) f.verts[ i ] = vv;
	 else addVertex( v );
      }
      
      return this;
   }
   
   public object3d addVertex( vertex v ) {
      vertex newverts[] = null;
      if ( nverts == verts.length ) {
	 newverts = new vertex[ nverts + 4 ];
	 System.arraycopy( verts, 0, newverts, 0, nverts );
	 verts = newverts;
      }
      verts[ nverts++ ] = v;
      return this;
   }

   public vertex hasVertex( vertex v ) {
      for ( int i = 0; i < nverts; i ++ ) {
	 vertex vv = verts[ i ];
	 if ( Math.abs( vv.x - v.x ) < 0.0001 && Math.abs( vv.y - v.y ) < 0.0001 &&
	      Math.abs( vv.z - v.z ) < 0.0001 ) return vv;
      }
      return null;
   }

   public object3d setToTransformed() {
      transform( matrix() );
      for ( int i = 0; i < nverts; i++ ) {
	 verts[ i ].setToTransformed();
      }
      return this;
   }

   static final void setInternalColor( float arr[], float r, float g, float b ) {
      if ( arr == null ) arr = new float[ 3 ];
      arr[ 0 ] = r;
      arr[ 1 ] = g;
      arr[ 2 ] = b;
   }
   
   public object3d setColor( float r, float g, float b ) {
      setInternalColor( color, r, g, b );
      return this;
   }

   public object3d setAmbient( float r, float g, float b ) {
      setInternalColor( ambient, r, g, b );
      return this;
   }

   public object3d setDiffuse( float r, float g, float b ) {
      setInternalColor( diffuse, r, g, b );
      return this;
   }

   public object3d setSpecular( float r, float g, float b, float hilight ) {
      setInternalColor( specular, r, g, b );
      if ( hilight > 0 ) spec_hilight = hilight;
      return this;
   }

   public object3d setPattern( int pix[], int w, int h ) {
      pattern = pix;
      patw = w;
      path = h;
      return this;
   }

   public object3d setColor( float c[] ) {
      color = c;
      return this;
   }

   public object3d setAmbient( float c[] ) {
      ambient = c;
      return this;
   }

   public object3d setDiffuse( float c[] ) {
      diffuse = c;
      return this;
   }

   public object3d setSpecular( float c[], float hilight ) {
      specular = c;
      if ( hilight > 0 ) spec_hilight = hilight;
      return this;
   }

   public static void setInternalColor( float arr[], Color c ) {
      if ( arr == null ) arr = new float[ 3 ];
      int rgb = c.getRGB();
      arr[0] = (float) ( ( rgb >> 16 ) & 0xff ) / 255.0f;
      arr[1] = (float) ( ( rgb >> 8  ) & 0xff ) / 255.0f;
      arr[2] = (float) ( ( rgb       ) & 0xff ) / 255.0f;
   }
   
   public object3d setColor( Color c ) {
      setInternalColor( color, c );
      return this;
   }

   public object3d setAmbient( Color c ) {
      setInternalColor( ambient, c );
      return this;
   }

   public object3d setDiffuse( Color c ) {
      setInternalColor( diffuse, c );
      return this;
   }

   public object3d setSpecular( Color c, float hilight ) {
      setInternalColor( specular, c );
      if ( hilight > 0 ) spec_hilight = hilight;
      return this;
   }

   public Color getColor() {
      return new Color( color[0], color[1], color[2] );
   }

   public Color getAmbient() {
      return new Color( ambient[0], ambient[1], ambient[2] );
   }

   public Color getDiffuse() {
      return new Color( diffuse[0], diffuse[1], diffuse[2] );
   }

   public Color getSpecular() {
      return new Color( specular[0], specular[1], specular[2] );
   }

   public object3d setDrawingMode( int mode ) {
      drawingMode = mode;
      return this;
   }

   public int drawingMode() {
      if ( ( drawingMode & face.OVERRIDE ) != 0 || world3d.currentWorld == null || ( drawingMode != 0 && 
	   ( world3d.currentWorld.drawingMode & face.OVERRIDE ) == 0 ) ) return drawingMode;
      return world3d.currentWorld.drawingMode;
   }
   
   public void draw( light lights[], int nlights, Graphics g, int pix[], int w, int h ) {
      currentObject = this;
      if ( ( drawingMode() & face.SORTED ) != 0 ) sortFaces(); // Sort faces if we're not culling
      face.drawingMode = drawingMode();
      face.color = color;
      face.ambient = ambient;
      face.diffuse = diffuse;
      face.specular = specular;
      face.spec_hilight = spec_hilight;
      if ( ( drawingMode() & face.PATTERN_MAPPING ) != 0 ) {
	 face.pattern = pattern;
	 face.patw = patw;
	 face.path = path;
      } else {
	 face.pattern = null;
      }
      for ( int i = 0; i < nfaces; i++ ) {
	 if ( faces[ i ] != null ) faces[ i ].draw( lights, nlights, g, pix, w, h );
      }
   }

   public void transform( matrix m ) {
      currentObject = this;
      for ( int i = 0; i < nverts; i++ ) verts[ i ].transform( m );
   }
   
   public void project( matrix m ) {
      currentObject = this;
      if ( recalculateMatrix ) {
	 if ( ( drawingMode() & face.SHADED ) != 0 ) { // Only need to compute normals if we're shading
	    for ( int i = 0; i < nfaces; i ++ ) {
	       faces[ i ].recalculateNormal = true;
	    }
	 }
	 transform( matrix() ); // Re-transform if the matrix has changed
      }
      for ( int i = 0; i < nverts; i++ ) verts[ i ].project( m );
   }

   public void sortFaces() {
      quickSort( faces, 0, nfaces-1, false ); // Sort faces in ascending z-order
   }
   
   public matrix matrix() {
      if ( ! recalculateMatrix ) return mat;
      mat.normalize();
      mat.scale( scale.i, scale.j, scale.k );
      mat.rotate( rot.i, rot.j, rot.k );
      mat.offset( offset.i, offset.j, offset.k );
      recalculateMatrix = false;
      return mat;
   }

   public object3d rotate( double rx, double ry, double rz ) {
      if ( rx == 0 && ry == 0 && rz == 0 ) return this;
      rot.plus( rx, ry, rz );
      recalculateMatrix = true;
      return this;
   }
   
   public object3d offset( double dx, double dy, double dz ) {
      if ( dx == 0 && dy == 0 && dz == 0 ) return this;
      offset.plus( dx, dy, dz );
      recalculateMatrix = true;
      return this;
   }
   
   public object3d scale( double x, double y, double z ) {
      if ( x == 1 && y == 1 && z == 1 ) return this;
      scale.times( x, y, z );
      recalculateMatrix = true;
      return this;
   }

   public object3d scale( double s ) {
      return scale( s, s, s );
   }

   public boolean containsPoint( int x, int y ) {
      for ( int i = 0; i < nfaces; i++ ) {
	 if ( faces[ i ].containsPoint( x, y ) ) return true;
      }
      return false;
   }
   
   public boolean visible() { // All vertices are in front of (z > z of) the camera
      for ( int i = 0; i < nfaces; i++ ) {
	 if ( faces[ i ].visible() ) return true;
      }
      return false;
   }

   public vertex centerOfMass() { // Simple CofM calculation
      vertex v = new vertex();
      for ( int i = 0; i < nverts; i ++ ) {
	 vertex vv = verts[ i ];
	 v.x += vv.x; v.y += vv.y; v.z += vv.z;
	 v.xt += vv.xt; v.yt += vv.yt; v.zt += vv.zt; // Also get transformed center.
	 v.xp += vv.xp; v.yp += vv.yp; v.zp += vv.zp; // Also get projected center.
      }
      double d = (double) nverts;
      v.x /= d; v.y /= d; v.z /= d;
      v.xt /= d; v.yt /= d; v.zt /= d;
      v.xp /= d; v.yp /= d; v.zp /= d;
      return v;
   }

   public void bounds( vertex min, vertex max ) {
      min.x = min.y = min.z = 999999;
      max.x = max.y = max.z = -999999;
      for ( int i = 0; i < nverts; i ++ ) {
	 vertex v = verts[ i ];
	 min.x = Math.min( v.x, min.x ); max.x = Math.max( v.x, max.x ); // Do untransformed bounds,
	 min.y = Math.min( v.y, min.y ); max.y = Math.max( v.y, max.y );
	 min.z = Math.min( v.z, min.z ); max.z = Math.max( v.z, max.z );
	 min.xt = Math.min( v.xt, min.xt ); max.xt = Math.max( v.xt, max.xt ); // transformed bounds,
	 min.yt = Math.min( v.yt, min.yt ); max.yt = Math.max( v.yt, max.yt );
	 min.zt = Math.min( v.zt, min.zt ); max.zt = Math.max( v.zt, max.zt );
	 min.xp = Math.min( v.xp, min.xp ); max.xp = Math.max( v.xp, max.xp ); // and projected bounds
	 min.yp = Math.min( v.yp, min.yp ); max.yp = Math.max( v.yp, max.yp );
	 min.zp = Math.min( v.zp, min.zp ); max.zp = Math.max( v.zp, max.zp );
      }
   }

   public double getZdist() { // Get distance from camera (assumings the vertices have been projected)
      double out = 0;
      for ( int i = 0; i < nverts; i ++ ) {
	 out += verts[ i ].wp;
      }
      out /= (double) nverts;
      return out;
   }      

   public String toString() {
      String out = "{";
      for ( int i = 0; i < nfaces; i++ ) {
	 out += faces[ i ].toString();
	 if ( i < nfaces - 1 ) out += ",";
      }
      out += "}";
      return out;
   }

   private static void quickSort( face array[], int left, int right, boolean ascending ) {
      if ( array.length <= 1 ) return;
      int i = left, j = right;
      face pivot = array[(left + right) / 2];
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
	    face tmp = array[i];
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
