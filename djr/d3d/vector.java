package djr.d3d;

/**
 * Class <code>vector</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class vector { // SMALL V VECTOR
   public static vector temp = new vector();
   public double i = 0, j = 0, k = 0;

   public vector() {
   }

   public vector( double i, double j, double k ) {
      set( i, j, k );
   }

   public vector( String str ) {
      int comma1 = str.indexOf( "," );
      int comma2 = str.indexOf( ",", comma1 + 1 );
      i = ( Double.valueOf( str.substring( str.indexOf( "{" ) + 1, comma1 ) ) ).doubleValue();
      j = ( Double.valueOf( str.substring( comma1 + 1, comma2 ) ) ).doubleValue();
      k = ( Double.valueOf( str.substring( comma2 + 1, str.indexOf( "}" ) ) ) ).doubleValue();
   }
   
   public vector( vertex vert1, vertex vert2 ) {
      set( vert1, vert2 );
   }

   public vector set( double i, double j, double k ) {
      this.i = i;
      this.j = j;
      this.k = k;
      return this;
   }

   public vector set( vertex vert1, vertex vert2 ) {
      return set( vert2.xt - vert1.xt, vert2.yt - vert1.yt, vert2.zt - vert1.zt );
   }
   
   public vector set( vertex v ) {
      return set( v.x, v.y, v.z );
   }
   
   public vector set( vector v ) {
      return set( v.i, v.j, v.k );
   }
   
   public vector plus( vector v ) {
      return set( i+v.i, j+v.j, k+v.k );
   }
   
   public vector plus( double ii, double jj, double kk ) {
      return set( i+ii, j+jj, k+kk );
   }
   
   public vector times( vector v ) {
      return set( i*v.i, j*v.j, k*v.k );
   }
   
   public vector times( double ii, double jj, double kk ) { // As if by a diagonal matrix
      return set( i*ii, j*jj, k*kk );
   }
   
   public double dot( vector v1 ) {
      return v1.i * i + v1.j * j + v1.k * k;
   }

   public double dot( vertex v1 ) {
      return v1.xt * i + v1.yt * j + v1.zt * k;
   }

   public vector cross( vector v1 ) {		
      temp.i = j*v1.k - k*v1.j;
      temp.j = k*v1.i - i*v1.k;
      temp.k = i*v1.j - j*v1.i;
      return set( temp.i, temp.j, temp.k );
   }
   
   public vector normalize() {
      double t = length();
      return set( i / t, j / t, k / t );
   }

   public double length() {
      return Math.sqrt( i*i + j*j + k*k );
   }

   public String toString() {
      return "{" + i + "," + j + "," + k + "}";
   }
}
