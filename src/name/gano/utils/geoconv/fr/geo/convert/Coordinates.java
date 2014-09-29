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
import java.util.Iterator;


/**
 * The abstract base class for all coordinate systems. A Coordinate
 * object represents a point in a given coordinate frame.
 * <p>It is
 * possible to convert each coordinate into a WGS84 frame
 * (<code>toWGS84</code> method) or from a WGS84 frame
 * (<code>create(WGS84)</code> method). This central format allow
 * conversion from any system to any other system.</p>
 * <p> A
 * coordinate may also be exported as a string (<code>toString</code>
 * method) or read from an array of strings
 * (<code>create(String[])</code> method) which is obtained by
 * splitting the toString output.</p>
 * <p> Finally, the coordinate
 * system may be displayed and edited in a graphic interface using the
 * <code>editor</code> method</p>
 */
public abstract class Coordinates {
  public static final int METER = 0;
  public static final int KILOMETER = 1;
  public static final int RADIAN = 0;
  public static final int DEGRE = 1;
  public static final int DEGMN = 2;
  public static final int DEGMNSEC = 3;

  protected static int lengthUnit = METER;
  protected static int angleUnit = RADIAN;

  public static int lengthUnit() {
    return lengthUnit;
  }

  public static int angleUnit() {
    return angleUnit;
  }

  public static void setLengthUnit(int unit) {
    if(unit == KILOMETER)
      lengthUnit = KILOMETER;
    else
      lengthUnit = METER;
  }

  public static void setAngleUnit(int unit) {
    angleUnit = unit;
    if(unit < 0 || unit > DEGMNSEC)
      unit = RADIAN;
  }

  public static double parseLength(String str) throws NumberFormatException {
    double v = Double.parseDouble(str.trim());
    if(lengthUnit == KILOMETER)
      return v * 1000.0;
    return v;
  }
  

  public static double parseAltitude(String str) throws NumberFormatException {
    return Double.parseDouble(str.trim());
  }

  
  public static double parseAngle(String str) throws NumberFormatException {
    int i, j, k;
    double v;
    String s = str.trim();

    switch(angleUnit) {
    case RADIAN:
      return Double.parseDouble(s);
    case DEGRE:
      return Math.PI * Double.parseDouble(s) / 180.0;
    case DEGMN:
      i = s.indexOf('�');
      if(i == -1)
	return 0.0;
      v = Integer.parseInt(s.substring(0, i));
      j = s.indexOf('\'');
      if(j != -1) {
	double d =  Double.parseDouble(s.substring(i + 1, j)) / 60.0;
	if(v >= 0.0)
	  v += d;
	else
	  v -= d;
      }
      return Math.PI * v / 180.0;
    case DEGMNSEC:
      i = s.indexOf('�');
      if(i == -1)
	return 0.0;
      v = Integer.parseInt(s.substring(0, i));
      j = s.indexOf('\'');
      if(j != -1) {
	double d =  Integer.parseInt(s.substring(i + 1, j)) / 60.0;
	if(v >= 0.0)
	  v += d;
	else
	  v -= d;
	k = s.indexOf('\"');
	if(k != -1) {
	d = Double.parseDouble(s.substring(j + 1, k)) / 3600.0;
	if(v >= 0.0)
	  v += d;
	else
	  v -= d;
	}
      }
      return Math.PI * v / 180.0;
    }
    return 0.0;
  }


  public static String lengthToString(double length) {
    if(lengthUnit == KILOMETER)
      return String.valueOf(Math.round(length) / 1000.0);
    return String.valueOf(Math.round(length));
  }


  public static String altitudeToString(double length) {
    return String.valueOf(Math.round(length));
  }


  public static String angleToString(double angle) {
    if(angleUnit == RADIAN)
      return String.valueOf(angle);
    double v = angle * 180.0 / Math.PI;
    if(angleUnit == DEGRE)
      return String.valueOf(v);
    String res;
    if(v < 0) {
      res = "-";
      v = -v;
    }
    else
      res = "";
    int d = (int) Math.floor(v);
    res += String.valueOf(d);
    res += '�';
    v -= d;
    v *= 60.0;
    if(angleUnit == DEGMN) {
      res += String.valueOf(Math.round(v * 100000.0) / 100000.0);
      res += '\'';
      return res;
    }
    d = (int) Math.floor(v);
    res += String.valueOf(d);
    res += '\'';
    v -= d;
    v *= 60.0;
    res += String.valueOf(Math.round(v * 1000.0) / 1000.0);
    res += '\"';
    return res;
  }

  

  /**
   * table of all existing coordinate systems
   */
  protected static Coordtable table = new Coordtable();

  /**
   * create a new coordinate object from its string representation
   *
   * @param read string in the format produced by the Coordinate.toString
   * method
   */
  public static Coordinates create(String read) {
    String strings[] = read.split(" +");
    if(strings == null || strings.length < 1)
      return new NoCoordinates();
    Coordinates coord = (Coordinates) table.get(strings[0]);
    if(coord == null)
      return new NoCoordinates();
    try {
      return coord.create(strings);
    }
    catch(InvalidCoordinate e) {
      return new NoCoordinates();
    }
  }

  /**
   * create a new coordinate object from its name and with given coordinates
   *
   * @param name coordinate system name as returned by Coordinate.getName
   * @param from reference coordinates in WGS84 frame
   */
  public static Coordinates create(String name, WGS84 from) {
    Coordinates coord = (Coordinates) table.get(name);
    if(coord == null)
      new NoCoordinates();
    return coord.create(from);
  }

  /**
   * returns an iterator initiazed at the beginning of systems table
   */
  public static Iterator systems() {
    return table.keySet().iterator();
  }

  /**
   * returns a unique identifier (base class name)
   */
  public String getName() {
    String str[] = getClass().getName().split("\\.");
    return str[str.length-1];
  }

  /**
   * each coordinate system should be able to output a string representing
   * this point in this frame
   */
  public abstract String toString();

  /**
   * each coordinate system should be able to create a new coordinate
   * from an array of words
   *
   * @param strings array words which concatenation produces the toString string
   */
  public abstract Coordinates create(String strings[])
    throws InvalidCoordinate;

  /**
   * each coordinate system should be able to create a new coordinate
   * converted from a reference WGS84 coordinate
   *
   * @param from reference WGS84 coordinate
   */
  public abstract Coordinates create(WGS84 from);

  /**
   * each coordinate system should be able to create a new WGS84 coordinate
   * which represent the same point
   */
  public abstract WGS84 toWGS84();

  /**
   * each coordinate system should be able to produce graphic widgets for its
   * display/edition in a GUI
   *
   * @param panel top window in which creating the widgets
   */
  public abstract void editor(JPanel panel);


  /**
   * each coordinate system should be able to toggle the editable property
   * of its widgets on and off
   *
   * @param edit toogle edition on or off
   */
  public abstract void setEditable(boolean edit);
}
