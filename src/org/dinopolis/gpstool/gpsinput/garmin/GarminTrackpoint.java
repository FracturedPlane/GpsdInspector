/***********************************************************************
 * @(#)$RCSfile: GarminTrackpoint.java,v $   $Revision: 1.4 $$Date: 2003/08/05 14:55:53 $
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

//----------------------------------------------------------------------
/**
 * Interface representing the basic functions of Garmin trackpoints.
 *
 * @author Sandra Brueckler, Stefan Feitl
 * @version $Revision: 1.4 $
 */

public interface GarminTrackpoint
{
//----------------------------------------------------------------------
/**
 * Get the Trackpoint Type
 *
 * @return Trackpoint Type
 * @throws GarminUnsupportedMethodException
 */
 public int getType() throws GarminUnsupportedMethodException;

//----------------------------------------------------------------------
/**
 * Get the Latitude (degrees)
 *
 * @return Latitude (degrees)
 * @throws GarminUnsupportedMethodException
 */
 public double getLatitude() throws GarminUnsupportedMethodException;
	
//----------------------------------------------------------------------
/**
 * Set the Latitude (degrees)
 *
 * @throws GarminUnsupportedMethodException
 */
 public void setLatitude(double latitude) throws GarminUnsupportedMethodException;
	
//----------------------------------------------------------------------
/**
 * Get the Longitude (degrees)
 *
 * @return Longitude (degrees)
 * @throws GarminUnsupportedMethodException
 */
 public double getLongitude() throws GarminUnsupportedMethodException;

//----------------------------------------------------------------------
/**
 * Set the Longitude (degrees)
 *
 * @throws GarminUnsupportedMethodException
 */
 public void setLongitude(double longitude) throws GarminUnsupportedMethodException;

//----------------------------------------------------------------------
/**
 * Get the time when the point was recorded. Is expressed as number of
 * seconds since UTC Dec 31, 1989 - 12:00 AM.
 *
 * @return Time (seconds)
 * @throws GarminUnsupportedMethodException
 */
 public long getTime() throws GarminUnsupportedMethodException;
	
//----------------------------------------------------------------------
/**
 * Set the time when the point was recorded. Is expressed as number of
 * seconds since UTC Dec 31, 1989 - 12:00 AM.
 *
 * @throws GarminUnsupportedMethodException
 */
 public void setTime(long time) throws GarminUnsupportedMethodException;
	
//----------------------------------------------------------------------
/**
 * Get the Altitude (metres). A value of 1.0e25 means the parameter
 * is unsupported or unknown.
 *
 * @return Altitude (meters)
 * @throws GarminUnsupportedMethodException
 */
 public float getAltitude() throws GarminUnsupportedMethodException;
	
//----------------------------------------------------------------------
/**
 * Set the Altitude (metres). A value of 1.0e25 means the parameter
 * is unsupported or unknown.
 *
 * @throws GarminUnsupportedMethodException
 */
 public void setAltitude(float altitude) throws GarminUnsupportedMethodException;
	
//----------------------------------------------------------------------
/**
 * Get the Depth (metres). A value of 1.0e25 means the parameter is
 * unsupported or unknown.
 *
 * @return Depth (meters)
 * @throws GarminUnsupportedMethodException
 */
 public float getDepth() throws GarminUnsupportedMethodException;
	
//----------------------------------------------------------------------
/**
 * Set the Depth (metres). A value of 1.0e25 means the parameter is
 * unsupported or unknown.
 *
 * @throws GarminUnsupportedMethodException
 */
 public void setDepth(float depth) throws GarminUnsupportedMethodException;
	
//----------------------------------------------------------------------
/**
 * Is this the beginning of a new track segment? If true, this point
 * marks the beginning of a new track segment.
 *
 * @return Beginning of new track segment (boolean)
 * @throws GarminUnsupportedMethodException
 */
 public boolean isNewTrack() throws GarminUnsupportedMethodException;

//----------------------------------------------------------------------
/**
 * Is this the beginning of a new track segment? If true, this point
 * marks the beginning of a new track segment.
 *
  * @throws GarminUnsupportedMethodException
 */
 public void setNewTrack(boolean new_track) throws GarminUnsupportedMethodException;
}
