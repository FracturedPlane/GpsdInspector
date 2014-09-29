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

/* http://www.swisstopo.ch   /  http://topo.epfl.ch/transcoco.php
 * (484000, 74000) -> 5�56'47.5, 45�48'27.8 / 5�56'46.79, 45�48'27.5
 * (550000, 100000) -> 6�47'33.6, 46�02'58.8 / 6�47'33.6, 46�02'58.82
 * (600000, 150000) -> 7�26'19.1, 46�30'04.7 / 7�26'19.08, 46�30'04.72
 * 5�70', 45�50' -> (501159, 76546) / (501158.76, 76546.18)
 * 6�50', 46�25' -> (553463, 140770) / (553462.88, 140770.46)
 * 7�25', 46�30' -> (598314, 149855) / (598313.65, 149854.51)
 */


/**
 * The Swiss projective system.
 */
public class CH1903 extends Coordinates {
  /**
   * east coordinate in Swiss projection (in meters)
   */
  protected double x;
  /**
   * north coordinate in Swiss projection (in meters)
   */
  protected double y;
  /**
   * altitude (in meters)
   */
  protected double z;

  /**
   * graphic text field to receive east coordinate value
   */
  protected JTextField tx = null;
  /**
   * graphic text field to receive north coordinate value
   */
  protected JTextField ty = null;
  /**
   * graphic text field to receive altitude value
   */
  protected JTextField tz = null;

  /**
   * whether the text fields may have been edited by the user
   */
  protected boolean edited = false;

  public static final double a = 6377397.155;
  public static final double b = 6356078.962822;
  public static final double phi0 = 46.9524055 * Math.PI / 180.0;
  public static final double lambda0 = 7.4395833 * Math.PI / 180.0;
  public static final double E2 = (a*a - b*b) / (a*a);
  public static final double E = Math.sqrt(E2);
  public static final double R  = a * Math.sqrt(1.0 - E2) /
    (1.0 - E2 * Math.sin(phi0) * Math.sin(phi0));
  public static final double alpha = Math.sqrt(1.0 + E2 / (1.0 - E2) *
					       Math.pow(Math.cos(phi0), 4.0));
  public static final double b0= Math.asin(Math.sin(phi0) / alpha);
  public static final double K = Math.log(Math.tan(Math.PI/4.0 + b0/2.0))
    - alpha * Math.log(Math.tan(Math.PI/4.0 + phi0/2.0))
    + alpha * E/2.0 * Math.log((1.0 + E*Math.sin(phi0)) /
			       (1.0 - E*Math.sin(phi0)));

  /**
   * initializes a default Swiss coordinate: (0, 0, 0)
   */
  public CH1903() {
    x = 484000.0;
    y = 74000.0;
    z = 0;
  }

  /**
   * initializes a Swiss coordinate
   *
   * @param x Swiss east coordinate in meters
   * @param y Swiss north coordinate in meters
   * @param x Swiss altitude in meters
   */
  public CH1903(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
    check();
  }

  
  /**
   * print an error message if the coordinates fall out of the swiss zone
   */
  void check() {
    if(x < 484000.0 || x > 834000.0 || y < 74000.0 || y > 296000.0) {
      System.err.println("out of Swiss zone (484000 <= east <= 834000, 74000 <= nord <= 296000)");
    }
  }


  /**
   * read data from graphic widget if needed
   */
  protected void update() {
    if(edited) {
      edited = false;
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
      check();
    }
  }

  /**
   * returns this coordinate as a string
   * 
   * @return string formated as "Swiss east north altitude"
   */
  public String toString() {
    update();
    return getName() + " " + Coordinates.lengthToString(x) + " " +
      Coordinates.lengthToString(y) + " " + Coordinates.altitudeToString(z);
  }


  /**
   * creates a new Swiss coordinate object initialized at the same location
   * than the input WGS84 coordinate
   *
   * @param from WGS84 coordinate to translate
   * @return Swiss coordinate object
   */
  public Coordinates create(WGS84 from) {
    /*
    double phi = from.latitude();
    double lambda = from.longitude();

    double S = -alpha * Math.log(Math.tan(Math.PI/4.0 - phi/2.0))
      - alpha*E/2.0 * Math.log((1.0 + E*Math.sin(phi))/(1.0 - E*Math.sin(phi)))
      + K;
    double b = 2.0 * (Math.atan(Math.exp(S)) - Math.PI/4.0);
    double l = alpha * (lambda - lambda0);
    double l_ = Math.atan(Math.sin(l) / (Math.sin(b0)*Math.tan(b) +
					 Math.cos(b0)*Math.cos(l)));
    double b_ = Math.asin(Math.cos(b0)*Math.sin(b) -
			  Math.sin(b0)*Math.cos(b)*Math.cos(l));
    double Y = R * l_;
    double X = R / 2.0 * Math.log((1.0 + Math.sin(b_))/(1.0 - Math.sin(b_)));

    return new CH1903(600000.0 + Y, 200000.0 + X, from.h());
    */
    double phi = (from.latitude() * 180.0 / Math.PI) * 3600.0;
    double lambda = (from.longitude() * 180.0 / Math.PI) * 3600.0;

    phi = (phi - 169028.66) / 10000.0;
    lambda = (lambda - 26782.5) / 10000.0;
    double phi2 = phi * phi;
    double phi3 = phi2 * phi;
    double lambda2 = lambda * lambda;
    double lambda3 = lambda2 * lambda;
    double y = 600072.37 + 211455.93 * lambda - 10938.51 * lambda * phi
      - 0.36 * lambda * phi2 - 44.54 * lambda3;
    double x = 200147.07 + 308807.95 * phi + 3745.25 * lambda2 + 76.63 * phi2
      - 194.56 * lambda2 * phi + 119.79 * phi3;

    return new CH1903(y, x, from.h());
  }



