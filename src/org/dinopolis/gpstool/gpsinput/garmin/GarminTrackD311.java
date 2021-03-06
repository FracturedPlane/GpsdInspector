/***********************************************************************
 * @(#)$RCSfile: GarminTrackD311.java,v $   $Revision: 1.1 $$Date: 2007/03/30 08:50:59 $
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

import org.dinopolis.gpstool.gpsinput.GPSTrack;

//----------------------------------------------------------------------
/**
 * This class represents packets in Garmin data format D311.
 *
 * @author Christof Dallermassl, Stefan Feitl, Massimo Nervi
 * @version $Revision: 1.1 $
 */

public class GarminTrackD311 extends GarminTrack{

//----------------------------------------------------------------------
/*
 * Constructor using an int[].
 *
 * @param buffer the buffer to read the data from.
 */
  public GarminTrackD311(int[] buffer)
  {
    setDisplayed(true);
    int color_index = GarminWaypointD108.DEFAULT_COLOR_INDEX;
    setColor(GarminWaypointD108.COLORS[color_index]);
    setIdentification("Track : "+GarminDataConverter.getGarminWord(buffer,2));
  }

//----------------------------------------------------------------------
/*
 * Constructor using an garmin packet.
 *
 * @param pack the packet to read the data from.
 */
  public GarminTrackD311(GarminPacket pack)
  {
    setDisplayed(true);
    int color_index = GarminWaypointD108.DEFAULT_COLOR_INDEX;
    setColor(GarminWaypointD108.COLORS[color_index]);
     setIdentification("Track : "+pack.getNextAsWord());
  }

//----------------------------------------------------------------------
/*
 * Copy constructor using another track.
 *
 * @param track the track to read the data from.
 */
  public GarminTrackD311(GPSTrack track)
  {
    setDisplayed(track.isDisplayed());
    setColor(track.getColor());
    setIdentification(track.getIdentification());
  }

//----------------------------------------------------------------------
/**
 * Convert data type to {@link org.dinopolis.gpstool.gpsinput.garmin.GarminPacket}.
 *
 * @param packet_id the id to put in the garmin packet.
 * @return GarminPacket representing content of data type.
 */
  public GarminPacket toGarminPacket(int packet_id)
  {
 
  /*  TODO 
      // display (boolean)
        // color (byte)
        // identification + NULL (max 51 char incl. NULL)
    int data_length = 1 + 1 + Math.min(getIdentification().length()+1,51);
    GarminPacket pack = new GarminPacket(packet_id,data_length);

    pack.setNextAsBoolean(isDisplayed());
//    pack.setNextAsByte(getColor());
    boolean color_found = false;
    int color_index = 0;
    while((color_index < GarminWaypointD108.COLORS.length) && (!color_found))
    {
      if(GarminWaypointD108.COLORS[color_index].equals(color_))
        color_found = true;
      else
        color_index++;
    }
    if((color_ == null) || (!color_found))
      pack.setNextAsByte(GarminWaypointD108.DEFAULT_COLOR_INDEX);
    else
      pack.setNextAsByte(color_index);
    pack.setNextAsString(getIdentification(),51,true);
    return (pack);
    */
    return null;
  }
  
}
