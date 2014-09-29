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
 * The Lambert 93 system is the last projective system defined by IGN for
 * France. As Lambert II extended, it covers whole France. This system
 * is not reported on IGN maps yet.
 */
public class Lambert93 extends Lambert {
  /**
   * Lambert93 projection exponent
   */
  public static final double n = 0.725607765;
  /**
   * Lambert93 projection constant
   */
  public static final double c = 11754255.426;
  /**
   * Lambert93 projection false east
   */
  public static final double Xs = 700000.0;
  /**
   * Lambert93 projection false north
   */
  public static final double Ys = 12655612.05;
  /**
   * Lambert93 offset to Greenwich meridian
   */
  public static final double lg0 = 0.05235987755982988730; // 3�00'00"

  /**
   * initializes a new Lambert93 coordinate at (0, 0, 0)
   */
  public Lambert93() {
    super(6, 0.0, 0.0, 0.0);
  }

  /**
   * initializes a new Lambert93 coordinate
   *
   * @param x east coordinate in meters
   * @param y north coordinate in meters
   * @param z altitude in meters
   */
  public Lambert93(double x, double y, double z) {
    super(6, x, y, z);
  }

  /**
   * creates a new Lambert93 coordinate object initialized at the same location
   * than the input WGS84 coordinate
   *
   * @param from WGS84 coordinate to translate
   * @return Lambert93 coordinate object
   */
  public Coordinates create(WGS84 from) {
    // WGS84 geographic (= Lambert93 geographic)
    Geographic lambert =
      new Geographic(from.longitude(), from.latitude(), from.h());

    // check longitude and latitude
    double lt = lambert.lt();
    double lg = lambert.lg();
    if(lt > 1.0471975511965976 || lt < 0.7173303225696694 ||
      lg > 0.1605702911834783 || lg < -0.0770853752964162) {
      System.err.println("out of Lambert93 zone");
      return new LambertIIe();
    }

    // Lambert93 geographic -> Lambert93 projection
    ConicProjection proj =
      new ConicProjection(lambert, Lambert93.Xs, Lambert93.Ys,
			  Lambert93.c, Lambert93.n, Ellipsoid.GRS80.e,
			  Lambert93.lg0);

    return new Lambert93(proj.east(), proj.north(), /*lambert.h()*/from.h());
  }


  /**
   * Creates a new WGS84 coordinates object initialized at the same location
   * than this Lambert93 coordinate.
   *
   * @return WGS84 coordinates object
   */
  public WGS84 toWGS84() {
    update();
    /*
     * Lambert93 projection => Lambert93 geographic (= WGS84 geographic)
     */
    ConicProjection proj = new ConicProjection(x, y);
    Geographic geo =
      new Geographic(proj, Lambert93.Xs, Lambert93.Ys,
		     Lambert93.c, Lambert93.n, Ellipsoid.GRS80.e,
		     Lambert93.lg0, z);

    return new WGS84(geo.lg(), geo.lt(), /*geo.h()*/z);
  }


  /**
   * Parse input string and creates a new Lambert93 Coordinate.
   *
   * @param strings array of 4 strings which are the "Lambert93" constant, east
   * coordinate, north coordinate, and altitude.
   * @return new Lambert93 coordinate
   */
  public Coordinates create(String strings[]) throws InvalidCoordinate {
    if(strings.length == 1)
      return new Lambert93();
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
    return new Lambert93(x, y, z);
  }

}
