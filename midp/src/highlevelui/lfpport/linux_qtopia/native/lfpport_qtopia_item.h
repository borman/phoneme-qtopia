#ifndef _LFPPORT_QTOPIA_ITEM_H_
#define _LFPPORT_QTOPIA_ITEM_H_

#include <lfpport_item.h>

#include "lfpport_qtopia_form.h"

class JItem: public QWidget
{
  Q_OBJECT
  public:
    JItem(MidpItem *item, JForm *form);
    virtual ~JItem();

    // "j_" prefix added to avoid conficts with QWidget method names

    virtual void j_setLabel(const QString &text) = 0;

    virtual int j_getMinimumWidth();
    virtual int j_getMinimumHeight();
    virtual int j_getPreferredWidth();
    virtual int j_getPreferredHeight();

    virtual MidpError j_show();
    virtual MidpError j_hide();

    virtual MidpError j_resize(int w, int h);
    virtual MidpError j_relocate(int x, int y);

    virtual MidpError j_destroy();
    
    void notifyFocusIn();
    void notifyResize();
    void notifyStateChanged();
  protected:
    void focusInEvent(QFocusEvent *event);
    void focusOutEvent(QFocusEvent *event);
    void resizeEvent(QResizeEvent *ev);
  private:
    JForm *form;
};

#endif // _LFPPORT_QTOPIA_ITEM_H_
