package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import betel.ATest.R;

import android.content.Context;

public class StringLoader {
	
	static private Object object = new Object(); 
	
	public static String loadStringExceptionless(Context context, int resource) {
		try {
			return loadString(context, resource);
		} catch (IOException e) {
			System.out.println("Can't resource " + resource + ", using empty string instead.");
			return "NULL";
		}
	}

	
	public static String loadString(Context context, int resource)
			throws IOException {
		//InputStream is = new FileInputStream(filename); // object.getClass().getResourceAsStream(filename);
		InputStream is = context.getResources().openRawResource(resource);
		if(is == null) {
			System.out.println("Cant't load text resource: " + resource);
			return "";
		}
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		StringBuilder builder = new StringBuilder();
		String line = null;

		while ((line = reader.readLine()) != null) {
			builder.append(line + "\n");
		}
		reader.close();

		return builder.toString();
	}
	
	public static void setObject(Object givenObject) {
		object = givenObject;
	}
}
