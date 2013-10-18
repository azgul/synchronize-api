package synchronize.api;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

public class PathConfiguration {
	
	private Properties properties;
	
	public PathConfiguration(Properties props) {
		properties = props;
	}
	
	private Path dataPath;
	public Path getDataPath() {
		if(dataPath == null)
			dataPath = FileSystems.getDefault().getPath(properties.getProperty("dataPath"));
		
		return dataPath;
	}
	
	public Path getJsonPath() { return getDataPath().resolve("json"); }
	
	public Path getFilesPath() { return getJsonPath().resolve("files.json"); }
	
	public Path getOldFilesPath() { return getJsonPath().resolve("files.old.json"); }
	
	public Path getCategoriesPath() { return getJsonPath().resolve("categories.json"); }
	
	public Path getOldCategoriesPath() { return getJsonPath().resolve("categories.old.json"); }
	
	public Path getImagePath() { return getDataPath().resolve("images"); }
	
	public String getJsonFilesURL() { return properties.getProperty("jsonFilesUrl"); }
	
	public String getJsonCategoriesURL() { return properties.getProperty("jsonCategoriesUrl"); }
	
	public Path getPdfPath() { return getDataPath().resolve("pdfs"); }
}
