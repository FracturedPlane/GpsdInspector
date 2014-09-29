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
 * the ED50 frame (Hayford's Ellipsoid).
 */
public class UTMED50 extends UTM {

  private void init() {
    ellipsoid = Ellipsoid.hayford;
    translation[0] = 84.0;
    translation[1] = 97.0;
    translation[2] = 117.0;
  }

  /**
   * initializes a default Lambert UTM coordinate: (0, 0, 0) zone 30, north
   */
  public UTMED50() {
    super();
    init();
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
  public UTMED50(int zone, double x, double y, double z, boolean north) {
    super(zone, x, y, z, north);
    init();
  }

}



