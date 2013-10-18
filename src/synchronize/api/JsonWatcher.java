package synchronize.api;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import synchronize.model.Category;
import synchronize.model.SyncFile;

import static java.nio.file.StandardWatchEventKinds.*;

public class JsonWatcher implements Runnable {
	private ExecutorService executor;
	private WatchService watcher;
	private PathConfiguration paths;
	
	private boolean exit = false;
	
	public JsonWatcher(PathConfiguration paths) throws IOException {
		this.paths = paths;
		watcher = FileSystems.getDefault().newWatchService();
		paths.getJsonPath().register(watcher, ENTRY_CREATE);
		executor = Executors.newCachedThreadPool();
	}

	@Override
	public void run() {
		for(;;) {
			WatchKey key;
			try {
				key = watcher.take();
			} catch(InterruptedException e) {
				System.err.println("Interrupted watcher...");
				e.printStackTrace();
				break;
			} catch(ClosedWatchServiceException e) {
				Synchronizer.debug("Closed watcher...");
				e.printStackTrace();
				break;
			}
			
			for(WatchEvent<?> event : key.pollEvents()) {
				Synchronizer.debug("Got watch event...");
				WatchEvent.Kind<?> kind = event.kind();
				
				if(kind == OVERFLOW)
					continue;
				
				Path filename = (Path)event.context();
				Path createdFile = paths.getJsonPath().resolve(filename);
				
				// parse only the categories and files json files.
				if(createdFile.equals(paths.getCategoriesPath())) {
					Synchronizer.debug("Categories file was changed");
					for(Category cat : Synchronizer.getInstance().getCategoryDiff()) {
						executor.execute(new SyncDownloader(cat, paths));
					}
				} else if(createdFile.equals(paths.getFilesPath())) {
					Synchronizer.debug("Files file was changed");
					for(SyncFile file : Synchronizer.getInstance().getFileDiff()) {
						executor.execute(new SyncDownloader(file, paths));
					}
				} else
					continue;
			}
			
			boolean valid = key.reset();
			if(!valid || exit)
				break;
		}
	}
	
	public void exit() {
		if(executor != null) {
			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch(InterruptedException e) {
				System.err.println("Watcher exit was interrupted...");
				e.printStackTrace();
			}
		}
		exit = true;
	}

}