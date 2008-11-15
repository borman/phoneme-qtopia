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

#include <QPixmap>
#include <QImage>
#include <cstring>
#include <midp_logging.h>
#include <midp_constants_data.h>

#include <gxapi_constants.h>
#include "gxpportqt_intern_graphics_util.h"

void
get_transformed_pixmap(QPixmap* originalPixmap,
                       QPixmap* destPixmap,
                       int src_x, int src_y,
                       int src_width, int src_height,
                       int transform,
                       bool hasAlpha)
{
    QImage originalImage = originalPixmap->toImage();

    //if ( hasAlpha )
    //{
        // Qt's handling of the alpha channel in the conversion
        // process between QPixmap and QImage is buggy.
        // If the pixmap's pixels only have alpha values 0x00 and 0xFF
        // then the resulting QImage from conversion will return
        // false for hasAlphaBuffer().
        // so we set our own flag instead of depending on Qt to
        // maintain alpha information.

        //originalImage.setAlphaBuffer(TRUE);
    //}

    /*Qt gives us this useful API that returns a section of a QImage*/
    QImage sectionImage  = originalImage.copy(src_x, src_y,
                           src_width, src_height);
    /* Skip this pixel-by-pixel copy if there is no transform */
    if (0 != transform)
    {
        QImage sectionImage32bpp = sectionImage;
        QImage processedImage;


        int nXOriginSrc = 0;
        int nYOriginSrc = 0;
        int nWidth      = src_width;
        int nHeight     = src_height;

        /*scan length of the source image*/
        int imageWidth  = src_width;
        /*number of rows of the source image*/
        int imageHeight = src_height;

        int imgLen;
        int srcImgLen;

        int t_width;
        int t_height;

        int srcX;
        int srcY;
        int xStart;
        int yStart;
        int xIncr;
        int yIncr;
        int destX;
        int destY;
        int yCounter;
        int xCounter;

        int srcIndex;
        int destIndex;

        uchar* srcBits     = NULL;
        uchar* destBits    = NULL;

        uchar* srcBitsPtr  = NULL;
        uchar* destBitsPtr = NULL;


        /* set dimensions of image being created,
           depending on transform */
        if (transform & TRANSFORM_INVERTED_AXES)
        {
            t_width  = src_height;
            t_height = src_width;
        }
        else
        {
            t_width  = src_width;
            t_height = src_height;
        }

        /* width * height * 4 gives us the size of a 32 bpp image */
        imgLen = nWidth * nHeight << 2;
        srcImgLen = imageWidth  * imageHeight << 2;

        /* Qt specific */
        processedImage = QImage(t_width, t_height, QImage::Format_ARGB32);

        srcBits  = sectionImage32bpp.bits();
        destBits = processedImage.bits();
        /* ----------- */

        if (transform & TRANSFORM_Y_FLIP)
        {
            yStart = nHeight-1;
            yIncr = -1;
        }
        else
        {
            yStart = 0;
            yIncr = +1;
        }

        if (transform & TRANSFORM_X_FLIP)
        {
            xStart = nWidth-1;
            xIncr = -1;
        }
        else
        {
            xStart = 0;
            xIncr = +1;
        }

        srcBitsPtr  = srcBits;
        destBitsPtr = destBits;


        /* increment srcX,Y regular. increment destX,Y according to transform.
           this makes handling of mask and alpha values easier */

        for (srcY = nYOriginSrc, destY = yStart, yCounter = 0;
                yCounter < nHeight;
                srcY++, destY+=yIncr, yCounter++)
        {

            /* in the current implementation we have source bitmap
               dimension as the width of the image and the height of the region
               destination bitmap is of the dimensions of the region */

            for (srcX = nXOriginSrc, destX = xStart, xCounter = 0;
                    xCounter < nWidth;
                    srcX++, destX+=xIncr, xCounter++)
            {

                if ( transform & TRANSFORM_INVERTED_AXES )
                {
                    destIndex =  ( ( (destX) * t_width) + (destY) );
                }
                else
                {
                    destIndex =  ( ( (destY) * t_width) + (destX) );
                }

                destBitsPtr =  destBits + (destIndex * 4) ;

                srcIndex = (((srcY) * imageWidth) + (srcX));
                srcBitsPtr = srcBits + (srcIndex * 4);


                /* copy the pixel that is pointed to */
                *((int *)destBitsPtr) = *((int *)srcBitsPtr);

            } /*for x*/

        } /* for y */

        destPixmap->fromImage(processedImage);
    }
    else
    {
        /* No transform, just copy the image sub-section */
        destPixmap->fromImage(sectionImage);
    }
}

