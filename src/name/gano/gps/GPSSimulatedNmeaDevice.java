/*
 * Simulate a NMEA GPS Device -- for testing without a GPS
 * Mimics the output of a - GlobalSat BU-353 USB GPS Receiver
 */

/*
    This file is part of GpsInspector.

    GpsInspector is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GpsInspector is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GpsInspector.  If not, see <http://www.gnu.org/licenses/>.

 */

/*
 *  Copyright(C) 2009 Shawn Gano shawn@gano.name
*/

package name.gano.gps;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Hashtable;
import javax.swing.JOptionPane;
import org.dinopolis.gpstool.gpsinput.GPSDevice;
import org.dinopolis.gpstool.gpsinput.GPSException;

/**
 *
 * @author Shawn Gano, 7 April 2009
 */
public class GPSSimulatedNmeaDevice implements GPSDevice, Runnable
{
    PipedInputStream in = new PipedInputStream();
    PipedOutputStream out;

    ByteArrayOutputStream outStream = new ByteArrayOutputStream(); //receives messages from processor

    boolean stopped = true; // initally stopped
    Thread simulation_thread;

    int SLEEP_TIME = 1000; // milli seconds to sleep inbetween data sendings (default - build in data)

    // playback from file options
    boolean playBackfromFile = false;
    File dataFile;
    BufferedReader dataBufferedReader;
    float playbackMultiplier = 1.0f;


    public GPSSimulatedNmeaDevice()
    {

    }

