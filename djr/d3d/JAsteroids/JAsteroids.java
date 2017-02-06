package djr.d3d.JAsteroids;
import java.awt.* ;
import java.awt.image.* ;
import java.applet.*;

import java.net.*;
import java.io.*;
import java.util.Hashtable;

import djr.d3d.*;
import djr.d3d.anim.*;
import djr.util.gui.AWTUtils;

/**
 * Class <code>JAsteroids</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.3
 */

public class JAsteroids extends Applet implements Runnable {
   static final int CUBE_SIZE = 300;
   static final int RADAR_SIZE = 60; // Pixels
   static final int ENERGY_BAR_LENGTH = 100; // Pixels
   static final int ENERGY_BAR_HEIGHT = 15; // Pixels
   static final int SCORE_FOR_NEW_GUY = 10000;
   static final String version = "Version 1.1.3 (March, 2003)";
   static final int FAST_SPEED = 1;
   static final int NORMAL_SPEED = 2;
   static final int SLOW_SPEED = 4;   
   static boolean JDK_11_COMPATIBLE = true;

   static JAsteroids roids = null;
   static Hashtable audioClips = new Hashtable();
   Image image = null, otherImage = null;
   Graphics offscreen = null, otherOffscreen = null; 
   world3d world1 = null, world2 = null;
   Thread t = null;
   boolean stopped = false;
   int oldX, oldY;
   String username = "";
   int speed = NORMAL_SPEED; // 4 is slow, 2 medium (default), 1 blazing

   MemoryImageSource source = null;
   int pix[] = null;

   String highScores[] = new String[ 10 ];
   int scoreRank = -1, savedScore = 0;
   ship ship = null;
   int shipsLeft = 3, level = 0;
   int score = 0; // Give 'em 10 seconds worth of shielding
   long waitForShip = 0, needNewShip = 0;
   long waitForNewLevel = 0, needNewLevel = 0;
   Rectangle rSide, rTop = null; // Rectangles to draw side and top views into
   static Rectangle rEnergy = null; // Rectangle for energy indicator
   Rectangle rOtherView = null;
   boolean gameOver = true, started = false;
   long dahDumTime = 0;
   boolean justPlayedDah = false;
   int asteroidsHit = 0, nStartingAst = 0;

   Button start = null, switchViews = null, pause = null;
   Checkbox showSmallView = null, soundIsOn = null;
   Choice choice = null;
   boolean fastDrawing = true;

   public void init() {
      String vers = System.getProperty( "java.version" );
      if ( vers == null ) vers = "1.0.2";
      vers = vers.substring( 0, 3 );
      if ( vers.equals( "Lin" ) || vers.equals( "JDK" ) ) vers = "1.1";
      try {
	 if ( Double.valueOf( vers ).doubleValue() >= 1.1 ) JDK_11_COMPATIBLE = true;
	 else JDK_11_COMPATIBLE = false;
      } catch( Exception e ) {
	 JDK_11_COMPATIBLE = true;
      }
      roids = this;
      username = getParameter( "username" );
      String spd = getParameter( "speed" );
      if ( spd.equals( "fast" ) ) speed = FAST_SPEED;
      else if ( spd.equals( "slow" ) ) speed = SLOW_SPEED;
      
      showStatus( "Initializing 3-D JAsteroids applet for player " + username + "." );
      start = new Button( "Start Game" );
      add( start );
      pause = new Button( " Pause " );
      add( pause );
      showSmallView = new Checkbox( "Ship's-Eye", null, true );
      add( showSmallView );
      soundIsOn = new Checkbox( "Sound", null, false );
      add( soundIsOn );
      switchViews = new Button( "Switch" );
      if ( showSmallView.getState() ) switchViews.enable();
      else switchViews.disable();
      //add( switchViews );
      choice = new Choice();
      choice.addItem( "Flat Shaded" );
      choice.addItem( "Wireframe" );
      choice.addItem( "Flat Unshaded" );
      choice.addItem( "Gourad Shaded" );
      choice.addItem( "Fast Drawing" );
      //add( choice );
            
      rSide = new Rectangle( size().width-RADAR_SIZE, size().height-RADAR_SIZE, 
			     RADAR_SIZE, RADAR_SIZE );
      rSide.translate( -1, -1 );
      rTop = new Rectangle( size().width-RADAR_SIZE*2, size().height-RADAR_SIZE, 
			    RADAR_SIZE, RADAR_SIZE );
      rTop.translate( -1, -1 );
      rEnergy = new Rectangle( rTop.x-ENERGY_BAR_LENGTH-5, 
			       size().height-ENERGY_BAR_HEIGHT-3,
			       ENERGY_BAR_LENGTH, ENERGY_BAR_HEIGHT );
      rOtherView = new Rectangle( 1, size().height-150-1, 150, 150 );

      setupWorlds();
      
      face f = new face( "{{0.101,0.059,0},{0.092,0.079,0},{0.089,0.100,0},{0.093,0.126,0},{0.105,0.150,0},{0.127,0.168,0},{0.159,0.174,0},{0.189,0.170,0},{0.223,0.160,0},{0.305,0.117,0},{0.388,0.074,0},{0.425,0.064,0},{0.458,0.060,0},{0.524,0.066,0},{0.580,0.084,0},{0.627,0.113,0},{0.666,0.155,0},{0.696,0.203,0},{0.717,0.253,0},{0.730,0.306,0},{0.734,0.360,0},{0.731,0.412,0},{0.720,0.461,0},{0.703,0.506,0},{0.679,0.548,0},{0.654,0.580,0},{0.625,0.608,0},{0.591,0.631,0},{0.554,0.651,0},{0.513,0.666,0},{0.468,0.676,0},{0.365,0.685,0},{0.340,0.685,0},{0.312,0.682,0},{0.310,0.708,0},{0.386,0.738,0},{0.453,0.770,0},{0.512,0.805,0},{0.562,0.841,0},{0.602,0.882,0},{0.631,0.928,0},{0.648,0.981,0},{0.654,1.039,0},{0.649,1.090,0},{0.636,1.135,0},{0.613,1.173,0},{0.581,1.205,0},{0.544,1.230,0},{0.504,1.248,0},{0.461,1.258,0},{0.416,1.262,0},{0.363,1.257,0},{0.312,1.242,0},{0.266,1.216,0},{0.222,1.181,0},{0.174,1.125,0},{0.123,1.045,0},{0.092,1.052,0},{0.116,1.124,0},{0.149,1.189,0},{0.191,1.246,0},{0.241,1.295,0},{0.298,1.335,0},{0.359,1.363,0},{0.424,1.380,0},{0.493,1.386,0},{0.565,1.381,0},{0.629,1.365,0},{0.683,1.339,0},{0.730,1.302,0},{0.766,1.258,0},{0.792,1.212,0},{0.808,1.161,0},{0.813,1.108,0},{0.809,1.061,0},{0.796,1.015,0},{0.774,0.972,0},{0.744,0.930,0},{0.694,0.881,0},{0.623,0.827,0},{0.710,0.783,0},{0.777,0.731,0},{0.825,0.675,0},{0.859,0.612,0},{0.879,0.542,0},{0.886,0.465,0},{0.877,0.373,0},{0.849,0.286,0},{0.802,0.205,0},{0.737,0.130,0},{0.697,0.096,0},{0.654,0.067,0},{0.607,0.042,0},{0.555,0.021,0},{0.500,0.005,0},{0.441,-0.006,0},{0.311,-0.015,0},{0.254,-0.013,0},{0.206,-0.006,0},{0.168,0.006,0},{0.139,0.022,0}}" );
      movableObject3d word = new movableObject3d();
      word.extrudeFrom( f, false ).setDrawingMode( world1.drawingMode | face.SORTED | face.BACKFACE_CULLING ).
	 setColor( new Color( Integer.parseInt( getParameter( "textcolor" ), 16 ) ) ).
	 setSpecular( 0f, 0f, 0f, 0f );
      f = new face( "{{1.057,0.000,0},{1.626,0.000,0},{1.736,0.004,0},{1.837,0.015,0},{1.929,0.033,0},{2.012,0.058,0},{2.096,0.096,0},{2.178,0.149,0},{2.237,0.199,0},{2.288,0.255,0},{2.331,0.316,0},{2.366,0.382,0},{2.394,0.452,0},{2.413,0.524,0},{2.425,0.597,0},{2.429,0.673,0},{2.424,0.775,0},{2.407,0.869,0},{2.380,0.955,0},{2.342,1.033,0},{2.293,1.103,0},{2.233,1.164,0},{2.163,1.218,0},{2.081,1.263,0},{1.979,1.303,0},{1.867,1.332,0},{1.743,1.349,0},{1.609,1.355,0},{1.057,1.355,0},{1.057,1.317,0},{1.151,1.303,0},{1.182,1.291,0},{1.204,1.277,0},{1.218,1.256,0},{1.229,1.225,0},{1.235,1.184,0},{1.237,1.133,0},{1.237,0.222,0},{1.235,0.169,0},{1.229,0.128,0},{1.218,0.097,0},{1.203,0.078,0},{1.181,0.064,0},{1.150,0.053,0},{1.057,0.037,0},{1.057,0.000,0},{1.459,0.102,0},{1.451,0.117,0},{1.445,0.158,0},{1.445,1.199,0},{1.451,1.241,0},{1.459,1.255,0},{1.470,1.265,0},{1.505,1.276,0},{1.563,1.280,0},{1.695,1.271,0},{1.813,1.246,0},{1.867,1.227,0},{1.917,1.204,0},{1.964,1.176,0},{2.008,1.144,0},{2.054,1.102,0},{2.094,1.055,0},{2.127,1.003,0},{2.155,0.946,0},{2.176,0.884,0},{2.192,0.818,0},{2.201,0.746,0},{2.204,0.670,0},{2.200,0.585,0},{2.189,0.506,0},{2.171,0.434,0},{2.145,0.369,0},{2.111,0.310,0},{2.070,0.257,0},{2.022,0.211,0},{1.966,0.171,0},{1.883,0.129,0},{1.790,0.100,0},{1.686,0.082,0},{1.571,0.076,0},{1.507,0.080,0},{1.470,0.092,0},{1.459,0.102,0}}" );
      object3d letter = new object3d().extrudeFrom( f ).
	 setDrawingMode( world1.drawingMode | face.SORTED | face.BACKFACE_CULLING ).offset( 0, 0, 2 );
      word.addObject( letter );
      vertex com = word.centerOfMass();
      word.setRotVelocity( 0, 50/speed, 0 ).startMoving().offset( -com.x, -com.y, -com.z ).
	 setToTransformed().rotate( 0, 0, 180 ).scale( 50, 50, 30 ).offset( 0, -200, 0 ).
	 setDiffuse( 0.9f, 0.9f, 0.9f ).setAmbient( 0.3f, 0.3f, 0.3f );
      world1.addObject( word );

      asteroid.setColors( new Color( Integer.parseInt( getParameter( "smastcolor" ), 16 ) ),
			  new Color( Integer.parseInt( getParameter( "medastcolor" ), 16 ) ),
			  new Color( Integer.parseInt( getParameter( "lgastcolor" ), 16 ) ) );
      ufo.createUFOs( new Color( Integer.parseInt( getParameter( "smufocolor" ), 16 ) ),
		      new Color( Integer.parseInt( getParameter( "lgufocolor" ), 16 ) ) );
      ship.createShip( new Color( Integer.parseInt( getParameter( "shipcolor" ), 16 ) ) );
      if ( speed != SLOW_SPEED ) torpedo.strip = new animStrip( "images/explosion.gif", JAsteroids.roids, 23 );

      t = new Thread( this );
      //t.setPriority( Thread.MIN_PRIORITY );
      t.start();
      
      showStatus( "" );
   }

