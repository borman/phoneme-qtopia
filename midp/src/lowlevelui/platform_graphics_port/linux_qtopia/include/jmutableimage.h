#ifndef JMUTABLEIMAGE_H
#define JMUTABLEIMAGE_H

#include <QImage>
#include <QPainter>

#include <gxpport_mutableimage.h>

// NOTE: Using 32-bit storage format, can assume it
class JMutableImage: public QImage
{
  public:
    JMutableImage()
    : QImage(), m_painter(NULL)
    {
    }
    
    JMutableImage(int width, int height)
    : QImage(width, height, QImage::Format_ARGB32), m_painter(NULL)
    {
    }
    
    inline gxpport_mutableimage_native_handle handle() const
    {
      return (gxpport_mutableimage_native_handle)this;
    }
    
    static inline JMutableImage *fromHandle(gxpport_mutableimage_native_handle h)
    {
      if (!h)
        return &m_backBuffer;
      return static_cast<JMutableImage *>(h);
    }
    
    // Returns a painter associated with this image and sets clip
    inline QPainter *painter(const jshort *clip = NULL)
    {
      if (m_painter==NULL)
        m_painter = new QPainter(this);
      if (!m_painter->isActive())
        m_painter->begin(this);
      if (clip)
        m_painter->setClipRect(clip[0], clip[1], clip[2]-clip[0]+1, clip[3]-clip[1]+1);
      else
        m_painter->setClipping(false);
      return m_painter;
    }
    
    inline void flush()
    {
      if (m_painter != NULL && m_painter->isActive())
        m_painter->end();
    }
    
  private:
    QPainter *m_painter;
    static JMutableImage m_backBuffer;
};

#endif // JMUTABLEIMAGE_H
