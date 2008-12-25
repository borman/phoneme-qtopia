#ifndef _LFPPORT_QTOPIA_FORM_H_
#define _LFPPORT_QTOPIA_FORM_H_

#include <QWidget>
#include <QString>
#include <QScrollArea>
#include <QLabel>

#include "lfpport_qtopia_displayable.h"

class JItem;

class JForm: public QWidget, public JDisplayable
{
  Q_OBJECT
  public:
    JForm(QWidget *parent, MidpDisplayable *disp, QString title, QString ticker);
    virtual ~JForm();

    static inline JForm *current();

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
  protected:
    void javaTitleChanged();
    void javaTickerChanged();
  private:
    QWidget *w_viewport;
    QScrollArea *w_scroller;
    //QLabel *w_title;
    QLabel *w_ticker;
    static JForm *currentForm;
};

#endif // _LFPPORT_QTOPIA_FORM_H_
