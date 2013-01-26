/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.qsardb.cargo.bodo.*;
import org.qsardb.cargo.map.*;
import org.qsardb.model.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.*;
import com.beust.jcommander.Parameter;

import net.sf.blueobelisk.*;

import org.openscience.cdk.*;
import org.openscience.cdk.qsar.*;

public class DescriptorRegistryManager extends ParameterRegistryManager<DescriptorRegistry, Descriptor> {

	static
	public void main(String... args) throws Exception {
		DescriptorRegistryManager manager = new DescriptorRegistryManager();

		JCommander commander = new JCommander(manager);
		commander.setProgramName(DescriptorRegistryManager.class.getName());

		commander.addCommand(manager.new AddCommand());
		commander.addCommand(manager.new AddCdkCommand());
		commander.addCommand(manager.new PurgeCommand());
		commander.addCommand(manager.new AttachValuesCommand());
		commander.addCommand(manager.new AttachUcumCommand());
		commander.addCommand(manager.new RemoveCommand());

		Command command;

		try {
			commander.parse(args);

			command = Command.getCommand(commander);
		} catch(ParameterException pe){
			commander.usage();

			logger.log(Level.SEVERE, pe.getMessage());

			System.exit(-1);

			return;
		}

		manager.run(command);
	}

	@Override
	public DescriptorRegistry getContainerRegistry(){
		return getQdb().getDescriptorRegistry();
	}

	@Parameters (
		commandNames = {"add"},
		commandDescription = "Add new descriptor"
	)
	private class AddCommand extends ContainerRegistryManager<DescriptorRegistry, Descriptor>.AddCommand {

		@Override
		public Descriptor toContainer(){
			Descriptor result = new Descriptor(super.id);
			result.setName(super.name);

			reserveCargos(result, super.cargos);

			return result;
		}
	}

	@Parameters (
		commandNames = {"add-cdk"},
		commandDescription = "Add all CDK descriptors as new descriptors"
	)
	private class AddCdkCommand extends Command {

		private String application = ("CDK" + " " + CDK.getVersion());


		@Override
		public void execute() throws Exception {
			DescriptorRegistry descriptors = getContainerRegistry();

			CdkDescriptorModel cdkDescriptors = new CdkDescriptorModel();
			for(CdkDescriptor cdkDescriptor : cdkDescriptors){
				Descriptor descriptor = toDescriptor(cdkDescriptor);

				if(descriptor != null && !descriptors.contains(descriptor)){
					descriptors.add(descriptor);
				}
			}

			descriptors.storeChanges();
		}

		private Descriptor toDescriptor(CdkDescriptor cdkDescriptor) throws Exception {
			IMolecularDescriptor descriptor = (IMolecularDescriptor)cdkDescriptor.getDescriptor();
			if(descriptor == null){
				return null;
			}

			String[] descriptorNames = descriptor.getDescriptorNames();
			if(descriptorNames.length != 1){
				throw new IllegalArgumentException(descriptor.toString());
			}

			String id = descriptorNames[0];

			if(id.indexOf('-') > -1){
				id = id.replace('-', '_');
			} // End if

			if(id.indexOf('.') > -1){
				id = id.replace('.', '_');
			} // End if

			if(id.startsWith("khs_")){
				id = "KHS_" + id.substring(4);
			}

			String name = cdkDescriptor.toString();

			// Prefer mixed-case identifier over lower-case identifier
			if(id.equalsIgnoreCase(name) && !id.equals(name)){
				id = name;
			} // End if

			if(id.length() > 1){
				char first = id.charAt(0);
				char second = id.charAt(1);

				if(first != 'n' && Character.isLowerCase(first) && Character.isLowerCase(second)){
					id = Character.toUpperCase(first) + id.substring(1);
				}
			} // End if

			if("naAromAtom".equals(id)){
				id = "nAromAtom";
			}

			Descriptor result = new Descriptor(id);
			result.setName(name);
			result.setApplication(this.application);

			BODOCargo bodoCargo = result.addCargo(BODOCargo.class);

			BODODescriptor bodoDescriptor = BODOUtil.format(descriptor);
			bodoCargo.storeBodoDescriptor(bodoDescriptor);

			return result;
		}
	}

	@Parameters (
		commandNames = {"purge"},
		commandDescription = "Remove all existing descriptors that are not calculable"
	)
	private class PurgeCommand extends Command {

		@Parameter (
			names = {"--categories"},
			description = "The minimum number of unique values"
		)
		private int categories = 1;


		@Override
		public void execute() throws Exception {
			List<Descriptor> badDescriptors = new ArrayList<Descriptor>();

			DescriptorRegistry descriptors = getContainerRegistry();

			for(Descriptor descriptor : descriptors){
				boolean calculable = isValid(descriptor);

				if(!calculable){
					badDescriptors.add(descriptor);
				}
			}

			descriptors.removeAll(badDescriptors);

			descriptors.storeChanges();
		}

		private boolean isValid(Descriptor descriptor) throws IOException {

			if(!descriptor.hasCargo(ValuesCargo.class)){
				return false;
			}

			ValuesCargo valuesCargo = descriptor.getCargo(ValuesCargo.class);

			Map<String, String> values = valuesCargo.loadStringMap();

			Set<String> uniqueValues = new LinkedHashSet<String>(values.values());

			if(uniqueValues.size() == 1){
				String value = (uniqueValues.iterator()).next();

				// All values are "N/A"
				if(value == null){
					return false;
				}
			}

			return (uniqueValues.size() >= this.categories);
		}
	}

	@Parameters (
		commandNames = {"remove"},
		commandDescription = "Remove existing descriptor"
	)
	private class RemoveCommand extends ContainerRegistryManager<DescriptorRegistry, Descriptor>.RemoveCommand {
	}
}