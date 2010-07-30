package jadex.tools.common.componenttree;

import jadex.tools.common.CombiIcon;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *  Renderer for component tree cells.
 */
public class ComponentTreeCellRenderer	extends DefaultTreeCellRenderer
{
	//-------- constructors --------
	
	/**
	 *  Create a new component tree cell renderer.
	 */
	public ComponentTreeCellRenderer()
	{
	}
	
	//-------- TreeCellRenderer interface --------
	
	/**
	 *  Get the cell renderer for a node.
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
		boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		// Change icons depending on node type.
		IComponentTreeNode	node	= (IComponentTreeNode)value;
		Icon	icon	= node.getIcon();
		// Add overlays to icon (if any).
		if(tree.getModel() instanceof ComponentTreeModel)
		{
			List	icons	= null;
			INodeHandler[]	overlays	= ((ComponentTreeModel)tree.getModel()).getNodeHandlers();
			for(int i=0; i<overlays.length; i++)
			{
				Icon	overlay	= overlays[i].getOverlay(node);
				if(overlay!=null)
				{
					if(icons==null)
					{
						icons	= new ArrayList();
						if(icon!=null)
							icons.add(icon);	// Base icon.
					}
					icons.add(overlay);
				}
			}
			if(icons!=null)
			{
				icon	= new CombiIcon((Icon[])icons.toArray(new Icon[icons.size()]));
			}
		}
		if(icon!=null)
		{
			setOpenIcon(icon);
			setClosedIcon(icon);
			setLeafIcon(icon);
		}
		else
		{
			setOpenIcon(getDefaultOpenIcon());
			setClosedIcon(getDefaultClosedIcon());
			setLeafIcon(getDefaultLeafIcon());
		}
		
		JComponent	comp	= (JComponent)super.getTreeCellRendererComponent(
			tree, value, selected, expanded, leaf, row, hasFocus);
		
		return comp;
	}

}
