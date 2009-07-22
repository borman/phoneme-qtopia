#include "lfpport_qtopia_pcsl_string.h"

#include <lfpport_customitem.h>
#include "lfpport_qtopia_customitem.h"
#include "lfpport_qtopia_debug.h"
#include "lfpport_qtopia_displayable.h"
#include <gxpportqt_image.h>
#include <jgraphics.h>
#include <jdisplay.h>
#include <QVBoxLayout>
#include <QPainter>
#include <QPaintEvent>
#include <QDebug>

#warning BEWARE of stubs!

#define PAD_SIZE 4

extern "C"
{

  MidpError lfpport_customitem_create(MidpItem* itemPtr,
                                      MidpDisplayable* ownerPtr,
                                      const pcsl_string* label, int layout)
  {
    debug_trace();
    JDisplayable *disp = static_cast<JDisplayable *>(ownerPtr->frame.widgetPtr);
    if(disp == NULL)
    {
        return KNI_ENOMEM;
    }
    qDebug() << "Create custemItem";
    JCustomItem *cItem = new JCustomItem(itemPtr, disp->toForm(), pcsl_string2QString(*label));
    qDebug() << "Created";
    return KNI_OK;
  }

  MidpError lfpport_customitem_refresh(MidpItem* itemPtr,
                                       int x, int y,
                                       int width, int height)
  {
        JCustomItem *item = static_cast<JCustomItem *>(itemPtr->widgetPtr);
        item->j_refreshSurface(x, y, width, height);
        qDebug() << "refresh";
        return KNI_OK;
  }

  MidpError lfpport_customitem_get_label_width(int *widthRet,
                                               int width,
                                               MidpItem* ciPtr)
  {
      (void)width;
      JCustomItem *item = static_cast<JCustomItem *>(ciPtr->widgetPtr);
      *widthRet = item->getLabelWidth();
      return KNI_OK;
  }

  MidpError lfpport_customitem_get_label_height(int width,
      int *heightRet,
      MidpItem* ciPtr)
  {
        (void)width;
        qDebug() << "label height";
        JCustomItem *item = static_cast<JCustomItem *>(ciPtr->widgetPtr);
        *heightRet = item->getLabelHeight();
        qDebug() << "label height";
        return KNI_OK;
  }

  MidpError lfpport_customitem_get_item_pad(int *pad, MidpItem* ciPtr)
  {
    return KNI_OK;
  }

  MidpError lfpport_customitem_set_content_buffer(MidpItem* ciPtr,
      unsigned char* imgPtr)
  {
      JCustomItem *item = static_cast<JCustomItem *>(ciPtr->widgetPtr);
      item->j_setContentBuffer(imgPtr);
      return KNI_OK;
  }
}
///*


JCustomItemSurface::JCustomItemSurface(QWidget *parent)
  : QWidget(parent)
{
    canvas = NULL;
}

JCustomItemSurface::~JCustomItemSurface()
{
}

void JCustomItemSurface::setCanvas(QPixmap *p)
{
    if(!p->isNull())
    {   
	canvas = p;
    }
}

void JCustomItemSurface::paintEvent(QPaintEvent *ev)
{
	QRect r(ev->rect());
	if(canvas != NULL)
	{
		QPainter painter;
		painter.drawPixmap(ev->rect(), *canvas);
	}
}

void JCustomItemSurface::refreshSurface(int x, int y, int w, int h)
{
	if(canvas != NULL)
	{
		QPainter painter;//(canvas);
		painter.drawPixmap(x, y, w , h, *canvas);
	}
}

//*/
//===================
//
JCustomItem::JCustomItem(MidpItem *item, JForm *form, const QString label)
  : JItem(item, form)
{
  QVBoxLayout *layout = new QVBoxLayout(this);
  w_label = new QLabel(this);
  w_label->setText(label);
  JCustomItemSurface *surface = new JCustomItemSurface(this);
  if(!label.isNull())
  {
    layout->addWidget(w_label);
  }
}

JCustomItem::~JCustomItem()
{
}

void JCustomItem::j_setLabel(const QString &text)
{
    w_label->setText(text);
}

QSize JCustomItem::j_getLabelSize()
{
    return w_label->size();
}

void JCustomItem::j_refreshSurface(int x, int y, int w, int h)
{
    qDebug() << "Refresh surface";
    surface->refreshSurface(x, y, w, h);
}

void JCustomItem::j_setContentBuffer(unsigned char *buffer)
{
    qDebug() << "set content buffer" << sizeof buffer << ":" << sizeof *buffer;


    QImage img = QImage(buffer, 32, 32,QImage::Format_RGB32);
    qDebug() << "fuck";

    QPixmap *pix = new QPixmap;
    qDebug() << "fuck 2";
    pix->fromImage(img);
    qDebug() << "fuck 3";
    surface->setCanvas(pix);
    qDebug() << "fuck 4";
//    qDebug() << "Buffer set compleated";
}

int JCustomItem::j_getItemPad()
{
    return 0;
}

int JCustomItem::getLabelHeight()
{
    return w_label->height();
}

int JCustomItem::getLabelWidth()
{
    return w_label->width();
}
//*/

#include "lfpport_qtopia_customitem.moc"
