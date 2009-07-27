#include <lfpport_form.h>
#include <lfpport_item.h>
#include <midpEventUtil.h>

#include "lfpport_qtopia_item.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_debug.h"

#define FAKE_ITEM_SIZE

extern "C"
{
# ifdef _DEBUG
#   define FETCH_ITEM(_name) \
      JItem *_name = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr)); \
      if (!_name) \
      { \
        qCritical("ERROR: invalid item\n"); \
        return KNI_EINVAL; \
      }
#else
#   define FETCH_ITEM(_name) \
      JItem *_name = static_cast<JItem *>(itemPtr->widgetPtr);
#endif

  MidpError jitem_getMinimumWidth(int *width, MidpItem *itemPtr)
  {
    FETCH_ITEM(item);
    *width = item->j_getMinimumWidth();
    qDebug("JItem[%s]: minimum width %d", item->metaObject()->className(), *width);
    return KNI_OK;
  }

  MidpError jitem_getMinimumHeight(int *height, MidpItem *itemPtr)
  {
    FETCH_ITEM(item);
    *height = item->j_getMinimumHeight();
    qDebug("JItem[%s]: minimum height %d", item->metaObject()->className(), *height);
    return KNI_OK;
  }

  MidpError jitem_getPreferredWidth(int *width, MidpItem *itemPtr, int lockedHeight)
  {
    FETCH_ITEM(item);
    *width = item->j_getPreferredWidth();
    qDebug("JItem[%s]: preferred width %d", item->metaObject()->className(), *width);
    return KNI_OK;
  }

  MidpError jitem_getPreferredHeight(int *height, MidpItem *itemPtr, int lockedWidth)
  {
    FETCH_ITEM(item);
    *height = item->j_getPreferredHeight();
    qDebug("JItem[%s]: preferred height %d", item->metaObject()->className(), *height);
    return KNI_OK;
  }

  MidpError jitem_setLabel(MidpItem *itemPtr, const pcsl_string *label)
  {
    FETCH_ITEM(item);
    item->j_setLabel(pcsl_string2QString(*label));
    return KNI_OK;
  }

  MidpError jitem_show(MidpItem *itemPtr)
  {
    FETCH_ITEM(item);
    return item->j_show();
  }

  MidpError jitem_relocate(MidpItem *itemPtr, int x, int y)
  {
    FETCH_ITEM(item);
    return item->j_relocate(x, y);
  }

  MidpError jitem_resize(MidpItem *itemPtr, int width, int height)
  {
    FETCH_ITEM(item);
    return item->j_resize(width, height);
  }

  MidpError jitem_hide(MidpItem *itemPtr)
  {
    FETCH_ITEM(item);
    return item->j_hide();
  }

  MidpError jitem_destroy(MidpItem *itemPtr)
  {
    FETCH_ITEM(item);
    return item->j_destroy();
  }
}

JItem::JItem(MidpItem *item, JForm *form)
  : QWidget(form->j_viewport())
{
  this->form = form;
  item->widgetPtr = this;

  item->getMinimumWidth = jitem_getMinimumWidth;
  item->getMinimumHeight = jitem_getMinimumHeight;
  item->getPreferredWidth = jitem_getPreferredHeight;
  item->getPreferredHeight = jitem_getPreferredHeight;
  item->setLabel = jitem_setLabel;
  item->show = jitem_show;
  item->hide = jitem_hide;
  item->relocate = jitem_relocate;
  item->resize = jitem_resize;
  item->destroy = jitem_destroy;
  item->handleEvent = NULL;

  setSizePolicy(QSizePolicy(QSizePolicy::Minimum, QSizePolicy::Minimum));
}

JItem::~JItem()
{
}

MidpError JItem::j_resize(int w, int h)
{
  setFixedSize(QSize(w, h));
  qDebug("JItem::j_resize(%d, %d)", w, h);
  return KNI_OK;
}

MidpError JItem::j_relocate(int x, int y)
{
  move(x, y);
  return KNI_OK;
}

MidpError JItem::j_show()
{
  show();
  return KNI_OK;
}

MidpError JItem::j_hide()
{
  hide();
  return KNI_OK;
}

MidpError JItem::j_destroy()
{
  delete this;
  return KNI_OK;
}

#ifdef FAKE_ITEM_SIZE
// JItem occupies the whole row
int JItem::j_getPreferredWidth()
{
  return form->viewportWidth();
}

// JItem is not horizontally shrinkable, minimumWidth==preferredWidth
int JItem::j_getMinimumWidth()
{
  return j_getPreferredWidth();
}

// uses sizeHint->minimumHeight
int JItem::j_getPreferredHeight()
{
  if (MidpFormInSingleItemMode(form->toMidpDisplayable()))
  {
    qDebug("MidpFormInSingleItemMode");
    return form->viewportHeight();
  }
  
  QSize hint = sizeHint();
  if (hint.isValid())
    return hint.height();
  else
    return minimumHeight();
}

// JItem is not vertially shrinkable, minimumHeight==preferredHeight
int JItem::j_getMinimumHeight()
{
  return j_getPreferredHeight();
}
#else
int JItem::j_getPreferredWidth()
{
  QSize hint = sizeHint();
  if (hint.isValid())
    return hint.height();
  else
    return minimumHeight();
}

int JItem::j_getMinimumWidth()
{
  return minimumWidth();
}

int JItem::j_getPreferredHeight()
{
  QSize hint = sizeHint();
  if (hint.isValid())
    return hint.height();
  else
    return minimumHeight();
}

int JItem::j_getMinimumHeight()
{
  return minimumHeight();
}
#endif

// Tell Java about widget focus change
void JItem::focusInEvent(QFocusEvent *event)
{
  qDebug("JItem: focus in");
  MidpFormFocusChanged(this);
}

// Nothing is focused between focusOut and focusIn
void JItem::focusOutEvent(QFocusEvent *event)
{
  qDebug("JItem: focus out");
  MidpFormFocusChanged(NULL);
}

void JItem::resizeEvent(QResizeEvent *ev)
{
  qDebug("JItem resized");
}

void JItem::notifyFocusIn()
{
  MidpFormFocusChanged(this);
}

void JItem::notifyResize()
{
  MidpFormItemPeerStateChanged(this, 1);
}

void JItem::notifyStateChanged()
{
  MidpFormItemPeerStateChanged(this, 0);
}

#include "lfpport_qtopia_item.moc"
