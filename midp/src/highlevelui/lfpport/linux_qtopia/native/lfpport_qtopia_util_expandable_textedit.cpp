#include "lfpport_qtopia_util_expandable_textedit.h"
#include <cmath>
#include <QFontMetrics>
#include "lfpport_qtopia_debug.h"

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
  lfpport_log("ExpandableTextEdit: calc height\n");
  int preferredHeight = qMax(/*sizeHint().height()*/ QFontMetrics(font()).height()+15, int(ceil(document()->size().height())+4));
  //int preferredHeight = ceil(document()->size().height())+8;
  if (height()!=preferredHeight)
    setFixedHeight(preferredHeight);
}

#include "lfpport_qtopia_util_expandable_textedit.moc"
