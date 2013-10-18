package synchronize.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import synchronize.model.Category;
import synchronize.model.SyncFile;

import com.google.gson.Gson;

public class Synchronizer {
	
	private static ExecutorService watchExec;
	private static JsonWatcher watcher;
	
	private static ScheduledExecutorService executor;
	
	private Properties properties;
	private PathConfiguration paths;
	
	private static Synchronizer instance;
	public static Synchronizer getInstance() {
		if(instance == null)
			instance = new Synchronizer();
		return instance;
	}
	
	private Synchronizer() {
		Properties properties = new Properties();
		String appHome = System.getProperty("app.home");
		Path home = FileSystems.getDefault().getPath(appHome);
		Path config = home.resolve("config");
		Path configFile = config.resolve("config.properties");
		
		try {
			properties.load(Files.newInputStream(configFile, StandardOpenOption.READ));
			Synchronizer.debug("Config file loaded.");
		} catch(IOException e) {
			System.err.println("Could not read properties file from " + configFile);
			Synchronizer.exit();
		}
		
		paths = new PathConfiguration(properties);
	}
	
	public static void main(String[] args) {
		TrayHandler.addTrayIcon(getInstance().paths);
		
		// setup watcher for new json files
		watchExec = Executors.newSingleThreadExecutor();
		try {
			watcher = new JsonWatcher(getInstance().paths);
			watchExec.execute(watcher);
		} catch(IOException e) {
			System.err.println("Could not fetch watcher service");
			e.printStackTrace();
		}
		
		// setup json downloader
		executor =
			    Executors.newSingleThreadScheduledExecutor();
		ScheduledFuture<?> future = executor.scheduleWithFixedDelay(new JsonDownloader(getInstance().paths), 0, 10, TimeUnit.MINUTES);
		
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
		if(watcher != null)
			watcher.exit();
		if(executor != null && watchExec != null) {
			executor.shutdown();
			watchExec.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
				watchExec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch(InterruptedException e) {
				System.err.println("Shutdown interrupted...");
				e.printStackTrace();
			}
		}
		
		System.exit(0);
	}
	

	private SyncFile[] parseFilesJSON(Path files) {
		Gson gson = new Gson();
		try {
			return gson.fromJson(Files.newBufferedReader(files, Charset.forName("UTF-8")), SyncFile[].class);
		} catch(Exception e) {
			e.printStackTrace();
			return new SyncFile[0];
		}
	}
	
	private Category[] parseCategoriesJSON(Path categories) {
		Gson gson = new Gson();
		try {
			return gson.fromJson(Files.newBufferedReader(categories, Charset.forName("UTF-8")), Category[].class);
		} catch(Exception e) {
			e.printStackTrace();
			return new Category[0];
		}
	}
	
	public List<SyncFile> getFileDiff() {
		List<SyncFile> files = Arrays.asList(parseFilesJSON(paths.getFilesPath()));
		if(Files.exists(paths.getOldFilesPath())) {
			List<SyncFile> oldFiles = Arrays.asList(parseFilesJSON(paths.getOldFilesPath()));
			files.removeAll(oldFiles);
		}
		return files;
	}
	
	public List<Category> getCategoryDiff() {
		List<Category> categories = Arrays.asList(parseCategoriesJSON(paths.getCategoriesPath()));
		if(Files.exists(paths.getOldCategoriesPath())) {
			List<Category> oldFiles = Arrays.asList(parseCategoriesJSON(paths.getOldCategoriesPath()));
			categories.removeAll(oldFiles);
		}
		return categories;
	}
	
	public Map<String,SyncFile> getFileMap() {
		SyncFile[] files = parseFilesJSON(paths.getFilesPath());
		HashMap<String,SyncFile> map = new HashMap<String,SyncFile>();
		for(SyncFile f : files)
			map.put(f.getFileName(), f);
		return map;
	}
	
	public static void debug(String str) {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		System.out.println(formatter.format(date) + ": " + str);
	}
}
