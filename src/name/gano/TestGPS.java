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
package name.gano;

import java.util.HashMap;
import java.util.Hashtable;
import name.gano.gps.GPSSimulatedNmeaDevice;
import org.dinopolis.gpstool.gpsinput.GPSDevice;
import org.dinopolis.gpstool.gpsinput.GPSNetworkGpsdDevice;
import org.dinopolis.gpstool.gpsinput.GPSRawDataListener;
import org.dinopolis.gpstool.gpsinput.GPSSerialDevice;
import org.dinopolis.gpstool.gpsinput.nmea.GPSNmeaDataProcessor;
import org.dinopolis.gpstool.gpsinput.*;

/**
 *
 * @author Shawn
 */
public class TestGPS
{
    static boolean simulation = false;

    static GPSNmeaDataProcessor gps_nmea_processor;
    static GPSNmeaDataProcessor gps_garmon_processor;
    static GPSNetworkGpsdDevice gpsdDevice;
    
    static int serial_port_speed = 4800;
    static String serial_port_name = "/dev/ttyUSB0";
    static String tcpPort = "2947";
    static String host = "127.0.0.1";

    public static void main(String[] args)
    {
       // gps_nmea_processor = new GPSNmeaDataProcessor();
        gps_garmon_processor = new GPSNmeaDataProcessor();

        gps_garmon_processor.addGPSRawDataListener(
		new GPSRawDataListener()
        {
          public void gpsRawDataReceived(char[] data, int offset, int length)
          {
            System.out.print("RAWLOG: "+new String(data,offset,length));
            // HashMap<String,Object> dataMap = new HashMap<String, Object>(gps_garmon_processor.getGPSData());
            // String Lat = dataMap.get(gps_garmon_processor.LOCATION).toString();
            // System.out.println("Latitude: " + dataMap.toString());
          }
        });

          // Define device to read data from
        GPSDevice gps_device;
        Hashtable<String,Object> environment = new Hashtable<String,Object>();

        if (serial_port_name != null)
        {
            environment.put(GPSSerialDevice.PORT_NAME_KEY, serial_port_name);
        }
        if (serial_port_speed > -1)
        {
        	
            environment.put(GPSSerialDevice.PORT_SPEED_KEY, new Integer(serial_port_speed));
        }

        if(simulation)
        {
            gps_device = new GPSSimulatedNmeaDevice();
        }
        else
        {
            gpsdDevice = new GPSNetworkGpsdDevice();
        }

		environment.put(GPSNetworkGpsdDevice.GPSD_HOST_KEY, "127.0.0.1");
		environment.put(GPSNetworkGpsdDevice.GPSD_PORT_KEY, 2947);


        try
        {
            // set params needed to open device (file,serial, ...):
            gpsdDevice.init(environment);
            // connect device and data processor:
            //gps_data_processor.setGPSDevice(gps_device);
            gps_garmon_processor.setGPSDevice(gpsdDevice);
            // gps_data_processor.close();
           // gps_data_processor.open();
            gps_garmon_processor.open();

            // TODO add writer so that I can write to gpsd so that data 
            // will be returned by gpsd.
            System.out.println("GPSInfo:");
            String[] infos = gps_garmon_processor.getGPSInfo();
            for (int index = 0; index < infos.length; index++)
            {
                System.out.println(infos[index]);
                System.out.println("Latitude: " + gps_garmon_processor.getGPSData().toString());
            }
            String location = gps_garmon_processor.LOCATION;
            
            
            
            
            
            
            // sleep for a while then turn off GPS
            Thread.sleep(15 * 1000);
            System.out.println(gps_garmon_processor.getGPSPosition().getLatitude());
            gps_garmon_processor.close(); // must close otherwise it keeps on running


        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    } //main

}
