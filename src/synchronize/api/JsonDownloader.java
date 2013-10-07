package synchronize.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;

public class JsonDownloader implements Runnable {
	
	private PathConfiguration paths;
	
	public JsonDownloader(PathConfiguration paths) {
		this.paths = paths;
	}
	
	@Override
	public void run() {
		Synchronizer.debug("Downloading json files...");
		
		Path data = paths.getDataPath();

		// Fetch all the paths we need
		Path filesOld = paths.getOldFilesPath();
		Path categoriesOld = paths.getOldCategoriesPath();
		Path files = paths.getFilesPath();
		Path categories = paths.getCategoriesPath();
		String filesJsonURL = paths.getJsonFilesURL();
		String categoriesJsonURL = paths.getJsonCategoriesURL();
		
		try {
			// create temp files to enable atomic move operation
			Path tempFiles = Files.createTempFile(data, "files", ".json");
			Path tempCategories = Files.createTempFile(data, "categories", ".json");
			
			// Initialize URLs
			URL filesURL = new URL(filesJsonURL);
			URL categoriesURL = new URL(categoriesJsonURL);
			
			// Transfer data
			FileUtils.copyURLToFile(filesURL, tempFiles.toFile());
			Synchronizer.debug("Successfully downloaded files.json");
			
			FileUtils.copyURLToFile(categoriesURL, tempCategories.toFile());
			Synchronizer.debug("Successfully downloaded categories.json");
			
			// check if categories file already exist
			if(Files.exists(categories, new LinkOption[0])) {
				// if new file is equal to old file simply delete temp new file
				if(FileUtils.contentEquals(tempCategories.toFile(), categories.toFile())) {
					Files.delete(tempCategories);
				} else { // if files differ move the current file and replace previous file
					Files.move(categories, categoriesOld, StandardCopyOption.REPLACE_EXISTING);
					// move temp file to current file
					Files.move(tempCategories, categories, StandardCopyOption.ATOMIC_MOVE);
				}
			}
			
			// Do the same for the files file as for the category file
			if(Files.exists(files, new LinkOption[0])) {
				if(FileUtils.contentEquals(tempFiles.toFile(), files.toFile())) {
					Files.delete(tempFiles);
				} else {
					Files.move(files, filesOld, StandardCopyOption.REPLACE_EXISTING);
					Files.move(tempFiles, files, StandardCopyOption.ATOMIC_MOVE);
				}
			}
			Synchronizer.debug("Everything cleaned up nicely.");
		} catch(MalformedURLException e) {
			System.err.println("The URL was malformed.");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("Could not find or create the output file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Unknown error occured.");
			e.printStackTrace();
		}
	}

}
