package org.eclipse.swt.graphics;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */
 
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.*;
import java.io.*;
 
/**
 * Instances of this class are graphics which have been prepared
 * for display on a specific device. That is, they are ready
 * to paint using methods such as <code>GC.drawImage()</code>
 * and display on widgets with, for example, <code>Button.setImage()</code>.
 * <p>
 * If loaded from a file format that supports it, an
 * <code>Image</code> may have transparency, meaning that certain
 * pixels are specified as being transparent when drawn. Examples
 * of file formats that support transparency are GIF and PNG.
 * </p><p>
 * There are two primary ways to use <code>Images</code>. 
 * The first is to load a graphic file from disk and create an
 * <code>Image</code> from it. This is done using an <code>Image</code>
 * constructor, for example:
 * <pre>
 *    Image i = new Image(device, "C:\\graphic.bmp");
 * </pre>
 * A graphic file may contain a color table specifying which
 * colors the image was intended to possess. In the above example,
 * these colors will be mapped to the closest available color in
 * SWT. It is possible to get more control over the mapping of
 * colors as the image is being created, using code of the form:
 * <pre>
 *    ImageData data = new ImageData("C:\\graphic.bmp"); 
 *    RGB[] rgbs = data.getRGBs(); 
 *    // At this point, rgbs contains specifications of all
 *    // the colors contained within this image. You may
 *    // allocate as many of these colors as you wish by
 *    // using the Color constructor Color(RGB), then
 *    // create the image:
 *    Image i = new Image(device, data);
 * </pre>
 * <p>
 * Applications which require even greater control over the image
 * loading process should use the support provided in class
 * <code>ImageLoader</code>.
 * </p><p>
 * Application code must explicitely invoke the <code>Image.dispose()</code> 
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 *
 * @see Color
 * @see ImageData
 * @see ImageLoader
 */
public final class Image implements Drawable{

	/**
	 * specifies whether the receiver is a bitmap or an icon
	 * (one of <code>SWT.BITMAP</code>, <code>SWT.ICON</code>)
	 */
	public int type;
	
	/**
	 * The handle to the OS pixmap resource.
	 * Warning: This field is platform dependent.
	 */
	public int pixmap;
	
	/**
	 * The handle to the OS mask resource.
	 * Warning: This field is platform dependent.
	 */
	public int mask;

	/**
	 * The device where this image was created.
	 */
	Device device;
	
	/**
	 * specifies the transparent pixel
	 * (Warning: This field is platform dependent)
	 */
	int transparentPixel = -1;
	
	/**
	 * The GC the image is currently selected in.
	 * Warning: This field is platform dependent.
	 */
	GC memGC;

	/**
	 * The alpha data of the image.
	 * Warning: This field is platform dependent.
	 */
	byte[] alphaData;
	
	/**
	 * The global alpha value to be used for every pixel.
	 * Warning: This field is platform dependent.
	 */
	int alpha = -1;
	
	/**
	 * Specifies the default scanline padding.
	 * Warning: This field is platform dependent.
	 */
	static final int DEFAULT_SCANLINE_PAD = 4;

Image() {
}

/**
 * Constructs an empty instance of this class with the
 * specified width and height. The result may be drawn upon
 * by creating a GC and using any of its drawing operations,
 * as shown in the following example:
 * <pre>
 *    Image i = new Image(device, width, height);
 *    GC gc = new GC(i);
 *    gc.drawRectangle(0, 0, 50, 50);
 *    gc.dispose();
 * </pre>
 * <p>
 * Note: Some platforms may have a limitation on the size
 * of image that can be created (size depends on width, height,
 * and depth). For example, Windows 95, 98, and ME do not allow
 * images larger than 16M.
 * </p>
 *
 * @param device the device on which to create the image
 * @param width the width of the new image
 * @param height the height of the new image
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if either the width or height is negative</li>
 * </ul>
 */
public Image(Device display, int width, int height) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, width, height);
}

