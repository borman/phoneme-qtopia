#include <cstdio>

#include <QRect>
#include <QPaintDevice>
#include <QPainter>
#include <QPixmap>

#include "jdisplay.h"

#include "jgraphics.h"
#include <gxpportqt_image.h>

QPainter *JGraphics::painter = NULL;

int   JGraphics::last_pen = 0;
int   JGraphics::last_brush = 0;
QRect JGraphics::last_clip;
int   JGraphics::last_dotted = 0;

void JGraphics::init()
{
  if (!painter)
    painter = new QPainter;
}

void JGraphics::destroy()
{
  if (painter)
  {
    delete painter;
    painter = NULL;
  }
}

QPainter *JGraphics::setupGC(int pixel_pen, int pixel_brush, const QRect &clip, QPaintDevice *dst, int dotted)
{
  bool need_reinit = false;

  if (!painter)
    init();
  if (!dst)
  {
    dst = JDisplay::current()->backBuffer();
  }
  if (!painter->isActive() || (dst != painter->device()))
  {
    if (painter->isActive())
      painter->end();
    painter->begin(dst);
    need_reinit = true;
  }

  if (need_reinit || (pixel_pen != last_pen) || (dotted != last_dotted)) // Need to set up pen
  {
    if (pixel_pen != -1)
    {
      QPen pen(QColor::fromRgb(pixel_pen));
      pen.setStyle(dotted?(Qt::DotLine):(Qt::SolidLine));
      painter->setPen(pen);
    }
    else
      painter->setPen(Qt::NoPen);
    last_pen = pixel_pen;
    last_dotted = dotted;
  }

  if (need_reinit || (pixel_brush != last_brush))
  {
    if (pixel_brush != -1)
      painter->setBrush(QBrush(QColor::fromRgb(pixel_brush)));
    else
      painter->setBrush(Qt::NoBrush);
    last_brush = pixel_brush;
  }

  if (need_reinit || (last_clip != clip))
  {
    if (!clip.isNull())
    {
      painter->setClipping(true);
      painter->setClipRegion(QRegion(clip));
    }
    else
      painter->setClipping(false);
    last_clip = clip;
  }

  return painter;
}

bool JGraphics::paintingOn(QPaintDevice *device)
{
  return (painter && (painter->device()==device));
}

void JGraphics::flush(QPaintDevice *device)
{
  if (painter && painter->isActive() && ((device==NULL) || painter->device()==device))
    painter->end();
}

QPixmap *JGraphics::mutablePixmap(gxpport_mutableimage_native_handle mutable_image)
{
  return (QPixmap *)mutable_image;
}

QPixmap *JGraphics::immutablePixmap(gxpport_image_native_handle immutableImage)
{
  return gxpportqt_get_immutableimage_pixmap(immutableImage);
}
