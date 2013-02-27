package org.kellerman;

import java.io.File;
import java.net.URI;

public class TRSObject {

	private String name;
	private String path;
	private URI uri;
	
	public TRSObject (File root) {
		setName(root.getName());
		setPath(root.getPath());
		setUri(root.toURI());
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	protected void print(String indent) {
		System.out.println(indent + "path: " + getPath());
		System.out.println(indent + "uri: " + getUri());
	}

}
