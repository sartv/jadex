/**
 * 
 */
package jadex.tools.bpmn.editor.properties;

import jadex.tools.bpmn.diagram.Messages;

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
public class JadexGlobalDiagramSection extends
		Abstract1ColumnTablePropertySection
{

	// ---- attributes ----

	/** The composite that holds the section parts */
	private Composite sectionComposite;
	
	/** The package label */
	private Label packageLabel;
	
	/** The text for the implementing class */
	private Text packageText;
	
	// HACK!
	private String[] textFieldNames = new String[] { "test1", "test2", "test3" };
	private Text[] textFields;
	
	/**
	 * Default constructor, initializes super class
	 */
	public JadexGlobalDiagramSection()
	{
		super(JadexCommonPropertySection.JADEX_GLOBAL_ANNOTATION, JadexCommonPropertySection.JADEX_IMPORT_LIST_DETAIL,
				Messages.JadexGlobalDiagramSection_Imports_Label, "import");
	}

	// ---- methods ----

	/**
	 * Creates the UI of the section.
	 */
	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage)
	{
		// create imports table from super class
		super.createControls(parent, aTabbedPropertySheetPage);
		
		// we append the package text to the imports table
		sectionComposite = getWidgetFactory().createComposite(parent);
		GridLayout sectionLayout = new GridLayout();
		sectionComposite.setLayout(sectionLayout);
		
		packageLabel = getWidgetFactory().createLabel(sectionComposite, Messages.JadexGlobalDiagramSection_Package_Label);
		packageText = getWidgetFactory().createText(sectionComposite, ""); // //$NON-NLS-1$
		packageText.addModifyListener(new ModifyJadexEAnnotation(JadexCommonPropertySection.JADEX_PACKAGE_DETAIL, packageText));
		
		
		
		GridData gd = new GridData();
		gd.minimumWidth = 500;
		gd.widthHint = 500;
		
		packageLabel.setLayoutData(gd);
		packageText.setLayoutData(gd);
		

		
		// HACK - FIXME: move
		textFields = new Text[textFieldNames.length];
		for (int i = 0; i < textFieldNames.length; i++)
		{
			Label tmpLabel = getWidgetFactory().createLabel(sectionComposite, textFieldNames[i]);
			Text tmpText = getWidgetFactory().createText(sectionComposite, textFieldNames[i]); // //$NON-NLS-1$
			textFields[i] = tmpText;
			tmpText.addModifyListener(new ModifyJadexEAnnotation(textFieldNames[i], tmpText));
			tmpLabel.setLayoutData(gd);
			tmpText.setLayoutData(gd);
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
				String packageValue = (String) ea.getDetails().get(JadexCommonPropertySection.JADEX_PACKAGE_DETAIL);
				packageText.setText(packageValue != null ? packageValue : "");
				
				
				// HACK!! - FIXME: move
				for (int i = 0; i < textFieldNames.length; i++)
				{
					String tmpName = textFieldNames[i];
					Text tmpField = textFields[i];
					String tmpValue = (String) ea.getDetails().get(tmpName);
					tmpField.setText(tmpValue != null ? tmpValue : "");
					tmpField.setEnabled(true);
				}
				

			}
			
			packageText.setEnabled(true);
			return;
		}
		
		// fall through
		packageText.setEnabled(false);
		
		
		// HACK!! - FIXME: move
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
