package com.pacoapp.paco.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTester {
	public static void main(String args[]){
		 String line = "This order was placed for QT3000! OK?";
	      String pattern = "(.*)";

	      // Create a Pattern object
	      Pattern r = Pattern.compile(pattern);

	      // Now create matcher object.
	      Matcher m = r.matcher(line);
	      if (m.find( )) {
	         System.out.println("Found value: " + m.group(0) );
	         System.out.println("Found value: " + m.group(1) );
	         System.out.println("Found value: " + m.group(2) );
	      }else {
	         System.out.println("NO MATCH");
	      }	
	}
	

}
