package jadex.platform.service.cron;

import jadex.bridge.IInternalAccess;
import jadex.commons.ICommand;
import jadex.commons.IFilter;
import jadex.commons.SUtil;
import jadex.commons.Tuple2;

/**
 *  The cron job consists of:
 *  - a unique id
 *  - a time pattern filter
 *  - a command
 */
public class CronJob
{
	//-------- attributes --------
	
	/** The id. */
	protected String id;
	
	/** The filter. */
	protected IFilter<Long> filter;
	
	/** The command. */
	protected ICommand<Tuple2<IInternalAccess, Long>> command;
	 
	//-------- constructors --------
	
	/**
	 *  Create a new cronjob. 
	 */
	public CronJob()
	{
	}

	/**
	 *  Create a new cron job.
	 */
	public CronJob(IFilter<Long> filter, ICommand<Tuple2<IInternalAccess, Long>> command)
	{
		this.id = SUtil.createUniqueId("cronjob");
		this.filter = filter;
		this.command = command;
	}

	//-------- methods --------
	
	/**
	 *  Get the id.
	 *  @return The id.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 *  Set the id.
	 *  @param id The id to set.
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 *  Get the filter.
	 *  @return The filter.
	 */
	public IFilter<Long> getFilter()
	{
		return filter;
	}

	/**
	 *  Set the filter.
	 *  @param filter The filter to set.
	 */
	public void setFilter(IFilter<Long> filter)
	{
		this.filter = filter;
	}

	/**
	 *  Get the command.
	 *  @return The command.
	 */
	public ICommand<Tuple2<IInternalAccess, Long>> getCommand()
	{
		return command;
	}

	/**
	 *  Set the command.
	 *  @param command The command to set.
	 */
	public void setCommand(ICommand<Tuple2<IInternalAccess, Long>> command)
	{
		this.command = command;
	}

//	/** 
//	 *  Compute the hashcode.
//	 */
//	public int hashCode()
//	{
//		return id.hashCode()*31;
//	}
//
//	/** 
//	 *  Test for equality.
//	 */
//	public boolean equals(Object obj)
//	{
//		boolean ret = false;
//		if(obj instanceof CronJob)
//		{
//			ret = ((CronJob)obj).getId().equals(getId());
//		}
//		return ret;
//	}
	
}
