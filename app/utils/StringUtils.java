package utils;

import java.io.StringWriter;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.w3c.dom.Node;

import play.Logger;
import play.Play;
import play.mvc.Http;
import play.mvc.Http.Request;

public class StringUtils {

	public static boolean isEmpty(String test) {
		if (test == null)
			return true;
		if (test.length() <= 0)
			return true;

		return false;
	}

	public static Date getDateFromString(String dateString) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date = new Date();
		try {
			date = formatter.parse(dateString);
		} catch (ParseException exc) {
		}
		
		return date;
	}
	
	public static String formatDate(String dateString, String pattern) {
		Date date = getDateFromString(dateString);
		return formatDate(date, pattern);
	}

	public static String formatDateForMySQL(Date date) {
		return formatDate(date, "yyyy-MM-dd HH:mm:ss");
	}

	public static String formatDate(Date date, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(date);
	}

	public static int getInt(String intString) {
		float qty = getFloat(intString, 0);
		return (int)qty;
	}

	public static int getInt(String intString, int defaultInt) {
		try {
			return Integer.parseInt(intString);
		} catch (NumberFormatException nfExc) {
			return defaultInt;
		}
	}
	
	public static long getLong(String longString) {
		return getLong(longString, 0);
	}

	public static long getLong(String longString, long defaultLong) {
		try {
			return Long.parseLong(longString);
		} catch (NumberFormatException nfExc) {
			return defaultLong;
		}
	}

	public static float getFloat(String floatString) {
		return getFloat(floatString, 0);
	}

	public static float getFloat(String floatString, float defaultFloat) {
		try {
			return Float.parseFloat(floatString);
		} catch (NumberFormatException nfExc) {
			return defaultFloat;
		} catch(Exception e){
			return defaultFloat;
		}
	}

	public static String hexEncode(byte[] data) {
		return new String(Hex.encodeHex(data)).toLowerCase();
	}

	public static String sqlEscapeString(String str) {
		if (str == null) {
			return "";
		}

		if (str.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/? ]", "").length() < 1) {
			return str;
		}

		String clean_string = str;
		clean_string = clean_string.replaceAll("\\\\", "\\\\\\\\");
		clean_string = clean_string.replaceAll("\\n", "\\\\n");
		clean_string = clean_string.replaceAll("\\r", "\\\\r");
		clean_string = clean_string.replaceAll("\\t", "\\\\t");
		clean_string = clean_string.replaceAll("\\00", "\\\\0");
		clean_string = clean_string.replaceAll("'", "\\\\'");
		clean_string = clean_string.replaceAll("\\\"", "\\\\\"");

		clean_string.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/?\\\\\"' ]", "").length();
		return clean_string;
	}

	public static String prettyPrintKeys(Map<String,String> s) {
		StringBuilder builder = new StringBuilder();
		Iterator<String> i = s.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			builder.append(key);
			builder.append("=");
			builder.append("<");
			builder.append(s.get(key));
			builder.append(">");
			if (!i.hasNext()) {
				break;
			}
			builder.append(",");
		}
		return builder.toString();
	}
	
	public static String join(Collection<?> s, String delimiter) {
		StringBuilder builder = new StringBuilder();
		Iterator<?> iter = s.iterator();
		while (iter.hasNext()) {
			builder.append(iter.next());
			if (!iter.hasNext()) {
				break;
			}
			builder.append(delimiter);
		}
		return builder.toString();
	}
	
	public static String generateEmailCode() {
		SecureRandom random = new SecureRandom();
		String time = "cdscvs"+System.currentTimeMillis();
		random.setSeed (time.getBytes());
		byte[] digits = new byte[4];
		for (int i = 0; i < digits.length; i++) {
			digits[i] = (byte)random.nextInt();
		}
		return StringUtils.hexEncode(digits);
	}
	
	public static String prettyPrintXML(Node node) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(node);
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();
			return xmlString;
		} catch (Exception anyExc) {
		}
		return "pretty printing xml didn't work";
	}
	
	public static String getRandomColor() {
		Random random = new Random();
	    String[] letters = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};  
	    String color = "rgba(";  
	    for (int i = 0; i < 3; i++ ) {  
	    	int colorNum = random.nextInt(256);
	        color += colorNum+",";  
	    }  
	    color += "0.0)";
	    return color;  
	}

}
