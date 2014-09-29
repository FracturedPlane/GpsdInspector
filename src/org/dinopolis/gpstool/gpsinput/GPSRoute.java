/***********************************************************************
 * @(#)$RCSfile: GPSRoute.java,v $   $Revision: 1.8 $$Date: 2003/08/05 14:56:33 $
 *
 * Copyright (c) 2003 IICM, Graz University of Technology
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

import java.awt.Color;
import java.util.List;

//----------------------------------------------------------------------
/**
 * Describes a route of a gps device. A route consists only of a list
 * of {@link org.dinopolis.gpstool.gpsinput.GPSWaypoint} and some
 * information, how these waypoints are connected (not implemented now).
 *
 * @author Christof Dallermassl
 * @version $Revision: 1.8 $
 */

public interface GPSRoute
{

//----------------------------------------------------------------------
/**
 * Get the identification.
 * @return the identification.
 */
  public String getIdentification();

//----------------------------------------------------------------------
/**
 * Set the identification.
 * @param identification The new identification.
 */
  public void setIdentification(String identification);

//----------------------------------------------------------------------
/**
 * Get the comment.
 * @return the comment or an empty string, if no comment was set.
 */
  public String getComment();

//----------------------------------------------------------------------
/**
 * Set the comment.
 * @param comment The comment.
 */
  public void setComment(String comment);

//--------------------------------------------------------------------------------
/**
 * Returns true if this track is displayed on the gps device, false otherwise.
 *
 * @return true if this track is displayed on the gps device, false otherwise.
 */
  public boolean isDisplayed();

//--------------------------------------------------------------------------------
/**
 * Define whether to display the current track or not.
 *
 * @param display if true, display current track.
 */
  public void setDisplayed(boolean display);

//--------------------------------------------------------------------------------
/**
 * Get the color of the current track.
 */
  public Color getColor();

//--------------------------------------------------------------------------------
/**
 * Define the color of the current track. 
 *
 * @param color Color of the current track.
 */
  public void setColor(Color color);

//----------------------------------------------------------------------
/**
 * Get the list of waypoints this route is made of.
 * @return the routepoints.
 */
  public List getWaypoints();

//----------------------------------------------------------------------
/**
 * Set the routepoints.
 *
 * @param routepoints The routepoints.
 */
  public void setWaypoints(List routepoints);

//----------------------------------------------------------------------
/**
 * Add a route point at the end of the list.
 *
 * @param routepoint The routepoint to add.
 */
  public void addWaypoint(GPSWaypoint routepoint);

//----------------------------------------------------------------------
/**
 * Add a route point at the given position.
 *
 * @param position the new position of the routepoint at (0 = add as first point). 
 * @param routepoint The routepoint to add.
 */
  public void addWaypoint(int position, GPSWaypoint routepoint);

//----------------------------------------------------------------------
/**
 * Return the routepoint at the given position.
 *
 * @return the routepoint.
 *
 * @throws IndexOutofBoundsException if the index is out of range
 * (index < 0 || index >= size()).
 */
  public GPSWaypoint getWaypoint(int position)
    throws IndexOutOfBoundsException;

//----------------------------------------------------------------------
/**
 * Remove the routepoint from the given position.
 *
 * @throws IndexOutofBoundsException if the index is out of range
 * (index < 0 || index >= size()).
 */
  public void removeWaypoint(int position)
    throws IndexOutOfBoundsException;

//----------------------------------------------------------------------
/**
 * Clears the route (routepoints, identification, comment, etc.).
 */
  public void clear();

//----------------------------------------------------------------------
/**
 * Returns the number of waypoints in this route.
 *
 * @return the number of waypoints in this route.
 */
  public int size();

//----------------------------------------------------------------------
/**
 * Returns the minimum latitude (furthest south) covered by this route.
 *
 * @return the minimum latitude covered by this route.
 */
  public double getMinLatitude();

//----------------------------------------------------------------------
/**
 * Returns the maximum latitude (furthest north) covered by this route.
 *
 * @return the maximum latitude covered by this route.
 */
  public double getMaxLatitude();

//----------------------------------------------------------------------
/**
 * Returns the minimum longitude (furthest west) covered by this route.
 *
 * @return the minimum longitude covered by this route.
 */
  public double getMinLongitude();

//----------------------------------------------------------------------
/**
 * Returns the maximum longitude (furthest east) covered by this route.
 *
 * @return the maximum longitude covered by this route.
 */
  public double getMaxLongitude();

//----------------------------------------------------------------------
/**
 * Returns the minimum altitude covered by this route.
 *
 * @return the minimum altitude covered by this route.
 */
  public double getMinAltitude();

//----------------------------------------------------------------------
/**
 * Returns the maximum altitude covered by this route.
 *
 * @return the maximum altitude covered by this route.
 */
  public double getMaxAltitude();


}


