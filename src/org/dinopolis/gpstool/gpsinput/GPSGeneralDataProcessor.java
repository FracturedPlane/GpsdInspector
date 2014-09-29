/***********************************************************************
 * @(#)$RCSfile: GPSGeneralDataProcessor.java,v $   $Revision: 1.9 $ $Date: 2006/06/30 08:06:27 $
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



import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

//import org.apache.log4j.Logger;
import org.dinopolis.util.ProgressListener;

//----------------------------------------------------------------------
/**
 * All classes extending this class are interpreting data from
 * a GPSDevice (serial gps-receivier, file containing gps data, ...)
 * and provide this information in a uniform way. So an NMEA-processor
 * interprets NMEA sentences, while a Garmin-Processor understands the
 * garmin protocol.
 * <P>
 * This abstract class adds some basic functionality all
 * GSPDataProcessors might use.
 *
 * @author Christof Dallermassl
 * @version $Revision: 1.9 $
 */

public abstract class GPSGeneralDataProcessor implements GPSDataProcessor
{
/** the GPSDevice */
  protected GPSDevice gps_device_ = null;
/** the map the gps data is stored in */
  protected Map gps_data_ = new HashMap();
/** the lock object for the gps data */
  protected Object gps_data_lock_ = new Object();
/** the gps data change listeners */
  protected PropertyChangeSupport property_change_support_;
/** the raw data listener */
  protected Vector raw_data_listener_;  
/** the progress listener */
  protected Vector progress_listener_;
  //private static Logger logger_ = Logger.getLogger(GPSGeneralDataProcessor.class);

//----------------------------------------------------------------------
/**
 * Starts the data processing. The Data Processor connects to the
 * GPSDevice and starts retrieving information.
 *
 * @exception if an error occured on connecting.
 */
  public abstract void open()
    throws GPSException;
  
//----------------------------------------------------------------------
/**
 * Stopps the data processing. The Data Processor disconnects from the
 * GPSDevice.
 *
 * @exception if an error occured on disconnecting.
 */
  public abstract void close()
    throws GPSException;
  
//----------------------------------------------------------------------
/**
 * Sets the GPSDevice where the data will be retrieved from.
 *
 * @param gps_device the GPSDevice to retrieve data from.
 */
  public void setGPSDevice(GPSDevice gps_device)
  {
    gps_device_ = gps_device;
  }

//----------------------------------------------------------------------
/**
 * Returns the GPSDevice where the data will be retrieved from.
 *
 * @return the GPSDevice where the data will be retrieved from.
 */
  public GPSDevice getGPSDevice()
  {
    return(gps_device_);
  }

//----------------------------------------------------------------------
/**
 * Returns the last received position from the GPSDevice or
 * <code>null</code> if no position was retrieved until now.
 * @return the position from the GPSDevice.
 */

