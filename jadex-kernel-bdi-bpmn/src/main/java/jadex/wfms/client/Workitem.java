package jadex.wfms.client;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jadex.commons.concurrent.IResultListener;

/**
 * A client workitem.
 *
 */
public class Workitem implements IWorkitem
{
	/** Name of the workitem */
	private String name;
	
	/** Type of the workitem */
	private int type;
	
	/** Role that is handling this workitem. */
	private String role;
	
	/** Types of the parameters */
	private Map parameterTypes;
	
	/** Values of the parameters */
	private Map parameterValues;
	
	/** Read-only parameters */
	private Set readOnlyParameters;
	
	/** Whether the workitem has been acquired */
	private boolean acquired;
	
	/** Result Listener when the workitem has been processed. */
	private IResultListener listener;
	
	public Workitem(String name, int type, String role, Map parameterTypes, Map parameterValues, Set readOnlyParameters, IResultListener listener)
	{
		this.name = name;
		this.type = type;
		this.role = role;
		this.parameterTypes = parameterTypes;
		this.parameterValues = parameterValues;
		this.readOnlyParameters = readOnlyParameters;
		this.acquired = false;
		this.listener = listener;
	}
	
	/**
	 * Gets the name of the workitem.
	 * 
	 * @return name of the workitem
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Gets the type of the workitem.
	 * 
	 * @return type of the workitem.
	 */
	public int getType()
	{
		return type;
	}
	
	/**
	 * Gets the role responsible for handling this workitem.
	 * 
	 * @return role responsible for handling this workitem.
	 */
	public String getRole()
	{
		return role;
	}
	
	/**
	 * Returns whether the workitem has been acquired.
	 * 
	 * @return true, if the workitem has been acquired.
	 */
	public boolean isAcquired()
	{
		return acquired;
	}
	
	/**
	 * Sets the workitem acquire state.
	 * 
	 * @param acquired workitem acquire state
	 */
	public void setAcquired(boolean acquired)
	{
		this.acquired = acquired;
	}
	
	/**
	 * Gets the parameter names.
	 * 
	 * @return parameter names
	 */
	public Set getParameterNames()
	{
		return parameterTypes.keySet();
	}
	
	/**
	 * Gets the value of a parameter.
	 * 
	 * @param parameterName name of the parameter
	 * @return value of the parameter, or null if no value is set.
	 */
	public Object getParameterValue(String parameterName)
	{
		return parameterValues.get(parameterName);
	}
	
	/**
	 * Sets the value of a parameter.
	 * 
	 * @param parameterName name of the parameter
	 * @param value new value of the parameter
	 * @throws IllegalArgumentException if the parameter is read-only
	 */
	public void setParameterValue(String parameterName, Object value)
	{
		if (readOnlyParameters.contains(parameterName))
			throw new IllegalArgumentException("Parameter is read-only: " + parameterName);
		if (!acquired)
			throw new IllegalArgumentException("Attempted to write to an unacquired workitem: " + getName() + " " + parameterName);
		parameterValues.put(parameterName, value);
	}
	
	/**
	 * Sets the value of multiple parameters.
	 * 
	 * @param parameters the parameters
	 * @throws IllegalArgumentException if the parameter is read-only
	 */
	public void setParameterValues(Map parameters)
	{
		if ((new HashSet(readOnlyParameters)).removeAll(parameters.keySet()))
			throw new IllegalArgumentException("Some parameter are read-only.");
		if (!acquired)
			throw new IllegalArgumentException("Attempted to write to an unacquired workitem: " + getName());
		parameterValues.putAll(parameters);
	}
	
	/**
	 * Gets the type of a parameter.
	 * 
	 * @param parameterName name of the parameter
	 * @return type of the parameter
	 */
	public Class getParameterType(String parameterName)
	{
		return (Class) parameterTypes.get(parameterName);
	}
	
	/**
	 * Returns whether a parameter is read-only.
	 * 
	 * @param parameterName name of the parameter
	 * @return true if the parameter is read-only
	 */
	public boolean isReadOnly(String parameterName)
	{
		return readOnlyParameters.contains(parameterName);
	}
	
	public IResultListener getListener()
	{
		return listener;
	}
}
