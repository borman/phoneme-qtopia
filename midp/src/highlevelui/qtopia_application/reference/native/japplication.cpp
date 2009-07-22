#include <QTimer>

#include <jvm.h>
#include <suspend_resume.h>
#include <midpEvents.h>

#include <japplication.h>
#include <jdisplay.h>

JApplication *JApplication::jApp = NULL;

JApplication::JApplication(int &argc, char **argv)
  : QtopiaApplication(argc, argv)
{
  sliceTimer.setSingleShot(true);
  vm_suspended = false;
  vm_stopped = true;
  connect(&sliceTimer, SIGNAL(timeout()), SLOT(timeSlice()));
}

JApplication::~JApplication()
{
}

void JApplication::startVM()
{
  vm_stopped = false;

  /* Setup next VM time slice to happen immediately */
  midp_resetEvents();

  scheduleTimeSlice(0);
}

void JApplication::stopVM()
{
  /* Stop any further VM time slice */
  scheduleTimeSlice(-1);
}

void JApplication::suspendVM()
{
  if (!vm_suspended)
  {
    vm_suspended = true;
    scheduleTimeSlice(-1);
  }
}

void JApplication::resumeVM()
{
  if (vm_suspended)
  {
    vm_suspended = false;
    scheduleTimeSlice(0);
  }
}

void JApplication::suspendMidp()
{
  midp_suspend();
}

void JApplication::resumeMidp()
{
  midp_resume();
}

void JApplication::scheduleTimeSlice(int millis)
{
  if (millis<0) // Negative time means that we must stop scheduling slices
    sliceTimer.stop();
  else
    sliceTimer.start(millis);
}

void JApplication::timeSlice()
{
  jlong ms;

  if (vm_stopped)
    return;

  /* check and align stack suspend/resume state */
  midp_checkAndResume();

  ms = vm_suspended ? SR_RESUME_CHECK_TIMEOUT : JVM_TimeSlice();

    /* There is a testing mode with VM running while entire stack is
     * considered to be suspended. Next invocation shold be scheduled
     * to SR_RESUME_CHECK_TIMEOUT in this case.
     */
  if (midp_getSRState() == SR_SUSPENDED)
    ms = SR_RESUME_CHECK_TIMEOUT;

  /* Let the VM run for some time */
  if (ms <= -2)
  {
    vm_stopped = true;
    exit();
  }
  else if (ms >= 0) // schedule next timeslice only if asked to
  {
    sliceTimer.start(ms);
  }
  //JDisplay::current()->repaint();
}

JApplication *JApplication::instance()
{
  return jApp;
}

void JApplication::init()
{
  printf("JApplication initialised\n");
  if (jApp)
    return;
  char *argv[] = {"kvm"};
  int argc = 1;
  jApp = new JApplication(argc, argv);
}

void JApplication::destroy()
{
  if (jApp)
  {
    delete jApp;
    jApp = NULL;
  }
}

#include "moc_japplication.cpp"
