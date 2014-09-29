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
 * The French Lambert projective system in the NTF frame.
 * <p>To minimize projection errors, the Lambert system is divided
 * in 4 zones covering metropolitan France: I in north of France
 * (latitude 53.5 gr to 57.0 gr), II in middle of France (latitude
 * 50.5 gr to 53.5 gr), III in south of France (latitude 47.0 gr to
 * 50.5 gr) and IV in Corsica (latitude 45.9 gr to 47.8 gr).</p>
 * <p>The reference for geographic coordinates is Clarke's Ellipsoid.</p>
 */
public class Lambert extends Coordinates {
  /**
   * Lambert zone (should be 1 to 4, 5 means LambertIIe and 6 means Lambert93).
   */
  private int zone;
  /**
   * east coordinate in Lambert frame (in meters)
   */
  protected double x;
  /**
   * north coordinate in Lambert frame (in meters)
   */
  protected double y;
  /**
   * altitude (in meters)
   */
  protected double z;

  /**
   * graphic text field to receive zone information
   */
  private JTextField tzone = null;
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

  /**
   * Lambert I, II, III, and IV projection exponents
   */
  public static final double n[] = {
    0.7604059656, 0.7289686274, 0.6959127966, 0.6712679322
  };
  /**
   * Lambert I, II, III, and IV projection constants
   */
  public static final double c[] = {
    11603796.98, 11745793.39, 11947992.52, 12136281.99
  };
  /**
   * Lambert I, II, III, and IV false east
   */
  public static final double Xs[] = {
    600000.0, 600000.0, 600000.0, 234.358
  };
  /**
   * Lambert I, II, III, and IV false north
   */
  public static final double Ys[] = {
    5657616.674, 6199695.768, 6791905.085, 7239161.542
  };
  /**
   * Lambert I, II, III, and IV longitudinal offset to Greenwich meridian
   */
  public static final double lg0 = 0.04079234433198; // 2�20'14.025"

  /**
   * initializes a default Lambert coordinate: (0, 0, 0) zone 1
   */
  public Lambert() {
    zone = 1;
    x = y = z = 0;
  }

