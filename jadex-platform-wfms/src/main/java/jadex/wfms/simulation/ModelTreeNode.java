package jadex.wfms.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jadex.bpmn.model.MActivity;
import jadex.bridge.ILoadableComponentModel;
import jadex.commons.collection.TreeNode;
import jadex.wfms.simulation.stateholder.IParameterStateSet;

public class ModelTreeNode extends TreeNode
{
	private ModelTreeNode parent;
	
	public ModelTreeNode()
	{
		this(null);
	}
	
	public ModelTreeNode(Object data)
	{
		super(data);
		parent = null;
	}
	
	public void addChild(TreeNode child)
	{
		if (child instanceof ModelTreeNode)
			((ModelTreeNode) child).setParent(this);
		super.addChild(child);
	}
	
	public void setChildren(List children)
	{
		if (children == null)
			children = new ArrayList();
		for (Iterator it = children.iterator(); it.hasNext(); )
			((ModelTreeNode) it.next()).setParent(this);
		super.setChildren(children);
	}
	
	public void setParent(ModelTreeNode parent)
	{
		this.parent = parent;
	}
	
	public ModelTreeNode getParent()
	{
		return parent;
	}
	
	public String toString()
	{
		if (data instanceof ILoadableComponentModel)
		{
			ILoadableComponentModel model = ((ILoadableComponentModel) data);
			String ret = model.getName();
			if (ret == null)
			{
				ret = model.getFilename();
				ret = ClientProcessMetaModel.resolveProcessName(model);
			}
			return ret;
		}
		else if (data instanceof MActivity)
			return ((MActivity) data).getName();
		else if (data instanceof IParameterStateSet)
			return ((IParameterStateSet) data).getParameterName();
		else
			return String.valueOf(data);
	}
}
