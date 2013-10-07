package synchronize.model;

public class SyncFile {
	protected int id;
	protected String name;
	protected String file;
	protected String version;
	protected String author;
	protected int category;
	protected String description;
	protected String language;
	protected String[] tags;
	protected String preview;
	protected String icon;
	protected String image;
	
	public String getFileName() {
		String[] fileParts = file.split("/");
		return fileParts[fileParts.length-1];
	}
	
	public String getFile() { return file; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public int getCategory() { return category; }
	public String getLanguage() { return language; }
	public String getKeywords() {
		String delim = " ";
		String result = "";
		for(int i = 0; i < tags.length; i++) {
			if(i == tags.length-1)
				result += tags[i];
			else
				result += tags[i]+delim;
		}
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof SyncFile))
			return false;
		
		SyncFile f = (SyncFile)o;
		
		// compare all fields
		return f.id == id &&
				f.name.equals(name) &&
				f.file.equals(file) &&
				f.version.equals(version) &&
				f.author.equals(author) &&
				f.category == category &&
				f.description.equals(description) &&
				f.language.equals(language) &&
				f.tags.equals(tags) &&
				f.preview.equals(preview) &&
				f.icon.equals(icon) &&
				f.image.equals(image);
	}
}
