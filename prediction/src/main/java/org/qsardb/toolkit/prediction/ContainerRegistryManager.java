/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.io.*;
import java.util.*;

import org.qsardb.model.*;
import org.qsardb.toolkit.*;

import com.beust.jcommander.Parameter;

abstract
public class ContainerRegistryManager<R extends ContainerRegistry<R, C>, C extends Container<R, C>> extends Manager {

	abstract
	public R getContainerRegistry();


	abstract
	protected class AddCommand extends Command {

		@Parameter (
			names = {"--id"},
			description = "Id",
			required = true
		)
		protected String id = null;

		@Parameter (
			names = {"--name"},
			description = "Name"
		)
		protected String name = null;

		@Parameter (
			names = {"--cargos"},
			description = "Cargo(s) to be reserved. " + MULTI_VALUE_MESSAGE
		)
		protected List<String> cargos = new ArrayList<String>();


		abstract
		public C toContainer();

		@Override
		public void execute() throws Exception {
			R registry = getContainerRegistry();

			C container = toContainer();
			registry.add(container);

			registry.storeChanges();
		}

		protected void reserveCargos(C container, Collection<String> ids){

			for(String id : ids){
				Cargo<C> cargo = container.addCargo(id);

				cargo.setPayload(ByteArrayPayload.EMPTY);
			}
		}
	}

	abstract
	protected class AttachCommand extends Command {

		@Parameter (
			names = {"--id"},
			description = "Id",
			required = true
		)
		protected String id = null;


		abstract
		public String getId();

		abstract
		public Payload getPayload();

		@Override
		public void execute() throws Exception {
			R registry = getContainerRegistry();

			C container = registry.get(this.id);
			if(container == null){
				throw new IllegalArgumentException("Id \'" + this.id + "\' not found");
			}

			Cargo<C> cargo = container.getOrAddCargo(getId());
			cargo.setPayload(getPayload());

			registry.storeChanges();
		}
	}

	abstract
	protected class AttachFileCommand extends AttachCommand {

		abstract
		public File getFile();

		@Override
		public Payload getPayload(){
			File file = getFile();

			if(file != null){
				return new FilePayload(file);
			}

			return ByteArrayPayload.EMPTY;
		}
	}

	abstract
	protected class RemoveCommand extends Command {

		@Parameter (
			names = {"--id"},
			description = "Id",
			required = true
		)
		protected String id = null;


		@Override
		public void execute() throws Exception {
			R registry = getContainerRegistry();

			C container = registry.get(this.id);
			if(container == null){
				throw new IllegalArgumentException("Id \'" + this.id + "\' not found");
			}

			registry.remove(container);

			registry.storeChanges();
		}
	}

	protected static final String MULTI_VALUE_MESSAGE = "If there are multiple values, use commas (',') to separate them";
}