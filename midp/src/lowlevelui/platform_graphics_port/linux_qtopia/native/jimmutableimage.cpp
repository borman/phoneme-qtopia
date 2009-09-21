#include "jimmutableimage.h"
#include "jgraphics_util.h"

#include <midpResourceLimit.h>
#include <gxapi_constants.h>
#include <midp_logging.h>
#include <imgdcd_image_util.h>
#include <img_errorcodes.h>

#include <QDebug>

static inline int resourceCount32(int width, int height)
{
  return width*height*4;
}

static inline JIMMutableImage *stubImage(int width, int height)
{
  JIMMutableImage *img = new JIMMutableImage(width, height);
  img->fill(Qt::yellow);
  return img;
}

static inline bool resourcesAvailable(int width, int height)
{
  return (midpCheckResourceLimit(RSC_TYPE_IMAGE_IMMUT, resourceCount32(width, height)) != 0);
}

void JIMMutableImage::allocResources()
{
  m_midpResCount = resourceCount32(width(), height());
  int ret = midpIncResourceCount(RSC_TYPE_IMAGE_IMMUT, m_midpResCount);
  qDebug("[IMM] Res +%8d: %d", m_midpResCount, ret);
}

void JIMMutableImage::freeResources()
{
  int ret = midpDecResourceCount(RSC_TYPE_IMAGE_IMMUT, m_midpResCount);
  qDebug("[IMM] Res -%8d: %d", m_midpResCount, ret);
}

#define debug_trace() //qDebug("TRACE: %s", __func__)

