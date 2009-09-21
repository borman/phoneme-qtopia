#include <QWidget>
#include <QString>
#include <QFormLayout>
#include <QPushButton>
#include <QLabel>
#include <QPixmap>
#include <QStackedWidget>
#include <QTextEdit>
#include <QEvent>
#include <QFocusEvent>
#include <QDebug>
#include <jimmutableimage.h>
#include <lfpport_stringitem.h>

#include "lfpport_qtopia_stringimageitem.h"
#include "lfpport_qtopia_util_expandable_textedit.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_debug.h"

extern "C"
{
  MidpError lfpport_stringitem_create(MidpItem* itemPtr,
            MidpDisplayable* ownerPtr,
            const pcsl_string* label, int layout,
            const pcsl_string* text,
            PlatformFontPtr fontPtr,
            int appearanceMode)
  {
    debug_trace();
    JDisplayable *disp = static_cast<JDisplayable *>(ownerPtr->frame.widgetPtr);
    JStringImageItem *sitem = new JStringImageItem(itemPtr, disp->toForm(),
                                                   pcsl_string2QString(*label), pcsl_string2QString(*text), NULL,
                                                   static_cast<QFont *>(fontPtr), appearanceMode);
    if (!sitem)
      return KNI_ENOMEM;
    return KNI_OK;
  }

  MidpError lfpport_stringitem_set_content(MidpItem* itemPtr,
                      const pcsl_string* text,
                      int appearanceMode)
  {
    debug_trace();
    JStringImageItem *sitem = static_cast<JStringImageItem *>(itemPtr->widgetPtr);
    sitem->j_setText(pcsl_string2QString(*text), appearanceMode);
    return KNI_OK;
  }

  MidpError lfpport_stringitem_set_font(MidpItem* itemPtr,
                        PlatformFontPtr fontPtr)
  {
    debug_trace();
    JStringImageItem *sitem = static_cast<JStringImageItem *>(itemPtr->widgetPtr);
    sitem->j_setFont(static_cast<QFont *>(fontPtr));
    return KNI_OK;
  }

  MidpError lfpport_imageitem_create(MidpItem* itemPtr,
                                     MidpDisplayable* ownerPtr,
                                     const pcsl_string* label, int layout,
                                     unsigned char* imgPtr,
                                     const pcsl_string* altText, int appearanceMode)
  {
    debug_trace();
    (void)altText;
    JDisplayable *disp = static_cast<JDisplayable *>(ownerPtr->frame.widgetPtr);
    JStringImageItem *sitem = new JStringImageItem(itemPtr, disp->toForm(),
        pcsl_string2QString(*label), QString::null, JIMMutableImage::fromHandle(imgPtr),
                             NULL, appearanceMode);
    if (!sitem)
      return KNI_ENOMEM;
    return KNI_OK;
  }

  MidpError lfpport_imageitem_set_content(MidpItem* itemPtr,
                                          unsigned char* imgPtr,
                                          const pcsl_string* altText,
                                          int appearanceMode)
  {
    debug_trace(); 
    JStringImageItem *sitem = static_cast<JStringImageItem *>(itemPtr->widgetPtr);
    sitem->j_setPixmap(JIMMutableImage::fromHandle(imgPtr), pcsl_string2QString(*altText), appearanceMode);
    return KNI_OK;
  }
}

JStringImageItem::JStringImageItem(MidpItem *item, JForm *form, const QString &v_label, const QString &v_text,
                                   QPixmap *v_pixmap,
                                   QFont *font, int apprMode)
  : JItem(item, form), text(v_text), label(v_label), appearanceMode(apprMode), pixmap(v_pixmap),
    w_label(NULL), w_pixmap(NULL), w_text(NULL), w_button(NULL)
{
  qDebug("JStringImageItem(), apprMode==%d", apprMode);

  layout = new QFormLayout(this);
  layout->setRowWrapPolicy(QFormLayout::WrapAllRows);
  
//if (font)
//      Comment: fix app crash
//    this->font = *font;

  j_setFont(font);

  updateContents();
}

JStringImageItem::~JStringImageItem()
{
}

void JStringImageItem::j_setLabel(const QString &text)
{
  this->label = text;
  updateContents();
}

