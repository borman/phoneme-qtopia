#ifndef _LFPPORT_QTOPIA_DEBUG_H_
#define _LFPPORT_QTOPIA_DEBUG_H_

#include <cstdio>

#define debug_trace() printf("%s\n", __func__)

#define debug_qobject(obj) printf("QObject<%s>\n", (obj)->metaObject()->className())

#define debug_dumpdisp(disp) printf("MidpDisplayable 0x%08x\n{\n  widgetPtr==0x%08x\n  show==0x%08x\n  hideAndDelete==0x%08x\n  handleEvent==0x%08x\n  setTitle==0x%08x\n  setTicker==0x%08x\n}\n", \
  (disp), (disp)->frame.widgetPtr, (disp)->frame.show, (disp)->frame.hideAndDelete, (disp)->frame.handleEvent, (disp)->setTitle, (disp)->setTicker)

#endif // _LFPPORT_QTOPIA_DEBUG_H_