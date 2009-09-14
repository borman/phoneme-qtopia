#ifndef _LFPPORT_QTOPIA_KEYMAP_H_
#define _LFPPORT_QTOPIA_KEYMAP_H_

#include <QHash>

class LFPKeyMap
{
  public:
    static LFPKeyMap *instance();
    static void init();
    static void destroy();

    bool map(int qtkey, const QString &unicode, int &javakey);
  private:
    LFPKeyMap();
    virtual ~LFPKeyMap();

    static LFPKeyMap *jkeymap;
    QHash<int, int> key_map;
};

#endif // _LFPPORT_QTOPIA_KEYMAP_H_
