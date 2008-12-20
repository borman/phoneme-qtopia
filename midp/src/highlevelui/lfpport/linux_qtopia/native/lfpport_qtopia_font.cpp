#include <lfpport_font.h>
#include <lfpport_error.h>
#include <midpMalloc.h>

#include <QCache>
#include <QFont>

#define QFONT_SIZE_SMALL           5
#define QFONT_SIZE_MEDIUM          7
#define QFONT_SIZE_LARGE           10

#define QFONT_FAMILY_SYSTEM        "Regular"
#define QFONT_FAMILY_PROPORTIONAL  "Regular"
#define QFONT_FAMILY_MONOSPACE     "Monospace"

QCache<int, QFont> fonts_cache;

extern "C"
{
  // Looking up the corresponding font in the fonts cache and creating a new font if required
  MidpError lfpport_get_font(PlatformFontPtr* fontPtr, int face, int style, int size)
  {
    *fontPtr = NULL;
    if (fonts_cache.contains(face|style|size))
    {
      *fontPtr = fonts_cache[face|style|size];
      return KNI_OK;
    }
    int qfont_size;
    QString qfont_family;
    switch (size)
    {
      case SIZE_SMALL:
        qfont_size = QFONT_SIZE_SMALL;
        break;
      case SIZE_MEDIUM:
        qfont_size = QFONT_SIZE_MEDIUM;
        break;
      case SIZE_LARGE:
        qfont_size = QFONT_SIZE_LARGE;
        break;
      default:
        return KNI_EINVAL;
    }
    switch (face)
    {
      case FACE_SYSTEM:
        qfont_family = QFONT_FAMILY_SYSTEM;
        break;
      case FACE_PROPORTIONAL:
        qfont_family = QFONT_FAMILY_PROPORTIONAL;
        break;
      case FACE_MONOSPACE:
        qfont_family = QFONT_FAMILY_MONOSPACE;
        break;
      default:
        return KNI_EINVAL;
    }

    QFont *qfont = new QFont(qfont_family, qfont_size);
    if (!qfont)
      return KNI_ENOMEM;

    if (style|STYLE_BOLD)
      qfont->setBold(true);
    if (style|STYLE_ITALIC)
      qfont->setItalic(true);
    if (style|STYLE_UNDERLINED)
      qfont->setUnderline(true);

    fonts_cache.insert(face|style|size, qfont);
    *fontPtr = qfont;
    return KNI_OK;
  }

  // Cleaning up fonts cache
  void lfpport_font_finalize()
  {
    fonts_cache.clear();
  }
}
