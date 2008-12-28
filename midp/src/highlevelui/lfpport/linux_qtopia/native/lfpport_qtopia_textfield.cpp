#include <QFormLayout>
#include <QEvent>
#include <QFocusEvent>
#include <lfpport_textfield.h>
#include <lfpport_form.h>

#include "lfpport_qtopia_util_expandable_textedit.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_textfield.h"
#include "lfpport_qtopia_debug.h"

//!TODO input constraints, size constraints

extern "C"
{
  MidpError lfpport_textfield_create(MidpItem *itemPtr, MidpDisplayable *ownerPtr, const pcsl_string *label,
                                     int layout, const pcsl_string *text, int maxSize, int constraints,
                                     const pcsl_string *initialInputMode)
  {
    debug_trace();
    debug_dumpdisp(ownerPtr);
    JDisplayable *disp = static_cast<JDisplayable *>(ownerPtr->frame.widgetPtr);
    debug_qobject(disp->toForm());
    JTextField *tf = new JTextField(itemPtr, disp->toForm(),
                                    pcsl_string2QString(*label), layout, pcsl_string2QString(*text),
                                    maxSize, constraints, pcsl_string2QString(*initialInputMode));
    if (!tf)
      return KNI_ENOMEM;
    return KNI_OK;
  }

  MidpError lfpport_textfield_set_string(MidpItem *itemPtr, const pcsl_string *text)
  {
    JTextField *tf = (JTextField *)itemPtr->widgetPtr;
    return tf->setString(pcsl_string2QString(*text));
  }

  MidpError lfpport_textfield_get_string(pcsl_string *text, jboolean *newChange, MidpItem *itemPtr)
  {
    JTextField *tf = (JTextField *)itemPtr->widgetPtr;
    return QString2pcsl_string(tf->getString(newChange), *text);
  }

  MidpError lfpport_textfield_set_max_size(MidpItem *itemPtr, int maxSize)
  {
    JTextField *tf = (JTextField *)itemPtr->widgetPtr;
    return tf->setMaxSize(maxSize);
  }

  MidpError lfpport_textfield_get_caret_position(int *position, MidpItem *itemPtr)
  {
    JTextField *tf = (JTextField *)itemPtr->widgetPtr;
    *position = tf->getCaretPosition();
    return KNI_OK;
  }

  MidpError lfpport_textfield_set_constraints(MidpItem *itemPtr, int constraints)
  {
    JTextField *tf = (JTextField *)itemPtr->widgetPtr;
    return tf->setConstraints(constraints);
  }
}

JTextField::JTextField(MidpItem *item, JForm *form,
                       QString labelText, int layout, QString text,
                       int maxSize, int constraints, QString initialInputMode)
  : JItem(item, form)
{
  (void) layout;
  (void) maxSize;
  (void) constraints;
  (void) initialInputMode;

  QFormLayout *formLayout = new QFormLayout(this);
  formLayout->setRowWrapPolicy(QFormLayout::WrapAllRows);
  tf_label = new QLabel(labelText, this);
  //tf_body = new ExpandableTextEdit(text, this);
  tf_body = new QTextEdit(text, this);
  tf_body->installEventFilter(this);
  setFocusProxy(tf_body);
  tf_label->setBuddy(tf_body);
  tf_label->setTextFormat(Qt::PlainText);
  tf_label->setWordWrap(true);
  formLayout->addRow(tf_label, tf_body);
  if (labelText.isEmpty())
    tf_label->hide();

  cont_changed = false;
  connect(tf_body, SIGNAL(textChanged()), SLOT(contentsModified()));
}

JTextField::~JTextField()
{
}

void JTextField::j_setLabel(const QString &text)
{
  tf_label->setText(text);
  if (text.isEmpty() && tf_label->isVisible())
    tf_label->hide();
  if (!text.isEmpty() && tf_label->isHidden())
    tf_label->show();
}

MidpError JTextField::setString(const QString &text)
{
  tf_body->setText(text);
  return KNI_OK;
}

QString JTextField::getString(jboolean *changed)
{
  *changed = cont_changed;
  cont_changed = false;
  return tf_body->toPlainText();
}

MidpError JTextField::setMaxSize(int size)
{
  return KNI_OK;
}

int JTextField::getCaretPosition()
{
  return tf_body->textCursor().position();
}

MidpError JTextField::setConstraints(int constr)
{
  return KNI_OK;
}

void JTextField::contentsModified()
{
  cont_changed = true;
  notifyStateChanged();
  
  /*
  lfpport_log("JTextField: sizeHint (%dx%d)\n", sizeHint().width(), sizeHint().height());
  if (sizeHint().height() != height())
  {
    lfpport_log("JTextField: asking for resize\n");
    notifyResize();
  }
  */
}

bool JTextField::eventFilter(QObject *watched, QEvent *event)
{
  if (event->type()==QEvent::FocusIn)
  {
    lfpport_log("JTextField: caught child *FocusIn*\n");
    QFocusEvent *f_event = static_cast<QFocusEvent *>(event);
    if (f_event->reason()!=Qt::OtherFocusReason)
    {
      lfpport_log("JTextField: Non-synthetic event, notifying VM\n");
      notifyFocusIn();
    }
  }
  return false;
}

#include "lfpport_qtopia_textfield.moc"

