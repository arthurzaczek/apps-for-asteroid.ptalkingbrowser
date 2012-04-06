package net.zaczek.PTalkingBrowser.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import net.zaczek.PTalkingBrowser.UrlRef;

import android.os.Environment;
import au.com.bytecode.opencsv.CSVReader;

public class DataManager {
	private static final int TIMEOUT = 20000;
	private static final int BUFFER_SIZE = 8 * 1024;

	public static FileReader openRead(String name) throws IOException {
		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root, "PTalkingBrowser");
		dir.mkdir();
		File file = new File(dir, name);
		if (!file.exists()) {
			file.createNewFile();
		}
		return new FileReader(file);
	}

	public static OutputStreamWriter openWrite(String name, boolean append) throws IOException {
		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root, "PTalkingBrowser");
		dir.mkdir();
		File file = new File(dir, name);
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file, append), "UTF-8");
		if (append == false)
			out.write('\ufeff');
		return out;
	}

	public static String readLine(BufferedReader in) throws IOException {
		String line = in.readLine();
		if (line == null)
			return null;
		line = line.trim();
		return line;
	}

	public static StringBuffer downloadText(URL url) throws IOException {
		final URLConnection c = url.openConnection();

		// Setup timeouts
		c.setConnectTimeout(TIMEOUT);
		c.setReadTimeout(TIMEOUT);

		final BufferedReader rd = new BufferedReader(new InputStreamReader(c.getInputStream()), BUFFER_SIZE);
		final StringBuffer result = new StringBuffer("");
		for (String line; (line = rd.readLine()) != null;) {
			result.append(line).append("\n");
		}
		rd.close();
		return result;
	}

	public static ArrayList<UrlRef> readUrls() throws IOException {
		final ArrayList<UrlRef> result = new ArrayList<UrlRef>();
		final FileReader sr = openRead("urls.csv");
		final CSVReader reader = new CSVReader(sr);
	    String [] line;
	    reader.readNext(); // skip first line
	    while ((line = reader.readNext()) != null) {
	    	result.add(new UrlRef(line[1], line[0]));
	    }
		return result;
	}

	public static void downloadUrls() throws IOException {
		final StringBuffer urls = downloadText(new URL("https://docs.google.com/spreadsheet/pub?key=0Au6e93kxiTMhdGdUVmZvdEdZcHdvaVBZUlp0WFpYU2c&single=true&gid=0&output=csv"));
		final OutputStreamWriter sw = openWrite("urls.csv", false);
		try {
			sw.write(urls.toString());
		} finally {
			sw.flush();
			sw.close();
		}
	}
}
