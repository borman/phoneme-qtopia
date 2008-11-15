#ifndef _LFPPORT_QTOPIA_FORM_H_
#define _LFPPORT_QTOPIA_FORM_H_

#include "lfpport_qtopia_displayable.h"

class JItem;

class JForm: public JDisplayable, public QWidget
{
  Q_OBJECT
  public:
    JForm(QWidget *parent, MidpDisplayable *disp, QString title, QString ticker);
    virtual ~JForm();

    int viewportWidth();
    int viewportHeight();

    QWidget *j_viewport()
    {
      return w_viewport;
    }

    MidpError j_show();
    MidpError j_hideAndDelete(jboolean onExit);

    MidpError setCurrentItem(JItem *item, int y_offset);
    MidpError setContentSize(int w, int h);
    int getScrollPosition();
    MidpError setScrollPosition(int pos);
  private:
    QWidget *w_viewport;
    QScrollArea *w_scroller;
    QLabel *w_title;
    QLabel *w_ticker;
};

#endif // _LFPPORT_QTOPIA_FORM_H_
