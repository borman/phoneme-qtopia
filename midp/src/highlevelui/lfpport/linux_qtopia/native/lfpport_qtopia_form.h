#ifndef _LFPPORT_QTOPIA_FORM_H_
#define _LFPPORT_QTOPIA_FORM_H_

#include <QWidget>
#include <QString>
#include <QScrollArea>
#include <QScrollBar>
#include <QLabel>

#include "lfpport_qtopia_displayable.h"
#include "lfpport_qtopia_ticker.h"

class JItem;

class JForm: public QWidget, public JDisplayable
{
  Q_OBJECT
  public:
    JForm(QWidget *parent, MidpDisplayable *disp, QString title, QString ticker);

    static inline JForm *current() 
    {
      if (!currentForm)
        qCritical("!!! INVALID CURRENT FORM\n");
      return currentForm;
    }

    inline int viewportWidth() { return w_scroller->viewport()->width(); }
    inline int viewportHeight() { return w_scroller->viewport()->height(); }

    inline QWidget *j_viewport() { return w_viewport; }

    MidpError j_show();
    MidpError j_hideAndDelete(jboolean onExit);

    MidpError setCurrentItem(JItem *item, int y_offset);
    MidpError setContentSize(int w, int h);
    inline int getScrollPosition() { return w_scroller->verticalScrollBar()->value(); }
    MidpError setScrollPosition(int pos);
    
    bool eventFilter(QObject *watched, QEvent *event);
  protected:
    void showEvent(QShowEvent *event);
    void javaTickerChanged();
  private:
    QWidget *w_viewport;
    QScrollArea *w_scroller;
    //QLabel *w_title;
    JTicker *w_ticker;
    static JForm *currentForm;
};

#endif // _LFPPORT_QTOPIA_FORM_H_
