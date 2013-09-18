package synchronize.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import synchronize.model.SyncFile;

import com.google.gson.Gson;

public class FilesSingleton {
	private SyncFile[] parseJSON() {
		Gson gson = new Gson();
		Path sample = FileSystems.getDefault().getPath("res", "sample-data", "files-sample.json");
		try {
			return gson.fromJson(Files.newBufferedReader(sample, Charset.forName("UTF-8")), SyncFile[].class);
		} catch(Exception e) {
			e.printStackTrace();
			return new SyncFile[0];
		}
	}
	
	private static FilesSingleton instance;
	public static FilesSingleton getInstance() {
		if(instance == null)
			instance = new FilesSingleton();
		return instance;
	}
	
	public Map<String,SyncFile> getFileMap() {
		SyncFile[] files = parseJSON();
		HashMap<String,SyncFile> map = new HashMap<String,SyncFile>();
		for(SyncFile f : files)
			map.put(f.getFileName(), f);
		return map;
	}
	
	public static void main(String[] args) {
		String appHome = System.getProperty("app.home");
		Properties props = new Properties();
		Path home = FileSystems.getDefault().getPath(appHome);
		Path config = home.resolve("config");
		Path configFile = config.resolve("config.properties");
		
		try {
			props.load(Files.newInputStream(configFile, StandardOpenOption.READ));
			System.out.println("We loaded the config file!");
		} catch(IOException e) {
			System.err.println("Could not read properties file.");
		}

	}
}
