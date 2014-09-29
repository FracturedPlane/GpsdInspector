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
 * a particular case of coordinate: the 'empty coordinate system' for
 * unknown coordinates.
 */
public class NoCoordinates extends Coordinates {
  /**
   * empty coordinate name in None
   */
  public static final String id = "None";

  /**
   * empty constructor
   */
  public NoCoordinates() {
  }

  /**
   * return "None"
   */
  public String getName() {
    return new String(id);
  }

  /**
   * returns an empty string
   */
  public String toString() {
    return "";
  }

  /**
   * returns an empty coordinate
   */
  public Coordinates create(WGS84 from) {
    return new NoCoordinates();
  }

  /**
   * returns a WGS84 coordinate initialized in (0.0, 0.0, 0.0)
   */
  public WGS84 toWGS84() {
    return new WGS84(0.0, 0.0, 0.0);
  }

  /**
   * returns an empty coordinate
   */
  public Coordinates create(String strings[]) {
    return new NoCoordinates();
  }

  /**
   * creates an empty panel
   */
  public void editor(JPanel panel) {
    panel.setLayout(new BorderLayout());
    panel.add(new JPanel(), BorderLayout.CENTER);
  }
  
  /**
   * does nothing
   */
  public void setEditable(boolean edit) {
  }
}
