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
import java.awt.event.*;
import java.util.Vector;
import java.util.Iterator;
import java.io.*;




/**
 * Main class to convert geographic coordinates.
 */
public class GeoConvert {
  private JFrame top;
  private JComboBox srcsystems;
  private JComboBox dstsystems;
  private JPanel srcpanel;
  private JPanel dstpanel;
  private Coordinates srccoord;
  private Coordinates dstcoord;

  /**
   * read command line arguments and convert coordinates from command line,
   * file, stdinput, or launch a GUI.
   */
  public static void main(String args[]) {
    // graphic interface
    if(args.length == 0) {
      new GeoConvert();
    }
    // file name
    else {
      int left;
      int start = 0;
      Coordinates out = null;
      String inconv = null;
      String outconv = null;
      String separator = " ";
      Translator intr = null;
      Translator outtr = null;
      boolean quiet = false;

      Coordinates.setLengthUnit(Coordinates.METER);
      Coordinates.setAngleUnit(Coordinates.DEGMN);

      while(start < args.length && args[start].charAt(0) == '-') {
	// -quiet option
	if(args[start].equals("-quiet")) {
	  quiet = true;
	}
	// -help option
	else if(args[start].equals("-usage")) {
	  usage();
	}
	// -m length unit option
	else if(args[start].equals("-m") || args[start].equals("-meter")) {
	  Coordinates.setLengthUnit(Coordinates.METER);
	}
	// -km length unit option
	else if(args[start].equals("-km") ||
		args[start].equals("-kilometer")) {
	  Coordinates.setLengthUnit(Coordinates.KILOMETER);
	}
	// -radian angle unit option
	else if(args[start].equals("-radian") || args[start].equals("-rad")) {
	  Coordinates.setAngleUnit(Coordinates.RADIAN);
	}
	// -degre angle unit option
	else if(args[start].equals("-degre") || args[start].equals("-deg") ||
		args[start].equals("�")) {
	  Coordinates.setAngleUnit(Coordinates.DEGRE);
	}
	// -degmn angle unit option
	else if(args[start].equals("-degre-minute") ||
		args[start].equals("-degmn") || args[start].equals("�'")) {
	  Coordinates.setAngleUnit(Coordinates.DEGMN);
	}
	// -degmnsec angle unit option
	else if(args[start].equals("-degre-minute-second") ||
		args[start].equals("-degmnsec") ||
		args[start].equals("�'\"")) {
	  Coordinates.setAngleUnit(Coordinates.DEGMNSEC);
	}
	else {
	  start++;
	  if(start >= args.length)
	    usage();
	  // -o option
	  if(args[start-1].equals("-o")) {
	    out = Coordinates.create(args[start]);
	    if(out.getName().equals("None")) {
	      System.err.println("invalid output coordinate: " + args[start]);
	      System.exit(-1);
	    }
	  }
	  // -sep option
	  else if(args[start-1].equals("-sep")) {
	    StringBuffer buf = new StringBuffer();
	    for(int i = 0; i < args[start].length(); i++) {
	      if(args[start].charAt(i) == '\\') {
		i++;
		if(i < args[start].length()) {
		  switch(args[start].charAt(i)) {
		  case 't':
		    buf.append('\t');
		    break;
		  case 'n':
		    buf.append('\n');
		  break;
		  case '\\':
		    buf.append('\\');
		    break;
		  default:
		    buf.append('\\');
		    buf.append(args[start].charAt(i));
		    break;
		  }
		}
		else
		  buf.append('\\');
	      }
	      else
		buf.append(args[start].charAt(i));
	    }
	    separator = buf.toString();
	  }
	  // -in option
	  else if(args[start-1].equals("-in")) {
	    inconv = args[start];
	  }
	  // -out option
	  else if(args[start-1].equals("-out")) {
	    outconv = args[start];
	  }
	  else
	    usage();
	}
	start++;
      }
      if(inconv != null)
	intr = new Translator(inconv);
      if(outconv != null)
	outtr = new Translator(outconv);

      left = args.length - start;
      // read from file
      if(left <= 1) {
	try {
	  Reader rd;
	  if(left == 0)
	    // read from std input
	    rd = new InputStreamReader(System.in);
	  else
	    rd = new FileReader(args[start]);
	  BufferedReader reader = new BufferedReader(rd);

	  try {
	    String line = reader.readLine();
	    if(line != null)
	      line = line.trim();
	    // for each line
	    while(line != null) {
	      // check if line is an instruction line
	      String [] arr = line.split("[ \t]");
	      if(arr.length < 1)
	        continue;
	      // check if line is a new output coordinate system
	      if(arr.length == 1) {
		out = Coordinates.create(line);
		if(out.getName().equals("None")) {
		  System.err.println("invalid output coordinate format: " +
				     line);
		  System.exit(-1);
		}
	      }
	      else {
		if(arr.length < 2) {
		  System.err.println("invalid input line: " + line);
		}
		// check if line is a length unit instruction
		if(arr[0].toLowerCase().equals("length")) {
		  String unit = arr[1].toLowerCase();
		  if(unit.equals("meter") || unit.equals("m")) {
		    Coordinates.setLengthUnit(Coordinates.METER);
		  }
		  else if(unit.equals("kilometer") || unit.equals("km")) {
		    Coordinates.setLengthUnit(Coordinates.KILOMETER);
		  }
		  else {
		    System.err.println("invalid length unit instruction: " +
				       line);
		  }
		}
		// check if line is an angle unit instruction
		else if(arr[0].toLowerCase().equals("angle")) {
		  String unit = arr[1].toLowerCase();
		  if(unit.equals("radian") || unit.equals("rad")) {
		    Coordinates.setAngleUnit(Coordinates.RADIAN);
		  }
		  else if(unit.equals("degre") || unit.equals("deg") ||
			  unit.equals("�")) {
		    Coordinates.setAngleUnit(Coordinates.DEGRE);
		  }
		  else if(unit.equals("degre-minute") ||
			  unit.equals("degmn") || unit.equals("�'")) {
		    Coordinates.setAngleUnit(Coordinates.DEGMN);
		  }
		  else if(unit.equals("degre-minute-second") ||
			  unit.equals("degmnsec") || unit.equals("�'\"")) {
		    Coordinates.setAngleUnit(Coordinates.DEGMNSEC);
		  }
		  else {
		    System.err.println("invalid angle unit instruction: " +
				       line);
		  }
		}
		// line is a coordinate
		else {
		  Coordinates in;
		  if(intr != null) { 
		    in = Coordinates.create(intr.translate(line, separator));
		  }
		  else
		    in = Coordinates.create(line);
		  if(in.getName().equals("None")) {
		    if(!quiet)
		      System.out.println("invalid input coordinate: " + line);
		  }
		  else {
		    if(out == null) {
		      System.err.println("No output system specified.");
		      System.exit(-1);
		    }
		    else {
		      Coordinates res =
			Coordinates.create(out.getName(), in.toWGS84());
		      if(outtr != null)
			System.out.println(outtr.translate(line, separator,
							   res.toString(),
							   " "));
		      else
			System.out.println(res.toString());
		    }
		  }
		}
	      }
	      line = reader.readLine();
	      if(line != null)
		line = line.trim();
	    }
	  }
	  catch(IOException e) {
	  }
	}
	catch(IOException e) {
	  System.err.println("unable to open input file: " + args[start]);
	}
      }
      // read input coordinate from command line
      else {
	String str = args[start];
	for(int i = start+1; i < args.length; i++)
	  str += " " + args[i];
	Coordinates in = Coordinates.create(str);
	if(in.getClass().getName().equals("NoCoordinates"))
	  System.err.println("invalid input coordinate: " + str);
	else {
	  System.out.println(Coordinates.create(out.getName(), in.toWGS84()));
	}
      }
    }
  }


