#include <lfpport_export.h>
#include <japplication.h>
#include <jdisplay.h>
#include <midp_logging.h>
#include <midp_constants_data.h>

#include "lfpport_qtopia_debug.h"
#include "lfpport_qtopia_command.h"
#include "lfpport_qtopia_form.h"

extern "C"
{
  /**
   * Refresh the given area.  For double buffering purposes.
   */
  void lfpport_refresh(int hardwareID, int x, int y, int w, int h)
  {
    //qDebug("lfpport_refresh(%d, %d, %d, %d, %d)", hardwareID, x, y, w, h);
    (void)hardwareID;
    QWidget *activeWidget = JDisplay::current()->currentWidget();
    if (activeWidget != NULL) // We may have only a JAlert active and it's not a stacked widget
      activeWidget->repaint(x, y, w, h);
  }

  /**
   * set the screen mode either to fullscreen or normal.
   *
   * @param mode The screen mode
   */
  void lfpport_set_fullscreen_mode(int hardwareID, jboolean mode)
  {
    (void)hardwareID;
    JDisplay::current()->setFullScreenMode(mode);
  }

  /**
   * Change screen orientation flag
   */
  jboolean lfpport_reverse_orientation(int hardwareID)
  {
    (void)hardwareID;
    JDisplay::current()->setReversedOrientation(!JDisplay::current()->reversedOrientation());
    return JDisplay::current()->reversedOrientation();
  }

  /**
   * Get screen orientation flag
   */
  jboolean lfpport_get_reverse_orientation(int hardwareID)
  {
    (void)hardwareID;
    return JDisplay::current()->reversedOrientation();
  }

  /**
   * Return screen width
   */
  int lfpport_get_screen_width(int hardwareID)
  {
    (void)hardwareID;
    qDebug("Screen width %d", JDisplay::current()->displayWidth());
    return JDisplay::current()->displayWidth();
  }

  /**
   *  Return screen height
   */
  int lfpport_get_screen_height(int hardwareID)
  {
    (void)hardwareID;
    qDebug("Screen height %d", JDisplay::current()->displayHeight());
    return JDisplay::current()->displayHeight();
  }

  /**
   * Resets native resources when foreground is gained by a new display.
   */
  void lfpport_gained_foreground(int hardwareID)
  {
      (void)hardwareID;
  }

  /**
   * Initializes the window system.
   */
  void lfpport_ui_init()
  {
    JApplication::init();
    JDisplay::init();
    JCommandManager::init();
    JApplication *app = JApplication::instance();
    app->setMainWidget(JDisplay::current());
    app->showMainWidget(JDisplay::current());
    app->registerRunningTask("PhoneME", app);
//    app->setParent(0);
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
  jboolean lfpport_direct_flush(int hardwareID, const java_graphics *g,
                const java_imagedata *offscreen_buffer, int h)
  {
    (void)hardwareID;
    return KNI_FALSE;
  }

  char * lfpport_get_display_name(int hardwareId) {
    (void)hardwareId;
    return 0;
}


/**
 * Check if the display device is primary
 */
jboolean lfpport_is_display_primary(int hardwareId) {
    (void)hardwareId;
    return KNI_TRUE;
}
/**
 * Check if the display device is build-in
 */
jboolean lfpport_is_display_buildin(int hardwareId) {
    (void)hardwareId;
    return KNI_TRUE;
}
/**
 * Check if the display device supports pointer events
 */
jboolean lfpport_is_display_pen_supported(int hardwareId) {
    (void)hardwareId;
    return KNI_TRUE;
}
/**
 * Check if the display device supports pointer motion  events
 */
jboolean lfpport_is_display_pen_motion_supported(int hardwareId) {
    (void)hardwareId;
    return KNI_TRUE;
}
/**
 * Get display device capabilities
 */
int lfpport_get_display_capabilities(int hardwareId) {
    (void)hardwareId;
    return 255;
  }


  static jint display_device_ids[] = {0};

  /**
   * Get the list of display device ids
  */

  jint* lfpport_get_display_device_ids(jint* n)
  {
    *n = 1;
    return display_device_ids;
  }

  /**
  * Notify the display device state has been changed
  */
  void lfpport_display_device_state_changed(int hardwareId, int state)
  {
    (void)hardwareId;
    (void)state;
  }

  void lfpport_handle_clamshell_event()
  {
  }

}