  /**
   * initializes a Lambert coordinate
   *
   * @param zone Lambert zone (from 1 to 4)
   * @param x Lambert east coordinate in meters
   * @param y Lambert north coordinate in meters
   * @param x Lambert altitude in meters
   */
  public Lambert(int zone, double x, double y, double z) {
    this.zone = zone;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * read data from graphic widget if needed
   */
  protected void update() {
    if(edited) {
      edited = false;
      if(tzone != null) {
	String str = tzone.getText().trim().toLowerCase();
	if(str.equals("i") || str.equals("1"))
	  zone = 1;
	else if(str.equals("ii") || str.equals("2"))
	  zone = 2;
	else if(str.equals("iii") || str.equals("3"))
	  zone = 3;
	else if(str.equals("iv") || str.equals("4"))
	  zone = 4;
	else
	  zone = 1;
      }
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
   * @return string formated as "Lambert [1-4] east north altitude"
   */
  public String toString() {
    update();
    String res = getName();
    if(zone <= 4)
      res += " " + String.valueOf(zone);
    return res + " " + Coordinates.lengthToString(x) + " " +
      Coordinates.lengthToString(y) + " " + Coordinates.altitudeToString(z);
  }


  /**
   * creates a new Lambert coordinate object initialized at the same location
   * than the input WGS84 coordinate
   *
   * @param from WGS84 coordinate to translate
   * @return Lambert coordinate object
   */
  public Coordinates create(WGS84 from) {
    // WGS84 geographic => WGS84 cartesian
    Cartesian wgs = new Cartesian(from.longitude(), from.latitude(), from.h(),
				  Ellipsoid.GRS80);
    // WGS84 => Lambert ellipsoide similarity
    wgs.translate(168.0, 60.0, -320.0);

    // Lambert cartesian => Lambert geographic
    Geographic lambert = new Geographic(wgs, Ellipsoid.clarke);

    int zone = 0;
    // check longitude and latitude
    double lt = lambert.lt();
    double lg = lambert.lg();
    if(lt > 1.0471975511965976 || lt < 0.7173303225696694 ||
      lg > 0.1605702911834783 || lg < -0.0770853752964162) {
      System.err.println("out of Lambert zone");
      return new Lambert();
    }
    // zone I
    if(lt > 53.5 * Math.PI / 200.0)
      zone = 0;
    // zone II
    else if(lt > 50.5 * Math.PI / 200.0)
      zone = 1;
    // zone III
    else if(lt > 47.0 * Math.PI / 200.0)
      zone = 2;
    // zone IV (warning: zone IV extends from 47.8 to 45.9 degres of
    //          latitude, there is an overlap with zone III that
    //          cannot be solved automatically)
    else if(lt > 45.9 * Math.PI / 200.0)
      zone = 3;

    // Lambert geographic => Lambert projection 
    ConicProjection proj =
      new ConicProjection(lambert, Lambert.Xs[zone], Lambert.Ys[zone],
			  Lambert.c[zone], Lambert.n[zone], Ellipsoid.clarke.e,
			  Lambert.lg0);

    zone++;
    return new Lambert(zone, proj.east(), proj.north(),
		       /*lambert.h()*/from.h());
  }



  /**
   * creates a new WGS84 coordinates object initialized at the same location
   * than this Lambert coordinate.
   *
   * @return WGS84 coordinates object
   */
  public WGS84 toWGS84() {
    update();
    int zone = this.zone - 1;

    /*
     * Lambert projection => Lambert geographic
     */
    ConicProjection proj = new ConicProjection(x, y);
    Geographic geo =
      new Geographic(proj, Lambert.Xs[zone], Lambert.Ys[zone],
		     Lambert.c[zone], Lambert.n[zone], Ellipsoid.clarke.e,
		     Lambert.lg0, z);

    // Lambert geographic => Lambert cartesian
    Cartesian lambert = new Cartesian(geo, Ellipsoid.clarke);

    // Lambert => WGS84 ellipsoide similarity
    lambert.translate(-168.0, -60.0, 320.0);

    // WGS84 cartesian => WGS84 geographic
    Geographic wgs = new Geographic(lambert, Ellipsoid.GRS80);

    return new WGS84(wgs.lg(), wgs.lt(), /*wgs.h()*/z);
  }


  /**
   * Parse input string and creates a new Lambert Coordinate.
   *
   * @param strings array of 5 strings which are the "Lambert" constant, zone
   * number (from 1 to 4), east coordinate, north coordinate, and altitude.
   * @return new Lambert coordinate
   */
  public Coordinates create(String strings[]) throws InvalidCoordinate {
    if(strings.length == 1)
      return new Lambert();
    if(strings.length != 5)
      throw new InvalidCoordinate();
    int zone;
    double x, y, z;
    try {
      zone = Integer.parseInt(strings[1].trim());
      x = Coordinates.parseLength(strings[2]);
      y = Coordinates.parseLength(strings[3]);
      z = Coordinates.parseAltitude(strings[4]);
    }
    catch(NumberFormatException e) {
      throw new InvalidCoordinate();
    }
    return new Lambert(zone, x, y, z);
  }


  /**
   * Creates Lambert coordinate graphic representation
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

    if(zone <= 4) {
      JLabel lzone = new JLabel("zone ");
      c.gridx = 0;
      c.weightx = 0.0;
      layout.setConstraints(lzone, c);
      panel.add(lzone);
      tzone = new JTextField(2);
      switch(zone) {
      case 1:
	tzone.setText("I");
	break;
      case 2:
	tzone.setText("II");
	break;
      case 3:
	tzone.setText("III");
	break;
      case 4:
	tzone.setText("IV");
	break;
      }
      c.gridx = 1;
      c.weightx = 0.0;
      layout.setConstraints(tzone, c);
      panel.add(tzone);
    }
    else
      tzone = null;
    
    JLabel lx = new JLabel(" X=");
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
      if(tzone != null)
	tzone.setEditable(edit);
      tx.setEditable(edit);
      ty.setEditable(edit);
      tz.setEditable(edit);
    }
  }
}


