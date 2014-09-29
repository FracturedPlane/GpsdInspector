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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author sgano
 */
public class SctterPlotItemGPSRenderer extends ScatterPolarItemRenderer
{

    private Color labelColor = Color.BLACK;
    private Color labelColorUnFilled = Color.YELLOW;
    
    public SctterPlotItemGPSRenderer()
    {
        this.setDefaultSize(18); // change default size
    }


     /**
     * Plots the data for a given series. - modified to draw points only not connected, add label
     *
     * @param g2  the drawing surface.
     * @param dataArea  the data area.
     * @param info  collects plot rendering info.
     * @param plot  the plot.
     * @param dataset  the dataset.
     * @param seriesIndex  the series index.
     */
    public void drawSeries(Graphics2D g2,
                           Rectangle2D dataArea,
                           PlotRenderingInfo info,
                           PolarPlot plot,
                           XYDataset dataset,
                           int seriesIndex)
    {

        Polygon poly = new Polygon();
        int numPoints = dataset.getItemCount(seriesIndex);

        g2.setPaint(lookupSeriesPaint(seriesIndex));
        g2.setStroke(lookupSeriesStroke(seriesIndex));

        for (int i = 0; i < numPoints; i++)
        {
            double theta = dataset.getXValue(seriesIndex, i);
            double radius = dataset.getYValue(seriesIndex, i);
            Point p = plot.translateValueThetaRadiusToJava2D(theta, radius, dataArea);
            //poly.addPoint(p.x, p.y);
             XYGPSDataItem gpsItem = (XYGPSDataItem)((XYSeriesCollection) dataset).getSeries(seriesIndex).getDataItem(i);
            drawScatterPoint(p, gpsItem, seriesIndex,g2); // draw the point

        }


    } // drawSeries



    protected void drawScatterPoint(Point p, XYGPSDataItem gpsItem,int seriesIndex, Graphics2D g2)
    {
        Paint paint_old = g2.getPaint(); // save old paint

        int size;
        if(seriesSize.containsKey(seriesIndex))
        {
            size = seriesSize.get(seriesIndex);
        }
        else
        {
            size = getDefaultSize();
        }

        int shape;
        if(seriesShape.containsKey(seriesIndex))
        {
            shape = seriesShape.get(seriesIndex);
        }
        else
        {
            shape = getDefaultScatterShape();
        }

        // if  signal not used
        if( gpsItem.getSignalType() == XYGPSDataItem.NOT_USED_FIX)
        {
            shape = CIRCLE;
        }
        else if(gpsItem.getSignalType() == XYGPSDataItem.NOT_TRACKING)
        {
            g2.setPaint(Color.RED);
            shape = SQUARE;
        }

        switch(shape)
        {
            case FILLED_CIRCLE:
                g2.fillOval(p.x-size/2, p.y-size/2, size, size);
                break;
            case CIRCLE:
                g2.drawOval(p.x-size/2, p.y-size/2, size, size);
                break;
            case FILLED_SQUARE:
                g2.fillRect(p.x-size/2, p.y-size/2, size, size);
                break;
            case SQUARE:
                g2.drawRect(p.x-size/2, p.y-size/2, size, size);
                break;
            default:
                g2.drawOval(p.x, p.y, size, size);
                break;
        }

        if(shape == CIRCLE || shape == SQUARE)
        {
            g2.setPaint(labelColorUnFilled); // unfilled color of lable
        }
        else
        {
            g2.setPaint(labelColor);
        }

        int fontSize = g2.getFont().getSize();

        // draw label
        g2.drawString(gpsItem.getLabel(), p.x-fontSize/2-2*(gpsItem.getLabel().length()-1), p.y+fontSize/2-1);

        g2.setPaint(paint_old);

    } //drawScatterPoint

    /**
     * @return the labelColorUnFilled
     */
    public Color getLabelColorUnFilled()
    {
        return labelColorUnFilled;
    }

    /**
     * @param labelColorUnFilled the labelColorUnFilled to set
     */
    public void setLabelColorUnFilled(Color labelColorUnFilled)
    {
        this.labelColorUnFilled = labelColorUnFilled;
    }

}
