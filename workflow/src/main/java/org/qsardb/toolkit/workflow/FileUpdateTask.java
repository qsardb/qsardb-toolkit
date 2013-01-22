/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.workflow;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.apache.tools.ant.*;

abstract
public class FileUpdateTask extends Task {

	private File file = null;

	private String encoding = null;


	abstract
	public List<String> update(List<String> lines);

	@Override
	public void execute() throws BuildException {
		File file = getFile();
		if(file == null){
			throw new BuildException("Attribute \"file\" not set");
		}

		String encoding = getEncoding();

		try {
			List<String> lines = FileUtils.readLines(file, encoding);

			lines = update(lines);

			FileUtils.writeLines(file, encoding, lines);
		} catch(Exception e){
			e.printStackTrace(System.err);

			throw new BuildException(e);
		}
	}

	public File getFile(){
		return this.file;
	}

	public void setFile(File file){
		this.file = file;
	}

	public String getEncoding(){
		return this.encoding;
	}

	public void setEncoding(String encoding){
		this.encoding = encoding;
	}
}