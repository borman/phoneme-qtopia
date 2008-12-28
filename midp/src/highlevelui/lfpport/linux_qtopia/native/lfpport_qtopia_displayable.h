#ifndef _LFPPORT_QTOPIA_DISPLAYABLE_H_
#define _LFPPORT_QTOPIA_DISPLAYABLE_H_

#include <lfpport_component.h>
#include <lfpport_displayable.h>
#include <lfpport_error.h>

#include <QWidget>
class JForm;

class JDisplayable
{
  public:
    JDisplayable(MidpDisplayable *frame, QString title=QString::null, QString ticker=QString::null);
    virtual ~JDisplayable();

    //  MidpFrame interface
    virtual MidpError j_show() = 0;
    virtual MidpError j_hideAndDelete(jboolean onExit) = 0;

    MidpError setTitle(const QString &text);
    MidpError setTicker(const QString &text);

    QString title() const;
    QString ticker() const;
    
    JForm *toForm() const;
  protected:
    virtual void javaTitleChanged();
    virtual void javaTickerChanged() {};
    
    JForm *form;
  private:
    QString m_title;
    QString m_ticker;
};

#endif //_LFPPORT_QTOPIA_DISPLAYABLE_H_
