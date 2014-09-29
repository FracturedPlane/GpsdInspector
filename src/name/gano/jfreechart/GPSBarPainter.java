/*
 * GPSBarPainter.java
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.RectangularShape;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author Shawn Gano
 */
public class GPSBarPainter extends StandardBarPainter
{
    //private Color barColor = Color.YELLOW;
    private Paint itemPaint = Color.YELLOW; //barColor;
    private Stroke outlineStroke = new BasicStroke(2.0f);

    private int[] signalType = new int[12];
   
    /**
     * Paints a single bar instance.
     *
     * @param g2  the graphics target.
     * @param renderer  the renderer.
     * @param row  the row index.
     * @param column  the column index.
     * @param bar  the bar
     * @param base  indicates which side of the rectangle is the base of the
     *              bar.
     */
    @Override
    public void paintBar(Graphics2D g2, BarRenderer renderer, int row,
            int column, RectangularShape bar, RectangleEdge base)
    {

        //Paint itemPaint = renderer.getItemPaint(row, column);
        
        
        GradientPaintTransformer t = renderer.getGradientPaintTransformer();
        if (t != null && itemPaint instanceof GradientPaint) {
            itemPaint = t.transform((GradientPaint) itemPaint, bar);
        }
        g2.setPaint(itemPaint);

        if(signalType[column] != XYGPSDataItem.NOT_USED_FIX)
        {
            g2.fill(bar);
        }
        else
        {
        // draw the outline...
        //if (renderer.isDrawBarOutline())
        //{
               // && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
            //Stroke stroke = renderer.getItemOutlineStroke(row, column);
            //Paint paint = renderer.getItemOutlinePaint(row, column);
            //if (stroke != null && paint != null)
            //{
                //g2.setPaint(Color.BLACK); // to match background
                //g2.fill(bar);

                g2.setPaint(itemPaint);
                g2.setStroke(outlineStroke);
                //g2.setPaint(paint);
                g2.draw(bar);
            //}
        //}
        }

    } // paintBar

    /**
     * @return the barColor
     */
    public Color getBarColor()
    {
        //return barColor;
        return (Color)itemPaint;
    }

    /**
     * @param barColor the barColor to set
     */
    public void setBarColor(Color barColor)
    {
        //this.barColor = barColor;
        this.itemPaint = barColor;
    }

    /**
     * @param satIndex ordered index number (bar order)
     * @return the signalType
     */
    public int getSignalType(int satIndex)
    {
        return signalType[satIndex];
    }

    /**
     * @param signalType the signalType to set
     */
    public void setSignalType(int satIndex, int signalType)
    {
        this.signalType[satIndex] = signalType;
    }


} //GPSBarPainter
