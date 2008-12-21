#include <cstdio>

#include <QWidget>
#include <QString>
#include <QFormLayout>
#include <QPushButton>
#include <QLabel>
#include <QPixmap>
#include <QStackedWidget>

#include <jgraphics.h>
#include <lfpport_stringitem.h>

#include "lfpport_qtopia_stringimageitem.h"
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
    JStringImageItem *sitem = static_cast<JStringImageItem *>(itemPtr->widgetPtr);
    sitem->j_setText(pcsl_string2QString(*text), appearanceMode);
    return KNI_OK;
  }

  MidpError lfpport_stringitem_set_font(MidpItem* itemPtr,
                        PlatformFontPtr fontPtr)
  {
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
        pcsl_string2QString(*label), QString::null, JGraphics::immutablePixmap(imgPtr),
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
    JStringImageItem *sitem = static_cast<JStringImageItem *>(itemPtr->widgetPtr);
    sitem->j_setPixmap(JGraphics::immutablePixmap(imgPtr), pcsl_string2QString(*altText), appearanceMode);
    return KNI_OK;
  }
}

JStringImageItem::JStringImageItem(MidpItem *item, JForm *form, const QString &v_label, const QString &v_text,
                                   QPixmap *v_pixmap,
                                   QFont *font, int apprMode)
  : JItem(item, form), text(v_text), label(v_label), appearanceMode(apprMode), pixmap(v_pixmap)
{
  printf("JStringImageItem(), apprMode==%d\n", apprMode);

  layout = new QFormLayout(this);

  w_label = new QLabel(this);
  w_switch = new QStackedWidget(this);
  w_text = new QLabel(w_switch);
  w_button = new QPushButton(w_switch);
  w_switch->addWidget(w_text);
  w_switch->addWidget(w_button);
  w_label->setBuddy(w_switch);
  w_label->setWordWrap(true);
  layout->addRow(w_label, w_switch);

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
  w_button->setFont(*font);
  w_text->setFont(*font);
}

void JStringImageItem::j_setText(const QString &text, int appearanceMode)
{
  if (this->pixmap)
  {
    delete this->pixmap;
    this->pixmap = NULL;
  }

  printf("JStringImageItem::j_setText: appearanceMode=%d\n", appearanceMode);

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

  printf("JStringImageItem::j_setPixmap: appearanceMode=%d\n", appearanceMode);

  this->pixmap = pixmap;
  this->text = text;
  this->appearanceMode = appearanceMode;
  updateContents();
}

void JStringImageItem::updateContents()
{
  w_label->setText(label);
  if (pixmap) // Pixmal label
  {
    switch (appearanceMode)
    {
      case Hyperlink:
      case Plain:
        w_text->setPixmap(*pixmap);
        w_switch->setCurrentWidget(w_text);
        break;
      case Button:
        w_button->setIcon(QIcon(*pixmap));
        w_switch->setCurrentWidget(w_button);
    }
  }
  else // Text label
  {
    QString t_text = text;
    switch (appearanceMode)
    {
      case Hyperlink:
        t_text = QString("<a>%1</a>").arg(t_text);
      case Plain:
        w_text->setText(t_text);
        w_switch->setCurrentWidget(w_text);
        break;
      case Button:
        w_button->setText(t_text);
        w_switch->setCurrentWidget(w_button);
    }
  }
}

#include "lfpport_qtopia_stringimageitem.moc"