  /**
   * print usage and exit
   */
  public static void usage() {
    System.out.println("usage: java GeoConvert [-quiet] [-o output_system] [-sep field_separator_regexp] [-m | -km] [-rad | -deg | -degmn | -degmnsec] [-in string] [-out string] <input_coordinate | input_file>\n\nConverts an input coordinate in an output coordinate. This program can\nbe used through its GUI, using command line, and reading input coordinates\nfrom a file\n\nNo arguments    : launch GUI.\ninput_file      : read input from a file. First file line should be output\n                  format if -o option was not specified. Any single word line\n                  is treated as a new output format system.\n-o output format: output coordinate system,\n                  input is input coordinate.\n-in string      : format conversion string for input parsing.\n-out string     : format conversion string for output parsing.\n\nRecognized coordinates systems are:");
    Iterator it = Coordinates.systems();
    it.next();
    while(it.hasNext()) {
      String name = (String) it.next();
      System.out.print(name + " ");
    }
    System.out.println();
    System.exit(0);
  }


  private Coordinates translate(String name, JPanel dstpanel,
				  Coordinates srccoord) {
    Coordinates res = Coordinates.create(name, srccoord.toWGS84());
    dstpanel.removeAll();
    res.editor(dstpanel);
    dstpanel.validate();
    srccoord.setEditable(true);
    return res;
  }

