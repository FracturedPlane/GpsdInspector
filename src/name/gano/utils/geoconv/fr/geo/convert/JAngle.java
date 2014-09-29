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
 * graphic widget to display angles in the (degre, minute, second) format
 */
class JAngle extends JPanel {
  /**
   * text field to display degres
   */
  private JTextField deg;
  /**
   * text field to display minutes
   */
  private JTextField mn = null;
  /**
   * text field to display seconds
   */
  private JTextField sec = null;

  /**
   * create a new graphic widget from an angle value (in radian)
   */
  public JAngle(double angle) {
    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.EAST;
    c.fill = GridBagConstraints.BOTH;
    setLayout(layout);
    c.gridy = 0;

    JLabel ldeg = null;
    int unit = Coordinates.angleUnit();
    double d;
    switch(unit) {
    case Coordinates.RADIAN:
      deg = new JTextField(12);
      ldeg = new JLabel("rad");
      deg.setText(String.valueOf(Math.round(angle * 10000000000.0) /
		  10000000000.0));
      break;
    case Coordinates.DEGRE:
      deg = new JTextField(14);
      ldeg = new JLabel("�");
      deg.setText(String.valueOf(Math.round(180.0 * angle / Math.PI *
					    10000000000.0) / 10000000000.0));
      break;
    case Coordinates.DEGMN:
    case Coordinates.DEGMNSEC:
      deg = new JTextField(3);
      String sgn = "";
      if(angle < 0.0) {
	angle = -angle;
	sgn = "-";
      }
      angle = 180.0 * angle / Math.PI;
      deg.setText(sgn + String.valueOf((int) angle));
      deg.setHorizontalAlignment(JTextField.RIGHT);
      ldeg = new JLabel("�");
      break;
    }
    c.gridx = 0;
    c.weightx = 0.0;

    layout.setConstraints(deg, c);
    add(deg);
    c.gridx = 1;
    c.weightx = 0.0;
    layout.setConstraints(ldeg, c);
    add(ldeg);

    if(unit == Coordinates.DEGMN || unit == Coordinates.DEGMNSEC) {
      mn = new JTextField((unit == Coordinates.DEGMN) ? 8 : 2);
      mn.setHorizontalAlignment(JTextField.RIGHT);
      c.gridx = 2;
      c.weightx = 0.0;
      layout.setConstraints(mn, c);
      add(mn);
      JLabel lmn = new JLabel("'");
      c.gridx = 3;
      c.weightx = 0.0;
      layout.setConstraints(lmn, c);
      add(lmn);
      angle -= (int) angle;
      angle *= 60.0;
      
      if(unit == Coordinates.DEGMNSEC) {
	mn.setText(String.valueOf((int) angle));
	angle -= (int) angle;
	angle *= 60.0;
	sec = new JTextField((unit == Coordinates.DEGMN) ? 5 : 6);
	sec.setHorizontalAlignment(JTextField.RIGHT);
	c.gridx = 4;
	c.weightx = 0.0;
	layout.setConstraints(sec, c);
	add(sec);
	JLabel lsec = new JLabel("\"");
	c.gridx = 5;
	c.weightx = 0.0;
	layout.setConstraints(lsec, c);
	add(lsec);
	sec.setText(String.valueOf(Math.round(angle * 10000.0) / 10000.0));
      }
      else {
	mn.setText(String.valueOf(Math.round(angle * 1000000.0) / 1000000.0));
      }
    }
  }


  /**
   * return the stored angle value
   */
  public double getAngle() {
    try {
      switch(Coordinates.angleUnit()) {
      case Coordinates.RADIAN:
	return Double.parseDouble(deg.getText().trim());
      case Coordinates.DEGRE:
	return Math.PI * Double.parseDouble(deg.getText().trim()) / 180.0;
      case Coordinates.DEGMN:
	String d = deg.getText().trim();
	if(d.length() == 0)
	  d = "0";
	String m = mn.getText().trim();
	if(m.length() == 0)
	  m = "0";
	return Coordinates.parseAngle(d + "�" + m + "'");
      case Coordinates.DEGMNSEC:
	d = deg.getText().trim();
	if(d.length() == 0)
	  d = "0";
	m = mn.getText().trim();
	if(m.length() == 0)
	  m = "0";
	String s = sec.getText().trim();
	if(s.length() == 0)
	  s = "0";
	return Coordinates.parseAngle(d + "�" + m + "'" + s + "\"");
      }
    }
    catch(NumberFormatException e) {
    }
    return 0.0;
  }

  /**
   * toggle the editable property of the graphic widgets
   *
   * @param edit wheter to toggle on or off
   */
  public void setEditable(boolean edit) {
    deg.setEditable(edit);
    if(mn != null)
      mn.setEditable(edit);
    if(sec != null)
      sec.setEditable(edit);
  }
}


