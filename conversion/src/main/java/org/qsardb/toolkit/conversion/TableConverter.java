/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.conversion;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import org.qsardb.cargo.map.*;
import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.structure.*;
import org.qsardb.conversion.regression.*;
import org.qsardb.conversion.table.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

import org.jpmml.manager.*;

import com.beust.jcommander.Parameter;

abstract
public class TableConverter extends Converter {

	@Parameter (
		names = "--interactive",
		description = "Allow console interaction",
		arity = 1
	)
	private boolean interactive = true;

	/**
	 * Number of interactive &quot;conversations&quot; held with the end-user via {@link java.io.Console}.
	 */
	protected int promptCount = 0;

	@Parameter (
		names = {"--id"},
		description = "Id column"
	)
	private String id = null;

	@Parameter (
		names = {"--name"},
		description = "Name column"
	)
	private String name = null;

	@Parameter (
		names = {"--cas"},
		description = "CAS column"
	)
	private String cas = null;

	@Parameter (
		names = {"--inchi"},
		description = "InChI column"
	)
	private String inChI = null;

	@Parameter (
		names = {"--smiles"},
		description = "SMILES column"
	)
	private String smiles = null;

	@Parameter (
		names = {"--labels"},
		description = "Labels column"
	)
	private String labels = null;

	@Parameter (
		names = {"--cargos"},
		description = "Compound Cargo column(s). " + MULTI_COLUMN_MESSAGE
	)
	private List<String> cargos = new ArrayList<String>();

	@Parameter (
		names = "--properties",
		description = "Property column(s). " + MULTI_COLUMN_MESSAGE
	)
	private List<String> properties = new ArrayList<String>();

	@Parameter (
		names = {"--reference"},
		description = "Property references column"
	)
	private String reference = null;

	@Parameter (
		names = "--descriptors",
		description = "Descriptor column(s). " + MULTI_COLUMN_MESSAGE
	)
	private List<String> descriptors = new ArrayList<String>();

	@Parameter (
		names = "--regression",
		description = "(Multi-)linear regression equation. Use a Property Id as a dependent variable and Descriptor Id(s) as independent variables"
	)
	private String regression = null;

	@Parameter (
		names = "--regression-predict",
		description = "Use the above (multi-)linear regression equation for training",
		arity = 1
	)
	private boolean regressionPredict = false;

	@Parameter (
		names = "--model-id",
		description = "Model ID for the regression equation."
	)
	private String modelId = null;

	@Parameter (
		names = "--model-name",
		description = "Model name for the regression equation."
	)
	private String modelName = null;

	@Parameter (
		names = "--prediction",
		description = "Predicted values column."
	)
	private String predictionColumn = null;

	@Parameter (
		names = "--prediction-type",
		description = "Prediction type (training, validation, or testing) for predicted values column."
	)
	private String predictionType = "training";


	@Override
	public void convert() throws Exception {
		Table table = createTable();
		TableSetup tableSetup = createTableSetup();

		if(this.regression != null){
			convertRegression();
		}

		if (predictionColumn != null) {
			createPredictionMapping(tableSetup);
		}

		Table2Qdb.convert(getQdb(), table, tableSetup);

		if(this.regressionPredict){
			performTraining();
		}
	}

	private void convertRegression() throws Exception {
		Qdb qdb = getQdb();

		EquationParser parser = new EquationParser();
		Equation equation = parser.parseEquation(this.regression);

		RegressionModelManager modelManager = RegressionUtil.parse(qdb, equation);

		Property property = FieldNameUtil.decodeProperty(qdb, modelManager.getTarget());

		modelId = modelId != null ? modelId : "regression";
		Model model = new Model(modelId, property);
		model.setName(modelName != null ? modelName : "Regression");

		PMMLCargo pmmlCargo = model.addCargo(PMMLCargo.class);
		pmmlCargo.storePmml(modelManager.getPmml());

		ModelRegistry models = qdb.getModelRegistry();
		models.add(model);
	}

