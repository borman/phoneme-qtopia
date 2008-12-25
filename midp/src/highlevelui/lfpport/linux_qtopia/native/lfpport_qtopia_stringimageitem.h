#ifndef _LFPPORT_QTOPIA_STRINGIMAGEITEM_H_
#define _LFPPORT_QTOPIA_STRINGIMAGEITEM_H_

#include <QString>
#include "lfpport_qtopia_item.h"

class QFormLayout;
class QLabel;
class QStackedWidget;
class QPushButton;
class QPixmap;
class QTextEdit;

class JStringImageItem: public JItem
{
  Q_OBJECT
  public:
    enum AppearanceMode
    {
      Plain = 0,
      Hyperlink = 1,
      Button = 2
    };

    JStringImageItem(MidpItem *item, JForm *form, const QString &label, const QString &text, QPixmap *pixmap,
                QFont *font, int appearanceMode);
    virtual ~JStringImageItem();

    virtual void j_setLabel(const QString &text);
    void j_setFont(QFont *font);
    void j_setText(const QString &text, int appearanceMode);
    void j_setPixmap(QPixmap *pixmap, const QString &text, int appearanceMode);
    
    bool eventFilter(QObject *watched, QEvent *event);
  private:
    void initPixmap();
    void initButton();
    void initText();
    
    void updateContents();

    int appearanceMode;

    QPixmap *pixmap;

    QString label;
    QString text;
    QFont font;

    QFormLayout *layout;
    QLabel *w_label;
    //QStackedWidget *w_switch;
    
    QLabel *w_pixmap;
    QTextEdit *w_text;
    QPushButton *w_button;
};

#endif // _LFPPORT_QTOPIA_STRINGIMAGEITEM_H_
