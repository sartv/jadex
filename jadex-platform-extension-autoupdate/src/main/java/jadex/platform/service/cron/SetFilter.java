package jadex.platform.service.cron;

import jadex.commons.IFilter;

import java.util.Set;

/**
 *  Simple set based filter that checks
 *  if a value is contained in the set.
 */
public class SetFilter<T> implements IFilter<T>
{
	//-------- attributes --------
	
	/** The allowed values. */
	protected Set<T> vals;
	
	//-------- methods --------
	
	/**
	 *  Create a new filter.
	 *  @param vals The values.
	 */
	public SetFilter(Set<T> vals)
	{
		this.vals = vals;
	}
	
	/**
	 *  Test if value is contained in filter.
	 *  @param obj the 
	 */
	public boolean filter(T obj)
	{
		return vals.contains(obj);
	}
}