/**
 * Constructs a new instance of this class based on the
 * provided image, with an appearance that varies depending
 * on the value of the flag. The possible flag values are:
 * <dl>
 * <dt><b>IMAGE_COPY</b></dt>
 * <dd>the result is an identical copy of srcImage</dd>
 * <dt><b>IMAGE_DISABLE</b></dt>
 * <dd>the result is a copy of srcImage which has a <em>disabled</em> look</dd>
 * <dt><b>IMAGE_GRAY</b></dt>
 * <dd>the result is a copy of srcImage which has a <em>gray scale</em> look</dd>
 * </dl>
 *
 * @param device the device on which to create the image
 * @param srcImage the image to use as the source
 * @param flag the style, either <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code>
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if srcImage is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the flag is not one of <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code></li>
 *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon, or
 *          is otherwise in an invalid state</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 */
public Image(Device device, Image srcImage, int flag) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (srcImage == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (srcImage.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	switch (flag) {
		case SWT.IMAGE_COPY:
		case SWT.IMAGE_DISABLE:
		case SWT.IMAGE_GRAY:
			break;
		default:
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	this.device = device;
	this.type = srcImage.type;

	/* Get source image size */
	int[] w = new int[1], h = new int[1];
 	OS.gdk_drawable_get_size(srcImage.pixmap, w, h);
 	int width = w[0];
 	int height = h[0];
 	
 	/* Copy the mask */
	if (srcImage.mask != 0 || srcImage.transparentPixel != -1) {
		/* Generate the mask if necessary. */
		if (srcImage.transparentPixel != -1) srcImage.createMask();
		int mask = OS.gdk_pixmap_new(0, width, height, 1);
		if (mask == 0) SWT.error(SWT.ERROR_NO_HANDLES);
		int gdkGC = OS.gdk_gc_new(mask);
		if (gdkGC == 0) SWT.error(SWT.ERROR_NO_HANDLES);
		OS.gdk_draw_drawable(mask, gdkGC, srcImage.mask, 0, 0, 0, 0, width, height);
		OS.g_object_unref(gdkGC);
		this.mask = mask;
		/* Destroy the image mask if the there is a GC created on the image */
		if (srcImage.transparentPixel != -1 && srcImage.memGC != null) srcImage.destroyMask();
	}

	/* Copy transparent pixel and alpha data when necessary */
	if (flag != SWT.IMAGE_DISABLE) {
		transparentPixel = srcImage.transparentPixel;
		alpha = srcImage.alpha;
		if (srcImage.alphaData != null) {
			alphaData = new byte[srcImage.alphaData.length];
			System.arraycopy(srcImage.alphaData, 0, alphaData, 0, alphaData.length);
		}
	}

	/* Create the new pixmap */
	int pixmap = OS.gdk_pixmap_new (OS.GDK_ROOT_PARENT(), width, height, -1);
	if (pixmap == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	int gdkGC = OS.gdk_gc_new(pixmap);
	if (gdkGC == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	this.pixmap = pixmap;
	
	if (flag == SWT.IMAGE_COPY) {
		OS.gdk_draw_drawable(pixmap, gdkGC, srcImage.pixmap, 0, 0, 0, 0, width, height);
		OS.g_object_unref(gdkGC);
		return;
	}
	
	/* Retrieve the source pixmap data */
	int pixbuf = OS.gdk_pixbuf_new(OS.GDK_COLORSPACE_RGB, false, 8, width, height);
	if (pixbuf == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	int colormap = OS.gdk_colormap_get_system();
	OS.gdk_pixbuf_get_from_drawable(pixbuf, srcImage.pixmap, colormap, 0, 0, 0, 0, width, height);
	int stride = OS.gdk_pixbuf_get_rowstride(pixbuf);
	int pixels = OS.gdk_pixbuf_get_pixels(pixbuf);

	/* Apply transformation */
	switch (flag) {
		case SWT.IMAGE_DISABLE: {
			Color zeroColor = device.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
			RGB zeroRGB = zeroColor.getRGB();
			byte zeroRed = (byte)zeroRGB.red;
			byte zeroGreen = (byte)zeroRGB.green;
			byte zeroBlue = (byte)zeroRGB.blue;
			Color oneColor = device.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			RGB oneRGB = oneColor.getRGB();
			byte oneRed = (byte)oneRGB.red;
			byte oneGreen = (byte)oneRGB.green;
			byte oneBlue = (byte)oneRGB.blue;
			byte[] line = new byte[stride];
			for (int y=0; y<height; y++) {
				OS.memmove(line, pixels + (y * stride), stride);
				for (int x=0; x<width; x++) {
					int offset = x*3;
					int red = line[offset] & 0xFF;
					int green = line[offset+1] & 0xFF;
					int blue = line[offset+2] & 0xFF;
					int intensity = red * red + green * green + blue * blue;
					if (intensity < 98304) {
						line[offset] = zeroRed;
						line[offset+1] = zeroGreen;
						line[offset+2] = zeroBlue;
					} else {
						line[offset] = oneRed;
						line[offset+1] = oneGreen;
						line[offset+2] = oneBlue;
					}
				}
				OS.memmove(pixels + (y * stride), line, stride);
			}
			break;
		}
		case SWT.IMAGE_GRAY: {			
			byte[] line = new byte[stride];
			for (int y=0; y<height; y++) {
				OS.memmove(line, pixels + (y * stride), stride);
				for (int x=0; x<width; x++) {
					int offset = x*3;
					int red = line[offset] & 0xFF;
					int green = line[offset+1] & 0xFF;
					int blue = line[offset+2] & 0xFF;
					byte intensity = (byte)((red+red+green+green+green+green+green+blue) >> 3);
					line[offset] = line[offset+1] = line[offset+2] = intensity;
				}
				OS.memmove(pixels + (y * stride), line, stride);
			}
			transparentPixel = srcImage.transparentPixel;
			alpha = srcImage.alpha;
			if (srcImage.alphaData != null) {
				alphaData = new byte[srcImage.alphaData.length];
				System.arraycopy(srcImage.alphaData, 0, alphaData, 0, alphaData.length);
			}
			break;
		}
	}

	/* Copy data back to destination pixmap */
	OS.gdk_pixbuf_render_to_drawable(pixbuf, pixmap, gdkGC, 0, 0, 0, 0, width, height, OS.GDK_RGB_DITHER_NORMAL, 0, 0);
	
	/* Free resources */
	OS.g_object_unref(pixbuf);
	OS.g_object_unref(gdkGC);
}

/**
 * Constructs an empty instance of this class with the
 * width and height of the specified rectangle. The result
 * may be drawn upon by creating a GC and using any of its
 * drawing operations, as shown in the following example:
 * <pre>
 *    Image i = new Image(device, boundsRectangle);
 *    GC gc = new GC(i);
 *    gc.drawRectangle(0, 0, 50, 50);
 *    gc.dispose();
 * </pre>
 * <p>
 * Note: Some platforms may have a limitation on the size
 * of image that can be created (size depends on width, height,
 * and depth). For example, Windows 95, 98, and ME do not allow
 * images larger than 16M.
 * </p>
 *
 * @param device the device on which to create the image
 * @param bounds a rectangle specifying the image's width and height (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the bounds rectangle is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if either the rectangle's width or height is negative</li>
 * </ul>
 */
public Image(Device display, Rectangle bounds) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (bounds == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, bounds.width, bounds.height);
}

/**
 * Constructs an instance of this class from the given
 * <code>ImageData</code>.
 *
 * @param device the device on which to create the image
 * @param data the image data to create the image from (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the image data is null</li>
 * </ul>
 */
public Image(Device device, ImageData data) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, data);
}

/**
 * Constructs an instance of this class, whose type is 
 * <code>SWT.ICON</code>, from the two given <code>ImageData</code>
 * objects. The two images must be the same size, and the mask image
 * must have a color depth of 1. Pixel transparency in either image
 * will be ignored. If either image is an icon to begin with, an
 * exception is thrown.
 * <p>
 * The mask image should contain white wherever the icon is to be visible,
 * and black wherever the icon is to be transparent. In addition,
 * the source image should contain black wherever the icon is to be
 * transparent.
 * </p>
 *
 * @param device the device on which to create the icon
 * @param source the color data for the icon
 * @param mask the mask data for the icon
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if either the source or mask is null </li>
 *    <li>ERROR_INVALID_ARGUMENT - if source and mask are different sizes or
 *          if the mask is not monochrome, or if either the source or mask
 *          is already an icon</li>
 * </ul>
 */
public Image(Device display, ImageData source, ImageData mask) {
	if (device == null) device = Device.getDevice();
	if (source == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (mask == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (source.width != mask.width || source.height != mask.height) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	if (mask.depth != 1) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	ImageData image = new ImageData(source.width, source.height, source.depth, source.palette, source.scanlinePad, source.data);
	image.maskPad = mask.scanlinePad;
	image.maskData = mask.data;
	init(device, image);
}

/**
 * Constructs an instance of this class by loading its representation
 * from the specified input stream. Throws an error if an error
 * occurs while loading the image, or if the result is an image
 * of an unsupported type.
 * <p>
 * This constructor is provided for convenience when loading a single
 * image only. If the stream contains multiple images, only the first
 * one will be loaded. To load multiple images, use 
 * <code>ImageLoader.load()</code>.
 * </p><p>
 * This constructor may be used to load a resource as follows:
 * </p>
 * <pre>
 *     new Image(device, clazz.getResourceAsStream("file.gif"));
 * </pre>
 *
 * @param device the device on which to create the image
 * @param stream the input stream to load the image from
 *
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
 *    <li>ERROR_IO - if an IO error occurs while reading data</li>
 * </ul>
 */
public Image(Device device, InputStream stream) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, new ImageData(stream));
}

/**
 * Constructs an instance of this class by loading its representation
 * from the file with the specified name. Throws an error if an error
 * occurs while loading the image, or if the result is an image
 * of an unsupported type.
 * <p>
 * This constructor is provided for convenience when loading
 * a single image only. If the specified file contains
 * multiple images, only the first one will be used.
 *
 * @param device the device on which to create the image
 * @param filename the name of the file to load the image from
 *
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
 *    <li>ERROR_IO - if an IO error occurs while reading data</li>
 * </ul>
 */
public Image(Device display, String filename) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, new ImageData(filename));
}