   public String getParameter( String param ) {
      String out = null;
      try { out = super.getParameter( param ); return out; }
      catch( Exception e ) { return System.getProperty( param ); }
   }

   public void showStatus( String status ) {
      try { super.showStatus( status ); }
      catch( Exception e ) { };
   }   
   
   void setupWorlds() {
      world1 = new world3d( 0, 0, size().width, size().height );
      world1.drawingMode = face.FLAT | face.SHADED | face.BACKFACE_CULLING;
      world1.addLight( new light( light.POINT ) ).offset( -1000, 1000, 1000 ).setColor( 0.7f, 0.7f, 0.7f );
      world1.addLight( new light( light.AMBIENT ) ).setColor( 0.7f, 0.7f, 0.7f );
      world1.offset( 0, -70, -500 ); // Move the camera back and slightly up so we see the objects.
      world1.pan( -15, 0, 0 ); // Pan down so we look at the center
      world2 = new world3d( 0, 0, rOtherView.width, rOtherView.height );
      world2.fov = 60;
      world2.drawingMode = face.FLAT | face.WIREFRAME | face.OVERRIDE;
      addObject( new object3d( object3d.CUBE ).scale( CUBE_SIZE ).setDrawingMode( face.WIREFRAME ) );
   }

   void startGame() {
      showStatus( "Starting new game." );
      level = 0;
      score = 0;
      savedScore = 0;
      gameOver = false;
      started = true;
      setupWorlds();
      
      newLevel( 1 ); // Start new level immediately
      shipsLeft = 3;
      newShip( 1500 );

      ufo.ufoTime = System.currentTimeMillis() + (long) ( 30000.0 * Math.random() + 20000.0 );
      //t.resume();
      showStatus( "" );
   }

   void newLevel( long waitms ) {
      savedScore = 0;
      dahDumTime = System.currentTimeMillis();
      justPlayedDah = false;
      asteroidsHit = 0;
      if ( needNewLevel == 0 ) {
	 waitForNewLevel = System.currentTimeMillis();
	 needNewLevel = waitms;
	 return;
      } else if ( System.currentTimeMillis() - waitForNewLevel > needNewLevel ) {
	 level ++;
	 showStatus( "Initializing new level " + level + "..." );
	 int nstart = Integer.parseInt( getParameter( "nasteroids" ) );
	 int nstep = Integer.parseInt( getParameter( "naststep" ) );
	 nStartingAst = nstart + (level-1) * nstep; // Make 5 asteroids for level 1, 9 for level 2, etc.
	 for ( int i = 0; i < nStartingAst; i ++ ) {
	    asteroid a = (asteroid) asteroid.makeLarge();
	    double d1 = Math.random(); // Put at a random position at the edge of the bounding cube
	    double d2 = asteroid.rand();
	    double d3 = asteroid.rand();
	    if ( d1 <= 0.33 ) { // Choose an edge to offset it to
	       if ( a.vel.i > 0 ) a.offset( -CUBE_SIZE+20, d2*CUBE_SIZE*2.0, d3*CUBE_SIZE*2.0 );
	       else a.offset( CUBE_SIZE-20, d2*CUBE_SIZE*2.0, d3*CUBE_SIZE*2.0 );
	    } else if ( d1 < 0.67 ) {
	       if ( a.vel.j > 0 ) a.offset( d2*CUBE_SIZE*2.0, -CUBE_SIZE+20, d3*CUBE_SIZE*2.0 );
	       else a.offset( d2*CUBE_SIZE*2.0, CUBE_SIZE-20, d3*CUBE_SIZE*2.0 );
	    } else {
	       if ( a.vel.k > 0 ) a.offset( d2*CUBE_SIZE*2.0, d3*CUBE_SIZE*2.0, -CUBE_SIZE+20 );
	       else a.offset( d2*CUBE_SIZE*2.0, d3*CUBE_SIZE*2.0, CUBE_SIZE-20 );
	    }
	    a.transform( a.matrix() ); // Initialize the center of mass correctly
	    addObject( a );
	    needNewLevel = waitForNewLevel = 0;
	 }
	 if ( ufo.currentUFO != null ) ufo.deleteUFO();
	 showStatus( "" );
      }
   }
   
   public void destroy() { // java.applet.Applet method
      //t.suspend();
      stopped = true;
      endGame();
   }
   
   public void stop() { // java.applet.Applet method
      stopped = true;
      if ( ufo.clip != null ) ufo.clip.stop();
      //t.suspend();
      if ( ! gameOver ) reportHighScore();
   }

   public void start() { // java.applet.Applet method
      //if ( stopped ) t.resume();
      stopped = false;
   }

   void endGame() {
      if ( ufo.clip != null ) ufo.clip.stop();
      if ( ! gameOver ) reportHighScore();
      if ( ! gameOver ) getHighScores();
      if ( ship != null && shipsLeft > 0 ) ship.blowUp();
      shipsLeft = 0;
      gameOver = true;
      if ( ! start.getLabel().equals( "Start Game" ) ) start.setLabel( "Start Game" );
      repaint();
   }