  /**
   * create GUI
   */
  public GeoConvert() {
    top = new JFrame("Geographic coordinates conversion");

    Coordinates.setLengthUnit(Coordinates.METER);
    Coordinates.setAngleUnit(Coordinates.DEGMN);

    JMenuBar mb = new JMenuBar();
    top.setJMenuBar(mb);
    JMenu menu = new JMenu("Parameters");
    mb.add(menu);
    final JCheckBoxMenuItem m, km;
    m = new JCheckBoxMenuItem("Length in meters");
    menu.add(m);
    m.setState(true);
    km = new JCheckBoxMenuItem("Length in kilometers");
    menu.add(km);
    menu.addSeparator();
    final JCheckBoxMenuItem rad, deg, degmn, degmnsec;
    rad = new JCheckBoxMenuItem("Angles in radian");
    menu.add(rad);
    deg = new JCheckBoxMenuItem("Angles in degres");
    menu.add(deg);
    degmn = new JCheckBoxMenuItem("Angles in deg/mn");
    menu.add(degmn);
    degmn.setState(true);
    degmnsec = new JCheckBoxMenuItem("Angles in deg/mn/sec");
    menu.add(degmnsec);
    menu.addSeparator();
    JMenuItem it = new JMenuItem("Exit");
    menu.add(it);
    it.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  System.exit(0);
	}
      });

    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    top.getContentPane().setLayout(layout);
    c.gridy = 0;

    JLabel src = new JLabel("Source coordinates");
    c.gridx = 0;
    c.gridwidth = 2;
    c.weightx = 0.0;
    layout.setConstraints(src, c);
    top.getContentPane().add(src);

    srcsystems = new JComboBox();
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.weightx = 0.0;
    layout.setConstraints(srcsystems, c);
    top.getContentPane().add(srcsystems);
    Iterator en = Coordinates.systems();
    en.next();
    while(en.hasNext()) {
      String name = (String) en.next();
      srcsystems.addItem(name);
    }

    srcpanel = new JPanel();
    srcpanel.setPreferredSize(new Dimension(240, 120));
    c.gridx = 1;
    c.weightx = 1.0;
    c.gridheight = 2;
    c.weighty = 1.0;
    layout.setConstraints(srcpanel, c);
    top.getContentPane().add(srcpanel);

    srccoord = Coordinates.create("Lambert 3 982616 157338 150");
    srccoord.editor(srcpanel);
    srccoord.setEditable(true);


    JLabel dst = new JLabel("Destination coordinates");
    c.gridy = 3;
    c.gridx = 0;
    c.gridwidth = 2;
    c.weightx = 0.0;
    c.gridheight = 1;
    c.weighty = 0.0;
    layout.setConstraints(dst, c);
    top.getContentPane().add(dst);

    dstsystems = new JComboBox();
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 1;
    c.weightx = 0.0;
    layout.setConstraints(dstsystems, c);
    top.getContentPane().add(dstsystems);
    en = Coordinates.systems();
    while(en.hasNext()) {
      String name = (String) en.next();
      dstsystems.addItem(name);
    }

    dstpanel = new JPanel();
    dstpanel.setPreferredSize(new Dimension(240, 120));
    c.gridx = 1;
    c.weightx = 1.0;
    c.gridheight = 2;
    c.weighty = 1.0;
    layout.setConstraints(dstpanel, c);
    top.getContentPane().add(dstpanel);

    dstcoord = Coordinates.create("");
    dstcoord.editor(dstpanel);
    dstcoord.setEditable(true);

    srcsystems.addItemListener(new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	  if(e.getStateChange() == ItemEvent.SELECTED) {
	    srccoord = Coordinates.create((String) e.getItem());
	    srcpanel.removeAll();
	    srccoord.editor(srcpanel);
	    srccoord.setEditable(true);
	    srcpanel.validate();
	    dstsystems.setSelectedItem("None");
	  }
	}
      });

    
    dstsystems.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  dstcoord = translate(dstsystems.getSelectedItem().toString(),
			       dstpanel, srccoord);
	}
      });

    m.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  Coordinates.setLengthUnit(Coordinates.METER);
	  km.setState(false);
	  dstcoord = translate(dstsystems.getSelectedItem().toString(),
			       dstpanel, srccoord);
	}
      });
    km.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  Coordinates.setLengthUnit(Coordinates.KILOMETER);
	  m.setState(false);
	  dstcoord = translate(dstsystems.getSelectedItem().toString(),
			       dstpanel, srccoord);
	}
      });
    rad.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  Coordinates.setAngleUnit(Coordinates.RADIAN);
	  deg.setState(false);
	  degmn.setState(false);
	  degmnsec.setState(false);
	  dstcoord = translate(dstsystems.getSelectedItem().toString(),
			       dstpanel, srccoord);
	}
      });
    deg.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  Coordinates.setAngleUnit(Coordinates.DEGRE);
	  rad.setState(false);
	  degmn.setState(false);
	  degmnsec.setState(false);
	  dstcoord = translate(dstsystems.getSelectedItem().toString(),
			       dstpanel, srccoord);
	}
      });
    degmn.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  Coordinates.setAngleUnit(Coordinates.DEGMN);
	  rad.setState(false);
	  deg.setState(false);
	  degmnsec.setState(false);
	  dstcoord = translate(dstsystems.getSelectedItem().toString(),
			       dstpanel, srccoord);
	}
      });
    degmnsec.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  Coordinates.setAngleUnit(Coordinates.DEGMNSEC);
	  rad.setState(false);
	  deg.setState(false);
	  degmn.setState(false);
	  dstcoord = translate(dstsystems.getSelectedItem().toString(),
			       dstpanel, srccoord);
	}
      });


    top.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	  top.dispose();
	}
      });
    top.pack();
    top.setVisible(true);
  }
}



