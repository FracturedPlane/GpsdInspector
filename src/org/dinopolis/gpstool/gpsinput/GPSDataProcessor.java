/***********************************************************************
 * @(#)$RCSfile: GPSDataProcessor.java,v $   $Revision: 1.13 $ $Date: 2007/04/11 07:51:37 $
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
import java.util.List;
import java.util.Map;
import org.dinopolis.util.ProgressListener;

//----------------------------------------------------------------------
/**
 * All classes implementing this interface are interpreting data from
 * a GPSDevice (serial gps-receiver, file containing gps data, ...)
 * and provide this information in a uniform way. So an NMEA-processor
 * interprets NMEA sentences, while a Garmin-Processor understands the
 * garmin protocol.
 * <p>
 * Other classes may register as a GPSDataListener and receive gps
 * events. The following events are supported (if provided by the gps device):
 * <ul>
 * <li><code>LOCATION</code>: the value is a {@link
 * org.dinopolis.gpstool.gpsinput.GPSPosition} object</li>
 * <li><code>HEADING</code>: the value is a Float</li>
 * <li><code>SPEED</code>: the value is a Float and is in kilometers per hour</li>
 * <li><code>NUMBER_SATELLITES</code>: the value is a Integer</li>
 * <li><code>ALTITUDE</code>: the value is a Float and is in meters</li>
 * <li><code>SATELLITE_INFO</code>: the value is a {@link org.dinopolis.gpstool.gpsinput.SatelliteInfo} object.</li>
 * <li><code>DEPTH</code>: the value is a Float and is in meters.</li>
 * <li><code>EPE</code>: estimated position error, the value is a GPSPositionError object.</li>
 * <li><code>IDS_SATELLITES</code>: An array of Integers holding the number of visible satellites.</li>
 * <li><code>PDOP</code>: a Float indicating the quality of the gps signal.</li>
 * <li><code>HDOP</code>: a Float indicating the quality of the gps signal in horizontal direction.</li>
 * <li><code>VDOP</code>: a Float indicating the quality of the gps signal in vertical direction.</li>
 * <ul>
 * <p>
 
 * Other classes may register as {@link
 * ProgressListener} to be informed about
 * progress in up/downloading routes/tracks/waypoints. The following
 * actions are supported:
 * <ul>
 * <li>GETROUTES</li>
 * <li>SETROUTES</li>
 * <li>GETTRACKS</li>
 * <li>SETTRACKS</li>
 * <li>GETWAYPOINTS</li>
 * <li>SETWAYPOINTS</li>
 * <li>GETSCREENSHOT</li>
 * </ul>
 *
 * @author Christof Dallermassl
 * @version $Revision: 1.13 $ */

public interface GPSDataProcessor
{

  public final static String LOCATION = "location";
  public final static String HEADING = "heading";
  public final static String SPEED = "speed";
  public final static String NUMBER_SATELLITES = "number_satellites";
  // PHILIPPE START
  public final static String IDS_SATELLITES = "ids_satellites";
  public final static String PDOP = "position_dop";
  public final static String HDOP = "horizontal_dop";
  public final static String VDOP = "vertical_dop";
  // PHILIPPE STOP
  public final static String ALTITUDE = "altitude";
  public final static String SATELLITE_INFO = "satellite_info";
  public final static String DEPTH = "depth";
  public final static String EPE = "epe";

  // Shawn Gano
  public final static String FIXTIME = "Time_of_fix";
  public final static String FIX_INFO = "Fix_Information";
  public final static String FIX_QUALITY = "Fix_Quality";


  public final static String GETROUTES = "getroutes";
  public final static String SETROUTES = "setroutes";
  public final static String GETTRACKS = "gettracks";
  public final static String SETTRACKS = "settracks";
  public final static String GETWAYPOINTS = "getwaypoints";
  public final static String SETWAYPOINTS = "setwaypoints";
  public final static String GETSCREENSHOT = "getscreenshot";
  
  public final static float KM2NAUTIC = 0.539956803f;
  
//----------------------------------------------------------------------
/**
 * Starts the data processing. The Data Processor connects to the
 * GPSDevice and starts retrieving information.
 *
 * @exception if an error occured on connecting.
 */
  public void open()
    throws GPSException;
  
//----------------------------------------------------------------------
/**
 * Stopps the data processing. The Data Processor disconnects from the
 * GPSDevice.
 *
 * @exception if an error occured on disconnecting.
 */
  public void close()
    throws GPSException;
  
//----------------------------------------------------------------------
/**
 * Sets the GPSDevice where the data will be retrieved from.
 *
 * @param gps_device the GPSDevice to retrieve data from.
 */
  public void setGPSDevice(GPSDevice gps_device);

//----------------------------------------------------------------------
/**
 * Returns the GPSDevice where the data will be retrieved from.
 *
 * @return the GPSDevice where the data will be retrieved from.
 */
  public GPSDevice getGPSDevice();

//----------------------------------------------------------------------
/**
 * Returns information about the gps connected (name of device, type
 * of connection, etc.) This information is for display to the user,
 * not for further processing (may change without notice).
 *
 * @return information about the gps connected.
 */
  public String[] getGPSInfo()
		throws GPSException;

  
//----------------------------------------------------------------------
/**
 * Returns the last received position from the GPSDevice or
 * <code>null</code> if no position was retrieved until now.
 * @return the position from the GPSDevice.
 */

