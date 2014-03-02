package djr.d3d;

/**
 * Class <code>vertex</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class vertex {	
   public double x = 0, y = 0, z = 0; // Actual coords
   public double xt = 0, yt = 0, zt = 0; // Real-world, 3d, transformed coords
   public double xp = 0, yp = 0, zp = 0, wp = 0;
   public boolean visible = false;
   
   public face faces[] = null; // All these are really needed only for gourad or phong shading
   public int nfaces = 0;
   public vector normal = null;
   public boolean recalculateNormal = true;

   public double map[] = null; // pixel X, Y coords for mapping onto a pattern (0->1 only!)

   public vertex() {
   }

   public vertex( double x, double y, double z ) {
      set( x, y, z );
   }
   
   public vertex( String str ) {
      int comma1 = str.indexOf( "," );
      int comma2 = str.indexOf( ",", comma1 + 1 );
      x = ( Double.valueOf( str.substring( str.indexOf( "{" ) + 1, comma1 ) ) ).doubleValue();
      y = ( Double.valueOf( str.substring( comma1 + 1, comma2 ) ) ).doubleValue();
      z = ( Double.valueOf( str.substring( comma2 + 1, str.indexOf( "}" ) ) ) ).doubleValue();
   }   	
	
   public vertex set( double x, double y, double z ) {
      this.x = x;
      this.y = y;
      this.z = z;
      return this;
   }
	
   public vertex set( vertex v ) {
      return set( v.x, v.y, v.z );
   }
	
   public vertex set( vector v ) {
      return set( v.i, v.j, v.k );
   }

   public vertex addFace( face f ) { // Keep track of adjacent faces for computing the normal.
      if ( f == null ) return this;
      if ( nfaces > 0 ) {
	 for ( int i = 0; i < nfaces; i ++ ) { // Make sure we dont already list this face.
	    if ( faces[ i ] == f ) return this;
	 }
	 face newfaces[] = null;
	 if ( nfaces == faces.length ) {
	    newfaces = new face[ nfaces + 4 ];
	    System.arraycopy( faces, 0, newfaces, 0, nfaces );
	    faces = newfaces;
	 }
      } else faces = new face[ 4 ];
      faces[ nfaces++ ] = f;
      return this;
   }

   public vector normal() {
      if ( recalculateNormal = false ) return normal;
      if ( normal == null ) normal = new vector();
      normal.set( 0, 0, 0 );
      if ( nfaces == 0 ) return normal;
      for ( int i = 0; i < nfaces; i ++ ) {
	 normal.plus( faces[ i ].normal() );
      }
      recalculateNormal = false;
      return normal.set( normal.i/nfaces, normal.j/nfaces, normal.k/nfaces );
   }
   	
   public void transform( matrix m ) {
      xt = m.M[0] * x + m.M[1] * y + m.M[2] * z + m.M[3];
      yt = m.M[4] * x + m.M[5] * y + m.M[6] * z + m.M[7];
      zt = m.M[8] * x + m.M[9] * y + m.M[10] * z + m.M[11];
      recalculateNormal = true;
   }
   
   public void project( matrix m ) {
      wp = m.M[12] * xt + m.M[13] * yt + m.M[14] * zt + m.M[15];
      visible = ( wp > 0 );
      if ( ! visible ) return; // Dont do rest of transform if its not visible.
      xp = ( m.M[0] * xt + m.M[1] * yt + m.M[2] * zt + m.M[3] ) / wp;
      yp = ( m.M[4] * xt + m.M[5] * yt + m.M[6] * zt + m.M[7] ) / wp;
      //zp = m.M[8] * xt + m.M[9] * yt + m.M[10] * zt + m.M[11]; // Gives distance from camera (given by m)
      recalculateNormal = true;
   }

   public void inverseProject( matrix m ) {
      if ( ! visible ) return; // We didn't project it in the first place.
      xp = m.M[0] * xp * wp + m.M[1] * yp * wp + m.M[2] * zp + m.M[3];
      yp = m.M[4] * xp * wp + m.M[5] * yp * wp + m.M[6] * zp + m.M[7];
      zp = m.M[8] * xp * wp + m.M[9] * yp * wp + m.M[10] * zp + m.M[11];
   }

   public void setToTransformed() {
      x = xt;
      y = yt;
      z = zt;
   }
   
   public String toString() {
      return "{" + x + "," + y + "," + z + "}";
   }

   public String toTransformedString() {
      return "{" + xt + "," + yt + "," + zt + "}";
   }

   public String toProjectedString() {
      return "{" + xp + "," + yp + "," + wp + "}";
   }
}
