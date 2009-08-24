#ifndef _LFPPORT_QTOPIA_ITEM_H_
#define _LFPPORT_QTOPIA_ITEM_H_

#include <lfpport_item.h>
#include <lfpport_form.h>

#include "lfpport_qtopia_form.h"

class JItem: public QWidget
{
  Q_OBJECT
  public:
    JItem(MidpItem *item, JForm *form);

    // "j_" prefix added to avoid conficts with QWidget method names

    virtual void j_setLabel(const QString &text) = 0;

    virtual int j_getMinimumWidth();
    virtual int j_getMinimumHeight();
    virtual int j_getPreferredWidth();
    virtual int j_getPreferredHeight();

    inline void notifyFocusIn() { MidpFormFocusChanged(this); }
    inline void notifyResize() { MidpFormItemPeerStateChanged(this, 1); }
    inline void notifyStateChanged() { MidpFormItemPeerStateChanged(this, 0); }
  protected:
    void focusInEvent(QFocusEvent *event);
    void focusOutEvent(QFocusEvent *event);
    void resizeEvent(QResizeEvent *ev);
    
    JForm *form;
};

#endif // _LFPPORT_QTOPIA_ITEM_H_
