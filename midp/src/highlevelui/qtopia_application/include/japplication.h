#ifndef _JAPPLICATION_H_
#define _JAPPLICATION_H_

#include <QtopiaApplication>
#include <QTimer>

class JApplication: public QtopiaApplication
{
  Q_OBJECT
  public:
    JApplication(int &argc, char **argv);
    virtual ~JApplication();

     /**
      * Start to give VM time slice to run.
      */
    void startVM();

    /**
     * Stop VM from any further time slice.
     * Any UI leftover resource will be freed.
     */
    void stopVM();

    /**
     * Suspend VM. VM will not receive time slices until resumed.
     */
    void suspendVM();

    /**
     * Resume VM to normal operation.
     */
    void resumeVM();

    /**
     * Suspend VM. VM will not receive time slices until resumed.
     */
    void suspendMidp();

    /**
     * Resume VM to normal operation.
     */
    void resumeMidp();

    /**
     * Trigger VM timeslice in \a millis milliseconds
     */
    void scheduleTimeSlice(int millis);

    static JApplication *instance();
    static void init();
    static void destroy();
  private slots:
    /**
     * Perform VM timeslice
     */
    void timeSlice();
  private:
    QTimer sliceTimer;
    bool vm_stopped;
    bool vm_suspended;
    static JApplication *jApp;
};

#endif // _JAPPLICATION_H_
