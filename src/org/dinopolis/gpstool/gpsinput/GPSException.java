/***********************************************************************
 * @(#)$RCSfile: GPSException.java,v $   $Revision: 1.2 $ $Date: 2003/04/03 09:01:22 $
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

//----------------------------------------------------------------------
/**
 * @author Christof Dallermassl
 * @version $Revision: 1.2 $
 */

public class GPSException extends Exception 
{
  //----------------------------------------------------------------------
/**
 * Default constructor
 */

  public GPSException()
  {
    super();
  }


//----------------------------------------------------------------------
/**
 * Constructs an Exception with the specified detail message.
 *
 * @param message the message for the exception.
 */

  public GPSException(String message)
  {
    super(message);
  }

//----------------------------------------------------------------------
/**
 * Constructs an Exception with the specified detail message.
 *
 * @param message the message for the exception.
 * @param cause the cause of this exception.
 */

  public GPSException(String message, Throwable cause)
  {
    super(message,cause);
  }

//----------------------------------------------------------------------
/**
 * Default constructor taking an exception to wrap.
 *
 * @param cause the cause of this exception.
 */

  public GPSException(Throwable cause)
  {
    super(cause);
  }

}


