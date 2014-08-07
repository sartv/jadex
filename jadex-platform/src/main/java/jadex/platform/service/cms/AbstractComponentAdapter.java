package jadex.platform.service.cms;

import jadex.base.Starter;
import jadex.bridge.CheckedAction;
import jadex.bridge.ComponentTerminatedException;
import jadex.bridge.DefaultMessageAdapter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentInterpreter;
import jadex.bridge.IConnection;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IMessageAdapter;
import jadex.bridge.modelinfo.IModelInfo;
import jadex.bridge.service.IServiceContainer;
import jadex.bridge.service.IServiceProvider;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Timeout;
import jadex.bridge.service.component.ComponentSuspendable;
import jadex.bridge.service.component.interceptors.CallAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.factory.IComponentAdapter;
import jadex.bridge.service.types.message.MessageType;
import jadex.commons.SReflect;
import jadex.commons.Tuple2;
import jadex.commons.concurrent.Executor;
import jadex.commons.concurrent.IExecutable;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.FutureHelper;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.ISuspendable;
import jadex.kernelbase.StatelessAbstractInterpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *  Base component adapter with reusable functionality for all platforms.
 */
//public abstract class AbstractComponentAdapter implements IComponentAdapter, IExecutable
{
	//-------- attributes --------

	/** The component identifier. */
	protected IExternalAccess parent;

	/** The component instance. */
	protected IComponentInterpreter component;
	
	/** The component model. */
	protected IModelInfo model;

	/** The description holding the execution state of the component
	   (read only! managed by component execution service). */
	protected IComponentDescription	desc;
	
	/** The component logger. */
	protected Logger logger;
	
	/** Flag to indicate a fatal error (component termination will not be passed to instance) */
	protected Exception exception;
	
	/** Flag to indicate that the component instance is created. */
	protected boolean	instantiated;
	
	/** The kill future to be notified in case of fatal error during shutdown. */
	protected Future<Void>	killfuture;
	
	/** Flag for testing double execution. */
	protected volatile boolean executing;
	
	/** The blocked threads (i.e. monitors) to be aborted on termination. */
	protected Map<Object, Executor>	blocked;
	
	//-------- steppable attributes --------
	
	/** The flag for a scheduled step (true when a step is allowed in stepwise execution). */
	protected boolean	dostep;
	
	/** The listener to be informed, when the requested step is finished. */
	protected Future<Void> stepfuture;
	
	/** The selected breakpoints (component will change to step mode, when a breakpoint is reached). */
	protected Set<String>	breakpoints;
	
	//-------- external actions --------

	/** The thread executing the component (null for none). */
	// Todo: need not be transient, because component should only be serialized when no action is running?
	protected transient Thread componentthread;

	// todo: ensure that entries are empty when saving
	/** The entries added from external threads. */
	protected List<Runnable>	ext_entries;

	/** The flag if external entries are forbidden. */
	protected boolean ext_forbidden;
	
	/** Set when wakeup was called. */
	protected boolean	wokenup;
	
	/** The cached cms. */
	protected IFuture<IComponentManagementService>	cms;

	/** The cached clock service. */
	protected IClockService clock;
	
	/** Retained listener notifications when switching threads due to blocking. */
	protected List<Tuple2<Future<?>, IResultListener<?>>>	notifications;
	
	//-------- constructors --------

	/**
	 *  Create a new component adapter.
	 *  Uses the thread pool for executing the component.
	 */
	public AbstractComponentAdapter(IComponentDescription desc, IModelInfo model, IComponentInterpreter component, IExternalAccess parent)
	{
		this.desc = desc;
		this.model = model;
		this.component = component;
		this.parent	= parent;
	}
	
	//-------- IComponentAdapter methods --------

	/**
	 *  Called by the component when it probably awoke from an idle state.
	 *  The platform has to make sure that the component will be executed
	 *  again from now on.
	 *  Note, this method can be called also from external threads
	 *  (e.g. property changes). Therefore, on the calling thread
	 *  no component related actions must be executed (use some kind
	 *  of wake-up mechanism).
	 *  Also proper synchronization has to be made sure, as this method
	 *  can be called concurrently from different threads.
	 */
	public void wakeup()
	{
		// Do not wake up until component instance is completely instantiated by factory
		// (to avoid double execution between constructor and executor)
		if(!instantiated)
		{
			return;
		}
		
		if(clock==null)
		{
			SServiceProvider.getService((IServiceProvider)getServiceContainer(), IClockService.class, RequiredServiceInfo.SCOPE_PLATFORM)
				.addResultListener(new DefaultResultListener<IClockService>(logger)
			{
				public void resultAvailable(IClockService result)
				{
					clock = result;
					wakeup();
				}

				public void exceptionOccurred(Exception exception)
				{
					if(!(exception instanceof ComponentTerminatedException))
						super.exceptionOccurred(exception);
				}
			});
		}
		else
		{
			wokenup	= true;
			if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()))
				throw new ComponentTerminatedException(desc.getName());
			
			// Resume execution of the component.
			if(IComponentDescription.STATE_ACTIVE.equals(desc.getState())
				|| IComponentDescription.STATE_SUSPENDED.equals(desc.getState()))	// Hack!!! external entries must also be executed in suspended state.
			{
				
//				if(getComponentIdentifier().toString().indexOf("rms")!=-1)
////					getModel().getFullName().indexOf("testcases.threading")!=-1)
//				{
//					System.out.println("doWakeup: "+getComponentIdentifier()+", "+System.currentTimeMillis());
//				}

				doWakeup();
			}
		}
	}

	/**
	 *  Return a component-identifier that allows to send
	 *  messages to this component.
	 */
	public IComponentIdentifier getComponentIdentifier()
	{
		return desc.getName();
	}
	
	/**
	 *  Get the logger.
	 *  @return The logger.
	 */
	public Logger getLogger()
	{
		if(logger==null)
		{
			// todo: problem: loggers can cause memory leaks
			// http://bugs.sun.com/view_bug.do;jsessionid=bbdb212815ddc52fcd1384b468b?bug_id=4811930
			String name = getLoggerName(getComponentIdentifier());
			
			
//			logger = LogManager.getLogManager().getLogger(name);	// Problems on app engine!?
			
			// if logger does not already exist, create it
			if(logger==null)
			{
				// Hack!!! Might throw exception in applet / webstart.
				try
				{
					logger = Logger.getLogger(name);
					initLogger(logger);
					logger = createLoggerWrapper(logger, clock);
					//System.out.println(logger.getParent().getLevel());
				}
				catch(SecurityException e)
				{
					// Hack!!! For applets / webstart use anonymous logger.
					logger = Logger.getAnonymousLogger();
					initLogger(logger);
					logger = createLoggerWrapper(logger, clock);
				}
			}
		}
		
		return logger;
	}

	/**
	 *  Cannot use logger wrapper on appengine :-(.
	 */
	protected static Logger createLoggerWrapper(Logger logger, IClockService clock)
	{
		Logger	ret	= logger;
		try
		{
			Class<?>	lwclass	= SReflect.classForName("jadex.platform.service.cms.LoggerWrapper", AbstractComponentAdapter.class.getClassLoader());
			ret	= (Logger)lwclass.getConstructor(Logger.class, IClockService.class).newInstance(logger, clock);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return ret;
	}

	/**
	 *  Get the logger name.
	 *  @param cid The component identifier.
	 *  @return The name.
	 */
	public static String getLoggerName(IComponentIdentifier cid)
	{
		// Prepend parent names for nested loggers.
		String	name	= null;
		for(; cid!=null; cid=cid.getParent())
		{
			name	= name==null ? cid.getLocalName() : cid.getLocalName() + "." +name;
		}
		return name;
	}
	
	/**
	 *  Init the logger with capability settings.
	 *  @param logger The logger.
	 */
	protected void initLogger(Logger logger)
	{
		// get logging properties (from ADF)
		// the level of the logger
		// can be Integer or Level
		
		Object prop = component.getProperty("logging.level");
		Level level = prop!=null? (Level)prop : logger.getParent()!=null && logger.getParent().getLevel()!=null ? logger.getParent().getLevel() : Level.SEVERE;
		logger.setLevel(level);
		
		// if logger should use Handlers of parent (global) logger
		// the global logger has a ConsoleHandler(Level:INFO) by default
		prop = component.getProperty("logging.useParentHandlers");
		if(prop!=null)
		{
			logger.setUseParentHandlers(((Boolean)prop).booleanValue());
		}
			
		// add a ConsoleHandler to the logger to print out
        // logs to the console. Set Level to given property value
		prop = component.getProperty("logging.addConsoleHandler");
		if(prop!=null)
		{
			Handler console;
			/*if[android]
			console = new jadex.commons.android.AndroidHandler();
			 else[android]*/
			console = new ConsoleHandler();
			/* end[android]*/
			
            console.setLevel(Level.parse(prop.toString()));
            logger.addHandler(console);
        }
		
		// Code adapted from code by Ed Komp: http://sourceforge.net/forum/message.php?msg_id=6442905
		// if logger should add a filehandler to capture log data in a file. 
		// The user specifies the directory to contain the log file.
		// $scope.getAgentName() can be used to have agent-specific log files 
		//
		// The directory name can use special patterns defined in the
		// class, java.util.logging.FileHandler, 
		// such as "%h" for the user's home directory.
		// 
		String logfile =	(String)component.getProperty("logging.file");
		if(logfile!=null)
		{
		    try
		    {
			    Handler fh	= new FileHandler(logfile);
		    	fh.setFormatter(new SimpleFormatter());
		    	logger.addHandler(fh);
		    }
		    catch (IOException e)
		    {
		    	System.err.println("I/O Error attempting to create logfile: "
		    		+ logfile + "\n" + e.getMessage());
		    }
		}
		
		// Add further custom log handlers.
		prop = component.getProperty("logging.handlers");
		if(prop!=null)
		{
			if(prop instanceof Handler)
			{
				logger.addHandler((Handler)prop);
			}
			else if(SReflect.isIterable(prop))
			{
				for(Iterator<?> it=SReflect.getIterator(prop); it.hasNext(); )
				{
					Object obj = it.next();
					if(obj instanceof Handler)
					{
						logger.addHandler((Handler)obj);
					}
					else
					{
						logger.warning("Property is not a logging handler: "+obj);
					}
				}
			}
			else
			{
				logger.warning("Property 'logging.handlers' must be Handler or list of handlers: "+prop);
			}
		}
	}
	
	/**
	 *  Get the model.
	 *  @return The model.
	 */
	public IModelInfo getModel()
	{
		return this.model;
	}

	/**
	 *  Get the parent component.
	 *  @return The parent (if any).
	 */
	public IExternalAccess getParent()
	{
		return parent;
	}
	
	/**
	 *  String representation of the component.
	 */
	public String toString()
	{
		return "StandaloneComponentAdapter("+desc.getName().getName()+")";
	}

	
	/**
	 *  Get the service provider.
	 */
	public IServiceContainer getServiceContainer()
	{
		return component.getServiceContainer();
	}
	
	/**
	 *  Get the (cached) cms.
	 */
	protected IFuture<IComponentManagementService> getCMS()
	{
		// Change comments below to test performance of cached cms vs. direct access.
		if(getServiceContainer()==null)
		{
			System.out.println("container is null: "+component+", "+getComponentIdentifier());
		}
//		return SServiceProvider.getServiceUpwards(getServiceContainer(), IComponentManagementService.class);
		if(cms==null || cms.getException()!=null)
		{
			cms	= SServiceProvider.getServiceUpwards((IServiceProvider)getServiceContainer(), IComponentManagementService.class);
			cms.addResultListener(new IResultListener<IComponentManagementService>()
			{
				public void resultAvailable(IComponentManagementService result)
				{
					// Replace future to save memory (all listeners and searchmanagers can be garbage collected)
					cms	= new Future<IComponentManagementService>(result);
				}
				
				public void exceptionOccurred(Exception exception)
				{
					// Replace future to save memory (all listeners and searchmanagers can be garbage collected)
					cms	= new Future<IComponentManagementService>(exception);
				}
			});
		}
		return cms;
	}
	
	//-------- methods called by the standalone platform --------
	
	/**
	 *  Set the inited flag to allow external component wake ups.
	 */
	public void	setInited(boolean inited)
	{
		this.instantiated	= inited;
	}
	
	/**
	 *  Get description.
	 */
	public IComponentDescription getDescription()
	{
		return desc;
	}
	
	/**
	 *  Gracefully terminate the component.
	 *  This method is called from cms and delegated to the reasoning engine,
	 *  which might perform arbitrary cleanup actions, goals, etc.
	 *  @return A future to indicate, when cleanup of the component is finished.
	 */
	public IFuture<Void> killComponent()
	{
		assert killfuture==null;
		
		killfuture = new Future<Void>();
		
		if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()))
		{
			killfuture.setException(new ComponentTerminatedException(desc.getName()));
		}
		else
		{
			if(exception==null)
			{
				invokeLater(new Runnable()
				{
					public void run()
					{
//						if(desc.getName().getLocalName().startsWith("Initiator"))
//							System.out.println("killComponent last step: "+getComponentIdentifier());
						component.cleanupComponent()
							.addResultListener(new IResultListener<Void>()
						{
							public void resultAvailable(Void result)
							{
								synchronized(AbstractComponentAdapter.this)
								{
									// Do final cleanup step as (last) ext_entry
									// for allowing previously added entries still be executed.
//									if(desc.getName().getLocalName().startsWith("Initiator"))
//										System.out.println("killComponent last step: "+getComponentIdentifier());

									Runnable laststep = new LastStep();
//									Runnable	laststep	= new Runnable()
//									{								
//										public void run()
//										{
//											clock	= null;
//											cms	= null;
////											component	= null;	// Required by getResults()
//											model	= null;
////											desc	= null;	// Required by toString()
//											parent	= null;
//											killfuture.setResult(null);
//											
////											System.out.println("Checking ext entries after cleanup: "+cid);
//											assert ext_entries==null || ext_entries.isEmpty() : "Ext entries after cleanup: "+desc.getName()+", "+ext_entries;
//										}
//									};
									// In case of platform invokerLater cannot be called.
									if(getComponentIdentifier().getParent()!=null)
									{
										invokeLater(laststep);
//										if(ext_entries==null)
//											ext_entries	= new ArrayList();
//										ext_entries.add(laststep);
										// No more ext entries after cleanup step allowed.
										ext_forbidden	= true;
//										wakeup();
									}
									else
									{
										// Execute last step of platform directly
										// No more ext entries after cleanup step allowed.
										
										// Resets component thread to avoid asserts
										Thread oldct = componentthread;
										componentthread	= Thread.currentThread();
										ext_forbidden	= true;
										if(ext_entries==null)
											ext_entries	= new ArrayList<Runnable>();
										ext_entries.add(laststep);
										executeExternalEntries(true);
										ext_forbidden	= true;
										componentthread = oldct;
//										laststep.run();
									}
								}
							}
							
							public void exceptionOccurred(Exception exception)
							{
								getLogger().warning("Exception during component cleanup: "+getComponentIdentifier()+", "+exception);
								killfuture.setException(exception);
							}
						});
					}
				});
			}
			else
			{
				killfuture.setResult(null);
//				listener.resultAvailable(this, getComponentIdentifier());
			}
		}
		
		return killfuture;
		
		// LogManager causes memory leak till Java 7
		// No way to remove loggers and no weak references. 
	}
	
	/**
	 *  Called when a message was sent to the component.
	 *  (Called from message transport).
	 *  (Is it ok to call on external thread?).
	 */
	public void	receiveMessage(Map<String, Object> message, MessageType type)
	{
		if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()) || exception!=null)
			throw new ComponentTerminatedException(desc.getName());

		// Add optional receival time.
