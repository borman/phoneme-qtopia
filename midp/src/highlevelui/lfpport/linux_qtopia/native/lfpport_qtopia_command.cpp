#include <cstdio>

#include <jdisplay.h>

#include <lfpport_command.h>
#include "lfpport_qtopia_command.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_alert.h"

extern "C"
{
  MidpError cmdmanager_create(MidpFrame* cmPtr)
  {
    (void)cmPtr;
    JCommandManager::init();
    return KNI_OK;
  }

  MidpError cmdmanager_set_commands(MidpFrame* cmPtr,
                                   MidpCommand* cmds, int numCmds)
  {
    (void)cmPtr;
    JCommandManager::instance()->setCommands(cmds, numCmds);
    return KNI_OK;
  }
}

JCommandManager *JCommandManager::m_instance = NULL;

void JCommandManager::init()
{
  if (!m_instance)
    m_instance = new JCommandManager(JDisplay::current());
}

JCommandManager *JCommandManager::instance()
{
  return m_instance;
}

void JCommandManager::setCommands(MidpCommand* cmds, int numCmds)
{
  for (int i=0; i<numCmds; i++)
  {
    QString longName = pcsl_string2QString(cmds[i].longLabel_str);
    QString shortName = pcsl_string2QString(cmds[i].shortLabel_str);
    printf("Add command \"%s\"(\"%s\"), type=%d, id=%d priority=%d\n", shortName.toUtf8().constData(), longName.toUtf8().constData(),
           cmds[i].type, cmds[i].id, cmds[i].priority);
  }
}

void JCommandManager::setAlertCommands(JAlert *alert, MidpCommand* cmds, int numCmds)
{
  for (int i=0; i<numCmds; i++)
  {
    QString longName = pcsl_string2QString(cmds[i].longLabel_str);
    QString shortName = pcsl_string2QString(cmds[i].shortLabel_str);
    printf("Add alert command \"%s\"(\"%s\"), type=%d, id=%d priority=%d\n", shortName.toUtf8().constData(), longName.toUtf8().constData(),
           cmds[i].type, cmds[i].id, cmds[i].priority);
  }
}

JCommandManager::JCommandManager(QObject *parent)
  : QObject(parent)
{
}

JCommandManager::~JCommandManager()
{
}

#include "lfpport_qtopia_command.moc"
