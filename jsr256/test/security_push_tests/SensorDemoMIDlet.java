/*
 *   
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package examples;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.io.Connector;
import javax.microedition.sensor.*;

/**
 * @author bmb054
 */
public class SensorDemoMIDlet extends MIDlet implements CommandListener, DataListener {

    private boolean midletPaused = false;

    private SensorConnection conn;
    private Data[] retData;
    private Timer timerReadFromSensor;
    private TimerTaskReadFromSensor timerTaskReadFromSensor;
            
    private Command exitCommand;
    private Command start_reading;
    private Command Batterylevel;
    private Command battery_charge;
    private Command Close;
    private Command stopCommand;
    private Command backCommand;
    private Command backCommand1;
    private Form form;
    private ChoiceGroup sensors_set;
    private TextField buffer_size;
    private ChoiceGroup sync_async;
    private TextField buffering_period;
    private TextField reading_interval;
    private Form data_reading;
    private StringItem please_wait;
    private Alert alert;
    private Form data_display;
    private StringItem data_from_sensor;
    private Gauge signal_diagram;
    private Font font;

    /**
     * The SensorDemoMIDlet constructor.
     */
    public SensorDemoMIDlet() {
    }

    /**
     * Initilizes the application.
     * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
     */
    private void initialize() {

    }

    /**
     * Performs an action assigned to the Mobile Device - MIDlet Started point.
     */
    public void startMIDlet() {
        switchDisplayable(null, getForm());
    }

    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {
    }

    /**
     * Switches a current displayable in a display. 
     * The <code>display</code> instance is taken from <code>getDisplay</code> method. 
     * This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; 
     * if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public synchronized void switchDisplayable(Alert alert,Displayable nextDisplayable) {
        Display display = getDisplay();
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }
    }

    /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
        if (displayable == alert) {
            if (command == backCommand) {
                switchDisplayable(null, getForm());//GEN-LINE:|7-commandAction|2|50-postAction
            }
        } else if (displayable == data_display) {
            if (command == backCommand1) {
                if (timerReadFromSensor != null) {
                    timerReadFromSensor.cancel();
                }
                switchDisplayable(null, getForm());
            }
        } else if (displayable == form) {
            if (command == exitCommand) {
                exitMIDlet();
            } else if (command == start_reading) {
                getData_reading();
                String reading_title = "wrong title";
                switch (sync_async.getSelectedIndex()) {
                    case 0: //Syncronous reading
                        reading_title = "Synchronous reading";
                        break;
                    case 1: //Asynchronous reading
                        reading_title = "Asynchronous reading";
                        break;
                }
                data_reading.setTitle(reading_title);
                switchDisplayable(null, getData_reading());
                long period = Long.parseLong(reading_interval.getString());
                if (timerReadFromSensor == null) {
                    timerReadFromSensor = new Timer();
                    timerTaskReadFromSensor = new TimerTaskReadFromSensor(this);
                }
                if (period > 0L) {
                    timerReadFromSensor.schedule(timerTaskReadFromSensor, 0L, period);
                } else {
                    timerReadFromSensor.schedule(timerTaskReadFromSensor, 0L);
                }
            }
        }
    }

    // read data from sensor on background
    void readSensor() {
        String openURL = "sensor:";
        switch(sensors_set.getSelectedIndex()) {
            case 0: // Battery level
                openURL += "battery_level";
                break;
            case 1: // Battery charge
                openURL += "battery_charge";
                break;
        }
        try {
            conn = (SensorConnection)Connector.open(openURL);
        } catch (Throwable ex) {
            getAlert().setString("Sensor open error: "+ex.getMessage());
            switchDisplayable(null, alert);
            return;
        }
        int buffSize = Integer.parseInt(buffer_size.getString());
        long period = Long.parseLong(buffering_period.getString());
        try {
            switch(sync_async.getSelectedIndex()) {
                case 0: // sync data reading
                    retData = conn.getData(buffSize, period, true, true, true);
                    break;
                case 1: // async data reading
                    conn.setDataListener((DataListener)this, buffSize, period, true, true, true);
                    synchronized (this) {
                        wait();
                    }
                    break;
            }
        } catch (Exception ex) {
            try {
                conn.close();
            } catch (IOException exc) {
                // ignore
            }
            getAlert().setString("Sensor reading error: "+ex.getMessage());
            switchDisplayable(null, alert);
            return;
        }
        int minValue = (int)conn.getSensorInfo().getChannelInfos()[0].getMeasurementRanges()[0].getSmallestValue();
        int maxValue = (int)conn.getSensorInfo().getChannelInfos()[0].getMeasurementRanges()[0].getLargestValue();
        try {
            conn.close();
        } catch (IOException ex) {
            // ignore
        }
        // Print table
        String outp = "";
        for (int i = 0; i < retData.length; i++) {
            outp += "Channel " + i +":\n";
            int[] chData = retData[i].getIntValues();
            for (int j = 0; j < chData.length; j++) {
                outp += chData[j] + " ";
            }
            outp += "\n";
        }
        getData_from_sensor().setText(outp);
        getSignal_diagram().setMaxValue(maxValue - minValue);
        getSignal_diagram().setValue(retData[0].getIntValues()[0]);
        switchDisplayable(null, getData_display());
    }
    
    public synchronized void dataReceived(SensorConnection sensor, Data[] data, boolean isDataLost) {
        retData = data;
        notify();
    }
    
    /**
     * Returns an initiliazed instance of exitCommand component.
     * @return the initialized component instance
     */
    public Command getExitCommand() {
        if (exitCommand == null) {
            exitCommand = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|18-getter|1|18-postInit
        }
        return exitCommand;
    }