  public abstract GPSPosition getGPSPosition();


//----------------------------------------------------------------------
/**
 * Returns the last received heading (direction) from the GPSDevice or
 * <code>-1.0</code> if no heading was retrieved until now.
 * @return the heading from the GPSDevice.
 */
  public abstract float getHeading();
  
//----------------------------------------------------------------------
  /**
   * Returns information about the gps connected (name of device, type
   * of connection, etc.) This information is for display to the user,
   * not for further processing (may change without notice).
   *
   * @return information about the gps connected.
   */
  public abstract String[] getGPSInfo();

//----------------------------------------------------------------------
/**
 * Returns the last received data from the GPSDevice that is named by
 * the <code>key</code> or <code>null</code> if no data with the given
 * key was retrieved until now. The naming scheme for the keys is
 * taken from the NMEA standard (e.g. GLL for location, HDG for
 * heading, ...)
 *
 * @param key the name of the data.
 * @return the heading from the GPSDevice.
 * @exception IllegalArgumentException if the <code>key</code> is
 * <code>null</code>.
 */
  public Object getGPSData(String key)
    throws IllegalArgumentException
  {
    if (key == null)
      throw new IllegalArgumentException("The key must not be <null>.");
    synchronized(gps_data_lock_)
    {
      return(gps_data_.get(key));
    }
  }

//----------------------------------------------------------------------
/**
 * Returns a map containing the last received data from the GPSDevice
 * or <code>null</code>, if no data was retrieved until now. The
 * naming scheme for the keys is taken from the NMEA standard
 * (e.g. GLL for location, HDG for heading, ...)
 *
 * @return a map containing all key-value pairs of GPS data.  */
  public Map getGPSData()
  {
    synchronized(gps_data_lock_)
    {
      return((Map)((HashMap)gps_data_).clone());
    }
  }

//--------------------------------------------------------------------------------
/**
 * Get a list of waypoints from the gps device.
 * @return a list of <code>GPSWaypoint</code> objects.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSWaypoint
 */
  public List getWaypoints()
    throws UnsupportedOperationException, GPSException
  {
    throw new UnsupportedOperationException("operation not supported by the device/protocol");
  }

//--------------------------------------------------------------------------------
/**
 * Write the waypoints to the gps device.
 * @param waypoints a list of {@link
 * org.dinopolis.gpstool.gpsinput.GPSWaypoint} objects.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSWaypoint
 */
  public void setWaypoints(List waypoints)
    throws UnsupportedOperationException, GPSException
  {
    throw new UnsupportedOperationException("operation not supported by the device/protocol");
  }

//--------------------------------------------------------------------------------
/**
 * Get a list of routes from the gps device.
 * @return a list of <code>GPSRoute</code> objects.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSRoute
 */
  public List getRoutes()
    throws UnsupportedOperationException, GPSException
  {
    System.out.println("GeneralDataProcessor.getRoutes()");
    throw new UnsupportedOperationException("operation not supported by the device/protocol");
  }

//--------------------------------------------------------------------------------
/**
 * Write the routes to the gps device.
 * @param routes a list of {@link
 * org.dinopolis.gpstool.gpsinput.GPSRoute} objects.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSWaypoint
 */
  public void setRoutes(List routes)
    throws UnsupportedOperationException, GPSException
  {
    throw new UnsupportedOperationException("operation not supported by the device/protocol");
  }

//--------------------------------------------------------------------------------
/**
 * Get a list of tracks from the gps device.
 * @return a list of <code>GPSTrack</code> objects.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSTrack
 */
  public List getTracks()
    throws UnsupportedOperationException, GPSException
  {
    throw new UnsupportedOperationException("operation not supported by the device/protocol");
  }

//--------------------------------------------------------------------------------
/**
 * Write the tracks to the gps device.
 * @param tracks a list of {@link
 * org.dinopolis.gpstool.gpsinput.GPSTrack} objects.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSTrack
 */
  public void setTracks(List tracks)
    throws UnsupportedOperationException, GPSException
  {
    throw new UnsupportedOperationException("operation not supported by the device/protocol");
  }

//--------------------------------------------------------------------------------
/**
 * Get a screenshot of the gpsdevice.
 * @return a image of the screen of the gps device.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 */
  public BufferedImage getScreenShot()
    throws UnsupportedOperationException, GPSException
  {
    throw new UnsupportedOperationException("operation not supported by the device/protocol");
  }


//----------------------------------------------------------------------
/**
 * Adds a listener for GPS data change events.
 *
 * @param listener the listener to be added.
 * @param key the key of the GPSdata to be observed.
 * @exception IllegalArgumentException if <code>key</code> or
 * <code>listener</code> is <code>null</code>. 
 */
  public void addGPSDataChangeListener(String key, PropertyChangeListener listener)
    throws IllegalArgumentException
  {
    if (key == null)
      throw new IllegalArgumentException("The key must not be <null>.");
    if (listener == null)
      throw new IllegalArgumentException("The listener must not be <null>.");

    if (property_change_support_ == null)
      property_change_support_ = new PropertyChangeSupport(this);
    property_change_support_.addPropertyChangeListener(key,listener);
  }

  
//----------------------------------------------------------------------
/**
 * Adds a listener for GPS data change events.
 *
 * @param listener the listener to be added.
 * @exception IllegalArgumentException if <code>listener</code> is
 * <code>null</code>.  
 */
  public void addGPSDataChangeListener(PropertyChangeListener listener)
    throws IllegalArgumentException
  {
    if (listener == null)
      throw new IllegalArgumentException("The listener must not be <null>.");

    if (property_change_support_ == null)
      property_change_support_ = new PropertyChangeSupport(this);
    property_change_support_.addPropertyChangeListener(listener);
  }
  
//----------------------------------------------------------------------
/**
 * Removes a listener for GPS data change events.
 *
 * @param listener the listener to be removed.
 * @param key the key of the GPSdata to be observed.
 * @exception IllegalArgumentException if <code>key<code> or
 * <code>listener</code> is <code>null</code>.  
 */
  public void removeGPSDataChangeListener(String key, PropertyChangeListener listener)
    throws IllegalArgumentException
  {
    if (key == null)
      throw new IllegalArgumentException("The key must not be <null>.");
    if (listener == null)
      throw new IllegalArgumentException("The listener must not be <null>.");

    if (property_change_support_ != null)
      property_change_support_.removePropertyChangeListener(key,listener);
  }


//----------------------------------------------------------------------
/**
 * Removes a listener for GPS data change events.
 *
 * @param listener the listener to be removed.
 * @exception IllegalArgumentException if <code>listener</code> is
 * <code>null</code>.  
 */
  public void removeGPSDataChangeListener(PropertyChangeListener listener)
    throws IllegalArgumentException
  {
    if (listener == null)
      throw new IllegalArgumentException("The listener must not be <null>.");

    if (property_change_support_ != null)
      property_change_support_.removePropertyChangeListener(listener);
  }

//----------------------------------------------------------------------
/**
 * Changes the gps data with given key. If there exists a gps data
 * with the given key, the new value replaces the old one. If the
 * value is set to <code>null</code> the gps data is deleted. If there
 * did not exist a gps data with the given key, it will be created.
 *
 * @param key the key of the gps data to be changed.
 * @param value the value of the gps data to be changed, or
 * <code>null</code> to delete the entry.
 * @exception IllegalArgumentException if the key is
 * <code>null</code>.  
 */

