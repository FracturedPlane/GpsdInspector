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

package name.gano;

import gnu.io.CommPortIdentifier;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.util.LayerManagerLayer;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Earth.MSVirtualEarthLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.Mercator.examples.OSMMapnikLayer;
import gov.nasa.worldwind.layers.Mercator.examples.VirtualEarthLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.util.measure.LengthMeasurer;
import gov.nasa.worldwind.view.BasicOrbitView;
import gov.nasa.worldwind.view.FlatOrbitView;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import name.gano.gps.GPSSimulatedNmeaDevice;
import name.gano.jfreechart.GPSBarPainter;
import name.gano.jfreechart.SctterPlotItemGPSRenderer;
import name.gano.jfreechart.XYGPSDataItem;
import name.gano.utils.CoordinateConversion;
import name.gano.utils.CustomFileFilter;
import name.glen.xml.XMLGPSParser;

import org.dinopolis.gpstool.gpsinput.GPSDataProcessor;
import org.dinopolis.gpstool.gpsinput.GPSDevice;
import org.dinopolis.gpstool.gpsinput.GPSNetworkGpsdDevice;
import org.dinopolis.gpstool.gpsinput.GPSPosition;
import org.dinopolis.gpstool.gpsinput.GPSRawDataListener;
import org.dinopolis.gpstool.gpsinput.GPSSerialDevice;
import org.dinopolis.gpstool.gpsinput.SatelliteInfo;
import org.dinopolis.gpstool.gpsinput.nmea.GPSNmeaDataProcessor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author sgano
 */
public class GpsInspector extends javax.swing.JFrame implements PropertyChangeListener
{
    public final String versionString = "v1.0 (28 April 2009)";


    static boolean simulation = false;
    boolean saveRawData2File = false;
    File gpsOutputfile;
    BufferedWriter gpsFileBufferedWriter;
    long timeSinceLateFileWrite = -1;

    // speed and heading display
    float lastSpeed = -1;
    float MIN_MPH_SPEED_TRUST_HEADING = 1.5f; // mph

    static GPSNmeaDataProcessor gps_data_processor;

    GPSDevice gps_device;
    GPSNetworkGpsdDevice gpsdDevice;

    // chart data
    private JFreeChart signalChart;
    private DefaultCategoryDataset signalDataSet = new DefaultCategoryDataset();
    private String snrSeriesTitle = "Signal Strength (SNR)";
    private GPSBarPainter gpsBarPainter;  // allows ability to change type of bar to be ploted for each satellite

    private JFreeChart polarChart;
    XYSeriesCollection polarPlotData = new XYSeriesCollection();;
    XYSeries seriesFix = new XYSeries("Used in Fix Calc");
    XYSeries seriesZero = new XYSeries("0 Signal");
    XYSeries seriesNotUsed = new XYSeries("Not Used in Fix but Tracking");

    public final static int DARK_THEME = 0;
    public final static int LIGHT_THEME = 1;
    public final static int SHAWN_THEME = 2;

    // world wind
    WorldWindowGLCanvas wwd;
    BasicModel wwdModel;
    private Globe roundGlobe;
    private FlatGlobe flatGlobe;
    BasicMarker bm;
    BasicMarker destinationMarker;
    gov.nasa.worldwind.geom.Position currentPos;
    LayerManagerLayer lml;
    boolean layerManagerVisible = false;
    ArrayList<Marker> markers;

    LatLon destLatLon = LatLon.ZERO; // default
    Polyline dest2PosLine;
    RenderableLayer lineLayer = new RenderableLayer();
    ArrayList<LatLon> destPosArrayList = new ArrayList<LatLon>();
    Polyline pathLine;
    // time
    private SimpleDateFormat timeDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");

    float altitude_meters = 0f;

