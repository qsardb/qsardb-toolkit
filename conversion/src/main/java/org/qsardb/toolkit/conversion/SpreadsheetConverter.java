/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.conversion;

import java.io.*;
import java.util.*;

import org.qsardb.conversion.csv.*;
import org.qsardb.conversion.excel.*;
import org.qsardb.conversion.opendocument.*;
import org.qsardb.conversion.spreadsheet.*;
import org.qsardb.conversion.table.*;

import com.beust.jcommander.*;

public class SpreadsheetConverter extends TableConverter {

	@Parameter (
		names = {"--header"},
		description = "Ignore the first row",
		arity = 1
	)
	private boolean header = true;

	@Parameter (
		names = {"--header-setup"},
		description = "Attempt Compound attributes' mapping based on the first row (implies --header)",
		arity = 1
	)
	private boolean headerSetup = false;

	private Map<Column, Cell> headerValues = null;

	@Parameter (
		names = {"--begin"},
		description = "The first row to convert"
	)
	private String begin = null;

	@Parameter (
		names = {"--end"},
		description = "The last row to convert"
	)
	private String end = null;

	@Parameter (
		names = "--source",
		description = "Spreadsheet (CSV, Excel, OpenDocument) file",
		required = true
	)
	private File source = null;

	@Parameter (
		names = "--select-where",
		description = "Expression for filtering table rows"
	)
	private String filterExpression = null;


	static
	public void main(String... args) throws Exception {
		SpreadsheetConverter converter = new SpreadsheetConverter();

		JCommander commander = new JCommander(converter);
		commander.setProgramName(SpreadsheetConverter.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		converter.run();
	}

	@Override
	protected Table createTable() throws Exception {

		if(!this.source.isFile()){
			throw new IOException(this.source.getAbsolutePath() + " is not a file");
		}

		Workbook workbook = getWorkbook();

		return workbook.getWorksheet(0);
	}

	@Override
	protected TableSetup createTableSetup() throws Exception {

		if(this.headerSetup){
			parseHeader();

			this.header = true;
		}

		TableSetup setup = super.createTableSetup();

		if(this.header){
			setup.setIgnored("1");
		}

		setup.setBeginRow(this.begin);
		setup.setEndRow(this.end);

		if (this.filterExpression != null) {
			setup.setRowFilter(filterExpression);
		}

		return setup;
	}

	private void parseHeader() throws Exception {
		Table table = createTable();

		Row header = table.getRow("1");

		this.headerValues = header.getValues();

		Collection<Map.Entry<Column, Cell>> entries = this.headerValues.entrySet();
		for(Map.Entry<Column, Cell> entry : entries){
			parseHeaderMapping(entry.getKey(), entry.getValue());
		}
	}

	private void parseHeaderMapping(Column column, Cell cell){
		String text = cell.getText();

		if("id".equalsIgnoreCase(text)){

			if(getId() == null){
				setId(column.getId());
			}
		} else

		if("name".equalsIgnoreCase(text)){

			if(getName() == null){
				setName(column.getId());
			}
		} else

		if("cas".equalsIgnoreCase(text) || "cas rn".equalsIgnoreCase(text) || "casrn".equalsIgnoreCase(text)){

			if(getCas() == null){
				setCas(column.getId());
			}
		} else

		if("inchi".equalsIgnoreCase(text)){

			if(getInChI() == null){
				setInChI(column.getId());
			}
		} else

		if("labels".equalsIgnoreCase(text)){

			if(getLabels() == null){
				setLabels(column.getId());
			}
		} else

		if("smiles".equalsIgnoreCase(text)){

			if(getSmiles() == null){
				setSmiles(column.getId());
			}
		}
	}

	@Override
	protected String prepareId(String column){
		Cell cell = getHeader(column);
		if(cell != null){
			return (cell.getText()).replaceAll("\\s", "_");
		}

		return super.prepareId(column);
	}

	@Override
	protected String prepareName(String column){
		Cell cell = getHeader(column);
		if(cell != null){
			return cell.getText();
		}

		return super.prepareName(column);
	}

	private Cell getHeader(String id){

		if(this.headerValues != null){
			return this.headerValues.get(new Column(id));
		}

		return null;
	}

	@Override
	protected List<String> prepareColumns(List<String> columns){
		List<String> result = new ArrayList<String>();

		for(String column : columns){
			int slash = column.indexOf('-');

			if(slash > -1){
				int begin = Worksheet.parseColumnId(column.substring(0, slash));
				int end = Worksheet.parseColumnId(column.substring(slash + 1));

				if(begin >= end){
					throw new IllegalArgumentException(column);
				}

				for(int index = begin; index <= end; index++){
					result.add(Worksheet.formatColumnId(index));
				}
			} else

			{
				result.add(column);
			}
		}

		return result;
	}

	private Workbook getWorkbook() throws Exception {
		InputStream is = new FileInputStream(this.source);

		try {
			String name = this.source.getName();

			String extension = name.substring(name.indexOf('.') + 1);

			if(extension.equalsIgnoreCase("csv")){
				return new CsvWorkbook(is, CsvUtil.getFormat(this.source));
			} else

			if(extension.equalsIgnoreCase("xls") || extension.equalsIgnoreCase("xlsx")){
				return new ExcelWorkbook(is);
			} else

			if(extension.equalsIgnoreCase("ods")){
				return new OpenDocumentWorkbook(is);
			}
		} finally {
			is.close();
		}

		throw new IllegalArgumentException(this.source.getAbsolutePath());
	}
}