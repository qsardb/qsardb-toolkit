/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.workflow;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.qsardb.model.*;
import org.qsardb.query.*;
import org.qsardb.storage.directory.*;
import org.qsardb.toolkit.*;

import org.apache.tools.ant.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

public class Processor {

	@Parameter (
		names = {"--log-level"},
		description = "The logging level. " + LevelConverter.MESSAGE,
		converter = LevelConverter.class
	)
	private Level level = Level.INFO;

	@Parameter (
		names = {"--file"},
		description = "Apache Ant build file"
	)
	private File file = new File("build.xml");

	@Parameter (
		names = {"--target"},
		description = "Target name"
	)
	private String target = null;

	@Parameter (
		names = {"--dir"},
		description = "QDB directory",
		required = true
	)
	private File dir = null;

	@Parameter (
		names = {"--filter"},
		description = "The label filtering expression",
		converter = LabelFilterConverter.class
	)
	private LabelFilter<Compound> filter = null;

	@Parameter (
		names = {"--begin"},
		description = "The Id of the first Compound to process"
	)
	private String begin = null;

	@Parameter (
		names = {"--end"},
		description = "The Id of the last Compound to process"
	)
	private String end = null;


	static
	public void main(String... args) throws Exception {
		Processor processor = new Processor();

		JCommander commander = new JCommander(processor);
		commander.setProgramName(Processor.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		processor.process();
	}

	public void process() throws Exception {
		logger.setLevel(this.level);

		Qdb qdb = new Qdb(new DirectoryStorage(this.dir));

		try {
			build(qdb);
		} finally {
			qdb.close();
		}
	}

	private boolean build(Qdb qdb) throws Exception {
		CompoundRegistry compounds = qdb.getCompoundRegistry();

		try {
			Compound first = compounds.get(this.begin);
			if(this.begin != null && first == null){
				logger.log(Level.WARNING, "Unknown begin compound " + this.begin);
			}

			Compound last = compounds.get(this.end);
			if(this.end != null && last == null){
				logger.log(Level.WARNING, "Unknown end compound " + this.end);
			}

			for(Compound compound : range(compounds, first, last)){

				if(this.filter != null && !this.filter.accept(compound)){
					logger.log(Level.WARNING, "Skipping compound " + compound.getId());

					continue;
				}

				boolean success = build(compound);

				if(!success){
					return false;
				}
			}
		} finally {
			compounds.storeChanges();
		}

		return true;
	}

	private boolean build(Compound compound) throws Exception {
		File propertiesFile = new File(this.dir, "compound.properties");
		if(propertiesFile.exists()){
			throw new IOException("Directory " + this.dir + " is already in use");
		}

		Properties properties = new Properties();
		properties.setProperty("compound.id", compound.getId());
		properties.setProperty("compound.name", compound.getName());
		properties.setProperty("compound.inchi", compound.getInChI());

		OutputStream os = new FileOutputStream(propertiesFile);

		try {
			properties.store(os, null);
		} finally {
			os.close();
		}

		Project project = new Project();
		project.setUserProperty("ant.file", this.file.getAbsolutePath());
		project.setUserProperty("qdb.dir", this.dir.getAbsolutePath());

		DefaultLogger logger = new DefaultLogger();
		logger.setErrorPrintStream(System.err);
		logger.setOutputPrintStream(System.out);
		logger.setMessageOutputLevel(Project.MSG_INFO);

		project.addBuildListener(logger);

		boolean success = false;

		try {
			project.fireBuildStarted();

			project.init();

			ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
			project.addReference("ant.projectHelper", projectHelper);

			projectHelper.parse(project, this.file);

			String target = this.target;
			if(target == null){
				target = project.getDefaultTarget();
			}

			project.executeTarget(target);

			success = true;

			project.fireBuildFinished(null);

			updateCargos(compound);
		} catch(BuildException be){
			project.fireBuildFinished(be);
		} finally {

			if(success){
				propertiesFile.delete();
			}
		}

		return success;
	}

	private void updateCargos(Compound compound) throws Exception {
		File compoundDir = new File(this.dir, compound.qdbPath());
		if(!compoundDir.isDirectory()){
			return;
		}

		List<String> files = Arrays.asList(compoundDir.list());

		// XXX: The notifications about additions and removals are not being propagated to the underlying storage
		Set<String> cargos = compound.getCargos();
		cargos.addAll(files);
		cargos.retainAll(files);

		compound.storeChanges();
	}

	static
	private Collection<Compound> range(Collection<Compound> compounds, Compound first, Compound last){
		List<Compound> compoundList = new ArrayList<Compound>(compounds);

		int firstIndex = compoundList.indexOf(first);
		int lastIndex = compoundList.indexOf(last);

		return compoundList.subList((firstIndex > -1 ? firstIndex : 0), (lastIndex > -1 ? (lastIndex + 1) : compoundList.size()));
	}

	private static final Logger logger = Logger.getLogger(Processor.class.getName());
}