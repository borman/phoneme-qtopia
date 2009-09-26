#include <QPainter>
#include <QPen>
#include <QBrush>

#include <gxapi_constants.h>
#include <gxpport_graphics.h>

#include "jmutableimage.h"
#include "jfont.h"

extern "C"
{
  /**
  * <b>Platform Porting API:</b>
  * Translate a RGB or Gray Scale to device-dependent pixel value. 
  *
  * <p>
  * Related Java platform declaration:
  * <pre>
  *     getPixel(IIZ)I
  * </pre>
  *
  * @param rgb    compact RGB representation
  * @param gray   gray scale
  * @param isGray use gray scale
  */
  extern jint gxpport_get_pixel(jint rgb, int gray, int isGray)
  {
    Q_UNUSED(gray);
    Q_UNUSED(isGray);
    return rgb;
  }

  /**
  * <b>Platform Porting API:</b>
  * Draws a straight line between the given coordinates using the
  * current color, stroke style, and clipping data.
  *
  * <p>
  * Related Java platform declaration:
  * <pre>
  *     drawLine(IIII)V
  * </pre>
  *
  * @param pixel Device-dependent pixel value
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param dotted Stroke style
  * @param x1 x coordinate of the start point
  * @param y1 y coordinate of the start point
  * @param x2 x coordinate of the end point
  * @param y2 y coordinate of the end point
  */
  extern void gxpport_draw_line(jint pixel, const jshort *clip, 
                                gxpport_mutableimage_native_handle dst,
                                int dotted, int x1, int y1, int x2, int y2)
  {
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    QPen pen(QColor::fromRgb(pixel));
    pen.setStyle(dotted?(Qt::DotLine):(Qt::SolidLine));
    p->setPen(pen);
    
    p->drawLine(x1, y1, x2, y2);
  }

  /**
  * <b>Platform Porting API:</b>
  * Draws the outline of the specified rectangle using the current
  * color and stroke style at (x,y) position.
  *
  * <p>
  * Related Java platform declaration:
  * <pre>
  *     drawRect(IIII)V
  * </pre>
  *
  * @note There is no need to check for negative (or zero) of
  * width and height since they are already checked before MIDP
  * runtime calls this function.
  *
  * @param pixel Device-dependent pixel value
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param dotted Stroke style
  * @param x x coordinate of the rectangle
  * @param y y coordinate of the rectangle
  * @param width  Width of the rectangle
  * @param height Height of the rectangle
  */
  extern void gxpport_draw_rect(jint pixel, const jshort *clip, 
                                gxpport_mutableimage_native_handle dst,
                                int dotted, int x, int y, int width, int height)
  {
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    QPen pen(QColor::fromRgb(pixel));
    pen.setStyle(dotted?(Qt::DotLine):(Qt::SolidLine));
    p->setPen(pen);
    p->setBrush(Qt::transparent);
    
    p->drawRect(x, y, width, height);
  }

  /**
  * <b>Platform Porting API:</b>
  * Fills the specified rectangle using the current color and
  * stroke style at (x,y) position.
  *
  * @note There is no need to check for negative (or zero) of width and
  * height since they are already checked before MIDP runtime calls
  * this function.
  *
  * <p>
  * <b>Reference:</b>
  * Related Java platform declaration:
  * <pre>
  *     fillRect(IIII)V
  * </pre>
  *
  * @param pixel Device-dependent pixel value
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param dotted The stroke style to be used
  * @param x The x coordinate of the rectangle to be drawn
  * @param y The y coordinate of the rectangle to be drawn
  * @param width The width of the rectangle to be drawn
  * @param height The height of the rectangle to be drawn
  */
  extern void gxpport_fill_rect(jint pixel, const jshort *clip, 
                              gxpport_mutableimage_native_handle dst,
                              int dotted, int x, int y, int width, int height)
  {
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    p->fillRect(x, y, width, height, QColor::fromRgb(pixel));
  }

  /**
  * <b>Platform Porting API:</b>
  * Draws the outline of the specified rectangle, with rounded
  * corners, using the current color and stroke style.
  *
  * @note There is no need to check for negative (or zero) of
  * width and height since they are already checked before MIDP
  * runtime calls this function.
  *
  * <p>
  * <b>Reference:</b>
  * Related Java platform declaration:
  * <pre>
  *     drawRoundRect(IIIIII)V
  * </pre>
  *
  * @param pixel Device-dependent pixel value
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param dotted The stroke style to be used
  * @param x The x coordinate of the rectangle to be drawn
  * @param y The y coordinate of the rectangle to be drawn
  * @param width The width of the rectangle to be drawn
  * @param height The height of the rectangle to be drawn
  * @param arcWidth The horizontal diameter of the arc at the four corners
  * @param arcHeight The vertical diameter of the arc at the four corners
  */
  extern void gxpport_draw_roundrect(jint pixel, const jshort *clip, 
                                    gxpport_mutableimage_native_handle dst,
                                    int dotted,
                                    int x, int y, int width, int height,
                                    int arcWidth, int arcHeight)
  {
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    QPen pen(QColor::fromRgb(pixel));
    pen.setStyle(dotted?(Qt::DotLine):(Qt::SolidLine));
    p->setPen(pen);
    p->setBrush(Qt::transparent);
    
#if QT_VERSION >= 0x040400
    p->drawRoundedRect(x, y, width, height, arcWidth, arcHeight);
#else
    p->drawRoundRect(x, y, width, height, arcWidth, arcHeight);
#endif
  }
    
  /**
  * <b>Platform Porting API:</b>
  * Fills the outline of the specified rectangle, with rounded corners,
  * using the current color and stroke style.
  * 
  * @note There is no need to check for negative (or zero) of
  * width and height since they are already checked before MIDP
  * runtime calls this function.
  *
  * <p>
  * <b>Reference:</b>
  * Related Java platform declaration:
  * <pre>
  *     fillRoundRect(IIIIII)V
  * </pre>
  *
  * @param pixel Device-dependent pixel value
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param dotted The stroke style to be used
  * @param x The x coordinate of the rectangle to be drawn
  * @param y The y coordinate of the rectangle to be drawn
  * @param width The width of the rectangle to be drawn
  * @param height The height of the rectangle to be drawn
  * @param arcWidth The horizontal diameter of the arc at the four corners
  * @param arcHeight The vertical diameter of the arc at the four corners
  */
  extern void gxpport_fill_roundrect(jint pixel, const jshort *clip, 
                                    gxpport_mutableimage_native_handle dst, 
                                    int dotted,
                                    int x, int y, int width, int height,
                                    int arcWidth, int arcHeight)
  {  
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    p->setPen(Qt::transparent);
    p->setBrush(QColor::fromRgb(pixel));
    
#if QT_VERSION >= 0x040400
    p->drawRoundedRect(x, y, width, height, arcWidth, arcHeight);
#else
    p->drawRoundRect(x, y, width, height, arcWidth, arcHeight);
#endif
  }
    
  /**
  * <b>Platform Porting API:</b>
  * Draws the outline of the specified circular or elliptical arc
  * segment using the current color and stroke style.
  *
  * The portion of the arc to be drawn starts at startAngle (with
  * 0 at the 3 o'clock position) and proceeds counterclockwise by
  * <arcAngle> degrees. Variable arcAngle may not be negative.
  *
  * @note There is no need to check for negative (or zero) of
  * width and height since they are already checked before MIDP.
  * If your platform supports drawing arc only counterclockwise
  * only, define <B>PLATFORM_SUPPORT_CCW_ARC_ONLY</B>
  * to be true.
  *
  * <p>
  * <b>Reference:</b>
  * Related Java platform declaration:
  * <pre>
  *     drawArc(IIIIII)V
  * </pre>
  * 
  * @param pixel Device-dependent pixel value
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param dotted The stroke style to be used
  * @param x The x coordinate of the upper-left corner of the arc
  *          to be drawn
  * @param y The y coordinate of the upper-left corner of the arc
  *          to be drawn
  * @param width The width of the arc to be drawn
  * @param height The height of the arc to be drawn
  * @param startAngle The beginning angle
  * @param arcAngle The angular extent of the arc, relative to
  *                 <tt>startAngle</tt>
  */
  extern void gxpport_draw_arc(jint pixel, const jshort *clip, 
                              gxpport_mutableimage_native_handle dst,
                              int dotted, int x, int y, int width, int height, 
                              int startAngle, int arcAngle)
  {
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    QPen pen(QColor::fromRgb(pixel));
    pen.setStyle(dotted?(Qt::DotLine):(Qt::SolidLine));
    p->setPen(pen);
    p->setBrush(Qt::transparent);
    
    p->drawArc(x, y, width, height, startAngle*16, arcAngle*16); // Qt uses 1/16'ths if degree as an angle unit
  }

  /**
  * <b>Platform Porting API:</b>
  * Fills the specified circular or elliptical arc segment using the
  * current color and stroke style.
  *
  * The portion of the arc to be drawn starts at startAngle (with
  * 0 at the 3 o'clock position) and proceeds counterclockwise by
  * <arcAngle> degrees. Variable arcAngle may not be negative.
  *
  * @note There is no need to check for negative (or zero) of
  * width and height since they are already checked before MIDP.
  * If your platform supports drawing arc only counterclockwise
  * only, you should defined <B>PLATFORM_SUPPORT_CCW_ARC_ONLY</B>
  * to be true.
  *
  * <p>
  * <b>Reference:</b>
  * Related Java platform declaration:
  * <pre>
  *     fillArc(IIIIII)V
  * </pre>
  * 
  * @param pixel Device-dependent pixel value
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param dotted The stroke style to be used
  * @param x The x coordinate of the upper-left corner of the arc
  *          to be drawn
  * @param y The y coordinate of the upper-left corner of the arc
  *          to be drawn
  * @param width The width of the arc to be drawn
  * @param height The height of the arc to be drawn
  * @param startAngle The beginning angle
  * @param arcAngle The angular extent of the arc, relative to
  *                 <tt>startAngle</tt>
  */
  extern void gxpport_fill_arc(jint pixel, const jshort *clip, 
                              gxpport_mutableimage_native_handle dst,
                              int dotted, int x, int y, int width, int height, 
                              int startAngle, int arcAngle)
  {
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    p->setPen(Qt::transparent);
    p->setBrush(QColor::fromRgb(pixel));
    
    p->drawChord(x, y, width, height, startAngle*16, arcAngle*16); // Qt uses 1/16'ths if degree as an angle unit
  }
    
  /**
  * <b>Platform Porting API:</b>
  * Fills the specified triangle using the current color and stroke
  * style with the specify vertices (x1,y1), (x2,y2), and (x3,y3).
  *
  * <p>
  * <b>Reference:</b>
  * Related Java platform declaration:
  * <pre>
  *     fillTriangle(IIIIII)V
  * </pre>
  *
  * @param pixel Device-dependent pixel value
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param dotted The stroke style to be used
  * @param x1 The x coordinate of the first vertex
  * @param y1 The y coordinate of the first vertex
  * @param x2 The x coordinate of the second vertex
  * @param y2 The y coordinate of the second vertex
  * @param x3 The x coordinate of the third vertex
  * @param y3 The y coordinate of the third vertex
  */
  extern void gxpport_fill_triangle(jint pixel, const jshort *clip,
                                    gxpport_mutableimage_native_handle dst, int dotted,
                                    int x1, int y1,
                                    int x2, int y2,
                                    int x3, int y3)
  {
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    p->setPen(Qt::transparent);
    p->setBrush(QColor::fromRgb(pixel));
    
    static QPoint tri[3];
    tri[0] = QPoint(x1, y1);
    tri[1] = QPoint(x2, y2);
    tri[2] = QPoint(x3, y3);
    
    p->drawConvexPolygon(tri, 3);
  }

  /**
  * <b>Platform Porting API:</b>
  * Draws the first n characters specified using the current font,
  * color, and anchor point.
  *
  * <p>
  * <b>Reference:</b>
  * Related Java platform declaration:
  * <pre>
  *     drawString(Ljava/lang/String;III)V
  * </pre>
  *
  * @param pixel Device-dependent pixel color value
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param dotted The stroke style to be used
  * @param face The font face to be used (Defined in <B>Font.java</B>)
  * @param style The font style to be used (Defined in
  * <B>Font.java</B>)
  * @param size The font size to be used. (Defined in <B>Font.java</B>)
  * @param x The x coordinate of the anchor point
  * @param y The y coordinate of the anchor point
  * @param anchor The anchor point for positioning the text
  * @param chararray Pointer to the characters to be drawn
  * @param n The number of characters to be drawn
  */
  extern void gxpport_draw_chars(jint pixel, const jshort *clip, 
                                gxpport_mutableimage_native_handle dst,
                                int dotted,
                                int face, int style, int size,
                                int x, int y, int anchor, 
                                const jchar *chararray, int n)
  {
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    QPen pen(QColor::fromRgb(pixel));
    pen.setStyle(dotted?(Qt::DotLine):(Qt::SolidLine));
    p->setPen(pen);
    
    QString str = QString::fromRawData(reinterpret_cast<const QChar *>(chararray), n);
    
    JFont *font = JFont::find(face, style, size);
    const QFontMetrics *metrics = font->fontMetrics();
    
    switch (anchor & (LEFT | RIGHT | HCENTER))
    {    
      case RIGHT:
        x -= metrics->width(str);
        break;        
      case HCENTER:
        x -= metrics->width(str)/2;
        break;
      default:
      case LEFT:
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
    
    p->setFont(*font);
    p->drawText(x, y, str);
  }
  
  /*
  * Get the ascent, descent and leading info for the font indicated
  * by face, style, size.
  */
  void gxpport_get_fontinfo(int face, int style, int size,
                      int *ascent, int *descent, int *leading)
  {
    JFont *font = JFont::find(face, style, size);
    const QFontMetrics *metrics = font->fontMetrics();

    *ascent  = metrics->ascent();
    /* 1 pixel has to be added to account for baseline in Qt */
    *descent = metrics->descent() + 1;
    *leading = metrics->leading();
  }

  /*
  * Get the advance width for the first n characters in charArray if
  * they were to be drawn in the font indicated by face, style, size.
  */
  int gxpport_get_charswidth(int face, int style, int size,
                        const jchar *charArray, int n)
  {
    JFont *font = JFont::find(face, style, size);
    const QFontMetrics *metrics = font->fontMetrics();
    
    //return qfontInfo->width(make_string(charArray, n));
    return metrics->width(QString::fromRawData(reinterpret_cast<const QChar *>(charArray), n));
  }

  /**
  * <b>Platform Porting API:</b>
  * Copies the specified region of the given image data to a new
  * destination, locating its anchor point at (x, y).
  *
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param x_src The x coordinate of the upper-left corner of the
  *        image source
  * @param y_src The y coordinate of the upper-left corner of the
  *        image source
  * @param width The width of the image in the source image
  * @param height The height of the image in the source image
  * @param x_dest The x coordinate of the upper-left corner of the
  *        image to be drawn
  * @param y_dest The y coordinate of the upper-left corner of the
  *        image to be drawn
  */
  extern void gxpport_copy_area(const jshort *clip, 
                                gxpport_mutableimage_native_handle dst,
                                int x_src, int y_src, int width, int height, 
                                int x_dest, int y_dest)
  {
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    // FIXME: is it correct at all?
    p->drawImage(x_dest, y_dest, *image, x_src, width, height);
  }

  /**
  * <b>Platform Porting API:</b>
  * Draws the specified pixels from the given data array. The
  * array consists of values in the form of 0xAARRGGBB.  Its
  * upper-left corner is located at (x,y).
  *
  * <p>
  * <b>Reference:</b>
  * Related Java platform declaration:
  * <pre>
  *     drawRGB([IIIIIIIZ)V
  * </pre>
  *
  * @param clip Clipping information
  * @param dst Platform dependent destination information
  * @param rgbData The array of ARGB pixels to draw
  * @param offset Zero-based index of first ARGB pixel to be drawn
  * @param scanlen Number of intervening pixels between pixels in
  *        the same column but in adjacent rows
  * @param x The x coordinate of the upper left corner of the
  *        region to draw
  * @param y The y coordinate of the upper left corner of the
  *        region to draw
  * @param width The width of the target region
  * @param height The height of the target region
  * @param processAlpha If <tt>true</tt>, alpha channel bytes
  *        should be used, otherwise, alpha channel bytes will
  *        be ignored
  */
  extern void gxpport_draw_rgb(const jshort *clip, 
                              gxpport_mutableimage_native_handle dst, 
                              jint *rgbData, 
                              jint offset, jint scanlen, jint x, jint y, 
                              jint width, jint height, jboolean processAlpha)
  {
    JMutableImage *image = JMutableImage::fromHandle(dst);
    QPainter *p = image->painter(clip);
    
    // Avoid making a deep copy of image
    const uchar *constRgbData =reinterpret_cast<uchar *>(rgbData+offset);
    QImage src(constRgbData, width, height, scanlen*4, processAlpha?QImage::Format_ARGB32:QImage::Format_RGB32);
    
    p->drawImage(x, y, src);
  }

  /**
  * Return the displayed RGB value of a given RGB pixel in both 0xRRGGBB format.
  * For example on system where blue is only 5 bits it would slightly rounded
  * down value, where as on an 8 bit system the color would be the same.
  *
  * @param color Java platform RGB color
  *
  * @return Device-dependent pixel color value but in Java platform color size
  */
  extern jint gxpport_get_displaycolor(jint color)
  {
    return color; // Assume our display is 32-bit
  }
}
