#ifndef _LFPPORT_QTOPIA_DEBUG_H_
#define _LFPPORT_QTOPIA_DEBUG_H_

#include <cstdio>
#include <midp_logging.h>

#define lfpport_log(format, ...) reportToLog(LOG_INFORMATION, 10345, format, ## __VA_ARGS__)

#define debug_trace() lfpport_log("%s\n", __func__)

#define debug_qobject(obj) lfpport_log("QObject<%s>\n", (obj)->metaObject()->className())

#define debug_dumpdisp(disp) lfpport_log("MidpDisplayable 0x%08x\n{\n  widgetPtr==0x%08x\n  show==0x%08x\n  hideAndDelete==0x%08x\n  handleEvent==0x%08x\n  setTitle==0x%08x\n  setTicker==0x%08x\n}\n", \
  (disp), (disp)->frame.widgetPtr, (disp)->frame.show, (disp)->frame.hideAndDelete, (disp)->frame.handleEvent, (disp)->setTitle, (disp)->setTicker)

#endif // #ifndef _LFPPORT_QTOPIA_DEBUG_H_