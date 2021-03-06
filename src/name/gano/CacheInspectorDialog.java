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

/*
 * CacheInspectorDialog.java
 *
 * Created on Apr 27, 2009, 2:27:15 PM
 */

package name.gano;

import gov.nasa.worldwind.WorldWind;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import name.gano.utils.CacheCleaner;
import name.gano.utils.CacheCleaner.CacheDirectory;
import name.gano.utils.CacheCleaner.CacheLocationData;

/**
 *
 * @author sgano
 */
public class CacheInspectorDialog extends javax.swing.JDialog {

    /** Creates new form CacheInspectorDialog
     * @param parent
     * @param modal
     */
    public CacheInspectorDialog(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();

        // set cache location box
        File writeCache = WorldWind.getDataFileStore().getWriteLocation();
        cacheTextField.setText(writeCache.getAbsolutePath());

        // anaylze cache
        analyzeCache();

    }

CacheLocationData cld;

    public void analyzeCache()
    {
        // loop for each high lecel dir for each size and display total size in a thread
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    // display each top level dir info
                    File writeCache = WorldWind.getDataFileStore().getWriteLocation();
                    String dataStr = "";
                    cld = CacheCleaner.analyseCacheLocation(writeCache);

                    int lenPathRoot = writeCache.getAbsolutePath().length();

                    for(CacheDirectory cd : cld.directories)
                    {
                        dataStr += cd.file.getAbsolutePath().substring(lenPathRoot)  + " size: " + String.format("%.3f",cd.sizeInBytes/Math.pow(2, 20)) + " MB \n";
                    }

                    dataStr += " ------------- \n";
                    
                    dataStr += "Total Number of Directories: " + cld.directories.size() + "\n";
                    dataStr += "Total Number of Files: " + cld.fileCount + "\n";
                    dataStr += "Total size (MB): " + String.format("%.3f",cld.sizeInBytes/Math.pow(2, 20));


                    cacheDataTextArea.setText(dataStr);

                }
            });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        cacheTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        cacheDataTextArea = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Imagery Cache Info");

        jLabel1.setText("Cache:");

        jLabel2.setText("Info:");

        jButton1.setText("Clean Cache");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        cacheDataTextArea.setColumns(20);
        cacheDataTextArea.setRows(5);
        cacheDataTextArea.setText("Anaylzing Cache.....");
        jScrollPane1.setViewportView(cacheDataTextArea);

        jButton2.setText("Refresh Data");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton1)
                                        .addGap(18, 18, 18)
                                        .addComponent(jButton2))
                                    .addComponent(cacheTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cacheTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
        cacheDataTextArea.setText("Anaylzing Cache.....");
        analyzeCache();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete all the cached imagery?");

        if(result == JOptionPane.OK_OPTION)
        {
            // clean it!
            cacheDataTextArea.setText("Cleaning Cache....");
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    CacheCleaner.cleanupCacheLocation(cld, 0); // last 0 means nothing left behind
                    cacheDataTextArea.append(".. done!");
                }
            });
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                CacheInspectorDialog dialog = new CacheInspectorDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea cacheDataTextArea;
    private javax.swing.JTextField cacheTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
