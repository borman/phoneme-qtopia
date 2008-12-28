#ifndef _LFPPORT_QTOPIA_COMMAND_H_
#define _LFPPORT_QTOPIA_COMMAND_H_

#include <QObject>
#include <QAction>
#include <QVector>
#include <lfp_command.h>

class JAlert;

class JCommand: public QObject
{
  Q_OBJECT
  public:
    enum Type
    {
      None = COMMAND_TYPE_NONE,
      Screen = COMMAND_TYPE_SCREEN, 
      Back = COMMAND_TYPE_BACK, 
      Cancel = COMMAND_TYPE_CANCEL,
      Ok = COMMAND_TYPE_OK,
      Help = COMMAND_TYPE_HELP,
      Stop = COMMAND_TYPE_STOP, 
      Exit = COMMAND_TYPE_EXIT, 
      Item = COMMAND_TYPE_ITEM 
    };
    
    JCommand(QObject *parent, const QString &short_name, const QString &long_name, int id, int type, int priority);
    ~JCommand();
    
    int id() const;
    
    int type() const;
    int priority() const;
    
    QAction *action();
    
    bool operator<(const JCommand &other) const;
  private slots:
    void triggered();
  private:
    int m_id;
    int m_type;
    int m_priority;
    
    QString m_sname;
    QString m_lname;
    
    QAction m_action;
    
    static int sort_order[9];
};

class JCommandManager: public QObject
{
  Q_OBJECT
  public:
    static void init(); // No corresponding destroy because JCommandManager is deleted by JDisplay
    static JCommandManager *instance();

    void setCommands(MidpCommand* cmds, int numCmds);
    void setAlertCommands(JAlert *alert, MidpCommand* cmds, int numCmds);
  private:
    JCommandManager(QObject *parent);
    virtual ~JCommandManager();

    static JCommandManager *m_instance;
    
    QVector<JCommand *> commands;
};

#endif // _LFPPORT_QTOPIA_COMMAND_H_