	private void performTraining() throws Exception {
		Qdb qdb = getQdb();

		Model model = qdb.getModel(modelId);
		Property property = model.getProperty();

		DecimalFormat format = Evaluator.getFormat(property);
		if(format == null){
			format = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));
		}

		Prediction prediction = new Prediction(model.getId() + "-training", model, Prediction.Type.TRAINING);
		prediction.setName("Training");

		Map<String, Double> values = new LinkedHashMap<String, Double>();

		EvaluatorFactory evaluatorFactory = EvaluatorFactory.getInstance();

		Evaluator evaluator = evaluatorFactory.getEvaluator(model);

		evaluator.init();

		try {
			Collection<Compound> compounds = qdb.getCompoundRegistry();
			for(Compound compound : compounds){
				Map<Descriptor, String> parameters = new LinkedHashMap<Descriptor, String>();

				DescriptorRegistry descriptors = qdb.getDescriptorRegistry();
				for(Descriptor descriptor : descriptors){
					String value = getValue(descriptor, compound.getId());
					if(value != null){
						parameters.put(descriptor, value);
					}
				}

				Evaluator.Result result = evaluator.evaluate(parameters);

				Double value = new Double(String.valueOf(result.getValue()));

				values.put(compound.getId(), value);
			}
		} finally {
			evaluator.destroy();
		}

		ValuesCargo valuesCargo = prediction.getOrAddCargo(ValuesCargo.class);
		valuesCargo.storeDoubleMap(values, format);

		PredictionRegistry predictions = qdb.getPredictionRegistry();
		predictions.add(prediction);
	}

	private Prediction getOrCreatePrediction() {
		if (modelId == null){
			throw new IllegalArgumentException("Missing model ID (check --regression or --model-id options)");
		}

		Model model = getQdb().getModel(modelId);
		if (model == null){
			throw new IllegalArgumentException("Unknown model: " + modelId);
		}

		String typePrefix = predictionType.split("-", 2)[0].toUpperCase();
		Prediction.Type type = Prediction.Type.valueOf(typePrefix);

		String predId = modelId + "-" + predictionType;

		Prediction prediction = getQdb().getPrediction(predId);
		if (prediction == null) {
			prediction = new Prediction(predId, model, type);
			prediction.setName(predictionType.substring(0,1).toUpperCase()
				+ predictionType.substring(1).toLowerCase());
			getQdb().getPredictionRegistry().add(prediction);
		}

		return prediction;
	}

	private String getValue(Descriptor descriptor, String id) throws IOException {
		ValuesCargo valuesCargo = descriptor.getCargo(ValuesCargo.class);

		Map<String, String> values = valuesCargo.loadStringMap();

		return values.get(id);
	}

	abstract
	protected Table createTable() throws Exception;

	protected TableSetup createTableSetup() throws Exception {
		TableSetup setup = new TableSetup();

		String id = getId();
		if(id != null){
			setup.addMapping(id, new CompoundIdMapping());
		}

		String name = getName();
		if(name != null){
			setup.addMapping(name, new CompoundNameMapping());
		}

		String cas = getCas();
		if(cas != null){
			setup.addMapping(cas, new CompoundCasMapping());
		}

		String inChI = getInChI();
		if(inChI != null){
			setup.addMapping(inChI, new CompoundInChIMapping());
		}

		String smiles = getSmiles();
		if(smiles != null){
			setup.addMapping(smiles, new CompoundCargoMapping(ChemicalMimeData.DAYLIGHT_SMILES.getId()));
		}

		String labels = getLabels();
		if(labels != null){
			setup.addMapping(labels, new CompoundLabelsMapping());
		}

		for(String column : this.cargos){
			createCargoMapping(setup, column);
		}

		for(String column : prepareColumns(this.properties)){
			createPropertyMapping(setup, column);
		}

		for(String column : prepareColumns(this.descriptors)){
			createDescriptorMapping(setup, column);
		}

		return setup;
	}

	protected List<String> prepareColumns(List<String> columns){
		return columns;
	}

	private void createCargoMapping(TableSetup setup, String column){
		CargoInfo info = prepareCargoInfo(column);

		Mapping mapping = new CompoundCargoMapping(info.getId());

		setup.addMapping(column, mapping);
	}

	private void createPropertyMapping(TableSetup setup, String column){
		ParameterInfo info = prepareParameterInfo("Property", column);

		PropertyRegistry properties = getQdb().getPropertyRegistry();

		Property property = properties.get(info.getId());
		if(property == null){
			property = new Property(info.getId());
			property.setName(info.getName());

			properties.add(property);
		}

		Mapping mapping = new PropertyValuesMapping<String>(property, new StringFormat());
		if(info.getPattern() != null){
			mapping = new PropertyValuesMapping<Double>(property, new DoubleFormat(info.getPattern()));
		}

		setup.addMapping(column, mapping);

		if(this.reference != null){
			mapping = new PropertyReferencesMapping(property){

				@Override
				public String filter(String string){

					if(string != null){

						if(string.lastIndexOf('/') > -1){
							string = filterURL(string);
						}
					}

					return string;
				}

				private String filterURL(String string){

					try {
						URL url = new URL(string);

						String path = url.getPath();

						while(path.length() > 0){
							char c = path.charAt(0);

							if(Character.isLetterOrDigit(c)){
								break;
							}

							path = path.substring(1);
						} // End while

						while(path.length() > 0){
							char c = path.charAt(path.length() - 1);

							if(Character.isLetterOrDigit(c)){
								break;
							}

							path = path.substring(0, path.length() - 1);
						}

						if(path.length() > 0){
							return path;
						}
					} catch(MalformedURLException mue){
						// Ignored
					}

					return string;
				}
			};

			setup.addMapping(this.reference, mapping);
		}
	}

	private void createDescriptorMapping(TableSetup setup, String column){
		ParameterInfo info = prepareParameterInfo("Descriptor", column);

		DescriptorRegistry descriptors = getQdb().getDescriptorRegistry();

		Descriptor descriptor = descriptors.get(info.getId());
		if(descriptor == null){
			descriptor = new Descriptor(info.getId());
			descriptor.setName(info.getName());

			descriptors.add(descriptor);
		}

		Mapping mapping = new DescriptorValuesMapping<String>(descriptor, new StringFormat());
		if(info.getPattern() != null){
			mapping = new DescriptorValuesMapping<Double>(descriptor, new DoubleFormat(info.getPattern()));
		}

		setup.addMapping(column, mapping);
	}

	private void createPredictionMapping(TableSetup setup){
		Prediction prediction = getOrCreatePrediction();
		Mapping valuesMapping = new PredictionValuesMapping<String>(prediction, new StringFormat());
		setup.addMapping(predictionColumn, valuesMapping);
	}

	protected String prepareId(String column){
		return "column_" + column.replaceAll("\\s", "_");
	}

	protected String prepareName(String column){
		return "Column " + column;
	}

	private CargoInfo prepareCargoInfo(String column){
		CargoInfo result = new CargoInfo();
		result.setId(prepareId(column));

		if(isInteractive()){
			Console console = System.console();

			if(this.promptCount > 0){
				console.printf("%n");
			}

			console.printf("%s. Compound Cargo (column '%s')%n", Integer.valueOf(this.promptCount + 1), column);

			String id = normalize(console.readLine("\t" + "Id (default: '%s'): ", result.getId()));
			if(id != null){
				result.setId(id);
			}

			this.promptCount++;
		}

		return result;
	}

	private ParameterInfo prepareParameterInfo(String title, String column){
		ParameterInfo result = new ParameterInfo();
		result.setId(prepareId(column));
		result.setName(prepareName(column));
		result.setPattern(null);

		if(isInteractive()){
			Console console = System.console();

			if(this.promptCount > 0){
				console.printf("%n");
			}

			console.printf("%d. %s attributes (column '%s')%n", Integer.valueOf(this.promptCount + 1), title, column);

			String id = normalize(console.readLine("\t" + "Id (default: '%s'): ", result.getId()));
			if(id != null){
				result.setId(id);
			}

			String name = normalize(console.readLine("\t" + "Name (default: '%s'): ", result.getName()));
			if(name != null){
				result.setName(name);
			}

			String pattern = normalize(console.readLine("\t" + "Value format pattern (default: as-is): "));
			if(pattern != null){
				result.setPattern(pattern);
			}

			this.promptCount++;
		}

		return result;
	}

	private String normalize(String string){

		if(string != null){
			string = string.trim();

			if("".equals(string)){
				string = null;
			}
		}

		return string;
	}

	public String getId(){
		return this.id;
	}

	protected void setId(String id){
		this.id = id;
	}

	public String getName(){
		return this.name;
	}

	protected void setName(String name){
		this.name = name;
	}

	public String getCas(){
		return this.cas;
	}

	protected void setCas(String cas){
		this.cas = cas;
	}

	public String getInChI(){
		return this.inChI;
	}

	protected void setInChI(String inChI){
		this.inChI = inChI;
	}

	public String getSmiles(){
		return this.smiles;
	}

	protected void setSmiles(String smiles){
		this.smiles = smiles;
	}

	public String getLabels(){
		return this.labels;
	}

	public void setLabels(String labels){
		this.labels = labels;
	}

	public boolean isInteractive(){
		return this.interactive;
	}

	protected void setInteractive(boolean interactive){
		this.interactive = interactive;
	}

	static
	private class CargoInfo {

		private String id = null;


		public String getId(){
			return this.id;
		}

		public void setId(String id){
			this.id = id;
		}
	}

	static
	private class ParameterInfo {

		private String id = null;

		private String name = null;

		private String pattern = null;


		public String getId(){
			return this.id;
		}

		public void setId(String id){
			this.id = id;
		}

		public String getName(){
			return this.name;
		}

		public void setName(String name){
			this.name = name;
		}

		public String getPattern(){
			return this.pattern;
		}

		public void setPattern(String pattern){
			this.pattern = pattern;
		}
	}

	protected static final String MULTI_COLUMN_MESSAGE = "If there are multiple columns, use commas (',') to separate them";
}