/**
 * 
 */
package jadex.tools.bpmn.editor.properties;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;


/**
 * @author Claas Altschaffel
 * 
 */
public abstract class AbstractMultiTextfieldPropertySection extends
		AbstractJadexPropertySection
{
	// ---- constants ----
	
	protected static final String[] DEFAULT_NAMES = new String[] { "Default_1", "Default_2", "Default_3" };
	
	// ---- attributes ----

	private String[] textFieldNames;
	
	private Text[] textFields;
	
	// ---- constructor ----
	
	/**
	 * Default Constructor
	 * 
	 * @param textFieldNames
	 * @param textFields
	 */
	protected AbstractMultiTextfieldPropertySection(
			String containerEAnnotationName, String annotationDetailName,
			String[] textFieldNames)
	{
		super(containerEAnnotationName, annotationDetailName);
		this.textFieldNames = textFieldNames != null ? textFieldNames : DEFAULT_NAMES;
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
		
		GridLayout sectionLayout = new GridLayout(2, true);
		sectionComposite.setLayout(sectionLayout);
		
		GridData gridData = new GridData();
		gridData.minimumWidth = 500;
		gridData.widthHint = 500;

		textFields = new Text[textFieldNames.length];
		for (int i = 0; i < textFieldNames.length; i++)
		{
			Label cLabel = getWidgetFactory().createLabel(sectionComposite, textFieldNames[i]);
			Text cTextfield = getWidgetFactory().createText(sectionComposite, textFieldNames[i]);
			textFields[i] = cTextfield;
			cTextfield.addModifyListener(new ModifyJadexEAnnotation(textFieldNames[i], cTextfield));
			cLabel.setLayoutData(gridData);
			cTextfield.setLayoutData(gridData);
		}

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
			EAnnotation ea = modelElement.getEAnnotation(containerEAnnotationName);
			if (ea != null)
			{
				for (int i = 0; i < textFieldNames.length; i++)
				{
					String tmpName = textFieldNames[i];
					Text tmpField = textFields[i];
					String tmpValue = (String) ea.getDetails().get(tmpName);
					tmpField.setText(tmpValue != null ? tmpValue : "");
					tmpField.setEnabled(true);
				}

			}
			
			return;
		}
		
		// fall through
		for (int i = 0; i < textFieldNames.length; i++)
		{
			Text tmpField = textFields[i];
			tmpField.setEnabled(false);
		}
		
		
	}

	// ---- internal used classes ----
	
	/**
	 * Tracks the change occurring on the text field.
	 */
	private class ModifyJadexEAnnotation implements ModifyListener
	{
		private String key;
		private Text field;

		public ModifyJadexEAnnotation(String k, Text field)
		{
			key = k;
			this.field = field;
		}

		public void modifyText(ModifyEvent e)
		{
			if (modelElement == null)
			{ 
				// the value was just initialized
				return;
			}
			
			updateJadexEAnnotation(key, field.getText());
		}
	}

}
