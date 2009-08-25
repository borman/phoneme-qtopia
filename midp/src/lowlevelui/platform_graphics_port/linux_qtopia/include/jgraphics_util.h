#ifndef JGRAPHICS_UTIL_H
#define JGRAPHICS_UTIL_H

#include <QTransform>

// WARNING: mindfuck ahead

// Maps javax.microedition.lcdui.game.Sprite transform to Qt's transform matrix
static QTransform transformFromId(int id, int w, int h)
{
/* QTransform's dx and dy are computed like this:
   dx = xw*width + xh*height;
   dy = yw*width + yh*height;
   */
  static struct {int m11; int m12; int m21; int m22; int xw; int xh; int yw; int yh;}
    trans_map[] =
    {
    // ..............................m11 m12 m21 m22    xw  xh  yw  yh
      /* [TRANS_NONE]          = */ {  1,  0,  0,  1,    0,  0,  0,  0},
      /* [TRANS_MIRROR_ROT180] = */ {  1,  0,  0, -1,    0,  0,  0,  1},
      /* [TRANS_MIRROR]        = */ { -1,  0,  0,  1,    1,  0,  0,  0},
      /* [TRANS_ROT180]        = */ { -1,  0,  0, -1,    1,  0,  0,  1},

      /* [TRANS_MIRROR_ROT270] = */ {  0, -1,  1,  0,    0,  0,  1,  0},
      /* [TRANS_ROT90]         = */ {  0,  1, -1,  0,    0,  1,  0,  0},
      /* [TRANS_ROT270]        = */ {  0,  1,  1,  0,    0,  0,  0,  0},
      /* [TRANS_MIRROR_ROT90]  = */ {  0, -1, -1,  0,    0,  1,  1,  0},
    };
    return QTransform(trans_map[id].m11, trans_map[id].m12,
                      trans_map[id].m21, trans_map[id].m22,
                      trans_map[id].xw*w+trans_map[id].xh*h,
                      trans_map[id].yw*w+trans_map[id].yh*h);
}

#if 1
inline bool transformIsFlipping(int id)
{
  return id>3; // Easier, but dirty
}
#else
static bool transformIsFlipping(int id)
{
  static bool trans_flipping[] =
  {
    /* [TRANS_NONE]          = */ false,
    /* [TRANS_MIRROR_ROT180] = */ false,
    /* [TRANS_MIRROR]        = */ false,
    /* [TRANS_ROT180]        = */ false,

    /* [TRANS_MIRROR_ROT270] = */ true,
    /* [TRANS_ROT90]         = */ true,
    /* [TRANS_ROT270]        = */ true,
    /* [TRANS_MIRROR_ROT90]  = */ true,
  };
  return trans_flipping[id];
}
#endif

#endif // JGRAPHICS_UTIL_H
