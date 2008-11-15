#ifndef _LFJPORT_QTOPIA_KEYMAP_H_
#define _LFJPORT_QTOPIA_KEYMAP_H_

#include <QHash>

class LFJKeyMap
{
  public:
    static LFJKeyMap *instance();
    static void init();
    static void destroy();

    bool map(int qtkey, const QString &unicode, int &javakey);
  private:
    LFJKeyMap();
    virtual ~LFJKeyMap();

    static LFJKeyMap *jkeymap;
    QHash<int, int> key_map;
};

#endif // _LFJPORT_QTOPIA_KEYMAP_H_
