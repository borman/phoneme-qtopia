#include <QPixmap>
#include <QResizeEvent>
#include <QScreen>
#include <QDesktopWidget>

#include <midp_logging.h>

#include "japplication.h"
#include "jdisplay.h"
#include "jmutableimage.h"

#define jdisplay_log(format, ...) reportToLog(LOG_INFORMATION, 10345, format, ## __VA_ARGS__)

// XXX: workaround because QFontMetrics returns wrong attributes after QFont::setPointSize()
#define DPI_SHIFT 8
const int dpi_mul = (25.4*(1<<DPI_SHIFT))/72;

JDisplay *JDisplay::m_instance = NULL;

JDisplay::JDisplay()
  : QStackedWidget(NULL), m_fullscreen(false), m_reversed(false), m_width(-1), m_height(-1)
{
  setWindowTitle("phoneME");
  QSize screenSize = JApplication::desktop()->availableGeometry().size();
  m_width = screenSize.width();
  m_height = screenSize.height();
  
  QScreen *screen = QScreen::instance();
  //m_dpi = ((dpi_mul*screen->deviceHeight())/screen->physicalHeight()) >> DPI_SHIFT;
  m_dpi = JApplication::desktop()->logicalDpiY();
}

JDisplay::~JDisplay()
{
}

JDisplay *JDisplay::current()
{
  if (!m_instance)
    init();
  return m_instance;
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
  JMutableImage *backBuffer = JMutableImage::fromHandle(NULL);
  if ((backBuffer->isNull()) || (newWidth>backBuffer->width() || newHeight>backBuffer->height()))
  {
    newWidth = qMax(backBuffer->width(), newWidth);
    newHeight = qMax(backBuffer->height(), newHeight);
    *backBuffer = JMutableImage(newWidth, newHeight);
    //backBuffer->fill(0);
  }
}

void JDisplay::setFullScreenMode(bool mode)
{
  if (mode!=m_fullscreen) // Do we actually need to change state?
  {
    m_fullscreen = mode;
    if (mode)
    {
      jdisplay_log("JDisplay: fullscreen ON\n");
      QString title = windowTitle();
      if (mode)
        setWindowTitle( QLatin1String("_allow_on_top_"));
      setWindowState(windowState() ^ Qt::WindowFullScreen);
      setWindowTitle(title);
    }
    else
    {
      jdisplay_log("JDisplay: fullscreen OFF\n");
      setWindowState(windowState() ^ Qt::WindowFullScreen);
    }
  }
  if (!currentWidget() || !currentWidget()->inherits("JForm"))
  {
    setDisplayWidth(width());
    setDisplayHeight(height());
  }
}

bool JDisplay::fullScreenMode() const
{
  return m_fullscreen;
}

// TODO: implement this
void JDisplay::setReversedOrientation(bool reverse)
{
}

bool JDisplay::reversedOrientation() const
{
  return m_reversed;
}

int JDisplay::displayWidth() const
{
  return m_width;
}

int JDisplay::displayHeight() const
{
  return m_height;
}

void JDisplay::setDisplayWidth(int w)
{
  m_width = w;
}

void JDisplay::setDisplayHeight(int h)
{
  m_height = h;
}

int JDisplay::dpi() const
{
  return m_dpi;
}

#include "moc_jdisplay.cpp"