/**
 * parse and translate a set of input strings with an awk-like syntax
 */
class Translator {

  private class Node {
    protected Vector children;

    public Node() {
      children = new Vector();
    }

    public String toString() {
      String res = "";
      for(int i = 0; i < children.size(); i++)
	res += children.elementAt(i).toString();
      return res;
    }

    protected int parseExpression(String format, int i) {
      int open = 1;
      int j = i + 1;
      while(open > 0 && j < format.length()) {
	switch(format.charAt(j)) {
	case '{':
	  if(format.charAt(j-1) != '\\')
	    open++;
	  break;
	case '}':
	  if(format.charAt(j-1) != '\\')
	    open--;
	  break;
	}
	if(open > 0)
	  j++;
      }
      if(open > 0) {
	System.err.println("Unbalanced bracket in input format string");
	return -1;
      }
      return j;
    }

    protected int parseField(String format, int i) {
      if(format.charAt(i) != '$' || format.charAt(i + 1) != '{')
	return -1;
      int j = i + 2;
      while(j < format.length() &&
	    (format.charAt(j) != '}' || format.charAt(j-1) == '\\'))
	j++;
      if(format.charAt(j) != '}') {
	System.err.println("Unclosed field in input format string");
	return -1;
      }
      return j;
    }
  }