void JStringImageItem::j_setFont(QFont *font)
{
  if (!font)
    return;
  if (w_button)
    w_button->setFont(*font);
  if (w_pixmap)
    w_pixmap->setFont(*font);
  if (w_text)
    w_text->setCurrentFont(*font);
}

void JStringImageItem::j_setText(const QString &text, int appearanceMode)
{
  qDebug("JStringImageItem::j_setText");
  if (this->pixmap)
  {
    delete this->pixmap;
    this->pixmap = NULL;
  }

  qDebug("JStringImageItem::j_setText: appearanceMode=%d", appearanceMode);

  this->text = text;
  this->appearanceMode = appearanceMode;
  updateContents();
}

void JStringImageItem::j_setPixmap(QPixmap *pixmap, const QString &text, int appearanceMode)
{
  if (this->pixmap)
  {
    delete this->pixmap;
    this->pixmap = NULL;
  }

  qDebug("JStringImageItem::j_setPixmap: appearanceMode=%d", appearanceMode);

  this->pixmap = pixmap;
  this->text = text;
  this->appearanceMode = appearanceMode;
  updateContents();
}

void JStringImageItem::updateContents()
{
  qDebug("Grrrrrrrrr");
  if (appearanceMode==Button)
  {
    initButton(); 
  }
  else
  {
    if (pixmap)
      initPixmap();
    else
      initText();
  }
  
  w_label->setText(label);
  if (label.isEmpty())
    w_label->hide();
  else
    w_label->show();
    
  if (pixmap) // Pixmal label
  {
    switch (appearanceMode)
    {
      case Hyperlink:
      case Plain:
        w_pixmap->setPixmap(*pixmap);
        break;
      case Button:
        w_button->setIcon(QIcon(*pixmap));
    }
  }
  else // Text label
  {
    QString t_text = text;
    switch (appearanceMode)
    {
      case Hyperlink:
        t_text = QString("<a name=\"link\" href=\"#link\">%1</a>").arg(t_text.trimmed());
        w_text->setHtml(t_text);
        break;
      case Plain:
        w_text->setPlainText(t_text.trimmed());
        break;
      case Button:
        w_button->setText(t_text);
    }
  }
  
  checkSize();
}

void JStringImageItem::initPixmap()
{
  if (w_pixmap)
    return;
  debug_trace();
  w_pixmap = new QLabel(this);
  w_pixmap->setWordWrap(true);
  w_pixmap->setFocusPolicy(Qt::StrongFocus);
  j_setFont(&font);
  
  w_label = new QLabel(this);
  w_label->setBuddy(w_pixmap);
  w_label->setWordWrap(true);
  
  layout->addRow(w_label, w_text);
}

void JStringImageItem::initButton()
{
  if (w_button)
    return;
  debug_trace();
  w_button = new QPushButton(this);
  j_setFont(&font);
  
  w_label = new QLabel(this);
  w_label->setBuddy(w_button);
  w_label->setWordWrap(true);
  
  layout->addRow(w_label, w_button);
}

void JStringImageItem::initText()
{
  if (w_text)
    return;
  debug_trace();
  //w_text = new QTextEdit(w_switch);
  w_text = new ExpandableTextEdit(this);
  w_text->setReadOnly(true);
  j_setFont(&font);
  
  w_label = new QLabel(this);
  w_label->setBuddy(w_text);
  w_label->setWordWrap(true);
  
  layout->addRow(w_label, w_text);
}

bool JStringImageItem::eventFilter(QObject *watched, QEvent *event)
{
  if (event->type()==QEvent::FocusIn)
  {
    qDebug("JStringImageItem: caught child *FocusIn*");
    QFocusEvent *f_event = static_cast<QFocusEvent *>(event);
    if (f_event->reason()!=Qt::OtherFocusReason)
    {
      qDebug("JStringImageItem: Non-synthetic event, notifying VM");
      notifyFocusIn();
    }
  }
  return false;
}

void JStringImageItem::showEvent(QShowEvent *)
{
  checkSize();
}

void JStringImageItem::checkSize()
{
  if (w_text)
    w_text->updateGeometry();
  qDebug("JStringImageItem: sizeHint (%dx%d)", sizeHint().width(), sizeHint().height());
  if (sizeHint().height() != height())
  {
    qDebug("JStringImageItem: asking for resize");
    form->requestInvalidate();
  }
}

#include "lfpport_qtopia_stringimageitem.moc"
