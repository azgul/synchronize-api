package synchronize.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import synchronize.model.Category;
import synchronize.model.SyncFile;

public class SyncDownloader implements Runnable {
	
	private String download; 
	private String type;
	private String name;
	private Path path;

	public SyncDownloader(Category cat, PathConfiguration paths) {
		path = paths.getImagePath();
		download = cat.getImage();
		type = "Category";
		name = cat.getName();
	}
	
	public SyncDownloader(SyncFile file, PathConfiguration paths) {
		path = paths.getPdfPath();
		download = file.getFile();
		type = "File";
		name = file.getName();
	}
	
	@Override
	public void run() {
		if(UrlValidator.getInstance().isValid(download)) {
			Synchronizer.debug("Valid URL found, now downloading...");
			URL url = null;
			try {
				url = new URL(download);
				Path dest = path.resolve(FilenameUtils.getName(download));
				FileUtils.copyURLToFile(url, dest.toFile());
				Synchronizer.debug("Successfully downloaded " + url + " to " + dest);
			} catch (MalformedURLException e) {
				System.err.println("Malformed URL in " + type + " " + name);
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Could not download file for " + type + " " + name + " at " + url);
				e.printStackTrace();
			}
			
		} else {
			System.err.println("Malformed URL (" + download + ") in " + type + " " + name);
		}
	}

}