/**
 * Create the receiver's mask if necessary.
 */
void createMask() {
	if (mask != 0) return;
	ImageData maskImage = getImageData().getTransparencyMask();
	byte[] maskData = maskImage.data;
	for (int i = 0; i < maskData.length; i++) {
		byte s = maskData[i];
		maskData[i] = (byte)(((s & 0x80) >> 7) | ((s & 0x40) >> 5) |
			((s & 0x20) >> 3) | ((s & 0x10) >> 1) | ((s & 0x08) << 1) |
			((s & 0x04) << 3) | ((s & 0x02) << 5) |	((s & 0x01) << 7));
	}
	int mask = OS.gdk_bitmap_create_from_data(0, maskData, maskImage.bytesPerLine * 8, maskImage.height);
	if (mask == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	this.mask = mask;
}

/**
 * Destroy the receiver's mask if it exists.
 */
void destroyMask() {
	if (mask == 0) return;
	OS.g_object_unref(mask);
	mask = 0;
}

/**
 * Disposes of the operating system resources associated with
 * the image. Applications must dispose of all images which
 * they allocate.
 */
public void dispose () {
	if (pixmap == 0) return;
	if (device.isDisposed()) return;
	if (pixmap != 0) OS.g_object_unref(pixmap);
	if (mask != 0) OS.g_object_unref(mask);
	device = null;
	pixmap = mask = 0;
	memGC = null;
}

/**
 * Compares the argument to the receiver, and returns true
 * if they represent the <em>same</em> object using a class
 * specific comparison.
 *
 * @param object the object to compare with this object
 * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
 *
 * @see #hashCode
 */
public boolean equals (Object object) {
	if (object == this) return true;
	if (!(object instanceof Image)) return false;
	Image image = (Image)object;
	return device == image.device && pixmap == image.pixmap &&
		transparentPixel == image.transparentPixel &&
		mask == image.mask;
}

/**
 * Returns the color to which to map the transparent pixel, or null if
 * the receiver has no transparent pixel.
 * <p>
 * There are certain uses of Images that do not support transparency
 * (for example, setting an image into a button or label). In these cases,
 * it may be desired to simulate transparency by using the background
 * color of the widget to paint the transparent pixels of the image.
 * Use this method to check which color will be used in these cases
 * in place of transparency. This value may be set with setBackground().
 * <p>
 *
 * @return the background color of the image, or null if there is no transparency in the image
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Color getBackground() {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (transparentPixel == -1) return null;
	//NOT DONE
	return null;
}

/**
 * Returns the bounds of the receiver. The rectangle will always
 * have x and y values of 0, and the width and height of the
 * image.
 *
 * @return a rectangle specifying the image's bounds
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
 * </ul>
 */
public Rectangle getBounds() {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	int[] width = new int[1]; int[] height = new int[1];
 	OS.gdk_drawable_get_size(pixmap, width, height);
	return new Rectangle(0, 0, width[0], height[0]);
}

/**
 * Returns an <code>ImageData</code> based on the receiver
 * Modifications made to this <code>ImageData</code> will not
 * affect the Image.
 *
 * @return an <code>ImageData</code> containing the image's data and attributes
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
 * </ul>
 *
 * @see ImageData
 */
public ImageData getImageData() {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);

	int[] w = new int[1], h = new int[1];
 	OS.gdk_drawable_get_size(pixmap, w, h);
 	int width = w[0], height = h[0]; 	
	int pixbuf = OS.gdk_pixbuf_new(OS.GDK_COLORSPACE_RGB, false, 8, width, height);
	if (pixbuf == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	int colormap = OS.gdk_colormap_get_system();
	OS.gdk_pixbuf_get_from_drawable(pixbuf, pixmap, colormap, 0, 0, 0, 0, width, height);
	int stride = OS.gdk_pixbuf_get_rowstride(pixbuf);
	int pixels = OS.gdk_pixbuf_get_pixels(pixbuf);
	byte[] srcData = new byte[stride * height];
	OS.memmove(srcData, pixels, srcData.length);
	OS.g_object_unref(pixbuf);

	PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
	ImageData data = new ImageData(width, height, 24, palette);
	data.data = srcData;
	data.bytesPerLine = stride;

	if (transparentPixel == -1 && type == SWT.ICON && mask != 0) {
		/* Get the icon mask data */
		int gdkImagePtr = OS.gdk_drawable_get_image(mask, 0, 0, width, height);
		if (gdkImagePtr == 0) SWT.error(SWT.ERROR_NO_HANDLES);
		GdkImage gdkImage = new GdkImage();
		OS.memmove(gdkImage, gdkImagePtr);
		byte[] maskData = new byte[gdkImage.bpl * height];
		OS.memmove(maskData, gdkImage.mem, maskData.length);
		OS.g_object_unref(gdkImagePtr);

		data.maskPad = 4;
		data.maskData = maskData;
		/* Bit swap the mask data if necessary */
		if (gdkImage.byte_order == OS.GDK_LSB_FIRST) {
			for (int i = 0; i < maskData.length; i++) {
				byte b = maskData[i];
				maskData[i] = (byte)(((b & 0x01) << 7) | ((b & 0x02) << 5) | 
					((b & 0x04) << 3) |	((b & 0x08) << 1) | ((b & 0x10) >> 1) | 
					((b & 0x20) >> 3) |	((b & 0x40) >> 5) | ((b & 0x80) >> 7));
			}
		}
	}
	data.transparentPixel = transparentPixel;
	data.alpha = alpha;
	if (alpha == -1 && alphaData != null) {
		data.alphaData = new byte[alphaData.length];
		System.arraycopy(alphaData, 0, data.alphaData, 0, alphaData.length);
	}
	return data;
}

/**	 
 * Invokes platform specific functionality to allocate a new image.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Image</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param device the device on which to allocate the color
 * @param type the type of the image (<code>SWT.BITMAP</code> or <code>SWT.ICON</code>)
 * @param pixmap the OS handle for the image
 * @param mask the OS handle for the image mask
 *
 * @private
 */
public static Image gtk_new(Device device, int type, int pixmap, int mask) {
	if (device == null) device = Device.getDevice();
	Image image = new Image();
	image.type = type;
	image.pixmap = pixmap;
	image.mask = mask;
	image.device = device;
	return image;
}

/**
 * Returns an integer hash code for the receiver. Any two 
 * objects which return <code>true</code> when passed to 
 * <code>equals</code> must return the same value for this
 * method.
 *
 * @return the receiver's hash
 *
 * @see #equals
 */
public int hashCode () {
	return pixmap;
}

void init(Device device, int width, int height) {
	if (width <= 0 || height <= 0) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}
	this.device = device;
	this.type = SWT.BITMAP;

	/* Create the pixmap */
	this.pixmap = OS.gdk_pixmap_new(OS.GDK_ROOT_PARENT(), width, height, -1);
	if (pixmap == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	/* Fill the bitmap with white */
	GdkColor white = new GdkColor();
	white.red = (short)0xFFFF;
	white.green = (short)0xFFFF;
	white.blue = (short)0xFFFF;
	int colormap = OS.gdk_colormap_get_system();
	OS.gdk_colormap_alloc_color(colormap, white, true, true);
	int gdkGC = OS.gdk_gc_new(pixmap);
	OS.gdk_gc_set_foreground(gdkGC, white);
	OS.gdk_draw_rectangle(pixmap, gdkGC, 1, 0, 0, width, height);
	OS.g_object_unref(gdkGC);
	OS.gdk_colormap_free_colors(colormap, white, 1);
}

void init(Device device, ImageData image) {
	if (image == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	this.device = device;
	int width = image.width;
	int height = image.height;
	PaletteData palette = image.palette;
	int pixbuf = OS.gdk_pixbuf_new(OS.GDK_COLORSPACE_RGB, false, 8, width, height);
	if (pixbuf == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	int stride = OS.gdk_pixbuf_get_rowstride(pixbuf);
	int data = OS.gdk_pixbuf_get_pixels(pixbuf);
	if (palette.isDirect) {
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;
		int[] pixels = new int[width];
		byte[] rgbPixels = new byte[stride];
		for (int y=0; y<height; y++) {
			image.getPixels(0, y, width, pixels, 0);
			for (int x=0; x<width; x++) {
				int pixel = pixels[x];
				int offset = x*3;
				int r = pixel & redMask;
				r = (redShift < 0) ? r >>> -redShift : r << redShift;
				int g = pixel & greenMask;
				g = (greenShift < 0) ? g >>> -greenShift : g << greenShift;
				int b = pixel & blueMask;
				b = (blueShift < 0) ? b >>> -blueShift : b << blueShift;
				rgbPixels[offset] = (byte)r;
				rgbPixels[offset + 1] = (byte)g;
				rgbPixels[offset + 2] = (byte)b;
			}
			OS.memmove(data + (stride * y), rgbPixels, rgbPixels.length);
		}
	} else {
		RGB[] rgbs = palette.colors;
		byte[] pixels = new byte[width];
		byte[] rgbPixels = new byte[stride];
		for (int y=0; y<height; y++) {
			image.getPixels(0, y, width, pixels, 0);
			for (int x=0; x<width; x++) {
				int pixel = pixels[x] & 0xFF;
				int r = 0, g = 0, b = 0;
				if (pixel < rgbs.length) {
					RGB rgb = rgbs[pixel];
					r = rgb.red; g = rgb.green; b = rgb.blue;
				}
				int offset = x*3;
				rgbPixels[offset] = (byte)r;
				rgbPixels[offset + 1] = (byte)g;
				rgbPixels[offset + 2] = (byte)b;
			}
			OS.memmove(data + (stride * y), rgbPixels, rgbPixels.length);
		}
	}
	int pixmap = OS.gdk_pixmap_new (OS.GDK_ROOT_PARENT(), width, height, -1);
	if (pixmap == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	int gdkGC = OS.gdk_gc_new(pixmap);
	if (gdkGC == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	OS.gdk_pixbuf_render_to_drawable(pixbuf, pixmap, gdkGC, 0, 0, 0, 0, width, height, OS.GDK_RGB_DITHER_NORMAL, 0, 0);
	OS.g_object_unref(gdkGC);
	OS.g_object_unref(pixbuf);
	
	if (image.getTransparencyType() == SWT.TRANSPARENCY_MASK || image.transparentPixel != -1) {
		if (image.transparentPixel != -1) {
			RGB rgb = null;
			if (palette.isDirect) {
				rgb = palette.getRGB(image.transparentPixel);
			} else {
				if (image.transparentPixel < palette.colors.length) {
					rgb = palette.getRGB(image.transparentPixel);
				}
			}
			if (rgb != null) {
				transparentPixel = rgb.red << 16 | rgb.green << 8 | rgb.blue;
			}
		}
		ImageData maskImage = image.getTransparencyMask();
		byte[] maskData = maskImage.data;
		for (int i = 0; i < maskData.length; i++) {
			byte s = maskData[i];
			maskData[i] = (byte)(((s & 0x80) >> 7) | ((s & 0x40) >> 5) |
				((s & 0x20) >> 3) | ((s & 0x10) >> 1) | ((s & 0x08) << 1) |
				((s & 0x04) << 3) | ((s & 0x02) << 5) |	((s & 0x01) << 7));
		}
		int mask = OS.gdk_bitmap_create_from_data(0, maskData, maskImage.bytesPerLine * 8 , height);
		if (mask == 0) SWT.error(SWT.ERROR_NO_HANDLES);
		this.mask = mask;
		if (image.getTransparencyType() == SWT.TRANSPARENCY_MASK) {
			this.type = SWT.ICON;
		} else {
			this.type = SWT.BITMAP;
		}
	} else {
		this.type = SWT.BITMAP;
		this.mask = 0;
		this.alpha = image.alpha;
		if (image.alpha == -1 && image.alphaData != null) {
			this.alphaData = new byte[image.alphaData.length];
			System.arraycopy(image.alphaData, 0, this.alphaData, 0, alphaData.length);
		}
	}
	this.pixmap = pixmap;
}

/**	 
 * Invokes platform specific functionality to allocate a new GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Image</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param data the platform specific GC data 
 * @return the platform specific GC handle
 *
 * @private
 */
public int internal_new_GC (GCData data) {
	if (pixmap == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (type != SWT.BITMAP || memGC != null) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	int gdkGC = OS.gdk_gc_new(pixmap);
	if (data != null) {
		data.device = device;
		data.drawable = pixmap;
		data.font = device.systemFont.handle;
		data.image = this;
	}
	return gdkGC;
}

/**	 
 * Invokes platform specific functionality to dispose a GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Image</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param handle the platform specific GC handle
 * @param data the platform specific GC data 
 *
 * @private
 */
public void internal_dispose_GC (int gdkGC, GCData data) {
	OS.g_object_unref(gdkGC);
}

/**
 * Returns <code>true</code> if the image has been disposed,
 * and <code>false</code> otherwise.
 * <p>
 * This method gets the dispose state for the image.
 * When an image has been disposed, it is an error to
 * invoke any other method using the image.
 *
 * @return <code>true</code> when the image is disposed and <code>false</code> otherwise
 */
public boolean isDisposed() {
	return pixmap == 0;
}

/**
 * Sets the color to which to map the transparent pixel.
 * <p>
 * There are certain uses of <code>Images</code> that do not support
 * transparency (for example, setting an image into a button or label).
 * In these cases, it may be desired to simulate transparency by using
 * the background color of the widget to paint the transparent pixels
 * of the image. This method specifies the color that will be used in
 * these cases. For example:
 * <pre>
 *    Button b = new Button();
 *    image.setBackground(b.getBackground());>
 *    b.setImage(image);
 * </pre>
 * </p><p>
 * The image may be modified by this operation (in effect, the
 * transparent regions may be filled with the supplied color).  Hence
 * this operation is not reversible and it is not legal to call
 * this function twice or with a null argument.
 * </p><p>
 * This method has no effect if the receiver does not have a transparent
 * pixel value.
 * </p>
 *
 * @param color the color to use when a transparent pixel is specified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the color has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setBackground(Color color) {
	if (isDisposed()) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (color == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (color.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	if (transparentPixel == -1) return;
	//NOT DONE
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the receiver
 */
public String toString () {
	if (isDisposed()) return "Image {*DISPOSED*}";
	return "Image {" + pixmap + "}";
}

}
