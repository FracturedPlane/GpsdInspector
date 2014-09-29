/*
 * fr.geo.convert package
 * A geographic coordinates converter.
 * Copyright (C) 2002 Johan Montagnat
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * Johan Montagnat
 * johan@creatis.insa-lyon.fr
 */

package name.gano.utils.geoconv.fr.geo.convert;

import javax.swing.*;
import java.awt.*;


/**
 * The UTM (Universal Transverse Mercator) projective system in
 * the GRS84 frame ().
 */
public class UTM extends Coordinates {
  /**
   * Mercator zone (from 1 to 60). France is covered by zones 30 to 32.
   */
  private int zone;
  /**
   * east coordinate in UTM frame (in meters)
   */
  private double x;
  /**
   * north coordinate in UTM frame (in meters)
   */
  private double y;
  /**
   * altitude (in meters)
   */
  private double z;
  /**
   * whether north or south hemisphere
   */
  private boolean north;

  /**
   * graphic text field to receive zone information
   */
  private JTextField tzone = null;
  /**
   * graphic text field to receive east coordinate value
   */
  private JTextField tx = null;
  /**
   * graphic text field to receive north coordinate value
   */
  private JTextField ty = null;
  /**
   * graphic text field to receive altitude value
   */
  private JTextField tz = null;
  /**
   * graphic widget to select north or south coordinate
   */
  private JComboBox tns = null;

  /**
   * whether the text fields may have been edited by the user
   */
  private boolean edited = false;

  /**
   * reference ellipsoid
   */
  protected Ellipsoid ellipsoid = null; 

  /**
   * WGS84 => reference ellipsoid translation
   */
  protected double translation[] = { 0.0, 0.0, 0.0 };

  /**
   * initializes a default Lambert UTM coordinate: (0, 0, 0) zone 30, north
   */
  public UTM() {
    north = true;
    zone = 30;
    x = y = z = 0;
    ellipsoid = Ellipsoid.GRS80;
  }

  /**
   * initializes a UTM coordinate
   *
   * @param zone UTM zone (from 1 to 60)
   * @param x UTM east coordinate in meters
   * @param y UTM north coordinate in meters
   * @param x UTM altitude in meters
   * @param north true if in north hemisphere, false in south hemisphere
   */
  public UTM(int zone, double x, double y, double z, boolean north) {
    this.zone = zone;
    this.x = x;
    this.y = y;
    this.z = z;
    this.north = north;
    ellipsoid = Ellipsoid.GRS80;
  }


  /**
   * read data from graphic widget if needed
   */
  protected void update() {
    if(edited) {
      edited = false;
      try {
	zone = Integer.parseInt(tzone.getText().trim());
      }
      catch(NumberFormatException e1) {
	zone = 30;
      }
      north = tns.getSelectedItem().toString().equals("N");
      try {
	x = Coordinates.parseLength(tx.getText());
      }
      catch(NumberFormatException e1) {
	x = 0;
      }
      try {
	y = Coordinates.parseLength(ty.getText());
      }
      catch(NumberFormatException e2) {
	y = 0;
      }
      try {
	z = Coordinates.parseAltitude(tz.getText());
      }
      catch(NumberFormatException e2) {
	z = 0;
      }
    }
  }

  /**
   * returns this coordinate as a string
   * 
   * @return string formated as "UTM <1-60> <N|S> east north altitude"
   */
  public String toString() {
    update();
    String res = getName() + " " + String.valueOf(zone) + " ";
    if(north)
      res += "N ";
    else
      res += "S ";
    return res + Coordinates.lengthToString(x) + " " +
      Coordinates.lengthToString(y) + " " + Coordinates.altitudeToString(z);
  }


  /**
   * creates a new UTM coordinate object initialized at the same location
   * than the input WGS84 coordinate
   *
   * @param from WGS84 coordinate to translate
   * @return UTM coordinate object
   */
  public Coordinates create(WGS84 from) {
    // WGS84 geographic => WGS84 cartesian
    Cartesian wgs = new Cartesian(from.longitude(), from.latitude(), from.h(),
				  Ellipsoid.GRS80);
    // WGS84 => reference ellipsoid similarity
    wgs.translate(translation[0], translation[1], translation[2]);

    // reference ellipsoid cartesian => reference ellipsoid geographic
    Geographic ref = new Geographic(wgs, ellipsoid);

    // reference ellipsoid geographic => UTM projection 
    MTProjection proj = new MTProjection(ref, ellipsoid.a, ellipsoid.e);

    return new UTM(proj.zone(), proj.east(), proj.north(), /*ref.h()*/from.h(),
		   proj.isNorth());
  }



