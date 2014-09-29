/*
 * Scatter plot renderer for polat plots
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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author Shawn Gano, 10 April 2009
 */
public class ScatterPolarItemRenderer extends DefaultPolarItemRenderer
{
    // render scatter points options
    public static final int FILLED_CIRCLE = 0;
    public static final int CIRCLE = 1;
    public static final int FILLED_SQUARE = 2;
    public static final int SQUARE = 3;
    
    // color already taken care of in extended class
    //private Color defaultColor = Color.RED;
    //private Color[] seriesColor;

    // size for each series
    private int defaultSize = 6; // pixel size for items
    protected HashMap<Integer,Integer> seriesSize = new HashMap<Integer,Integer>(); // hold array to specify series size

    // shape of each series
    private int defaultShape = FILLED_CIRCLE;
    protected HashMap<Integer,Integer> seriesShape = new HashMap<Integer,Integer>();

    // label for the point??




    /**
     * Plots the data for a given series. - modified to draw points only not connected
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
            drawScatterPoint(p,seriesIndex,g2); // draw the point
        }
        
//        if (isSeriesFilled(seriesIndex))
//        {
//            Composite savedComposite = g2.getComposite();
//            g2.setComposite(AlphaComposite.getInstance(
//                    AlphaComposite.SRC_OVER, 0.5f));
//            g2.fill(poly);
//            g2.setComposite(savedComposite);
//        }
//        else {
//            g2.draw(poly);
//        }
    } // drawSeries

    protected void drawScatterPoint(Point p,int seriesIndex, Graphics2D g2)
    {
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

        switch(shape)
        {
            case FILLED_CIRCLE:
                g2.fillOval(p.x-size/2, p.y-size/2, size, size);
                break;
            case CIRCLE:
                g2.drawOval(p.x-size/2, p.y-size/2, size, size);
                break;
            case FILLED_SQUARE:
                g2.drawRect(p.x-size/2, p.y-size/2, size, size);
                break;
            case SQUARE:
                g2.fillRect(p.x-size/2, p.y-size/2, size, size);
                break;
            default:
                g2.drawOval(p.x, p.y, size, size);
                break;
        }

    } //drawScatterPoint

    /**
     * @return the defaultSize
     */
    public int getDefaultSize()
    {
        return defaultSize;
    }

    /**
     * @param defaultSize the defaultSize to set
     */
    public void setDefaultSize(int defaultSize)
    {
        this.defaultSize = defaultSize;
    }

    /**
     * @param series
     * @return the seriesSize
     */
    public int getSeriesSize(int series)
    {
        return seriesSize.get(series);
    }

    /**
     * @param series
     * @param size
     */
    public void setSeriesSize(int series, int size)
    {
        seriesSize.put(series,size);
    }

    /**
     * @return the defualtShape
     */
    public int getDefaultScatterShape()
    {
        return defaultShape;
    }

    /**
     * @param defaultShape the defualtShape to set
     */
    public void setDefaultScatterShape(int defaultShape)
    {
        this.defaultShape = defaultShape;
    }

    /**
     * @return the seriesShap
     */
    public int getSeriesScatterShape(int series)
    {
        return seriesShape.get(series);
    }

    /**
     * @param seriesShap the seriesShap to set
     */
    public void setSeriesScatterShape(int series, int shape)
    {
        this.seriesShape.put(series, shape);
    }

}
