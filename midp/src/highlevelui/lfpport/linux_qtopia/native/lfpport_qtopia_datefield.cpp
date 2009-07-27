#include <QDateTimeEdit>
#include <QDateTime>
#include <QFormLayout>
#include <QLabel>
#include <QLocale>

#include <lfpport_datefield.h>

#include "lfpport_qtopia_datefield.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_debug.h"

extern "C"
{
  MidpError lfpport_datefield_create(MidpItem* datefieldPtr,
        MidpDisplayable* ownerPtr,
        const pcsl_string* label, int layout,
        int input_mode,
        long time, const pcsl_string* timeZone)
  {
    debug_trace();
    JDisplayable *disp = static_cast<JDisplayable *>(ownerPtr->frame.widgetPtr);
    JDateField *df = new JDateField(datefieldPtr, disp->toForm(),
                                    pcsl_string2QString(*label), layout,
                                    input_mode, time, pcsl_string2QString(*timeZone));
    if (!df)
      return KNI_ENOMEM;
    return KNI_OK;
  }

  MidpError lfpport_datefield_set_date(MidpItem* datefieldPtr, long time)
  {
    JDateField *df = static_cast<JDateField *>(datefieldPtr->widgetPtr);
    df->j_setDateTime(time);
    return KNI_OK;
  }

  MidpError lfpport_datefield_get_date(long* time, MidpItem* datefieldPtr)
  {
    JDateField *df = static_cast<JDateField *>(datefieldPtr->widgetPtr);
    *time = df->j_dateTime();
    return KNI_OK;
  }

  MidpError lfpport_datefield_set_input_mode(MidpItem* datefieldPtr, int mode)
  {
    JDateField *df = static_cast<JDateField *>(datefieldPtr->widgetPtr);
    df->j_setInputMode(mode);
    return KNI_OK;
  }
}

JDateField::JDateField(MidpItem *item, JForm *form, const QString &labelText, int layout,
               int input_mode, long time, const QString &timeZone)
  : JItem(item, form)
{
  (void)layout;

  QFormLayout *flayout = new QFormLayout(this);
  flayout->setRowWrapPolicy(QFormLayout::WrapAllRows);
  label = new QLabel(labelText, this);
  dtedit = new QDateTimeEdit(this);
  label->setBuddy(dtedit);
  label->setWordWrap(true);
  flayout->addRow(label, dtedit);

  qDebug("JDateField(): tz \"%s\"", timeZone.toUtf8().constData());

  j_setDateTime(time);
  j_setInputMode(input_mode);
}

JDateField::~JDateField()
{
}

void JDateField::j_setLabel(const QString &text)
{
  label->setText(text);
}

long JDateField::j_dateTime()
{
  QDateTime dt = dtedit->dateTime();
  return dt.toTime_t();
}

void JDateField::j_setDateTime(long date)
{
  QDateTime dt;
  dt.setTime_t(date);
  dtedit->setDateTime(dt);
}

void JDateField::j_setInputMode(int mode)
{
  QLocale locale = QLocale::system();
  switch (mode)
  {
    case DATE:
      dtedit->setDisplayFormat(locale.dateFormat());
      break;
    case TIME:
      dtedit->setDisplayFormat(locale.timeFormat());
      break;
    case DATE_TIME:
      dtedit->setDisplayFormat(locale.dateFormat() + " " + locale.timeFormat());
      break;
    default:
      qCritical("JDateField::j_setInputMode: invalid mode %d\n", mode);
  }
}

#include "lfpport_qtopia_datefield.moc"
