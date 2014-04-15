/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.qsardb.cargo.bibtex.BibTeXCargo;

import org.qsardb.model.*;
import org.qsardb.resolution.doi.DOIResolver;
import org.qsardb.toolkit.*;


abstract
public class ContainerRegistryManager<R extends ContainerRegistry<R, C>, C extends Container<R, C>> extends Manager {

	abstract
	public R getContainerRegistry();

	abstract
	protected class ContainerCommand extends Command {

		@Parameter (
			names = {"--id"},
			description = "Id",
			required = true
		)
		protected String id = null;

		protected C findContainer() {
			R registry = getContainerRegistry();

			C container = registry.get(this.id);
			if(container == null){
				throw new IllegalArgumentException("Id \'" + this.id + "\' not found");
			}

			return container;
		}
	}

	abstract
	protected class AddCommand extends ContainerCommand {

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
	protected class AttachCommand extends ContainerCommand {

		abstract
		public String getId();

		abstract
		public Payload getPayload();

		@Override
		public void execute() throws Exception {
			C container = findContainer();
			Cargo<C> cargo = container.getOrAddCargo(getId());
			cargo.setPayload(getPayload());

			getContainerRegistry().storeChanges();
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

	@Parameters (
		commandNames = {"attach-bibtex"},
		commandDescription = "Attach BibTeX Cargo"
	)
	protected class AttachBibTeXCommand extends AttachFileCommand {

		@Parameter (
			names = {"--bibtex"},
			description = "BibTeX formatted file",
			required = true
		)
		private File file = null;

		@Override
		public String getId(){
			return BibTeXCargo.ID;
		}

		@Override
		public File getFile() {
			return this.file;
		}
	}

	@Parameters (
		commandNames = {"add-citation"},
		commandDescription = "Add bibliography entry to the BibTeX Cargo"
	)
	protected class AddBibTeXCommand extends ContainerCommand {

		@Parameter (
			names = ("--doi"),
			description = "Resolve citation from DOI code",
			required = true
		)
		private String doi = null;

		@Override
		public void execute() throws Exception {
			C container = findContainer();

			BibTeXEntry entry = resolveDOI();

			BibTeXCargo cargo = container.getOrAddCargo(BibTeXCargo.class);

			BibTeXDatabase db = loadBibTeX(cargo);
			if (db.resolveEntry(entry.getKey()) != null) {
				logger.log(Level.WARNING, "Entry for {0} already exists.", entry.getKey().getValue());
				return;
			}
			db.addObject(entry);

			cargo.storeBibTeX(db);
			getContainerRegistry().storeChanges();
		}

		private BibTeXEntry resolveDOI() {
			try {
				return DOIResolver.asBibTeXEntry(this.doi);
			} catch (IOException e) {
				throw new IllegalArgumentException("Unresolvable DOI: "+this.doi, e);
			}
		}

		private BibTeXDatabase loadBibTeX(BibTeXCargo cargo) throws Exception {
			if (cargo.isLoadable()) {
				return cargo.loadBibTeX();
			} else {
				return new BibTeXDatabase();
			}
		}
	}

	abstract
	protected class RemoveCommand extends ContainerCommand {

		@Override
		public void execute() throws Exception {
			C container = findContainer();

			getContainerRegistry().remove(container);

			getContainerRegistry().storeChanges();
		}
	}

	abstract
	protected class SetCommand extends ContainerCommand {

		@Parameter (
			names = {"--name"},
			description = "Set name attribute"
		)
		protected String name = null;

		public void handleAttributeOptions(C container) {
			if (this.name != null) {
				container.setName(this.name);
			}
		}

		@Override
		public void execute() throws Exception {
			C container = findContainer();

			handleAttributeOptions(container);

			getContainerRegistry().storeChanges();
		}
	}

	protected static final String MULTI_VALUE_MESSAGE = "If there are multiple values, use commas (',') to separate them";
}