//		String rd = type.getReceiveDateIdentifier();
//		Object recdate = message.get(rd);
//		if(recdate==null)
//			message.put(rd, new Long(getClock().getTime()));
		
		IMessageAdapter msg = new DefaultMessageAdapter(message, type);
		component.messageArrived(msg);
	}
	
	/**
	 *  Called when a stream was sent to the component.
	 *  (Called from message transport).
	 *  (Is it ok to call on external thread?).
	 */
	public void	receiveStream(IConnection con)
	{
		if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()) || exception!=null)
			throw new ComponentTerminatedException(desc.getName());

		component.streamArrived(con);
	}
	
	//-------- IExecutable interface --------
	
	/**
	 *  Executable code for running the component
	 *  in the platforms executor service.
	 */
	public boolean	execute()
	{
		boolean	ret;
		
//		if(getComponentIdentifier().toString().startsWith("ExternalTask"))
//		{
//			System.out.println("Enter: "+getComponentIdentifier()+", "+System.currentTimeMillis()+", "+Thread.currentThread().hashCode());
//		}
		
		ISuspendable.SUSPENDABLE.set(new ComponentSuspendable(this));
		
//		synchronized(this)
		{
			if(executing)
			{
				System.err.println(getComponentIdentifier()+": double execution"+" "+Thread.currentThread()+" "+componentthread);
				new RuntimeException("executing: "+getComponentIdentifier()).printStackTrace();
			}
			executing	= true;
		}
//		if(getComponentIdentifier().toString().indexOf("@Receiver.EventSystem")!=-1)
//		{
//			System.err.println(getComponentIdentifier()+": execution0 "+System.identityHashCode(Executor.EXECUTOR.get()));
//		}
		wokenup	= false;	
		
		// Note: wakeup() can be called from arbitrary threads (even when the
		// component itself is currently running. I.e. it cannot be ensured easily
		// that an execution task is enqueued and the component has terminated
		// meanwhile.
		if(!IComponentDescription.STATE_TERMINATED.equals(desc.getState()))
		{
			if(exception!=null)
			{
				this.executing	= false;
				return false;	// Component already failed: tell executor not to call again. (can happen during failed init)
			}
	
			// Remember execution thread.
			this.componentthread	= Thread.currentThread();
//			System.out.println("set local: "+getComponentIdentifier());
			IComponentIdentifier.LOCAL.set(getComponentIdentifier());
			IComponentAdapter.LOCAL.set(this);
			
			ClassLoader	cl	= Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(component.getClassLoader());
			
			// Process listener notifications from old component thread.
			boolean notifexecuted	= false;
			if(notifications!=null)
			{
				FutureHelper.addStackedListeners(notifications);
//				System.out.println("readded stack size: "+notifications.size()+", "+getComponentIdentifier());
				notifications	= null;
				
				try
				{
					FutureHelper.notifyStackedListeners();
					notifexecuted	= true;
				}
				catch(Exception e)
				{
					fatalError(e);
				}
				catch(StepAborted sa)
				{
				}
				catch(Throwable t)
				{
					fatalError(new RuntimeException(t));
				}

			}
	
			// Copy actions from external threads into the state.
			// Is done in before tool check such that tools can see external actions appearing immediately (e.g. in debugger).
			boolean extexecuted	= false;
			if(!notifexecuted)
			{
				try
				{
	//				if(getComponentIdentifier()!=null && getComponentIdentifier().getParent()==null)
	//					System.out.println("Ext Executing: "+getComponentIdentifier()+", "+Thread.currentThread());
					extexecuted	= executeExternalEntries(false);
	//				if(getComponentIdentifier()!=null && getComponentIdentifier().getParent()==null)
	//					System.out.println("Ext Not Executing: "+getComponentIdentifier()+", "+Thread.currentThread());
				}
				catch(Exception e)
				{
					fatalError(e);
				}
				catch(StepAborted sa)
				{
				}
				catch(Throwable t)
				{
					fatalError(new RuntimeException(t));
				}
			}
				
			// Suspend when breakpoint is triggered.
			// Necessary because component wakeup could be called anytime even if is at breakpoint..
			boolean	breakpoint_triggered	= false;
			if(!dostep && !IComponentDescription.STATE_SUSPENDED.equals(desc.getState()))
			{
				if(component.isAtBreakpoint(desc.getBreakpoints()))
				{
					breakpoint_triggered	= true;
					getCMS().addResultListener(new DefaultResultListener<IComponentManagementService>(logger)
					{
						public void resultAvailable(IComponentManagementService cms)
						{
							cms.suspendComponent(desc.getName());
						}
						
						public void exceptionOccurred(Exception exception)
						{
							if(!(exception instanceof ComponentTerminatedException))
							{
								super.exceptionOccurred(exception);
							}
						}
					});
				}
			}
			boolean	again	= false;
			if(!breakpoint_triggered && !extexecuted  && !notifexecuted && (!IComponentDescription.STATE_SUSPENDED.equals(desc.getState()) || dostep))
			{
				try
				{
//					if(getComponentIdentifier()!=null && getComponentIdentifier().getParent()==null)
//						System.out.println("Executing: "+getComponentIdentifier()+", "+Thread.currentThread());
					again	= component.executeStep();
//					if(getComponentIdentifier()!=null && getComponentIdentifier().getParent()==null)
//						System.out.println("Not Executing: "+getComponentIdentifier()+", "+Thread.currentThread());
				}
				catch(Exception e)
				{
					fatalError(e);
				}
				catch(StepAborted sa)
				{
				}
				catch(Throwable t)
				{
					fatalError(new RuntimeException(t));
				}
				if(dostep)
				{
					dostep	= false;
					if(stepfuture!=null)
					{
						stepfuture.setResult(null);
					}
				}
				
				// Suspend when breakpoint is triggered.
				if(!IComponentDescription.STATE_SUSPENDED.equals(desc.getState()))
				{
					if(component.isAtBreakpoint(desc.getBreakpoints()))
					{
						breakpoint_triggered	= true;
						getCMS().addResultListener(new DefaultResultListener<IComponentManagementService>(logger)
						{
							public void resultAvailable(IComponentManagementService cms)
							{
								cms.suspendComponent(desc.getName());
							}
							
							public void exceptionOccurred(Exception exception)
							{
								if(!(exception instanceof ComponentTerminatedException))
								{
									super.exceptionOccurred(exception);
								}
							}
						});
					}
				}
			}
			
			// Reset execution thread.
			IComponentIdentifier.LOCAL.set(null);
			IComponentAdapter.LOCAL.set(null);
			// Must reset service call settings when thread retreats from components
			CallAccess.resetCurrentInvocation();
			CallAccess.resetNextInvocation();
			Thread.currentThread().setContextClassLoader(cl);
			this.componentthread = null;
			
//			if(getComponentIdentifier()!=null && getComponentIdentifier().getParent()==null)
//				System.out.println("Set to null: "+getComponentIdentifier()+", "+Thread.currentThread());
			
			ret	= (again && !IComponentDescription.STATE_SUSPENDED.equals(desc.getState())) || extexecuted || notifexecuted;
		}
		else
		{
			ret	= false;
		}
		
//		if(getComponentIdentifier().toString().indexOf("@Receiver.EventSystem")!=-1)
//		{
//			System.err.println(getComponentIdentifier()+": !execution0 "+System.identityHashCode(Executor.EXECUTOR.get()));
//		}
		executing	= false;
		ISuspendable.SUSPENDABLE.set(null);

//		if(getComponentIdentifier().toString().indexOf("ExternalTask")!=-1)
////		if(getModel().getFullName().indexOf("marsworld.sentry")!=-1)
//		{
//			System.out.println("Leave: "+getComponentIdentifier()+", "+System.currentTimeMillis());
//		}
		
//		if(getComponentIdentifier().getName()!=getComponentIdentifier().getPlatformName() && Future.STACK.get()!=null && Future.STACK.get()!=null)
//		{
//			System.out.println("Again: "+getComponentIdentifier()+", "+ret+", "+Thread.currentThread()+", "+Future.STACK.get());
//		}
		
		return ret;
	}
	
	/**
	 *  Block the current thread and allow execution on other threads.
	 *  @param monitor	The monitor to wait for.
	 */
	public void	block(final Object monitor, long timeout)
	{
		if(Thread.currentThread()!=componentthread)
		{
			throw new RuntimeException("Can only block current component thread: "+componentthread+", "+Thread.currentThread());
		}
		
		// Retain listener notifications for new component thread.
		assert notifications==null;
		notifications	= FutureHelper.removeStackedListeners();
//		System.out.println("removed stack size: "+notifications.size()+", "+getComponentIdentifier());
		
		Executor	exe	= Executor.EXECUTOR.get();
		if(exe==null)
		{
			throw new RuntimeException("Cannot block: no executor");
		}
		
		component.beforeBlock();
		
//		if(getComponentIdentifier().toString().indexOf("@Receiver.EventSystem")!=-1)
//		{
//			System.err.println(getComponentIdentifier()+": !execution1 "+System.identityHashCode(Executor.EXECUTOR.get()));
//		}
		this.executing	= false;
		this.componentthread	= null;
//		
//		if(getComponentIdentifier().toString().indexOf("IntermediateTest")!=-1)
////		if(getModel().getFullName().indexOf("marsworld.sentry")!=-1)
//		{
//			System.out.println("Blocking: "+getComponentIdentifier()+", "+System.currentTimeMillis());
//		}
		
		if(blocked==null)
		{
			blocked	= new HashMap<Object, Executor>();
		}
		blocked.put(monitor, exe);
		
		
		
		
		final boolean[]	unblocked	= new boolean[1];
		
		if(timeout!=Timeout.NONE)
		{
			((StatelessAbstractInterpreter)getComponentInstance()).waitForDelay(timeout)
				.addResultListener(new IResultListener<Void>()
			{
				public void resultAvailable(Void result)
				{
					if(!unblocked[0])
					{
//						if(getComponentIdentifier().toString().indexOf("IntermediateTest")!=-1)
//						{
//							System.out.println("Unblocking after timeout: "+getComponentIdentifier()+", "+System.currentTimeMillis());
//						}
						
						// Cannot use timeout exception as component would not be correctly entered.
						// Todo: allow informing future about timeout.
						unblock(monitor, null); //new TimeoutException());
					}
//					else if(getComponentIdentifier().toString().indexOf("IntermediateTest")!=-1)
//					{
//						System.out.println("Not unblocking after timeout (already unblocked): "+getComponentIdentifier()+", "+System.currentTimeMillis());
//					}
				}
				
				public void exceptionOccurred(Exception exception)
				{
				}
			});
		}
		
		exe.blockThread(monitor);
		
		unblocked[0]	= true;
		
		
		
		
		assert !IComponentDescription.STATE_TERMINATED.equals(desc.getState());
		
//		if(getComponentIdentifier().toString().indexOf("IntermediateTest")!=-1)
////		if(getModel().getFullName().indexOf("marsworld.sentry")!=-1)
//		{
//			System.out.println("Unblocked: "+getComponentIdentifier()+", "+System.currentTimeMillis());
//		}
//		
		synchronized(this)
		{
			if(executing)
			{
				System.err.println(getComponentIdentifier()+": double execution");
				new RuntimeException("executing: "+getComponentIdentifier()).printStackTrace();
			}
			this.executing	= true;
		}
//		if(getComponentIdentifier().toString().indexOf("@Receiver.EventSystem")!=-1)
//		{
//			System.err.println(getComponentIdentifier()+": execution1 "+System.identityHashCode(Executor.EXECUTOR.get()));
//		}

		this.componentthread	= Thread.currentThread();
		
		component.afterBlock();
	}
	
	/**
	 *  Unblock the thread waiting for the given monitor
	 *  and cease execution on the current thread.
	 *  @param monitor	The monitor to notify.
	 */
	public void	unblock(Object monitor, Throwable exception)
	{
		if(Thread.currentThread()!=componentthread)
		{
			throw new RuntimeException("Can only unblock current component thread: "+componentthread+", "+Thread.currentThread());
		}
		
		Executor exe = blocked.remove(monitor);
		if(blocked.isEmpty())
		{
			blocked	= null;
		}
				
		exe.switchThread(monitor, exception);
	}

	/**
	 *  Execute external entries.
	 */
	protected boolean executeExternalEntries(boolean platform)
	{
		// Copy actions from external threads into the state.
		// Is done in before tool check such that tools can see external actions appearing immediately (e.g. in debugger).
		boolean	extexecuted	= false;
		
		Runnable[]	entries	= null;
		synchronized(this)
		{
			if(ext_entries!=null && !(ext_entries.isEmpty()))
			{
				entries	= (Runnable[])ext_entries.toArray(new Runnable[ext_entries.size()]);
				ext_entries.clear();
				
				extexecuted	= true;
			}
		}
		
		try
		{
			for(int i=0; entries!=null && i<entries.length; i++)
			{
				if(entries[i] instanceof CheckedAction)
				{
					if(((CheckedAction)entries[i]).isValid())
					{
						entries[i].run();
					}
					((CheckedAction)entries[i]).cleanup();
				}
				else //if(entries[i] instanceof Runnable)
				{
					entries[i].run();
				}
			}
		}
		catch(Exception e)
		{
			fatalError(e);
		}
		
		return extexecuted;
	}
	
	/**
	 * 	Called when an error occurs during component execution.
	 *  @param e	The error.
	 */
	protected void fatalError(final Exception e)
	{
//		if(getComponentIdentifier().toString().indexOf("ExternalTask")!=-1)
//		{
//			System.out.println("fatal error: "+getComponentIdentifier()+", "+System.currentTimeMillis());
//		}
//		e.printStackTrace();
		if(getComponentIdentifier().getParent()==null)
		{
//			System.err.println("fatal platform error: "+getComponentIdentifier());
//			e.printStackTrace();
			getLogger().log(Level.SEVERE, "fatal platform error: "+getComponentIdentifier(), e);
		}
		else
		{
			getLogger().info("fatal error: "+getComponentIdentifier()+e.getMessage());
		}
		
		// Fatal error!
		exception = e;
		
		if(killfuture!=null)
		{
			// Already in termination.
			// todo: shouldn't be called when killfuture already done!?
			killfuture.setExceptionIfUndone(exception);
		}
		else
		{
			// Remove component from platform.
			getCMS().addResultListener(new DefaultResultListener<IComponentManagementService>(logger)
			{
				public void resultAvailable(IComponentManagementService cms)
				{
//					cms.setComponentException(cid, e);
//					System.err.println("fatal error -> destroy: "+getComponentIdentifier());
//					e.printStackTrace();
					cms.destroyComponent(desc.getName());
				}
				
				public void exceptionOccurred(Exception exception)
				{
					if(!(exception instanceof ComponentTerminatedException))
					{
						super.exceptionOccurred(exception);
					}
				}
			});
		}
	}
	
	/**
	 *  Get the exception.
	 *  @return The exception.
	 */
	public Exception getException()
	{
		return exception;
	}
	
	/**
	 *  Check if the external thread is accessing.
	 *  @return True, if called from an external (i.e. non-synchronized) thread.
	 */
	public boolean isExternalThread()
	{
		boolean ret = Thread.currentThread()!=componentthread && 
			!(IComponentDescription.STATE_TERMINATED.equals(getDescription().getState()) 
				&& Starter.isRescueThread(getComponentIdentifier()));
		if(ret)
			ret = getComponentInstance().isExternalThread();
		return ret;
	}
	
	//-------- external access --------
	
	/**
	 *  Execute an action on the component thread.
	 *  May be safely called from any (internal or external) thread.
	 *  The contract of this method is as follows:
	 *  The component adapter ensures the execution of the external action, otherwise
	 *  the method will throw a terminated exception.
	 *  @param action The action to be executed on the component thread.
	 */
	public void invokeLater(Runnable action)
	{
		if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()) || exception!=null)
			throw new ComponentTerminatedException(desc.getName());

		synchronized(this)
		{
//			System.out.println("Adding to ext entries: "+cid);
			if(ext_forbidden)
			{
				throw new ComponentTerminatedException(desc.getName());
//				{
//					public void printStackTrace()
//					{
//						Thread.dumpStack();
//					}
//				};
			}
			else
			{
				if(ext_entries==null)
					ext_entries	= new ArrayList<Runnable>();
				ext_entries.add(action);
			}
		}

		try
		{
			wakeup();
		}
		catch(ComponentTerminatedException cte)
		{
			// If wakeup doesn't work -> remove action as it gets executed on rescue thread.
			synchronized(this)
			{
				if(ext_entries!=null)
				{
					ext_entries.remove(action);
				}
			}
			
			throw cte;
		}
	}
	
	//-------- test methods --------
	
	/**
	 *  Make kernel component available.
	 */
	public IComponentInterpreter	getComponentInstance()
	{
		return component;
	}

	//-------- step handling --------
	
	/**
	 *  Set the step mode.
	 */
	public IFuture<Void> doStep()
	{
		Future<Void> ret = new Future<Void>();
		if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()) || exception!=null)
		{
			ret.setException(new ComponentTerminatedException(desc.getName()));
		}
		else if(dostep)
		{
			ret.setException(new RuntimeException("Only one step allowed at a time."));
		}
		
		this.dostep	= true;		
		this.stepfuture = ret;
		
		wakeup();
		
		return ret;
	}
	
	public class LastStep implements Runnable
	{
		public void run()
		{
//			if(desc.getName().getLocalName().indexOf("Initiator")!=-1)
//				System.out.println("killComponent last: "+getComponentIdentifier());

			cleanup();
			clock	= null;
			cms	= null;
//			component	= null;	// Required by getResults()
//			model	= null;	// Required by message service?
//			desc	= null;	// Required by toString()
			parent	= null;
			killfuture.setResult(null);
			
//			System.out.println("Checking ext entries after cleanup: "+cid);
			assert ext_entries==null || ext_entries.isEmpty() : "Ext entries after cleanup: "+desc.getName()+", "+ext_entries;
		}
	}

	/**
	 *  Wake up this component.
	 */
	protected abstract void	doWakeup();
	
	/**
	 *  Clean up this component.
	 */
	public void	cleanup()
	{
//		if(toString().indexOf("Tester@")!=-1)
//		{
//			System.err.println("cleanup: "+this+", "+(blocked!=null?blocked.size():0));
//		}
		while(blocked!=null && !blocked.isEmpty())
		{
			// Unblock throwing thread death as component already has been terminated.
			unblock(blocked.keySet().iterator().next(), new StepAborted());
		}
	}
}
