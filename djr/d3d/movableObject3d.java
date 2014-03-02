package djr.d3d;

/**
 * Class <code>movablObject3d</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class movableObject3d extends object3d {
   public static movableObject3d objects[] = new movableObject3d[ 5 ];
   public static int nobjects = 0;
   public vector vel = new vector(), rotVel = new vector();
   public boolean moving = false;
   public boolean unitPerSecond = true; // If false its velocity is in units per frame.
   public long lastTime = 0;
   
   public movableObject3d() {
      super();
   }
   
   public movableObject3d( int type ) {
      super( type );
   }

   public movableObject3d( String str ) {
      super( str );
   }

   private static void addObject( movableObject3d obj ) {
      movableObject3d newobj[] = null;
      if ( nobjects == objects.length ) {
	 newobj = new movableObject3d[ nobjects + 5 ];
	 System.arraycopy( objects, 0, newobj, 0, nobjects );
	 objects = newobj;
      }
      objects[ nobjects++ ] = obj;
   }

   private static void removeObject( movableObject3d obj ) {
      for ( int i = 0; i < nobjects; i ++ ) {
	 if ( objects[ i ] == obj ) {
	    objects[ i ] = null;
	    int j = objects.length - i - 1;
	    if ( j > 0 ) {
	       System.arraycopy( objects, i+1, objects, i, j );
	       nobjects --;
	    }
	    return;
	 }
      }
   }

   public static void moveObjects() {
      for ( int i = nobjects-1; i >= 0; i -- ) {
	 if ( objects[ i ] != null ) objects[ i ].move();
      }
   }

   public static void pauseObjects() {
      for ( int i = nobjects-1; i >= 0; i -- ) {
	 if ( objects[ i ] != null ) objects[ i ].pause();
      }
   }

   public movableObject3d startMoving() {
      if ( moving == true ) return this;
      moving = true;
      movableObject3d.addObject( this );
      if ( unitPerSecond ) lastTime = System.currentTimeMillis();
      return this;
   }

   public movableObject3d stopMoving() {
      if ( moving == false ) return this;
      moving = false;
      movableObject3d.removeObject( this );
      return this;
   }

   public void pause() {
      lastTime = System.currentTimeMillis();
   }

   public void move() {
      if ( ! moving ) return;
      if ( unitPerSecond ) {
	 long newTime = System.currentTimeMillis();
	 double frac = ( (double) newTime - lastTime ) / 1000.0;
	 rot.plus( rotVel.i * frac, rotVel.j * frac, rotVel.k * frac );
	 offset.plus( vel.i * frac, vel.j * frac, vel.k * frac );
	 lastTime = newTime;
      } else {
	 rot.plus( rotVel );
	 offset.plus( vel );
      }
      recalculateMatrix = true;
      //transform( matrix() );
   }
   
   public movableObject3d setVelocity( double dx, double dy, double dz ) {
      vel.set( dx, dy, dz );
      return this;
   }

   public movableObject3d setRotVelocity( double dx, double dy, double dz ) {
      rotVel.set( dx, dy, dz );
      return this;
   }
}

   
   
