/*
*
*
* Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License version
* 2 only, as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* General Public License version 2 for more details (a copy is
* included at /legal/license.txt).
*
* You should have received a copy of the GNU General Public License
* version 2 along with this work; if not, write to the Free Software
* Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA
*
* Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
* Clara, CA 95054 or visit www.sun.com if you need additional
* information or have any questions.
*
* This source file is specific for Qt-based configurations.
*/

#include <QFont>
#include <QFontMetrics>
#include <QPainter>
#include <QScreen>

#include <midp_logging.h>

#include <jgraphics.h>
#include <jdisplay.h>
#include <jfont.h>
#include <gxapi_constants.h>
#include <gxpport_graphics.h>
#include <gxpport_font.h>
#include <gxpportqt_image.h>

/**
* Make a QString from an array of unicode chars.
*/
/*
static QString
make_string(const jchar *charArray, int len)
{
  QChar chars[len];
  int i;
  
  for (i = 0; i < len; i++)
  {
    chars[i] = QChar(charArray[i]);
    }
    return QString(chars, len);
    }
    */

/**
* Draw the first n characters in charArray, with the anchor point of the
* entire (sub)string located at x, y.
*/
extern "C" void
gxpport_draw_chars(jint pixel, const jshort *clip,
                   gxpport_mutableimage_native_handle dst,
                   int dotted,
                   int face, int style, int size,
                   int x, int y, int anchor,
                   const jchar *charArray, int n)
{
 QPixmap* qpixmap = JGraphics::mutablePixmap(dst);
 QString s = QString::fromUtf16(charArray, n);
 
 REPORT_INFO4(LC_LOWUI, "gxpport_draw_chars(%d, %d, %x, [chars...], %d)",
 x, y, anchor, n);
 
 JFont *font = JFont::find(face, style, size);
 const QFontMetrics *metrics = font->fontMetrics();
 
 switch (anchor & (LEFT | RIGHT | HCENTER))
 {
   case LEFT:
     break;
     
   case RIGHT:
     x -= metrics->width(s);
     break;
     
   case HCENTER:
     x -= metrics->width(s)/2;
     break;
 }
 
 switch (anchor & (TOP | BOTTOM | BASELINE))
 {
   case BOTTOM:
     /* 1 pixel has to be added to account for baseline in Qt */
     y -= metrics->descent()+1;
     
   case BASELINE:
     break;
     
   default:
   case TOP:
     y += metrics->ascent();
     break;
 }
 
 QRect clipRect;
 if (clip)
   clipRect.setCoords(clip[0], clip[1], clip[2], clip[3]);
 QPainter *gc = JGraphics::setupGC(pixel, -1, clipRect, qpixmap, dotted);
 gc->setFont(*font);
 gc->drawText(x, y, s);
}

/*
* Get the ascent, descent and leading info for the font indicated
* by face, style, size.
*/
extern "C" void
gxpport_get_fontinfo(int face, int style, int size,
                     int *ascent, int *descent, int *leading)
{
JFont *font = JFont::find(face, style, size);
const QFontMetrics *metrics = font->fontMetrics();

*ascent  = metrics->ascent();
/* 1 pixel has to be added to account for baseline in Qt */
*descent = metrics->descent() + 1;
*leading = metrics->leading();

REPORT_INFO6(LC_LOWUI, "gxpport_get_fontinfo(%d, %d, %d) = %d, %d, %d\n",
face, style, size, *ascent, *descent, *leading);

REPORT_INFO6(LC_LOWUI, "gxpport_get_fontinfo: height %d\n",
metrics->height(), 0, 0, 0, 0, 0);
}

/*
* Get the advance width for the first n characters in charArray if
* they were to be drawn in the font indicated by face, style, size.
*/
extern "C" int
gxpport_get_charswidth(int face, int style, int size,
                       const jchar *charArray, int n)
{
 JFont *font = JFont::find(face, style, size);
 const QFontMetrics *metrics = font->fontMetrics();
 
 //return qfontInfo->width(make_string(charArray, n));
 return metrics->width(QString::fromUtf16(charArray, n));
}