  /**
   * creates a new WGS84 coordinates object initialized at the same location
   * than this UTM coordinate.
   *
   * @return WGS84 coordinates object
   */
  public WGS84 toWGS84() {
    update();

    /*
     * UTM projection => reference ellipsoid geographic
     */
    MTProjection proj = new MTProjection(x, y, zone, north);
    Geographic geo = new Geographic(proj, ellipsoid.a, ellipsoid.e, z);

    // reference ellipsoid geographic => reference ellipsoid cartesian
    Cartesian utm = new Cartesian(geo, ellipsoid);

    // reference ellipsoid => WGS84 ellipsoide similarity
    utm.translate(-translation[0], -translation[1], -translation[2]);

    // WGS84 cartesian => WGS84 geographic
    Geographic wgs = new Geographic(utm, Ellipsoid.GRS80);

    return new WGS84(wgs.lg(), wgs.lt(), /*wgs.h()*/z);
  }


  /**
   * Parse input string and creates a new UTM Coordinate.
   *
   * @param strings array of 6 strings which are the "UTM" constant, zone
   * number (from 1 to 60), N or S letter, east coordinate, north coordinate,
   * and altitude.
   * @return new UTM coordinate
   */
  public Coordinates create(String strings[]) throws InvalidCoordinate {
    if(strings.length == 1)
      return new UTM();
    if(strings.length != 6)
      throw new InvalidCoordinate();
    int zone;
    boolean north;
    double x, y, z;
    try {
      zone = Integer.parseInt(strings[1].trim());
      north = strings[2].trim().toUpperCase().equals("N");
      x = Coordinates.parseLength(strings[3]);
      y = Coordinates.parseLength(strings[4]);
      z = Coordinates.parseAltitude(strings[5]);
    }
    catch(NumberFormatException e) {
      throw new InvalidCoordinate();
    }
    return new UTM(zone, x, y, z, north);
  }


  /**
   * Creates UTM coordinate graphic representation
   * 
   * @param panel parent window to create wigets in
   */
  public void editor(JPanel panel) {
    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    panel.setLayout(layout);
    c.gridy = 0;

    JLabel lzone = new JLabel("zone ");
    c.gridx = 0;
    c.weightx = 0.0;
    layout.setConstraints(lzone, c);
    panel.add(lzone);
    tzone = new JTextField(2);
    tzone.setText(String.valueOf(zone));
    c.gridx = 1;
    c.weightx = 0.0;
    layout.setConstraints(tzone, c);
    panel.add(tzone);
    
    JLabel lhemi = new JLabel("hemisphere ");
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.0;
    layout.setConstraints(lhemi, c);
    panel.add(lhemi);
    tns = new JComboBox();
    tns.addItem("N");
    tns.addItem("S");
    tns.setSelectedItem(north ? "N" : "S");
    c.gridx = 1;
    c.weightx = 0.0;
    layout.setConstraints(tns, c);
    panel.add(tns);
    
    JLabel lx = new JLabel(" X=");
    c.gridy = 0;
    c.gridx = 2;
    c.weightx = 0.0;
    layout.setConstraints(lx, c);
    panel.add(lx);
    tx = new JTextField(Coordinates.lengthToString(x));
    c.gridx = 3;
    c.weightx = 1.0;
    layout.setConstraints(tx, c);
    panel.add(tx);
    
    JLabel ly = new JLabel(" Y=");
    c.gridx = 2;
    c.gridy = 1;
    c.weightx = 0.0;
    layout.setConstraints(ly, c);
    panel.add(ly);
    ty = new JTextField(Coordinates.lengthToString(y));
    c.gridx = 3;
    c.weightx = 1.0;
    layout.setConstraints(ty, c);
    panel.add(ty);
    
    JLabel lz = new JLabel(" Z=");
    c.gridx = 2;
    c.gridy = 2;
    c.weightx = 0.0;
    layout.setConstraints(lz, c);
    panel.add(lz);
    tz = new JTextField(Coordinates.altitudeToString(z));
    c.gridx = 3;
    c.weightx = 1.0;
    layout.setConstraints(tz, c);
    panel.add(tz);
    
    setEditable(false);
  }


  /**
   * toggle the editable property of the graphic widgets
   *
   * @param edit wheter to toggle on or off
   */
  public void setEditable(boolean edit) {
   if(tx != null) {
      if(edit)
	edited = true;
      tzone.setEditable(edit);
      tx.setEditable(edit);
      ty.setEditable(edit);
      tz.setEditable(edit);
      tns.setEnabled(edit);
    }
  }
}