  protected void changeGPSData(String key, Object value)
    throws IllegalArgumentException
  {
    if (key == null)
    {
      throw new IllegalArgumentException("The key must not be <null>!");
    }
    Object old_value;
    synchronized(gps_data_lock_)
    {
      old_value = gps_data_.get(key);
      if (value == null)
        gps_data_.remove(key);
      else
        gps_data_.put(key,value);
    }
//    if (logger_.isDebugEnabled())
//      logger_.debug("fire event for key "+key+" oldvalue="+old_value+" new="+value);
    if (property_change_support_ != null)
      property_change_support_.firePropertyChange(key,old_value,value);
  }

//----------------------------------------------------------------------
/**
 * Fire the event for raw data that was received (for loggers etc.)
 *
 * @param raw_data the raw_data
 * @param length the number of characters to use from the raw_data array. 
 */

  protected void fireRawDataReceived(char[] raw_data, int offset, int length)
  {
    if (raw_data_listener_ == null)
      return;
    Iterator listeners;
    synchronized(raw_data_listener_)
    {
      listeners = ((Vector)raw_data_listener_.clone()).iterator();
    }
    while(listeners.hasNext())
    {
      ((GPSRawDataListener)listeners.next()).gpsRawDataReceived(raw_data,offset,length);
    }
  }
  
//----------------------------------------------------------------------
/**
 * Adds a listener for raw GPS data (for loggin purpose or similar).
 *
 * @param listener the listener to be added.
 * @exception IllegalArgumentException if <code>listener</code> is
 * <code>null</code>.  
 */
  public void addGPSRawDataListener(GPSRawDataListener listener)
    throws IllegalArgumentException
  {
    if (listener == null)
      throw new IllegalArgumentException("The listener must not be <null>.");
    if (raw_data_listener_ == null)
      raw_data_listener_ = new Vector();
    synchronized(raw_data_listener_)
    {
      raw_data_listener_.addElement(listener);
    }
  }

//----------------------------------------------------------------------
/**
 * Removes a listener for faw GPS data.
 *
 * @param listener the listener to be removed.
 * @exception IllegalArgumentException if <code>listener</code> is
 * <code>null</code>.  
 */
  public void removeGPSRawDataListener(GPSRawDataListener listener)
    throws IllegalArgumentException
  {
    if (listener == null)
      throw new IllegalArgumentException("The listener must not be <null>.");
    if (raw_data_listener_ == null)
      return;

    synchronized(raw_data_listener_)
    {
      raw_data_listener_.remove(listener);
    }
  }

//----------------------------------------------------------------------
/**
 * Adds a listener for transfer progress (for transfer or
 * route/track/waypoint data).
 *
 * @param listener the listener to be added.
 * @exception IllegalArgumentException if <code>listener</code> is
 * <code>null</code>.
 * @see ProgressListener
 */
  public void addProgressListener(ProgressListener listener)
    throws IllegalArgumentException
  {
    if (listener == null)
      throw new IllegalArgumentException("The listener must not be <null>.");
    if (progress_listener_ == null)
      progress_listener_ = new Vector();
    synchronized(progress_listener_)
    {
      progress_listener_.addElement(listener);
    }
  }

//----------------------------------------------------------------------
/**
 * Removes a listener for transfer progress (for transfer or
 * route/track/waypoint data).
 *
 * @param listener the listener to be added.
 * @exception IllegalArgumentException if <code>listener</code> is
 * <code>null</code>.
 * @see ProgressListener
 */
  public void removeProgressListener(ProgressListener listener)
    throws IllegalArgumentException
  {
    if (listener == null)
      throw new IllegalArgumentException("The listener must not be <null>.");
    if (progress_listener_ == null)
      return;

    synchronized(progress_listener_)
    {
      progress_listener_.remove(listener);
    }
  }

//----------------------------------------------------------------------
/**
 * Fire the ProgressListener actionStart
 *
 * @param action_id the id of the action that is started. This id may
 * be used to display a message for the user.
 * @param min_value the minimum value of the progress counter.
 * @param max_value the maximum value of the progress counter. If the
 * max value is unknown, max_value is set to <code>Integer.NaN</code>.
 */

