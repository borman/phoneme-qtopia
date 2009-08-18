#include "lfpport_qtopia_ticker.h"
#include "lfpport_qtopia_displayable.h"
#include <jdisplay.h>
#include <QEvent>
#include <QPainter>
#include <QTimerEvent>

JTicker::JTicker(QString text, QWidget *parent)
	:QWidget(parent)
{
	str = text;
	offset = 0;
	timerid = 0;
}

JTicker::~JTicker()
{
}

void JTicker::setText(QString text)
{
	str = text;
	update();
	updateGeometry();
}

QString JTicker::text()
{
	return str;
}

void JTicker::paintEvent(QPaintEvent *event)
{
	QPainter painter(this);
	int textWidth = fontMetrics().width(str);
	if(textWidth < 1)
	{
		return;
	}
	int x = -offset;
	while(x < width())
	{
		painter.drawText(x, 0, textWidth, height(), Qt::AlignVCenter, str);
		x += textWidth;
	}
}

void JTicker::showEvent(QShowEvent *)
{
	resize(JDisplay::current()->displayWidth(), height());
	timerid = startTimer(30);
}

void JTicker::timerEvent(QTimerEvent *event)
{
	if(event->timerId() == timerid)
	{
		++offset;
		if(offset >= fontMetrics().width(str))
		{
			offset = 0;
		}
		scroll(-1, 0);
	}
	else
	{
		QWidget::timerEvent(event);
	}
}

void JTicker::hideEvent(QHideEvent *)
{
	killTimer(timerid);
}


#include "lfpport_qtopia_ticker.moc"
