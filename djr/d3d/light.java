package djr.d3d;
import java.awt.*;
 
/**
 * Class <code>light</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class light extends movableObject3d { // Make it movable for flexibility
   public static int AMBIENT = 1; // Direction and location unimportant
   public static int DIRECTIONAL = 2; // Direction vector uses direction and location vertices
   public static int POINT = 3; // Only location important
   public static int ATTENUATED_POINT = 4; // Ditto
   public static vector tempvec1 = new vector(), tempvec2 = new vector();

   public int type = DIRECTIONAL;
   public vertex location = new vertex(), direction = new vertex( 0, 0, 1 );
   public float params[] = null;

   public light( int type ) {
      this.type = type;
      if ( type == DIRECTIONAL ) addVertex( location ).addVertex( direction );
      else if ( type == POINT || type == ATTENUATED_POINT ) addVertex( location );
      if ( type == ATTENUATED_POINT ) { // Attenuation constants (see p. 726 of that book)
	 params = new float[ 3 ];
	 params[ 0 ] = 0.25f; // Default attenuation constants
	 params[ 1 ] = 0.25f;
	 params[ 2 ] = 0.5f;
      }
      color[ 0 ] = color[ 1 ] = color[ 2 ] = 1.0f;
   }

   public vector getDirectionAt( vertex v ) { // Direction of light at vertex
      if ( type == DIRECTIONAL ) return tempvec1.set( location, direction );
      else if ( type == POINT || type == ATTENUATED_POINT ) return tempvec1.set( v, location );
      return null;
   }

   public vector getIntensityAt( vertex v ) { // Intensity of light at vertex
      if ( type == ATTENUATED_POINT ) {
	 float dl = (float) getDirectionAt( v ).length(); // Distance to light
	 float atten_factor = 1.0f / ( params[0] + params[1] * dl + params[1] * dl * dl );
	 atten_factor = atten_factor < 1.0f ? atten_factor : 1.0f; // Clamp to a max of 1
	 return tempvec2.set( color[0] * atten_factor, color[1] * atten_factor, color[2] * atten_factor );
      }
      return tempvec2.set( color[0], color[1], color[2] );
   }
}
