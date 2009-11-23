/**
 * 
 */
package jadex.tools.bpmn.editor.properties;

import jadex.tools.bpmn.diagram.Messages;
import jadex.tools.bpmn.editor.JadexBpmnEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.ui.services.util.CommonLabelProvider;
import org.eclipse.gmf.runtime.draw2d.ui.figures.FigureUtilities;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.stp.bpmn.Activity;
import org.eclipse.stp.bpmn.diagram.part.BpmnDiagramEditorPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * Property section Tab to enable Jadex specific properties
 * 
 * @author Claas Altschaffel
 */
public abstract class AbstractParameterTablePropertySection extends AbstractJadexPropertySection
{
	
	// containerEAnnotationName annotationDetailName

	// ---- constants ----
	
	/**
	 * the name column label
	 */
	protected final static String NAME_COLUMN = "Name"; //$NON-NLS-1$
	
	/**
	 * the type column label
	 */
	protected final static String TYPE_COLUMN = "Type"; //$NON-NLS-1$
	
	/**
	 * the value column label
	 */
	protected final static String VALUE_COLUMN = "Value"; //$NON-NLS-1$
	
	/**
	 * the direction column label
	 */
	protected final static String DIRECTION_COLUMN = "Direction"; //$NON-NLS-1$
	
	/**
	 * parameter direction values 
	 */
	protected final static String[] DIRECTION_VALUES = new String[] {"inout", "in", "out"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * default parameter direction
	 */
	protected final static String DEFAULT_DIRECTION = "inout"; //$NON-NLS-1$
	
	
	protected final static String[] DEFAULT_COLUMN_NAMES
		= new String[] { NAME_COLUMN, TYPE_COLUMN, VALUE_COLUMN, DIRECTION_COLUMN };
	
	protected final static int[] DEFAULT_COLUMN_WEIGHT = new int[] { 1, 1, 4, 1 };
	
	
	// ---- attributes ----
	
	/** The composite that holds the section parts */
	private Composite sectionComposite;

	/** The viewer/editor for parameter */ 
	private TableViewer tableViewer;
	
	/** The table add element button */
	private Button addButton;
	
	/** The table delete element button */
	private Button delButton;
	
	/** The table column weights */
	private int[] columnWeights;

	// ---- constructor ----
	
	/**
	 * @param containerEAnnotationName
	 * @param annotationDetailName
	 */
	protected AbstractParameterTablePropertySection(
			String containerEAnnotationName, String annotationDetailName)
	{
		this(containerEAnnotationName, annotationDetailName,
				DEFAULT_COLUMN_WEIGHT);
	}

	/**
	 * @param containerEAnnotationName
	 * @param annotationDetailName
	 */
	protected AbstractParameterTablePropertySection(
			String containerEAnnotationName, String annotationDetailName,
			int[] columnWeights)
	{
		super(containerEAnnotationName, annotationDetailName);
		this.columnWeights = columnWeights;
	}
	
	// ---- methods ----

	/**
	 * Creates the UI of the section.
	 */
	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage)
	{
		super.createControls(parent, aTabbedPropertySheetPage);
		createParameterTableComposite(sectionComposite);
	}

	
	/**
	 * Manages the input.
	 */
	@Override
	public void setInput(IWorkbenchPart part, ISelection selection)
	{
		super.setInput(part, selection);
		if (modelElement != null)
		{
			tableViewer.setInput(modelElement);
			addButton.setEnabled(true);
			delButton.setEnabled(true);
			
			return;
		}

		tableViewer.setInput(null);
		addButton.setEnabled(false);
		delButton.setEnabled(false);
	}
	
	

