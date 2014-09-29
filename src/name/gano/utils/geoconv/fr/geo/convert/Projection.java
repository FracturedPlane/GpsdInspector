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
 * Base class for projected coordinates of a point:
 * (East, North) pair expressed in meters.
 */
class Projection {
  /**
   * X coordinate (east from origin) in meters 
   */
  protected double east;

  /**
   * Y coordinate (north from origin) in meters 
   */
  protected double north;

  /**
   * Creates point (0, 0)
   */
  public Projection() {
    east = 0.0;
    north = 0.0;
  }

  /**
   * initializes new projection coordinates
   *
   * @param east east from origin in meters 
   * @param north north from origin in meters 
   */
  public Projection(double east, double north) {
    this.east = east;
    this.north = north;
  }

  /**
   * returns east coordinate (in meters)
   */
  public double east() {
    return east;
  }

  /**
   * returns north coordinate (in meters)
   */
  public double north() {
    return north;
  }
}


/**
 * Conic projection of a point.
 */
class ConicProjection extends Projection {
  /**
   * initializes new projection coordinates
   *
   * @param east east from origin in meters 
   * @param north north from origin in meters 
   */
  public ConicProjection(double east, double north) {
    super(east, north);
  }


  /**
   * initalizes from geographic coordinates
   *
   * @param coord geographic coordinates triplet
   * @param Xs false east (coordinate system origin) in meters
   * @param Ys false north (coordinate system origin) in meters
   * @param c projection constant
   * @param n projection exponent
   * @param e reference ellipsoid excentricity
   * @param lg0 longitude of origin wrt to the Greenwich meridian (in radian)
   */
  public ConicProjection(Geographic coord, double Xs, double Ys,
			 double c, double n, double e, double lg0) {
    double eslt = e * Math.sin(coord.lt());
    double l = Math.log(Math.tan(Math.PI/4.0 + coord.lt()/2.0) *
			Math.pow((1.0 - eslt)/(1.0 + eslt), e/2.0));
    east = Xs + c * Math.exp(-n * l) * Math.sin(n * (coord.lg() - lg0));
    north = Ys - c * Math.exp(-n * l) * Math.cos(n * (coord.lg() - lg0));
  }
}



