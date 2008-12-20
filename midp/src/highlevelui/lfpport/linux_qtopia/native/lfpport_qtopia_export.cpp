#include <lfpport_export.h>
#include <japplication.h>
#include <jdisplay.h>
#include <midp_logging.h>
#include <midp_constants_data.h>


extern "C"
{
    /**
   * Refresh the given area.  For double buffering purposes.
   */
  void lfpport_refresh(int x, int y, int w, int h)
  {
    
  }

  /**
   * set the screen mode either to fullscreen or normal.
   *
   * @param mode The screen mode
   */
  void lfpport_set_fullscreen_mode(jboolean mode)
  {
    JDisplay::current()->setFullScreenMode(mode);
  }

  /**
   * Change screen orientation flag
   */
  jboolean lfpport_reverse_orientation()
  {
    JDisplay::current()->setReversedOrientation(!JDisplay::current()->reversedOrientation());
    return JDisplay::current()->reversedOrientation();
  }

  /**
   * Get screen orientation flag
   */
  jboolean lfpport_get_reverse_orientation()
  {
    return JDisplay::current()->reversedOrientation();
  }

  /**
   * Return screen width
   */
  int lfpport_get_screen_width()
  {
    return JDisplay::current()->displayWidth();
  }

  /**
   *  Return screen height
   */
  int lfpport_get_screen_height()
  {
    return JDisplay::current()->displayHeight();
  }

  /**
   * Resets native resources when foreground is gained by a new display.
   */
  void lfpport_gained_foreground()
  {
  }

  /**
   * Initializes the window system.
   */
  void lfpport_ui_init()
  {
    JApplication::init();
    JDisplay::init();
    JApplication *app = JApplication::instance();
    app->showMainWidget(JDisplay::current());
  }

  /**
   * Finalize the window system.
   */
  void lfpport_ui_finalize()
  {
    JDisplay::current()->hide();
    JDisplay::destroy();
    JApplication::destroy();
  }

  /**
   * Flushes the offscreen buffer directly to the device screen.
   * The size of the buffer flushed is defined by offscreen buffer width
   * and passed in height. 
   * Offscreen_buffer must be aligned to the top-left of the screen and
   * its width must be the same as the device screen width.
   * @param graphics The Graphics handle associated with the screen.
   * @param offscreen_buffer The ImageData handle associated with 
   *                         the offscreen buffer to be flushed
   * @param height The height to be flushed
   * @return KNI_TRUE if direct_flush was successful, KNI_FALSE - otherwise
   */
  jboolean lfpport_direct_flush(const java_graphics *g, 
                const java_imagedata *offscreen_buffer, int h)
  {
    return KNI_FALSE;
  }
}
