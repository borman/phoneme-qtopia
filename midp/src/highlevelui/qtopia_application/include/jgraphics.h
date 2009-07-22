#ifndef _JGRAPHICS_H_
#define _JGRAPHICS_H_

#include <gxpport_mutableimage.h>
#include <gxpport_immutableimage.h>

class QRect;
class QPainter;
class QPaintDevice;
class QPixmap;

/*!
\brief Graphics utils

Static class offering low-level graphics utils.
*/
class JGraphics
{
  public:
    /**
    \brief Initialize

    Initialize JGraphics singleton.
    **/
    static void init();
    /**
    \brief Destroy

    Destroy JGraphics singleton.
     **/
    static void destroy();
    /**
    \brief Quickly setup a QPainter

    Setup a QPainter with given props for \a dst or for screen backbuffer if \a dst is NULL
     **/
    static QPainter *setupGC(int pixel_pen, int pixel_brush, const QRect &clip, QPaintDevice *dst, int dotted);
    /**
    \brief Check for painting activity

    Check whether painting on \a device is active
     **/
    static bool paintingOn(QPaintDevice *device); // Are we painting on this device now?
    /**
    \brief Flush pending painting

    Flush all pending painting commands and release \a device if specified
    or flush and release all devices if \a device is NULL
     **/
    static void flush(QPaintDevice *device = NULL); // Flush all pending operations and free all devices
    
    static QPixmap *mutablePixmap(gxpport_mutableimage_native_handle mutableImage);
    static QPixmap *immutablePixmap(gxpport_image_native_handle immutableImage);
  private:
    static QPainter *painter;

    // painter parameters cache
    static int last_pen;
    static int last_brush;
    static QRect last_clip;
    static int last_dotted;
};

#endif //_JGRAPHICS_H_
