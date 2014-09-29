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
 * The Lambert II extended system is an extenstion of the Lambert II
 * system covering all France. It is continuous over France and Corsica
 * but it introduces more deformations than the Lambert system.
 */
public class LambertIIe extends Lambert {
  /**
   * Lambert II extended projection exponent
   */
  public static final double n = 0.7289686274;
  /**
   * Lambert II extended projection constant
   */
  public static final double c = 11745793.39;
  /**
   * Lambert II extended false east
   */
  public static final double Xs = 600000.0;
  /**
   * Lambert II extended false north
   */
  public static final double Ys = 8199695.768;

  /**
   * initializes a new Lambert II extended coordinate at (0, 0, 0)
   */
  public LambertIIe() {
    super(5, 0.0, 0.0, 0.0);
  }

  /**
   * initializes a new Lambert II extended coordinate
   *
   * @param x east coordinate in meters
   * @param y north coordinate in meters
   * @param z altitude in meters
   */
  public LambertIIe(double x, double y, double z) {
    super(5, x, y, z);
  }

  /**
   * creates a new Lambert II extended coordinate object initialized at
   * the same location than the input WGS84 coordinate
   *
   * @param from WGS84 coordinate to translate
   * @return LambertIIe coordinate object
   */
  public Coordinates create(WGS84 from) {
    // WGS84 geographic => WGS84 cartesian
    Cartesian wgs = new Cartesian(from.longitude(), from.latitude(), from.h(),
				  Ellipsoid.GRS80);
    // WGS84 => Lambert ellipsoide similarity
    wgs.translate(168.0, 60.0, -320.0);

    // Lambert cartesian => Lambert geographic
    Geographic lambert = new Geographic(wgs, Ellipsoid.clarke);

    // check longitude and latitude
    double lt = lambert.lt();
    double lg = lambert.lg();
    if(lt > 1.0471975511965976 || lt < 0.7173303225696694 ||
      lg > 0.1605702911834783 || lg < -0.0770853752964162) {
      System.err.println("out of LambertIIe zone");
      return new LambertIIe();
    }

    // Lambert geographic => Lambert projection 
    ConicProjection proj =
      new ConicProjection(lambert, LambertIIe.Xs, LambertIIe.Ys,
			  LambertIIe.c, LambertIIe.n, Ellipsoid.clarke.e,
			  LambertIIe.lg0);

    return new LambertIIe(proj.east(), proj.north(), /*lambert.h()*/from.h());
  }


  /**
   * Creates a new WGS84 coordinates object initialized at the same location
   * than this Lambert II extended coordinate.
   *
   * @return WGS84 coordinates object
   */
  public WGS84 toWGS84() {
    update();
    /*
     * Lambert projection => Lambert geographic
     */
    ConicProjection proj = new ConicProjection(x, y);
    Geographic geo =
      new Geographic(proj, LambertIIe.Xs, LambertIIe.Ys,
		     LambertIIe.c, LambertIIe.n, Ellipsoid.clarke.e,
		     LambertIIe.lg0, z);

    // Lambert geographic => Lambert cartesian
    Cartesian lambert = new Cartesian(geo, Ellipsoid.clarke);

    // Lambert => WGS84 ellipsoide similarity
    lambert.translate(-168.0, -60.0, 320.0);

    // WGS84 cartesian => WGS84 geographic
    Geographic wgs = new Geographic(lambert, Ellipsoid.GRS80);

    return new WGS84(wgs.lg(), wgs.lt(), /*wgs.h()*/z);
  }


  /**
   * Parse input string and creates a new Lambert II extended Coordinate.
   *
   * @param strings array of 4 strings which are the "LambertIIe" constant,
   * east coordinate, north coordinate, and altitude.
   * @return new Lambert II extended coordinate
   */
  public Coordinates create(String strings[]) throws InvalidCoordinate {
    if(strings.length == 1)
      return new LambertIIe();
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
    return new LambertIIe(x, y, z);
  }

}


