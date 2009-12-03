/*
 *
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 *
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 *
 * This source file is specific for Qtopia4-based configurations.
 * Email: trollsid@gmail.com
 */

#include "lfpport_qtopia_pcsl_string.h"

#include <lfpport_customitem.h>
#include "lfpport_qtopia_customitem.h"
#include "lfpport_qtopia_debug.h"
#include "lfpport_qtopia_displayable.h"
#include <midpEventUtil.h>
#include <midpEvents.h>
#include <jkey.h>
#include <keymap_input.h>
#include <jdisplay.h>
#include <jmutableimage.h>

#include <QVBoxLayout>
#include <QPainter>
#include <QPaintEvent>
#include <QDebug>
#include <QPixmap>
#include <QEvent>
#include <QFocusEvent>
#include <QMouseEvent>
#include <QStyle>

#include <japplication.h>

extern "C"
{

  MidpError lfpport_customitem_create(MidpItem* itemPtr,
      MidpDisplayable* ownerPtr,
      const pcsl_string* label, int layout)
  {
    debug_trace();
    JDisplayable *disp = static_cast<JDisplayable *>(ownerPtr->frame.widgetPtr);
    if(disp == NULL)
      return KNI_ENOMEM;
    qDebug("CustomItem: Create");
    JCustomItem *cItem = new JCustomItem(itemPtr, disp->toForm(), pcsl_string2QString(*label));
    qDebug("CustomItem: Created");
    return KNI_OK;
  }

  MidpError lfpport_customitem_refresh(MidpItem* itemPtr,
      int x, int y,
      int width, int height)
  {
    JCustomItem *item = static_cast<JCustomItem *>(itemPtr->widgetPtr);
    item->j_refreshSurface(x, y, width, height);
    qDebug("CustomItem: refresh (%d,%d) %dx%d", x,y,width, height);
    return KNI_OK;
  }

  MidpError lfpport_customitem_get_label_width(int *widthRet,
      int width,
      MidpItem* ciPtr)
  {
    (void)width;
    JCustomItem *item = static_cast<JCustomItem *>(ciPtr->widgetPtr);
    *widthRet = item->getLabelWidth();
    qDebug("CustomItem: label width %d", *widthRet);
    return KNI_OK;
  }

  MidpError lfpport_customitem_get_label_height(int width,
      int *heightRet,
      MidpItem* ciPtr)
  {
    (void)width;
    JCustomItem *item = static_cast<JCustomItem *>(ciPtr->widgetPtr);
    *heightRet = item->getLabelHeight();
    qDebug("CustomItem: label height %d", *heightRet);
    return KNI_OK;
  }

  MidpError lfpport_customitem_get_item_pad(int *pad, MidpItem* ciPtr)
  {
    QStyle *style = JApplication::style();
    *pad = qMax(
        qMax(
          style->pixelMetric(QStyle::PM_LayoutLeftMargin),
          style->pixelMetric(QStyle::PM_LayoutRightMargin)),
        qMax(
          style->pixelMetric(QStyle::PM_LayoutTopMargin),
          style->pixelMetric(QStyle::PM_LayoutBottomMargin)));
    qDebug("CustomItem: padding %d", *pad);

    return KNI_OK;
  }

  MidpError lfpport_customitem_set_content_buffer(MidpItem* ciPtr,
      unsigned char* imgPtr)
  {
    JCustomItem *item = static_cast<JCustomItem *>(ciPtr->widgetPtr);
    item->j_setContentBuffer(imgPtr);
    return KNI_OK;
  }
}

  JCustomItemSurface::JCustomItemSurface(QWidget *parent)
: QWidget(parent), canvas(NULL)
{
	setFocusPolicy(Qt::StrongFocus);
	setMouseTracking(true);
}

void JCustomItemSurface::mousePressEvent(QMouseEvent *event)
{
  qDebug("CustomItemSurface: mousePress (%d,%d)", event->x(), event->y());
  MidpEvent ev;
  MIDP_EVENT_INITIALIZE(ev);
  ev.type = MIDP_PEN_EVENT;
  ev.ACTION = KEYMAP_STATE_PRESSED;
  ev.X_POS = event->x();
  ev.Y_POS = event->y();
  midpStoreEventAndSignalForeground(ev);
}