    /**
     * Returns an initiliazed instance of form component.
     * @return the initialized component instance
     */
    public Form getForm() {
        if (form == null) {
            form = new Form("SensorDemo", new Item[] { getSensors_set(), getSync_async(), getBuffer_size(),
            getBuffering_period(), getReading_interval() });
            form.addCommand(getExitCommand());
            form.addCommand(getStart_reading());
            form.setCommandListener(this);
        }
        return form;
    }

    /**
     * Returns an initiliazed instance of start_reading component.
     * @return the initialized component instance
     */
    public Command getStart_reading() {
        if (start_reading == null) {
            start_reading = new Command("Start", Command.ITEM, 0);
        }
        return start_reading;
    }

    /**
     * Returns an initiliazed instance of Batterylevel component.
     * @return the initialized component instance
     */
    public Command getBatterylevel() {
        if (Batterylevel == null) {
            Batterylevel = new Command("Battery   level", Command.ITEM, 0);
        }
        return Batterylevel;
    }

    /**
     * Returns an initiliazed instance of battery_charge component.
     * @return the initialized component instance
     */
    public Command getBattery_charge() {
        if (battery_charge == null) {
            battery_charge = new Command("Battery charge", Command.ITEM, 0);
        }
        return battery_charge;
    }

    /**
     * Returns an initiliazed instance of sensors_set component.
     * @return the initialized component instance
     */
    public ChoiceGroup getSensors_set() {
        if (sensors_set == null) {
            sensors_set = new ChoiceGroup("Sensors", Choice.EXCLUSIVE);
            sensors_set.append("Battery level", null);
            sensors_set.append("Charge level", null);
            sensors_set.setSelectedFlags(new boolean[] { false, false });
            sensors_set.setFont(0, null);
            sensors_set.setFont(1, null);
        }
        return sensors_set;
    }

    /**
     * Returns an initiliazed instance of Close component.
     * @return the initialized component instance
     */
    public Command getClose() {
        if (Close == null) {
            Close = new Command("Close", Command.ITEM, 0);
        }
        return Close;
    }

    /**
     * Returns an initiliazed instance of data_reading component.
     * @return the initialized component instance
     */
    public Form getData_reading() {
        if (data_reading == null) {
            data_reading = new Form("", new Item[] { getPlease_wait() });
            data_reading.setCommandListener(this);
        }
        return data_reading;
    }

    /**
     * Returns an initiliazed instance of stopCommand component.
     * @return the initialized component instance
     */
    public Command getStopCommand() {
        if (stopCommand == null) {
            stopCommand = new Command("Cancel", Command.ITEM, 0);
        }
        return stopCommand;
    }

    /**
     * Returns an initiliazed instance of backCommand component.
     * @return the initialized component instance
     */
    public Command getBackCommand() {
        if (backCommand == null) {
            backCommand = new Command("Back", Command.BACK, 0);
        }
        return backCommand;
    }