   void reportHighScore() { // Prevent too many scores from being reported.
      if ( score > 1000 && score != savedScore && ! username.equals( "djr" ) ) {
	 showStatus( "Reporting score to server." );
	 try {
	    URL url = new URL( getParameter( "hsserver" ) );
	    URLConnection connection = url.openConnection();
	    connection.setDoOutput( true );
	    PrintStream out = new PrintStream( connection.getOutputStream() );
	    out.println( "passwd=cool%dude&score="+score+"&level="+level+"&lives="+shipsLeft+"&name="+username );
	    out.close();
	    DataInputStream in = new DataInputStream( new BufferedInputStream( connection.getInputStream() ) );
	    String inputLine;
	    boolean started = false;
	    showStatus( "You did not get a high score this time." );
	    while ( ( inputLine = in.readLine() ) != null ) {
	       if ( started ) scoreRank = Integer.parseInt( inputLine );
	       if ( inputLine.equals( "<html>" ) ) started = true;
	    }
	    in.close();
	    if ( scoreRank > -1 ) scoreRank ++;
	    savedScore = score;
	 } catch( Exception e ) {
	    showStatus( "Error connecting to " + getParameter( "hsserver" ) ); //http://www.astro.washington.edu" );
	    System.err.println( e );
	 }
	 showStatus( "" );
      }
   }

   void getHighScores() {
      showStatus( "Getting high scores from server." );
      try {
	 URL url = new URL( getParameter( "hsserver" ) );
	 URLConnection connection = url.openConnection();
	 connection.setDoOutput( true );
	 PrintStream out = new PrintStream( connection.getOutputStream() );
	 out.println( "passwd=cool%dude&get=yes" );
	 out.close();
	 DataInputStream in = new DataInputStream( new BufferedInputStream( connection.getInputStream() ) );
	 int i = 0;
	 String inputLine;
	 boolean started = false;
	 while ( ( inputLine = in.readLine() ) != null ) {
	    if ( started ) highScores[ i ++ ] = inputLine;
	    if ( inputLine.equals( "<html>" ) ) started = true;
	 }
	 in.close();
      } catch( Exception e ) {
	 showStatus( "Error connecting to host " + getParameter( "hsserver" ) );//http://www.astro.washington.edu" );
	 System.err.println( e );
      }
      if ( scoreRank > 0 ) {
	 showStatus( "Congratulations, you have made the high scores list, number " + scoreRank + "!" );
	 scoreRank = -1;
      } else {
	 showStatus( "" );
      }
   }

   void newShip( long waitms ) {
      savedScore = 0;
      if ( shipsLeft == 0 ) {
	 endGame();
	 return;
      }
      if ( needNewShip == 0 ) {
	 waitForShip = System.currentTimeMillis();
	 needNewShip = waitms;
	 return;
      } else if ( System.currentTimeMillis() - waitForShip > needNewShip ) {
	 ship = ship.newShip();
	 addObject( ship );
	 shipsLeft --;
	 needNewShip = waitForShip = 0;
	 if ( ufo.currentUFO != null ) ufo.deleteUFO();
      }
   }      

   void drawRadar( Graphics g ) {
      FontMetrics fm = g.getFontMetrics( g.getFont() );
      g.setColor( Color.black ); // Erase the stuff behind it.
      g.fillRect( rTop.x, rTop.y, rTop.width, rTop.height );
      g.fillRect( rSide.x, rSide.y, rSide.width, rSide.height );
      g.setColor( Color.green );
      g.drawRect( rTop.x, rTop.y, rTop.width, rTop.height );
      g.drawRect( rSide.x, rSide.y, rSide.width, rSide.height );
      int width = fm.stringWidth( "Top" );
      g.drawString( "Top", rTop.x+rTop.width/2-width/2, rTop.y-2 );
      width = fm.stringWidth( "Side" );
      g.drawString( "Side", rSide.x+rSide.width/2-width/2, rSide.y-2 );      
      g.setColor( Color.green.darker() );
      g.drawLine( rTop.x+rTop.width/2, rTop.y+3, rTop.x+rTop.width/2, rTop.y+rTop.height-3 );
      g.drawLine( rSide.x+rSide.width/2, rSide.y+3, rSide.x+rSide.width/2, rSide.y+rSide.height-3 );
      g.drawLine( rTop.x+3, rTop.y+rTop.height/2, rTop.x+rTop.width-3, rTop.y+rTop.height/2 );
      g.drawLine( rSide.x+3, rSide.y+rSide.height/2, rSide.x+rSide.width-3, rSide.y+rSide.height/2 );
      for ( int i = 0; i < world1.nobjects; i ++ ) {
	 if ( ! ( world1.objects[ i ] instanceof asteroid ) ) continue;
	 asteroid o = (asteroid) world1.objects[ i ];
	 if ( ship != null && o == ship.shield ) continue;
	 g.setColor( o.getColor() );
	 int xT = (int) ( o.com.xt*RADAR_SIZE/CUBE_SIZE/2 + rTop.x + rTop.width/2 );
	 int yT = (int) ( -o.com.zt*RADAR_SIZE/CUBE_SIZE/2 + rTop.y + rTop.height/2 );
	 int xS = (int) ( o.com.zt*RADAR_SIZE/CUBE_SIZE/2 + rSide.x + rSide.width/2 );
	 int yS = (int) ( o.com.yt*RADAR_SIZE/CUBE_SIZE/2 + rSide.y + rSide.height/2 );
	 if ( ! ( o instanceof ship ) ) {
	    if ( ! ( o instanceof torpedo ) ) {
	       g.drawOval( xT - 1, yT - 1, 3, 3 ); // Small offsets are to CENTER the circle on
	       g.drawOval( xS - 1, yS - 1, 3, 3 ); // the point
	    } else {
	       g.drawLine( xT, yT, xT, yT );
	       g.drawLine( xS, yS, xS, yS );
	    }
	 } else {
	    g.fillOval( xT - 2, yT - 2, 5, 5 );
	    g.fillOval( xS - 2, yS - 2, 5, 5 );
	    asteroid.tempv.set( ship.dir.xt, ship.dir.yt, ship.dir.zt ).normalize();
	    g.drawLine( xT, yT, xT + (int) (asteroid.tempv.i*6), yT - (int) (asteroid.tempv.k*6) );
	    g.drawLine( xS, yS, xS + (int) (asteroid.tempv.k*6), yS + (int) (asteroid.tempv.j*6) );
	    if ( ship != null && ship.shieldIsOn ) { // Shield is on
	       g.setColor( ship.shield.getColor() );
	       g.drawOval( xT - 3, yT - 3, 7, 7 );
	       g.drawOval( xS - 3, yS - 3, 7, 7 );
	    }
	 }
      }
   }

   void drawEnergy( Graphics g ) {
      Font fnt = g.getFont();
      FontMetrics fm = g.getFontMetrics( fnt );
      g.setColor( Color.green.darker() );
      g.fillRect( rEnergy.x, rEnergy.y, rEnergy.width, rEnergy.height );
      int energy = ship != null ? ship.energy : 0;
      int x = (int) ( (double) energy / 10000 * rEnergy.width );
      g.setColor( Color.red );
      g.fillRect( rEnergy.x, rEnergy.y, x, rEnergy.height );
      g.setColor( Color.green );
      g.drawRect( rEnergy.x, rEnergy.y, rEnergy.width, rEnergy.height );
      int width = fm.stringWidth( "Energy" );
      g.drawString( "Energy", rEnergy.x+rEnergy.width/2-width/2, rEnergy.y-2 );
      x = rEnergy.width / 5;
      for ( int i = rEnergy.x + x; i < rEnergy.x + rEnergy.width; i += x ) {
	 g.drawLine( i, rEnergy.y, i, rEnergy.y + rEnergy.height );
      }
      width = fm.stringWidth( "Ships: " + shipsLeft );
      g.drawString( "Ships: " + shipsLeft, rEnergy.x+rEnergy.width/2-width/2, rEnergy.y-20 );
      width = fm.stringWidth( "Level: " + level );
      g.drawString( "Level: " + level, rEnergy.x+rEnergy.width/2-width/2, rEnergy.y-35 );
      width = fm.stringWidth( "Score: " + score );
      g.drawString( "Score: " + score, rEnergy.x+rEnergy.width/2-width/2, rEnergy.y-50 );
      if ( gameOver ) { // Draw GAME OVER text in large white font
	 Font f = new Font( fnt.getName(), Font.BOLD, 24 );
	 g.setFont( f );
	 g.setColor( Color.white );
	 fm = g.getFontMetrics( f );
	 width = fm.stringWidth( "Game Over" );
	 g.drawString( "Game Over", size().width/2-width/2, 75 );
	 g.setFont( fnt );
	 drawHighScores( g );
      }
   }

