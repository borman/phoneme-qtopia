#include <QHash>
#include <Qt>

#include <keymap_input.h>
#include "lfjport_qtopia_keymap.h"

LFJKeyMap *LFJKeyMap::jkeymap = NULL;

LFJKeyMap *LFJKeyMap::instance()
{
  if (!jkeymap)
    init();
  return jkeymap;
}

void LFJKeyMap::init()
{
  if (!jkeymap)
    jkeymap = new LFJKeyMap();
}

void LFJKeyMap::destroy()
{
  if (jkeymap)
  {
    delete jkeymap;
    jkeymap = NULL;
  }
}

bool LFJKeyMap::map(int qtkey, const QString &unicode, int &javakey)
{
  QHash<int, int>::const_iterator it = key_map.find(qtkey);
  if (it==key_map.end())
  {
    if (!unicode.isEmpty())
    {
      javakey = unicode[0].unicode();
      return true;
    }
    return false;
  }
  javakey = *it;
  return true;
}

LFJKeyMap::LFJKeyMap()
{
  key_map.insert(Qt::Key_Context1, KEYMAP_KEY_SOFT2);
  key_map.insert(Qt::Key_Back, KEYMAP_KEY_SOFT1);

  key_map.insert(Qt::Key_0, KEYMAP_KEY_0);
  key_map.insert(Qt::Key_1, KEYMAP_KEY_1);
  key_map.insert(Qt::Key_2, KEYMAP_KEY_2);
  key_map.insert(Qt::Key_3, KEYMAP_KEY_3);
  key_map.insert(Qt::Key_4, KEYMAP_KEY_4);
  key_map.insert(Qt::Key_5, KEYMAP_KEY_5);
  key_map.insert(Qt::Key_6, KEYMAP_KEY_6);
  key_map.insert(Qt::Key_7, KEYMAP_KEY_7);
  key_map.insert(Qt::Key_8, KEYMAP_KEY_8);
  key_map.insert(Qt::Key_9, KEYMAP_KEY_9);

  key_map.insert(Qt::Key_Left, KEYMAP_KEY_LEFT);
  key_map.insert(Qt::Key_Right, KEYMAP_KEY_RIGHT);
  key_map.insert(Qt::Key_Up, KEYMAP_KEY_UP);
  key_map.insert(Qt::Key_Down, KEYMAP_KEY_DOWN);
  key_map.insert(Qt::Key_Select, KEYMAP_KEY_SELECT);
}

LFJKeyMap::~LFJKeyMap()
{
}
