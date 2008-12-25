#ifndef _LFPPORT_QTOPIA_GAUGE_H_
#define _LFPPORT_QTOPIA_GAUGE_H_

#include "lfpport_qtopia_item.h"
#include <QSlider>
#include <QProgressBar>
#include <QTimer>

// A virtual interface for a Gauge.
// Gauge's look-and-feel is set in its subclasses
class JGauge: public JItem
{
  Q_OBJECT
  public:
    JGauge(MidpItem *item, JForm *form);
    virtual ~JGauge();

    virtual int value() = 0;
    virtual void setValue(int val, int maxval) = 0;
    
    bool eventFilter(QObject *watched, QEvent *event);
};

// An interactive gauge.
// Uses QSlider widget
class JInteractiveGauge: public JGauge
{
  Q_OBJECT
  public:
    JInteractiveGauge(MidpItem *item, JForm *form,
                      QString label, int layout, int maxValue, int initialValue);
    virtual ~JInteractiveGauge();

    int value();
    void setValue(int val, int maxval);
    void j_setLabel(const QString &text);
  private:
    int m_value;
    int m_maxValue;
    QSlider *slider;
    QLabel *label;
};

// A non-interative definite gauge
// Uses QProgressBar widget
class JProgressiveGauge: public JGauge
{
  Q_OBJECT
  public:
    JProgressiveGauge(MidpItem *item, JForm *form,
                      QString label, int layout, int maxValue, int initialValue);
    virtual ~JProgressiveGauge();

    int value();
    void setValue(int val, int maxval);
    void j_setLabel(const QString &text);
  private:
    int m_value;
    int m_maxValue;
    QProgressBar *pbar;
    QLabel *label;
};

// A non-interactive indefinite gauge
// Uses QWaitWidget widget
class JIndefiniteGauge: public JGauge
{
  Q_OBJECT
  public:
    JIndefiniteGauge(MidpItem *item, JForm *form,
                     QString label, int layout, int initialValue);
    virtual ~JIndefiniteGauge();

    int value();
    void setValue(int val, int maxval);
    void j_setLabel(const QString &text);
  private slots:
    void step();
  private:
    int m_value;
    int m_maxValue;
    QProgressBar *pbar;
    QLabel *label;
    int increment;
    int state;
    QTimer stepTimer;
};

#endif // _LFPPORT_QTOPIA_GAUGE_H_