  private class Value extends Node {
    public static final int STRING = 0, INT = 1, DOUBLE = 2;
    protected int kind;
    String str;
    double d;
    int i;

    public Value(String s) {
      try {
	d = Double.parseDouble(s);
	try {
	  i = Integer.parseInt(s);
	  if((double) i == d)
	    kind = INT;
	  else
	    kind = DOUBLE;
	}
	catch(NumberFormatException e) {
	  kind = DOUBLE;
	}
      }
      catch(NumberFormatException e) {
	kind = STRING;
	str = new String(s);
      }
    }

    public Value(String s, int k) {
      kind = k;
      try {
	switch(kind) {
	case STRING:
	  str = new String(s);
	  break;
	case DOUBLE:
	  d = Double.parseDouble(s);
	  break;
	case INT:
	  i = Integer.parseInt(s);
	  break;
	}
      }
      catch(NumberFormatException e) {
	kind = STRING;
	str = new String(s);
      }
    }


    public String toString() {
      switch(kind) {
      case STRING:
	return str;
      case DOUBLE:
	return String.valueOf(d);
      case INT:
	return String.valueOf(i);
      }
      return "";
    }
  }

  private class Expression extends Node {
    public static final int SUM = 0, DIF = 1, MUL = 2, DIV = 3;
    protected int kind;

    public Expression(Root root, String s) {
      int i = 0;
      if(s.charAt(i) == '{') {
	int j = parseExpression(s, i);
	if(j == -1)
	  invalid(s);
	children.add(new Expression(root, s.substring(1, j)));
	i = j + 1;
      }
      else if(s.charAt(i) == '$') {
	int j = parseField(s, i);
	if(j == -1)
	  invalid(s);
	children.add(new Value(root.get(s.substring(2, j)), Value.STRING));
	i = j + 1;
      }
      else {
	while(i < s.length() && s.charAt(i) != '+' && s.charAt(i) != '-' &&
	      s.charAt(i) != '*' && s.charAt(i) != '/')
	  i++;
	children.add(new Value(s.substring(0, i), Value.DOUBLE));
      }
      if(i >= s.length())
	invalid(s);
      switch(s.charAt(i)) {
      case '+':
	kind = SUM;
	break;
      case '-':
	kind = DIF;
	break;
      case '*':
	kind = MUL;
	break;
      case '/':
	kind = DIV;
	break;
      default:
	invalid(s);
	break;
      }
      i++;
      if(s.charAt(i) == '{') {
	int j = parseExpression(s, i);
	if(j == -1)
	  invalid(s);
	children.add(new Expression(root, s.substring(i + 1, j)));
      }
      else if(s.charAt(i) == '$') {
	int j = parseField(s, i);
	if(j == -1)
	  invalid(s);
	children.add(new Value(root.get(s.substring(i + 2, j)), Value.STRING));
      }
      else {
	children.add(new Value(s.substring(i), Value.DOUBLE));
      }
    }

    private void invalid(String s) {
      System.err.println("Invalid expression: " + s);
      System.exit(-1);
    }

    public String toString() {
      try {
	double d1 = Double.parseDouble(children.elementAt(0).toString());
	double d2 = Double.parseDouble(children.elementAt(1).toString());
	switch(kind) {
	case SUM:
	  return String.valueOf(d1 + d2);
	case DIF:
	  return String.valueOf(d1 - d2);
	case MUL:
	  return String.valueOf(d1 * d2);
	case DIV:
	  return String.valueOf(d1 / d2);
	}
      }
      catch(NumberFormatException e) {
	/*System.err.println("Invalid expression. " +
			   children.elementAt(0).toString() +
			   " " + kind + " " +
			   children.elementAt(1).toString());
			   System.exit(-1);*/
	return "error";
      }
      return "";
    }
  }


  private class Root extends Node {
    String fields[][];

