/*
 * XYData Item for GPS plots - includes a label and past data
 */
/*
    This file is part of GpsInspector.

    GpsInspector is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GpsInspector is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GpsInspector.  If not, see <http://www.gnu.org/licenses/>.

 */

/*
 *  Copyright(C) 2009 Shawn Gano shawn@gano.name
*/

package name.gano.jfreechart;

import org.jfree.data.xy.XYDataItem;

/**
 *
 * @author Shawn Gano, 18 April 2009
 */
public class XYGPSDataItem extends XYDataItem
{
    // static ints indecating what type of signal received, zero signal, tracking but not used in fix, and trancking and used in fix
    public static int NOT_TRACKING = 0;
    public static int NOT_USED_FIX = 1;
    public static int USED_FIX = 2;

    private String label = "";
    private int id = -1;
    private int signalType = USED_FIX;

    public XYGPSDataItem(double x, double y)
    {
        super(x,y);
    }

    public XYGPSDataItem(double x, double y, int id, String label)
    {
        super(x,y);
        this.setId(id);
        this.setLabel(label);

    }

    public XYGPSDataItem(double x, double y, int id, String label, int signalType)
    {
        super(x,y);
        this.setId(id);
        this.setLabel(label);
        this.setSignalType(signalType);
    }

    public XYGPSDataItem(java.lang.Number x, java.lang.Number y)
    {
        super(x,y);
    }

    // ======== Get and Set methods ================

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the signalType
     */
    public int getSignalType()
    {
        return signalType;
    }

    /**
     * @param signalType the signalType to set
     */
    public void setSignalType(int signalType)
    {
        this.signalType = signalType;
    }
    

} // XYGPSDataItem
