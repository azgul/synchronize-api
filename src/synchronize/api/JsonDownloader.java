package synchronize.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class JsonDownloader implements Runnable {

	private Properties props;
	
	public JsonDownloader(Properties props) {
		this.props = props;
	}
	
	@Override
	public void run() {
		FilesSingleton.debug("Downloading json files...");
		
		Path data = FileSystems.getDefault().getPath(props.getProperty("dataPath"));
		Path jsonPath = data.resolve("json");

		// Fetch all the paths we need
		Path filesOld = jsonPath.resolve("files.old.json");
		Path categoriesOld = jsonPath.resolve("categories.old.json");
		Path files = jsonPath.resolve("files.json");
		Path categories = jsonPath.resolve("categories.json");
		String jsonURL = props.getProperty("jsonURL");
		String filesJsonURL = jsonURL + "files.json";
		String categoriesJsonURL = jsonURL + "categories.json";
		
		try {
			// create temp files to enable atomic move operation
			Path tempFiles = Files.createTempFile(data, "files", ".json");
			Path tempCategories = Files.createTempFile(data, "categories", ".json");
		
			
			// Move previous files to old - replacing previous old files
			if(Files.exists(files, new LinkOption[0]))
				Files.move(files, filesOld, StandardCopyOption.REPLACE_EXISTING);
			if(Files.exists(categories, new LinkOption[0]))
				Files.move(categories, categoriesOld, StandardCopyOption.REPLACE_EXISTING);
			
			// Initialize URLs
			URL filesURL = new URL(filesJsonURL);
			URL categoriesURL = new URL(categoriesJsonURL);
			
			// Transfer data
			ReadableByteChannel filesRBC = Channels.newChannel(filesURL.openStream());
			FileChannel filesFC = FileChannel.open(tempFiles, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			filesFC.transferFrom(filesRBC, 0, Long.MAX_VALUE);
			FilesSingleton.debug("Successfully downloaded files.json");
			
			ReadableByteChannel categoriesRBC = Channels.newChannel(categoriesURL.openStream());
			FileChannel categoriesFC = FileChannel.open(tempCategories, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			categoriesFC.transferFrom(categoriesRBC, 0, Long.MAX_VALUE);
			FilesSingleton.debug("Successfully downloaded categories.json");
			
			// atomic moves
			Files.move(tempFiles, files, StandardCopyOption.ATOMIC_MOVE);
			Files.move(tempCategories, categories, StandardCopyOption.ATOMIC_MOVE);
			
			// cleanup
			filesFC.close();
			categoriesFC.close();
			filesRBC.close();
			categoriesRBC.close();
			FilesSingleton.debug("Everything cleaned up nicely.");
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
