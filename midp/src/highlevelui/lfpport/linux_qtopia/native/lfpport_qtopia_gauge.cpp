#include <QFormLayout>
#include <QEvent>
#include <QFocusEvent>

#include <lfpport_gauge.h>

#include "lfpport_qtopia_gauge.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_debug.h"

#define INFINITE_GAUGE_LENGTH 10
#define INFINITE_GAUGE_TIMESTEP 500

#define CONTINUOUS_IDLE   0
#define INCREMENTAL_IDLE    1
#define CONTINUOUS_RUNNING    2
#define INCREMENTAL_UPDATING    3

extern "C"
{
  MidpError lfpport_gauge_create(MidpItem *gaugePtr, MidpDisplayable *ownerPtr,
                                 const pcsl_string *label, int layout, jboolean interactive, int maxValue, int initialValue)
  {
    debug_trace();
    JDisplayable *disp = static_cast<JDisplayable *>(ownerPtr->frame.widgetPtr);
    JGauge *gauge;
    if (interactive)
      gauge = new JInteractiveGauge(gaugePtr, disp->toForm(),
                                    pcsl_string2QString(*label), layout, maxValue, initialValue);
    else
    {
      if (maxValue>0)
        gauge = new JProgressiveGauge(gaugePtr, disp->toForm(),
                                      pcsl_string2QString(*label), layout, maxValue, initialValue);
      else
        gauge = new JIndefiniteGauge(gaugePtr, disp->toForm(),
                                     pcsl_string2QString(*label), layout, initialValue);
    }
    return KNI_OK;
  }

  MidpError lfpport_gauge_set_value(MidpItem *gaugePtr, int value, int maxValue)
  {
    JGauge *gauge = static_cast<JGauge *>(gaugePtr->widgetPtr);
    gauge->setValue(value, maxValue);
    return KNI_OK;
  }

  MidpError lfpport_gauge_get_value(int *value, MidpItem *gaugePtr)
  {
    JGauge *gauge = static_cast<JGauge *>(gaugePtr->widgetPtr);
    *value = gauge->value();
    return KNI_OK;
  }
}

// JGauge

JGauge::JGauge(MidpItem *item, JForm *form)
  : JItem(item, form)
{
}

JGauge::~JGauge()
{
}

bool JGauge::eventFilter(QObject *watched, QEvent *event)
{
  if (event->type()==QEvent::FocusIn)
  {
    lfpport_log("JGauge: caught child *FocusIn*\n");
    QFocusEvent *f_event = static_cast<QFocusEvent *>(event);
    if (f_event->reason()!=Qt::OtherFocusReason)
    {
      lfpport_log("JGauge: Non-synthetic event, notifying VM\n");
      notifyFocusIn();
    }
  }
  return false;
}

// JInteractiveGauge

JInteractiveGauge::JInteractiveGauge(MidpItem *item, JForm *form,
                  QString labelText, int j_layout, int maxValue, int initialValue)
  : JGauge(item, form)
{
  QFormLayout *layout = new QFormLayout(this);
  layout->setRowWrapPolicy(QFormLayout::WrapAllRows);
  label = new QLabel(labelText, this);
  
  slider = new QSlider(this);
  slider->setRange(0, maxValue);
  slider->setValue(initialValue);
  slider->setOrientation(Qt::Horizontal);
  slider->installEventFilter(this);
  setFocusProxy(slider);
  
  label->setBuddy(slider);
  layout->addRow(label, slider);
}

JInteractiveGauge::~JInteractiveGauge()
{
}

void JInteractiveGauge::j_setLabel(const QString &text)
{
  label->setText(text);
}

int JInteractiveGauge::value()
{
  return slider->value();
}

void JInteractiveGauge::setValue(int val, int maxval)
{
  slider->setMaximum(maxval);
  slider->setValue(val);
}

// JProgressiveGauge

JProgressiveGauge::JProgressiveGauge(MidpItem *item, JForm *form,
                                     QString labelText, int j_layout, int maxValue, int initialValue)
  : JGauge(item, form)
{
  QFormLayout *layout = new QFormLayout(this);
  layout->setRowWrapPolicy(QFormLayout::WrapAllRows);
  label = new QLabel(labelText, this);
  
  pbar = new QProgressBar(this);
  pbar->setRange(0, maxValue);
  pbar->setValue(initialValue);
  pbar->setFocusPolicy(Qt::StrongFocus);
  pbar->installEventFilter(this);
  
  label->setBuddy(pbar);
  layout->addRow(label, pbar);
}

JProgressiveGauge::~JProgressiveGauge()
{
}

void JProgressiveGauge::j_setLabel(const QString &text)
{
  label->setText(text);
}

int JProgressiveGauge::value()
{
  return pbar->value();
}

void JProgressiveGauge::setValue(int val, int maxval)
{
  pbar->setMaximum(maxval);
  pbar->setValue(val);
}

// JIndefiniteGauge

JIndefiniteGauge::JIndefiniteGauge(MidpItem *item, JForm *form, QString labelText, int j_layout, int initialValue)
  : JGauge(item, form)
{
  QFormLayout *layout = new QFormLayout(this);
  layout->setRowWrapPolicy(QFormLayout::WrapAllRows);
  label = new QLabel(labelText, this);
  
  pbar = new QProgressBar(this);
  pbar->setRange(0, 0);
  pbar->setValue(0);
  pbar->setFocusPolicy(Qt::StrongFocus);
  pbar->installEventFilter(this);

  state = initialValue;
  increment = 1;

  label->setBuddy(pbar);
  layout->addRow(label, pbar);
  connect(&stepTimer, SIGNAL(timeout()), SLOT(step()));
  step();
}

JIndefiniteGauge::~JIndefiniteGauge()
{
}

void JIndefiniteGauge::j_setLabel(const QString &text)
{
  label->setText(text);
}

int JIndefiniteGauge::value()
{
  return state;
}

void JIndefiniteGauge::setValue(int val, int maxval)
{
  (void)maxval;
  state = val;
  step();
}

void JIndefiniteGauge::step()
{
  lfpport_log("JIndefiniteGauge::step()\n");
  if (state==CONTINUOUS_RUNNING || state==INCREMENTAL_UPDATING) // Go to next animation step
  {
    int newValue = pbar->value()+increment;
    if (newValue<0 || newValue>pbar->maximum())
    {
      pbar->setInvertedAppearance(pbar->invertedAppearance());
      increment = -increment;
      newValue += increment;
    }
    pbar->setValue(newValue);
  }
  if (state==CONTINUOUS_RUNNING)
  {
    stepTimer.setSingleShot(true);
    stepTimer.start(INFINITE_GAUGE_TIMESTEP);
  }
  else
    stepTimer.stop();
}

#include "lfpport_qtopia_gauge.moc"
