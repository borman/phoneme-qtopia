#include "lfpport_qtopia_gauge.h"
#include <lfpport_gauge.h>
#include <QFormLayout>

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
    JGauge *gauge;
    if (interactive)
      gauge = new JInteractiveGauge(gaugePtr, (JForm*)ownerPtr->widgetPtr,
                                    pscl_string2QString(*label), layout, maxValue, initialValue);
    else
    {
      if (maxValue>0)
        gauge = new JProgressiveGauge(gaugePtr, (JForm*)ownerPtr->widgetPtr,
                                      pscl_string2QString(*label), layout, maxValue, initialValue);
      else
        gauge = new JIndefiniteGauge(gaugePtr, (JForm*)ownerPtr->widgetPtr,
                                     pscl_string2QString(*label), layout, initialValue);
    }
  }

  MidpError lfpport_gauge_set_value(MidpItem *gaugePtr, int value, int maxValue)
  {
    JGauge *gauge = (JGauge *)gaugePtr->widgetPtr;
    gauge->setValue(value, maxValue);
    return KNI_OK;
  }

  MidpError lfpport_gauge_get_value(int *value, MidpItem *gaugePtr)
  {
    JGauge *gauge = (JGauge *)gaugePtr->widgetPtr;
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

// JInteractiveGauge

JInteractiveGauge::JInteractiveGauge(MidpItem *item, JForm *form,
                  QString labelText, int layout, int maxValue, int initialValue)
  : JGauge(item, form)
{
  QFormLayout *layout = new QFormLayout(this);
  layout->setRowWrapPolicy(QFormLayout::WrapAllRows);
  label = new QLabel(labelText, this);
  slider = new QSlider(this);
  slider->setRange(0, maxValue);
  slider->setValue(initialValue);
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
                                     QString labelText, int layout, int maxValue, int initialValue)
  : JGauge(item, form)
{
  QFormLayout *layout = new QFormLayout(this);
  layout->setRowWrapPolicy(QFormLayout::WrapAllRows);
  label = new QLabel(labelText, this);
  pbar = new QProgressBar(this);
  pbar->setRange(0, maxValue);
  pbar->setValue(initialValue);
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

JIndefiniteGauge::JIndefiniteGauge(MidpItem *item, JForm *form,
                                     QString labelText, int layout, int initialValue)
  : JGauge(item, form)
{
  QFormLayout *layout = new QFormLayout(this);
  layout->setRowWrapPolicy(QFormLayout::WrapAllRows);
  label = new QLabel(labelText, this);
  pbar = new QProgressBar(this);
  pbar->setRange(0, INFINITE_GAUGE_LENGTH);
  pbar->setValue(0);

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
}

void JIndefiniteGauge::step();
{
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
    stepTimer.start(INDEFINITE_GAUGE_TIMESTEP);
  }
  else
    stepTimer.stop();
}