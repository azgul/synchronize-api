package synchronize.model;

public class Category {
	protected int id;
	protected int parentId;
	protected String name;
	protected String image;
	protected transient Category parent;
	
	public String getName() {
		return name;
	}
	
	public int getParentId() {
		return parentId;
	}
	
	public int getId() {
		return id;
	}
	
	public String getImage() {
		return image;
	}
	
	public Category getParent() {
		return parent;
	}
	
	public void setParent(Category par) {
		parent = par;
	}
	
	public int getDepth() {
		if(parent != null)
			return parent.getDepth()+1;
		else
			return 1;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Category))
			return false;
		return ((Category)other).getId() == this.id;
	}
	
	public Category() {
		
	}
	
	public String toString() {
		return name;
	}
}
