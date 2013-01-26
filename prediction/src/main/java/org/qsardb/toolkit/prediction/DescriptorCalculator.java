/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.logging.*;

import org.qsardb.cargo.bodo.*;
import org.qsardb.cargo.map.*;
import org.qsardb.cargo.structure.*;
import org.qsardb.model.*;
import org.qsardb.storage.directory.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

import org.openscience.cdk.exception.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.qsar.*;

public class DescriptorCalculator {

	@Parameter (
		names = {"--log-level"},
		description = "The logging level. " + LevelConverter.MESSAGE,
		converter = LevelConverter.class
	)
	private Level level = Level.INFO;

	@Parameter (
		names = {"--dir"},
		description = "QDB directory",
		required = true
	)
	private File dir = null;

	@Parameter (
		names = {"--incremental"},
		description = "Incremental mode",
		arity = 1
	)
	private boolean incremental = true;

	private Qdb qdb = null;

	private DescriptorValueCache cache = new DescriptorValueCache();


	static
	public void main(String... args) throws Exception {
		DescriptorCalculator calculator = new DescriptorCalculator();

		JCommander commander = new JCommander(calculator);
		commander.setProgramName(DescriptorCalculator.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		calculator.run();
	}

	public void run() throws Exception {
		logger.setLevel(this.level);

		Qdb qdb = new Qdb(new DirectoryStorage(this.dir));

		setQdb(qdb);

		try {
			try {
				calculate();
			} finally {
				qdb.close();
			}
		} finally {
			setQdb(null);
		}
	}

	private void calculate() throws Exception {
		CompoundRegistry compounds = getQdb().getCompoundRegistry();
		DescriptorRegistry descriptors = getQdb().getDescriptorRegistry();

		List<Collector> collectors = new ArrayList<Collector>();

		for(Descriptor descriptor : descriptors){
			logger.log(Level.FINE, "Descriptor " + descriptor.getId());

			IMolecularDescriptor molecularDescriptor;

			if(descriptor.hasCargo(BODOCargo.class)){
				BODOCargo bodoCargo = descriptor.getCargo(BODOCargo.class);

				molecularDescriptor = (IMolecularDescriptor)BODOUtil.parse(bodoCargo.loadBodoDescriptor());

				logger.log(Level.FINER, "Loaded BODO implementation");
			} else

			{
				logger.log(Level.FINER, "Cannot recognize any implementations");

				continue;
			}

			DescriptorInfo descriptorInfo = new DescriptorInfo();
			descriptorInfo.setId(descriptor.getId());
			descriptorInfo.setDescriptor(molecularDescriptor);

			Collector collector = new Collector(descriptorInfo);

			if(descriptor.hasCargo(ValuesCargo.class)){
				ValuesCargo valuesCargo = descriptor.getCargo(ValuesCargo.class);

				(collector.getValues()).putAll(valuesCargo.loadBigDecimalMap());
			}

			collectors.add(collector);
		}

		logger.log(Level.INFO, "Calculating " + collectors.size() + " descriptors");

		for(Compound compound : compounds){
			logger.log(Level.FINE, "Compound " + compound.getId());

			IAtomContainer molecule;

			if(compound.hasCargo(ChemicalMimeData.MDL_MOLFILE.getId())){
				Cargo<Compound> molfileCargo = compound.getCargo(ChemicalMimeData.MDL_MOLFILE.getId());

				InputStream is = molfileCargo.getInputStream();

				try {
					molecule = CdkUtil.parseMolfile(is);

					logger.log(Level.FINER, "Loaded MDL molfile");
				} finally {
					is.close();
				}
			} else

			if(compound.hasCargo(ChemicalMimeData.DAYLIGHT_SMILES.getId())){
				Cargo<Compound> smilesCargo = compound.getCargo(ChemicalMimeData.DAYLIGHT_SMILES.getId());

				molecule = CdkUtil.parseSMILES(smilesCargo.loadString());

				logger.log(Level.FINER, "Loaded SMILES");
			} else

			if(compound.hasCargo("smiles")){
				Cargo<Compound> smilesCargo = compound.getCargo("smiles");

				molecule = CdkUtil.parseSMILES(smilesCargo.loadString());

				logger.log(Level.FINER, "Loaded SMILES");
			} else

			{
				logger.log(Level.FINER, "Cannot recognize any structure formats");

				continue;
			}

			molecule = DescriptorUtil.prepareMolecule(molecule);

			CompoundInfo compoundInfo = new CompoundInfo();
			compoundInfo.setId(compound.getId());
			compoundInfo.setMolecule(molecule);

			this.cache.clear();

			for(Collector collector : collectors){
				collector.collect(compoundInfo);
			}
		}

		for(Collector collector : collectors){
			DescriptorInfo descriptorInfo = collector.getDescriptor();

			Descriptor descriptor = descriptors.get(descriptorInfo.getId());

			ValuesCargo valuesCargo = descriptor.getOrAddCargo(ValuesCargo.class);
			valuesCargo.storeBigDecimalMap(truncate(round(collector.getValues())));
		}

		descriptors.storeChanges();
	}

	private Object calculate(IMolecularDescriptor descriptor, IAtomContainer molecule) throws CDKException {

		try {
			return this.cache.calculate(descriptor, molecule);
		} catch(DescriptorException de){
			return null;
		}
	}

	public Qdb getQdb(){
		return this.qdb;
	}

	private void setQdb(Qdb qdb){
		this.qdb = qdb;
	}

	public boolean isIncremental(){
		return this.incremental;
	}

	static
	private Map<String, BigDecimal> round(Map<String, BigDecimal> values){
		ScaleFrequencyMap map = ScaleFrequencyMap.sample(values.values());

		int min = map.minScale();
		int max = map.maxScale();

		if((max - min) <= 2){
			return values;
		}

		Map<String, BigDecimal> result = new LinkedHashMap<String, BigDecimal>();

		for(Map.Entry<String, BigDecimal> entry : values.entrySet()){
			String id = entry.getKey();
			BigDecimal value = entry.getValue();

			if(value == null || value.scale() <= (min + 2)){
				result.put(id, value);
			} else

			{
				BigDecimal minValue = value.setScale(min, RoundingMode.HALF_UP);
				BigDecimal minPlus2Value = value.setScale(min + 2, RoundingMode.HALF_UP);

				if((minValue).compareTo(minPlus2Value) == 0){
					result.put(id, minValue);
				} else

				{
					result.put(id, minPlus2Value);
				}
			}
		}

		return result;
	}

	static
	private Map<String, BigDecimal> truncate(Map<String, BigDecimal> values){
		Map<String, BigDecimal> result = new LinkedHashMap<String, BigDecimal>();

		for(Map.Entry<String, BigDecimal> entry : values.entrySet()){
			String id = entry.getKey();
			BigDecimal value = entry.getValue();

			if(value == null){
				result.put(id, value);
			} else

			{
				BigDecimal longValue = new BigDecimal(value.longValue());

				if((value).compareTo(longValue) == 0){
					result.put(id, longValue);
				} else

				{
					result.put(id, value);
				}
			}
		}

		return result;
	}

	static
	private class CompoundInfo {

		private String id = null;

		private IAtomContainer molecule = null;


		public String getId(){
			return this.id;
		}

		private void setId(String id){
			this.id = id;
		}

		public IAtomContainer getMolecule(){
			return this.molecule;
		}

		private void setMolecule(IAtomContainer molecule){
			this.molecule = molecule;
		}
	}

	static
	private class DescriptorInfo {

		private String id = null;

		private IMolecularDescriptor descriptor = null;


		public String getId(){
			return this.id;
		}

		private void setId(String id){
			this.id = id;
		}

		public IMolecularDescriptor getDescriptor(){
			return this.descriptor;
		}

		private void setDescriptor(IMolecularDescriptor descriptor){
			this.descriptor = descriptor;
		}
	}

	private class Collector {

		private DescriptorInfo descriptor = null;

		private Map<String, BigDecimal> values = new LinkedHashMap<String, BigDecimal>();


		public Collector(DescriptorInfo descriptor){
			setDescriptor(descriptor);
		}

		public void collect(CompoundInfo compound) throws Exception {
			DescriptorInfo descriptor = getDescriptor();

			String id = compound.getId();

			if(isIncremental()){

				if(this.values.containsKey(id)){
					return;
				}
			}

			Object result = calculate(descriptor.getDescriptor(), compound.getMolecule());

			if(result == null){
				this.values.put(id, null);
			} else

			{
				this.values.put(id, BigDecimalFormat.toBigDecimal(result));
			}
		}

		public DescriptorInfo getDescriptor(){
			return this.descriptor;
		}

		private void setDescriptor(DescriptorInfo descriptor){
			this.descriptor = descriptor;
		}

		public Map<String, BigDecimal> getValues(){
			return this.values;
		}
	}

	private static final Logger logger = Logger.getLogger(DescriptorCalculator.class.getName());
}