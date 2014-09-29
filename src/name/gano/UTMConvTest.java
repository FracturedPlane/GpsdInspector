package name.gano;

import java.util.ArrayList;

import name.gano.utils.CoordinateConversion;

public class UTMConvTest
{

	public static void main(String[] args)
	{
		
		double Lat = 38.563453;
		double Lon = -108.45643;
		
		String digraph = "S ";
		String sector = "12 ";
		
		double northing = 4252119 ;
		double easting = 519008 ;
		
		String UTMString = sector + digraph + easting + " " + northing;
		
		CoordinateConversion conv = new CoordinateConversion();
		
		double[] GPSConversion = conv.utm2LatLon(UTMString);
		
		
		String[] utm_coordinates_array;
		
		String utm_version = conv.latLon2UTM(Lat, Lon);
		utm_coordinates_array = utm_version.split(" ");
		System.out.println(utm_version);
		System.out.println(utm_coordinates_array[0]);
		
		System.out.println(" northing " + northing + " easting " + easting );
		System.out.println("Lat: " + GPSConversion[0] + " Lon: " + GPSConversion[1]);
	}
}
