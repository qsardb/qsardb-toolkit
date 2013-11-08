/*
 * Copyright (c) 2013 University of Tartu
 */
package org.qsardb.toolkit.conversion;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.qsardb.conversion.csv.CsvExporter;
import org.qsardb.conversion.table.TableExporter;
import org.qsardb.model.Model;
import org.qsardb.model.Qdb;
import org.qsardb.model.QdbException;
import org.qsardb.toolkit.StorageUtil;

public class SpreadSheetExporter {

	@com.beust.jcommander.Parameter (
		names = {"--source"},
		description = "QDB file or directory",
		required = true
	)
	private File qdbPath = null;

	@com.beust.jcommander.Parameter (
		names = {"--target"},
		description = "CSV file",
		required = true
	)
	private File target = null;

	@com.beust.jcommander.Parameter (
		names = {"--model-id"},
		description = "export data related to model ID"
	)
	private String modelId = null;

	private Qdb qdb;

	public void run() throws Exception {
		qdb = new Qdb(StorageUtil.openInput(qdbPath));

		FileOutputStream os = new FileOutputStream(target);
		TableExporter exporter = new CsvExporter(os);

		if (modelId == null) {
			exporter.prepareDataSet(qdb);
		} else {
			exporter.prepareModel(getModel(modelId));
		}

		try {
			exporter.write();
		} finally {
			exporter.close();
		}
	}

	private Model getModel(String modelId) {
		Model m = qdb.getModel(modelId);
		if (m != null) {
			return m;
		}
		throw new IllegalArgumentException("Nonexistent model: " + modelId);
	}

	private void close() throws IOException, QdbException {
		if (qdb != null) {
			qdb.close();
		}
	}

	public static void main(String[] args) throws Exception {
		SpreadSheetExporter app = new SpreadSheetExporter();

		JCommander commander = new JCommander(app);
		try {
			commander.parse(args);
		} catch (ParameterException e) {
			commander.usage();
			System.exit(64);
		}

		try {
			app.run();
		} finally {
			app.close();
		}
	}

}