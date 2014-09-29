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

import java.util.LinkedHashMap;

/**
 * list of all existing coordinate systems
 */
public class Coordtable extends LinkedHashMap {
  /**
   * build a hashmap containing all existing coordinate systems
   */
  public Coordtable() {
    super();
    put("None", new NoCoordinates());
    // the keys of all coordinate systems but the special 'NoCoordinate'
    // system should be the base class name
    put("Lambert", new Lambert());
    put("LambertIIe", new LambertIIe());
    put("Lambert93", new Lambert93());
    put("WGS84", new WGS84());
    put("UTM", new UTM());
    put("UTM-ED50", new UTMED50());
    put("CH1903", new CH1903());
  }
}
