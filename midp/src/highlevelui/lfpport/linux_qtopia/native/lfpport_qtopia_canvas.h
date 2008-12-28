#ifndef _LFPPORT_QTOPIA_CANVAS_H_
#define _LFPPORT_QTOPIA_CANVAS_H_

#include "lfpport_qtopia_displayable.h"
#include <QWidget>
#include <lfpport_error.h>

class JCanvas: public JDisplayable, public QWidget
{
  public:
    JCanvas(QWidget *parent, MidpDisplayable *canvasPtr, QString title, QString ticker);
    virtual ~JCanvas();

    MidpError j_show();
    MidpError j_hideAndDelete(jboolean onExit);
  protected:
    void showEvent(QShowEvent *event);
    void paintEvent(QPaintEvent *ev);
    void resizeEvent(QResizeEvent *ev);
    void mouseMoveEvent(QMouseEvent *event);
    void mousePressEvent(QMouseEvent *event);
    void mouseReleaseEvent(QMouseEvent *event);
    void keyPressEvent(QKeyEvent *event);
    void keyReleaseEvent(QKeyEvent *event);
};

#endif // _LFPPORT_QTOPIA_CANVAS_H_
