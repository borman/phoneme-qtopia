#include <QDesktopWidget>

// NOTE: include this for font attribute constants, also found in <lfpport_font.h>
#include <gxapi_constants.h> 
#include <japplication.h>

#include "jfont.h"

#define QFONT_SIZE_SMALL           5
#define QFONT_SIZE_MEDIUM          7
#define QFONT_SIZE_LARGE           10

#define QFONT_FAMILY_SYSTEM        "Regular"
#define QFONT_FAMILY_PROPORTIONAL  "Regular"
#define QFONT_FAMILY_MONOSPACE     "Monospace"

JFont *JFont::cache[JFONT_CACHE_SIZE];

JFont::JFont(int face, int style, int size)
  : QFont(), metrics(*this)
{
  int qfont_size;
  QString qfont_family;
  switch (size)
  {
    case SIZE_SMALL:
      qfont_size = QFONT_SIZE_SMALL;
      break;
      
    default:
    case SIZE_MEDIUM:
      qfont_size = QFONT_SIZE_MEDIUM;
      break;
      
    case SIZE_LARGE:
      qfont_size = QFONT_SIZE_LARGE;
      break;
  }
  setPointSize(qfont_size);
  
  switch (face)
  {
    case FACE_SYSTEM:
      qfont_family = QFONT_FAMILY_SYSTEM;
      break;
    
    default:
    case FACE_PROPORTIONAL:
      qfont_family = QFONT_FAMILY_PROPORTIONAL;
      break;
      
    case FACE_MONOSPACE:
      qfont_family = QFONT_FAMILY_MONOSPACE;
      break;
  }
  setFamily(qfont_family);

  if (style & STYLE_BOLD)
    setBold(true);
  if (style & STYLE_ITALIC)
    setItalic(true);
  if (style & STYLE_UNDERLINED)
    setUnderline(true);
  
  metrics = QFontMetrics(*this, JApplication::desktop()->screen());
  
  id = attrs2id(face, style, size);
  cache[id] = this;
}

JFont::~JFont()
{
  cache[id] = NULL;
}

const QFontMetrics *JFont::fontMetrics() const
{
  return &metrics;
}

JFont *JFont::find(int face, int style, int size)
{
  int id = attrs2id(face, style, size);
  if (cache[id])
    return cache[id];
  else 
    return new JFont(face, style, size);
}

int JFont::attrs2id(int face, int style, int size)
{
  int face_n, style_n, size_n;
  switch (size)
  {
    case SIZE_SMALL:
      size_n = 0;
      break;
      
    default:
    case SIZE_MEDIUM:
      size_n = 1;
      break;
      
    case SIZE_LARGE:
      size_n = 2;
      break;
  }
  switch (face)
  {
    case FACE_SYSTEM:
      face_n = 0;
      break;
    
    default:
    case FACE_PROPORTIONAL:
      face_n = 1;
      break;
      
    case FACE_MONOSPACE:
      face_n = 2;
      break;
  }
  style_n = 0;
  if (style & STYLE_BOLD)
    style_n |= 1;
  if (style & STYLE_ITALIC)
    style_n |= 2;
  if (style & STYLE_UNDERLINED)
    style_n |= 4;
  
  return 9*style_n+3*size_n+face_n;
}

void JFont::clearCache()
{
  for (int i=0; i<JFONT_CACHE_SIZE; i++)
    if (cache[i])
    {
      delete cache[i];
      cache[i] = 0;
    }
}

