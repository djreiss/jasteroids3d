package djr.d3d;
import java.awt.*;

/**
 * Class <code>face</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class face {
   public static final int WIREFRAME = 0x0001;
   public static final int SHADED = 0x0002;
   public static final int FLAT = 0x0004;
   public static final int GOURAD = 0x0008;
   public static final int PHONG = 0x0010;
   public static final int BACKFACE_CULLING = 0x0020;
   public static final int PATTERN_MAPPING = 0x0040;
   public static final int SORTED = 0x0080;
   public static final int OVERRIDE = 0x0100; // World or object has final say in drawing mode
   public static final int MAXVERTS = 500; // Maximum number of vertices allowed per face.
   
   public static int drawingMode = 0;
   public static int xpoly[] = new int[ MAXVERTS ], ypoly[] = new int[ MAXVERTS ];
   public static Polygon poly = new Polygon();
   public static vector temp1 = new vector(), temp2 = new vector();
   public static float grayshade[] = { 0, 0, 0 };
   public static int vertcolors[] = new int[ MAXVERTS ];
   public static float color[] = null, ambient[] = null, diffuse[] = null;
   public static float specular[] = null, spec_hilight = 0;
   public static int pattern[] = null, patw = 0, path = 0;
   public static double xmap[] = new double[ MAXVERTS ], ymap[] = new double[ MAXVERTS ];

   public int nverts = 0;
   public vertex verts[] = new vertex[ 4 ];
   public vector normal = new vector(), pnormal = new vector();
   public boolean recalculateNormal = true;
   
   public face() {
   }

   public face( vertex v1, vertex v2, vertex v3 ) {
      addVertex( v1 ).addVertex( v2 ).addVertex( v3 );
   }
	
   public face( vertex v1, vertex v2, vertex v3, vertex v4 ) {
      addVertex( v1 ).addVertex( v2 ).addVertex( v3 ).addVertex( v4 );
   }
	
   public face( String str ) {
      int start = 0, end = 0, count = 0;
      while( start > -1 ) {
	 start = str.indexOf( "{", start + 1 );
	 if ( start == -1 ) break;
	 count ++;
      }
      start = 0;
      while( start > -1 ) {
	 start = str.indexOf( "{", start + 1 );
	 if ( start == -1 ) break;
	 end = str.indexOf( "}" , start );
	 String vertString = str.substring( start, end + 1 );
	 addVertex( new vertex( vertString ) );
	 start = end + 1;
      }
   }

   public face addVertex( vertex v ) {
      if ( v == null ) return this;
      if ( hasVertex( v ) != null ) return this;
      vertex newverts[] = null;
      if ( nverts == verts.length ) {
	 newverts = new vertex[ nverts + 4 ];
	 System.arraycopy( verts, 0, newverts, 0, nverts );
	 verts = newverts;
      }
      verts[ nverts++ ] = v;
      recalculateNormal = true;
      v.addFace( this ); // Really only need to do this for gourad and phong shading.
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
   
   public vector normal() {
      if ( recalculateNormal == false ) return normal;
      if ( nverts < 3 ) normal.set( 0, 0, 0 );
      else normal.set( verts[1], verts[2] ).cross( temp2.set( verts[0], verts[1] ) );
      recalculateNormal = false;
      return normal.normalize();
   }
   
   public Polygon toPoly() { // Set polygon for drawing
      if ( nverts <= 2 ) return null;
      for ( int i = 0; i < nverts; i ++ ) {
	 vertex v = verts[ i ];
	 //if ( ! v.visible ) continue; 
	 xpoly[ i ] = (int) v.xp;
	 ypoly[ i ] = (int) v.yp;
      }
      poly.xpoints = xpoly;
      poly.ypoints = ypoly;
      poly.npoints = nverts;
      return poly;
   }
	
   public void draw( light lights[], int nlights, Graphics g, int pix[], int w, int h ) {
      if ( ! shade( lights, nlights ) ) return;
      
      Color c = null;
      if ( ( drawingMode & SHADED ) == 0 ) c = new Color( color[0], color[1], color[2] );
      else if ( ( drawingMode & ( GOURAD | PHONG ) ) == 0 ) // shade() fills in grayshade[]
	 c = new Color( grayshade[0], grayshade[1], grayshade[2] );

      if ( g != null ) g.setColor( c );
      else if ( pix != null ) {
	 polyDraw.initialize( pix, w, h, toPoly(), c );
	 if ( ( drawingMode & ( GOURAD | PATTERN_MAPPING ) ) != 0 )
	    polyDraw.setColors( vertcolors ); // shade() fills in vertcolors
	 else polyDraw.setColors( null );
	 if ( ( drawingMode & PATTERN_MAPPING ) != 0 && ( drawingMode & WIREFRAME ) == 0 )
	    polyDraw.setPattern( pattern, patw, path, xmap, ymap );
	 else polyDraw.pattern = null;
      }

      if ( nverts > 2 ) {
	 if ( g != null ) {
	    if ( ( drawingMode & WIREFRAME ) == 0 ) g.fillPolygon( toPoly() );
	    else g.drawPolygon( toPoly() );
	 } else if ( pix != null ) {
	    if ( ( drawingMode & WIREFRAME ) == 0 ) polyDraw.fillPolygon();
	    else polyDraw.drawPolygon();
	 }
      } else if ( nverts == 2 ) {
	 if ( g != null )
	    g.drawLine( (int) verts[0].xp, (int) verts[0].yp, (int) verts[1].xp, (int) verts[1].yp );
	 else
	    polyDraw.midpointLine( (int) verts[0].xp, (int) verts[0].yp, (int) verts[1].xp, (int) verts[1].yp );
      } else if ( nverts == 1 ) {
	 if ( g != null ) g.fillRect( (int) verts[0].xp, (int) verts[0].yp, 1, 1 );
	 else if ( pix != null ) pix[ (int) verts[0].xp + ( (int) verts[0].yp ) * w ] = polyDraw.rgb;
      }	 
   }
   
   public boolean shade( light lights[], int nlights ) {
      if ( ! visible() ) return false; // Has at least one vertex behind the camera
      else if ( nverts <= 2 || ( drawingMode & SHADED ) == 0 ) {
	 grayshade[0] = grayshade[1] = grayshade[2] = 1.0f;
	 if ( nverts <= 2 ) return true;
      }

      temp1.set( verts[2].xp - verts[1].xp, verts[2].yp - verts[1].yp, verts[2].zt - verts[1].zt ).
	 cross( temp2.set( verts[1].xp - verts[0].xp, verts[1].yp - verts[0].yp, verts[1].zt - verts[0].zt ) );
      if ( ( drawingMode & BACKFACE_CULLING ) != 0 && temp1.k < 0 ) return false; // Facing away from camera...cull it
      if ( ( drawingMode & SHADED ) == 0 ) return true; // Don't need to shade. 
      if ( ( drawingMode & ( GOURAD | PHONG ) ) == 0 ) { // Flat or wire-frame (solid) shading
	 getShadingAtVertex( null, lights, nlights, grayshade ); // Use the face's normal
      } else if ( ( drawingMode & GOURAD ) != 0 ) { // Gourad shading. Get colors for the different vertices.
	 for ( int i = 0; i < nverts; i ++ ) {
	    getShadingAtVertex( verts[ i ], lights, nlights, grayshade );
	    vertcolors[ i ] = ( 255 << 24 ) | ( ( (int)( grayshade[0] * 255 ) ) << 16 ) |
	       ( ( (int)( grayshade[1] * 255 ) ) << 8 ) | ( (int)( grayshade[2] * 255 ) );
	 }
      } else if ( ( drawingMode & PHONG ) != 0 ) { // Phong shading. Get normals for the different vertices.
      }
      if ( ( drawingMode & PATTERN_MAPPING ) != 0 ) { // Get mapping coords for vertices
	 for ( int i = 0; i < nverts; i ++ ) {
	    if ( ( drawingMode & ( GOURAD | PHONG ) ) == 0 ) { // Flat or wire-frame (solid) shading
	       vertcolors[ i ] = ( 255 << 24 ) | ( ( (int)( grayshade[0] * 255 ) ) << 16 ) |
		  ( ( (int)( grayshade[1] * 255 ) ) << 8 ) | ( (int)( grayshade[2] * 255 ) );
	    }
	    if ( verts[ i ].map == null ) { // Dont do it if one of the verts doesnt have a map point.
	       pattern = null;
	       break;
	    } else {
	       xmap[ i ] = verts[ i ].map[ 0 ];
	       ymap[ i ] = verts[ i ].map[ 1 ];
	    }
	 }	 
      }
      return true;
   }

   public void getShadingAtVertex( vertex v, light lights[], int nlights, float outshade[] ) {
      boolean useFaceNormal = false;
      if ( v == null ) { // v is null if we're doing flat shading. Then use the face's normal to calculate
	 useFaceNormal = true; // shading.
	 v = verts[ 0 ];
      }
      outshade[0] = outshade[1] = outshade[2] = 0.0f;
      for ( int j = 0; j < nlights; j ++ ) {
	 light l = lights[ j ];
	 vector inten = l.getIntensityAt( v ); // Intensity of light source at vertex
	 if ( l.type == light.AMBIENT ) {
	    if ( color != null && ambient != null ) {
	       outshade[ 0 ] += color[ 0 ] * ambient[ 0 ] * inten.i;
	       outshade[ 1 ] += color[ 1 ] * ambient[ 1 ] * inten.j;
	       outshade[ 2 ] += color[ 2 ] * ambient[ 2 ] * inten.k;
	    }
	 } else { // DIFFUSE + SPECULAR
	    vector direc = null;
	    float dot = 0;
	    if ( color != null && diffuse != null ) { // DIFFUSE 
	       direc = l.getDirectionAt( v ).normalize(); // Direction to light
	       if ( ! useFaceNormal ) dot = (float) v.normal().dot( direc ); // Use vertex normal
	       else dot = (float) normal().dot( direc ); // Use face's normal (if flat shading)
	       if ( dot > 0 ) {
		  if ( diffuse[ 0 ] > 0 ) outshade[ 0 ] += color[ 0 ] * diffuse[ 0 ] * dot * inten.i;
		  if ( diffuse[ 1 ] > 0 ) outshade[ 1 ] += color[ 1 ] * diffuse[ 1 ] * dot * inten.j;
		  if ( diffuse[ 2 ] > 0 ) outshade[ 2 ] += color[ 2 ] * diffuse[ 2 ] * dot * inten.k;
	       }
	    }
	    if ( color != null && specular != null && spec_hilight > 0 ) { // SPECULAR
	       if ( direc == null ) { // Dont need to re-calculate direction to light if done already.
		  direc = l.getDirectionAt( v ).normalize(); // To light (L)
		  if ( ! useFaceNormal ) dot = (float) v.normal().dot( direc ); // Use vertex normal
		  else dot = (float) normal().dot( direc ); // Use face's normal (if flat shading)
	       }
	       if ( dot <= 0 ) continue; // No specular if light is on opposite side of face.
	       vector wo = world3d.currentWorld.offset;
	       vector h = temp2.set( v.xt - wo.i, v.yt - wo.j, v.zt - wo.k ).normalize(); // To camera (E)
	       h.plus( direc ).normalize(); // This gives the h vector...midway between E and L.
	       dot = (float) Math.pow( v.normal().dot( h ), (double) spec_hilight );
	       if ( specular[ 0 ] > 0 ) outshade[ 0 ] += specular[ 0 ] * dot * inten.i;
	       if ( specular[ 1 ] > 0 ) outshade[ 1 ] += specular[ 1 ] * dot * inten.j;
	       if ( specular[ 2 ] > 0 ) outshade[ 2 ] += specular[ 2 ] * dot * inten.k;
	    }
	 }
      }
      if ( outshade[ 0 ] > 1.0 ) outshade[ 0 ] = 1.0f;
      if ( outshade[ 1 ] > 1.0 ) outshade[ 1 ] = 1.0f;
      if ( outshade[ 2 ] > 1.0 ) outshade[ 2 ] = 1.0f;
      world3d.currentWorld.attenuateColor( outshade, v ); // World might have fog.
   }      

   public boolean containsPoint( int x, int y ) {
      Polygon p = toPoly();
      if ( p == null ) return false;
      return toPoly().inside( x, y );
   }

   public void transform( matrix m ) { // TRANSFORM face via matrix (transform all vertices)
      for ( int i = 0; i < nverts; i++ ) {
	 verts[ i ].transform( m );
      }
      recalculateNormal = true;
   }

   public void project( matrix m ) {
      for ( int i = 0; i < nverts; i++ ) {
	 verts[ i ].project( m );
      }
   }

   public void setToTransformed() {
      for ( int i = 0; i < nverts; i++ ) {
	 verts[ i ].setToTransformed();
      }
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

   public boolean visible() {
      for ( int i = 0; i < nverts; i++ ) {
	 if ( ! verts[ i ].visible ) return false;
      }
      return true;
   }

   public String toString() {
      String out = "{";
      for ( int i = 0; i < nverts; i++ ) {
	 out += verts[ i ].toString();
	 if ( i < nverts - 1 ) out += ",";
      }
      out += "}";
      return out;
   }
}
