#ifndef _LFPPORT_QTOPIA_TICKER_H_
#define _LFPPORT_QTOPIA_TICKER_H_

#include <QWidget>

class JTicker: public QWidget
{
	Q_OBJECT
	public:
		JTicker(QString text, QWidget *parent);
		~JTicker();
		void	setText(QString text);
		QString text();
	protected:
		void	paintEvent(QPaintEvent *event);
		void	timerEvent(QTimerEvent *event);
		void	showEvent(QShowEvent *event);
		void	hideEvent(QHideEvent *event);
	private:
		QString str;
		int		offset;
		int		timerid;
};

#endif //_LFPPORT_QTOPIA_TICKER_H_

