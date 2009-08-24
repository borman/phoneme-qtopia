#include <QPixmap>
#include <QResizeEvent>
#include <QScreen>
#include <QDesktopWidget>

#include <midp_logging.h>

#include "japplication.h"
#include "jdisplay.h"

JDisplay *JDisplay::m_instance = NULL;

JDisplay::JDisplay()
  : QStackedWidget(NULL), m_fullscreen(false), m_reversed(false), m_backbuffer(new QPixmap), m_width(-1), m_height(-1)
{
  setWindowTitle("phoneME");
  QSize screenSize = JApplication::desktop()->availableGeometry().size();
  m_width = screenSize.width();
  m_height = screenSize.height();
  
  QScreen *screen = QScreen::instance();
  m_dpi = JApplication::desktop()->logicalDpiY();
}

JDisplay::~JDisplay()
{
}

void JDisplay::init()
{
  if (!m_instance)
    m_instance = new JDisplay;
}

void JDisplay::destroy()
{
  if (m_instance)
  {
    delete m_instance;
    m_instance = NULL;
  }
}

void JDisplay::resizeEvent(QResizeEvent *e)
{
  resizeBackBuffer(e->size().width(), e->size().height());
  if (m_width<0)
    m_width = width();
  if (m_height<0)
    m_height = height();
}

// resize backbuffer only if required size is bigger than qpixmap size to minimize amount of pixmap reallocations
void JDisplay::resizeBackBuffer(int newWidth, int newHeight)
{
  if ((m_backbuffer->isNull()) || (newWidth>m_backbuffer->width() || newHeight>m_backbuffer->height()))
  {
    QPixmap *old_buffer = m_backbuffer;
    m_backbuffer = new QPixmap(qMax(old_buffer->width(), newWidth), qMax(old_buffer->height(), newHeight));
    m_backbuffer->fill();
    delete old_buffer;
  }
}

void JDisplay::setFullScreenMode(bool mode)
{
  if (mode!=m_fullscreen) // Do we actually need to change state?
  {
    m_fullscreen = mode;
    if (mode)
    {
      qDebug("JDisplay: fullscreen ON\n");
      QString title = windowTitle();
      if (mode)
        setWindowTitle( QLatin1String("_allow_on_top_"));
      setWindowState(windowState() ^ Qt::WindowFullScreen);
      setWindowTitle(title);
    }
    else
    {
      qDebug("JDisplay: fullscreen OFF\n");
      setWindowState(windowState() ^ Qt::WindowFullScreen);
    }
  }
  if (!currentWidget() || !currentWidget()->inherits("JForm"))
  {
    setDisplayWidth(width());
    setDisplayHeight(height());
  }
}

#include "moc_jdisplay.cpp"