	// ---- control creation methods ----
	
	
	/**
	 * Creates the controls of the Parameter page section. Creates a table
	 * containing all Parameter of the selected {@link ParameterizedVertex}.
	 * 
	 * We use our own layout
	 * 
	 * @generated NOT
	 */
	protected TableViewer createParameterTableComposite(Composite parent)
	{
		
		// The layout of the table composite
		GridLayout layout = new GridLayout(3, false);
		sectionComposite.setLayout(layout);
		
		GridData gridData = new GridData(GridData.FILL_BOTH
				| GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.minimumHeight = 200;
		gridData.heightHint = 200;
		sectionComposite.setLayoutData(gridData);
		
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.grabExcessHorizontalSpace = true;
		tableLayoutData.grabExcessVerticalSpace = true;
		tableLayoutData.horizontalSpan = 3;

		// create the table
		getWidgetFactory().createLabel(sectionComposite, Messages.JadexCommonParameterListSection_ParameterTable_Label);
		TableViewer viewer = createTable(sectionComposite, tableLayoutData);

		// create cell modifier command
		createCellModifier(viewer);

		// create buttons
		createButtons(sectionComposite);
		
		return tableViewer = viewer;

	}
	
	/**
	 * Create the parameter edit table
	 * @param parent
	 * 
	 */
	private TableViewer createTable(Composite parent, GridData tableLayoutData)
	{
		String[] columns = DEFAULT_COLUMN_NAMES;
		int[] columnWeight = columnWeights;

		// the displayed table
		TableViewer viewer = new TableViewer(getWidgetFactory().createTable(parent,
				SWT.SINGLE | /*SWT.H_SCROLL | SWT.V_SCROLL |*/ SWT.FULL_SELECTION | SWT.BORDER));

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);

		viewer.getTable().setLayoutData(tableLayoutData);

		Font tableFont = viewer.getTable().getFont();
		TableLayout tableLayout = new TableLayout();
		for (int i = 0; i < columns.length; i++)
		{
			TableColumn column = new TableColumn(viewer.getTable(),
					SWT.LEFT);
			column.setText(columns[i]);

			tableLayout.addColumnData(new ColumnWeightData(columnWeight[i],
					FigureUtilities.getTextWidth(columns[i], tableFont), true));
		}
		viewer.getTable().setLayout(tableLayout);

		viewer.setContentProvider(new ParameterListContentProvider());
		viewer.setLabelProvider(new ParameterListLabelProvider());
		viewer.setColumnProperties(columns);

		return viewer;
	}
	
	/**
	 * Create the cell modifier command to update {@link EAnnotation}
	 */
	private void createCellModifier(TableViewer viewer)
	{
		TableViewerEditor.create(viewer,
				new ColumnViewerEditorActivationStrategy(viewer),
				TableViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK
						| TableViewerEditor.TABBING_HORIZONTAL
						| TableViewerEditor.TABBING_CYCLE_IN_ROW);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[] {
				new TextCellEditor(viewer.getTable()), // name (text)
				new TextCellEditor(viewer.getTable()), // type
				new TextCellEditor(viewer.getTable()), // value
				new ComboBoxCellEditor(
						viewer.getTable(),
						DIRECTION_VALUES, 
						SWT.READ_ONLY) // direction
		};
		viewer.setCellEditors(editors);

		// create the modify command
		viewer.setCellModifier(new ICellModifier()
		{
			/**
			 * Can modify all columns if model element exist. [Can only modify
			 * the column named NAME else.]
			 * @generated NOT
			 */
			public boolean canModify(Object element, String property)
			{
				if (element instanceof Parameter)
				{
					return true;
				}
				return false;
			}

			/**
			 * @return the value of the property for the given element.
			 * @generated NOT
			 */
			public Object getValue(Object element, String property)
			{
				if (element instanceof Parameter)
				{
					Parameter param = (Parameter) element;
					if (NAME_COLUMN.equals(property))
					{
						return param.getName();
					}

					if (TYPE_COLUMN.equals(property))
					{
						return param.getType();
					}

					if (VALUE_COLUMN.equals(property))
					{
						return param.getValue();
					}

					if (DIRECTION_COLUMN.equals(property))
					{
						String value = param.getDirection();
						for (int i = 0; i < DIRECTION_VALUES.length; i++)
						{
							if (DIRECTION_VALUES[i].equals(value))
							{
								return new Integer(i);
							}
						}
						
						// fall through
						return new Integer(0);
					}

				}
				// fall through
				return null;
			}

			/**
			 * modifies the value of the GeneralParameter according to the value
			 * given by the CellEditor.
			 */
			public void modify(Object element, String property,
					final Object value)
			{

				if (element instanceof TableItem)
				{

					if ((((TableItem) element).getData()) instanceof Parameter)
					{
						final Parameter param = (Parameter) ((TableItem) element)
								.getData();
						final String fproperty = property;

						// modify the element itself
						if (value != null)
						{
							// modify the Parameter
							ModifyJadexEAnnotationCommand command = new ModifyJadexEAnnotationCommand(
									modelElement,
									Messages.JadexUserTaskActivityPropertySection_update_command_name)
							{
								@Override
								protected CommandResult doExecuteWithResult(
										IProgressMonitor monitor,
										IAdaptable info)
										throws ExecutionException
								{
									
									List params = getTaskParameterList();
									Parameter paramToChange = (Parameter) params.get(params.indexOf(param));

									if (NAME_COLUMN.equals(fproperty))
									{
										paramToChange.setName((String) value);
									}
									else if (TYPE_COLUMN.equals(fproperty))
									{
										paramToChange.setType((String) value);
									}
									else if (VALUE_COLUMN.equals(fproperty))
									{
										paramToChange.setValue((String) value);
									}
									else if (DIRECTION_COLUMN.equals(fproperty))
									{
										if (value instanceof Integer)
										{
											paramToChange
													.setDirection(DIRECTION_VALUES[((Integer) value)
																	.intValue()]);
										}
									}
									else
									{
										throw new UnsupportedOperationException(
												Messages.JadexCommonPropertySection_InvalidEditColumn_Message);
									}

									updateTaskParameterList(params);
									
									return CommandResult.newOKCommandResult();
								}
							};
							try
							{
								command.execute(null, null);
								refresh();
								refreshSelection();
							}
							catch (ExecutionException e)
							{
								BpmnDiagramEditorPlugin
										.getInstance()
										.getLog()
										.log(
												new Status(
														IStatus.ERROR,
														JadexBpmnEditor.ID,
														IStatus.ERROR, e
																.getMessage(),
														e));
							}
						}

					}
				}

			}
		});
	}
	
