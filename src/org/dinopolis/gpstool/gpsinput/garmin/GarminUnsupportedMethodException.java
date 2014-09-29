/***********************************************************************
 * @(#)$RCSfile: GarminUnsupportedMethodException.java,v $   $Revision: 1.1 $$Date: 2003/03/21 16:59:05 $
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

//----------------------------------------------------------------------
/**
 * @author Christof Dallermassl
 * @version $Revision: 1.1 $
 */

public class GarminUnsupportedMethodException extends RuntimeException
{
//----------------------------------------------------------------------
/**
 * Empty Constructor
 */
  public GarminUnsupportedMethodException()
  {
    super();
  }
//----------------------------------------------------------------------
/**
 * Constructor taking a message.
 *
 * @param message the message of the exception
 */
  public GarminUnsupportedMethodException(String message)
  {
    super(message);
  }
}


