#ifndef _JDISPLAY_H_
#define _JDISPLAY_H_

#include <QStackedWidget>

class QPixmap;

/*!
  \brief J2ME diplay manager sigleton

  Being the main widget of the JVM application, JDisplay manages window states and stores
  application-global backbuffer for either JCanvas lfpport displayable or lfjport screen.
  Implementation-specific screens should be added to this widget as to QStackedWidget.
*/

class JDisplay: public QStackedWidget
{
  Q_OBJECT
  protected:
    JDisplay();
    virtual ~JDisplay();
  public:
    static JDisplay *current();
    static void init();
    static void destroy();

    QPixmap *backBuffer() const; // Return backbuffer for painting
    // Painter MUST NOT rely on painter size as screen size though it MUST always be sufficient to paint the whole screen

    void setFullScreenMode(bool mode); // set display maximized/regular state
    bool fullScreenMode() const;

    void setReversedOrientation(bool reverse); // Set display rotation
    bool reversedOrientation() const;

    // Display sizes cannot be calculated here, thus they are calculated by a Canvas native peer and stored here for convenience
    int displayWidth() const;
    int displayHeight() const;
    void setDisplayWidth(int w);
    void setDisplayHeight(int h);
    
    int dpi() const;

  protected:
    void resizeEvent(QResizeEvent *e);
  private:
    void resizeBackBuffer(int newWidth, int newHeight); // Handle backbuffer resizing
    static JDisplay *m_instance;

    bool m_fullscreen;
    bool m_reversed;

    int m_width;
    int m_height;
    
    int m_dpi;

    QPixmap *m_backbuffer;
};

#endif // _JDISPLAY_H_
