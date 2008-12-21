#include "lfpport_qtopia_util_expandable_textedit.h"
#include <cmath>

ExpandableTextEdit::ExpandableTextEdit(QWidget *parent)
  : QTextEdit(parent)
{
  init();
}

ExpandableTextEdit::ExpandableTextEdit(const QString &text, QWidget *parent)
  : QTextEdit(text, parent)
{
  init();
}

ExpandableTextEdit::~ExpandableTextEdit()
{
}

void ExpandableTextEdit::init()
{
  checkHeight();
  connect(this, SIGNAL(textChanged()), SLOT(checkHeight()));
}

void ExpandableTextEdit::checkHeight()
{
  int preferredHeight = qMax(sizeHint().height(), int(ceil(document()->size().height())+4));
  if (height()!=preferredHeight)
    setFixedHeight(preferredHeight);
}

#include "lfpport_qtopia_util_expandable_textedit.moc"