extern "C"
{
  /**
  * Creates a copy of the specified mutable image
  *
  * @param srcMutableImage   pointer to source image to copy
  * @param newImmutableImage address of pointer to return created new image
  * @param creationErrorPtr  pointer to error status.
  *                          This function sets creationErrorPtr's value.
  *
  */
  void gxpport_createimmutable_from_mutable
  (gxpport_mutableimage_native_handle srcMutableImage,
  gxpport_image_native_handle* newImmutableImage,
  img_native_error_codes* creationErrorPtr)
  { debug_trace();
    JMutableImage *src = JMutableImage::fromHandle(srcMutableImage);
    Q_ASSERT(src != NULL);
    src->flush();
    
    /* Check resource limit before copying */
    if (!resourcesAvailable(src->width(), src->height()))
    {
      /* Exceeds resource limit */
      *creationErrorPtr  = IMG_NATIVE_IMAGE_RESOURCE_LIMIT;
      return;
    }
    JIMMutableImage *dest = new JIMMutableImage(*src);
    if (!dest)
    {
      *creationErrorPtr = IMG_NATIVE_IMAGE_OUT_OF_MEMORY_ERROR;
      return;
    }
    
    *creationErrorPtr = IMG_NATIVE_IMAGE_NO_ERROR;
    *newImmutableImage = dest->handle();
  }

  /**
  * Creates an immutable image that is a copy of a region
  * of the specified immutable image.
  *
  * @param srcImmutableImage pointer to source image
  * @param newImmutableImage address of pointer to return created new image
  * @param src_x      x-coord of the region
  * @param src_y      y-coord of the region
  * @param src_width  width of the region
  * @param src_height height of the region
  * @param transform  transform to be applied to the region
  * @param creationErrorPtr  pointer to error status.
  *                          This function sets creationErrorPtr's value.
  *
  */
  void
  gxpport_createimmutable_from_immutableregion
  (gxpport_image_native_handle srcImmutableImage,
  int src_x, int src_y, 
  int src_width, int src_height,
  int transform,
  gxpport_image_native_handle* newImmutableImage,
  img_native_error_codes* creationErrorPtr)
  { debug_trace();
    JIMMutableImage *src = JIMMutableImage::fromHandle(srcImmutableImage);
    Q_ASSERT(src != NULL);
    
    int dest_width, dest_height;
    if (transformIsFlipping(transform))
    {
      dest_width = src_width;
      dest_height = src_height;
    }
    
    /* Check resource limit before copying */
    if (!resourcesAvailable(dest_width, dest_height))
    {
      /* Exceeds resource limit */
      *creationErrorPtr  = IMG_NATIVE_IMAGE_RESOURCE_LIMIT;
      return;
    }
    JIMMutableImage *dest = new JIMMutableImage(dest_width, dest_height);
    if (!dest)
    {
      *creationErrorPtr = IMG_NATIVE_IMAGE_OUT_OF_MEMORY_ERROR;
      return;
    }
    
    QPainter p(dest);
    p.setTransform(transformFromId(transform, src_width, src_height));
    p.drawPixmap(0, 0, *src, src_x, src_y, src_width, src_height);
    
    *creationErrorPtr = IMG_NATIVE_IMAGE_NO_ERROR;
    *newImmutableImage = dest->handle();
  }

  /**
  * Creates an immutable image that is a copy of a region
  * of the specified mutable image.
  *
  * @param srcMutableImage   pointer to source image
  * @param newImmutableImage address of pointer to return created new image
  * @param src_x             x-coord of the region
  * @param src_y             y-coord of the region
  * @param src_width         width of the region
  * @param src_height        height of the region
  * @param transform         transform to be applied to the region
  * @param creationErrorPtr  pointer to error status.
  *                          This function sets creationErrorPtr's value.
  *
  */
  void
  gxpport_createimmutable_from_mutableregion
  (gxpport_mutableimage_native_handle srcMutableImage,
  int src_x, int src_y, 
  int src_width, int src_height,
  int transform,
  gxpport_image_native_handle* newImmutableImage,
  img_native_error_codes* creationErrorPtr)
  { debug_trace();
    JMutableImage *src = JMutableImage::fromHandle(srcMutableImage);
    src->flush();
    
    int dest_width, dest_height;
    if (transformIsFlipping(transform))
    {
      dest_width = src_width;
      dest_height = src_height;
    }
    
    /* Check resource limit before copying */
    if (!resourcesAvailable(dest_width, dest_height))
    {
      /* Exceeds resource limit */
      *creationErrorPtr = IMG_NATIVE_IMAGE_RESOURCE_LIMIT;
      return;
    }

    JIMMutableImage *dest = new JIMMutableImage(dest_width, dest_height);
    if (!dest)
    {
      *creationErrorPtr = IMG_NATIVE_IMAGE_OUT_OF_MEMORY_ERROR;
      return;
    }
    
    QPainter p(dest);
    p.setTransform(transformFromId(transform, src_width, src_height));
    p.drawImage(0, 0, *src, src_x, src_y, src_width, src_height);
    
    *creationErrorPtr = IMG_NATIVE_IMAGE_NO_ERROR;
    *newImmutableImage = dest->handle();
  }

  /**
  * Decodes the given input data into a storage format used by immutable
  * images.  The input data should be in a self-identifying format; that is,
  * the data must contain a description of the decoding process.
  * 
  *  @param newImmutableImage address of pointer to return created new image
  *  @param srcBuffer input data to be decoded.
  *  @param length length of the input data.
  *  @param ret_imgWidth pointer to the width of the decoded image when the
  *         function runs successfully. This function sets ret_imgWidth's
  *         value.
  *  @param ret_imgHeight pointer to the height of the decoded image when the
  *         function runs successfully. This function sets ret_imgHeight's
  *         value.
  *  @param creationErrorPtr pointer to the status of the decoding
  *         process. This function sets creationErrorPtr's value.
  */
  void
  gxpport_decodeimmutable_from_selfidentifying
  (unsigned char* srcBuffer, int length, 
  int* ret_imgWidth, int* ret_imgHeight,
  gxpport_image_native_handle* newImmutableImage,
  img_native_error_codes* creationErrorPtr)
  { debug_trace();
    MIDP_ERROR err;
    imgdcd_image_format format;
    unsigned int w, h;

    err = imgdcd_image_get_info(srcBuffer, (unsigned int)length,
                                &format, &w, &h);

    if (err != MIDP_ERROR_NONE || format == IMGDCD_IMAGE_FORMAT_UNSUPPORTED)
    {
      qDebug("unsupported format");
      *creationErrorPtr = IMG_NATIVE_IMAGE_UNSUPPORTED_FORMAT_ERROR;
      return;
    }

    if (!resourcesAvailable(w, h))
    {
      qDebug("resource limit");
      /* Exceed Resource limit */
      *creationErrorPtr = IMG_NATIVE_IMAGE_RESOURCE_LIMIT;
      return;
    }

    JIMMutableImage *image = new JIMMutableImage(w, h);
    if (image == NULL)
    {
      qDebug("out of memory");
      *creationErrorPtr = IMG_NATIVE_IMAGE_OUT_OF_MEMORY_ERROR;
      return;
    }
    
    switch (format)
    {
      case IMGDCD_IMAGE_FORMAT_JPEG:
      case IMGDCD_IMAGE_FORMAT_PNG:
      {
        const char *format_str = (format==IMGDCD_IMAGE_FORMAT_JPEG)?"JPG":"PNG";
        qDebug("trying to load as %dx%d %s", w, h, format_str);
        if(!image->loadFromData(srcBuffer, length))
        {
          qDebug("FAIL");
          *creationErrorPtr = IMG_NATIVE_IMAGE_UNSUPPORTED_FORMAT_ERROR;
          delete image;
          return;
        }
      } break;

      case IMGDCD_IMAGE_FORMAT_RAW:
        qDebug("raw... bueeeeee...");
        image->fill();
        break;

      default:
        /* Shouldn't be here */
        qDebug("WTF?!");
        *creationErrorPtr = IMG_NATIVE_IMAGE_UNSUPPORTED_FORMAT_ERROR;
        delete image;
        return;
    }

    *ret_imgWidth = image->width();
    *ret_imgHeight = image->height();
    *newImmutableImage = image->handle();
    *creationErrorPtr = IMG_NATIVE_IMAGE_NO_ERROR;
  }

  /**
  * Decodes the ARGB input data into a storage format used by immutable images.
  * The array consists of values in the form of 0xAARRGGBB.
  * 
  * @param srcBuffer input data to be decoded.
  * @param width width of the image, in pixels.
  * @param height height of the image, in pixels.
  * @param processAlpha if true Alpha channel should be processed
  * @param newImmutableImage address of pointer to return created new image
  * @param creationErrorPtr pointer to the status of the decoding
  *        process. This function sets creationErrorPtr's value.
  */
  void
  gxpport_decodeimmutable_from_argb
  (jint* srcBuffer,
  int width, int height,
  jboolean processAlpha,
  gxpport_image_native_handle* newImmutableImage,
  img_native_error_codes* creationErrorPtr)
  { debug_trace();
    Q_UNUSED(srcBuffer);
    Q_UNUSED(width);
    Q_UNUSED(height);
    Q_UNUSED(processAlpha);
    *newImmutableImage = stubImage(width, height);
    *creationErrorPtr = IMG_NATIVE_IMAGE_NO_ERROR;
    
    #warning STUB
  }

  /**
  * Renders the contents of the specified immutable image
  * onto the destination specified.
  *
  * @param immutableImage      pointer to source image
  * @param graphicsDestination pointer to destination graphics object
  * @param clip                pointer to structure holding the clip
  *                              [x, y, width, height]
  * @param x_dest              x-coordinate in the destination
  * @param y_dest              y-coordinate in the destination
  * 
  */
  void gxpport_render_immutableimage
  (gxpport_image_native_handle immutableImage,
  gxpport_mutableimage_native_handle graphicsDestination,
  const jshort *clip,
  jint x_dest, jint y_dest)
  { 
    JIMMutableImage *src = JIMMutableImage::fromHandle(immutableImage);
    JMutableImage *dest = JMutableImage::fromHandle(graphicsDestination);
    QPainter *p = dest->painter(clip);
    
    //qDebug("gxpport_render_immutableimage(%08X, (%d, %d), %dx%d)", (uint)src, x_dest, y_dest, src->width(), src->height());
    
    p->drawPixmap(x_dest, y_dest, *src);
  }

  /**
  * Renders the contents of the specified region of this
  * immutable image onto the destination specified.
  *
  * @param srcImmutableImage   pointer to source image
  * @param graphicsDestination pointer to destination graphics object
  * @param clip                pointer to structure holding the clip
  *                                [x, y, width, height]
  * @param x_dest              x-coordinate in the destination
  * @param y_dest              y-coordinate in the destination
  * @param width               width of the region
  * @param height              height of the region
  * @param x_src               x-coord of the region
  * @param y_src               y-coord of the region
  * @param transform           transform to be applied to the region
  *
  */
  void gxpport_render_immutableregion
  (gxpport_image_native_handle srcImmutableImage,
  gxpport_mutableimage_native_handle graphicsDestination,
  const jshort *clip,
  jint x_dest, jint y_dest, 
  jint width, jint height,
  jint x_src, jint y_src,
  jint transform)
  { debug_trace();
    JIMMutableImage *src = JIMMutableImage::fromHandle(srcImmutableImage);
    JMutableImage *dest = JMutableImage::fromHandle(graphicsDestination);
    QPainter *p = dest->painter(clip);
    
    p->setTransform(transformFromId(transform, width, height));
    p->drawPixmap(x_dest, y_dest, *src, x_src, y_src, width, height);
    p->resetTransform();
  }

  /**
  * Gets ARGB representation of the specified immutable image
  * @param imutableImage pointer to the source image
  * @param rgbBuffer     pointer to buffer to write with the ARGB data
  * @param offset        offset in the buffer at which to start writing
  * @param scanLength    the relative offset within the array
  *                      between corresponding pixels of consecutive rows
  * @param x             x-coordinate of region
  * @param y             y-coordinate of region
  * @param width         width of region
  * @param height        height of region
  * @param errorPtr Error status pointer to the status.
  *                 This function sets creationErrorPtr's value.
  */
  void gxpport_get_immutable_argb
  (gxpport_image_native_handle immutableImage,
  jint* rgbBuffer, int offset, int scanLength,
  int x, int y, int width, int height,
  img_native_error_codes* errorPtr)
  { debug_trace();
    Q_UNUSED(immutableImage);
    Q_UNUSED(rgbBuffer);
    Q_UNUSED(offset);
    Q_UNUSED(scanLength);
    Q_UNUSED(x);
    Q_UNUSED(y);
    Q_UNUSED(width);
    Q_UNUSED(height);
    Q_UNUSED(immutableImage);
    Q_UNUSED(errorPtr);
    
    #warning STUB
  }
    
  /**
  * Cleans up any native resources to prepare the image to be garbage collected.
  *
  * @param immutableImage pointer to the platform immutable image to destroy.
  */
  void gxpport_destroy_immutable(gxpport_image_native_handle immutableImage)
  { debug_trace();
    delete JIMMutableImage::fromHandle(immutableImage);
  }

  /**
  * Decodes the given input data into a native platform representation that can
  * be saved.  The input data should be in a self-identifying format; that is,
  * the data must contain a description of the decoding process.
  *
  *  @param srcBuffer input data to be decoded. 
  *  @param length length of the input data.
  *  @param ret_dataBuffer pointer to the platform representation data that
  *         be saved.
  *  @param ret_length pointer to the length of the return data. 
  *  @param creationErrorPtr pointer to the status of the decoding
  *         process. This function sets creationErrorPtr's value.
  */
  void gxpport_decodeimmutable_to_platformbuffer
  (unsigned char* srcBuffer, long length,
  unsigned char** ret_dataBuffer, long* ret_length,
  img_native_error_codes* creationErrorPtr)
  { debug_trace();
    Q_UNUSED(srcBuffer);
    Q_UNUSED(length);
    Q_UNUSED(ret_dataBuffer);
    Q_UNUSED(ret_length);
    Q_UNUSED(creationErrorPtr);
    
    #warning STUB
  }

  /**
  * Loads the given input data into a storage format used by immutable
  * images.  The input data should be the native platform representation.
  * 
  *  @param newImmutableImage address of pointer to return created new image
  *  @param srcBuffer input data to be loaded.
  *  @param length length of the input data.
  *  @param ret_imgWidth pointer to the width of the loaded image when the
  *         function runs successfully. This function sets ret_imgWidth's
  *         value.
  *  @param ret_imgHeight pointer to the height of the loaded image when the
  *         function runs successfully. This function sets ret_imgHeight's
  *         value.
  *  @param creationErrorPtr pointer to the status of the loading
  *         process. This function sets creationErrorPtr's value.
  */
  void gxpport_loadimmutable_from_platformbuffer
  (unsigned char* srcBuffer, int length, jboolean isStatic,
  int* ret_imgWidth, int* ret_imgHeight,
  gxpport_image_native_handle* newImmutableImage,
  img_native_error_codes* creationErrorPtr)
  { debug_trace();
    Q_UNUSED(srcBuffer);
    Q_UNUSED(length);
    Q_UNUSED(isStatic);
    *ret_imgWidth = 10;
    *ret_imgHeight = 10;
    *newImmutableImage = stubImage(10, 10);
    *creationErrorPtr = IMG_NATIVE_IMAGE_NO_ERROR;
    
    #warning STUB
  }
}