  /**
   * creates a new WGS84 coordinates object initialized at the same location
   * than this Swiss coordinate.
   *
   * @return WGS84 coordinates object
   */
  public WGS84 toWGS84() {
    update();
    /*
    double X = y - 200000.0;
    double Y = x - 600000.0;

    double l_ = Y/R;
    double b_ = 2.0 * (Math.atan(Math.exp(X/R)) - Math.PI/4.0);
    double b = Math.asin(Math.cos(b0)*Math.sin(b_) +
			 Math.sin(b0)*Math.cos(b_)*Math.cos(l_));
    double l = Math.atan(Math.sin(l_) / (Math.cos(b0)*Math.cos(l_) -
					 Math.sin(b0)*Math.tan(b_)));
    double lambda = lambda0 + l/alpha;

    double phi = b;
    double prev_phi = phi;
    int i = 0;
    do {
      i++;
      prev_phi = phi;
      double S = + 1.0/alpha * (Math.log(Math.tan(Math.PI/4.0 + b/2.0)) - K)
	+ E * Math.log(Math.tan(Math.PI/4.0 + Math.asin(E*Math.sin(phi))/2.0));
      phi = 2.0 * Math.atan(Math.exp(S)) - Math.PI/2.0;
    }
    while(prev_phi != phi && i < 10);

    return new WGS84(lambda, phi, z);
    */

    double y = (this.x - 600000.0) / 1000000.0;
    double y2 = y * y;
    double y3 = y2 * y;
    double x = (this.y - 200000.0) / 1000000.0;
    double x2 = x * x;
    double x3 = x2 * x;
    double lambda = 2.6779094 + 4.728982 * y + 0.791484 * y * x
      + 0.1306 * y * x2 - 0.0436 * y3;
    double phi = 16.9023892 + 3.238272 * x - 0.270978 * y2 - 0.002528 * x2
      - 0.0447 * y2 * x - 0.014 * x3;
    
    return new WGS84(lambda * 100.0 * Math.PI / 6480.0,
		     phi * 100.0 * Math.PI / 6480.0, z);
  }


  /**
   * Parse input string and creates a new Swiss Coordinate.
   *
   * @param strings array of 5 strings which are the "Swiss" constant, zone
   * number (from 1 to 4), east coordinate, north coordinate, and altitude.
   * @return new Swiss coordinate
   */
  public Coordinates create(String strings[]) throws InvalidCoordinate {
    if(strings.length == 1)
      return new CH1903();
    if(strings.length != 4)
      throw new InvalidCoordinate();
    double x, y, z;
    try {
      x = Coordinates.parseLength(strings[1]);
      y = Coordinates.parseLength(strings[2]);
      z = Coordinates.parseAltitude(strings[3]);
    }
    catch(NumberFormatException e) {
      throw new InvalidCoordinate();
    }
    return new CH1903(x, y, z);
  }


  /**
   * Creates Swiss coordinate graphic representation
   * 
   * @param panel parent window to create wigets in
   */
  public void editor(JPanel panel) {
    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    panel.setLayout(layout);

    JLabel lx = new JLabel(" Y=");
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.0;
    layout.setConstraints(lx, c);
    panel.add(lx);
    tx = new JTextField(Coordinates.lengthToString(x));
    c.gridx = 2;
    c.weightx = 1.0;
    layout.setConstraints(tx, c);
    panel.add(tx);
    
    JLabel ly = new JLabel(" X=");
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 0.0;
    layout.setConstraints(ly, c);
    panel.add(ly);
    ty = new JTextField(Coordinates.lengthToString(y));
    c.gridx = 2;
    c.weightx = 1.0;
    layout.setConstraints(ty, c);
    panel.add(ty);
    
    JLabel lz = new JLabel(" Z=");
    c.gridx = 1;
    c.gridy = 2;
    c.weightx = 0.0;
    layout.setConstraints(lz, c);
    panel.add(lz);
    tz = new JTextField(Coordinates.altitudeToString(z));
    c.gridx = 2;
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
      tx.setEditable(edit);
      ty.setEditable(edit);
      tz.setEditable(edit);
    }
  }
}



