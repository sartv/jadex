package jadex.tools.common.componenttree;

import javax.swing.Action;
import javax.swing.Icon;

/**
 *  Node handlers provide additional information for nodes
 *  such as icon overlays and popup actions.
 */
public interface INodeHandler
{
	/**
	 *  Get the overlay for a node if any.
	 */
	public Icon	getOverlay(IComponentTreeNode node);

	/**
	 *  Get the popup actions available for all of the given nodes, if any.
	 */
	public Action[]	getPopupActions(IComponentTreeNode[] nodes);
}