    /**
     * Returns an initiliazed instance of alert component.
     * @return the initialized component instance
     */
    public Alert getAlert() {
        if (alert == null) {
            alert = new Alert("alert", null, null, AlertType.ERROR);
            alert.addCommand(getBackCommand());
            alert.setCommandListener(this);
            alert.setTimeout(Alert.FOREVER);
        }
        return alert;
    }

    /**
     * Returns an initiliazed instance of sync_async component.
     * @return the initialized component instance
     */
    public ChoiceGroup getSync_async() {
        if (sync_async == null) {
            sync_async = new ChoiceGroup("Sensor action", Choice.EXCLUSIVE);
            sync_async.append("Synchromous input", null);
            sync_async.append("Asynchronous input", null);
            sync_async.setSelectedFlags(new boolean[] { false, false });
            sync_async.setFont(0, null);
            sync_async.setFont(1, null);
        }
        return sync_async;
    }

    /**
     * Returns an initiliazed instance of buffer_size component.
     * @return the initialized component instance
     */
    public TextField getBuffer_size() {
        if (buffer_size == null) {
            buffer_size = new TextField("Buffer size (-1 on undefined)", "1", 32,
            TextField.NUMERIC);
        }
        return buffer_size;
    }

    /**
     * Returns an initiliazed instance of buffering_period component.
     * @return the initialized component instance
     */
    public TextField getBuffering_period() {
        if (buffering_period == null) {
            buffering_period = new TextField("Buffering period (-1 on unlimited)", "-1", 32,
            TextField.NUMERIC);
        }
        return buffering_period;
    }

    /**
     * Returns an initiliazed instance of please_wait component.
     * @return the initialized component instance
     */
    public StringItem getPlease_wait() {
        if (please_wait == null) {
            please_wait = new StringItem("Please wait...", "", Item.PLAIN);
            please_wait.setLayout(ImageItem.LAYOUT_CENTER | Item.LAYOUT_TOP | Item.LAYOUT_BOTTOM |
            Item.LAYOUT_VCENTER);
        }
        return please_wait;
    }

    /**
     * Returns an initiliazed instance of font component.
     * @return the initialized component instance
     */
    public Font getFont() {
        if (font == null) {
            font = Font.getDefaultFont();
        }
        return font;
    }

    /**
     * Returns an initiliazed instance of backCommand1 component.
     * @return the initialized component instance
     */
    public Command getBackCommand1() {
        if (backCommand1 == null) {
            backCommand1 = new Command("Back", Command.BACK, 0);
        }
        return backCommand1;
    }

    /**
     * Returns an initiliazed instance of data_display component.
     * @return the initialized component instance
     */
    public Form getData_display() {
        if (data_display == null) {
            data_display = new Form("Data from sensor", new Item[] { getData_from_sensor(), getSignal_diagram()
            });
            data_display.addCommand(getBackCommand1());
            data_display.setCommandListener(this);
        }
        return data_display;
    }

    /**
     * Returns an initiliazed instance of data_from_sensor component.
     * @return the initialized component instance
     */
    public StringItem getData_from_sensor() {
        if (data_from_sensor == null) {
            data_from_sensor = new StringItem("", null);
        }
        return data_from_sensor;
    }

    /**
     * Returns an initiliazed instance of reading_interval component.
     * @return the initialized component instance
     */
    public TextField getReading_interval() {
        if (reading_interval == null) {
            reading_interval = new TextField("Reading interval (0 - once)", "0", 32,
            TextField.NUMERIC);
        }
        return reading_interval;
    }

    /**
     * Returns an initiliazed instance of signal_diagram component.
     * @return the initialized component instance
     */
    public Gauge getSignal_diagram() {
        if (signal_diagram == null) {
            signal_diagram = new Gauge("First data", false, 100, 50);
        }
        return signal_diagram;
    }

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay () {
        return Display.getDisplay(this);
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        switchDisplayable (null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        if (midletPaused) {
            resumeMIDlet ();
        } else {
            initialize ();
            startMIDlet ();
        }
        midletPaused = false;
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
        midletPaused = true;
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally 
     * terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
    }

}

class TimerTaskReadFromSensor extends TimerTask {

    private SensorDemoMIDlet sensorMIDlet;

    TimerTaskReadFromSensor(SensorDemoMIDlet sensorMIDlet) {
        this.sensorMIDlet = sensorMIDlet;
    }
    
    public void run() {
        sensorMIDlet.readSensor();
    }
}
