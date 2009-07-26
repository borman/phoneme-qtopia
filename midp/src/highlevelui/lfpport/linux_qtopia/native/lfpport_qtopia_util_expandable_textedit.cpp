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
  setSizePolicy(QSizePolicy(QSizePolicy::Minimum, QSizePolicy::Minimum));
  setHorizontalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
  setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
  checkHeight();
  connect(this, SIGNAL(textChanged()), SLOT(checkHeight()));
}

void ExpandableTextEdit::checkHeight()
{
  updateGeometry();
}

QSize ExpandableTextEdit::sizeHint() const
{
  //int preferredHeight = qMax(/*sizeHint().height()*/ QFontMetrics(font()).height()+15, int(ceil(document()->size().height())+4));
  int preferredHeight = ceil(document()->size().height())+4;
  
  QSize sh = QTextEdit::sizeHint();
  sh.setHeight(preferredHeight);
  qDebug("ExpandableTextEdit -> sizeHint: (%dx%d); minimumSize: (%dx%d)", 
               sh.width(), sh.height(), minimumSize().width(), minimumSize().height());
  return sh;
}

QSize ExpandableTextEdit::minimumSizeHint() const
{
  QSize sh = sizeHint();
  sh.setHeight(QFontMetrics(font()).height());
  return sh;
}

#include "lfpport_qtopia_util_expandable_textedit.moc"
