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


/**
 * the reference ellipsoids
 */
class Ellipsoid {
  /**
   * Clarke's elipsoid (NTF system)
   */
  public static final Ellipsoid clarke = new Ellipsoid(6378249.2, 6356515.0);
  /**
   * Hayford's ellipsoid (ED50 system)
   */
  public static final Ellipsoid hayford =
    new Ellipsoid(6378388.0, 6356911.9461);
  /**
   * WGS84 elipsoid
   */
  public static final Ellipsoid GRS80 = new Ellipsoid(6378137.0, 6356752.314);

  /**
   * half long axis
   */
  public final double a;
  /**
   * half short axis
   */
  public final double b;
  /**
   * first excentricity
   */
  public final double e;
  /**
   * first excentricity squared
   */
  public final double e2;

  /**
   * create a new ellipsoid and precompute its parameters
   *
   * @param a ellipsoid long axis (in meters)
   * @param b ellipsoid short axis (in meters)
   */
  public Ellipsoid(double a, double b) {
    this.a = a;
    this.b = b;
    e2 = (a*a - b*b) / (a*a);
    e = Math.sqrt(e2);
  }
}


