#include "item.h"

extern "C"
{
  MidpError jitem_getMinimumWidth(int *width, MidpItem *itemPtr)
  {
    JItem *item = itemPtr->widgetPtr;
#ifdef DEBUG
    if (!(item) || !item->inherits("JItem"))
    {
      printf("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
#endif
    *width = item->j_getMinimumWidth(width);
    return KNI_OK;
  }

  MidpError jitem_getMinimumHeight(int *height, MidpItem *itemPtr)
  {
    JItem *item = itemPtr->widgetPtr;
#ifdef DEBUG
    if (!(item) || !item->inherits("JItem"))
    {
      printf("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
#endif
    *height = item->j_getMinimumHeight(width);
    return KNI_OK;
  }

  MidpError jitem_getPreferredWidth(int *width, MidpItem *itemPtr, int lockedHeight)
  {
    JItem *item = itemPtr->widgetPtr;
#ifdef DEBUG
    if (!(item) || !item->inherits("JItem"))
    {
      printf("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
#endif
    *width = item->j_getPreferredWidth(width);
    return KNI_OK;
  }

  MidpError jitem_getPreferredHeight(int *height, MidpItem *itemPtr, int lockedWidth)
  {
    JItem *item = itemPtr->widgetPtr;
#ifdef DEBUG
    if (!(item) || !item->inherits("JItem"))
    {
      printf("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
#endif
    *height = item->j_getPreferredHeight(width);
    return KNI_OK;
  }

  MidpError jitem_setLabel(MidpItem *itemPtr, const pcsl_string *label)
  {
    JItem *item = itemPtr->widgetPtr;
#ifdef DEBUG
    if (!(item) || !item->inherits("JItem"))
    {
      printf("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
#endif
    return item->j_setLabel(pcsl_string2QString(label);
  }

  MidpError jitem_show(MidpItem *itemPtr)
  {
    JItem *item = itemPtr->widgetPtr;
#ifdef DEBUG
    if (!(item) || !item->inherits("JItem"))
    {
      printf("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
#endif
    return item->j_show()
  }

  MidpError jitem_relocate(MidpItem *itemPtr, int x, int y)
  {
    JItem *item = itemPtr->widgetPtr;
#ifdef DEBUG
    if (!(item) || !item->inherits("JItem"))
    {
      printf("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
#endif
    return item->j_relocate(x, y);
  }

  MidpError jitem_resize(MidpItem *itemPtr, int width, int height)
  {
    JItem *item = itemPtr->widgetPtr;
#ifdef DEBUG
    if (!(item) || !item->inherits("JItem"))
    {
      printf("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
#endif
    return item->j_resize(width, height);
  }

  MidpError jitem_hide(MidpItem *itemPtr)
  {
    JItem *item = itemPtr->widgetPtr;
#ifdef DEBUG
    if (!(item) || !item->inherits("JItem"))
    {
      printf("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
#endif
    return item->j_hide();
  }

  MidpError jitem_destroy(MidpItem *itemPtr)
  {
    JItem *item = itemPtr->widgetPtr;
#ifdef DEBUG
    if (!(item) || !item->inherits("JItem"))
    {
      printf("ERROR: invalid item\n");
      return KNI_EINVAL;
    }
#endif
    return item->j_destroy();
  }
}

JItem::JItem(MidpItem *item, JForm *form)
  : QWidget(form->viewport())
{
  this->form = form;
  item->widgetPtr = this;

  item->getMinimumWidth = jitem_getMinimumWidth;
  item->getMinimuHeight = jitem_getMinimumHeight;
  item->getPreferredWidth = jitem_getPreferredHeight;
  item->getPreferredHeight = jitem_getPreferredHeight;
  item->setLabel = jitem_setLabel;
  item->show = jitem_show;
  item->hide = jitem_hide;
  item->relocate = jitem_relocate;
  item->resize = jitem_resize;
  item->destroy = jitem_destroy;
  item->handleEvent = NULL;
}

JItem::~JItem()
{
}

MidpError JItem::j_resize(int w, int h)
{
  setFixedSize(QSize(w, h));
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
void focusInEvent(QFocusEvent *event)
{
  MidpFormFocusChanged(this);
}

// Nothing is focused between focusOut and focusIn
void focusOutEvent(QFocusEvent *event)
{
  MidpFormFocusChanged(NULL);
}

