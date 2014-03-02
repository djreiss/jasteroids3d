package djr.d3d;

/**
 * Class <code>matrix</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class matrix {
   public static final double RAD_PER_DEGREE = Math.PI / 180.0;
   static matrix tempm = new matrix(), rotTempm = new matrix();
   public double M[] = new double[ 16 ];

   public matrix() {
      normalize();
   }
   
   public matrix normalize() {
      M[0] = M[5] = M[10] = M[15] = 1.0;
      M[1] = M[2] = M[3] = M[4] = M[6] = M[7] = M[8] = M[9] = M[11] = M[12] = M[13] = M[14] = 0.0;
      return this;
   }
   
   public matrix copyFrom( matrix m ) {
      System.arraycopy( m.M, 0, this.M, 0, 16 );
      return this;
   }

   public matrix times( matrix m ) {
      double acu;
      int i, j;
      for ( i = 0; i < 4; i++ ) {
	 for ( j = 0; j < 4; j++ ) {
	    acu = 0;
	    for ( int k = 0; k < 4; k++ ) acu += m.M[ 4 * i + k ] * M[ 4 * k + j ];
	    tempm.M[ 4 * i + j ] = acu;
	 }
      }
      return copyFrom( tempm );
   }
   
   public matrix mult( matrix m1, matrix m2 ) {
      return copyFrom( m1 ).times( m2 );
   }
      
   public matrix offset( double dx, double dy, double dz ) {
      M[3] += dx;
      M[7] += dy;
      M[11] += dz;
      return this;
   }
   
   public matrix scale( double sx, double sy, double sz ) {
      M[0] *= sx;
      M[5] *= sy;
      M[10] *= sz;
      return this;
   }
   
   public matrix rotate( double rx, double ry, double rz ) {
      rotX( rx );
      rotY( ry );
      rotZ( rz );
      return this;
   }
   
   public String toString() {
      return "[" + M[0] + "," + M[1] + "," + M[2] + "," + M[3] + "]\n[" + M[4] + "," +
      M[5] + "," + M[6] + "," + M[7] + "]\n[" + M[8] + "," + M[9] + "," + M[10] + "," + 
      M[11] + "]\n[" + M[12] + "," + M[13] + "," + M[14] + "," + M[15] + "]";
   }

   void rotX( double r ) {
      if ( r == 0 ) return;
      rotTempm.normalize();
      r *= RAD_PER_DEGREE;
      double c = Math.cos( r );
      double s = Math.sin( r );
      rotTempm.M[ 5 ] = rotTempm.M[ 10 ] = c;
      rotTempm.M[ 6 ] = -s;
      rotTempm.M[ 9 ] = s;
      times( rotTempm );
   }	

   void rotY( double r ) {
      if ( r == 0 ) return;
      rotTempm.normalize();
      r *= RAD_PER_DEGREE;
      double c = Math.cos( r );
      double s = Math.sin( r );
      rotTempm.M[ 0 ] = rotTempm.M[ 10 ] = c;
      rotTempm.M[ 2 ] = s;
      rotTempm.M[ 8 ] = -s;
      times( rotTempm );
   }	

   void rotZ( double r ) {
      if ( r == 0 ) return;
      rotTempm.normalize();
      r *= RAD_PER_DEGREE;
      double c = Math.cos( r );
      double s = Math.sin( r );
      rotTempm.M[ 0 ] = rotTempm.M[ 5 ] = c;
      rotTempm.M[ 1 ] = -s;
      rotTempm.M[ 4 ] = s;
      times( rotTempm );
   }

   /*public double determinant() { // Computed in mathematica
     return M[1]*M[11]*M[14]*M[4] - M[1]*M[10]*M[15]*M[4] - M[11]*M[13]*M[2]*M[4] + M[10]*M[13]*M[3]*M[4] - M[0]*M[11]*M[14]*M[5] + M[0]*M[10]*M[15]*M[5] + 
     M[11]*M[12]*M[2]*M[5] - M[10]*M[12]*M[3]*M[5] - M[1]*M[11]*M[12]*M[6] + M[0]*M[11]*M[13]*M[6] + M[1]*M[10]*M[12]*M[7] - M[0]*M[10]*M[13]*M[7] - 
     M[15]*M[2]*M[5]*M[8] + M[14]*M[3]*M[5]*M[8] + M[1]*M[15]*M[6]*M[8] - M[13]*M[3]*M[6]*M[8] - M[1]*M[14]*M[7]*M[8] + M[13]*M[2]*M[7]*M[8] + 
     M[15]*M[2]*M[4]*M[9] - M[14]*M[3]*M[4]*M[9] - M[0]*M[15]*M[6]*M[9] + M[12]*M[3]*M[6]*M[9] + M[0]*M[14]*M[7]*M[9] - M[12]*M[2]*M[7]*M[9];
   }
   
   public matrix inverse() { // Computed in mathematica
      double det = determinant();
      tempm.M[0] = (-(M[11]*M[14]*M[5]) + M[10]*M[15]*M[5] + M[11]*M[13]*M[6] - M[10]*M[13]*M[7] - M[15]*M[6]*M[9] + M[14]*M[7]*M[9])/det;
      tempm.M[1] = (M[1]*M[11]*M[14] - M[1]*M[10]*M[15] - M[11]*M[13]*M[2] + M[10]*M[13]*M[3] + M[15]*M[2]*M[9] - M[14]*M[3]*M[9])/det;
   	tempm.M[2] = (-(M[15]*M[2]*M[5]) + M[14]*M[3]*M[5] + M[1]*M[15]*M[6] - M[13]*M[3]*M[6] - M[1]*M[14]*M[7] + M[13]*M[2]*M[7])/det;
   	tempm.M[3] = (M[11]*M[2]*M[5] - M[10]*M[3]*M[5] - M[1]*M[11]*M[6] + M[1]*M[10]*M[7] + M[3]*M[6]*M[9] - M[2]*M[7]*M[9])/det;
   	tempm.M[4] = (M[11]*M[14]*M[4] - M[10]*M[15]*M[4] - M[11]*M[12]*M[6] + M[10]*M[12]*M[7] + M[15]*M[6]*M[8] - M[14]*M[7]*M[8])/det;
   	tempm.M[5] = (-(M[0]*M[11]*M[14]) + M[0]*M[10]*M[15] + M[11]*M[12]*M[2] - M[10]*M[12]*M[3] - M[15]*M[2]*M[8] + M[14]*M[3]*M[8])/det;
   	tempm.M[6] = (M[15]*M[2]*M[4] - M[14]*M[3]*M[4] - M[0]*M[15]*M[6] + M[12]*M[3]*M[6] + M[0]*M[14]*M[7] - M[12]*M[2]*M[7])/det;
   	tempm.M[7] = (-(M[11]*M[2]*M[4]) + M[10]*M[3]*M[4] + M[0]*M[11]*M[6] - M[0]*M[10]*M[7] - M[3]*M[6]*M[8] + M[2]*M[7]*M[8])/det;
   	tempm.M[8] = (-(M[11]*M[13]*M[4]) + M[11]*M[12]*M[5] - M[15]*M[5]*M[8] + M[13]*M[7]*M[8] + M[15]*M[4]*M[9] - M[12]*M[7]*M[9])/det;
   	tempm.M[9] = (-(M[1]*M[11]*M[12]) + M[0]*M[11]*M[13] + M[1]*M[15]*M[8] - M[13]*M[3]*M[8] - M[0]*M[15]*M[9] + M[12]*M[3]*M[9])/det;
   	tempm.M[10] = (-(M[1]*M[15]*M[4]) + M[13]*M[3]*M[4] + M[0]*M[15]*M[5] - M[12]*M[3]*M[5] + M[1]*M[12]*M[7] - M[0]*M[13]*M[7])/det;
   	tempm.M[11] = (M[1]*M[11]*M[4] - M[0]*M[11]*M[5] + M[3]*M[5]*M[8] - M[1]*M[7]*M[8] - M[3]*M[4]*M[9] + M[0]*M[7]*M[9])/det;
   	tempm.M[12] = (M[10]*M[13]*M[4] - M[10]*M[12]*M[5] + M[14]*M[5]*M[8] - M[13]*M[6]*M[8] - M[14]*M[4]*M[9] + M[12]*M[6]*M[9])/det;
   	tempm.M[13] = (M[1]*M[10]*M[12] - M[0]*M[10]*M[13] - M[1]*M[14]*M[8] + M[13]*M[2]*M[8] + M[0]*M[14]*M[9] - M[12]*M[2]*M[9])/det;
   	tempm.M[14] = (M[1]*M[14]*M[4] - M[13]*M[2]*M[4] - M[0]*M[14]*M[5] + M[12]*M[2]*M[5] - M[1]*M[12]*M[6] + M[0]*M[13]*M[6])/det;
   	tempm.M[15] = (-(M[1]*M[10]*M[4]) + M[0]*M[10]*M[5] - M[2]*M[5]*M[8] + M[1]*M[6]*M[8] + M[2]*M[4]*M[9] - M[0]*M[6]*M[9])/det;
   	return tempm;
   }

   public matrix invert() {
      return copyFrom( inverse() );
   }*/
}
