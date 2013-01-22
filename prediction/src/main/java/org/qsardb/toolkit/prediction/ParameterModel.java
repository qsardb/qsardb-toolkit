/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.toolkit.prediction;

import java.lang.reflect.*;
import java.util.*;

import javax.swing.table.*;

import org.openscience.cdk.qsar.*;

public class ParameterModel extends AbstractTableModel {

	private IDescriptor descriptor = null;

	private List<Parameter> parameters = new ArrayList<Parameter>();


	@Override
	public int getRowCount(){
		return this.parameters.size();
	}

	@Override
	public int getColumnCount(){
		return 2;
	}

	@Override
	public String getColumnName(int column){

		switch(column){
			case 0:
				return "Name";
			case 1:
				return "Value";
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public Object getValueAt(int row, int column){
		Parameter parameter = this.parameters.get(row);

		switch(column){
			case 0:
				return parameter.getName();
			case 1:
				return parameter.getValue();
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public boolean isCellEditable(int row, int column){

		switch(column){
			case 1:
				return true;
			default:
				return false;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int column){
		Parameter parameter = this.parameters.get(row);

		switch(column){
			case 1:
				try {
					value = convertValue(parameter.getType(), value);

					parameter.setValue(value);

					Object[] parameters = this.descriptor.getParameters();
					parameters[row] = parameter.getValue();
					this.descriptor.setParameters(parameters);
				} catch(Exception e){
					System.err.println(e);

					return;
				}

				fireTableCellUpdated(row, column);
				break;
			default:
				throw new IllegalArgumentException();
		}
	}

	public IDescriptor getDescriptor(){
		return this.descriptor;
	}

	public void setDescriptor(IDescriptor descriptor){
		this.descriptor = descriptor;

		this.parameters.clear();

		if(this.descriptor != null){
			List<Parameter> parameters = createParameters(this.descriptor);

			this.parameters.addAll(parameters);
		}

		fireTableDataChanged();
	}

	static
	private List<Parameter> createParameters(IDescriptor descriptor){
		List<Parameter> result = new ArrayList<Parameter>();

		String[] parameterNames = descriptor.getParameterNames();
		if(parameterNames != null){
			Object[] parameterValues = descriptor.getParameters();

			for(int i = 0; i < parameterNames.length; i++){
				Parameter parameter = new Parameter();
				parameter.setName(parameterNames[i]);
				parameter.setValue(parameterValues[i]);

				Object type = descriptor.getParameterType(parameterNames[i]);
				parameter.setType(type.getClass());

				result.add(parameter);
			}
		}

		return result;
	}

	static
	private Object convertValue(Class<?> clazz, Object object) throws Exception{
		String string = (object != null ? String.valueOf(object) : null);

		if(String.class.equals(clazz)){
			return string;
		}

		Method method = clazz.getDeclaredMethod("valueOf", String.class);

		return method.invoke(null, string);
	}

	static
	public class Parameter {

		private String name = null;

		private Class<?> type = null;

		private Object value = null;


		public String getName(){
			return this.name;
		}

		private void setName(String name){
			this.name = name;
		}

		public Class<?> getType(){
			return this.type;
		}

		private void setType(Class<?> type){
			this.type = type;
		}

		public Object getValue(){
			return this.value;
		}

		private void setValue(Object value){
			this.value = value;
		}
	}
}