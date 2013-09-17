package synchronize.api;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
		Map<String,SyncFile> map = FilesSingleton.getInstance().getFileMap();
		for(Map.Entry<String,SyncFile> entry : map.entrySet()) {
			System.out.println(entry.getKey());
		}
		System.out.println(map.get("ds100c01gb14.pdf").getName());
	}
}
