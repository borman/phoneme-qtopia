#include <cstdio>

#include <QVBoxLayout>
#include <QScrollBar>

#include <jdisplay.h>
#include <lfpport_error.h>
#include <lfpport_form.h>
#include <lfpport_item.h>
#include <midpEventUtil.h>

#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_item.h"
#include "lfpport_qtopia_form.h"
#include "lfpport_qtopia_debug.h"

class JFormViewport: public QWidget
{
  public:
    JFormViewport(QWidget *parent = NULL);
    virtual ~JFormViewport();
  protected:
    void resizeEvent(QResizeEvent *event);
};

JFormViewport::JFormViewport(QWidget *parent)
  : QWidget(parent)
{
}

JFormViewport::~JFormViewport()
{
}

void JFormViewport::resizeEvent(QResizeEvent *event)
{
  lfpport_log("JFormViewport resized to (%dx%d)\n", width(), height());
}
//------------------

class JFormScrollArea: public QScrollArea
{
  public:
    JFormScrollArea(QWidget *parent = NULL);
    virtual ~JFormScrollArea();
    
  protected:
    void resizeEvent(QResizeEvent *e);
};

JFormScrollArea::JFormScrollArea(QWidget *parent)
  : QScrollArea(parent)
{
}

JFormScrollArea::~JFormScrollArea()
{
}
    
void JFormScrollArea::resizeEvent(QResizeEvent *e)
{
  lfpport_log("Form viewport resized to (%d[%d: -%d] x %d)\n", viewport()->width(), verticalScrollBar()->isVisible(), verticalScrollBar()->width(), viewport()->height());
  int v_w = viewport()->width();
  if (verticalScrollBar()->isVisible())
    v_w -= verticalScrollBar()->width();
  int v_h = viewport()->height();
  JDisplay::current()->setDisplayWidth(v_w);
  JDisplay::current()->setDisplayHeight(v_h);
  
  MidpFormViewportChanged(this, 1); // 1 -> SIZE_REFRESH
  /*
  MidpEvent evt;
  MIDP_EVENT_INITIALIZE(evt);
  evt.type = MIDP_INVALIDATE_EVENT;
  midpStoreEventAndSignalForeground(evt);
  */
}

//------------------

JForm *JForm::currentForm = NULL;

extern "C"
{
  MidpError lfpport_form_create(MidpDisplayable *formPtr, const pcsl_string *title, const pcsl_string *ticker)
  {
    debug_trace();

    JForm *form = new JForm(JDisplay::current(), formPtr,
                            pcsl_string2QString(*title), pcsl_string2QString(*ticker));

    if (!form)
      return KNI_ENOMEM;
    else
      return KNI_OK;
  }

  MidpError lfpport_form_set_content_size(MidpDisplayable *formPtr, int w, int h)
  {
    debug_trace();
    JForm *form = (static_cast<JDisplayable *>(formPtr->frame.widgetPtr))->toForm();
    return form->setContentSize(w, h);
  }

  MidpError lfpport_form_set_current_item(MidpItem *itemPtr, int yOffset)
  {
    debug_trace();
    JForm *form = JForm::current();
    if (form)
      return form->setCurrentItem(qobject_cast<JItem*>(static_cast<QObject *>(itemPtr->widgetPtr)), yOffset);
    else
      return KNI_OK;
  }

  MidpError lfpport_form_get_scroll_position(int *pos)
  {
    debug_trace();
    JForm *form = JForm::current();
    if (form)
      *pos = form->getScrollPosition();
    return KNI_OK;
  }

  MidpError lfpport_form_set_scroll_position(int pos)
  {
    debug_trace();
    JForm *form = JForm::current();
    if (form)
      return form->setScrollPosition(pos);
    else
      return KNI_OK;
  }

  MidpError lfpport_form_get_viewport_height(int *height)
  {
    debug_trace();
    JForm *form = JForm::current();
    if (form)
      *height = form->viewportHeight();
    lfpport_log("Viewport height reported %d\n", *height);
    return KNI_OK;
  }
}

JForm::JForm(QWidget *parent, MidpDisplayable *disp, QString title, QString ticker)
  : JDisplayable(disp, title, ticker), QWidget(parent)
{
  form = this;

  JDisplay::current()->addWidget(this);

  QVBoxLayout *layout = new QVBoxLayout(this);

  //w_title = new QLabel(title, this);
  //w_title->setTextFormat(Qt::PlainText);
  w_ticker = new QLabel(ticker, this);
  w_ticker->setTextFormat(Qt::PlainText);

  //w_scroller = new QScrollArea(this);
  w_scroller = new JFormScrollArea(this);
  w_scroller->setFrameStyle(QFrame::Plain | QFrame::StyledPanel);
  w_scroller->setFocusPolicy(Qt::NoFocus);
  //w_scroller->setHorizontalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
  //w_viewport = new QWidget(w_scroller->viewport());
  w_viewport = new JFormViewport(w_scroller->viewport());
  w_viewport->resize(30, 30);
  w_scroller->setWidget(w_viewport);
  w_scroller->setWidgetResizable(true);

  //layout->addWidget(w_title);
  layout->addWidget(w_ticker);
  layout->addWidget(w_scroller);

  javaTitleChanged();
  javaTickerChanged();

  lfpport_log("JForm frame width %d\n", w_scroller->frameWidth());

  //currentForm = this;
}

JForm::~JForm()
{
  currentForm = NULL;
}

JForm *JForm::current()
{
  if (!currentForm)
    lfpport_log("!!! INVALID CURRENT FORM\n");
  return currentForm;
}

int JForm::viewportWidth()
{
  return w_scroller->viewport()->width();
}

int JForm::viewportHeight()
{
  return w_scroller->viewport()->height();
}


MidpError JForm::setCurrentItem(JItem *item, int y_offset)
{
  w_scroller->ensureVisible(0, item->y()+y_offset);
  item->setFocus(Qt::OtherFocusReason);
  return KNI_OK;
}

MidpError JForm::setContentSize(int w, int h)
{
  lfpport_log("JForm::setContentSize(%d, %d)\n", w, h);
  w_viewport->setFixedSize(w, h);
  return KNI_OK;
}

int JForm::getScrollPosition()
{
  return w_scroller->verticalScrollBar()->value();
}

MidpError JForm::setScrollPosition(int pos)
{
  w_scroller->verticalScrollBar()->setValue(pos);
  return KNI_OK;
}

MidpError JForm::j_show()
{
  JDisplay::current()->setCurrentWidget(this);
  currentForm = this;
  return KNI_OK;
}

MidpError JForm::j_hideAndDelete(jboolean onExit)
{
  delete this;
  if (currentForm==this)
    currentForm = NULL;
  return KNI_OK;
}

void JForm::javaTickerChanged()
{
  if (ticker().isEmpty())
    w_ticker->hide();
  else
  {
    w_ticker->setText(ticker());
    if (w_ticker->isHidden())
      w_ticker->show();
  }
}

void JForm::showEvent(QShowEvent *event)
{
  lfpport_log("JForm::showEvent\n");
  javaTitleChanged();
}

#include "lfpport_qtopia_form.moc"
