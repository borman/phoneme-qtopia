#ifndef _LFPPORT_QTOPIA_COMMAND_H_
#define _LFPPORT_QTOPIA_COMMAND_H_

#include <QObject>

class JCommandManager: public QObject
{
  Q_OBJECT
  public:
    static void init(); // No corresponding destroy because JCommandManager is deleted by JDisplay
    static JCommandManager *instance();

    void setCommands(MidpCommand* cmds, int numCmds);
  protected:
    JCommandManager(QObject *parent);
    virtual ~JCommandManager();

    static JCommandManager *m_instance;
};

#endif // _LFPPORT_QTOPIA_COMMAND_H_