   void drawHighScores( Graphics g ) {
      if ( highScores[0] == null || highScores[0].equals( "" ) ) return;
      Font fnt = g.getFont();
      FontMetrics fm = g.getFontMetrics( fnt );
      g.setColor( Color.green );
      int width = fm.stringWidth( highScores[ 0 ] );
      for ( int i = 0; i < 10; i ++ ) {
	 g.drawString( highScores[i], size().width/2-width/2-20, 150 + 20 * i );
      }
      Font f = new Font( fnt.getName(), Font.BOLD, 18 );
      g.setFont( f );
      fm = g.getFontMetrics( f );
      g.setColor( Color.yellow );
      width = fm.stringWidth( "High Scores:" );
      g.drawString( "High Scores:", size().width/2-width/2, 100 );
      f = new Font( fnt.getName(), Font.BOLD, 14 );
      g.setFont( f );
      fm = g.getFontMetrics( f );
      width = fm.stringWidth( highScores[ 0 ] );
      g.drawString( " Score:    Level:   Lives:               Player Name:",
		    size().width/2-width/2, 125 );
      g.setFont( fnt );
   }

   void drawOtherView( Graphics g ) {
      if ( otherOffscreen == null ) {
         try {
            otherImage = createImage( rOtherView.width, rOtherView.height );
            otherOffscreen = otherImage.getGraphics();
         } catch ( Exception e ) {
            otherOffscreen = null;
	    if ( world2 != null ) world2.draw( g );
	    return;
         }
      }
      if ( world2 != null ) world2.draw( otherOffscreen );
      g.drawImage( otherImage, rOtherView.x, rOtherView.y, this );
      g.setColor( Color.green );
      g.drawRect( rOtherView.x, rOtherView.y, rOtherView.width, rOtherView.height );
      int xx = rOtherView.x + rOtherView.width / 2;
      int yy = rOtherView.y + rOtherView.height / 2;
      int sx = rOtherView.width / 5;
      int sy = rOtherView.height / 5;
      g.drawLine( xx, yy-sy, xx, yy-10 );
      g.drawLine( xx, yy+sy, xx, yy+10 );
      g.drawLine( xx-sx, yy, xx-10, yy );
      g.drawLine( xx+sx, yy, xx+10, yy );
   }
 
   void draw( Graphics g ) { // Override here for drawing.
      if ( world1 != null ) {
	 if ( fastDrawing ) {
	    world1.draw( g );
	 } else if ( JDK_11_COMPATIBLE ) {
	    world1.draw( pix, size().width, size().height );
	    source.newPixels();
	 }
	 drawRadar( g );
	 if ( ship != null ) drawEnergy( g );
	 if ( showSmallView.getState() ) drawOtherView( g );
      } else {
	 g.setColor( Color.black );
	 g.fillRect( 0, 0, size().width, size().height );
      }
      
      if ( ! started ) { // Draw text in the center when first loaded
	 Font fnt = g.getFont();
	 Font f = new Font( fnt.getName(), Font.BOLD, 28 );
	 g.setFont( f );
	 g.setColor( Color.white );
	 FontMetrics fm = g.getFontMetrics( f );
	 int width = fm.stringWidth( "3-D JAsteroids" );
	 g.drawString( "3-D JAsteroids", size().width/2-width/2, size().height/2-20 );
	 g.setFont( fnt );
	 fm = g.getFontMetrics( fnt );
	 width = fm.stringWidth( version );
	 g.drawString( version, size().width/2-width/2, size().height/2+20 );
	 width = fm.stringWidth( "By astrodud" );
	 g.drawString( "By astrodud", size().width/2-width/2, size().height/2+35 );
	 width = fm.stringWidth( "Press the \"Start Game\" button to begin." );
	 g.drawString( "Press the \"Start Game\" button to begin.", size().width/2-width/2, size().height/2+55 );
      }
   }

   public synchronized void run() {
      while ( true ) {
	 if ( ! stopped ) {
	    repaint();
	    if ( needNewShip > 0 && ufo.currentUFO == null ) newShip( 0 );
	    if ( needNewLevel > 0 ) newLevel( 0 );
	    if ( started ) ufo.newUFO();
	    movableObject3d.moveObjects();
	    if ( started && ! gameOver ) playDahDum();
	    Thread.yield();
	    try { wait(); } catch( Exception e ) { };
	 } else {
	    movableObject3d.pauseObjects();
	 }
      }
   }

   public synchronized void paint( Graphics g ) {
      Graphics gr = g;
      
      if ( fastDrawing ) {
	 if ( image != null && 
	      ( image.getWidth( this ) != size().width ||
		image.getHeight( this ) != size().height ) ) {
	    image = null;
	    offscreen = null;
	    world1.setBounds( new Rectangle( 0, 0, size().width, size().height ) );
	    world1.offsetTo( 0, -70, -500 );
	    rSide = new Rectangle( size().width-RADAR_SIZE, size().height-RADAR_SIZE, 
				   RADAR_SIZE, RADAR_SIZE );
	    rSide.translate( -1, -1 );
	    rTop = new Rectangle( size().width-RADAR_SIZE*2, size().height-RADAR_SIZE, 
				  RADAR_SIZE, RADAR_SIZE );
	    rTop.translate( -1, -1 );
	    rEnergy = new Rectangle( rTop.x-ENERGY_BAR_LENGTH-5, 
				     size().height-ENERGY_BAR_HEIGHT-3,
				     ENERGY_BAR_LENGTH, ENERGY_BAR_HEIGHT );
	    rOtherView = new Rectangle( 1, size().height-150-1, 150, 150 );
	 }

	 source = null;
	 pix = null;
	 if ( offscreen == null ) {
	    try {
	       image = createImage( size().width, size().height );
	       offscreen = image.getGraphics();
	    } catch ( Exception e ) { // double-buffering not available
	       offscreen = null;
	    }
	 }
	 if ( offscreen != null ) gr = offscreen; // double-buffering available
      } else {
	 offscreen = null;
	 if ( source == null ) {
	    if ( pix == null ) pix = new int[ size().width * size().height ];
	    if ( JDK_11_COMPATIBLE ) {
	       source = new MemoryImageSource( size().width, size().height, ColorModel.getRGBdefault( ),
					       pix, 0, size().width );
	       source.setAnimated( true );
	       source.setFullBufferUpdates( true );
	       image = createImage( source );
	    }
	 }
      }
      
      draw( gr );
      if ( image != null ) g.drawImage( image, 0, 0, this );
      notifyAll();
   }

   public void update( Graphics g ) {
      paint( g );
   }

   public boolean mouseDown( java.awt.Event evt, int x, int y ) {
      oldX = x;
      oldY = y;
      return true;
   }

   public boolean mouseDrag( java.awt.Event evt, int x, int y ) {
      world3d w = world1;
      boolean altDown = ( evt.modifiers & Event.ALT_MASK ) != 0;
      if ( altDown ) w.rotateWorld( ( oldY - y ) * 0.5f, ( x - oldX ) * 0.5f, 0 );
      else if ( evt.metaDown() ) w.offset( ( x - oldX ), ( y - oldY ), 0 );
      else w.offset( ( x - oldX ), 0, -( y - oldY ) * 2.0f );

      repaint();
      oldX = x;
      oldY = y;
      return true;
   }   

   public boolean keyDown( Event evt, int key ) {
      if ( gameOver ) return false;
      return ( ship != null ) ? ship.keyDown( evt, key ) : false;
   }

   public boolean keyUp( Event evt, int key ) {
      if ( gameOver ) return false;
      return ( ship != null ) ? ship.keyUp( evt, key ) : false;
   }

   public boolean action( Event evt, Object what ) { // evt.target = object that caused action
      // evt = event that caused action, what == string label of button that caused action
      if ( showSmallView.getState() ) switchViews.enable();
      else switchViews.disable();
      if ( what instanceof Boolean ) { // From 'Alternate View' checkbox
	 repaint();
      } else if ( ( (String) what ).equals( "End Game" ) ) {
	 ( (Button) evt.target ).setLabel( "Start Game" );
	 endGame();
	 return true;
      } else if ( ( (String) what ).equals( "Start Game" ) ) {
	 ( (Button) evt.target ).setLabel( "End Game" );
	 startGame();
	 return true;
      } else if ( ( (String) what ).equals( " Pause " ) ) {
	 ( (Button) evt.target ).setLabel( "Resume" );
	 //t.suspend();
	 stopped = true;
	 return true;
      } else if ( ( (String) what ).equals( "Resume" ) ) {
	 ( (Button) evt.target ).setLabel( " Pause " );
	 //t.resume();
	 stopped = false;
	 return true;
      } else if ( ( (String) what ).equals( "Quit" ) ) {
	 System.exit( 0 );
      } else if ( ( (String) what ).equals( "Switch" ) ) {
	 world3d tmp = world2;
	 world2 = world1;
	 world1 = tmp;
	 Rectangle b = world2.bounds();
	 world2.setBounds( world1.bounds() );
	 world1.setBounds( b );
      } else if ( evt.target instanceof Choice ) {
	fastDrawing = false;
	if ( ( (String) what ).equals( "Wireframe" ) ) world1.drawingMode = face.WIREFRAME;
	else if ( ( (String) what ).equals( "Flat Unshaded" ) ) world1.drawingMode = face.FLAT | face.BACKFACE_CULLING;
	else if ( ( (String) what ).equals( "Flat Shaded" ) ) world1.drawingMode = face.FLAT | face.SHADED | face.BACKFACE_CULLING;
	else if ( ( (String) what ).equals( "Gourad Shaded" ) ) world1.drawingMode = face.GOURAD | face.SHADED | face.BACKFACE_CULLING;
	else if ( ( (String) what ).equals( "Fast Drawing" ) ) fastDrawing = true;
	return true;
      }
      return false;
   }
   
