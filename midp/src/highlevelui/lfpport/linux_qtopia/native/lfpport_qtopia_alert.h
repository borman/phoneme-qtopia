#ifndef _LFPPORT_QTOPIA_ALERT_H_
#define _LFPPORT_QTOPIA_ALERT_H_

#include <QDialog>
#include <lfpport_component.h>
#include "lfpport_qtopia_displayable.h"

class QLabel;

class JAlert: public QDialog, public JDisplayable
{
  Q_OBJECT
  public:
    JAlert(QWidget *parent, MidpDisplayable *alertDisp, QString title, QString ticker, MidpComponentType type);
    virtual ~JAlert();

    MidpError j_show();
    MidpError j_hideAndDelete(jboolean onExit);
    MidpError j_setContents(const QPixmap *image, const QRect &gauge, const QString &text);
  private:
    QLabel *label;
};

#endif // _LFPPORT_QTOPIA_ALERT_H_

