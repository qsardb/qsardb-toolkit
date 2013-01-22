/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit;

import java.io.*;

import org.qsardb.model.*;
import org.qsardb.storage.directory.*;
import org.qsardb.storage.zipfile.*;

public class StorageUtil {

	private StorageUtil(){
	}

	static
	public Storage openInput(File file) throws IOException {

		if(file.isFile()){
			return new ZipFileInput(file);
		} else

		if(file.isDirectory()){
			return new DirectoryStorage(file);
		}

		throw new IOException("Path " + file.getAbsolutePath() + " does not denote an exisiting file or directory");
	}

	static
	public Storage openOutput(File file) throws IOException {

		if(file.isFile()){
			return new ZipFileOutput(file);
		} else

		if(file.isDirectory()){
			return new DirectoryStorage(file);
		}

		throw new IOException("Path " + file.getAbsolutePath() + " does not denote an existing file or directory");
	}
}