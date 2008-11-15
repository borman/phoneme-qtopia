#include "expandable_textedit.h"
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
  setFixedHeight(ceil(document()->size().height())+4);
  connect(this, SIGNAL(textChanged()), SLOT(checkHeight()));
}

void ExpandableTextEdit::checkHeight()
{
  int preferredHeight = ceil(document()->size().height())+4;
  if (height()!=preferredHeight)
    setFixedHeight(preferredHeight);
}