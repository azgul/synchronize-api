package synchronize.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import synchronize.model.SyncFile;

import com.google.gson.Gson;

public class Synchronizer {
	
	private static ScheduledExecutorService executor;
	
	private Properties properties;
	
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
	
	private static Synchronizer instance;
	public static Synchronizer getInstance() {
		if(instance == null)
			instance = new Synchronizer();
		return instance;
	}
	
	public Map<String,SyncFile> getFileMap() {
		SyncFile[] files = parseJSON();
		HashMap<String,SyncFile> map = new HashMap<String,SyncFile>();
		for(SyncFile f : files)
			map.put(f.getFileName(), f);
		return map;
	}
	
	private Path dataPath;
	public Path getDataPath() {
		if(dataPath == null)
			dataPath = FileSystems.getDefault().getPath(properties.getProperty("dataPath"));
		
		return dataPath;
	}
	
	public Path getJsonPath() { return getDataPath().resolve("json"); }
	
	public Path getFilesPath() { return getJsonPath().resolve("files.json"); }
	
	public Path getCategoriesPath() { return getJsonPath().resolve("categories.json"); }
	
	public String getJsonFilesURL() { return properties.getProperty("jsonFilesUrl"); }
	
	public String getJsonCategoriesURL() { return properties.getProperty("jsonCatgoriesUrl"); }
	
	public static void debug(String str) {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		System.out.println(formatter.format(date) + ": " + str);
	}
	
	private Synchronizer() {
		String appHome = System.getProperty("app.home");
		Properties props = new Properties();
		Path home = FileSystems.getDefault().getPath(appHome);
		Path config = home.resolve("config");
		Path configFile = config.resolve("config.properties");
		
		try {
			props.load(Files.newInputStream(configFile, StandardOpenOption.READ));
			Synchronizer.debug("Config file loaded.");
		} catch(IOException e) {
			System.err.println("Could not read properties file");
		}
	}
	
	public static void main(String[] args) {
		TrayHandler.addTrayIcon();
		
		executor =
			    Executors.newSingleThreadScheduledExecutor();
		ScheduledFuture<?> future = executor.scheduleWithFixedDelay(new JsonDownloader(), 0, 10, TimeUnit.MINUTES);
		
		ExecutorService watchExec = Executors.newSingleThreadExecutor();
		try {
			JsonWatcher watcher = new JsonWatcher();
			watchExec.execute(watcher);
		} catch(IOException e) {
			System.err.println("Could not fetch watcher service");
			e.printStackTrace();
		}
		
		try {
			future.get();
		} catch (ExecutionException e) {
			System.err.println("Execution exception...");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Interrupted...");
			e.printStackTrace();
		} catch (CancellationException e) {
			Synchronizer.debug("Cancelled future tasks.");
		}
	}
	
	public static void exit() {
		if(executor != null) {
			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch(InterruptedException e) {
				System.err.println("Shutdown interrupted...");
				e.printStackTrace();
			}
		}
		
		System.exit(0);
	}
}
