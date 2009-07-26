#include <QCache>
#include <QFont>

#include <lfpport_font.h>
#include <lfpport_error.h>
#include <midpMalloc.h>
#include <jdisplay.h>
#include <jfont.h>

#include "lfpport_qtopia_debug.h"

extern "C"
{
  // Looking up the corresponding font in the fonts cache and creating a new font if required
  MidpError lfpport_get_font(PlatformFontPtr* fontPtr, int face, int style, int size)
  {
    qDebug("lfpport_get_font(%d, %d, %d)", face, style, size);
    *fontPtr = JFont::find(face, style, size);
    if (fontPtr)
      return KNI_OK;
    else
      return KNI_ENOMEM;
  }

  // Cleaning up fonts cache
  void lfpport_font_finalize()
  {
    JFont::clearCache();
  }
}
