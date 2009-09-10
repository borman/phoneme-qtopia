#include "lfpport_qtopia_ticker.h"
#include <jdisplay.h>
#include <QEvent>
#include <QPainter>
#include <QTimerEvent>
#include <QDebug>


JTicker::JTicker(QWidget *parent)
	:QWidget(parent)
{
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
	qDebug() << "JTicker::setText(): " << text ;
	updateGeometry();
}

QString JTicker::text()
{
	return str;
}

void JTicker::paintEvent(QPaintEvent *)
{
	QPainter painter(this);
	int textWidth = fontMetrics().width(text());
	if(textWidth < 1)
	{
		return;
	}
	int x = -offset;
	while(x < width())
	{
		painter.drawText(x, 0, textWidth, height(), Qt::AlignVCenter, text());
		x += textWidth;
	}
}

void JTicker::showEvent(QShowEvent *)
{
    resize(JDisplay::current()->displayWidth(), height());
	timerid = startTimer(30);
	qDebug() << "JTIcker: show event";
}

void JTicker::timerEvent(QTimerEvent *event)
{
	if(event->timerId() == timerid)
	{
//		qDebug() << "JTicker::timerEvent()";
		++offset;
		if(offset >= fontMetrics().width(text()))
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
