#ifndef _LFPPORT_QTOPIA_TEXTFIELD_H_
#define _LFPPORT_QTOPIA_TEXTFIELD_H_

#include "lfpport_qtopia_item.h"
class QLabel;
class QTextEdit;
class QObject;
class QEvent;
class QString;
class ExpandableTextEdit;

class JTextField: public JItem
{
  Q_OBJECT
  public:
    JTextField(MidpItem *item, JForm *form,
               QString labelText, int layout, QString text,
               int maxSize, int constraints, QString initialInputMode);
    virtual ~JTextField();

    void j_setLabel(const QString &text);

    MidpError setString(const QString &text);
    QString getString(jboolean *changed);
    MidpError setMaxSize(int size);
    int getCaretPosition();
    MidpError setConstraints(int constr);
    
    virtual bool eventFilter(QObject *watched, QEvent *event);

  private slots:
    void contentsModified();
  protected:
    void showEvent(QShowEvent *);
  private:
    void checkSize();
    
    QLabel *tf_label;
//    QTextEdit
	ExpandableTextEdit	*tf_body;
    bool cont_changed;
};

#endif // _LFPPORT_QTOPIA_TEXTFIELD_H_

