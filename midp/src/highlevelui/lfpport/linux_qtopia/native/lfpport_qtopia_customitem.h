#ifndef _LFPPORT_QTOPIA_CUSTOMITEM_H_
#define _LFPPORT_QTOPIA_CUSTOMITEM_H_

#include "lfpport_qtopia_item.h"
#include <QLabel>
#include <QObject>

class QPixmap;
class QEvent;
class JMutableImage;
class QFocusEvent;

class JCustomItemSurface: public QWidget
{
  Q_OBJECT
  public:
    JCustomItemSurface(QWidget *parent = 0);
    inline void setCanvas(JMutableImage *p) { canvas = p; updateGeometry(); }
    virtual QSize sizeHint() const;
//  protected:
    void paintEvent(QPaintEvent *ev);
    void mousePressEvent(QMouseEvent *event);
    void mouseMoveEvent(QMouseEvent *event);
    void mouseReleaseEvent(QMouseEvent *event);
    void keyPressEvent(QKeyEvent *event);
    void keyReleaseEvent(QKeyEvent *event);
  private:
    bool mousePressed;
    JMutableImage *canvas;
};


class JCustomItem: public JItem
{
  Q_OBJECT
	public:
		JCustomItem(MidpItem *item, JForm *form, const QString &label);
		void j_setLabel(const QString &text);
		void j_refreshSurface(int x, int y, int w, int h);
		void j_setContentBuffer(unsigned char *buffer);
		int getLabelWidth();
		int getLabelHeight();
	protected:
		void focusInEvent(QFocusEvent *event);
		void keyPressEvent(QKeyEvent *event);
		void keyReleaseEvent(QKeyEvent *event);
	private:
		QLabel *w_label;
		JCustomItemSurface *surface;
};

//*/

#endif // _LFPPORT_QTOPIA_CUSTOMITEM_H_