   static final void playSound( String sound ) {
      AudioClip clip = (AudioClip) audioClips.get( sound );
      if ( clip == null ) {
	 clip = AWTUtils.getAudioClip( roids, "sounds/" + sound );
	 if ( clip != null ) audioClips.put( sound, clip );
      }
      if ( clip != null ) clip.play();
   }

   void playDahDum() {
      if ( ! roids.soundIsOn.getState() ) return;
      long newTime = System.currentTimeMillis();
      int nHitsPossible = 6 * nStartingAst; // # of asteroid hits needed to end level.
      if ( newTime - dahDumTime > 1900 - (1500*asteroidsHit/nHitsPossible) ) {
	 if ( justPlayedDah ) { // This makes it so it plays the dah-dums no more than 2 seconds apart
	    playSound( "dum.au" ); // no less that 1/2 second apart, and with a separation inversely 
	    justPlayedDah = false; // proportional to the number of asteroids hit this round.
	 } else {
	    playSound( "dah.au" );
	    justPlayedDah = true;
	 }
	 dahDumTime = newTime;
      }
   }

   int asteroidsLeft() {
      if ( world1 == null ) return 0;
      int out = 0;
      for ( int i = 0; i < world1.nobjects; i ++ ) {
	 if ( world1.objects[i] instanceof asteroid &&
	      ! ( world1.objects[i] instanceof ship || world1.objects[i] instanceof torpedo ) ) out ++;
      }
      return out;
   }
   
   void addObject( object3d o ) {
      if ( o == null ) return;
      world1.addObject( o );
      world2.addObject( o );
   }
   
   void removeObject( object3d o ) {
      if ( o == null ) return;
      if ( o instanceof movableObject3d ) ( (movableObject3d) o ).stopMoving();
      world1.removeObject( o );
      world2.removeObject( o );
   }

   public static void main( String args[] ) {
      Frame f = new Frame( "JAsteroids3D" );
      f.setSize( 650, 450 );
      f.setLayout( new BorderLayout() );
      JAsteroids j3d = new JAsteroids();
      f.add( BorderLayout.CENTER, j3d );
      f.show();
      j3d.init();
      j3d.add( new Button( "Quit" ) );
      j3d.doLayout();
      j3d.start();
   }
}

class asteroid extends movableObject3d {
   static final int LARGE = 40;
   static final int MEDIUM = 30;
   static final int SMALL = 20;
   public static Color smColor, medColor, lgColor = null;
   static vector tempv = new vector();

   vertex com = null;
   int size = LARGE;
   double hitRadiusSquared = (double) LARGE * LARGE;

   static final asteroid makeLarge() {
      STEPSIZE = 30;
      if ( JAsteroids.roids.speed == JAsteroids.SLOW_SPEED ) STEPSIZE = 45;
      return new asteroid( LARGE );
   }

   static final asteroid makeMedium() {
      STEPSIZE = 45;
      return new asteroid( MEDIUM );
   }

   static final asteroid makeSmall() {
      STEPSIZE = 60;
      if ( JAsteroids.roids.speed == JAsteroids.FAST_SPEED ) STEPSIZE = 45;
      return new asteroid( SMALL);
   }
   
   static final void setColors( Color c1, Color c2, Color c3 ) {
   	smColor = c1;
   	medColor = c2;
   	lgColor = c3;
   }

   public asteroid() {
   }

   public asteroid( int size ) {
      super( object3d.SPHERE );
      this.size = size;
      this.hitRadiusSquared = (double) size * size;
      Color c = Color.magenta; // Color for large asteroids
      if ( size == MEDIUM ) c = Color.cyan; // color for medium asteroids
      else if ( size == SMALL ) c = Color.orange; // color for small asteroids
      setColor( c );
      specular = null;
      diffuse[ 0 ] = diffuse[ 1 ] = diffuse[ 2 ] = 0.9f;
      ambient[ 0 ] = ambient[ 1 ] = ambient[ 2 ] = 0.3f;

      for ( int i = 0; i < nverts; i ++ ) { // Then for each vertex in the sphere...
	 double d1 = rand() / 5.0;
	 double d2 = rand() / 5.0;
	 double d3 = rand() / 5.0;
	 vertex v = verts[ i ];
	 v.x += d1; // ...perturb its coords to make it look bumpy.
	 v.y += d2;
	 v.z += d3;	    
      }
      
      com = centerOfMass(); // Keep track of the center of the asteroid

      double d1 = Math.random() + 0.5; // Make it rotate faster on higher levels
      setRotVelocity( d1 * ( 1000.0 + JAsteroids.roids.level*500 ) / size, 0, 0 ); // Bigger objects rotate slower

      d1 = rand();
      double d2 = rand();
      double d3 = rand();
      rotate( d1 * 180, d2 * 180, d3 * 180 );
      
      d1 = Math.random() * 0.5 + 0.5;
      d2 = Math.random() * 0.5 + 0.5;
      scale( d1 * size, d2 * size, size ); // Make sure at LEAST one axis is as big as size.

      d1 = rand();
      d2 = rand();
      d3 = rand(); // Make it move faster on higher levels...70 for level 1
      setVelocity( d1 * ( 1000.0 + JAsteroids.roids.level*500 ) / size,
		   d2 * ( 1000.0 + JAsteroids.roids.level*500 ) / size,
		   d3 * ( 1000.0 + JAsteroids.roids.level*500 ) / size );

      startMoving();
   }
   
   public movableObject3d setVelocity( double dx, double dy, double dz ) {
      super.setVelocity( dx/JAsteroids.roids.speed, dy/JAsteroids.roids.speed, dz/JAsteroids.roids.speed );
      return this;
   }

   public movableObject3d setRotVelocity( double dx, double dy, double dz ) {
      super.setRotVelocity( dx/JAsteroids.roids.speed, dy/JAsteroids.roids.speed, dz/JAsteroids.roids.speed );
      return this;
   }
   
   static final double rand() {
      return Math.random() - 0.5;
   }

   public void transform( matrix m ) { // Override to transform the COM
      super.transform( m );
      com.transform( m );
   }

   public void project( matrix m ) { // Override so COM gets projected so we know its dist. from the camera
      super.project( m );
      com.project( m );
   }

   public void move() { // Override to loop back to other side if its too far out from center
      if ( ! moving ) return;
      super.move();
      if ( ! ( this instanceof torpedo ) || // Flip back only if its not part of an explosion
	   ( this instanceof torpedo && ! ( (torpedo) this ).inExplosion ) ) flipBack();
   }
   
   boolean flipBack() { // Flip to other side of cube if its too far out
      boolean farOut = ( com.xt > JAsteroids.CUBE_SIZE || com.xt < -JAsteroids.CUBE_SIZE ||
			 com.yt > JAsteroids.CUBE_SIZE || com.yt < -JAsteroids.CUBE_SIZE ||
			 com.zt > JAsteroids.CUBE_SIZE || com.zt < -JAsteroids.CUBE_SIZE );
      if ( ! farOut ) return false;
      if ( com.xt > JAsteroids.CUBE_SIZE ) offset( -JAsteroids.CUBE_SIZE*2, 0, 0 );
      else if ( com.xt < -JAsteroids.CUBE_SIZE ) offset( JAsteroids.CUBE_SIZE*2, 0, 0 );
      else if ( com.yt > JAsteroids.CUBE_SIZE ) offset( 0, -JAsteroids.CUBE_SIZE*2, 0 );
      else if ( com.yt < -JAsteroids.CUBE_SIZE ) offset( 0, JAsteroids.CUBE_SIZE*2, 0 );
      else if ( com.zt > JAsteroids.CUBE_SIZE ) offset( 0, 0, -JAsteroids.CUBE_SIZE*2 );
      else if ( com.zt < -JAsteroids.CUBE_SIZE ) offset( 0, 0, JAsteroids.CUBE_SIZE*2 );
      recalculateMatrix = true;
      transform( matrix() );
      return true;
   }