  public GPSPosition getGPSPosition();


//----------------------------------------------------------------------
/**
 * Returns the last received heading (direction) from the GPSDevice or
 * <code>-1.0</code> if no heading was retrieved until now.
 * @return the heading from the GPSDevice.
 */
  public float getHeading();


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
    throws IllegalArgumentException;

//----------------------------------------------------------------------
/**
 * Returns a map containing the last received data from the GPSDevice
 * or <code>null</code>, if no data was retrieved until now. The
 * naming scheme for the keys is taken from the NMEA standard
 * (e.g. GLL for location, HDG for heading, ...)
 *
 * @return a map containing all key-value pairs of GPS data.  */
  public Map getGPSData();


//--------------------------------------------------------------------------------
/**
 * Get a list of waypoints from the gps device.
 * @return a list of {@link GPSWaypoint} objects.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSWaypoint
 */
  public List getWaypoints()
    throws UnsupportedOperationException, GPSException;

//--------------------------------------------------------------------------------
/**
 * Write the waypoints to the gps device.
 * @param waypoints The new waypoints.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSWaypoint
 */
  public void setWaypoints(List waypoints)
    throws UnsupportedOperationException, GPSException;

//--------------------------------------------------------------------------------
/**
 * Get a list of routes from the gps device.
 * @return a list of {@link GPSRoute} objects.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSRoute
 */
  public List getRoutes()
    throws UnsupportedOperationException, GPSException;

//--------------------------------------------------------------------------------
/**
 * Write the routes to the gps device.
 * @param routes The new routes.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSRoute
 */
  public void setRoutes(List routes)
    throws UnsupportedOperationException, GPSException;

//--------------------------------------------------------------------------------
/**
 * Get a list of tracks from the gps device.
 * @return a list of {@link GPSTrack} objects.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSTrack
 */
  public List getTracks()
    throws UnsupportedOperationException, GPSException;

//--------------------------------------------------------------------------------
/**
 * Write the tracks to the gps device.
 * @param tracks The new tracks.
 *
 * @throws UnsupportedOperationException if the operation is not
 * supported by the gps device or by the protocol used.
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 * @see GPSTrack
 */
  public void setTracks(List tracks)
    throws UnsupportedOperationException, GPSException;


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
    throws UnsupportedOperationException, GPSException;

//----------------------------------------------------------------------
/**
 * Requests the gps device to send the current
 * position/heading/etc. periodically.
 *
 * @param period time in milliseconds between periodically sending
 * position/heading/etc. This value may be changed by the gps device,
 * so do not rely on the value given!
 * @return the period chosen by the gps device or 0 if the gps device
 * is unable to send periodically. Do not rely on this value as some
 * drivers just do not know!
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 */
  public long startSendPositionPeriodically(long period)
    throws GPSException;

//----------------------------------------------------------------------
/**
 * Requests the gps device to stop to send the current
 * position/heading/etc. periodically. Do not rely on this, as some
 * gps devices may not stop it (e.g. NMEA).
 * @throws GPSException if the operation threw an exception
 * (e.g. communication problem).
 */
  public void stopSendPositionPeriodically()
    throws GPSException;
  
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
    throws IllegalArgumentException;

//----------------------------------------------------------------------
/**
 * Adds a listener for GPS data change events.
 *
 * @param listener the listener to be added.
 * @exception IllegalArgumentException if <code>listener</code> is
 * <code>null</code>.  
 */
  public void addGPSDataChangeListener(PropertyChangeListener listener)
    throws IllegalArgumentException;

  
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
    throws IllegalArgumentException;


//----------------------------------------------------------------------
/**
 * Adds a listener for raw GPS data (for loggin purpose or similar).
 *
 * @param listener the listener to be added.
 * @exception IllegalArgumentException if <code>listener</code> is
 * <code>null</code>.  
 */
  public void addGPSRawDataListener(GPSRawDataListener listener)
    throws IllegalArgumentException;

//----------------------------------------------------------------------
/**
 * Removes a listener for faw GPS data.
 *
 * @param listener the listener to be removed.
 * @exception IllegalArgumentException if <code>listener</code> is
 * <code>null</code>.  
 */
  public void removeGPSRawDataListener(GPSRawDataListener listener)
    throws IllegalArgumentException;

//----------------------------------------------------------------------
/**
 * Removes a listener for GPS data change events.
 *
 * @param listener the listener to be removed.
 * @exception IllegalArgumentException if <code>listener</code> is
 * <code>null</code>.  
 */
  public void removeGPSDataChangeListener(PropertyChangeListener listener)
    throws IllegalArgumentException;

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
    throws IllegalArgumentException;

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
    throws IllegalArgumentException;



}


