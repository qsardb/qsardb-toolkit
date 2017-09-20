/*
 * Copyright (c) 2013 University of Tartu
 */
package org.qsardb.toolkit.conversion;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.qsardb.conversion.table.TableExporter;
import org.qsardb.conversion.csv.CsvExporter;
import org.qsardb.conversion.excel.ExcelExporter;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;
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

	@com.beust.jcommander.Parameter (
		names = {"--prediction-id"},
		description = "export data related to prediction ID"
	)
	private String predictionId = null;

	private Qdb qdb;

	public void run() throws Exception {
		qdb = new Qdb(StorageUtil.openInput(qdbPath));

		TableExporter exporter = getTableExporter();

		if (modelId != null) {
			exporter.prepareModel(getModel(modelId));
		} else if (predictionId != null) {
			exporter.preparePrediction(getPrediction(predictionId));
		} else {
			exporter.prepareDataSet(qdb);
		}

		try {
			exporter.write();
		} finally {
			exporter.close();
		}
	}

	private TableExporter getTableExporter() throws IOException {
		FileOutputStream os = new FileOutputStream(target);

		String name = target.getName().toLowerCase();
		if (name.endsWith(".xlsx")) {
			return new ExcelExporter(os);
		} else if (name.endsWith(".xls")) {
			return new ExcelExporter(os, true);
		} else {
			return new CsvExporter(os);
		}
	}

	private Model getModel(String modelId) {
		Model m = qdb.getModel(modelId);
		if (m != null) {
			return m;
		}
		throw new IllegalArgumentException("Nonexistent model: " + modelId);
	}

	private Prediction getPrediction(String predictionId) {
		Prediction p = qdb.getPrediction(predictionId);
		if (p != null) {
			return p;
		}
		throw new IllegalArgumentException("Nonexistent predictionId: " + predictionId);
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