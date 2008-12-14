#ifndef _LFPPORT_QTOPIA_TEXTFIELD_H_
#define _LFPPORT_QTOPIA_TEXTFIELD_H_

#include "lfpport_qtopia_item.h"
#include "lfpport_qtopia_util_expandable_textedit.h"

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

  private slots:
    void contentsModified();
  private:
    QLabel *tf_label;
    ExpandableTextEdit *tf_body;
    bool cont_changed;
};

#endif // _LFPPORT_QTOPIA_TEXTFIELD_H_

