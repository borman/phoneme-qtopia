#include <cstdio>
#include <lfpport_form.h>
#include <lfpport_item.h>
#include <midpEventUtil.h>

#include "lfpport_qtopia_item.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_debug.h"

extern "C"
{
  MidpError jitem_getMinimumWidth(int *width, MidpItem *itemPtr)
  {
    JItem *item = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr));
    if (!item)
    {
      lfpport_log("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
    *width = item->j_getMinimumWidth();
    lfpport_log("JItem: minimum width %d\n", *width);
    return KNI_OK;
  }

  MidpError jitem_getMinimumHeight(int *height, MidpItem *itemPtr)
  {
    JItem *item = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr));
    if (!item)
    {
      lfpport_log("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
    *height = item->j_getMinimumHeight();
    lfpport_log("JItem: minimum height %d\n", *height);
    return KNI_OK;
  }

  MidpError jitem_getPreferredWidth(int *width, MidpItem *itemPtr, int lockedHeight)
  {
    JItem *item = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr));
    if (!item)
    {
      lfpport_log("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
    *width = item->j_getPreferredWidth();
    lfpport_log("JItem: preferred width %d\n", *width);
    return KNI_OK;
  }

  MidpError jitem_getPreferredHeight(int *height, MidpItem *itemPtr, int lockedWidth)
  {
    JItem *item = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr));
    if (!item)
    {
      lfpport_log("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
    *height = item->j_getPreferredHeight();
    lfpport_log("JItem: preferred height %d\n", *height);
    return KNI_OK;
  }

  MidpError jitem_setLabel(MidpItem *itemPtr, const pcsl_string *label)
  {
    JItem *item = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr));
    if (!item)
    {
      lfpport_log("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
    item->j_setLabel(pcsl_string2QString(*label));
    return KNI_OK;
  }

  MidpError jitem_show(MidpItem *itemPtr)
  {
    JItem *item = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr));
    if (!item)
    {
      lfpport_log("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
    return item->j_show();
  }

  MidpError jitem_relocate(MidpItem *itemPtr, int x, int y)
  {
    JItem *item = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr));
    if (!item)
    {
      lfpport_log("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
    return item->j_relocate(x, y);
  }

  MidpError jitem_resize(MidpItem *itemPtr, int width, int height)
  {
    JItem *item = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr));
    if (!item)
    {
      lfpport_log("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
    return item->j_resize(width, height);
  }

  MidpError jitem_hide(MidpItem *itemPtr)
  {
    JItem *item = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr));
    if (!item)
    {
      lfpport_log("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
    return item->j_hide();
  }

  MidpError jitem_destroy(MidpItem *itemPtr)
  {
    JItem *item = qobject_cast<JItem *>(static_cast<QObject *>(itemPtr->widgetPtr));
    if (!item)
    {
      lfpport_log("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
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
  lfpport_log("JItem::j_resize(%d, %d)\n", w, h);
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


// Tell Java about widget focus change
void JItem::focusInEvent(QFocusEvent *event)
{
  lfpport_log("JItem: focus in\n");
  MidpFormFocusChanged(this);
}

// Nothing is focused between focusOut and focusIn
void JItem::focusOutEvent(QFocusEvent *event)
{
  lfpport_log("JItem: focus out\n");
  MidpFormFocusChanged(NULL);
}

void JItem::resizeEvent(QResizeEvent *ev)
{
  lfpport_log("JItem resized\n");
  /*
  MidpEvent evt;

  MIDP_EVENT_INITIALIZE(evt);
  evt.type = MIDP_INVALIDATE_EVENT;
  midpStoreEventAndSignalForeground(evt);
  */
}

#include "lfpport_qtopia_item.moc"
