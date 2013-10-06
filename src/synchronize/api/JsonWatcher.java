package synchronize.api;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.StandardWatchEventKinds.*;

public class JsonWatcher implements Runnable {
	
	private WatchService watcher;
	private PathConfiguration paths;
	
	public JsonWatcher(PathConfiguration paths) throws IOException {
		this.paths = paths;
		watcher = FileSystems.getDefault().newWatchService();
		paths.getJsonPath().register(watcher, ENTRY_CREATE);
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
				WatchEvent.Kind<?> kind = event.kind();
				
				if(kind == OVERFLOW)
					continue;
				
				Path filename = (Path)event.context();
				Path createdFile = paths.getJsonPath().resolve(filename);
				
				// parse only the categories and files json files.
				if(createdFile.equals(paths.getCategoriesPath())) {
					
				} else if(createdFile.equals(paths.getFilesPath())) {
					
				} else
					continue;
			}
			
			boolean valid = key.reset();
			if(!valid)
				break;
		}
	}

}
