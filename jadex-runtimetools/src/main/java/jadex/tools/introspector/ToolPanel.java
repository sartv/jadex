package jadex.tools.introspector;

import jadex.adapter.base.fipa.IAMS;
import jadex.adapter.base.fipa.SFipa;
import jadex.bdi.interpreter.BDIInterpreter;
import jadex.bdi.runtime.IExternalAccess;
import jadex.bdi.runtime.impl.ElementFlyweight;
import jadex.bridge.IAgentIdentifier;
import jadex.commons.concurrent.IResultListener;
import jadex.rules.tools.reteviewer.RuleEnginePanel;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *  A generic tool panel for easy integration into the introspector.
 */
public class ToolPanel	extends JPanel
{
	//-------- constants --------

	/** The text when nothing is selected in the details view. */
	public static final String NOTHING = "No element selected for detailed view.\nUse double-click to view element.";

	//-------- attributes --------

	/** The rule engine panel. */
	protected RuleEnginePanel	panel;
	
	//-------- constructors --------

	/**
	 *  Create a new tool panel for a remote agent.
	 *  @param agent	The agent access.
	 *  @param active	Flags indicating which tools should be active.
	 */
	public ToolPanel(IExternalAccess agent, IAgentIdentifier observed)
	{
        // Hack!?!?!
		((IAMS)agent.getPlatform().getService(IAMS.class, SFipa.AMS_SERVICE))
			.getExternalAccess(observed, new IResultListener()
		{
			public void exceptionOccurred(Exception exception)
			{
				exception.printStackTrace();
			}
			
			public void resultAvailable(final Object result)
			{
				// Hack!!!
				final BDIInterpreter bdii = ((ElementFlyweight)result).getInterpreter();
				// Open tool on introspected agent thread as required for copy state constructor (hack!!!)
				bdii.invokeLater(new Runnable()
				{
					public void run()
					{
						final IntrospectorAdapter	tooladapter	= (IntrospectorAdapter)bdii.getToolAdapter(IntrospectorAdapter.class);
						ToolPanel.this.panel	= new RuleEnginePanel(bdii.getRuleSystem(), tooladapter);
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{
								setLayout(new BorderLayout());
								add("Center", ToolPanel.this.panel);
								
								doLayout();
								repaint();
							}
						});
					}
				});
			}
		});
	}
	
	/**
	 *  Dispose the panel and remove all listeners.
	 */
	public void	dispose()
	{
		if(panel!=null)
			panel.dispose();
	}
}