   public double getZdist() {
      return com.wp;
   }

   void blowUp() {
      torpedo.createExplosion( 10, com, vel, this.getColor() );
      JAsteroids.roids.removeObject( this );
      if ( size == LARGE ) {
	 for ( int i = 1; i <= 2; i ++ ) {
	    object3d a = makeMedium().offset( com.xt+rand()*10, com.yt+rand()*10, com.zt+rand()*10 );
	    ( (asteroid) a ).transform( a.matrix() ); // Initialize the center of mass correctly
	    JAsteroids.roids.addObject( a );
	 }
      } else if ( size == MEDIUM ) {
	 for ( int i = 1; i <= 3; i ++ ) {
	    object3d a = makeSmall().offset( com.xt, com.yt, com.zt );
	    ( (asteroid) a ).transform( a.matrix() ); // Initialize the center of mass correctly
	    JAsteroids.roids.addObject( a );
	 }
      }
      JAsteroids.roids.asteroidsHit ++;
      if ( JAsteroids.roids.asteroidsLeft() == 0 ) JAsteroids.roids.newLevel( 2000 );
   }

   asteroid asteroidClosestTo() {
      asteroid out = null;
      double min = 9999999;
      vertex v = this.com;
      for ( int i = 0; i < JAsteroids.roids.world1.nobjects; i ++ ) {
	 if ( JAsteroids.roids.world1.objects[ i ] instanceof asteroid ) {
	    asteroid a = (asteroid) JAsteroids.roids.world1.objects[ i ];
	    if ( a != this ) {
	       double rad = ( a.com.xt - v.xt ) * ( a.com.xt - v.xt ) +
		  ( a.com.yt - v.yt ) * ( a.com.yt - v.yt ) +
		  ( a.com.zt - v.zt ) * ( a.com.zt - v.zt );
	       if ( rad < min ) {
		  min = rad;
		  out = a;
	       }
	    }
	 }
      }
      return out;
   }      

   static final asteroid asteroidHitBy( asteroid aa ) {
      asteroid a = aa.asteroidClosestTo();
      if ( a == null ) return null;
      vertex v = aa.com;
      vector vel = aa.vel;
      if ( vel.i == 0 && vel.j == 0 && vel.k == 0 ) vel = a.vel; // For when ship aint moving.
      double x1 = v.xt - vel.i; // See http://www.mhri.edu.au/~pdb/geometry/pointline/
      double y1 = v.yt - vel.j;
      double z1 = v.zt - vel.k;
      double x2 = v.xt + vel.i;
      double y2 = v.yt + vel.j;
      double z2 = v.zt + vel.k;
      double dist = (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) + (z2-z1)*(z2-z1);
      double x3 = a.com.xt;
      double y3 = a.com.yt;
      double z3 = a.com.zt;
      double u = ( (x3-x1)*(x2-x1) + (y3-y1)*(y2-y1) + (z3-z1)*(z2-z1) ) / dist;
      if ( u > 1 || u < 0 ) return null;
      double x = x1 + u*(x2-x1);
      double y = y1 + u*(y2-y1);
      double z = z1 + u*(z2-z1);
      double d = (x3-x)*(x3-x) + (y3-y)*(y3-y) + (z3-z)*(z3-z);
      if ( d <= a.hitRadiusSquared + aa.hitRadiusSquared ) return a;
      return null;
   }

   int score() { // Make it worth more on higher levels
      int out = 200;
      if ( size == LARGE ) out = 50;
      else if ( size == MEDIUM ) out = 100;
      out += ( JAsteroids.roids.level - 1 ) * 30;
      return out;
   }
}

class ship extends asteroid {
   static asteroid hitBy = null;
   static ship shp = null;
   vertex dir = new vertex( 0, 0, -1 );
   long lastTime = 0, lastTorp = 0;
   movableObject3d shield = null, fire = null;
   boolean shieldKeyIsDown = false, fireKeyIsDown = false, shieldIsOn = false;
   int arrowKeyIsDown = 0, thrustKeyIsDown = 0, energy = 10000;
   
   public ship( Color color ) {
      setColor( color );
      object3d o1 = new object3d().latheFrom( 60, 360, new face( "{{1,0,.5},{0,0,-1.5}}" ) );
      object3d o2 = new object3d().latheFrom( -60, 360, new face( "{{1,0,.5},{0,0,0.25}}}" ) );
      addObject( o1 ); // Front of ship
      addObject( o2 ); // Inverted cone for back of ship
      scale( 10 );
      hitRadiusSquared = 100;
      com = centerOfMass(); // Keep track of the center of the ship
      setAmbient( 0.4f, 0.4f, 0.4f );
      setDiffuse( 0.8f, 0.8f, 0.8f );
      setSpecular( 1.0f, 1.0f, 1.0f, 20.0f );

      STEPSIZE = 30;
      shield = new movableObject3d( object3d.SPHERE );
      shield.setDrawingMode( face.WIREFRAME | face.SHADED | face.BACKFACE_CULLING );
      shield.setColor( Color.yellow ).setDiffuse( 1.0f, 1.0f, 1.0f ).setAmbient( 0.5f, 0.5f, 0.5f );
      shield.scale( 20 );
      shield.specular = null;
      hitRadiusSquared = 400;
   }

   static final ship newShip() {
      shp.offset( -shp.offset.i, -shp.offset.j, -shp.offset.k );
      shp.rotate( -shp.rot.i, -shp.rot.j, -shp.rot.k );
      shp.setVelocity( 0, 0, 0 );
      shp.transform( shp.matrix() );
      shp.startMoving();
		shp.energy = 10000;
      return shp;
   }

   public static final ship createShip( Color color ) {
      shp = new ship( color );
      return shp;
   }

   void shield( boolean on ) {
      if ( shieldIsOn == on ) return;
      if ( on ) {
	 shield.offset.set( offset.i, offset.j, offset.k );
	 shield.rot.set( rot.i, rot.j, rot.k );
	 JAsteroids.roids.addObject( shield );
      } else {
	 JAsteroids.roids.removeObject( shield );
	 hitRadiusSquared = 100;
      }
      shieldIsOn = on;
   }

   boolean keyDown( Event evt, int key ) {
      if ( key == Event.LEFT || key == Event.RIGHT || key == Event.UP || key == Event.DOWN ||
	   key == '4' || key == '6' || key == '8' || key == '2' ) arrowKeyIsDown = key;
      else if ( key == 'a' || key == 'z' ) thrustKeyIsDown = key;
      else if ( key == 's' ) shieldKeyIsDown = true;
      else if ( key == ' ' || key == 'm' ) fireKeyIsDown = true;
      if ( arrowKeyIsDown != 0 || thrustKeyIsDown != 0 || shieldKeyIsDown || fireKeyIsDown ) handleShipEvents();
      return true;
   }
   
   boolean keyUp( Event evt, int key ) {
      if ( key == Event.LEFT || key == Event.RIGHT || key == Event.UP || key == Event.DOWN ||
	   key == '4' || key == '6' || key == '8' || key == '2' ) arrowKeyIsDown = 0;
      else if ( key == 'a' || key == 'z' ) thrustKeyIsDown = 0;
      else if ( key == 's' ) shieldKeyIsDown = false;
      else if ( key == ' ' || key == 'm' ) fireKeyIsDown = false;
      handleShipEvents();
      return true;
   }
   
   public matrix matrix() {
      if ( ! recalculateMatrix ) return mat;
      mat.normalize();
      mat.scale( scale.i, scale.j, scale.k );
      mat.rotate( rot.i, rot.j, rot.k );
      dir.transform( mat );
      mat.offset( offset.i, offset.j, offset.k );
      recalculateMatrix = false;

      if ( JAsteroids.roids.showSmallView.getState() ) {
	 JAsteroids.roids.world2.lookat.set( -dir.xt, -dir.yt, -dir.zt ); // Make camera "be" on the ship.
	 JAsteroids.roids.world2.offset.set( offset.i, offset.j, offset.k );
	 JAsteroids.roids.world2.recomputeMatrix = true;
      }  

      return mat;
   }

   public void move() {
      if ( ! moving ) return;
      super.move();

      asteroid a = asteroidHitBy( this ); // See if we're near an asteroid or UFO
      if ( a != null && ! ( a instanceof ship ) && ! ( a instanceof torpedo ) ) {
	 a.blowUp(); // blow the asteroid up 
	 JAsteroids.roids.score += a.score();
	 if ( ! shieldIsOn ) {
	    hitBy = a; // A hack to make the explosion form where the asteroid is.
	    blowUp(); // and blow the ship up if its shield isnt on.
	 }
      }

      handleShipEvents();
   }
	
