#include "jmutableimage.h"
#include "jgraphics_util.h"

#include <midpResourceLimit.h>
#include <gxapi_constants.h>
#include <midp_logging.h>

JMutableImage JMutableImage::m_backBuffer;

static inline int resourceCount32(int width, int height)
{
  return width*height*4;
}

extern "C"
{
  /**
  * Initializes the internal members of the native image structure, as required
  * by the platform.
  *
  * @param newImagePtr address of pointer to return created new image
  * @param width width of the image, in pixels.
  * @param height height of the image, in pixels.
  * @param creationErrorPtr pointer for the status of the decoding
  *        process. This function sets creationErrorPtr's value.
  */
  void gxpport_create_mutable(
      gxpport_mutableimage_native_handle* newImagePtr,
      int  width, int height,
      img_native_error_codes* creationErrorPtr)
  {
    // Check Java resource allocation limit
    if (midpCheckResourceLimit(RSC_TYPE_IMAGE_MUT,
                               resourceCount32(width, height)) == 0)
    {
        *creationErrorPtr = IMG_NATIVE_IMAGE_RESOURCE_LIMIT;
        return;
    }
    
    JMutableImage *image = new JMutableImage(width, height);
    if (image == NULL)
    {
      *creationErrorPtr = IMG_NATIVE_IMAGE_OUT_OF_MEMORY_ERROR;
       return;
    }
    
    // Update the resource count
    int ret = midpIncResourceCount(RSC_TYPE_IMAGE_MUT, resourceCount32(width, height));
    if (ret == 0)
      qWarning("Error in increasing resource count for mutable image");
    
    *newImagePtr = image->handle();
    *creationErrorPtr = IMG_NATIVE_IMAGE_NO_ERROR;
  }


  /**
  * Renders the contents of the specified mutable image
  * onto the destination specified.
  *
  * @param srcImagePtr         pointer to source image
  * @param graphicsDestination pointer to destination graphics object
  * @param clip                pointer to structure holding the clip
  * @param x_dest              x-coordinate in the destination
  * @param y_dest              y-coordinate in the destination
  * 
  */
  void gxpport_render_mutableimage(
	  gxpport_mutableimage_native_handle srcImagePtr,
	  gxpport_mutableimage_native_handle graphicsDestination,
	  const jshort *clip,
	  jint x_dest, jint y_dest)
  {
    JMutableImage *srcImage = JMutableImage::fromHandle(srcImagePtr);
    JMutableImage *destImage = JMutableImage::fromHandle(graphicsDestination);
    
    srcImage->flush();
    QPainter *p = destImage->painter(clip);
    p->drawImage(x_dest, y_dest, *srcImage);
  }

  /**
  * Renders the contents of the specified region of this
  * mutable image onto the destination specified.
  *
  * @param srcImagePtr         pointer to source image
  * @param graphicsDestination pointer to destination graphics object
  * @param clip                pointer to structure holding the clip
  * @param x_dest              x-coordinate in the destination
  * @param y_dest              y-coordinate in the destination
  * @param width               width of the region
  * @param height              height of the region
  * @param x_src               x-coord of the region
  * @param y_src               y-coord of the region
  * @param transform           transform to be applied to the region
  *
  */
  void gxpport_render_mutableregion(
      gxpport_mutableimage_native_handle srcImagePtr,
      gxpport_mutableimage_native_handle graphicsDestination,
      const jshort *clip,
      jint x_dest, jint y_dest, 
      jint width, jint height,
      jint x_src, jint y_src,
      jint transform)
  {
    JMutableImage *srcImage = JMutableImage::fromHandle(srcImagePtr);
    JMutableImage *destImage = JMutableImage::fromHandle(graphicsDestination);
    
    srcImage->flush();
    QPainter *p = destImage->painter(clip);
    p->setWorldTransform(transformFromId(transform, width, height));
    p->drawImage(x_dest, y_dest, *srcImage, x_src, y_src, width, height);
    p->resetTransform();
  }

  /**
  * Gets ARGB representation of the specified immutable image
  * @param nativePixmap  pointer to the source image
  * @param rgbBuffer     pointer to buffer to write with the ARGB data
  * @param offset        offset in the buffer at which to start writing
  * @param scanLength    the relative offset within the array
  *                      between corresponding pixels of consecutive rows
  * @param x             x-coordinate of region
  * @param y             y-coordinate of region
  * @param width         width of region
  * @param height        height of region
  * @param errorPtr Error status pointer to the status
  *                 This function sets creationErrorPtr's value.
  */
  void gxpport_get_mutable_argb(
      gxpport_mutableimage_native_handle nativePixmap,
      jint* rgbBuffer, int offset, int scanLength,
      int x, int y, int width, int height,
      img_native_error_codes* errorPtr)
  {
    JMutableImage *image = JMutableImage::fromHandle(nativePixmap);
    image->flush();
    
    uchar *srcBuffer = image->bits();
    int srcScanLength = image->bytesPerLine();
    srcBuffer += srcScanLength*y + x*4; // Advance to src region start
    
    rgbBuffer += offset; // Advance to dest image start  
    size_t lineLength = width * 4;
    
    for (int dy = 0; dy<height; dy++) // dy is y-coordinate relative to region top
    {
      memcpy(rgbBuffer, srcBuffer, lineLength);
      rgbBuffer += scanLength;
      srcBuffer += srcScanLength;
    }
    
    *errorPtr = IMG_NATIVE_IMAGE_NO_ERROR;
  }


  /**
  * Cleans up any native resources to prepare the image to be garbage collected.
  *
  * @param destrImagePtr pointer to the image that needs to be cleaned up
  */
  void gxpport_destroy_mutable(gxpport_mutableimage_native_handle destrImagePtr)
  {
    JMutableImage *image = JMutableImage::fromHandle(destrImagePtr);
    if (image)
    {
      int ret = midpDecResourceCount(RSC_TYPE_IMAGE_MUT, resourceCount32(image->width(), image->height()));
      if (ret == 0)
        qWarning("Error in decreasing resource count for mutable image");
      delete image;
    }
  }
}