  protected void fireProgressActionStart(String action_id, int min_value, int max_value)
  {
    if (progress_listener_ == null)
      return;
    Iterator listeners;
    synchronized(progress_listener_)
    {
      listeners = ((Vector)progress_listener_.clone()).iterator();
    }
    while(listeners.hasNext())
    {
      ((ProgressListener)listeners.next()).actionStart(action_id,min_value,max_value);
    }
  }

 //----------------------------------------------------------------------
/**
 * Fire the progressListener actionStart
 *
 * @param action_id the id of the action that is started. This id may
 * be used to display a message for the user.
 * @param current_value the current value
 */

  protected void fireProgressActionProgress(String action_id, int current_value)
  {
    if (progress_listener_ == null)
      return;
    Iterator listeners;
    synchronized(progress_listener_)
    {
      listeners = ((Vector)progress_listener_.clone()).iterator();
    }
    while(listeners.hasNext())
    {
      ((ProgressListener)listeners.next()).actionProgress(action_id,current_value);
    }
  }

//----------------------------------------------------------------------
/**
 * Fire the ProgressListener actionEnd
 *
 * @param action_id the id of the action that is ended. This id may
 * be used to display a message for the user.
 */

  protected void fireProgressActionEnd(String action_id)
  {
    if (progress_listener_ == null)
      return;
    Iterator listeners;
    synchronized(progress_listener_)
    {
      listeners = ((Vector)progress_listener_.clone()).iterator();
    }
    while(listeners.hasNext())
    {
      ((ProgressListener)listeners.next()).actionEnd(action_id);
    }
  }

  
}
