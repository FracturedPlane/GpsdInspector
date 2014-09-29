/***********************************************************************
 * @(#)$RCSfile: GarminRouteD202.java,v $   $Revision: 1.5 $$Date: 2003/12/01 09:34:22 $
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
 * This class represents packets in Garmin data format D202.
 *
 * @author Christof Dallermassl
 * @version $Revision: 1.5 $
 */

public class GarminRouteD202 extends GarminRoute  
{
  public GarminRouteD202(int[] buffer)
  {
    setIdentification(GarminDataConverter.getGarminString(buffer,2));
  }

  public GarminRouteD202(GarminPacket pack)
  {
    setIdentification(pack.getNextAsString(Math.min(pack.getPacketSize(),51)));
  }

  public GarminRouteD202(GPSRoute route)
  {
    setIdentification(route.getIdentification());
  }

//----------------------------------------------------------------------
/**
 * Convert data type to {@link org.dinopolis.gpstool.gpsinput.garmin.GarminPacket}
 * @return GarminPacket representing content of data type.
 */
  public GarminPacket toGarminPacket(int packet_id)
  {
    int data_length = Math.min(getIdentification().length()+1,51);
    GarminPacket pack = new GarminPacket(packet_id,data_length);
    pack.setNextAsString(getIdentification(),data_length,true);
    return (pack);
  }

}