	/**
	 * Create the Add and Delete button
	 * @param parent
	 * @generated NOT
	 */
	private void createButtons(Composite parent)
	{

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 80;

		// Create and configure the "Add" button
		Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
		add.setText(Messages.JadexCommonPropertySection_ButtonAdd_Label);
		add.setLayoutData(gridData);
		add.addSelectionListener(new SelectionAdapter()
		{
			/** 
			 * Add a ContextElement to the Context and refresh the view
			 * @generated NOT 
			 */
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// modify the Activity annotation
				ModifyJadexEAnnotationCommand command = new ModifyJadexEAnnotationCommand(
						modelElement,
						Messages.JadexUserTaskActivityPropertySection_add_command_name)
				{
					@Override
					protected CommandResult doExecuteWithResult(
							IProgressMonitor monitor, IAdaptable info)
							throws ExecutionException
					{
						Parameter newElement = new Parameter(Messages.JadexUserTaskActivityPropertySection_NewParameterName_Value, "Object", "null", DEFAULT_DIRECTION); //$NON-NLS-2$ //$NON-NLS-3$

						List params = getTaskParameterList();
						params.add(newElement);
						updateTaskParameterList(params);
						
						return CommandResult.newOKCommandResult(newElement);
					}
				};
				try
				{
					command.execute(null, null);
					refresh();
					refreshSelection();
				}
				catch (ExecutionException ex)
				{
					BpmnDiagramEditorPlugin.getInstance().getLog().log(
							new Status(IStatus.ERROR,
									JadexBpmnEditor.ID, IStatus.ERROR,
									ex.getMessage(), ex));
				}
			}
		});
		addButton = add;