   void handleShipEvents() {
      long t = System.currentTimeMillis();
      boolean doingNothing = true;
      if ( arrowKeyIsDown != 0 ) {
	 tempv.set( 0, 0, 0 );
	 if ( arrowKeyIsDown == Event.LEFT || arrowKeyIsDown == '4' ) tempv.j -= 2;
	 else if ( arrowKeyIsDown == Event.RIGHT || arrowKeyIsDown == '6' ) tempv.j += 2;
	 else if ( arrowKeyIsDown == Event.UP || arrowKeyIsDown == '8' ) tempv.i -= 2;
	 else if ( arrowKeyIsDown == Event.DOWN || arrowKeyIsDown == '2' ) tempv.i += 2;
	 rotate( tempv.i, tempv.j, tempv.k );
      }
      if ( thrustKeyIsDown != 0 && energy > 0 ) {
	 boolean reallyThrusting = false;
	 if ( thrustKeyIsDown == 'z' ) {
	    if ( vel.length() >= 0.1 ) {
	       vel.plus( -vel.i*0.1, -vel.j*0.1, -vel.k*0.1 ); // Decelerate (brake)
	       reallyThrusting = true;
	    }
	 } else if ( thrustKeyIsDown == 'a' ) {
	    tempv.set( dir.xt, dir.yt, dir.zt ).normalize(); // Accelerate frontwards
	    vel.plus( tempv.i*2, tempv.j*2, tempv.k*2 );
	    reallyThrusting = true;
	 }
	 if ( reallyThrusting ) {
	    energy -= ( t - lastTime ); // Decrease 1000 units per second
	    JAsteroids.playSound( "thrust.au" );
	    doingNothing = false;
	 }
      }
      shield( shieldKeyIsDown && energy > 0 );
      if ( shieldKeyIsDown ) { // Shield is on
	 if ( shieldIsOn ) {
	    shield.offset.set( offset.i, offset.j, offset.k ); // Make shield follow ship
	    shield.rot.set( rot.i, rot.j, rot.k );
	    shield.recalculateMatrix = true;
	    energy -= ( t - lastTime ); // Subtract from energy, 1000 units per second
	    doingNothing = false;
	 }
      }
      if ( fireKeyIsDown && energy >= 1000 && t - lastTorp > 330 ) { // Fire!!
	 new torpedo( this ); // Allow 3 torps per second?
	 lastTorp = t;
	 energy -= 1000; // 1000 units per torp
	 JAsteroids.playSound( "torpedo.au" );
	 doingNothing = false;
      }
      if ( energy < 0 ) energy = 0; // Energy too low
      if ( doingNothing ) {
	 if ( energy < 10000 ) { // Replenish 500 units per second
	    energy += ( t - lastTime )/2;
	    if ( energy > 10000 ) energy = 10000;
	 }
      }
      lastTime = t;
   }

   void blowUp() {
      JAsteroids.roids.removeObject( this );
      if ( hitBy != null ) {
	 torpedo.createExplosion( 10, hitBy.com, vel, this.getColor() );
	 hitBy = null;
      } else {
	 torpedo.createExplosion( 10, com, vel, this.getColor() );
      }
      JAsteroids.roids.newShip( 2000 );
   }
}

class torpedo extends asteroid {
   static final int MAXSHARDS = 30;
   static int nshards = 0; // Dont let more than say 30 explosion shards to exist at one time
   static animStrip strip = null;
   static animStrip3d newStrip = null, newStrip2 = null;
   boolean inExplosion = false, fromUFO = false;
   long startTime = System.currentTimeMillis();

   public torpedo() {
      int size = 5;
      addFace( new face( new vertex(0,1,0), new vertex(0.5,0,0), new vertex(-0.5,0,0) ) );
      addFace( new face( new vertex(0,1,0), new vertex(0,0.5,1), new vertex(0.5,0,0) ) );
      addFace( new face( new vertex(0.5,0,0), new vertex(0,0.5,1), new vertex(-0.5,0,0) ) );
      addFace( new face( new vertex(-0.5,0,0), new vertex(0,0.5,1), new vertex(0,1,0) ) );
      com = centerOfMass(); // Keep track of the center of the torpedo
      offset( -com.x*size, -com.y*size, -com.z*size ); // Move to 0,0,0
      scale( size );
      hitRadiusSquared = (double) ( size * size );
      setColor( Color.red ).setDrawingMode( face.FLAT | face.BACKFACE_CULLING );
      JAsteroids.roids.addObject( this );
      startMoving();
   }

   public torpedo( ship sh ) {
      this();
      offset( sh.offset.i, sh.offset.j, sh.offset.k );
      tempv.set( sh.dir.xt, sh.dir.yt, sh.dir.zt ).normalize(); // Make it go out the front of the ship
      offset( tempv.i * 10, tempv.j * 10, tempv.k * 10 ); // So it doesn't start at the back of the ship, move toward front
      setVelocity( tempv.i * 200 + sh.vel.i, tempv.j * 200 + sh.vel.j, tempv.k * 200 + sh.vel.k );
   }

   public torpedo( int nvertices ) {
      if ( nvertices == 1 ) addFace( (face) new face().addVertex( new vertex(0,0,0) ) );
      else addFace( (face) new face().addVertex( new vertex(-0.5,0,0) ).addVertex( new vertex(0.5,0,0) ) );
      com = centerOfMass(); // Keep track of the center of the torpedo
      double d = Math.random();
      Color c = Color.red;
      if ( d < 0.33 ) c = Color.orange.darker();
      else if ( d < 0.67 ) c = Color.yellow;
      setColor( c ).setDrawingMode( face.FLAT | face.BACKFACE_CULLING );
      startTime += (int) 300 * d;
      inExplosion = true;
      if ( nvertices > 1 ) {
	 scale( (Math.random()+0.3)*3, (Math.random()+0.3)*3, (Math.random()+0.3)*3 );
	 rotate( rand()*180, rand()*180, rand()*180 );
	 setRotVelocity( rand()*200.0, rand()*200.0, rand()*200.0 );
      }
      JAsteroids.roids.addObject( this );
      startMoving();
   }

   static final void createExplosion( int ndots, vertex where, vector veloc, Color c ) {
      for ( int i = 0; i < ndots; i ++ ) {
	 if ( nshards > MAXSHARDS ) continue;
	 torpedo t = new torpedo();
	 nshards ++;
	 t.inExplosion = true;
	 t.rotate( rand()*180, rand()*180, rand()*180 );
	 t.scale( (Math.random()+0.1)*2, (Math.random()+0.1)*2, (Math.random()+0.1)*2 );
	 t.setVelocity( rand()*200.0+veloc.i, rand()*200.0+veloc.j, rand()*200.0+veloc.k );
	 t.offset( where.xt+t.vel.i/10, where.yt+t.vel.j/10, where.zt+t.vel.k/10 ); // So it doesnt start at 
	 t.setRotVelocity( rand()*600.0, rand()*600.0, rand()*600.0 ); // a point...move it a bit towards where its headed
	 double d = Math.random();
	 if ( d < 0.33 ) c = c.brighter();
	 else if ( d > 0.67 ) c = c.darker();
	 t.setColor( c );
	 t.startTime += (int) 300 * d;
      }

      JAsteroids.playSound( "explosion.au" );
      if ( JAsteroids.roids.speed != JAsteroids.SLOW_SPEED ) {
	 double scale = 1;
	 vector off = JAsteroids.roids.world1.offset;
	 double dist = tempv.set( off.i-where.xt, off.j-where.yt, off.k-where.zt ).length();
	 if ( dist > 600 ) scale = 0.75;
	 else if ( dist < 400 ) scale = 2.0; // Make explosions smaller if they're further away
	 newStrip = new animStrip3d( strip.images, JAsteroids.roids, 23, scale );
	 newStrip.setCenter( where );
	 newStrip.strip.advance = 2;
	 JAsteroids.roids.world1.addObject( newStrip );
	 newStrip.startMoving();
	 if ( JAsteroids.roids.speed != JAsteroids.SLOW_SPEED && JAsteroids.roids.showSmallView.getState() ) {
	    scale = 1;
	    off = JAsteroids.roids.world2.offset;
	    dist = tempv.set( off.i-where.xt, off.j-where.yt, off.k-where.zt ).length();
	    if ( dist > 200 ) scale = 0.75;
	    else if ( dist < 100 ) scale = 2.0; // Make explosions smaller if they're further away
	    newStrip2 = new animStrip3d( strip.images, JAsteroids.roids, 23, scale );
	    newStrip2.setCenter( where );
	    newStrip2.strip.advance = 2;
	    JAsteroids.roids.world2.addObject( newStrip2 );
	    newStrip2.startMoving();
	 }
      }
   }