    public GPSSimulatedNmeaDevice(File dataFile, float playbackMultiplier)
    {
        this.dataFile = dataFile;
        this.playbackMultiplier = playbackMultiplier;

        // create buffered file reader
        try
        {
            dataBufferedReader = new BufferedReader(new FileReader(dataFile));

            playBackfromFile = true;
        } catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "Error Reading GPS File: \n" + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }

    } // GPSSimulatedNmeaDevice

    /**
     * Initialize the GPSDevice and hand over all information needed for
     * the specific GPSDevice to opens the connection.
     *
     * @param environment contains all informations needed to initialize
     * the gps device.
     * @exception GPSException if the initialization was not successfull,
     * e.g. some information in the environment is missing.
     */
    public void init(Hashtable environment) throws GPSException
    {
        // do nothing
    } // init

    /**
     * Opens the gps device (e.g. serial connection to gps-receiver or
     * file containing logging information from a gps-receiver).
     * @exception GPSException if the opening of the device was not successfull.
     */
    public void open() throws GPSException
    {

        try{
        out = new PipedOutputStream(in);
        }catch(Exception e)
        {
            throw new GPSException(e.toString());
        }


        // start the Runnable Thread here that generates the output
        //    System.out.println("GPSSimulationDataProcessor.open called");
        if(!stopped) // still running
        {
            close();
        }
        stopped = false;
        simulation_thread = new Thread(this, "GPSSimulatedNmeaDevice");
        //simulation_thread.setDaemon(true); // so thread is finished after exit of application
        // NOT A DAEMON -- because it should run until told to stop
        simulation_thread.start();

    } // open

    /**
     * Closes the connection to the GPSDevice.
     * @exception GPSException if closing the device was not successfull.
     */
    public void close() throws GPSException
    {
        stopped = true;
        if(simulation_thread != null)
        {
            simulation_thread.interrupt();
        }

        // close streams
        try
        {
            in.close();
            out.close();

            if(playBackfromFile)
            {
                dataBufferedReader.close();
            }

        }
        catch(Exception e)
        {
        }
    } // close

    //----------------------------------------------------------------------
    /**
     * Returns an input stream from the gps device. If the port is
     * unidirectional and doesn't support receiving data or
     * <code>open</code> was not called before, then getInputStream
     * returns <code>null</code>.
     * @return an input stream from the gps device.
     */
    public InputStream getInputStream() throws IOException
    {
        //return new ByteArrayInputStream(gpsDataStream.toByteArray()); //inStream;
        return in;
    } // getInputStream

    //----------------------------------------------------------------------
    /**
     * Returns an output stream from the gps device. If the port is
     * unidirectional and doesn't support receiving data or
     * <code>open</code> was not called before, then getOutputStream
     * returns <code>null</code>.
     * @return an output stream from the gps device.
     */
    public OutputStream getOutputStream() throws IOException
    {
        return outStream;
    } // getOutputStream

    // -----------------------------------------------------------------------
    // Simulation THREAD

    int messageNum =0;
    int secondCount = 0;


    public void run()
    {

        //inStream.
        long sleepTime = SLEEP_TIME;
        
        while(!stopped)
        {

            if(playBackfromFile)
            {
                sleepTime = sendReadBufferDataMessageBlocks();
            }
            else // use internal sim data
            {
                sleepTime = sendBuiltInDataMessageBlocks();
            }


            try
            {
                Thread.sleep(sleepTime);
            }
            catch(InterruptedException ie)
            {
                System.out.println("GPSSimulatedNmeaDevice: simulation thread stopped.");
                return;
            }
            //System.out.println("slept " + stopped);

        } // while

    } //run

    // reads in GPS data from a file 
    private long sendReadBufferDataMessageBlocks()
    {
        //TODO
        long sleep = 0;

        String line = "";
        try
        {
            line = dataBufferedReader.readLine();
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Error reading from file: " + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
            try{
                close(); // close down the device
            }catch(Exception ee){}
        }

        // see if this is the end of the file
        if(line == null)
        {
            JOptionPane.showMessageDialog(null, "Done reading from file.", "Finished", JOptionPane.INFORMATION_MESSAGE);
            try{
                close(); // close down the device
            }catch(Exception ee){}
            return 1000;
        }

        // see if line commented
        if(line.startsWith("#"))
        {
            return 0;  // skip this line
        }

        // see if line is proper
        String[] segments = line.split("##");
        if(segments.length < 2)
        {
            return 0; // not a valid line
        }

        // handle the data
        String message = segments[0] + (char)13 + (char)10;
        sleep = Long.parseLong(segments[1]);

        try
        {
            out.write(message.getBytes(), 0, message.length());
            out.flush(); // SEG test
            //System.out.println(message);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        long sleepReturn = (long)(sleep / playbackMultiplier);

        return sleepReturn;
    }


    // sends a built in simulated message blocks
    private long sendBuiltInDataMessageBlocks()
    {
            secondCount++;

            int messages2Send = 3;
            if(secondCount >= 5)
            {
                messages2Send = 6; // include GSV messages
                secondCount = 0;
            }

            for(int i = 0; i < messages2Send; i++)
            {

                String message = nmeaData[messageNum] + (char)13 + (char)10;
                messageNum++;

                // check if we need to start loop to the top
                if(messageNum >= nmeaData.length)
                {
                    messageNum = 0;
                }

                //gpsDataStream.write(message.getBytes(),0,message.length());
                //System.out.println(message);

                try
                {
                    out.write(message.getBytes(), 0, message.length());
                    out.flush(); // helps mem problem?
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            } // messages to send
            // having this message and the slept message seems to stop the max messages error on closing
            //System.out.println("sent");

            return SLEEP_TIME; // always wait 1 second after each block
    } // sendBuiltInDataMessageBlocks


    //===================================================================
    // Approx 1 minute worth of data
    //===================================================================

    // no errors
    String[] nmeaData = new String[]
    {
        "$GPGGA,021811.000,2930.7036,N,09501.3401,W,2,08,1.1,15.1,M,-23.9,M,0.8,0000*79",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.4*31",
        "$GPRMC,021811.000,A,2930.7036,N,09501.3401,W,0.15,157.02,060409,,*15",
        "$GPGGA,021812.000,2930.7036,N,09501.3401,W,2,08,1.1,14.9,M,-23.9,M,0.8,0000*73",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.4*31",
        "$GPRMC,021812.000,A,2930.7036,N,09501.3401,W,0.39,174.84,060409,,*17",
        "$GPGGA,021813.000,2930.7036,N,09501.3401,W,2,08,1.1,15.2,M,-23.9,M,1.8,0000*79",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.4*31",
        "$GPRMC,021813.000,A,2930.7036,N,09501.3401,W,0.15,116.01,060409,,*11",
        "$GPGGA,021814.000,2930.7036,N,09501.3401,W,2,08,1.1,15.2,M,-23.9,M,2.8,0000*7D",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.4*31",
        "$GPRMC,021814.000,A,2930.7036,N,09501.3401,W,0.19,130.74,060409,,*1C",
        "$GPGGA,021815.000,2930.7035,N,09501.3401,W,2,08,1.1,14.9,M,-23.9,M,3.8,0000*74",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.4*31",
        "$GPGSV,3,1,12,23,81,342,19,03,48,124,,13,46,325,33,06,43,099,31*72",
        "$GPGSV,3,2,12,16,40,038,28,25,39,313,29,20,28,212,26,19,28,155,*70",
        "$GPGSV,3,3,12,07,24,294,25,32,19,187,21,31,02,085,,51,53,203,27*7C",
        "$GPRMC,021815.000,A,2930.7035,N,09501.3401,W,0.21,150.90,060409,,*19",
        "$GPGGA,021816.000,2930.7036,N,09501.3401,W,2,08,1.1,14.7,M,-23.9,M,0.8,0000*79",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.4*31",
        "$GPRMC,021816.000,A,2930.7036,N,09501.3401,W,0.05,58.47,060409,,*2C",
        "$GPGGA,021817.000,2930.7035,N,09501.3401,W,2,08,1.1,14.9,M,-23.9,M,0.8,0000*75",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.4*31",
        "$GPRMC,021817.000,A,2930.7035,N,09501.3401,W,0.20,152.03,060409,,*12",
        "$GPGGA,021818.000,2930.7035,N,09501.3402,W,2,08,1.1,15.0,M,-23.9,M,1.8,0000*70",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.4*31",
        "$GPRMC,021818.000,A,2930.7035,N,09501.3402,W,0.13,6.89,060409,,*1C",
        "$GPGGA,021819.000,2930.7035,N,09501.3402,W,2,08,1.1,14.8,M,-23.9,M,0.8,0000*79",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.4*31",
        "$GPRMC,021819.000,A,2930.7035,N,09501.3402,W,0.24,26.64,060409,,*28",
        "$GPGGA,021820.000,2930.7034,N,09501.3403,W,2,08,1.1,14.8,M,-23.9,M,1.8,0000*72",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.3*36",
        "$GPGSV,3,1,12,23,81,342,20,03,48,124,,13,46,325,32,06,43,099,31*79",
        "$GPGSV,3,2,12,16,40,038,28,25,39,313,29,20,28,211,25,19,28,155,*70",
        "$GPGSV,3,3,12,07,24,294,25,32,19,187,20,31,02,085,,51,53,203,26*7C",
        "$GPRMC,021820.000,A,2930.7034,N,09501.3403,W,0.09,159.91,060409,,*1E",
        "$GPGGA,021821.000,2930.7034,N,09501.3403,W,2,08,1.1,14.5,M,-23.9,M,0.8,0000*7F",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.3*36",
        "$GPRMC,021821.000,A,2930.7034,N,09501.3403,W,0.21,179.07,060409,,*18",
        "$GPGGA,021822.000,2930.7033,N,09501.3404,W,2,08,1.1,14.4,M,-23.9,M,1.8,0000*7C",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.3*36",
        "$GPRMC,021822.000,A,2930.7033,N,09501.3404,W,0.15,21.97,060409,,*29",
        "$GPGGA,021823.000,2930.7033,N,09501.3404,W,2,08,1.1,14.2,M,-23.9,M,0.8,0000*7A",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.3*36",
        "$GPRMC,021823.000,A,2930.7033,N,09501.3404,W,0.23,33.13,060409,,*22",
        "$GPGGA,021824.000,2930.7033,N,09501.3404,W,2,08,1.1,14.3,M,-23.9,M,0.8,0000*7C",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.3*36",
        "$GPRMC,021824.000,A,2930.7033,N,09501.3404,W,0.13,104.39,060409,,*1B",
        "$GPGGA,021825.000,2930.7032,N,09501.3405,W,2,08,1.1,14.3,M,-23.9,M,0.8,0000*7D",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.3*36",
        "$GPGSV,3,1,12,23,81,342,20,03,48,124,,13,46,325,32,06,43,099,31*79",
        "$GPGSV,3,2,12,16,40,038,28,25,39,313,28,20,28,211,25,19,28,155,*71",
        "$GPGSV,3,3,12,07,24,294,24,32,19,187,20,31,02,085,,51,53,203,27*7C",
        "$GPRMC,021825.000,A,2930.7032,N,09501.3405,W,0.10,147.49,060409,,*19",
        "$GPGGA,021826.000,2930.7031,N,09501.3405,W,2,08,1.1,13.9,M,-23.9,M,1.8,0000*71",
        "$GPGSA,A,3,23,16,20,13,25,06,32,07,,,,,2.6,1.1,2.3*36",
        "$GPRMC,021826.000,A,2930.7031,N,09501.3405,W,0.16,169.44,060409,,*1E",
        "$GPGGA,021827.000,2930.7030,N,09501.3405,W,2,09,1.0,13.7,M,-23.9,M,0.8,0000*7E",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.6,1.0,2.3*3F",
        "$GPRMC,021827.000,A,2930.7030,N,09501.3405,W,0.25,118.58,060409,,*15",
        "$GPGGA,021828.000,2930.7029,N,09501.3405,W,2,09,1.0,13.6,M,-23.9,M,1.8,0000*79",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021828.000,A,2930.7029,N,09501.3405,W,0.33,158.11,060409,,*1C",
        "$GPGGA,021829.000,2930.7028,N,09501.3405,W,2,09,1.0,13.5,M,-23.9,M,0.8,0000*7B",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021829.000,A,2930.7028,N,09501.3405,W,0.28,156.96,060409,,*17",
        "$GPGGA,021830.000,2930.7028,N,09501.3405,W,2,09,1.0,13.3,M,-23.9,M,1.8,0000*74",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPGSV,3,1,12,23,81,342,22,03,48,124,,13,46,325,32,06,43,099,31*7B",
        "$GPGSV,3,2,12,16,40,038,28,25,39,313,28,20,28,211,26,19,28,155,17*74",
        "$GPGSV,3,3,12,07,24,294,24,32,19,187,20,31,02,085,,51,53,203,27*7C",
        "$GPRMC,021830.000,A,2930.7028,N,09501.3405,W,0.11,113.80,060409,,*13",
        "$GPGGA,021831.000,2930.7028,N,09501.3406,W,2,09,1.0,13.5,M,-23.9,M,0.8,0000*71",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021831.000,A,2930.7028,N,09501.3406,W,0.28,16.34,060409,,*20",
        "$GPGGA,021832.000,2930.7029,N,09501.3406,W,2,09,1.0,13.3,M,-23.9,M,1.8,0000*74",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021832.000,A,2930.7029,N,09501.3406,W,0.30,28.32,060409,,*20",
        "$GPGGA,021833.000,2930.7029,N,09501.3406,W,2,09,1.0,13.2,M,-23.9,M,0.8,0000*75",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021833.000,A,2930.7029,N,09501.3406,W,0.17,117.00,060409,,*18",
        "$GPGGA,021834.000,2930.7029,N,09501.3405,W,2,09,1.0,12.8,M,-23.9,M,1.8,0000*7B",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021834.000,A,2930.7029,N,09501.3405,W,0.32,154.02,060409,,*1E",
        "$GPGGA,021835.000,2930.7028,N,09501.3405,W,2,09,1.0,12.8,M,-23.9,M,0.8,0000*7A",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPGSV,3,1,12,23,81,342,23,03,48,124,,13,46,325,32,06,43,099,31*7A",
        "$GPGSV,3,2,12,16,40,038,28,25,39,313,28,20,28,211,26,19,28,155,18*7B",
        "$GPGSV,3,3,12,07,24,294,24,32,19,187,20,31,02,085,,51,53,203,27*7C",
        "$GPRMC,021835.000,A,2930.7028,N,09501.3405,W,0.17,84.01,060409,,*26",
        "$GPGGA,021836.000,2930.7028,N,09501.3405,W,2,09,1.0,13.1,M,-23.9,M,0.8,0000*71",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021836.000,A,2930.7028,N,09501.3405,W,0.30,90.80,060409,,*2C",
        "$GPGGA,021837.000,2930.7028,N,09501.3405,W,2,09,1.0,13.3,M,-23.9,M,0.8,0000*72",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021837.000,A,2930.7028,N,09501.3405,W,0.23,142.88,060409,,*19",
        "$GPGGA,021838.000,2930.7028,N,09501.3405,W,2,09,1.0,13.0,M,-23.9,M,1.8,0000*7F",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021838.000,A,2930.7028,N,09501.3405,W,0.21,190.69,060409,,*14",
        "$GPGGA,021839.000,2930.7028,N,09501.3405,W,2,09,1.0,13.2,M,-23.9,M,2.8,0000*7F",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021839.000,A,2930.7028,N,09501.3405,W,0.17,9.55,060409,,*1E",
        "$GPGGA,021840.000,2930.7028,N,09501.3406,W,2,09,1.0,13.5,M,-23.9,M,0.8,0000*77",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPGSV,3,1,12,23,81,342,24,03,48,123,,13,47,325,32,06,43,098,31*7A",
        "$GPGSV,3,2,12,25,40,313,28,16,39,038,27,19,28,155,20,20,28,211,27*7E",
        "$GPGSV,3,3,12,07,24,294,25,32,19,187,21,31,01,085,,51,53,203,28*70",
        "$GPRMC,021840.000,A,2930.7028,N,09501.3406,W,0.05,150.77,060409,,*1D",
        "$GPGGA,021841.000,2930.7028,N,09501.3406,W,2,09,1.0,13.7,M,-23.9,M,0.8,0000*74",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021841.000,A,2930.7028,N,09501.3406,W,0.09,148.62,060409,,*1D",
        "$GPGGA,021842.000,2930.7027,N,09501.3407,W,2,09,1.0,13.7,M,-23.9,M,0.8,0000*79",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021842.000,A,2930.7027,N,09501.3407,W,0.31,179.08,060409,,*15",
        "$GPGGA,021843.000,2930.7027,N,09501.3407,W,2,09,1.0,13.4,M,-23.9,M,0.8,0000*7B",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021843.000,A,2930.7027,N,09501.3407,W,0.24,192.18,060409,,*14",
        "$GPGGA,021844.000,2930.7028,N,09501.3407,W,2,09,1.0,13.2,M,-23.9,M,0.8,0000*75",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021844.000,A,2930.7028,N,09501.3407,W,0.18,196.26,060409,,*1A",
        "$GPGGA,021845.000,2930.7028,N,09501.3406,W,2,09,1.0,13.0,M,-23.9,M,0.8,0000*77",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPGSV,3,1,12,23,81,342,24,03,48,123,19,13,47,325,32,06,43,098,31*72",
        "$GPGSV,3,2,12,25,40,313,29,16,39,038,27,19,28,155,21,20,28,211,27*7E",
        "$GPGSV,3,3,12,07,24,294,25,32,19,187,21,31,01,085,,51,53,203,27*7F",
        "$GPRMC,021845.000,A,2930.7028,N,09501.3406,W,0.14,15.47,060409,,*2B",
        "$GPGGA,021846.000,2930.7029,N,09501.3406,W,2,09,1.0,12.7,M,-23.9,M,1.8,0000*72",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021846.000,A,2930.7029,N,09501.3406,W,0.07,62.88,060409,,*28",
        "$GPGGA,021847.000,2930.7030,N,09501.3405,W,2,09,1.0,12.3,M,-23.9,M,0.8,0000*",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021847.000,A,2930.7030,N,09501.3405,W,0.07,52.58,060409,,*2C",
        "$GPGGA,021848.000,2930.7031,N,09501.3404,W,2,09,1.0,11.7,M,-23.9,M,0.8,0000*75",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021848.000,A,2930.7031,N,09501.3404,W,0.10,144.20,060409,,*1C",
        "$GPGGA,021849.000,2930.7032,N,09501.3404,W,2,09,1.0,11.4,M,-23.9,M,0.8,0000*74",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021849.000,A,2930.7032,N,09501.3404,W,0.19,96.44,060409,,*2B",
        "$GPGGA,021850.000,2930.7032,N,09501.3403,W,2,09,1.0,11.1,M,-23.9,M,1.8,0000*7F",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPGSV,3,1,12,23,81,342,25,03,48,123,20,13,47,325,32,06,43,098,31*79",
        "$GPGSV,3,2,12,25,40,313,29,16,39,038,26,19,28,155,22,20,28,211,27*7C",
        "$GPGSV,3,3,12,07,25,295,25,32,19,187,22,31,01,085,,51,53,203,28*73",
        "$GPRMC,021850.000,A,2930.7032,N,09501.3403,W,0.24,152.78,060409,,*1C",
        "$GPGGA,021851.000,2930.7033,N,09501.3402,W,2,09,1.0,10.2,M,-23.9,M,2.8,0000*7F",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021851.000,A,2930.7033,N,09501.3402,W,0.14,118.99,060409,,*1F",
        "$GPGGA,021852.000,2930.7034,N,09501.3400,W,2,09,1.0,9.6,M,-23.9,M,0.8,0000*",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021852.000,A,2930.7034,N,09501.3400,W,0.19,130.44,060409,,*1E",
        "$GPGGA,021853.000,2930.7036,N,09501.3398,W,2,09,1.0,8.4,M,-23.9,M,0.8,0000*41",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021853.000,A,2930.7036,N,09501.3398,W,0.13,176.05,060409,,*16",
        "$GPGGA,021854.000,2930.7037,N,09501.3397,W,2,09,1.0,7.1,M,-23.9,M,0.8,0000*42",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021854.000,A,2930.7037,N,09501.3397,W,0.33,169.98,060409,,*17",
        "$GPGGA,021855.000,2930.7039,N,09501.3396,W,2,09,1.0,6.2,M,-23.9,M,0.8,0000*4E",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPGSV,3,1,12,23,81,342,25,03,48,123,21,13,47,325,32,06,43,098,31*78",
        "$GPGSV,3,2,12,25,40,313,29,16,39,038,26,19,28,155,23,20,28,211,27*7D",
        "$GPGSV,3,3,12,07,25,295,26,32,19,187,22,31,01,085,,51,53,203,28*70",
        "$GPRMC,021855.000,A,2930.7039,N,09501.3396,W,0.16,55.87,060409,,*2E",
        "$GPGGA,021856.000,2930.7040,N,09501.3395,W,2,09,1.0,5.5,M,-23.9,M,1.8,0000*45",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021856.000,A,2930.7040,N,09501.3395,W,0.22,51.92,060409,,*27",
        "$GPGGA,021857.000,2930.7042,N,09501.3394,W,2,09,1.0,4.6,M,-23.9,M,2.8,0000*46",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021857.000,A,2930.7042,N,09501.3394,W,0.22,26.10,060409,,*2F",
        "$GPGGA,021858.000,2930.7043,N,09501.3394,W,2,09,1.0,3.8,M,-23.9,M,3.8,0000*40",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021858.000,A,2930.7043,N,09501.3394,W,0.19,17.25,060409,,*2D",
        "$GPGGA,021859.000,2930.7044,N,09501.3393,W,2,09,1.0,3.3,M,-23.9,M,0.8,0000*49",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021859.000,A,2930.7044,N,09501.3393,W,0.16,90.54,060409,,*2A",
        "$GPGGA,021900.000,2930.7045,N,09501.3392,W,2,09,1.0,2.6,M,-23.9,M,0.8,0000*40",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPGSV,3,1,12,23,81,342,25,03,48,123,21,13,47,325,32,06,43,098,32*7B",
        "$GPGSV,3,2,12,25,40,313,28,16,39,038,26,19,28,155,24,20,28,211,27*7B",
        "$GPGSV,3,3,12,07,25,295,26,32,19,187,22,31,01,085,,51,53,203,27*7F",
        "$GPRMC,021900.000,A,2930.7045,N,09501.3392,W,0.13,40.95,060409,,*22",
        "$GPGGA,021901.000,2930.7046,N,09501.3392,W,2,09,1.0,2.1,M,-23.9,M,0.8,0000*45",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021901.000,A,2930.7046,N,09501.3392,W,0.25,50.50,060409,,*2D",
        "$GPGGA,021902.000,2930.7046,N,09501.3393,W,2,09,1.0,2.1,M,-23.9,M,1.8,0000*46",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021902.000,A,2930.7046,N,09501.3393,W,0.19,43.79,060409,,*29",
        "$GPGGA,021903.000,2930.7046,N,09501.3392,W,2,09,1.0,1.9,M,-23.9,M,2.8,0000*4E",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021903.000,A,2930.7046,N,09501.3392,W,0.23,45.05,060409,,*2D",
        "$GPGGA,021904.000,2930.7047,N,09501.3392,W,2,09,1.0,1.6,M,-23.9,M,3.8,0000*46",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021904.000,A,2930.7047,N,09501.3392,W,0.13,149.14,060409,,*15",
        "$GPGGA,021905.000,2930.7047,N,09501.3392,W,2,09,1.0,1.3,M,-23.9,M,0.8,0000*41",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPGSV,3,1,12,23,81,342,25,03,48,123,22,13,47,325,32,06,43,098,32*78",
        "$GPGSV,3,2,12,25,40,313,28,16,39,038,26,19,28,155,25,20,28,211,27*7A",
        "$GPGSV,3,3,12,07,25,295,26,32,19,187,23,31,01,085,,51,53,203,28*71",
        "$GPRMC,021905.000,A,2930.7047,N,09501.3392,W,0.09,174.65,060409,,*17",
        "$GPGGA,021906.000,2930.7047,N,09501.3393,W,2,09,1.0,1.0,M,-23.9,M,0.8,0000*40",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021906.000,A,2930.7047,N,09501.3393,W,0.03,175.40,060409,,*19",
        "$GPGGA,021907.000,2930.7047,N,09501.3393,W,2,09,1.0,0.6,M,-23.9,M,0.8,0000*46",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021907.000,A,2930.7047,N,09501.3393,W,0.23,30.60,060409,,*28",
        "$GPGGA,021908.000,2930.7048,N,09501.3392,W,2,09,1.0,0.3,M,-23.9,M,1.8,0000*43",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021908.000,A,2930.7048,N,09501.3392,W,0.29,32.77,060409,,*27",
        "$GPGGA,021909.000,2930.7049,N,09501.3392,W,2,09,1.0,-0.0,M,-23.9,M,0.8,0000*6C",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPRMC,021909.000,A,2930.7049,N,09501.3392,W,0.04,347.35,060409,,*1F",
        "$GPGGA,021910.000,2930.7049,N,09501.3393,W,2,09,1.0,-0.3,M,-23.9,M,1.8,0000*67",
        "$GPGSA,A,3,23,16,20,13,25,06,19,32,07,,,,2.5,1.0,2.3*3C",
        "$GPGSV,3,1,12,23,81,342,25,03,48,123,22,13,47,325,32,06,43,098,32*78",
        "$GPGSV,3,2,12,25,40,313,28,16,39,038,26,19,28,155,26,20,28,211,27*79",
        "$GPGSV,3,3,12,07,25,295,26,32,19,187,23,31,01,085,,51,53,203,28*71",
        "$GPRMC,021910.000,A,2930.7049,N,09501.3393,W,0.09,182.19,060409,,*1E",
    };



} // GPSSimulatedNmeaDevice