void JCustomItemSurface::mouseMoveEvent(QMouseEvent *event)
{
  qDebug("CustomItemSurface: mouseMove (%d,%d)", event->x(), event->y());
  MidpEvent ev;
  MIDP_EVENT_INITIALIZE(ev);
  ev.type = MIDP_PEN_EVENT;
  ev.ACTION = KEYMAP_STATE_DRAGGED;
  ev.X_POS = event->x();
  ev.Y_POS = event->y();
  midpStoreEventAndSignalForeground(ev);
}

void JCustomItemSurface::mouseReleaseEvent(QMouseEvent *event)
{
  qDebug("CustomItemSurface: mouseRelease (%d,%d)", event->x(), event->y());
  MidpEvent ev;
  MIDP_EVENT_INITIALIZE(ev);
  ev.type = MIDP_PEN_EVENT;
  ev.ACTION = KEYMAP_STATE_RELEASED;
  ev.X_POS = event->x();
  ev.Y_POS = event->y();
  midpStoreEventAndSignalForeground(ev);
}

QSize JCustomItemSurface::sizeHint() const
{
  if (canvas == NULL)
    return QSize();
  else 
    return canvas->size();
}

void JCustomItemSurface::paintEvent(QPaintEvent *ev)
{
  if(canvas != NULL)
  {
    canvas->flush();
    QPainter painter(this);
    painter.drawImage(ev->rect(), *canvas);
  }
}

void JCustomItemSurface::keyPressEvent(QKeyEvent *event)
{
  MidpEvent midp_event;
  MIDP_EVENT_INITIALIZE(midp_event);
  qDebug() << "JCustomItemSurface:Key pressed";
  if(LFPKeyMap::instance()->map(event->key(), event->text(), midp_event.CHR))
  {
    midp_event.type = MIDP_KEY_EVENT;
    midp_event.ACTION = KEYMAP_STATE_PRESSED;
    midpStoreEventAndSignalForeground(midp_event);
  }
}

void JCustomItemSurface::keyReleaseEvent(QKeyEvent *event)
{

  MidpEvent midp_event;
  MIDP_EVENT_INITIALIZE(midp_event);
  qDebug() << "JCustomItemSufrface: key released";
  if(LFPKeyMap::instance()->map(event->key(), event->text(), midp_event.CHR))
  {
    midp_event.type = MIDP_KEY_EVENT;
    midp_event.ACTION = KEYMAP_STATE_RELEASED;
    midpStoreEventAndSignalForeground(midp_event);
  }
}


//===================

JCustomItem::JCustomItem(MidpItem *item, JForm *form, const QString &label)
: JItem(item, form)
{
	QVBoxLayout *layout = new QVBoxLayout(this);
	w_label = new QLabel(this);
	surface = new JCustomItemSurface(this);
	setFocusPolicy(Qt::StrongFocus);
	layout->addWidget(w_label);
	layout->addWidget(surface);
	j_setLabel(label);
	surface->setFocusProxy(this);
}

void JCustomItem::j_setLabel(const QString &text)
{
  w_label->setText(text);
  if (text.isEmpty())
    w_label->hide();
  else
    w_label->show();
}

void JCustomItem::j_refreshSurface(int x, int y, int w, int h)
{
  surface->update(x, y, w, h);
}

void JCustomItem::focusInEvent(QFocusEvent *e)
{
	if(e->reason() != Qt::OtherFocusReason)
	{
		qDebug() << "JCustomItem: focus in ";
		MidpFormFocusChanged(this);
	}
}

void JCustomItem::j_setContentBuffer(unsigned char *buffer)
{
  if(buffer != NULL)
  {
    JMutableImage *pix = JMutableImage::fromHandle(buffer);
    surface->setCanvas(pix);
  }
}


int JCustomItem::getLabelHeight()
{
  return w_label->height();
}

int JCustomItem::getLabelWidth()
{
  return w_label->width();
}

void JCustomItem::keyPressEvent(QKeyEvent *event)
{
	surface->keyPressEvent(event);
}

void JCustomItem::keyReleaseEvent(QKeyEvent *event)
{
	surface->keyReleaseEvent(event);
}

#include "lfpport_qtopia_customitem.moc"
