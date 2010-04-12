package jadex.application.model;

import jadex.application.runtime.IApplication;
import jadex.javaparser.SimpleValueFetcher;
import jadex.javaparser.javaccimpl.JavaCCExpressionParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Component instance representation. 
 */
public class MComponentInstance
{
	//-------- attributes --------
	
	/** The name. */
	protected String name;
	
	/** The type name. */
	protected String typename;
	
	/** The configuration. */
	protected String configuration;

	/** The start flag. */
	protected boolean start;
	
	/** The number of components. */
//	protected int number;
	protected String numbertext;
	
	/** The master flag. */
	protected boolean master;
	
	/** The suspended flag. */
	protected boolean suspended;
	
	/** The daemon flag. */
	protected boolean daemon;
	
	/** The list of contained arguments. */
	protected List arguments;
	
	/** The argument parser. */
	protected JavaCCExpressionParser parser;
	
	//-------- constructors --------
	
	/**
	 *  Create a new component.
	 */
	public MComponentInstance()
	{
		this.arguments = new ArrayList();
		this.start = true;
//		this.number = 1;
	}
	
	//-------- methods --------
	
	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName()
	{
		return this.name;
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
	 *  Get the type name.
	 *  @return The type name.
	 */
	public String getTypeName()
	{
		return this.typename;
	}

	/**
	 *  Set the type name.
	 *  @param type The type name to set.
	 */
	public void setTypeName(String typename)
	{
		this.typename = typename;
	}

	/**
	 *  Get the configuration.
	 *  @return The configuration.
	 */
	public String getConfiguration()
	{
		return this.configuration;
	}

	/**
	 *  Set the configuration.
	 *  @param configuration The configuration to set.
	 */
	public void setConfiguration(String configuration)
	{
		this.configuration = configuration;
	}
	
	/**
	 *  Test if component should be started (not only created).
	 *  @return True, if should be started.
	 */
	public boolean isStart()
	{
		return this.start;
	}

	/**
	 *  Set if the component should also be started.
	 *  @param start The start flag to set.
	 */
	public void setStart(boolean start)
	{
		this.start = start;
	}
	
	/**
	 *  Get the master flag.
	 *  @return True, if master.
	 */
	public boolean isMaster()
	{
		return this.master;
	}

	/**
	 *  Set the master flag..
	 *  @param start The master flag.
	 */
	public void setMaster(boolean master)
	{
		this.master = master;
	}
	
	/**
	 *  Get the daemon.
	 *  @return The daemon.
	 */
	public boolean isDaemon()
	{
		return this.daemon;
	}

	/**
	 *  Set the daemon.
	 *  @param daemon The daemon to set.
	 */
	public void setDaemon(boolean daemon)
	{
		this.daemon = daemon;
	}

	/**
	 *  Get the suspended.
	 *  @return The suspended.
	 */
	public boolean isSuspended()
	{
		return this.suspended;
	}

	/**
	 *  Set the suspended.
	 *  @param suspended The suspended to set.
	 */
	public void setSuspended(boolean suspended)
	{
		this.suspended = suspended;
	}

	/**
	 *  Set the number text.
	 *  @param numbertext The number text.
	 */
	public void setNumberText(String numbertext)
	{
		this.numbertext = numbertext;
	}
	
	/**
	 *  Get the number text (expression).
	 *  @return The number text.
	 */
	public String getNumberText()
	{
		return this.numbertext;
	}
	
	/**
	 *  Get the number of components to start.
	 *  @return The number.
	 */
	public int getNumber(IApplication context, ClassLoader classloader)
	{
		SimpleValueFetcher fetcher = new SimpleValueFetcher();
		fetcher.setValue("$platform", context.getServiceContainer());
		fetcher.setValue("$args", context.getArguments());
		fetcher.setValue("$results", context.getResults());

		String[] imports = context.getApplicationType().getAllImports();
		if(parser==null)
			parser = new JavaCCExpressionParser();
			
		Object val = numbertext!=null? parser.parseExpression(numbertext, imports, null, classloader).getValue(fetcher): null;
		
		return val instanceof Integer? ((Integer)val).intValue(): 1;
	}
	
	/**
	 *  Get the number of components to start.
	 *  @return The number.
	 * /
	public int getNumber()
	{
		return this.number;
	}*/

	/**
	 *  Set the number of components.
	 *  @param number The number to set.
	 * /
	public void setNumber(int number)
	{
//		this.number = number;
	}*/

	/**
	 *  Add an argument.
	 *  @param arg The argument.
	 */
	public void addMArgument(MArgument arg)
	{
		this.arguments.add(arg);
	}

	/**
	 *  Get the list of arguments.
	 *  @return The arguments.
	 */
	public List getMArguments()
	{
		return this.arguments;
	}
	
	/**
	 *  Get the arguments.
	 *  @return The arguments as a map of name-value pairs.
	 */
	public Map getArguments(IApplication context, ClassLoader classloader)
	{
		Map ret = null;

		if(arguments!=null)
		{
			ret = new HashMap();

			SimpleValueFetcher fetcher = new SimpleValueFetcher();
			fetcher.setValue("$platform", context.getServiceContainer());
			fetcher.setValue("$args", context.getArguments());
			fetcher.setValue("$results", context.getResults());

			String[] imports = context.getApplicationType().getAllImports();
			for(int i=0; i<arguments.size(); i++)
			{
				MArgument p = (MArgument)arguments.get(i);
				String valtext = p.getValue();
				
				if(parser==null)
					parser = new JavaCCExpressionParser();
				
				Object val = parser.parseExpression(valtext, imports, null, classloader).getValue(fetcher);
				ret.put(p.getName(), val);
			}
		}
		
		return ret;
	}
	
	/**
	 *  Get the model of the component instance.
	 *  @param apptype The application type this component is used in.
	 *  @return The name of the component type.
	 */
	public MComponentType getType(MApplicationType apptype)
	{
		MComponentType ret = null;
		List componenttypes = apptype.getMComponentTypes();
		for(int i=0; ret==null && i<componenttypes.size(); i++)
		{
			MComponentType at = (MComponentType)componenttypes.get(i);
			if(at.getName().equals(getTypeName()))
				ret = at;
		}
		return ret;
	}
}