    /** Creates new form TestGPSGui */
    public GpsInspector()
    {
        // set look and feel
        try
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); //
        }
        catch(Exception ex1) // default using jgoodies looks plastic theme
        {
        }

        initComponents();

        // set icon
        super.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/Earth-Scan-24x24.png")));


        this.setSize(675, 500); // w,h

        versionLabel.setText(versionString);

        // -- Signal Strength Chart setup ---------

        //signalDataSet.setValue(6, snrSeriesTitle, "Sat1");
        signalChart = createBarChart(signalDataSet);

        // set theme
        applyTheme(DARK_THEME,signalChart);

        // now apply the bar renderer to the chart
         gpsBarPainter = new GPSBarPainter();
        ((BarRenderer)((CategoryPlot)signalChart.getPlot()).getRenderer()).setBarPainter(gpsBarPainter);
        ((BarRenderer)((CategoryPlot)signalChart.getPlot()).getRenderer()).setShadowVisible(false);

        // make room for last digit - needs jcommon lib
        //signalChart.setPadding( new RectangleInsets(0,0,0,5) ); // add some space to right side

        ChartPanel chartP = new ChartPanel(signalChart);
        signalPanel.add(chartP, BorderLayout.CENTER);

        // ---------------------------------------

        //  -- Polar Plot ------------------------
        polarPlotData.addSeries(seriesFix);
        polarPlotData.addSeries(seriesZero);
        polarPlotData.addSeries(seriesNotUsed);

        polarChart = createPolarPlot(polarPlotData);

        ChartPanel chartPP = new ChartPanel(polarChart);
        satLocPanel.add(chartPP, BorderLayout.CENTER);

        // --End Ploar Plot ----------------------


        // WorldWind Java
        wwd = new WorldWindowGLCanvas();
        globePanel.add(wwd, java.awt.BorderLayout.CENTER);
        wwdModel = new BasicModel();
        wwd.setModel(wwdModel);

        if (isFlatGlobe())
        {
            this.flatGlobe = (FlatGlobe)wwd.getModel().getGlobe();
            this.roundGlobe = new Earth();
        }
        else
        {
            this.flatGlobe = new EarthFlat();
            this.roundGlobe = wwd.getModel().getGlobe();
        }
        MarkerLayer layer = this.buildMarkerLayer();
        insertBeforeCompass(wwd, layer);

        //
        ViewControlsLayer vcl = new ViewControlsLayer();
        vcl.setPosition(AVKey.SOUTHEAST);
        vcl.setScale(0.6);
        vcl.setLocationOffset( new Vec4(0,35,0,0));
        wwd.addSelectListener(new ViewControlsSelectListener(wwd, vcl));
        insertBeforeCompass(wwd, vcl);

        lml = new LayerManagerLayer(wwd);
        lml.setEnabled(false);
        //wwd.getModel().getLayers().add(lml);
        lml.setLocationOffset( new Vec4(0,40,0,0));

        // this is the default lower level imagery -- also bulk downloadable
        MSVirtualEarthLayer ms = new MSVirtualEarthLayer(MSVirtualEarthLayer.LAYER_HYBRID);
        insertBeforeCompass(wwd, ms);
        // below is the experimental version that also has full globe imagary
        VirtualEarthLayer ve = new VirtualEarthLayer();
        ve.setEnabled(false);
        insertBeforeCompass(wwd, ve);

        //OpenStreetMapLayer osm = new OpenStreetMapLayer();
        //osm.setEnabled(false);
        //insertBeforeCompass(wwd, osm);
        // experimental version
        OSMMapnikLayer osm = new OSMMapnikLayer();
        osm.setEnabled(false);
        insertBeforeCompass(wwd, osm);

        // remove a couple of laters
        // set default layer visabiliy
        for (Layer currentLayer : wwd.getModel().getLayers())
        {
            if (currentLayer instanceof WorldMapLayer)
            {
                ((WorldMapLayer) currentLayer).setEnabled(false); // off
            }
            if (currentLayer instanceof CompassLayer)
            {
                ((CompassLayer) currentLayer).setShowTilt(true);
                //((CompassLayer) currentLayer).setEnabled(false);
                //((CompassLayer) currentLayer).setIconScale(0.5);
            }
        } // layers

        // line from position to dest
        destPosArrayList.add( new LatLon(destLatLon));
        destPosArrayList.add(new LatLon(this.currentPos.getLatLon()));

        dest2PosLine = new Polyline(destPosArrayList,0.0);
        dest2PosLine.setAntiAliasHint(Polyline.ANTIALIAS_FASTEST);
        dest2PosLine.setFollowTerrain(true);
        dest2PosLine.setLineWidth(2.0);
        dest2PosLine.setColor( new Color(0f,0f,1f,0.5f));
        dest2PosLine.setPathType(Polyline.GREAT_CIRCLE);

        // path line
        pathLine = new Polyline(new ArrayList<Position>());
        pathLine.setAntiAliasHint(Polyline.ANTIALIAS_FASTEST);
        pathLine.setFollowTerrain(true);
        pathLine.setLineWidth(1.0);
        pathLine.setColor( Color.MAGENTA );
        pathLine.setPathType(Polyline.LINEAR); // linear should be spaced in small steps



         // create line layer
        //lineLayer.addRenderable(dest2PosLine);
        lineLayer.setPickEnabled(false); // no pick
        wwd.getModel().getLayers().add(lineLayer); // add layer but don't add the lines to it yet

    } // constructor


    /**
     * Creates a sample chart.
     *
     * @param dataset  the dataset.
     *
     * @return A sample chart.
     */
    private JFreeChart createBarChart(final DefaultCategoryDataset  dataset)
    {

        // for different colors in a serires see:
        // http://www.java2s.com/Code/Java/Chart/JFreeChartBarChartDemo3differentcolorswithinaseries.htm

        //createBarChart3D also works
       final JFreeChart result = ChartFactory.createBarChart(
                "Signal Strength of Tracked Satellites",// chart title
                "Satellite ID",  // x axis label
                "SNR", // y axis label
                dataset, // data
                PlotOrientation.VERTICAL,
                false, // include legend
                true, // tooltips
                false);// urls

       // FINALLY a away to get rid of those gradient lines on the bars
       //CategoryPlot plot = (CategoryPlot) result.getPlot();
       //BarRenderer renderer = (BarRenderer) plot.getRenderer();
       //renderer.setBarPainter(new StandardBarPainter());

        return result;
    } // createChart

    /**
     * Creates a sample chart.
     *
     * @param dataset  the dataset.
     *
     * @return A sample chart.
     */
    private JFreeChart createPolarPlot(final XYDataset dataset)
    {
        final JFreeChart chart = ChartFactory.createPolarChart(
            "Satellite Locations",
            dataset,
            false, //lengend
            true,  // tooltips
            false // url
        );
        final PolarPlot plot = (PolarPlot) chart.getPlot();

        chart.setBackgroundPaint(new Color(220,220,220));

        ChartTheme dark = StandardChartTheme.createDarknessTheme();
        dark.apply(chart);
        plot.setOutlinePaint(Color.BLACK);
       

        //final DefaultPolarItemRenderer renderer = (DefaultPolarItemRenderer) plot.getRenderer();
        // set renderer - using my custom one
        SctterPlotItemGPSRenderer rend = new SctterPlotItemGPSRenderer();
        plot.setRenderer(rend);

        // set horizon = 0-90
        ValueAxis axis = plot.getAxis();
        axis.setRange(0.0,90.0);
        // set tick spacing
        //axis.getRange().
        //axis.setAutoTickUnitSelection(false);
        //axis.setAutoRangeMinimumSize(3);
        TickUnits tu = new TickUnits();
        tu.add( new NumberTickUnit(15));
        axis.setStandardTickUnits( tu );


        return chart;
    } // create polar plot


    private void applyTheme(int themeNum, JFreeChart chart)
    {
        switch(themeNum)
        {
            case DARK_THEME:
                ChartTheme dark = StandardChartTheme.createDarknessTheme();
                dark.apply(chart);
                
                BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
                 chart.getPlot().setOutlinePaint(Color.DARK_GRAY);
                renderer.setSeriesPaint(0, new Color(51, 102, 153));
                break;
            case LIGHT_THEME:
                ChartTheme jfree =	StandardChartTheme.createJFreeTheme();
                jfree.apply(chart);
                break;
            case SHAWN_THEME:
                jfree =	StandardChartTheme.createJFreeTheme();
                jfree.apply(chart);
                Color lines = new Color(120,166,255);
//                chart.getXYPlot().setBackgroundPaint(Color.WHITE);
//                chart.getXYPlot().setDomainGridlinePaint(lines);
//                chart.getXYPlot().setRangeGridlinePaint(lines);
//                chart.getXYPlot().setDomainMinorGridlinesVisible(true);
//                chart.getXYPlot().setDomainMinorGridlinePaint(lines);
//                chart.getXYPlot().setRangeMinorGridlinePaint(lines);
                chart.setBackgroundPaint(new Color(220,220,220));
                
                // bar chart only
                renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
                // FINALLY a away to get rid of those stupid gradient lines on the bars
                renderer.setBarPainter(new StandardBarPainter());
                
//                renderer.setDrawBarOutline(false);
//                final GradientPaint gp0 = new GradientPaint(
//                        0.0f, 0.0f, Color.blue,
//                        0.0f, 0.0f, Color.lightGray);
//                renderer.setSeriesPaint(0, gp0);
 
               renderer.setSeriesPaint(0, new Color(51, 102, 153));

               renderer.setSeriesPaint(1, new Color(255, 0, 0));

                renderer.setShadowVisible(false);

                
                
                
        break;



        }
    } // apply theme

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        portComboBox = new javax.swing.JComboBox();
        speedTextField = new javax.swing.JTextField();
        scanButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        simRadioButton = new javax.swing.JRadioButton();
        replayRadioButton = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        record2FileCheckBox = new javax.swing.JCheckBox();
        rateTextField = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        latitude_label = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        longitude_label = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        altitude_label = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        speed_label = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        heading_label = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        fixTimeTextField = new javax.swing.JTextField();
        utcToggleButton = new javax.swing.JToggleButton();
        jLabel11 = new javax.swing.JLabel();
        fixInfoTextField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        fixQualTextField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        pdopTextField = new javax.swing.JTextField();
        hdopTextField = new javax.swing.JTextField();
        vdopTextField = new javax.swing.JTextField();
        decimalCheckBox = new javax.swing.JCheckBox();
        jPanel12 = new javax.swing.JPanel();
        savePathCheckBox = new javax.swing.JCheckBox();
        showPathCheckBox = new javax.swing.JCheckBox();
        clearPathButton = new javax.swing.JButton();
        followTerrainCheckBox = new javax.swing.JCheckBox();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        destinationPanel = new javax.swing.JPanel();
        useDestinationCheckBox = new javax.swing.JCheckBox();
        jPanel10 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        destLatTextField = new javax.swing.JTextField();
        destLonTextField = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        setDestLocationButton = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        destDistTextField = new javax.swing.JTextField();
        toaTextField = new javax.swing.JTextField();
        heading2DestTextField = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        globePanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        projectionCombo = new javax.swing.JComboBox();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        layerListButton = new javax.swing.JButton();
        headingCheckBox = new javax.swing.JCheckBox();
        followCheckBox = new javax.swing.JCheckBox();
        offlineModeCheckBox = new javax.swing.JCheckBox();
        bulkDownloaderButton = new javax.swing.JButton();
        cacheButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        signalPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        satLocPanel = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        rawDataTextArea = new javax.swing.JTextArea();
        
        
        /*
         * The below vairiable were added by Glen Berseth to make it possible
         * for GpsInspector to use gpsd.
         */
        host_address = new javax.swing.JTextField();
        host_port = new JTextField();
        gpsd_checkbox = new JCheckBox();
        gpsd_connect = new JButton();
        
        
        host_address_label = new JLabel();
        host_port_label = new JLabel();
        gpsd = new JLabel();
        
        host_address_label.setText("host address");
        host_port_label.setText("host port");
        gpsd.setText("GPSD");
        host_address.setText("127.0.0.1");
        host_port.setText("2947");
        gpsd_connect.setText("gpsd connect");
        
        gpsd_connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gpsdConnectActionPerformed(evt);
            }
        });
        

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("GPS Inspector");

        jPanel1.setPreferredSize(new java.awt.Dimension(50, 50));

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(50, 50));

        jPanel2.setPreferredSize(new java.awt.Dimension(50, 50));

        startButton.setText("start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        jButton2.setText("stop");
        jButton2.setPreferredSize(new java.awt.Dimension(40, 25));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel6.setText("Serial Port:");

        jLabel7.setText("Speed:");

        portComboBox.setEditable(true);
        portComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "COM1", "COM2", "COM3", "COM4", "/dev/ttyUSB0", "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3", "/dev/ttyS4" }));

        speedTextField.setText("4800");

        scanButton.setText("scan for list of ports");
        scanButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanButtonActionPerformed(evt);
            }
        });

        jLabel8.setText("Created by: Shawn Gano, shawn@gano.name");

        versionLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        versionLabel.setText("v x.x.x");

        simRadioButton.setText("Simulate GPS Device");
        simRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simRadioButtonActionPerformed(evt);
            }
        });

        replayRadioButton.setText("Replay from File");
        replayRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replayRadioButtonActionPerformed(evt);
            }
        });

        jLabel9.setText("Rate:");

        record2FileCheckBox.setText("Record to file");

        rateTextField.setText("1.0");

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Earth-Scan-128x128.png"))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(record2FileCheckBox))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 194, Short.MAX_VALUE)
                        .addComponent(versionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gpsd) // Added by Glen Berseth for gpsd
                            .addComponent(host_address_label) // Added by Glen Berseth for gpsd
                            .addComponent(host_port_label) // Added by Glen Berseth for gpsd
                            )
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(speedTextField)
                                    .addComponent(portComboBox, 0, 117, Short.MAX_VALUE)
                                    .addComponent(host_address) // Added by Glen Berseth for gpsd
                                    .addComponent(host_port) // Added by Glen Berseth for gpsd
                                    .addComponent(gpsd_connect) // Added by Glen Berseth for gpsd
                                    )
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scanButton))
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(replayRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(simRadioButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 236, Short.MAX_VALUE)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(portComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(scanButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(speedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        	.addComponent(gpsd, GroupLayout.Alignment.LEADING)
                        	)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        	.addComponent(host_address_label)
                        	.addComponent(host_address)
                        	) // Added by Glen Berseth for gpsd
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        		.addComponent(host_port_label)
                        		.addComponent(host_port)
                        		)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        	.addComponent(gpsd_connect)
                        		)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(startButton)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(record2FileCheckBox)
                        .addGap(24, 24, 24)
                        .addComponent(simRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(replayRadioButton)
                            .addComponent(jLabel9)
                            .addComponent(rateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 108, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(versionLabel))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("GPS Controls", jPanel2);

        jPanel4.setPreferredSize(new java.awt.Dimension(200, 200));

        jPanel7.setPreferredSize(new java.awt.Dimension(200, 200));

        jLabel1.setText("Latitude:");

        jLabel2.setText("Longitude:");

        jLabel3.setText("Altitude [ft]:");
        jLabel3.setToolTipText("MSL");

        jLabel4.setText("Speed [mph]:");

        jLabel5.setText("Heading [deg]:");
        jLabel5.setToolTipText("Heading in Degrees from North True");

        jLabel10.setText("Fix Time:");

        utcToggleButton.setText("UTC/Local");
        utcToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utcToggleButtonActionPerformed(evt);
            }
        });

        jLabel11.setText("Fix Info:");

        jLabel12.setText("Fix Quality:");

        jLabel13.setText("PDOP:");
        jLabel13.setToolTipText("Position Dilution of Precision");

        jLabel14.setText("HDOP:");
        jLabel14.setToolTipText("Horizontal Dilution of Precision");

        jLabel15.setText("VDOP:");
        jLabel15.setToolTipText("Vertical Dilution of Precision");
        
        northing_label = new JLabel();
        northing_label.setText("northing");
        easting_label = new JLabel();
        easting_label.setText("easting");
        altitude_in_meters_label = new JLabel();
        altitude_in_meters_label.setText("altitude [m]");
        speed_in_kph_label = new JLabel();
        speed_in_kph_label.setText("speed [kph]");
        utm_label = new JLabel();
        utm_label.setText("UTM");
         
        northing = new JTextField();
        easting = new JTextField();
        altitude_in_meters = new JTextField();
        speed_in_kph = new JTextField();
        utm_sector = new JTextField();
        

        decimalCheckBox.setText("decimal format");

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Display Options"));

        savePathCheckBox.setText("Save Path");

        showPathCheckBox.setText("Show Path");
        showPathCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPathCheckBoxActionPerformed(evt);
            }
        });

        clearPathButton.setText("Clear Path");
        clearPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearPathButtonActionPerformed(evt);
            }
        });

        followTerrainCheckBox.setSelected(true);
        followTerrainCheckBox.setText("Follow Terrain");
        followTerrainCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                followTerrainCheckBoxActionPerformed(evt);
            }
        });

        jButton7.setText("Calc Dist");
        jButton7.setToolTipText("Calcualte Distance of the Path");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(savePathCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showPathCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearPathButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(followTerrainCheckBox)
                .addGap(40, 40, 40)
                .addComponent(jButton7)
                .addContainerGap(105, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(savePathCheckBox)
                    .addComponent(showPathCheckBox)
                    .addComponent(clearPathButton)
                    .addComponent(followTerrainCheckBox)
                    .addComponent(jButton7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton8.setText("DOP Scale");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(fixTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(utcToggleButton))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel1)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel5))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(heading_label, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                                            .addComponent(speed_label, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                                            .addComponent(altitude_label, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                                            .addComponent(longitude_label, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                                            .addComponent(latitude_label, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)))
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(fixInfoTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    ))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
		                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		                                	// Added by Glen Berseth
		                                	.addComponent(utm_label)
		                                	.addComponent(northing_label)// Added by Glen Berseth	
		                                	.addComponent(easting_label)
		                                	.addComponent(altitude_in_meters_label)
		                                	.addComponent(speed_in_kph_label)
		                                	)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		                                	.addComponent(utm_sector, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
		                                	.addComponent(northing, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE )// Added by Glen Berseth
		                                	.addComponent(easting, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE )
		                                	.addComponent(altitude_in_meters, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE )
		                                	.addComponent(speed_in_kph, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE )
		                                	))))
		                                	//
		                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel12))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton8)
                                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(fixQualTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(vdopTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                                            
                                            .addComponent(hdopTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                                            .addComponent(pdopTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))))))
                        .addGap(274, 274, 274))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(decimalCheckBox)
                        .addContainerGap(478, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(122, 122, 122))))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(fixInfoTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(utm_label)
                            .addComponent(utm_sector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         )
                        .addGap(18, 18, 18)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(latitude_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(northing_label)
                            .addComponent(northing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        )
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(longitude_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(easting_label)
                            .addComponent(easting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                        )
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(altitude_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(altitude_in_meters_label)
                            .addComponent(altitude_in_meters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        )
                        .addGap(18, 18, 18)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(speed_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(speed_in_kph_label)
                            .addComponent(speed_in_kph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        )
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(heading_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                    	.addComponent(northing_label)// added by Glen Berseth
                    		
                    )     
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fixQualTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(pdopTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(hdopTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15)
                            .addComponent(vdopTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)))
                .addGap(19, 19, 19)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(fixTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(utcToggleButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(decimalCheckBox)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Position Info", jPanel4);

        useDestinationCheckBox.setText("Use Destination");
        useDestinationCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useDestinationCheckBoxActionPerformed(evt);
            }
        });

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Destination Description"));

        jLabel17.setText("Latitude:");

        jLabel18.setText("Longitude:");

        jLabel19.setText("or:");

        jButton6.setText("search");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        setDestLocationButton.setText("Set Location");
        setDestLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDestLocationButtonActionPerformed(evt);
            }
        });

        /*
         * These variable are for the xml File chooser
         */
        
        add_waypoints = new JButton();
        add_waypoints.setText("add waypoints");
        add_waypoints.addActionListener(new java.awt.event.ActionListener() 
        {
        	public void actionPerformed(java.awt.event.ActionEvent evt) 
        	{
                addWaypointsButtonActionPerformed(evt);
            }
        }
        );
        
        remove_waypoints = new JButton();
        remove_waypoints.setText("remove waypoints");
        remove_waypoints.addActionListener(new java.awt.event.ActionListener() 
        {
        	public void actionPerformed(java.awt.event.ActionEvent evt) 
        	{
                removeWaypointsButtonActionPerformed(evt);
            }
        }
        );
        waypoint_xml_filechooser = new JFileChooser();
        
        // String[] data = {"oneeeeeeeeeeeeeee", "two", "three", "four", "five", "six", "seven", "eight"};
 
        waypoint_list = new JList();
        waypoint_pane = new JScrollPane(waypoint_list);
        set_selected_as_destination = new JButton();
        set_selected_as_destination.setText("set as destination");
        
        
        set_selected_as_destination.addActionListener(new java.awt.event.ActionListener() 
        {
        	public void actionPerformed(java.awt.event.ActionEvent evt) 
        	{
                setDestinationButtonActionPerformed(evt);
            }
        });
        
        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(setDestLocationButton)
                        //.addComponent(waypoint_list)
                    )
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18)
                            .addComponent(add_waypoints))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(destLonTextField)
                            .addComponent(destLatTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                            .addComponent(remove_waypoints)
                         )
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)
                        )
                	)
                	.addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                		.addComponent(waypoint_pane,  280, 300, 500 )
                		.addComponent(set_selected_as_destination)
                	)
                 
                .addGap(293, 293, 293))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(destLatTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(destLonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(setDestLocationButton)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(add_waypoints)
                	.addComponent(remove_waypoints)
                	)
            )
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(waypoint_pane,  100, 150, 300)
                .addComponent(set_selected_as_destination)
            )

            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jButton6)))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Calculated Values"));

        jLabel20.setText("Direct Distance  [mi]:");

        jLabel21.setText("Estimate time to arrive:");

        jLabel22.setText("Heading [true] :");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel22)
                )
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(heading2DestTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(destDistTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(314, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(destDistTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(toaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(heading2DestTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        
        
        //TODO modifiey this to support xml file waypoint additions
        javax.swing.GroupLayout destinationPanelLayout = new javax.swing.GroupLayout(destinationPanel);
        destinationPanel.setLayout(destinationPanelLayout);
        destinationPanelLayout.setHorizontalGroup(
            destinationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, destinationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(destinationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    //.addComponent(waypoint_xml_filechooser)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
                    .addComponent(useDestinationCheckBox, javax.swing.GroupLayout.Alignment.LEADING))
               // .addGroup(destinationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                //	.addComponent(waypoint_xml_filechooser)
                	//	)
                .addContainerGap())
        );
        destinationPanelLayout.setVerticalGroup(
            destinationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(destinationPanelLayout.createSequentialGroup()
            	//.addGroup(destinationPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(useDestinationCheckBox)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
	                //.addComponent(waypoint_xml_filechooser)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addContainerGap()
	             //.addGroup(destinationPanelLayout.createSequentialGroup()
	            //	.addComponent(waypoint_xml_filechooser)
	             )
        );

        jTabbedPane1.addTab("Destination", destinationPanel);

        globePanel.setMinimumSize(new java.awt.Dimension(0, 0));
        globePanel.setPreferredSize(new java.awt.Dimension(50, 31));
        globePanel.setLayout(new java.awt.BorderLayout());

        jToolBar1.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/emblem-web.png"))); // NOI18N
        jButton1.setToolTipText("3D Globe");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/treasure-map-24x24.png"))); // NOI18N
        jButton3.setToolTipText("2D Map using selected projection");
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        projectionCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mercator", "Sinusoidal", "Modified Sin.", "Lat/Lon" }));
        projectionCombo.setSelectedIndex(3);
        projectionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectionComboActionPerformed(evt);
            }
        });
        jToolBar1.add(projectionCombo);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Zoom-In-24x24.png"))); // NOI18N
        jButton4.setToolTipText("Zoom to current location");
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Zoom-Out-24x24.png"))); // NOI18N
        jButton5.setToolTipText("Zoom Out to View Whole Earth");
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);

        layerListButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Task-List-24x24.png"))); // NOI18N
        layerListButton.setToolTipText("Toggle Layer List");
        layerListButton.setFocusable(false);
        layerListButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        layerListButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        layerListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layerListButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(layerListButton);

        headingCheckBox.setText("Auto Heading");
        headingCheckBox.setFocusable(false);
        jToolBar1.add(headingCheckBox);

        followCheckBox.setText("Follow Position");
        followCheckBox.setFocusable(false);
        jToolBar1.add(followCheckBox);

        offlineModeCheckBox.setText("Offline Mode");
        offlineModeCheckBox.setToolTipText("Uses only cached imagery and doesn't use a network connection");
        offlineModeCheckBox.setFocusable(false);
        offlineModeCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        offlineModeCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        offlineModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                offlineModeCheckBoxActionPerformed(evt);
            }
        });
        jToolBar1.add(offlineModeCheckBox);

        bulkDownloaderButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/My-Downloads-24x24.png"))); // NOI18N
        bulkDownloaderButton.setToolTipText("Imagery Layer Bulk Downloader (cache for offline use)");
        bulkDownloaderButton.setFocusable(false);
        bulkDownloaderButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bulkDownloaderButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bulkDownloaderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bulkDownloaderButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(bulkDownloaderButton);

        cacheButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Trash-Full-24x24.png"))); // NOI18N
        cacheButton.setToolTipText("Imagery Cache Info/Cleaner");
        cacheButton.setFocusable(false);
        cacheButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cacheButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cacheButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cacheButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(cacheButton);

        globePanel.add(jToolBar1, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(globePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(globePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Globe", jPanel9);

        signalPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(signalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(signalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Signal Strength", jPanel5);

        satLocPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(satLocPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(satLocPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Sat Locations", jPanel6);

        rawDataTextArea.setBackground(new java.awt.Color(0, 0, 0));
        rawDataTextArea.setColumns(20);
        rawDataTextArea.setForeground(new java.awt.Color(51, 255, 0));
        rawDataTextArea.setRows(5);
        rawDataTextArea.setText("Raw GPS Data:\n");
        jScrollPane1.setViewportView(rawDataTextArea);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Raw Data", jPanel8);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    private void gpsdConnectActionPerformed(ActionEvent evt)
    {

        // if sim or not
        simulation = simRadioButton.isSelected();

        /// check for record to a file
        if(record2FileCheckBox.isSelected())
        {
            // get the user to specify a file
            saveRawData2File = promptUserForLogFile();
            
            // reset timer
            timeSinceLateFileWrite = -1;

            if(!saveRawData2File)
            {
                record2FileCheckBox.setSelected(false);
            }

        } // record to file?
        else
        {
            saveRawData2File = false;
        }
        

       
        rawDataTextArea.setText("");  // clear area

        // check to see if we are replaying from a file
        gps_data_processor = new GPSNmeaDataProcessor();

        gps_data_processor.addGPSRawDataListener(
        new GPSRawDataListener()
        {
          public void gpsRawDataReceived(char[] data, int offset, int length)
          {
            
             // put data in text area, and limit length of text
              String newData = new String(data,offset,length);
              rawDataTextArea.append(newData);

              // Make sure the last line is always visible
              rawDataTextArea.setCaretPosition(rawDataTextArea.getDocument().getLength());

              // Keep the text area down to a certain character size
              int idealSize = 50000;
              int maxExcess = 200;
              int excess = rawDataTextArea.getDocument().getLength() - idealSize;
              if(excess >= maxExcess)
              {
                  rawDataTextArea.replaceRange("", 0, excess);
              }

              // if we are supposed to save the data out to a file
              if(saveRawData2File)
              {
                  try
                  {
                     // check if time in ms > 0 if so then report time and create a linebreak
                      if( timeSinceLateFileWrite>0 )
                      {
                          long currTime = System.currentTimeMillis();
                          long diff = currTime-timeSinceLateFileWrite;
                          timeSinceLateFileWrite = currTime; // update time

                          gpsFileBufferedWriter.write("##" + (diff) + "\n" );
                      }
                      else
                      {
                          timeSinceLateFileWrite = System.currentTimeMillis();
                      }

                     // write new data (without line break at the end)
                     gpsFileBufferedWriter.write( newData.trim() );
                  }
                  catch(Exception e)
                  {
                      // file must be messed up - alert user
                      JOptionPane.showMessageDialog(null, "Error saving to file (file will no longer be written to): \n" + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
                      // try to close file
                      try{
                        gpsFileBufferedWriter.close();
                      }catch(Exception ee){}
                      
                      saveRawData2File = false;
                  }
              } // save raw data 2 file

          }
        });

        gps_data_processor.addGPSDataChangeListener(this); // add panel

          // Define device to read data from
       
        // get data from GUI
        int speedBaud = 4800;
        String portName = "COM1";
        String hostAddress = "127.0.0.1";
        int hostPort = 2947;
        try
        {
            speedBaud = Integer.parseInt(speedTextField.getText());
            portName = portComboBox.getSelectedItem().toString();
            hostAddress = host_address.getText();
            hostPort = Integer.parseInt(host_port.getText());
            
        }catch(Exception e)
        {
            JOptionPane.showMessageDialog(this,"Data Entry Error: \n" + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // save settings
        Hashtable environment = new Hashtable();
        environment.put(GPSSerialDevice.PORT_NAME_KEY, portName);
        environment.put(GPSSerialDevice.PORT_SPEED_KEY, new Integer(speedBaud));
		environment.put(GPSNetworkGpsdDevice.GPSD_HOST_KEY, hostAddress);
		environment.put(GPSNetworkGpsdDevice.GPSD_PORT_KEY, hostPort);
        

        if(replayRadioButton.isSelected())
        {
            // ask for a file  - if cancel quit
            final JFileChooser fc = new JFileChooser();
            CustomFileFilter gpsFilter = new CustomFileFilter("gps","*.gps");
            fc.addChoosableFileFilter(gpsFilter);

            int returnVal = fc.showOpenDialog(this);

            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
                File inFile = fc.getSelectedFile();
                // get playback rate
                float rate = Float.parseFloat(rateTextField.getText());

                gps_device = new GPSSimulatedNmeaDevice(inFile, rate);
            }
            else
            {
                return;  // don't start just quit
            }

        }
        else if(simulation)
        {
            gps_device = new GPSSimulatedNmeaDevice();
            
        }
        else
        {
            gpsdDevice = new GPSNetworkGpsdDevice();
            System.out.println("NetworkedG GPS device is created");
        }

		
        try
        {
            // set params needed to open device (file,serial, ...):
            gpsdDevice.init(environment);
            // connect device and data processor:
            gps_data_processor.setGPSDevice(gpsdDevice);
            gps_data_processor.open();

            System.out.println("GPSInfo::");
            String[] infos = gps_data_processor.getGPSInfo();
            String gpsInfo= "";
            for (int index = 0; index < infos.length; index++)
            {
                gpsInfo += infos[index] + "\n";
            }

            // disable start button
            startButton.setEnabled(false);

            // alert it started ok
            JOptionPane.showMessageDialog(this,"Connected to GPS Successfully: \n" + gpsInfo, "Success", JOptionPane.INFORMATION_MESSAGE);

        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this,"Error connection to GPS: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();

            // re enable start button
            startButton.setEnabled(true);
        }
    }
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_startButtonActionPerformed
    {//GEN-HEADEREND:event_startButtonActionPerformed

        // if sim or not
        simulation = simRadioButton.isSelected();

        /// check for record to a file
        if(record2FileCheckBox.isSelected())
        {
            // get the user to specify a file
            saveRawData2File = promptUserForLogFile();
            
            // reset timer
            timeSinceLateFileWrite = -1;

            if(!saveRawData2File)
            {
                record2FileCheckBox.setSelected(false);
            }

        } // record to file?
        else
        {
            saveRawData2File = false;
        }
        

       
        rawDataTextArea.setText("");  // clear area

        // check to see if we are replaying from a file
        gps_data_processor = new GPSNmeaDataProcessor();


        gps_data_processor.addGPSRawDataListener(
        new GPSRawDataListener()
        {
          public void gpsRawDataReceived(char[] data, int offset, int length)
          {
            
             // put data in text area, and limit length of text
              String newData = new String(data,offset,length);
              rawDataTextArea.append(newData);

              // Make sure the last line is always visible
              rawDataTextArea.setCaretPosition(rawDataTextArea.getDocument().getLength());

              // Keep the text area down to a certain character size
              int idealSize = 50000;
              int maxExcess = 200;
              int excess = rawDataTextArea.getDocument().getLength() - idealSize;
              if(excess >= maxExcess)
              {
                  rawDataTextArea.replaceRange("", 0, excess);
              }

              // if we are supposed to save the data out to a file
              if(saveRawData2File)
              {
                  try
                  {
                     // check if time in ms > 0 if so then report time and create a linebreak
                      if( timeSinceLateFileWrite>0 )
                      {
                          long currTime = System.currentTimeMillis();
                          long diff = currTime-timeSinceLateFileWrite;
                          timeSinceLateFileWrite = currTime; // update time

                          gpsFileBufferedWriter.write("##" + (diff) + "\n" );
                      }
                      else
                      {
                          timeSinceLateFileWrite = System.currentTimeMillis();
                      }

                     // write new data (without line break at the end)
                     gpsFileBufferedWriter.write( newData.trim() );
                  }
                  catch(Exception e)
                  {
                      // file must be messed up - alert user
                      JOptionPane.showMessageDialog(null, "Error saving to file (file will no longer be written to): \n" + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
                      // try to close file
                      try{
                        gpsFileBufferedWriter.close();
                      }catch(Exception ee){}
                      
                      saveRawData2File = false;
                  }
              } // save raw data 2 file

          }
        });

        gps_data_processor.addGPSDataChangeListener(this); // add panel

          // Define device to read data from
       
        // get data from GUI
        int speedBaud = 4800;
        String portName = "COM1";
        try
        {
            speedBaud = Integer.parseInt(speedTextField.getText());
            portName = portComboBox.getSelectedItem().toString();
        }catch(Exception e)
        {
            JOptionPane.showMessageDialog(this,"Data Entry Error: \n" + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // save settings
        Hashtable environment = new Hashtable();
        environment.put(GPSSerialDevice.PORT_NAME_KEY, portName);
        environment.put(GPSSerialDevice.PORT_SPEED_KEY, new Integer(speedBaud));
        

        if(replayRadioButton.isSelected())
        {
            // ask for a file  - if cancel quit
            final JFileChooser fc = new JFileChooser();
            CustomFileFilter gpsFilter = new CustomFileFilter("gps","*.gps");
            fc.addChoosableFileFilter(gpsFilter);

            int returnVal = fc.showOpenDialog(this);

            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
                File inFile = fc.getSelectedFile();
                // get playback rate
                float rate = Float.parseFloat(rateTextField.getText());

                gps_device = new GPSSimulatedNmeaDevice(inFile, rate);
            }
            else
            {
                return;  // don't start just quit
            }

        }
        else if(simulation)
        {
            gps_device = new GPSSimulatedNmeaDevice();
        }
        else
        {
            gps_device = new GPSSerialDevice();
        }

        try
        {
            // set params needed to open device (file,serial, ...):
            gps_device.init(environment);
            // connect device and data processor:
            gps_data_processor.setGPSDevice(gps_device);
            gps_data_processor.open();

            System.out.println("GPSInfo:");
            String[] infos = gps_data_processor.getGPSInfo();
            String gpsInfo= "";
            for (int index = 0; index < infos.length; index++)
            {
                gpsInfo += infos[index] + "\n";
            }

            // disable start button
            startButton.setEnabled(false);

            // alert it started ok
            JOptionPane.showMessageDialog(this,"Connected to GPS Successfully: \n" + gpsInfo, "Success", JOptionPane.INFORMATION_MESSAGE);

        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this,"Error connection to GPS: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();

            // re enable start button
            startButton.setEnabled(true);
        }
}//GEN-LAST:event_startButtonActionPerformed

    private boolean promptUserForLogFile()
    {
            final JFileChooser fc = new JFileChooser();
            CustomFileFilter gpsFilter = new CustomFileFilter("gps","*.gps");
            fc.addChoosableFileFilter(gpsFilter);
            
            int returnVal = fc.showSaveDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File file = fc.getSelectedFile();

                String fileExtension = "gps"; // default
                if(fc.getFileFilter() == gpsFilter)
                {
                    fileExtension = "gps";
                }
                
                String extension = getExtension(file);
                if (extension != null)
                {
                    fileExtension = extension;
                }
                else
                {
                    // append the extension
                    gpsOutputfile = new File(file.getAbsolutePath() + "." + fileExtension);
                    // create buffered writer
                    try
                    {
                        gpsFileBufferedWriter = new BufferedWriter(new FileWriter(gpsOutputfile)); // overwrite

                        gpsFileBufferedWriter.write("# NMEA GPS data followed by ## then the time in ms before next message was sent\n");

                    }
                    catch(IOException e)
                    {
                        JOptionPane.showMessageDialog(this, "Error opening file: \n" + e.toString(), "File Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }


                }

            }
            else
            {
                return false;
            }
            
            return true;

    } // prompt user

    public static String getExtension(File f)
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1)
        {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    } // getExtension


    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed

        startButton.setEnabled(true);
        
        try
        {
            gps_data_processor.close(); // must close otherwise it keeps on running

            // close file if needed
            if(saveRawData2File)
            {
                gpsFileBufferedWriter.close();
            }
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, e.toString()); // JOptionPane.ERROR_MESSAGE
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void scanButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_scanButtonActionPerformed
    {//GEN-HEADEREND:event_scanButtonActionPerformed

        // clear selections
        ((DefaultComboBoxModel)portComboBox.getModel()).removeAllElements();


        // do an auto scan or serial ports availiable
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier portId;

        while(portList.hasMoreElements())
        {
            portId = (CommPortIdentifier)portList.nextElement();
            if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                // add option:
                ((DefaultComboBoxModel)portComboBox.getModel()).addElement( portId.getName() );

            } // if
        } // while


    }//GEN-LAST:event_scanButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        this.enableFlatGlobe(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton3ActionPerformed
    {//GEN-HEADEREND:event_jButton3ActionPerformed
        this.enableFlatGlobe(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void projectionComboActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectionComboActionPerformed
    {//GEN-HEADEREND:event_projectionComboActionPerformed
        updateProjection();
    }//GEN-LAST:event_projectionComboActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton4ActionPerformed
    {//GEN-HEADEREND:event_jButton4ActionPerformed
        LatLon latLon = currentPos.getLatLon();
        if (latLon != null)
        {
            OrbitView view = (OrbitView) wwd.getView();
            Globe globe = wwd.getModel().getGlobe();
            view.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
                    view, globe, new Position(latLon, 0), view.getHeading(), view.getPitch(), 3000.0  )); //view.getZoom()
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void simRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_simRadioButtonActionPerformed
    {//GEN-HEADEREND:event_simRadioButtonActionPerformed
        replayRadioButton.setSelected(false);
    }//GEN-LAST:event_simRadioButtonActionPerformed

    private void replayRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_replayRadioButtonActionPerformed
    {//GEN-HEADEREND:event_replayRadioButtonActionPerformed
        simRadioButton.setSelected(false);
    }//GEN-LAST:event_replayRadioButtonActionPerformed

    private void utcToggleButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_utcToggleButtonActionPerformed
    {//GEN-HEADEREND:event_utcToggleButtonActionPerformed
        if(utcToggleButton.isSelected())
        {
            timeDateFormat.setTimeZone( TimeZone.getTimeZone("UTC") );
        }
        else
        {
             timeDateFormat.setTimeZone( TimeZone.getDefault() );
             //timeDateFormat.setTimeZone( TimeZone.getTimeZone("CDT") );

             //System.out.println("" + timeDateFormat.getTimeZone().toString() );
        }

        fixTimeTextField.setText(""); // clear time box
    }//GEN-LAST:event_utcToggleButtonActionPerformed

    private void layerListButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_layerListButtonActionPerformed
    {//GEN-HEADEREND:event_layerListButtonActionPerformed

        layerManagerVisible = !layerManagerVisible;

        if(layerManagerVisible)
        {
            wwd.getModel().getLayers().add(lml);
            lml.setMinimized(false);
        }
        else
        {
            wwd.getModel().getLayers().remove(lml);
        }

    }//GEN-LAST:event_layerListButtonActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton5ActionPerformed
    {//GEN-HEADEREND:event_jButton5ActionPerformed
        LatLon latLon = currentPos.getLatLon();
        if (latLon != null)
        {
            OrbitView view = (OrbitView) wwd.getView();
            Globe globe = wwd.getModel().getGlobe();
            view.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
                    view, globe, new Position(latLon, 0), view.getHeading(), view.getPitch(), 20000000.0  )); //view.getZoom()
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton6ActionPerformed
    {//GEN-HEADEREND:event_jButton6ActionPerformed
        LocationSearchDialog dialog = new LocationSearchDialog(this, true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // get results
        if(dialog.isLocationSelected())
        {
            LatLon ll = dialog.getLocLatLon();
            destLatTextField.setText(ll.getLatitude().getDegrees()+""); // .toDecimalDegreesString(8)
            destLonTextField.setText(ll.getLongitude().getDegrees()+"");
            //setDestLocationButton.doClick(); // set location
            destLatLon = ll;
            destinationMarker.setPosition( new Position(destLatLon.getLatitude(), destLatLon.getLongitude(), 0.0) );
            useDestinationCheckBox.setSelected(true); // auto select
            manageDestinationObjects();
        }
        
    }//GEN-LAST:event_jButton6ActionPerformed

    private void setDestLocationButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_setDestLocationButtonActionPerformed
    {//GEN-HEADEREND:event_setDestLocationButtonActionPerformed
        try
        {
            destLatLon = new LatLon(Angle.fromDegrees(Double.parseDouble(destLatTextField.getText())),Angle.fromDegrees(Double.parseDouble(destLonTextField.getText())) );
            destinationMarker.setPosition( new Position(destLatLon.getLatitude(), destLatLon.getLongitude(), 0.0) );
            useDestinationCheckBox.setSelected(true);
            manageDestinationObjects();
            //System.out.println("here" + destLatLon.toString());
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, "Data Error: \n" + e.toString(), "Data Error",JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_setDestLocationButtonActionPerformed

    private void useDestinationCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_useDestinationCheckBoxActionPerformed
    {//GEN-HEADEREND:event_useDestinationCheckBoxActionPerformed
         manageDestinationObjects();
    }//GEN-LAST:event_useDestinationCheckBoxActionPerformed

    private void followTerrainCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_followTerrainCheckBoxActionPerformed
    {//GEN-HEADEREND:event_followTerrainCheckBoxActionPerformed
        pathLine.setFollowTerrain( followTerrainCheckBox.isSelected() );
}//GEN-LAST:event_followTerrainCheckBoxActionPerformed

    private void showPathCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showPathCheckBoxActionPerformed
    {//GEN-HEADEREND:event_showPathCheckBoxActionPerformed
        // deal with showing the path
        if(showPathCheckBox.isSelected())
        {
            lineLayer.addRenderable(pathLine);
        }
        else
        {
            lineLayer.removeRenderable(pathLine);
        }
    }//GEN-LAST:event_showPathCheckBoxActionPerformed

    private void clearPathButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearPathButtonActionPerformed
    {//GEN-HEADEREND:event_clearPathButtonActionPerformed
        ((ArrayList<Position>)pathLine.getPositions()).clear();
    }//GEN-LAST:event_clearPathButtonActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton7ActionPerformed
    {//GEN-HEADEREND:event_jButton7ActionPerformed
        LengthMeasurer lm = new LengthMeasurer((ArrayList<Position>)pathLine.getPositions());

        lm.setFollowTerrain( followTerrainCheckBox.isSelected() );

        lm.setPathType(Polyline.LINEAR);

        double len = lm.getLength(roundGlobe);

        JOptionPane.showMessageDialog(this, "The Path Distance is:\n Meters: " + len  + "\n Miles: " + (0.000621371192*len), "Path Distance", JOptionPane.INFORMATION_MESSAGE);


    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        JOptionPane.showMessageDialog(this,
                "1 = Ideal\n"
                + "2-3 = Execellent\n"
                + "4-6 = Good\n"
                + "7-8 = Moderate\n"
                + "9-20 = Fair\n"
                + "21-50 = Poor\n"
                + "See the DOP wikipedia page for more info",
                "DOP",
                JOptionPane.INFORMATION_MESSAGE);
        
    }//GEN-LAST:event_jButton8ActionPerformed

    private void offlineModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_offlineModeCheckBoxActionPerformed
    {//GEN-HEADEREND:event_offlineModeCheckBoxActionPerformed
        // set off line mode (if true, doesn't stream data from the net, but does use cached imagery)
        gov.nasa.worldwind.WorldWind.getNetworkStatus().setOfflineMode( offlineModeCheckBox.isSelected() );
}//GEN-LAST:event_offlineModeCheckBoxActionPerformed

    private void bulkDownloaderButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_bulkDownloaderButtonActionPerformed
    {//GEN-HEADEREND:event_bulkDownloaderButtonActionPerformed
        
        BulkDownload_GPS.start("Bulk Imagery Download Tool", BulkDownload_GPS.AppFrame.class);
        
    }//GEN-LAST:event_bulkDownloaderButtonActionPerformed

    private void cacheButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cacheButtonActionPerformed
    {//GEN-HEADEREND:event_cacheButtonActionPerformed
        CacheInspectorDialog d = new CacheInspectorDialog(this, false);
        d.setLocationRelativeTo(this);
        d.setSize(400, 400); // w,h
        d.setVisible(true);
    }//GEN-LAST:event_cacheButtonActionPerformed

    private void manageDestinationObjects()
    {
        if(useDestinationCheckBox.isSelected())
        {

        	/*
        	 BasicMarkerAttributes attrs2 =
                 new BasicMarkerAttributes(Material.RED, BasicMarkerShape.SPHERE, 1d); // HEADING_ARROW
        	 destinationMarker = new BasicMarker(currentPos, attrs2, Angle.fromDegrees(90.0));
        	 
        	 currentPos = new Position(Angle.fromDegrees(pos.getLatitude()), Angle.fromDegrees(pos.getLongitude()), 0.0);

         */
        	//BasicMarkerAttributes markerattribues = new BasicMarkerAttributes(arg0, arg1, arg2, arg3, arg4)
        	//TODO Set up XML file parsing for Coordinates
        	//BasicMarkerAttributes markerattributes = new BasicMarkerAttributes( Material.BLACK, BasicMarkerShape.CONE, 1d);
        	//Position position = new Position(Angle.fromDegrees(45), Angle.fromDegrees(45), 0.0);
        	//BasicMarker testMarker = new BasicMarker(position, markerattributes, Angle.fromDegrees(90.0));
            // set destination position:
        	destinationMarker.setPosition( new Position(destLatLon.getLatitude(), destLatLon.getLongitude(), 0.0) );
            System.out.println(" Dest = " + destinationMarker.getPosition().toString());
            markers.add( destinationMarker );
            //markers.add(testMarker);

            // not thread safe see:
            // http://forum.worldwindcentral.com/showthread.php?t=20508
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {

                    destPosArrayList.clear();
                    destPosArrayList.add(new LatLon(destLatLon)); // new latLon to prevent null
                    destPosArrayList.add(new LatLon(currentPos.getLatLon()));
                    dest2PosLine.setPositions(destPosArrayList, 0.0);
                }
            });

            lineLayer.addRenderable(dest2PosLine);
            //wwd.getModel().getLayers().add(lineLayer)
            //insertBeforeCompass(wwd, lineLayer);
        }
        else
        {
            markers.remove( destinationMarker );

            lineLayer.removeRenderable(dest2PosLine);
            //wwd.getModel().getLayers().remove(lineLayer)
        }

        wwd.redraw();// redraw
    } // manageDestinationObjects

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) 
    {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run()
            {
                GpsInspector app = new GpsInspector();
                RefineryUtilities.centerFrameOnScreen(app);
                app.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField altitude_label;
    private javax.swing.JButton bulkDownloaderButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cacheButton;
    private javax.swing.JButton clearPathButton;
    private javax.swing.JCheckBox decimalCheckBox;
    private javax.swing.JTextField destDistTextField;
    private javax.swing.JTextField destLatTextField;
    private javax.swing.JTextField destLonTextField;
    private javax.swing.JPanel destinationPanel;
    private javax.swing.JTextField fixInfoTextField;
    private javax.swing.JTextField fixQualTextField;
    private javax.swing.JTextField fixTimeTextField;
    private javax.swing.JCheckBox followCheckBox;
    private javax.swing.JCheckBox followTerrainCheckBox;
    private javax.swing.JPanel globePanel;
    private javax.swing.JTextField hdopTextField;
    private javax.swing.JTextField heading2DestTextField;
    private javax.swing.JCheckBox headingCheckBox;
    private javax.swing.JTextField heading_label;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTextField latitude_label;
    private javax.swing.JButton layerListButton;
    private javax.swing.JTextField longitude_label;
    private javax.swing.JCheckBox offlineModeCheckBox;
    private javax.swing.JTextField pdopTextField;
    private javax.swing.JComboBox portComboBox;
    private javax.swing.JComboBox projectionCombo;
    private javax.swing.JTextField rateTextField;
    private javax.swing.JTextArea rawDataTextArea;
    private javax.swing.JCheckBox record2FileCheckBox;
    private javax.swing.JRadioButton replayRadioButton;
    private javax.swing.JPanel satLocPanel;
    private javax.swing.JCheckBox savePathCheckBox;
    private javax.swing.JButton scanButton;
    private javax.swing.JButton setDestLocationButton;
    private javax.swing.JCheckBox showPathCheckBox;
    private javax.swing.JPanel signalPanel;
    private javax.swing.JRadioButton simRadioButton;
    private javax.swing.JTextField speedTextField;
    private javax.swing.JTextField speed_label;
    private javax.swing.JButton startButton;
    private javax.swing.JTextField toaTextField;
    private javax.swing.JCheckBox useDestinationCheckBox;
    private javax.swing.JToggleButton utcToggleButton;
    private javax.swing.JTextField vdopTextField;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
    
    /*
     * These variables are added by Glen Berseth to make it possible
     * for this program to used gpsd over the network.
     */

    private JLabel host_address_label;
    private JLabel host_port_label;
    private JLabel gpsd;
    private JTextField host_address;
    private JTextField host_port;
    private JButton gpsd_connect;
    private JCheckBox gpsd_checkbox;
    
    /*
     * These variables were added by Glen Berseth to allow for UTM and
     * other metric coordinates to be displayed.
     * 
     */

    private JLabel altitude_in_meters_label;
    private JTextField altitude_in_meters;
    private JLabel easting_label;
    private JTextField easting;
    private JLabel northing_label;
    private JTextField northing;
    private JLabel speed_in_kph_label;
    private JTextField speed_in_kph;
    private JLabel utm_label;
    private JTextField utm_sector;
    private CoordinateConversion UTMConvertor;
    
    /*
     * These atributes are for using the xml coordinate waypoint adder
     */
    
    private JButton add_waypoints;
    private JButton remove_waypoints;
    private JFileChooser waypoint_xml_filechooser;
    private JList waypoint_list;
    private JScrollPane waypoint_pane;
    private JButton set_selected_as_destination;
    
    
    private void setDestinationButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    	//System.out.println(wwd.getCurrentPosition().latitude.toString());
    	
    	Position position = (Position) waypoint_list.getSelectedValue();
    	System.out.println(position.toString());
    	
    	destLatTextField.setText("" + position.latitude.degrees);
    	destLonTextField.setText("" + position.longitude.degrees);
        destLatLon = new LatLon(Angle.fromDegrees(Double.parseDouble(destLatTextField.getText())), 
        		Angle.fromDegrees(Double.parseDouble(destLonTextField.getText())) );

    	
       // destinationMarker.setPosition( new Position(position.getLatitude(), position.getLongitude(), 0.0) );
        useDestinationCheckBox.setSelected(true);
        manageDestinationObjects();
    	
    }
    private void addWaypointsButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    	 int returnVal = waypoint_xml_filechooser.showOpenDialog(this);
    	 List<Position> waypoints = new ArrayList<Position>();

    	 System.out.println(returnVal);
    	 
    	 if (returnVal == JFileChooser.APPROVE_OPTION)
    	 {
    		 System.out.println("Approve option");
    		 File xmlFile = waypoint_xml_filechooser.getSelectedFile();
    		 System.out.println(xmlFile.getAbsolutePath());
    		 
    		 XMLGPSParser xmlParser = new XMLGPSParser();
    		 xmlParser.parseXmlFile(xmlFile);
    		 
    		 waypoints.addAll(xmlParser.parseDocument());
    		// waypoint_list.setListData(waypoints.toArray());
    		 
    		 /*
    		  * //BasicMarkerAttributes markerattribues = new BasicMarkerAttributes(arg0, arg1, arg2, arg3, arg4)
        	//TODO Set up XML file parsing for Coordinates
        	BasicMarkerAttributes markerattributes = new BasicMarkerAttributes( Material.BLACK, BasicMarkerShape.CONE, 1d);
        	Position position = new Position(Angle.fromDegrees(45), Angle.fromDegrees(45), 0.0);
        	BasicMarker testMarker = new BasicMarker(position, markerattributes, Angle.fromDegrees(90.0));
            // set destination position:
            destinationMarker.setPosition( new Position(destLatLon.getLatitude(), destLatLon.getLongitude(), 0.0) );
            markers.add( destinationMarker );
            markers.add(testMarker);
    		  */
    		 
    		 BasicMarkerAttributes markerattributes = new BasicMarkerAttributes( Material.CYAN, BasicMarkerShape.CONE, 1d);
    		 Position position;
    		 BasicMarker testMarker;
    		 
    		 Iterator<Position> points = waypoints.iterator();
    		 while (points.hasNext())
    		 {
    			 Position tmp = points.next();
    			 position = new Position(tmp.latitude, tmp.longitude, 0.0);
    			 testMarker = new BasicMarker(position, markerattributes, Angle.fromDegrees(90.0));
    			 markers.add(testMarker);
    		 }
    		 
    	 }
    	 waypoint_list.setListData(waypoints.toArray());
    	
    }
    
    
    private void removeWaypointsButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    	
    	int returnVal = waypoint_xml_filechooser.showOpenDialog(this);
   	 	List<Position> waypoints = new ArrayList<Position>();

   	 	System.out.println(returnVal);
   	 
   	 if (returnVal == JFileChooser.APPROVE_OPTION)
   	 {
   		 System.out.println("Approve option");
   		 File xmlFile = waypoint_xml_filechooser.getSelectedFile();
   		 System.out.println(xmlFile.getAbsolutePath());
   		 
   		 XMLGPSParser xmlParser = new XMLGPSParser();
   		 xmlParser.parseXmlFile(xmlFile);
   		 
   		 waypoints.addAll(xmlParser.parseDocument());
   		 
   		 /*
   		  * //BasicMarkerAttributes markerattribues = new BasicMarkerAttributes(arg0, arg1, arg2, arg3, arg4)
       	//TODO Set up XML file parsing for Coordinates
       	BasicMarkerAttributes markerattributes = new BasicMarkerAttributes( Material.BLACK, BasicMarkerShape.CONE, 1d);
       	Position position = new Position(Angle.fromDegrees(45), Angle.fromDegrees(45), 0.0);
       	BasicMarker testMarker = new BasicMarker(position, markerattributes, Angle.fromDegrees(90.0));
           // set destination position:
           destinationMarker.setPosition( new Position(destLatLon.getLatitude(), destLatLon.getLongitude(), 0.0) );
           markers.add( destinationMarker );
           markers.add(testMarker);
   		  */
   		 
   		 BasicMarkerAttributes markerattributes = new BasicMarkerAttributes( Material.CYAN, BasicMarkerShape.CONE, 1d);
   		 Position position;
   		 BasicMarker testMarker;
   		 
   		 Iterator<Position> points = waypoints.iterator();
   		 while (points.hasNext())
   		 {
   			 Position tmp = points.next();
   			 position = new Position(tmp.latitude, tmp.longitude, 0.0);
   			 testMarker = new BasicMarker(position, markerattributes, Angle.fromDegrees(90.0));
   			 
   			 
   			System.out.println("Contains " + testMarker.toString() + " " + markers.contains(testMarker));
   			for (int k = 0; k < markers.size(); k++)
   			{
   				Position test = new Position(markers.get(k).getPosition().latitude, markers.get(k).getPosition().longitude, 
   						markers.get(k).getPosition().elevation);
   				
   				System.out.println("position = " + position.toString());
   				System.out.println("test   =   " + test.toString());
   				if ( test.equals(position) )
   						//test.latitude.equals(position.latitude) && test.longitude.equals(position.longitude) )
   				{
   					markers.remove(k);
   					break;
   				}
   				
   				
   			}
   			 
   			 
   			 for (int j = 0; j < markers.size(); j++)
   			 System.out.println(markers.get(j).getPosition().toString());
   			 
   		 }
   		 
   	 }
    	
    }
    // WWJ -----------------
    public boolean isFlatGlobe()
    {
        return wwd.getModel().getGlobe() instanceof FlatGlobe;
    }

    public void enableFlatGlobe(boolean flat)
    {
        if (isFlatGlobe() == flat)
        {
            return;
        }

        if (!flat)
        {
            // Switch to round globe
            wwd.getModel().setGlobe(roundGlobe);
            // Switch to orbit view and update with current position
            FlatOrbitView flatOrbitView = (FlatOrbitView) wwd.getView();
            BasicOrbitView orbitView = new BasicOrbitView();
            orbitView.setCenterPosition(flatOrbitView.getCenterPosition());
            orbitView.setZoom(flatOrbitView.getZoom());
            orbitView.setHeading(flatOrbitView.getHeading());
            orbitView.setPitch(flatOrbitView.getPitch());
            wwd.setView(orbitView);
            // Change sky layer
            LayerList layers = wwd.getModel().getLayers();
            for (int i = 0; i < layers.size(); i++)
            {
                if (layers.get(i) instanceof SkyColorLayer)
                {
                    layers.set(i, new SkyGradientLayer());
                }
            }
        } else
        {
            // Switch to flat globe
            wwd.getModel().setGlobe(flatGlobe);
            flatGlobe.setProjection(this.getProjection());
            // Switch to flat view and update with current position
            BasicOrbitView orbitView = (BasicOrbitView) wwd.getView();
            FlatOrbitView flatOrbitView = new FlatOrbitView();
            flatOrbitView.setCenterPosition(orbitView.getCenterPosition());
            flatOrbitView.setZoom(orbitView.getZoom());
            flatOrbitView.setHeading(orbitView.getHeading());
            flatOrbitView.setPitch(orbitView.getPitch());
            wwd.setView(flatOrbitView);
            // Change sky layer
            LayerList layers = wwd.getModel().getLayers();
            for (int i = 0; i < layers.size(); i++)
            {
                if (layers.get(i) instanceof SkyGradientLayer)
                {
                    layers.set(i, new SkyColorLayer());
                }
            }
        }

        wwd.redraw();
    } // enableFlatGlobe
    
    private String getProjection()
    {
        String item = (String) projectionCombo.getSelectedItem();
        if(item.equals("Mercator"))
            return FlatGlobe.PROJECTION_MERCATOR;
        else if(item.equals("Sinusoidal"))
            return FlatGlobe.PROJECTION_SINUSOIDAL;
        else if(item.equals("Modified Sin."))
            return FlatGlobe.PROJECTION_MODIFIED_SINUSOIDAL;
        else if(item.equals("Lat/Lon"))
            return FlatGlobe.PROJECTION_LAT_LON;
        // Default to lat-lon
        return FlatGlobe.PROJECTION_LAT_LON;
    } // getProjection

    // Update flat globe projection
    private void updateProjection()
    {
        if (!isFlatGlobe())
                return;

        // Update flat globe projection
        this.flatGlobe.setProjection(this.getProjection());
        this.wwd.redraw();
    } //updateProjection


    private MarkerLayer buildMarkerLayer()
    {
        try
        {

            BasicMarkerAttributes attrs =
                    new BasicMarkerAttributes(Material.WHITE, BasicMarkerShape.ORIENTED_CONE, 1d); // HEADING_ARROW


            markers = new ArrayList<Marker>();
            //while (positions.hasNext())
            //{
                currentPos = new Position(Angle.fromDegrees(29.5),Angle.fromDegrees(-95.1),10.0); // Lat/Lon/Alt
                bm = new BasicMarker(currentPos, attrs, Angle.fromDegrees(90.0)); // position, attributes, heading
                bm.setHeading(Angle.fromDegrees(90.0));

                markers.add(bm);
            //}

            // make destination marker but don't add it to the map yet
            BasicMarkerAttributes attrs2 =
                    new BasicMarkerAttributes(Material.RED, BasicMarkerShape.SPHERE, 1d); // HEADING_ARROW
            destinationMarker = new BasicMarker(currentPos, attrs2, Angle.fromDegrees(90.0));


            MarkerLayer layer = new MarkerLayer(markers);
            layer.setOverrideMarkerElevation(true);
            layer.setElevation(0);
            layer.setEnablePickSizeReturn(true);

            


            return layer;
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    } // buildMarkerLayer

    public static void insertBeforeCompass(WorldWindow wwd, Layer layer)
    {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof CompassLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    } // insertBeforeCompass


    //  end WWJ  ------------

    // property change listener ======================================
    Integer[] satellites_ids = new Integer[12];

    String[] fixQualityStr = new String[] {"Invalid","GPS fix (SPS)","DGPS fix","PPS fix","Real Time Kinematic","Float RTK","estimated (dead reckoning)","Manual input mode","Simulation mode"};

    public void propertyChange(PropertyChangeEvent event)
    {
    	UTMConvertor = new CoordinateConversion();
        String name = event.getPropertyName();
        Object value = event.getNewValue();

        if(name.equals(GPSDataProcessor.IDS_SATELLITES))
        {
             satellites_ids = (Integer[])value; // save
        }
        else if(name.equals(GPSDataProcessor.SATELLITE_INFO))
        {
            // clear old data
            signalDataSet.clear();
            // polar plot clear
            seriesFix.clear();
            seriesZero.clear();
            seriesNotUsed.clear();

            // Two series do not work - must specify paintrenderer
            
            SatelliteInfo[] infos = (SatelliteInfo[])value;
            SatelliteInfo info;
            for(int count = 0; count < infos.length; count++)
            {
                info = infos[count];
                //System.out.println("sat " + info.getPRN() + ": elev=" + info.getElevation() + " azim=" + info.getAzimuth() + " dB=" + info.getSNR());

                // get SNR or if used in tracking
               int signalType = XYGPSDataItem.USED_FIX;
               if( info.getSNR() == 0 )
               {
                   signalType = XYGPSDataItem.NOT_TRACKING;
               }
               else if(!isSatUsedInFixSolution(info.getPRN())) // check if used in fix solution
               {
                    signalType = XYGPSDataItem.NOT_USED_FIX;
               }

                // for now dump them all in fix
                if(count != infos.length -1 )
                {   // add points but don't notify
                    gpsBarPainter.setSignalType(count, signalType);
                    signalDataSet.addValue(info.getSNR(), snrSeriesTitle, "" + info.getPRN());

                    seriesFix.add(new XYGPSDataItem(info.getAzimuth(), info.getElevation(),info.getPRN(),""+info.getPRN(),signalType),false);
                }
                else // last value, add last point and notify plot of changes
                {
                     gpsBarPainter.setSignalType(count, signalType);
                     signalDataSet.setValue(info.getSNR(), snrSeriesTitle, "" + info.getPRN());

                    // make changes and send repaint notification
                    seriesFix.add(new XYGPSDataItem(info.getAzimuth(), info.getElevation(),info.getPRN(),""+info.getPRN(),signalType),true);
                }
            }


        } // sat info
        else if (name.equals(GPSDataProcessor.LOCATION))
        {
            GPSPosition pos = (GPSPosition) value;
            String[] UTMCoords;
            UTMCoords = UTMConvertor.latLon2UTM(pos.getLatitude(), pos.getLongitude()).split(" ");
           
            utm_sector.setText(UTMCoords[0] + " " + UTMCoords[1]);
            
            northing.setText(UTMCoords[3]);
            easting.setText(UTMCoords[2]);

            if (decimalCheckBox.isSelected())
            {
                latitude_label.setText(String.format("%.8f", pos.getLatitude()));
                longitude_label.setText(String.format("%.8f", pos.getLongitude()));
            } else
            {
                latitude_label.setText(decimalDegrees2DegMinSecStr(pos.getLatitude(), false));
                longitude_label.setText(decimalDegrees2DegMinSecStr(pos.getLongitude(), true));
            }


            currentPos = new Position(Angle.fromDegrees(pos.getLatitude()), Angle.fromDegrees(pos.getLongitude()), 0.0);
            bm.setPosition(currentPos);

            if (followCheckBox.isSelected())
            {
                ((OrbitView) wwd.getView()).setCenterPosition(currentPos);
            }

            // destination calculations
            if(useDestinationCheckBox.isSelected())
            {
                double distMiles = 0.000621371192 * LatLon.ellipsoidalDistance(currentPos.getLatLon(), destLatLon, 6378.1363E3, 6356752.3142);
                destDistTextField.setText("" + String.format("%.3f",distMiles));

                // not quite right - see Vincenty java algorithm
                Angle destHead = LatLon.greatCircleAzimuth(currentPos.getLatLon(), destLatLon);
                if(destHead.getDegrees() < 0) // correct negative values
                {
                    destHead = destHead.addDegrees(360.0);
                }
                if(decimalCheckBox.isSelected())
                {
                    heading2DestTextField.setText( destHead.toDecimalDegreesString(4) );
                }
                else
                {
                    heading2DestTextField.setText( destHead.toDMSString() );
                }

                if(lastSpeed >= MIN_MPH_SPEED_TRUST_HEADING)
                {
                    double hrs = distMiles/lastSpeed;
                    int hr = (int) hrs;
                    int min = (int)((hrs-hr)*60.0);
                    int sec = (int) (( ((hrs-hr)*60.0) - min)*60.0);
                    toaTextField.setText( String.format("%6d:%2d:%2d",hr,min,sec)  );
                }
                else
                {
                    toaTextField.setText("NA");
                }

                // update globe! and line between points!
                // not thread safe see:
                // http://forum.worldwindcentral.com/showthread.php?t=20508
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {

                        destPosArrayList.clear();
                        destPosArrayList.add(new LatLon(destLatLon)); // new latLon to prevent null
                        destPosArrayList.add(new LatLon(currentPos.getLatLon()));
                        dest2PosLine.setPositions(destPosArrayList, 0.0);
                    }
                });



            } // destination

            if(savePathCheckBox.isSelected())
            {
                ((ArrayList<Position>)pathLine.getPositions()).add( new Position(currentPos,altitude_meters));  // when is altitude updated?
            } // save path

            wwd.redraw();

        } // location
        else if (name.equals(GPSDataProcessor.ALTITUDE))
        {
            altitude_meters = (Float)value;
            //altitude_meters = ft;
            altitude_in_meters.setText("" + altitude_meters); // Added by Glen Berseth
            altitude_label.setText( (altitude_meters * 3.2808399f) + ""); // convert to m from ft
        }
        else if (name.equals(GPSDataProcessor.SPEED))
        {
            float ft = (Float)value;
            lastSpeed = ft * 0.621371192f;
            speed_in_kph.setText("" + ft); // Added by Glen berseth
            speed_label.setText( lastSpeed + "" );  // convert to mph from km/h
        }
        else if (name.equals(GPSDataProcessor.HEADING))
        {
            Float ft = (Float)value;

            if(lastSpeed >= MIN_MPH_SPEED_TRUST_HEADING)
            {
                heading_label.setText(ft + "");
                bm.setHeading(Angle.fromDegrees(ft));
                wwd.redraw();

                if(headingCheckBox.isSelected())
                {
                    // auot heading of globe
                    ((OrbitView)wwd.getView()).setHeading(Angle.fromDegrees(((Float)value).doubleValue()));
                }
            }
            else
            {
                heading_label.setText("NA");
            } // speed too low to accept heading
        }
        else if (name.equals(GPSDataProcessor.FIXTIME))
        {
            fixTimeTextField.setText(timeDateFormat.format(((GregorianCalendar) value).getTime()));
        }
        else if (name.equals(GPSDataProcessor.FIX_INFO))
        {
            switch (((Integer) value).intValue())
            {
                case 1: // no fix
                    fixInfoTextField.setText("No Fix");
                    break;
                case 2: // no fix
                    fixInfoTextField.setText("2D Fix");
                    break;
                case 3: // no fix
                    fixInfoTextField.setText("3D Fix");
                    break;

            } // switch

        } // fix info
        else if (name.equals(GPSDataProcessor.FIX_QUALITY))
        {
            fixQualTextField.setText(fixQualityStr[((Integer) value).intValue()]);
        } // fix qual
        else if (name.equals(GPSDataProcessor.PDOP))
        {
            pdopTextField.setText(String.format("%.2f", (Float) value));
        } // pdop
        else if (name.equals(GPSDataProcessor.HDOP))
        {
            hdopTextField.setText(String.format("%.2f", (Float) value));
        } // hdop
        else if (name.equals(GPSDataProcessor.VDOP))
        {
            vdopTextField.setText(String.format("%.2f", (Float) value));
        } // vdop


    } // propertyChange
    
    private boolean isSatUsedInFixSolution(int satID)
    {
        try
        {
            for (int i = 0; i < satellites_ids.length; i++)
            {
                if (satID == satellites_ids[i].intValue())
                {
                    return true;
                }
            }
        } catch (Exception e)
        {
            return false; // end of the list
        }
        return false;
    } // isSatUsedInFixSolution

     public String decimalDegrees2DegMinSecStr(double degrees, boolean eastWest)
     {
         boolean neg = false;

         if(degrees < 0)
         {
             neg = true;
         }

         // insure positive value
         degrees = Math.abs(degrees);

         int deg = (int) degrees;
         degrees = (degrees-deg)*60.0;
         int min = (int) degrees;
         double sec = (degrees-min)*60.0;

         if(!eastWest) // North South
         {
             // OLD WAY ASSUMING they wanted a negative reading
//            String negStr = "";
//            if(neg)
//            {
//                negStr = "-";
//            }
//            return String.format(negStr + "%d"+(char)176+" %d' %.3f\"", deg,min,sec);
            String dir = "N";
             if(neg)
             {
                 dir = "S";
             }
             return String.format("%d"+(char)176+" %d' %.4f\" " + dir, deg,min,sec);
         }
         else
         {
             String dir = "E";
             if(neg)
             {
                 dir = "W";
             }
             return String.format("%d"+(char)176+" %d' %.4f\" " + dir, deg,min,sec);
         }

     } // decimalDegrees2DegMinSecStr

}
