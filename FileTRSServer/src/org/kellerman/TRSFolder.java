package org.kellerman;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Spring;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TRSFolder extends TRSObject {

	private Vector<TRSFolder> folders = new Vector<TRSFolder>();
	private Vector<TRSFile> files = new Vector<TRSFile>();
	private TRSFileFilter filter = new TRSFileFilter();

	public TRSFolder(File root) {
		super(root);
		File[] list = root.listFiles(filter);
		for (int i = 0; i < list.length; i++) {
			if (list[i].isFile()) {
				TRSFile tFile = new TRSFile(list[i]);
				addFile(tFile);
			} else if (list[i].isDirectory()) {
				TRSFolder tFolder = new TRSFolder(list[i]);
				addFolder(tFolder);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		TRSFolder root;
		Model RDFmodel = ModelFactory.createDefaultModel();
		System.out.println(args[0]);
		String rootPath = args[0];
		File f = new File(rootPath);
		if (!f.exists()) {
			System.out.println("Folder " + rootPath + " not found.");
		} else if (!f.isDirectory()) {
			System.out.println(rootPath + " is not a folder.");
		} else {
			System.out.println("Current directory: " + rootPath);
			System.out.println("Name: " + f.getName());
			System.out.println("Path: " + f.getPath());
			System.out.println("Last modified: " + f.lastModified());
			root = new TRSFolder(f);
			root.print();
		}
	}

	public Vector<TRSFolder> getFolders() {
		return folders;
	}

	public void addFolder(TRSFolder f) {
		if (getFolders() == null) {
			setFolders(new Vector<TRSFolder>());
		}
		getFolders().add(f);
	}

	public void addFile(TRSFile f) {
		if (getFiles() == null) {
			setFiles(new Vector<TRSFile>());
		}
		getFiles().add(f);
	}

	public void setFolders(Vector<TRSFolder> folders) {
		this.folders = folders;
	}

	public Vector<TRSFile> getFiles() {
		return files;
	}

	public void setFiles(Vector<TRSFile> files) {
		this.files = files;
	}

	public void print() {
		String indent = "";
		print(indent);
	}

	protected void print(String indent) {
		super.print(indent);
		String indentIncrement = "  ";

		System.out.println(indent + "Files:");
		if (getFiles().size() > 0) {
			for (int i = 0; i < getFiles().size(); i++) {
				getFiles().get(i).print(indent + indentIncrement);
			}
		} else {
			System.out.println(indent + indentIncrement + "none");
		}

		System.out.println(indent + "Folders:");
		if (getFolders().size() > 0) {
			for (int i = 0; i < getFolders().size(); i++) {
				getFolders().get(i).print(indent + indentIncrement);
			}
		} else {
			System.out.println(indent + indentIncrement + "none");
		}
	}

}
