#ifndef _LFPPORT_QTOPIA_CUSTOMITEM_H_
#define _LFPPORT_QTOPIA_CUSTOMITEM_H_

#include "lfpport_qtopia_item.h"
#include <QLabel>
#include <QObject>
///*

class JCustomItemSurface: public QWidget
{
  Q_OBJECT
  public:
    JCustomItemSurface(QWidget *parent = 0);
    virtual ~JCustomItemSurface();
    void refreshSurface(int x, int y, int w, int h);
    void setCanvas(QPixmap *p);
  protected:
    void paintEvent(QPaintEvent *ev);
  private:
    bool        mousePressed;
    QPixmap *canvas;
};


class JCustomItem: public JItem
{
  Q_OBJECT
  public:
    JCustomItem(MidpItem *item, JForm *form, const QString label);
    virtual ~JCustomItem();
    void j_setLabel(const QString &text);
    QSize j_getLabelSize();
    void j_refreshSurface(int x, int y, int w, int h);
    void j_setContentBuffer(unsigned char *buffer);
    int j_getItemPad();
    int getLabelWidth();
    int getLabelHeight();
  private:
    QLabel *w_label;
    JCustomItemSurface *surface;
};

//*/

#endif // _LFPPORT_QTOPIA_CUSTOMITEM_H_
