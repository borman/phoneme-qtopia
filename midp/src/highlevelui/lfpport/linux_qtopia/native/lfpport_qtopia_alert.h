#ifndef _LFPPORT_QTOPIA_ALERT_H_
#define _LFPPORT_QTOPIA_ALERT_H_

#include <QDialog>
#include "lfpport_qtopia_displayable.h"

class JAlert: public QDialog, public JDisplayable
{
  Q_OBJECT
  public:
    enum Type
    {
      Null,
      Info,
      Warning,
      Error,
      Alarm,
      Confirmation
    };

    JAlert(QWidget *parent, MidpDisplayable *alertDisp, QString title, QString ticker, Type type);
    virtual ~JAlert();

    MidpError j_show();
    MidpError j_hideAndDelete(jboolean onExit);
};

#endif // _LFPPORT_QTOPIA_ALERT_H_

