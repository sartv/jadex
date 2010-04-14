package jadex.gpmn.model2;

public class MBpmnPlan
{
	/** The id. */
	protected String id;
	
	/** The name. */
	protected String name;
	
	/** The BPMN plan. */
	protected String bpmnplan;
	
	/** The precondition. */
	protected String precondition;
	
	/** The contextcondition. */
	protected String contextcondition;
	
	/**
	 *  Get the id.
	 *  @return The id.
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 *  Set the id.
	 *  @param id the id to set.
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	
	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 *  Set the name.
	 *  @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 *  Get the bpmnplan.
	 *  @return The bpmnplan.
	 */
	public String getBpmnPlan()
	{
		return bpmnplan;
	}

	/**
	 *  Set the bpmnplan.
	 *  @param bpmnplan The bpmnplan to set.
	 */
	public void setBpmnPlan(String bpmnplan)
	{
		this.bpmnplan = bpmnplan;
	}
	
	/**
	 *  Get the precondition.
	 *  @return The precondition.
	 */
	public String getPreCondition()
	{
		return precondition;
	}

	/**
	 *  Set the precondition.
	 *  @param precondition The precondition to set.
	 */
	public void setPreCondition(String precondition)
	{
		this.precondition = precondition;
	}

	/**
	 *  Get the contextcondition.
	 *  @return The contextcondition.
	 */
	public String getContextCondition()
	{
		return contextcondition;
	}

	/**
	 *  Set the contextcondition.
	 *  @param contextcondition The contextcondition to set.
	 */
	public void setContextCondition(String contextcondition)
	{
		this.contextcondition = contextcondition;
	}
}
