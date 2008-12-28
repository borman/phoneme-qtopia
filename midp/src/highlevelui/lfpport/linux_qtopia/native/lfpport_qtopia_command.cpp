#include <QSoftMenuBar>
#include <QMenu>

#include <jdisplay.h>
#include <lfpport_command.h>

#include "lfpport_qtopia_command.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_alert.h"
#include "lfpport_qtopia_debug.h"

static char *commandTypeNames[9] = 
{
  "None",
  "Screen",
  "Back",
  "Cancel",
  "Ok",
  "Help",
  "Stop",
  "Exit",
  "Item",  
};

extern "C"
{
  MidpError cmdmanager_create(MidpFrame* cmPtr)
  {
    lfpport_log("cmdmanager_create\n");
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

//----------------------------

int JCommand::sort_order[9] = COMMAND_SORT_ALL_TABLE;

JCommand::JCommand(QObject *parent, const QString &short_name, const QString &long_name, int id, int type, int priority)
  : QObject(parent),
  m_id(id), m_type(type), m_priority(priority), m_sname(short_name), m_lname(long_name),
  m_action(this)
{
  m_action.setText(short_name);
  connect(&m_action, SIGNAL(triggered()), SLOT(triggered()));
}

JCommand::~JCommand()
{
}

int JCommand::id() const
{
  return m_id;
}

int JCommand::type() const
{
  return m_type;
}

int JCommand::priority() const
{
  return m_priority;
}

bool JCommand::operator<(const JCommand &other) const
{
  return (sort_order[m_type]<sort_order[other.m_type]) || 
         (sort_order[m_type]==sort_order[other.m_type] && m_priority<other.m_priority);
}

QAction *JCommand::action()
{
  return &m_action;
}

void JCommand::triggered()
{
  MidpCommandSelected(m_id);
}

//----------------------------

JCommandManager *JCommandManager::m_instance = NULL;

void JCommandManager::init()
{
  lfpport_log("JCommandManager::init()\n");
  if (!m_instance)
    m_instance = new JCommandManager(JDisplay::current());
}

JCommandManager *JCommandManager::instance()
{
  return m_instance;
}

void JCommandManager::setCommands(MidpCommand* cmds, int numCmds)
{
  lfpport_log("Command list start\n");
  
  QMenu *commandsMenu = QSoftMenuBar::menuFor(JDisplay::current());
  commandsMenu->clear();
  if (!commands.isEmpty())
    qDeleteAll(commands);
  commands.clear();
  commands.reserve(numCmds);
  
  for (int i=0; i<numCmds; i++)
  {
    QString longName = pcsl_string2QString(cmds[i].longLabel_str);
    QString shortName = pcsl_string2QString(cmds[i].shortLabel_str);
    lfpport_log("Command \"%s\"(\"%s\"), type=\"%s\", id=%d priority=%d\n", 
           shortName.toUtf8().constData(), longName.toUtf8().constData(),
           commandTypeNames[cmds[i].type], cmds[i].id, cmds[i].priority);
    
    commands.append(new JCommand(this, shortName, longName, cmds[i].id, cmds[i].type, cmds[i].priority));
  }
  
  qSort(commands);
  for (QVector<JCommand *>::iterator it = commands.begin(); it!=commands.end(); it++)
  {
    if ((*it)->type()!=JCommand::None)
      commandsMenu->addAction((*it)->action());
  }
  
  lfpport_log("Command list end\n");
}

void JCommandManager::setAlertCommands(JAlert *alert, MidpCommand* cmds, int numCmds)
{
  lfpport_log("Alert command list start\n");
  for (int i=0; i<numCmds; i++)
  {
    QString longName = pcsl_string2QString(cmds[i].longLabel_str);
    QString shortName = pcsl_string2QString(cmds[i].shortLabel_str);
    lfpport_log("Alert command \"%s\"(\"%s\"), type=\"%s\", id=%d priority=%d\n", shortName.toUtf8().constData(), longName.toUtf8().constData(),
           commandTypeNames[cmds[i].type], cmds[i].id, cmds[i].priority);
  }
  lfpport_log("Alert command list end\n");
}

JCommandManager::JCommandManager(QObject *parent)
  : QObject(parent)
{
}

JCommandManager::~JCommandManager()
{
  lfpport_log("CommandManager constructed\n");
  commands.clear();
}

#include "lfpport_qtopia_command.moc"
