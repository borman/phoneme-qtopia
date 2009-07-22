#include <lfpport_customitem.h>

#include "lfpport_qtopia_debug.h"

#warning BEWARE of stubs!

extern "C"
{

  MidpError lfpport_customitem_create(MidpItem* itemPtr,
                                      MidpDisplayable* ownerPtr,
                                      const pcsl_string* label, int layout)
  {
    debug_trace();
    return KNI_OK;
  }

  MidpError lfpport_customitem_refresh(MidpItem* itemPtr,
                                       int x, int y,
                                       int width, int height)
  {
    return KNI_OK;
  }

  MidpError lfpport_customitem_get_label_width(int *widthRet,
                                               int width,
                                               MidpItem* ciPtr)
  {
    return KNI_OK;
  }

  MidpError lfpport_customitem_get_label_height(int width,
      int *heightRet,
      MidpItem* ciPtr)
  {
    return KNI_OK;
  }

  MidpError lfpport_customitem_get_item_pad(int *pad, MidpItem* ciPtr)
  {
    return KNI_OK;
  }

  MidpError lfpport_customitem_set_content_buffer(MidpItem* ciPtr,
      unsigned char* imgPtr)
  {
    return KNI_OK;
  }
}
/*
class JCustomItemSurface: public QWidget
{
  Q_OBJECT
  public:
    JCustomItemSurface(QWidget *parent);
    virtual ~JCustomItemSurface();

    void setCanvas(QPixmap *pixmap);
  protected:
    void paintEvent(QPaintEvent *ev);
  private:
    QPixmap *canvas;
};

JCustomItemSurface::JCustomItemSurface(QWidget *parent)
  : QWidget(parent);
{

}

JCustomItemSurface::~JCustomItemSurface()
{
}

void JCustomItemSurface::setCanvas(QPixmap *pixmap)
{
}

void JCustomItemSurface::paintEvent(QPaintEvent *ev)
{
}


//===================

JCustomItem::JCustomItem(MidpItem *item, JForm *form, const QString &label)
  : JItem(item, form
{
  QFormLayout *layout = new QFormLayout(this);
  layout->setRowWrapPolicy(QFormLayout::WrapAllRows);
  w_label = new QLabel(this);
  w_label->setText(label);

}

JCustomItem::~JCustomItem()
{
}

void JCustomItem::j_setLabel(const QString &text)
{
}

QSize JCustomItem::j_getLabelSize()
{
}

void JCustomItem::j_refreshSurface(int x, int y, int w, int h)
{
}

void JCustomItem::j_setContentBuffer(QPixmap *buffer)
{
}

int JCustomItem::j_getItemPad()
{
}
*/

#include "lfpport_qtopia_customitem.moc"
