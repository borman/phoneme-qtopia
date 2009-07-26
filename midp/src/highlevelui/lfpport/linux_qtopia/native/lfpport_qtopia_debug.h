#ifndef _LFPPORT_QTOPIA_DEBUG_H_
#define _LFPPORT_QTOPIA_DEBUG_H_

#include <midp_logging.h>
#include <QtDebug>

#define debug_trace() qDebug("TRACE: %s\n", __func__)

#define debug_dumpdisp(disp) qDebug("MidpDisplayable 0x%08x\n{\n  widgetPtr==0x%08x\n  show==0x%08x\n  hideAndDelete==0x%08x\n  handleEvent==0x%08x\n  setTitle==0x%08x\n  setTicker==0x%08x\n}\n", \
  (disp), (disp)->frame.widgetPtr, (disp)->frame.show, (disp)->frame.hideAndDelete, (disp)->frame.handleEvent, (disp)->setTitle, (disp)->setTicker)

#endif // #ifndef _LFPPORT_QTOPIA_DEBUG_H_
