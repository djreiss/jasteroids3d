package djr.d3d.anim;
import java.awt.Image;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

/**
  * This class allows to split an image in as many parts as the user decides,
  * all of them with the same size.
  *
  * @author (c) LuisM Pena, August-1997.
  * @version 1.1.2
  */
public class SplitImage implements ImageObserver
{
	/**
	  * This class splits an image in different parts.
	  * @param image the image to split
	  * @param rows number of images per column. It has to be greater than 0
	  * @param columns number of images per row. It has to be greater than 0
	  */
	public SplitImage(Image image, int rows, int columns)
	{
		this.rows=rows;
		this.columns=columns;
		this.image=image;
		
		//arguments checking
		if (rows<1 || columns<1)
			error=readen=true;
		else
		{
			height=image.getHeight(this);
			width=image.getWidth(this);
			checkIfStart();
		}
	}
	
	/**
	  * Gives the height of each of the splitted images.
	  * @return the height of each of the splitted images, or -1 if there has been
	  * an error (this error may have been produced in the initialization, if the
	  * number of rows of columns was not valid).
	  * @exception InterruptedException
	  */
	public synchronized int getHeight() throws InterruptedException
	{
		while (!readen)
			wait();
		return finalHeight;
	}
	
	/**
	  * Gives the width of each of the splitted images.
	  * @return the width of each of the splitted images, or -1 if there has been
	  * an error (this error may have been produced in the initialization, if the
	  * number of rows of columns was not valid).
	  * @exception InterruptedException
	  */
	public synchronized int getWidth() throws InterruptedException
	{
		while (!readen)
			wait();
		return finalWidth;
	}
	
	/**
	  * Generates an imageProducer for the selected portion of the image.
	  * @return the ImageProducer generated, or null if there has been an error.
	  * The error may have been generated because (1) bad initialization, wrong number
	  * of rows or columns (2) error reading the image (3) wrong parameter in the call
	  * to the method, row and/or col are not valids
	  * @param row the row of the portion. It must be a number between 0 and the number of rows - 1
	  * @param col the column of the portion. It must be a number between 0 and the number of columns - 1
	  * @exception InterruptedException
	  */
	public synchronized ImageProducer getImageProducer(int row, int col) throws InterruptedException
	{
		while (!readen)
			wait();
		ImageProducer imageProducer=null;
		if (!error && row>=0 && col>=0 && row<rows && col < columns)
			imageProducer=new FilteredImageSource(image.getSource(),
							      new CropImageFilter(col*finalWidth,row*finalHeight, finalWidth, finalHeight));

		return imageProducer;
	}

	/**
	  *	ImageObserver Method
	  */
	public synchronized boolean imageUpdate(Image img, int flags, int x, int y, int w, int h)
	{
		boolean ret;
		if ((flags&ImageObserver.ERROR)!=0)
		{
			ret=false;
			error=readen=true;
			notifyAll();
		}
		else
		{
			if ((flags&ImageObserver.WIDTH)!=0)
				width=w;
			if ((flags&ImageObserver.HEIGHT)!=0)
				height=h;
			ret=!checkIfStart();
		}
		return ret;
	}
	
	/**
	  * Checks if the height and the width are already available, and -if so-, it
	  * creates the needed filters;
	  * @return true if the checking has been positive
	  */
	synchronized boolean checkIfStart()
	{
		boolean ret=width!=-1 && height!=-1;
		if (ret)
		{
			finalWidth=width/columns;
			finalHeight=height/rows;
			readen=true;
			notifyAll();
		}
		return ret;
	}
	
	/**
	  * Identifies any error given by the Image Producer
	  */
	boolean error=false;
	/**
	  * Identifies when the image has been readed (in fact, when the height and
	  * width are known)
	  */
	boolean readen=false;
	/**
	  * The image being splitting
	  */
	Image image;
	/**
	  * Number of rows and columns used to split the image
	  */
	int rows, columns;
	/**
	  * Dimensions of the original image
	  */
	int width=-1, height=-1;
	/**
	  * Dimensions of the final image
	  */
	public int finalWidth=-1, finalHeight=-1;
}
