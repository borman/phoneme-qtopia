#ifndef _LFPPORT_QTOPIA_DATEFIELD_H_
#define _LFPPORT_QTOPIA_DATEFIELD_H_

#include "lfpport_qtopia_item.h"

class QDateTimeEdit;
class QLabel;

class JDateField: public JItem
{
  Q_OBJECT
  public:
    JDateField(MidpItem *item, JForm *form, const QString &label, int layout, 
               int input_mode, long time, const QString &timeZone);
    virtual ~JDateField();
    
    void j_setLabel(const QString &text);
    long j_dateTime();
    void j_setDateTime(long date);
    void j_setInputMode(int mode);
  private:
    enum InputMode
    {
      DATE = 1,
      TIME = 2,
      DATE_TIME = 3
    };
    QDateTimeEdit *dtedit;
    QLabel *label;
};

#endif // #ifndef _LFPPORT_QTOPIA_DATEFIELD_H_