		// Create and configure the "Delete" button
		Button delete = new Button(parent, SWT.PUSH | SWT.CENTER);
		delete.setText(Messages.JadexCommonPropertySection_ButtonDelete_Label);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 80;
		delete.setLayoutData(gridData);
		delete.addSelectionListener(new SelectionAdapter()
		{
			/** 
			 * Remove selected ContextElement from the Context and refresh the view
			 * @generated NOT 
			 */
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// modify the Activity annotation
				ModifyJadexEAnnotationCommand command = new ModifyJadexEAnnotationCommand(
						modelElement,
						Messages.JadexUserTaskActivityPropertySection_delete_command_name)
				{
					@Override
					protected CommandResult doExecuteWithResult(
							IProgressMonitor monitor, IAdaptable info)
							throws ExecutionException
					{
						Parameter element = (Parameter) ((IStructuredSelection) tableViewer
								.getSelection()).getFirstElement();
						
						List params = getTaskParameterList();
						params.remove(element);
						updateTaskParameterList(params);
						
						return CommandResult.newOKCommandResult(null);
					}
				};
				try
				{
					command.execute(null, null);
					refresh();
					refreshSelection();
				}
				catch (ExecutionException ex)
				{
					BpmnDiagramEditorPlugin.getInstance().getLog().log(
							new Status(IStatus.ERROR,
									JadexBpmnEditor.ID, IStatus.ERROR,
									ex.getMessage(), ex));
				}
			}
		});
		delButton = delete;
	}
	
	// ---- converter and help methods ----
	
	/**
	 * Retrieve the EAnnotation from the modelElement and converts it to a Parameter list
	 * @param act
	 * @return
	 */
	private List<Parameter> getTaskParameterList()
	{
		EAnnotation ea = modelElement.getEAnnotation(containerEAnnotationName);
		if (ea != null)
		{
			String value = (String) ea.getDetails().get(annotationDetailName);
			return convertTaskParameterString(value);
		}
		
		return new ArrayList<Parameter>(0);
	}
	
	/**
	 * Updates the EAnnotation for the modelElement task parameter list
	 * @param params
	 */
	private void updateTaskParameterList(List<Parameter> params)
	{
		updateJadexEAnnotation(annotationDetailName, convertTaskParameterList(params));
		
		// HACK? Should use notification?
		Display.getCurrent().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				tableViewer.refresh();
			}
		});
		
	}
	
	
	/**
	 * Convert a string representation of a GeneralParameter list into a GeneralParameter list 
	 * @param stringToConvert
	 * @return
	 */
	protected List<Parameter> convertTaskParameterString(String stringToConvert)
	{
		StringTokenizer listTokens = new StringTokenizer(stringToConvert, JadexCommonPropertySection.LIST_ELEMENT_DELIMITER);
		List<Parameter> params = new ArrayList<Parameter>(listTokens.countTokens());
		int i = 0;
		while (listTokens.hasMoreTokens())
		{
			String paramElement = listTokens.nextToken();
			StringTokenizer paramTokens = new StringTokenizer(paramElement,JadexCommonPropertySection.LIST_ELEMENT_ATTRIBUTE_DELIMITER);
			// require 4 tokens: name, type, value, direction
			if(paramTokens.countTokens() == 4)
			{
				String name = paramTokens.nextToken();
				String type = paramTokens.nextToken();
				String value = paramTokens.nextToken();
				String direction = paramTokens.nextToken();
				params.add(new Parameter(name, type, value, direction));
			}
			else
			{
				BpmnDiagramEditorPlugin.getInstance().getLog().log(
						new Status(IStatus.ERROR,
								JadexBpmnEditor.ID, IStatus.ERROR,
								Messages.ActivityParameterListSection_WrongElementDelimiter_message + " \""+paramElement+"\"", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// update token index
			i++;
		}
		return params;
	}
	
	/**
	 * Convert a list of GeneralParameter into a string representation
	 * @param arrayToConvert
	 * @return
	 */
	protected String convertTaskParameterList(List<Parameter> params)
	{
		StringBuffer buffer = new StringBuffer();
		for (Parameter parameter : params)
		{
			if (buffer.length() != 0)
			{
				buffer.append(JadexCommonPropertySection.LIST_ELEMENT_DELIMITER);
			}
			//buffer.append(JadexCommonPropertySection.LIST_ELEMENT_ATTRIBUTE_DELIMITER);
			buffer.append(parameter.getName());
			buffer.append(JadexCommonPropertySection.LIST_ELEMENT_ATTRIBUTE_DELIMITER);
			buffer.append(parameter.getType());
			buffer.append(JadexCommonPropertySection.LIST_ELEMENT_ATTRIBUTE_DELIMITER);
			buffer.append(parameter.getValue());
			buffer.append(JadexCommonPropertySection.LIST_ELEMENT_ATTRIBUTE_DELIMITER);
			buffer.append(parameter.getDirection());
		}

		return buffer.toString();
	}
	
	// ---- internal used classes ----
	
	/**
	 * Simple content provider that reflects the ContextElemnts 
	 * of an Context given as an input. Marked as dynamic / static.
	 */
	private class ParameterListContentProvider implements IStructuredContentProvider {

		/**
		 * Generate the content for the table.
		 * 
		 * @return Object[] that contains GeneralParameter objects.
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement)
		{
			if (inputElement instanceof Activity)
			{
				EAnnotation ea = ((Activity)inputElement).getEAnnotation(containerEAnnotationName);
				inputElement = ea;
			}
			
			if (inputElement instanceof EAnnotation)
			{
				String parameterListString = ((EAnnotation) inputElement).getDetails().get(annotationDetailName);
				return convertTaskParameterString(parameterListString).toArray();
			}
			
			return new Object[] {};
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose()
		{
			// nothing to dispose.
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
			// no actions taken.
		}

	}
	
	/**
	 * Label provider in charge of rendering the keys and values of the annotation
	 * attached to the object. Currently based on CommonLabelProvider.
	 */
	protected class ParameterListLabelProvider extends CommonLabelProvider
			implements ITableLabelProvider
	{

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex)
		{
			return super.getImage(element);
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex)
		{
			if (element instanceof Parameter)
			{
				Parameter param = (Parameter) element;
				switch (columnIndex)
				{
				case 0:
					return param.getName();
				case 1:
					return param.getType();
				case 2:
					return param.getValue();
				case 3:
					return param.getDirection();
				
				default:
					return super.getText(param);
				}
			}
			return super.getText(element);
		}
		
	}
	
	/**
	 * Internal representation of a jadex runtime GeneralParameter
	 * 
	 * @author Claas Altschaffel
	 */
	protected class Parameter {
		
		// ---- attributes ----
		
		private String name;
		private String type;
		private String value;
		private String direction;
		
		// ---- constructors ----
		
		/** default constructor */
		public Parameter(String name, String type, String value,
				String direction)
		{
			super();
			
			assert name != null;
			assert type != null;
			assert value != null;
			assert direction != null;
			
			this.name = name;
			this.type = type;
			this.value = value;
			this.direction = direction;
		}

		// ---- overrides ----
		
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof Parameter))
			{
				return false;
			}
			
			return name.equals(((Parameter) obj).getName())
					&& type.equals(((Parameter) obj).getType())
					&& value.equals(((Parameter) obj).getValue())
					&& direction.equals(((Parameter) obj).getDirection());
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return name.hashCode() * 31
					+ type.hashCode() * 31
					+ value.hashCode() * 31
					+ direction.hashCode() * 31;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return name + JadexCommonPropertySection.LIST_ELEMENT_ATTRIBUTE_DELIMITER + type
					+ JadexCommonPropertySection.LIST_ELEMENT_ATTRIBUTE_DELIMITER + value
					+ JadexCommonPropertySection.LIST_ELEMENT_ATTRIBUTE_DELIMITER + direction;
		}
		
		// ---- getter / setter ----

		/**
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}

		

		/**
		 * @param name the name to set
		 */
		public void setName(String name)
		{
			this.name = name;
		}

		/**
		 * @return the type
		 */
		public String getType()
		{
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(String type)
		{
			this.type = type;
		}

		/**
		 * @return the value
		 */
		public String getValue()
		{
			return value;
		}

		/**
		 * @param value the value to set
		 */
		public void setValue(String value)
		{
			this.value = value;
		}

		/**
		 * @return the direction
		 */
		public String getDirection()
		{
			return direction;
		}

		/**
		 * @param direction the direction to set
		 */
		public void setDirection(String direction)
		{
			this.direction = direction;
		}

	}
	
	
	
}


