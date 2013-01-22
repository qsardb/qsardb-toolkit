/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.toolkit.curation;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.qsardb.model.*;
import org.qsardb.resolution.chemical.*;

public class CompoundEditorTable extends JTable {

	private Curator curator = null;

	private ValueSelectionDialog dialog = null;


	public CompoundEditorTable(Curator curator){
		super(new CompoundModel());

		this.curator = curator;

		this.dialog = new ValueSelectionDialog();

		setRowHeight(20);

		setIntercellSpacing(new Dimension(4, 2));

		TableColumnModel columnModel = getColumnModel();

		TableColumn name = columnModel.getColumn(0);
		name.setWidth(100);
		name.setMinWidth(100);
		name.setMaxWidth(100);
		name.setPreferredWidth(100);

		TableColumn status = columnModel.getColumn(2);
		status.setWidth(100);
		status.setMinWidth(100);
		status.setMaxWidth(100);
		status.setPreferredWidth(100);

		CompoundModel model = getModel();

		TableModelListener modelListener = new TableModelListener(){

			@Override
			public void tableChanged(TableModelEvent event){

				if(event.getColumn() == 1){
					CompoundEditorTable.this.curator.refreshCompound();
				}
			}
		};

		model.addTableModelListener(modelListener);

		MouseListener mouseListener = new MouseAdapter(){

			@Override
			public void mousePressed(MouseEvent event){
				triggerPopup(event);
			}

			@Override
			public void mouseReleased(MouseEvent event){
				triggerPopup(event);
			}

			private void triggerPopup(MouseEvent event){

				if(event.isPopupTrigger()){
					Point point = event.getPoint();

					JPopupMenu popup = createPopupMenu(point);
					if(popup != null){
						popup.show(CompoundEditorTable.this, (int)point.getX(), (int)point.getY());
					}
				}
			}
		};
		addMouseListener(mouseListener);
	}

	private JPopupMenu createPopupMenu(Point point){
		int row = rowAtPoint(point);
		int column = columnAtPoint(point);

		switch(column){
			case 1:
				return createResolverPopupMenu(row, column);
			default:
				return null;
		}
	}

	private JPopupMenu createResolverPopupMenu(final int row, final int column){
		JPopupMenu menu = new JPopupMenu("Chemical Identifier Resolver");

		final
		CompoundModel model = getModel();

		List<CompoundModel.Attribute> attributes = model.getAttributes();

		final
		CompoundModel.Attribute source = attributes.get(row);

		String value = source.getValue();
		if(value == null){
			return null;
		}

		final
		Identifier identifier = source.createIdentifier();
		if(identifier == null){
			return null;
		}

		for(int i = 0; i < attributes.size(); i++){
			final
			CompoundModel.Attribute target = attributes.get(i);

			final
			Resolver resolver = target.createResolver(identifier);
			if(resolver == null){
				continue;
			}

			menu.add(new AbstractAction(target.getName() + "..."){

				@Override
				public void actionPerformed(ActionEvent event){
					Runnable runnable = new Runnable(){

						@Override
						public void run(){
							setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

							try {
								String value = resolve(resolver);
								if(value != null){
									target.setValue(value);

									model.fireTableCellUpdated(row, column);
								}
							} finally {
								setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							}
						}

						private void setCursor(Cursor cursor){
							CompoundEditorTable.this.curator.setCursor(cursor);
						}
					};
					SwingUtilities.invokeLater(runnable);
				}
			});
		}

		return menu;
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	private String resolve(Resolver resolver){
		List<String> values = new ArrayList<String>();

		try {
			CompoundModel model = getModel();

			Object result = resolver.resolve(model.getCompound());
			if(result == null){
				return null;
			} // End if

			if(result instanceof Collection){
				values.addAll((Collection<String>)result);
			} else

			{
				values.add((String)result);
			}
		} catch(IOException ioe){
			System.err.println(ioe);

			return null;
		} catch(Exception e){
			System.err.println(e);

			return null;
		}

		return this.dialog.select(values);
	}

	@Override
	public Dimension getPreferredScrollableViewportSize(){
		return new Dimension(600, 120);
	}

	public void setCompound(Compound compound){
		CompoundModel model = getModel();

		CellEditor editor = getCellEditor();
		if(editor != null){
			editor.cancelCellEditing();
		}

		model.setCompound(compound);
	}

	@Override
	public CompoundModel getModel(){
		return (CompoundModel)super.getModel();
	}
}