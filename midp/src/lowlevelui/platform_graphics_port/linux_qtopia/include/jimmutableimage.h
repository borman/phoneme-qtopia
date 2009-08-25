#ifndef JIMMUTABLEIMAGE_H
#define JIMMUTABLEIMAGE_H

#include <QPixmap>

#include <gxpport_immutableimage.h>

#include "jmutableimage.h"

class JIMMutableImage: public QPixmap
{
  public:
    JIMMutableImage(const QImage &mimage)
    : QPixmap(QPixmap::fromImage(mimage))
    {
    }
    
    JIMMutableImage(int width, int height)
    : QPixmap(width, height)
    {
    }
    
    inline gxpport_image_native_handle handle() const
    {
      return (gxpport_image_native_handle)this;
    }
    
    static inline JIMMutableImage *fromHandle(gxpport_image_native_handle h)
    {
      return static_cast<JIMMutableImage *>(h);
    } 
};

#endif // JIMMUTABLEIMAGE_H
