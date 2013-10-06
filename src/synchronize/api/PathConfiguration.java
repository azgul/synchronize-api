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
	
	public Path getCategoriesPath() { return getJsonPath().resolve("categories.json"); }
	
	public Path getImagePath() { return getDataPath().resolve("image"); }
	
	public String getJsonFilesURL() { return properties.getProperty("jsonFilesUrl"); }
	
	public String getJsonCategoriesURL() { return properties.getProperty("jsonCatgoriesUrl"); }
	
	
}
