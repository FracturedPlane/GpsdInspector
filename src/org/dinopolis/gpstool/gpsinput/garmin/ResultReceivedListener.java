/***********************************************************************
 * @(#)$RCSfile: ResultReceivedListener.java,v $   $Revision: 1.1 $ $Date: 2003/05/04 21:49:45 $
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
 * Callbackinterface for listeners for the result package of the
 * garmin protocol.
 *
 * @author Christof Dallermassl
 * @version $Revision: 1.1 $
 */

public interface ResultReceivedListener
{
//----------------------------------------------------------------------
/**
 * Called whenever a result package is received.
 * @param result true, if ACK was sent, false if NAK was sent.
 * @param package_id the package_id of the result
 */
  public void receivedResult(boolean result, int package_id);
  
}