   public void move() {
      if ( ! moving ) return;
      super.move();
      long durationScale = 1;
      if ( ( ! inExplosion && lastTime-startTime > 1500*JAsteroids.roids.speed ) ||
	   ( inExplosion && lastTime-startTime > 1000*JAsteroids.roids.speed ) ) {
	 blowUp(); // Just disappears
      } else if ( ! inExplosion ) {
	 asteroid a = asteroidHitBy( this ); // Check if it did or will hit an asteroid this frame
	 if ( a != null ) {
	    if ( ( this.fromUFO && ! ( a instanceof ufo ) ) || // UFO cant shoot itself
		 ( ! this.fromUFO && ! ( a instanceof ship ) ) ) { // UFO can only shoot ship
	       if ( ! ( a instanceof torpedo ) ) { // Torpedo can't shoot torpedo
		  int lastScore = JAsteroids.roids.score;
		  a.blowUp();
		  if ( ! this.fromUFO ) JAsteroids.roids.score += a.score();
		  int newShips = JAsteroids.roids.score / JAsteroids.SCORE_FOR_NEW_GUY -
		     lastScore / JAsteroids.SCORE_FOR_NEW_GUY;
		  if ( newShips > 0 ) { // Earned a new ship!
		     JAsteroids.playSound( "cheer!!!.au" );
		     JAsteroids.roids.shipsLeft += newShips;
		  }
		  blowUp();		  
	       }
	    }
	 }
      }
   }

   void blowUp() {
      JAsteroids.roids.removeObject( this );
      if ( inExplosion ) nshards --;
   }
}

class ufo extends asteroid {
   static ufo currentUFO = null, smallUFO = null, largeUFO = null;
   static long ufoTime = Long.MAX_VALUE;
   static java.applet.AudioClip clip = null;
   int mainDirection = 0;
   long lastShot = System.currentTimeMillis();

   public ufo( int size, Color c ) {
      this.size = size;
      object3d.STEPSIZE = 30;
      if ( JAsteroids.roids.speed == JAsteroids.SLOW_SPEED ) object3d.STEPSIZE = 45;
      addObject( new object3d( object3d.SPHERE ).rotate( 90, 0, 0 ).scale( 1, 1, 0.3 ) );
      addObject( new object3d( object3d.SPHERE ).scale( 0.4, 0.6, 0.4 ).setColor( Color.yellow ) );
      scale( size );
      this.hitRadiusSquared = (double) size * size;
      this.com = centerOfMass();
      currentUFO = this;

      setColor( c );
      setAmbient( 0.4f, 0.4f, 0.4f );
      setDiffuse( 0.8f, 0.8f, 0.8f );
      setSpecular( 1.0f, 1.0f, 1.0f, 20.0f );
   }

   static final void createUFOs( Color smColor, Color lgColor ) {
      smallUFO = new ufo( SMALL, smColor );
      largeUFO = new ufo( MEDIUM, lgColor );
   }

   static final ufo newUFO() {
      if ( currentUFO != null || System.currentTimeMillis() <= ufoTime ) return null;
      ufo u = smallUFO;
      if ( Math.random() < 0.7 ) u = largeUFO;      
      currentUFO = u;
      u.setDrawingMode( JAsteroids.roids.world1.drawingMode | face.SORTED ); // Need to sort faces
      u.offset( -u.offset.i, -u.offset.j, -u.offset.k );
      u.rotate( -u.rot.i, -u.rot.j, -u.rot.k );
      double temp = Math.random();
      if ( temp <= 0.33 ) u.mainDirection = 1; // Main direction of motion... X ...
      else if ( temp <= 0.67 ) u.mainDirection = 2; // ... or Y ...
      else u.mainDirection = 3; // ... or Z ...
      u.changeDirection();
      double d2 = rand();
      double d3 = rand();
      int x = JAsteroids.CUBE_SIZE;
      if ( u.mainDirection == 1 ) { // Choose an edge to offset it to
	 if ( u.vel.i > 0 ) u.offset( -x+20, d2*x*2.0, d3*x*2.0 );
	 else u.offset( x-20, d2*x*2.0, d3*x*2.0 );
      } else if ( u.mainDirection == 2 ) {
	 if ( u.vel.j > 0 ) u.offset( d2*x*2.0, -x+20, d3*x*2.0 );
	 else u.offset( d2*x*2.0, x-20, d3*x*2.0 );
      } else {
	 if ( u.vel.k > 0 ) u.offset( d2*x*2.0, d3*x*2.0, -x+20 );
	 else u.offset( d2*x*2.0, d3*x*2.0, x-20 );
      }
      u.transform( u.matrix() ); // Initialize the center of mass correctly
      JAsteroids.roids.addObject( u );
      u.setRotVelocity( 0, 50, 0 );
      u.startMoving();
      if ( JAsteroids.roids.soundIsOn.getState() ) {
	 if ( clip == null ) {
	    clip = (AudioClip) JAsteroids.audioClips.get( "ufo.au" );
	    if ( clip == null ) {
	       clip = AWTUtils.getAudioClip( JAsteroids.roids, "sounds/ufo.au" );
	       if ( clip != null ) JAsteroids.audioClips.put( "ufo.au", clip );
	    }
	 }
	 if ( clip != null ) clip.loop();
      }
      ufoTime = Long.MAX_VALUE;
      return u;
   }

   static final void deleteUFO() {
      currentUFO = null;
   }

   public void move() {
      if ( ! moving ) return;
      super.move();

      asteroid a = asteroidHitBy( this ); // See if we're near an asteroid or the ship
      if ( a != null && ! ( a instanceof ufo ) && ! ( a instanceof torpedo ) ) {
	 a.blowUp(); // blow the asteroid up
	 blowUp(); // and blow the ufo up.
      }

      double val = ( size == 30 ) ? 3000 : 2000;
      if ( System.currentTimeMillis() > lastShot + val ) {
	 fire(); // Fire and change direction more often for smaller/faster UFOs
	 changeDirection(); // Once every 3 or 5 seconds
	 lastShot = System.currentTimeMillis();
      }
   }

   boolean flipBack() { // Dont want to go back to other side of box...want to disappear.
      boolean farOut = ( com.xt > JAsteroids.CUBE_SIZE || com.xt < -JAsteroids.CUBE_SIZE ||
			 com.yt > JAsteroids.CUBE_SIZE || com.yt < -JAsteroids.CUBE_SIZE ||
			 com.zt > JAsteroids.CUBE_SIZE || com.zt < -JAsteroids.CUBE_SIZE );
      if ( farOut ) { // Went out the other side of the box. Get rid of it.
	 JAsteroids.roids.removeObject( this );
	 currentUFO = null;
	 ufoTime = System.currentTimeMillis() + (long) ( 30000.0 * Math.random() + 20000.0 ); // New ship once every 45 sec on average
	 if ( clip != null ) clip.stop();
      }
      return farOut;
   }
   
   void fire() {
      if ( currentUFO == null ) return;
      torpedo torp = new torpedo();
      torp.fromUFO = true; // Flag it as being from a UFO
      torp.setColor( new Color( 0.4f, 0.4f, 1.0f ) ); // Make it bright blue
      torp.offset( offset.i, offset.j, offset.k );
      if ( JAsteroids.roids.ship != null ) {
	 vector so = JAsteroids.roids.ship.offset; // Make it shoot towards, but slightly off from, the ship
	 tempv.set( so.i-offset.i, so.j-offset.j, so.k-offset.k ).normalize();
      }
      torp.setVelocity( tempv.i*200+rand()*150, tempv.j*200+rand()*150, tempv.k*200+rand()*150 );
      JAsteroids.playSound( "ufo-torpedo.au" );
   }

   void changeDirection() {
      if ( currentUFO == null ) return;
      double dd = ( size == 30 ) ? 50 : 80; // Faster for smaller UFOs
      if ( mainDirection == 1 ) setVelocity( dd, rand()*dd, rand()*dd );
      else if ( mainDirection == 2 ) setVelocity( rand()*dd, dd, rand()*dd );
      else setVelocity( rand()*dd*2, rand()*dd, dd );
   }      

   void blowUp() {
      torpedo.createExplosion( 10, com, vel, this.getColor() );
      JAsteroids.roids.removeObject( this );
      currentUFO = null;
      ufoTime = System.currentTimeMillis() + (long) ( 30000.0 * Math.random() + 20000.0 ); // New ship once every 45 sec on average
      if ( clip != null ) clip.stop();
   }

   int score() {
      int out = ( size == 30 ) ? 1000 : 2000; // More valuable for faster/smaller UFOs
      return out;
   }
}