    public String get(String id) {
      try {
	int i, j;
	int n = 0;
	int m;
	boolean all = false;
	for(i = 0; i < id.length(); i++) {
	  if(id.charAt(i) == ':') {
	    i = id.length();
	    break;
	  }	    
	  if(id.charAt(i) == '.') {
	    n = Integer.parseInt(id.substring(0, i)) - 1;
	    i++;
	    if(i >= id.length())
	      return "";
	    if(id.charAt(i) == '>') {
	      all = true;
	      i++;
	      if(i >= id.length())
		return "";
	    }
	    break;
	  }
	}
	if(i >= id.length())
	  i = 0;
	for(j = i + 1; j < id.length(); j++) {
	  if(id.charAt(j) == ':')
	    break;
	}
	m = Integer.parseInt(id.substring(i, j)) - 1;

	String res = "";;
	if(n >= 0 && n < fields.length && m >= 0 && m < fields[n].length) {
	  if(all) {
	    while(++m < fields[n].length)
	      res += fields[n][m];
	  }
	  else
	    res = fields[n][m];
	}

	if(res.equals("") && j < id.length() && id.charAt(j) == ':')
	  return id.substring(j+1);
	else
	  return res;
      }
      catch(NumberFormatException e) {
	return "";
      }
    }


    public Root(String format, String f[][]) {
      fields = f;
      int i = 0;
      StringBuffer current = new StringBuffer();
      while(i < format.length()) {
	if(format.charAt(i) == '\\') {
	  if(++i < format.length()) {
	    switch(format.charAt(i)) {
	    case '\\':
	      current.append('\\');
	      break;
	    case 't':
	      current.append('\t');
	      break;
	    case 'n':
	      current.append('\n');
	      break;
	    case '{':
	      current.append('{');
	      break;
	    case '}':
	      current.append('}');
	      break;
	    default:
	      current.append('\\');
	      current.append(format.charAt(i));
	      break;
	    }
	  }
	  else {
	    --i;
	    current.append('\\');
	  }
	}
	else if(format.charAt(i) == '{') {
	  current = addChild(current);
	  int j = parseExpression(format, i);
	  if(j == -1)
	    System.exit(-1);
	  children.add(new Expression(this, format.substring(i+1, j)));
	  i = j;
	}
	else if(format.charAt(i) == '$') {
	  if(++i < format.length()) {
	    if(format.charAt(i) == '{') {
	      current = addChild(current);
	      int j = parseField(format, i - 1);
	      children.add(new Value(get(format.substring(i + 1, j)),
				     Value.STRING));
	      i = j;
	    }
	    else {
	      current.append('$');
	      --i;
	    }
	  }
	  else
	    current.append('$');
	}
	else {
	  current.append(format.charAt(i));
	}
	i++;
      }
      addChild(current);
    }


    protected StringBuffer addChild(StringBuffer child) {
      if(child.length() > 0) {
	children.add(new Value(child.toString(), Value.STRING));
	return new StringBuffer();
      }
      else
	return child;
    }
  }

  /**
   * output string format
   */
  private String format;


  /**
   * build a new translator with given output format
   * 
   * @param format output format string
   */
  public Translator(String format) {
    this.format = format;
  }

  /**
   * translates a set of input strings into output format
   * 
   * @param in alternate array of input string and splitting regular
   * expressions
   */
  public String translate(String in[]) {
    String fields[][] = new String[in.length/2][];
    for(int i = 0; i < in.length; i += 2)
      fields[i/2] = in[i].split(in[i+1]);
    return new Root(format, fields).toString();
  }

  public String translate(String s, String sep) {
    String in[] = new String[2];
    in[0] = s;
    in[1] = sep;
    return translate(in);
  }

  public String translate(String s1, String sep1, String s2, String sep2) {
    String in[] = new String[4];
    in[0] = s1;
    in[1] = sep1;
    in[2] = s2;
    in[3] = sep2;
    return translate(in);
  }

  public String translate(String s1, String sep1, String s2, String sep2,
			  String s3, String sep3) {
    String in[] = new String[6];
    in[0] = s1;
    in[1] = sep1;
    in[2] = s2;
    in[3] = sep2;
    in[4] = s3;
    in[5] = sep3;
    return translate(in);
  }
}
