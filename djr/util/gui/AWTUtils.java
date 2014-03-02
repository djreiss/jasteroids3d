package djr.util.gui;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Random;

/**
 * Class <code>AWTUtils</code>
 *
 * @author <a href="mailto:astrodud">astrodud</a>
 * @version 1.1.2
 */
public class AWTUtils {
   public static Image getImage( Object obj, String imgname ) {
      Image out = null;
      InputStream is = obj.getClass().getResourceAsStream( "/" + imgname );
      try {
	 out = Toolkit.getDefaultToolkit().createImage( readAllFromStream( is ) );
      } catch ( IOException e ) {
	 System.err.println( "Could not find image " + imgname + "! " + e );
	 out = null;
      }
      finally { 
	 try { is.close(); } catch (IOException e) { e.printStackTrace(); }
      } 
      return out;
   }

   public static byte[] readAllFromStream(InputStream is) throws IOException {
      DataInputStream dis = new DataInputStream(is);
      byte[] buffer = new byte[4096];
      int totalBytesRead = 0, bytesRead;
      while ((bytesRead = dis.read(buffer, totalBytesRead, buffer.length - 
				   totalBytesRead)) >= 0) {
	 if (totalBytesRead + bytesRead >= buffer.length) {
	    byte[] oldBuffer = buffer;
	    buffer = new byte[oldBuffer.length * 2];
	    System.arraycopy(oldBuffer, 0, buffer, 0, oldBuffer.length);
	 }
	 totalBytesRead += bytesRead;
      }

      byte[] ret = new byte[totalBytesRead];
      System.arraycopy(buffer, 0, ret, 0, totalBytesRead);
      return ret;
   }

   public static boolean waitForImage(Image image, Component component) {
      try {
	 MediaTracker mediaTracker = new MediaTracker(component);
	 int i = new Random().nextInt();
	 mediaTracker.addImage(image, i);
	 mediaTracker.waitForID(i);
	 return true;
      }
      catch (InterruptedException e) {
	 return false;
      }
   }

   public static java.applet.AudioClip getAudioClip( java.applet.Applet obj, 
						     String name ) {
      java.applet.AudioClip clip = null;
      if ( name == null ) return null;
      try {
	 URL url = obj.getClass().getResource( "/" + name );
	 if ( url == null ) {
	    //System.err.println( "no resource:" + "/" + name );
	    return null;
	 }
	 clip = obj.getAudioClip( url );
      } catch ( Exception e ) {
	 //System.err.println( "audio read of '" + name + "': " + e );
	 clip = null;
      } 
      if ( clip == null ) {
	 try {
	    URL url = obj.getClass().getResource( "/" + name );
	    if ( url == null ) {
	       System.err.println( "no resource:" + "/" + name );
	       return null;
	    }
	    clip = java.applet.Applet.newAudioClip( url );
	 } catch ( Exception e ) {
	    //System.err.println( "audio read of '" + name + "' 2: " + e );
	    clip = null;
	 } 
      }
      return clip;
   }
}
