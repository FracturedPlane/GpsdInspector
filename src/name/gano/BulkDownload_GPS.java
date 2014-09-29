/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package name.gano;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.examples.util.SectorSelector;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.Earth.MSVirtualEarthLayer;
import gov.nasa.worldwind.layers.Mercator.examples.OSMMapnikLayer;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ArrayList;

/**
 * Bulk download of layer data.
 *
 * @author Patrick Murris, Edits by Shawn Gano (GPS version)
 * @version $Id: BulkDownload.java 10475 2009-04-24 02:53:53Z patrickmurris $
 */
public class BulkDownload_GPS extends ApplicationTemplate
{

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // set icon
            super.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/Earth-Scan-24x24.png")));


            // Add control panel
            this.getLayerPanel().add(new BulkDownloadPanel(getWwd()), BorderLayout.SOUTH);

            this.getLayerPanel().update(getWwd());

        }
    }

    public static AppFrame start(String appName, Class appFrameClass)
    {
        if (Configuration.isMacOS() && appName != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try
        {
            final AppFrame frame = (AppFrame) appFrameClass.newInstance();
            frame.setTitle(appName);

            // SEG -- CHANGED didn't want full app to be closed
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                }
            });

            return frame;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static class BulkDownloadPanel extends JPanel
    {
        private WorldWindow wwd;
        private Sector currentSector;
        private ArrayList<BulkRetrievablePanel> retrievables;

        private JButton selectButton;
        private JLabel sectorLabel;
        private JButton startButton;
        private JPanel monitorPanel;

        private SectorSelector selector;
        Timer updateTimer;


        public BulkDownloadPanel(WorldWindow wwd)
        {
            this.wwd = wwd;

            // add wanted layers here!
            // this is the default lower level imagery -- also bulk downloadable
            MSVirtualEarthLayer ms = new MSVirtualEarthLayer(MSVirtualEarthLayer.LAYER_HYBRID);
            insertBeforeCompass(wwd, ms);

            OSMMapnikLayer osm = new OSMMapnikLayer();
            osm.setEnabled(false);
            insertBeforeCompass(wwd, osm);

            ViewControlsLayer vcl = new ViewControlsLayer();
            vcl.setPosition(AVKey.SOUTHEAST);
            vcl.setScale(0.6);
            vcl.setLocationOffset(new Vec4(0, 35, 0, 0));
            wwd.addSelectListener(new ViewControlsSelectListener(wwd, vcl));
            insertBeforeCompass(wwd, vcl);



            // Init retievable list
            this.retrievables = new ArrayList<BulkRetrievablePanel>();
            // Layers
            for(Layer layer : this.wwd.getModel().getLayers())
            {
                if(layer instanceof BulkRetrievable)
                {
                    this.retrievables.add(new BulkRetrievablePanel((BulkRetrievable)layer));
                }
            }
            // Elevation models
            CompoundElevationModel cem = (CompoundElevationModel)wwd.getModel().getGlobe().getElevationModel();
            for(ElevationModel elevationModel : cem.getElevationModels())
            {
                if(elevationModel instanceof BulkRetrievable)
                {
                    this.retrievables.add(new BulkRetrievablePanel((BulkRetrievable)elevationModel));
                }
            }

            // Init sector selector
            this.selector = new SectorSelector(wwd);
            this.selector.setInteriorColor(new Color(1f, 1f, 1f, 0.1f));
            this.selector.setBorderColor(new Color(1f, 0f, 0f, 0.5f));
            this.selector.setBorderWidth(3);
            this.selector.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY, new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    updateSector();
                }
            });

            JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
            this.initComponents();
        }

        private void updateSector()
        {
            this.currentSector = this.selector.getSector();
            if (this.currentSector != null)
            {
                // Update sector description
                this.sectorLabel.setText(makeSectorDescription(this.currentSector));
                this.selectButton.setText("Clear sector");
                this.startButton.setEnabled(true);
            }
            else
            {
                // null sector
                this.sectorLabel.setText("-");
                this.selectButton.setText("Select sector");
                this.startButton.setEnabled(false);
            }
            updateRetrievablePanels(this.currentSector);
        }

        private void updateRetrievablePanels(Sector sector)
        {
            for (BulkRetrievablePanel panel : this.retrievables)
                panel.updateDescription(sector);
        }

        private void selectButtonActionPerformed(ActionEvent event)
        {
            if (this.selector.getSector() != null)
            {
                this.selector.disable();
            }
            else
            {
                this.selector.enable();
            }
            updateSector();
        }

        private void startButtonActionPerformed(ActionEvent event)
        {
            for( BulkRetrievablePanel panel : this.retrievables)
            {
                if (panel.selectCheckBox.isSelected())
                {
                    BulkRetrievable retrievable = panel.retrievable;
                    BulkRetrievalThread thread = retrievable.makeLocal(this.currentSector, 0);
                    this.monitorPanel.add(new DownloadMonitorPanel(thread));
                }
            }
            this.getTopLevelAncestor().validate();
        }

        private void initComponents()
        {
            int border = 6;
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Download")));
            this.setToolTipText("Layer imagery bulk download.");

            // Select sector button
            JPanel sectorPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            sectorPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            selectButton = new JButton("Select sector");
            selectButton.setToolTipText("Press Select then press and drag button 1 on globe");
            selectButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    selectButtonActionPerformed(event);
                }
            });
            sectorPanel.add(selectButton);
            sectorLabel = new JLabel("-");
            sectorLabel.setPreferredSize(new Dimension(350, 16));
            sectorLabel.setHorizontalAlignment(JLabel.CENTER);
            sectorPanel.add(sectorLabel);
            this.add(sectorPanel);

            // Retrievable list combo and start button
            JPanel retrievablesPanel = new JPanel();
            retrievablesPanel.setLayout(new BoxLayout(retrievablesPanel, BoxLayout.Y_AXIS));
            retrievablesPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

            // RetrievablePanel list
            for (JPanel panel : this.retrievables)
                retrievablesPanel.add(panel);
            this.add(retrievablesPanel);

            // Start button
            JPanel startPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            startPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            startButton = new JButton("Start download");
            startButton.setEnabled(false);
            startButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    startButtonActionPerformed(event);
                }
            });
            startPanel.add(startButton);
            this.add(startPanel);

            // Download monitor panel
            monitorPanel = new JPanel();
            monitorPanel.setLayout(new BoxLayout(monitorPanel, BoxLayout.Y_AXIS));
            monitorPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            //this.add(monitorPanel);

            // Put the monitor panel in a scroll pane.
            JPanel dummyPanel = new JPanel(new BorderLayout());
            dummyPanel.add(monitorPanel, BorderLayout.NORTH);

            JScrollPane scrollPane = new JScrollPane(dummyPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            scrollPane.setPreferredSize(new Dimension(350, 100));
            this.add(scrollPane);
        }

        public static String makeSectorDescription(Sector sector)
        {
            return String.format("S %7.4f\u00B0 W %7.4f\u00B0 N %7.4f\u00B0 E %7.4f\u00B0",
                sector.getMinLatitude().degrees,
                sector.getMinLongitude().degrees,
                sector.getMaxLatitude().degrees,
                sector.getMaxLongitude().degrees);
        }

        public static String makeSizeDescription(long size)
        {
            return String.format("%.1f MB", (double)size / 1024 / 1024);
        }

    }

    public static class BulkRetrievablePanel extends JPanel
    {
        private BulkRetrievable retrievable;
        private JCheckBox selectCheckBox;
        private JLabel descriptionLabel;
        private Thread updateThread;
        private Sector sector;

        BulkRetrievablePanel(BulkRetrievable retrievable)
        {
            this.retrievable = retrievable;

            this.initComponents();
        }

        private void initComponents()
        {
            this.setLayout(new BorderLayout());
            this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

            // Check + name
            this.selectCheckBox = new JCheckBox(this.retrievable.getName());
            this.selectCheckBox.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (((JCheckBox)e.getSource()).isSelected() && sector != null)
                        updateDescription(sector);
                }
            });
            this.add(this.selectCheckBox, BorderLayout.WEST);
            // Description (size...)
            this.descriptionLabel = new JLabel();
            this.add(this.descriptionLabel, BorderLayout.EAST);
        }

        public void updateDescription(final Sector sector)
        {
            if (this.updateThread != null && this.updateThread.isAlive())
                this.updateThread.interrupt();

            this.sector = sector;
            if (!this.selectCheckBox.isSelected())
            {
                doUpdateDescription(null);
                return;
            }

            this.updateThread = new Thread(new Runnable()
            {
                public void run()
                {
                    descriptionLabel.setText("...");
                    doUpdateDescription(sector);
                }
            });
            this.updateThread.setDaemon(true);
            this.updateThread.start();
        }

        private void doUpdateDescription(Sector sector)
        {
            if (sector != null)
            {
                try
                {
                    this.descriptionLabel.setText(
                        BulkDownloadPanel.makeSizeDescription(this.retrievable.getEstimatedMissingDataSize(sector, 0)));
                }
                catch (Exception e)
                {
                    this.descriptionLabel.setText("-");
                }
            }
            else
                this.descriptionLabel.setText("-");
        }

        public String toString()
        {
            return this.retrievable.getName();
        }
    }

    public static class DownloadMonitorPanel extends JPanel
    {
        private BulkRetrievalThread thread;
        private Progress progress;
        private Timer updateTimer;

        private JLabel descriptionLabel;
        private JProgressBar progressBar;
        private JButton cancelButton;

        public DownloadMonitorPanel(BulkRetrievalThread thread)
        {
            this.thread = thread;
            this.progress = thread.getProgress();

            this.initComponents();

            this.updateTimer = new Timer(1000, new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    updateStatus();
                }
            });
            this.updateTimer.start();
        }

        private void updateStatus()
        {
            // Update description
            String text = thread.getRetrievable().getName();
            text = text.length() > 30 ? text.substring(0, 27) + "..." : text;
            text += " (" + BulkDownloadPanel.makeSizeDescription(this.progress.getCurrentSize())
                + " / " +  BulkDownloadPanel.makeSizeDescription(this.progress.getTotalSize())
                + ")";
            this.descriptionLabel.setText(text);
            // Update progress bar
            int percent = 0;
            if (this.progress.getTotalCount() > 0)
                percent = (int)((float)this.progress.getCurrentCount() /  this.progress.getTotalCount() * 100f);
            this.progressBar.setValue(Math.min(percent, 100));
            // Update tooltip
            String tooltip = BulkDownloadPanel.makeSectorDescription(this.thread.getSector());
            this.descriptionLabel.setToolTipText(tooltip);
            this.progressBar.setToolTipText(makeProgressDescription());

            // Check for end of thread
            if (!this.thread.isAlive())
            {
                // Thread is done
                this.cancelButton.setText("Remove");
                this.cancelButton.setBackground(Color.GREEN);
                this.updateTimer.stop();
            }
        }

        private void cancelButtonActionPerformed(ActionEvent event)
        {
            if (this.thread.isAlive())
            {
                // Cancel thread
                this.thread.interrupt();
                this.cancelButton.setBackground(Color.ORANGE);
                this.cancelButton.setText("Remove");
                this.updateTimer.stop();
            }
            else
            {
                // Remove from monitor panel
                Container top = this.getTopLevelAncestor();
                this.getParent().remove(this);
                top.validate();
            }
        }

        private void initComponents()
        {
            int border = 2;
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            // Description label
            JPanel descriptionPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            descriptionPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            String text = thread.getRetrievable().getName();
            text = text.length() > 40 ? text.substring(0, 37) + "..." : text;
            descriptionLabel = new JLabel(text);
            descriptionPanel.add(descriptionLabel);
            this.add(descriptionPanel);

            // Progrees and cancel button
            JPanel progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
            progressPanel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            progressBar = new JProgressBar(0, 100);
            progressBar.setPreferredSize(new Dimension(100, 16));
            progressPanel.add(progressBar);
            progressPanel.add(Box.createHorizontalStrut(8));
            cancelButton = new JButton("Cancel");
            cancelButton.setBackground(Color.RED);
            cancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    cancelButtonActionPerformed(event);
                }
            });
            progressPanel.add(cancelButton);
            this.add(progressPanel);
        }

        private String makeProgressDescription()
        {
            String text = "";
            if (this.progress.getTotalCount() > 0)
            {
                int percent = (int)((float)this.progress.getCurrentCount() /  this.progress.getTotalCount() * 100f);
                text = percent + "% of ";
                text += BulkDownloadPanel.makeSizeDescription(this.progress.getTotalSize());
            }
            return text;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Layer Download", AppFrame.class);
    }
}
