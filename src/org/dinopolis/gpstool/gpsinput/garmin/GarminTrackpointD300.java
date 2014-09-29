/***********************************************************************
 * @(#)$RCSfile: GarminTrackpointD300.java,v $   $Revision: 1.8 $$Date: 2003/12/01 09:36:03 $
 *
 * Copyright (c) 2001-2003 Sandra Brueckler, Stefan Feitl
 * Written during an XPG-Project at the IICM of the TU-Graz, Austria
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


package org.dinopolis.gpstool.gpsinput.garmin;

import org.dinopolis.gpstool.gpsinput.GPSTrackpoint;


//----------------------------------------------------------------------
/**
 * This class represents packets in Garmin data format D300.
 *
 * @author Sandra Brueckler, Stefan Feitl
 * @version $Revision: 1.8 $
 */

public class GarminTrackpointD300 implements GarminTrackpoint
{
  public double latitude_;
  public double longitude_;
  public long time_;
  public boolean new_track_;

  protected final static int TRACKPOINT_TYPE = 300;
  
  public GarminTrackpointD300()
  {
  }

  public GarminTrackpointD300(int[] buffer)
  {
    latitude_ = GarminDataConverter.getGarminSemicircleDegrees(buffer,2);
    longitude_ = GarminDataConverter.getGarminSemicircleDegrees(buffer,6);
    time_ = GarminDataConverter.getGarminLongWord(buffer,10);
    new_track_ = GarminDataConverter.getGarminBoolean(buffer,14);
  }

  public GarminTrackpointD300(GarminPacket pack)
  {
    latitude_ = pack.getNextAsSemicircleDegrees();
    longitude_ = pack.getNextAsSemicircleDegrees();
    time_ = pack.getNextAsLongWord();
    new_track_ = pack.getNextAsBoolean();
  }

  public GarminTrackpointD300(GPSTrackpoint trackpoint)
  {
    latitude_ = trackpoint.getLatitude();  
    longitude_ = trackpoint.getLongitude();
    time_ = GarminDataConverter.convertDateToGarminTime(trackpoint.getDate());
    new_track_ = trackpoint.isNewTrack();
  }

//----------------------------------------------------------------------
/**
 * Convert data type to {@link org.dinopolis.gpstool.gpsinput.garmin.GarminPacket}
 * @return GarminPacket representing content of data type.
 */
  public GarminPacket toGarminPacket(int packet_id)
  {
    int data_length = 4 + 4 + 4 + 1;
    GarminPacket pack = new GarminPacket(packet_id,data_length);
    int[] data = new int[data_length];

    data = GarminDataConverter.setGarminSemicircleDegrees(latitude_,data,0);
    data = GarminDataConverter.setGarminSemicircleDegrees(longitude_,data,4);
    data = GarminDataConverter.setGarminLongWord(time_,data,8);
    data = GarminDataConverter.setGarminBoolean(new_track_,data,12);
    pack.put(data);

    return (pack);
  }

//----------------------------------------------------------------------
/**
 * Get the Trackpoint Type
 *
 * @return Trackpoint Type
 * @throws GarminUnsupportedMethodException
 */
  public int getType() throws GarminUnsupportedMethodException
  {
    return(TRACKPOINT_TYPE);
  }

//----------------------------------------------------------------------
/**
 * Get the Latitude (degrees)
 *
 * @return Latitude (degrees)
 * @throws GarminUnsupportedMethodException
 */
  public double getLatitude() throws GarminUnsupportedMethodException
  {
    return(latitude_);
  }

//----------------------------------------------------------------------
/**
 * Set the Latitude (degrees)
 *
 * @throws GarminUnsupportedMethodException
 */
  public void setLatitude(double latitude) throws GarminUnsupportedMethodException
  {
    latitude_ = latitude;
  }

//----------------------------------------------------------------------
/**
 * Get the Longitude (degrees)
 *
 * @return Longitude (degrees)
 * @throws GarminUnsupportedMethodException
 */
  public double getLongitude() throws GarminUnsupportedMethodException
  {
    return(longitude_);
  }

//----------------------------------------------------------------------
/**
 * Set the Longitude (degrees)
 *
 * @throws GarminUnsupportedMethodException
 */
  public void setLongitude(double longitude) throws GarminUnsupportedMethodException
  {
    longitude_ = longitude;
  }

//----------------------------------------------------------------------
/**
 * Get the time when the point was recorded. Is expressed as number of
 * seconds since UTC Dec 31, 1989 - 12:00 AM.
 *
 * @return Time (seconds)
 * @throws GarminUnsupportedMethodException
 */
 public long getTime() throws GarminUnsupportedMethodException
 {
   return(time_);
 }
	
//----------------------------------------------------------------------
/**
 * Set the time when the point was recorded. Is expressed as number of
 * seconds since UTC Dec 31, 1989 - 12:00 AM.
 *
 * @throws GarminUnsupportedMethodException
 */
 public void setTime(long time) throws GarminUnsupportedMethodException
 {
   time_ = time;
 }
	
//----------------------------------------------------------------------
/**
 * Get the Altitude (meters).  A value of 1.0e25 means the parameter is
 * unsupported or unknown.
 *
 * @return Altitude (meters)
 * @throws GarminUnsupportedMethodException
 */
  public float getAltitude() throws GarminUnsupportedMethodException
  {
    return(1.0E25F);
  }

//----------------------------------------------------------------------
/**
 * Set the Altitude (meters).  A value of 1.0e25 means the parameter is
 * unsupported or unknown.
 *
 * @throws GarminUnsupportedMethodException
 */
  public void setAltitude(float altitude) throws GarminUnsupportedMethodException
  {
    throw new GarminUnsupportedMethodException("Trackpoint D300 does not support altitude.");
  }

//----------------------------------------------------------------------
/**
 * Get the Depth (meters). A value of 1.0e25 means the parameter is
 * unsupported or unknown.
 *
 * @return Depth (meters)
 * @throws GarminUnsupportedMethodException
 */
  public float getDepth() throws GarminUnsupportedMethodException
  {
    return(1.0E25F);
  }
  
//----------------------------------------------------------------------
/**
 * Set the Depth (meters). A value of 1.0e25 means the parameter is
 * unsupported or unknown.
 *
 * @throws GarminUnsupportedMethodException
 */
  public void setDepth(float depth) throws GarminUnsupportedMethodException
  {
    throw new GarminUnsupportedMethodException("Trackpoint D300 does not support depth.");
  }
  
//----------------------------------------------------------------------
/**
 * Is this the beginning of a new track segment? If true, this point
 * marks the beginning of a new track segment.
 *
 * @return Beginning of new track segment (boolean)
 * @throws GarminUnsupportedMethodException
 */
  public boolean isNewTrack() throws GarminUnsupportedMethodException
  {
    return(new_track_);
  }

//----------------------------------------------------------------------
/**
 * Is this the beginning of a new track segment? If true, this point
 * marks the beginning of a new track segment.
 *
 * @throws GarminUnsupportedMethodException
 */
  public void setNewTrack(boolean new_track) throws GarminUnsupportedMethodException
  {
   new_track_ = new_track;
  }

  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("GarminTrackpoint[");
    buffer.append("lat=").append(latitude_).append(", ");
    buffer.append("lon=").append(longitude_).append(", ");
    buffer.append("time=").append(time_).append(", ");
    buffer.append("new_track=").append(new_track_);
    buffer.append("]");
    return(buffer.toString());
  }
}
