#ifndef _LFPPORT_QTOPIA_UTIL_EXPANDABLE_TEXTEDIT_H_
#define _LFPPORT_QTOPIA_UTIL_EXPANDABLE_TEXTEDIT_H_

#include <QTextEdit>

// A simple hack for QTextEdit
// This patched version resizes itself to fit the contents. No scrolling is used inside it
// It relies on scrolling facilities of the parent widget
class ExpandableTextEdit: public QTextEdit
{
  Q_OBJECT
  public:
    ExpandableTextEdit(QWidget *parent = 0);
    ExpandableTextEdit(const QString &text, QWidget *parent = 0);
    virtual ~ExpandableTextEdit();
    
    QSize sizeHint() const;
    QSize minimumSizeHint() const;
  private:
    void init();
  private slots:
    void checkHeight();
};

#endif // _LFPPORT_QTOPIA_UTIL_EXPANDABLE_TEXTEDIT_H_
