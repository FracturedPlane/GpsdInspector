package name.glen.xml;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import name.gano.utils.CoordinateConversion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLGPSParser 
{

	//No generics
	List<Position> myPositions;
	Document dom;
	
	private static final String SECTOR = "sector";
	private static final String DIGRAPH = "digraph";
	private static final String NORTHING = "northing";
	private static final String EASTING = "easting";
	


	public XMLGPSParser()
	{
		//create a list to hold the employee objects
		myPositions = new ArrayList<Position>();
	}

	
	public void runExample() 
	{		
		//parse the xml file and get the dom object
		parseXmlFile(new File("/home/yurt/playground/GpsInspector_src/src/name/glen/xml/UTMCoordinates.xml"));
		
		//get each employee element and create a Employee object
		parseDocument();
		
		//Iterate through the list and print the data
		printData();
		
	}
	
	
	public void parseXmlFile(File xmlFile)
	{
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try 
		{
			
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			//parse using builder to get DOM representation of the XML file
			dom = db.parse(xmlFile.getAbsolutePath());
			

		}
		catch(ParserConfigurationException pce) 
		{
			pce.printStackTrace();
		}catch(SAXException se) 
		{
			se.printStackTrace();
		}catch(IOException ioe) 
		{
			ioe.printStackTrace();
		}
	}

	
	public List<Position> parseDocument()
	{
		//get the root elememt
		Element docEle = dom.getDocumentElement();
		
		//get a nodelist of <employee> elements
		NodeList nl = docEle.getElementsByTagName("Coordinate");
		if(nl != null && nl.getLength() > 0) 
		{
			if (docEle.getTagName().equalsIgnoreCase("GPSCoords") )
			{
				System.out.println(docEle.getTagName());
				for(int i = 0 ; i < nl.getLength();i++)
				{
					
					//get the employee element
					Element el = (Element) nl.item(i);
					
					//get the Employee object
					Position position = getGPSPosition(el);
					
					//add it to list
					myPositions.add(position);
				}
			}
			else if (docEle.getTagName().equalsIgnoreCase("UTMCoords"))
			{
				System.out.println("UTMCoords");
				for(int i = 0 ; i < nl.getLength();i++)
				{
					
					//get the employee element
					Element el = (Element) nl.item(i);
					
					//get the Employee object
					Position position = getUTMPosition(el);
					
					//add it to list
					myPositions.add(position);
				}
			}
		}
		return myPositions;
	}


	
	private Position getUTMPosition(Element empEl)
	{
		CoordinateConversion converter = new CoordinateConversion();
		
		String sector = getTextValue(empEl, SECTOR) + " ";
		String digraph = getTextValue(empEl, DIGRAPH) + " ";
		String easting = getTextValue(empEl, EASTING) + " ";
		String northing = getTextValue(empEl, NORTHING);
		
		String UTMCoord = sector + digraph + easting + northing;
		
		System.out.println(UTMCoord);
		
		double[] gpsCoords = converter.utm2LatLon(UTMCoord);
		
		System.out.println(gpsCoords[0]);
		System.out.println(gpsCoords[1]);
		double lat = gpsCoords[0];
		double lon = gpsCoords[1];
		
		
		//Create a new Employee with the value read from the xml nodes
		Position e = new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), 0.0);
		
		return e;
	}
	/**
	 * I take an employee element and read the values in, create
	 * an Employee object and return it
	 * @param empEl
	 * @return
	 */
	private Position getGPSPosition(Element empEl)
	{
		

		double Lat = Double.parseDouble(getTextValue(empEl,"Lat"));
		double Lon = Double.parseDouble(getTextValue(empEl,"Lon"));
		double Altitude = Double.parseDouble(getTextValue(empEl,"Altitude"));

		String type = empEl.getAttribute("type");
		System.out.println("The type is : " + type);
		
		//Create a new Employee with the value read from the xml nodes
		Position e = new Position(Angle.fromDegrees(Lat), Angle.fromDegrees(Lon), Altitude);
		
		return e;
	}


	/**
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content 
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is name I will return John  
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private String getTextValue(Element ele, String tagName)
	{
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) 
		{
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return new String(textVal);
	}

	
	/**
	 * Calls getTextValue and returns a int value
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private int getIntValue(Element ele, String tagName) 
	{
		//in production application you would catch the exception
		return Integer.parseInt(getTextValue(ele,tagName));
	}
	
	
	
	
	/**
	 * Iterate through the list and print the 
	 * content to console
	 */
	
	private void printData()
	{
		
		System.out.println("No of Positions '" + myPositions.size() + "'.");
		
		Iterator<Position> it = myPositions.iterator();
		while(it.hasNext()) 
		{
			System.out.println(it.next().toString());
		}
	}

	
	public static void main(String[] args)
	{
		//create an instance
		XMLGPSParser dpe = new XMLGPSParser();
		
		//call run example
		dpe.runExample();
	}
	
}
