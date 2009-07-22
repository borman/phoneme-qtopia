#ifndef _LFJPORT_QTOPIA_SCREEN_H_
#define _LFJPORT_QTOPIA_SCREEN_H_

#include <QWidget>

//class QPaintEvent;
class QMouseEvent;
class QString;

class LFJScreen: public QWidget
{
  Q_OBJECT
  public:
    LFJScreen(QWidget *parent = NULL);
    virtual ~LFJScreen();
    void setSoftButtonLabel(int index, const QString &label);
    static LFJScreen *instance();
    static void init(QWidget *parent = NULL);
    static void destroy();
  protected:
    virtual void paintEvent(QPaintEvent *event);
    virtual void resizeEvent(QResizeEvent *event);
    virtual void mouseMoveEvent(QMouseEvent *event);
    virtual void mousePressEvent(QMouseEvent *event);
    virtual void mouseReleaseEvent(QMouseEvent *event);
    virtual void keyPressEvent(QKeyEvent *event);
    virtual void keyReleaseEvent(QKeyEvent *event);
  private:
    static LFJScreen *m_screen;
};

#endif // _LFJPORT_QTOPIA_SCREEN_H_

