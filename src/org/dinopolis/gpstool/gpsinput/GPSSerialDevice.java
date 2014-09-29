/***********************************************************************
 * @(#)$RCSfile: GPSSerialDevice.java,v $   $Revision: 1.5 $ $Date: 2006/01/24 09:20:48 $
 *
 * Copyright (c) 2001 IICM, Graz University of Technology
 * Inffeldgasse 16c, A-8010 Graz, Austria.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL)
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 ***********************************************************************/


package org.dinopolis.gpstool.gpsinput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

//import org.apache.log4j.Logger;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.NoSuchPortException;

//----------------------------------------------------------------------
/**
 * This class connects to a gps-receiver by the serial connection.
 *
 * @author Christof Dallermassl
 * @version $Revision: 1.5 $
 */

public class GPSSerialDevice implements GPSDevice
{
  public final static String PORT_NAME_KEY = "port_name";
  public final static String PORT_SPEED_KEY = "port_speed";
  public final static String RAW_DATA_LOG_FILENAME_KEY = "data_logfile";
  
  protected final static String DEFAULT_PORT_NAME_LINUX = "/dev/ttyS1";
  protected final static String DEFAULT_PORT_NAME_WIN = "COM1";
  protected final static int DEFAULT_PORT_SPEED = 4800;

  protected SerialPort serial_port_;
  String serial_port_name_;
  int serial_port_speed_;
  //private static Logger logger_ = Logger.getLogger(GPSSerialDevice.class);

//----------------------------------------------------------------------
/**
 * Initialize the GPSDevice and hand over all information needed for
 * the specific GPSDevice to opens the connection.
 *
 * @param environment contains all informations needed to initialize
 * the gps device.  
 * @exception GPSException if the initialization was not successfull,
 * e.g. some information in the environment is missing.
 */
  
  public void init(Hashtable environment)
    throws GPSException
  {

    try
    {
      if ((serial_port_name_ = (String)environment.get(PORT_NAME_KEY)) == null)
      {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
          serial_port_name_ = DEFAULT_PORT_NAME_WIN;
        else
          serial_port_name_ = DEFAULT_PORT_NAME_LINUX;
      }
      
      if (environment.containsKey(PORT_SPEED_KEY))
        serial_port_speed_ = ((Integer)environment.get(PORT_SPEED_KEY)).intValue();
      else
        serial_port_speed_ = DEFAULT_PORT_SPEED;

    }
    catch(Exception e)
    {
      throw new GPSException("Invalid environment set for serial connection: "+e.getMessage());
    }
  }
    
//----------------------------------------------------------------------
/**
 * Opens the gps device (e.g. serial connection to gps-receiver or
 * file containing logging information from a gps-receiver). 
 * @exception GPSException if the opening of the device was not successfull.
 */

  public void open()
    throws GPSException
  {

    try
    {
//      if (logger_.isDebugEnabled())
//        logger_.debug("try to connect to port '"
//                      + serial_port_name_ + "' using "+serial_port_speed_+" baud");
      CommPortIdentifier port_id = CommPortIdentifier.getPortIdentifier(serial_port_name_);
      serial_port_ = (SerialPort)port_id.open("GpsTool",2000);
//      if (logger_.isDebugEnabled())
//        logger_.debug("setting speed and serial params to 8,N,1");
      serial_port_.setSerialPortParams(serial_port_speed_,
                                     SerialPort.DATABITS_8,
                                     SerialPort.STOPBITS_1,
                                     SerialPort.PARITY_NONE);
    }
    catch(NoSuchPortException e)
    {
//      e.printStackTrace();
      throw new GPSException("port '"+serial_port_name_+"' not available.");
    }
    catch(PortInUseException e)
    {
      throw new GPSException("port '" + serial_port_name_ + "' is in use by another application (" +
                         e.currentOwner + ")");
    }
    catch (UnsupportedCommOperationException e)
    {
      e.printStackTrace();
      throw new GPSException(e.getMessage());
    }
  }
  
//----------------------------------------------------------------------
/**
 * Closes the connection to the GPSDevice.
 * @exception GPSException if closing the device was not successfull.
 */

  public void close()
    throws GPSException
  {
    if (serial_port_ != null)
      serial_port_.close();
  }
    
//----------------------------------------------------------------------
/**
 * Returns an input stream from the gps device. If the port is
 * unidirectional and doesn't support receiving data or
 * <code>open</code> was not called before, then getInputStream
 * returns <code>null</code>.
 * @return an input stream from the gps device.
 */

  public InputStream getInputStream()
    throws IOException
  {
    if (serial_port_ != null)
      return(serial_port_.getInputStream());
    else
      return(null);
  }
  
//----------------------------------------------------------------------
/**
 * Returns an output stream from the gps device. If the port is
 * unidirectional and doesn't support receiving data or
 * <code>open</code> was not called before, then getOutputStream
 * returns <code>null</code>.
 * @return an output stream from the gps device.
 */

  public OutputStream getOutputStream()
    throws IOException
  {
    if (serial_port_ != null)
      return(serial_port_.getOutputStream());
    else
      return(null);
  }

//----------------------------------------------------------------------
/**
 * Sets the speed for the serial port.
 * @param speed the speed to set (e.g. 4800, 9600, 19200, 38400, ...)
 */
  public void setSerialPortSpeed(int speed)
    throws IOException
  {
    try
    {
      serial_port_.setSerialPortParams(speed,
                                       SerialPort.DATABITS_8,
                                       SerialPort.STOPBITS_1,
                                       SerialPort.PARITY_NONE);
    }
    catch(UnsupportedCommOperationException e)
    {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
  }
}