/*
 * Helper function to transform QImages
 *
 * @param srcQImage the QImage to be transformed
 * @param transform the transform to perform on the image
 */
void transform_image(QImage* srcQImage, int transform)
{
#if 0
    /* make sure the source image depth is 32 */
    //if (32 != srcQImage->depth()) {
    //    *srcQImage = srcQImage->convertDepth(32);
    //}

    int nXOriginSrc = 0;
    int nYOriginSrc = 0;
    int nWidth      = srcQImage->width();
    int nHeight     = srcQImage->height();

    /*scan length of the source image*/
    int imageWidth  = nWidth;
    /*number of rows of the source image*/
    int imageHeight = nHeight;

    int imgLen;
    int srcImgLen;

    int t_width;
    int t_height;

    int srcX;
    int srcY;
    int xStart;
    int yStart;
    int xIncr;
    int yIncr;
    int destX;
    int destY;
    int yCounter;
    int xCounter;

    int srcIndex;
    int destIndex;

    uchar* srcBits     = NULL;
    uchar* destBits    = NULL;

    uchar* srcBitsPtr  = NULL;
    uchar* destBitsPtr = NULL;


    /* set dimensions of image being created,
       depending on transform */
    if (transform & TRANSFORM_INVERTED_AXES)
    {
        t_width  = nHeight;
        t_height = nWidth;
    }
    else
    {
        t_width  = nWidth;
        t_height = nHeight;
    }

    /* width * height * 4 gives us the size of a 32 bpp image */
    imgLen = nWidth * nHeight << 2;
    srcImgLen = imageWidth  * imageHeight << 2;

    /* Qt specific */
    QImage processedQImage = QImage(t_width, t_height, QImage::Format_ARGB32);

    srcBits  = srcQImage->bits();
    destBits = processedQImage.bits();
    /* ----------- */

    if (transform & TRANSFORM_Y_FLIP)
    {
        yStart = nHeight-1;
        yIncr = -1;
    }
    else
    {
        yStart = 0;
        yIncr = +1;
    }

    if (transform & TRANSFORM_X_FLIP)
    {
        xStart = nWidth-1;
        xIncr = -1;
    }
    else
    {
        xStart = 0;
        xIncr = +1;
    }

    srcBitsPtr  = srcBits;
    destBitsPtr = destBits;


    /* increment srcX,Y regular. increment destX,Y according to transform.
       this makes handling of mask and alpha values easier */

    for (srcY = nYOriginSrc, destY = yStart, yCounter = 0;
            yCounter < nHeight;
            srcY++, destY+=yIncr, yCounter++)
    {

        /* in the current implementation we have source bitmap
           dimension as the width of the image and the height of the region
           destination bitmap is of the dimensions of the region */

        for (srcX = nXOriginSrc, destX = xStart, xCounter = 0;
                xCounter < nWidth;
                srcX++, destX+=xIncr, xCounter++)
        {

            if ( transform & TRANSFORM_INVERTED_AXES )
            {
                destIndex =  ( ( (destX) * t_width) + (destY) );
            }
            else
            {
                destIndex =  ( ( (destY) * t_width) + (destX) );
            }

            destBitsPtr =  destBits + (destIndex * 4) ;

            srcIndex = (((srcY) * imageWidth) + (srcX));
            srcBitsPtr = srcBits + (srcIndex * 4);


            /* copy the pixel that is pointed to */
            *((int *)destBitsPtr) = *((int *)srcBitsPtr);

        } /*for x*/

    } /* for y */


    /* ---------- */

    //processedQImage.setAlphaBuffer(srcQImage->hasAlphaBuffer());

    *srcQImage = processedQImage;
#else
    bool mirrorVertical = transform & TRANSFORM_Y_FLIP;
    bool mirrorHorizontal = transform & TRANSFORM_Y_FLIP;
    if (transform & TRANSFORM_INVERTED_AXES)
        mirrorVertical = !mirrorVertical;
    //*srcQImage = srcQImage->mirrored(mirrorHorizontal, mirrorVertical);
    if (transform & TRANSFORM_INVERTED_AXES)
    {
        QMatrix rotMatrix(0, 1, -1, 0, 0, 0);
        //*srcQImage = srcQImage->transformed(rotMatrix);
    }
#endif
}
