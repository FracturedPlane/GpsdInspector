/***********************************************************************
 * @(#)$RCSfile: GarminRouteD201.java,v $   $Revision: 1.6 $$Date: 2003/12/01 09:33:53 $
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


package org.dinopolis.gpstool.gpsinput.garmin;

import org.dinopolis.gpstool.gpsinput.GPSRoute;

//----------------------------------------------------------------------
/**
 * This class represents packets in Garmin data format D201.
 *
 * @author Christof Dallermassl
 * @version $Revision: 1.6 $
 */

public class GarminRouteD201 extends GarminRoute  
{
  protected static byte route_id_ = 1;

  public GarminRouteD201(int[] buffer)
  {
    setIdentification(Integer.toString(GarminDataConverter.getGarminByte(buffer,2)));
    setComment(GarminDataConverter.getGarminString(buffer,3,20));
  }

    public GarminRouteD201(GarminPacket pack)
  {
    setIdentification(Integer.toString(pack.getNextAsByte()));
    setComment(pack.getNextAsString(20));
  }

  public GarminRouteD201(GPSRoute route)
  {
    String tmp;

    tmp = route.getIdentification();
    setIdentification(tmp == null ? "" : tmp);
    tmp = route.getComment();
    setComment(tmp == null ? "" : tmp);
  }

//----------------------------------------------------------------------
/**
 * Convert data type to {@link org.dinopolis.gpstool.gpsinput.garmin.GarminPacket}
 * @return GarminPacket representing content of data type.
 */
  public GarminPacket toGarminPacket(int packet_id)
  {
    byte id;
    int data_length = 1 + 20;
    GarminPacket pack = new GarminPacket(packet_id,data_length);

    // Try to parse route identification and get valid route id
    // If parse fails, a default value is generated to avoid errors
    try
    {
      id = java.lang.Byte.parseByte(getIdentification());
			pack.setNextAsByte(id);
			pack.setNextAsString(getComment(),20,false);
    }
    catch (NumberFormatException nfe)
    {
      id = route_id_++;
			pack.setNextAsByte(id);
			pack.setNextAsString(getIdentification(),20,false); // set identificaton as comment
    }

    return (pack);
  }

}
