package jadex.backup.job.processing;

import jadex.backup.job.SyncJob;
import jadex.backup.job.SyncTask;
import jadex.backup.job.SyncTaskEntry;
import jadex.backup.job.Task;
import jadex.backup.job.management.IJobManagementService;
import jadex.backup.resource.BackupEvent;
import jadex.backup.resource.BackupResource;
import jadex.backup.resource.FileInfo;
import jadex.backup.resource.ILocalResourceService;
import jadex.backup.resource.IResourceService;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateResultListener;
import jadex.commons.future.IResultListener;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *  Agent that is responsible for processing a job.
 */
@Agent
@Arguments(
{
	@Argument(name="job", clazz=SyncJob.class, description="The job that is executed by the agent."),
	@Argument(name="autoupdate", clazz=boolean.class, defaultvalue="false", description="Automatically update files or let user manually decide.")
})
@ProvidedServices(
{
	@ProvidedService(type=IJobProcessingService.class, implementation=@Implementation(JobProcessingService.class))
})
@RequiredServices(
{
	@RequiredService(name="cms", type=IComponentManagementService.class, 
		binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)),
	@RequiredService(name="rps", type=IResourceService.class, 
		binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
public class SyncJobProcessingAgent
{
	//-------- attributes --------

	/** The agent. */
	@Agent
	protected MicroAgent agent;
	
	/** The job. */
	@AgentArgument
	protected SyncJob job;
	
	/** Perform automatic updates (or just collect changes and let user decide). */
	@AgentArgument
	protected boolean autoupdate;

	/** The corresponding resource service. */
	protected ILocalResourceService resser;
	
	/** The list of resource services to sync with. */
	protected List<IResourceService> resservices;
	
	//-------- constructors --------
	
	/**
	 *  Called on startup.
	 */
	@AgentCreated
	public IFuture<Void> start()
	{
		final Future<Void>	ret	= new Future<Void>();
		
//		System.out.println("args: "+agent.getArguments());

		this.resservices = new ArrayList<IResourceService>();
		
//		SServiceProvider.getService(agent.getServiceProvider(), IJobManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
//			.addResultListener(new DefaultResultListener<IJobManagementService>()
//		{
//			public void resultAvailable(IJobManagementService js)
//			{
//				ISubscriptionIntermediateFuture<JobEvent> subscription = js.subscribe();
//				subscription.addResultListener(new SwingIntermediateDefaultResultListener<JobEvent>()
//				{
//					public void customIntermediateResultAvailable(JobEvent ce)
//					{
//						if(JobEvent.JOB_CHANGED.equals(ce.getType()))
//						{
//							if(ce.getJob().getId().equals(job.getId()))
//							{
//								System.out.println("sync agent received changed job: "+ce.getJob());
//								job = (SyncJob)ce.getJob();
//								
//								List<SyncRequest> srs = job.getSyncRequests();
//								{
//									if(srs!=null)
//									{
//										for(Iterator<SyncRequest> it=srs.iterator(); it.hasNext(); )
//										{
//											SyncRequest sr = it.next();
//											if(SyncRequest.STATE_ACKNOWLEDGED.equals(sr.getState()))
//											{
//												sr.setState(SyncRequest.STATE_ACTIVE);
//												List<SyncEntry> ses = sr.getEntries();
//												if(ses!=null)
//												{
//													updateFiles(localresser, resser, ses.iterator())
//												}
//												
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//					
//					public void customExceptionOccurred(Exception exception)
//					{
//						// todo:
//						System.out.println("ex: "+exception);
//	//					ret.setExceptionIfUndone(exception);
//					}
//				});
//			}
//		});
		
		
		IFuture<IComponentManagementService> fut = agent.getServiceContainer().getRequiredService("cms");
		fut.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Void>(ret)
		{
			public void customResultAvailable(final IComponentManagementService cms)
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("dir", job.getLocalResource());
				args.put("id", job.getGlobalResource());
				CreationInfo ci = new CreationInfo(agent.getComponentIdentifier());
				ci.setArguments(args);
				cms.createComponent(null, "jadex/backup/resource/ResourceProviderAgent.class", ci, null)
					.addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, Void>(ret)
				{
					public void customResultAvailable(IComponentIdentifier cid) 
					{
						agent.getServiceContainer().getService(ILocalResourceService.class, cid)
							.addResultListener(new ExceptionDelegationResultListener<ILocalResourceService, Void>(ret)
						{
							public void customResultAvailable(ILocalResourceService result)
							{
								resser = result;
								ret.setResult(null);
							}
						});
					}
				});
			}
		});
		
		return ret;
	}
	
	/**
	 *  The agent body.
	 */
	@AgentBody
	public void body()
	{
		agent.scheduleStep(new IComponentStep<Void>()
		{
			public IFuture<Void> execute(IInternalAccess ia)
			{
				final IComponentStep<Void> self = this;
								
				final Future<Void> fini = new Future<Void>();
				if(resservices.size()==0)
				{
					agent.getServiceContainer().searchServices(IResourceService.class, RequiredServiceInfo.SCOPE_GLOBAL)
						.addResultListener(new IntermediateDefaultResultListener<IResourceService>()
					{
						public void intermediateResultAvailable(IResourceService result)
						{
							if(result.getResourceId().equals(job.getGlobalResource())
								&& !result.getLocalId().equals(resser.getLocalId()))
							{
								resservices.add(result);
								fini.setResultIfUndone(null);
							}
						}
						
						public void finished()
						{
							if(resservices.size()>0)
							{
								fini.setResultIfUndone(null);
							}
							else
							{
								fini.setExceptionIfUndone(new RuntimeException("No sync partner"));
							}
						}
						
						public void resultAvailable(Collection<IResourceService> result)
						{
							for(IResourceService resser: result)
							{
								intermediateResultAvailable(resser);
							}
						}
						
						public void exceptionOccurred(Exception exception) 
						{
							fini.setExceptionIfUndone(new RuntimeException("No sync partner"));
						}
					});
				}
				
				fini.addResultListener(new IResultListener<Void>() 
				{
					public void resultAvailable(Void result)
					{
						startSync().addResultListener(new IResultListener<Void>()
						{ 
							public void resultAvailable(Void result)
							{
								agent.waitForDelay(60000, self);
							}
							
							public void exceptionOccurred(Exception exception)
							{
								agent.waitForDelay(60000, self);
							}
						});	
					}	
					
					public void exceptionOccurred(Exception exception) 
					{
						agent.waitForDelay(60000, self);
					}
				});
				
				return IFuture.DONE;
			}
		});
	}
	
	/**
	 * 
	 */
	protected IFuture<Void> startSync()
	{
		final Future<Void> ret = new Future<Void>();
		
		final IResourceService remresser = resservices.get(0);
		System.out.println("starting sync with: "+resservices.get(0));
		
		final SyncTask st = new SyncTask(remresser.getLocalId(), System.currentTimeMillis());
		
		resser.update(remresser).addResultListener(new IIntermediateResultListener<BackupEvent>()
		{
			public void intermediateResultAvailable(BackupEvent result)
			{
//				System.out.println(result);
				if(BackupResource.FILE_ADDED.equals(result.getType())
//					|| BackupResource.FILE_REMOVED.equals(result.getType())
					|| BackupResource.FILE_MODIFIED.equals(result.getType()))
				{
					if(!autoupdate)
					{
//						changelist.add(new Tuple2<String, FileInfo>(result.getType(), result.getFile()));
						st.addSyncEntry(new SyncTaskEntry(result.getFile(), result.getType()));
					}
					else
					{
						updateFile(resser, remresser, result.getFile()).addResultListener(new IResultListener<Void>()
						{
							public void resultAvailable(Void result)
							{
							}
							
							public void exceptionOccurred(Exception exception)
							{
								exception.printStackTrace();
							}
						});
					}
				}
			}
			
			public void finished()
			{
				System.out.println("finished sync scan");
				if(!autoupdate)
				{
					SServiceProvider.getService(agent.getServiceProvider(), IJobManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
						.addResultListener(new DefaultResultListener<IJobManagementService>()
					{
						public void resultAvailable(IJobManagementService js)
						{
							if(st.getEntries()!=null && st.getEntries().size()>0)
							{
								// Publish modified job 
								System.out.println("publishing sync request");
								job.addSyncRequest(st);
								JobProcessingService jps = (JobProcessingService)agent.getServiceContainer().getProvidedServiceRawImpl(IJobProcessingService.class);
								jps.publishEvent(new JobProcessingEvent(JobProcessingEvent.TASK_ADDED, job.getId(), st));
								ret.setResult(null);
							}
						}
					});
					
//					updateFiles(resser, remresser, changelist.iterator())
//						.addResultListener(new IResultListener<Void>()
//					{
//						public void resultAvailable(Void result)
//						{
//							resservices.remove(0);
//							agent.waitForDelay(60000, self);
//						}
//						
//						public void exceptionOccurred(Exception exception)
//						{
//							resultAvailable(null);
//						}
//					});
				}
				else
				{
					ret.setResult(null);
				}
			}
			
			public void resultAvailable(Collection<BackupEvent> result)
			{
				System.out.println("finished sync");
				resservices.remove(0);
				ret.setResult(null);
			}
			
			public void exceptionOccurred(Exception exception)
			{
				System.out.println("Update error: "+exception);
				ret.setResult(null);
			}
		});
		
		return ret;
	}
	
	/**
	 * 
	 */
	public void jobModified(SyncJob job)
	{
		if(job.getId().equals(job.getId()))
		{
			System.out.println("sync agent received changed job: "+job);
			this.job = job;
			
			List<Task> srs = job.getTasks();
			{
				if(srs!=null)
				{
					for(Iterator<Task> it=srs.iterator(); it.hasNext(); )
					{
						SyncTask sr = (SyncTask)it.next();
						if(SyncTask.STATE_ACKNOWLEDGED.equals(sr.getState()))
						{
							sr.setState(SyncTask.STATE_ACTIVE);
							List<SyncTaskEntry> ses = sr.getEntries();
							if(ses!=null)
							{
								IResourceService remresser = findRessourceService(sr.getSource());
								updateFiles(resser, remresser, ses.iterator())
									.addResultListener(new IResultListener<Void>()
								{
									public void resultAvailable(Void result)
									{
										System.out.println("Finished updating files");
									}

									public void exceptionOccurred(Exception exception)
									{
										System.out.println("Exception during updating files");
									}
								});
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 */
	protected IResourceService findRessourceService(String localid)
	{
		IResourceService ret = null;
		for(IResourceService res: resservices)
		{
			if(res.getLocalId().equals(localid))
			{
				ret = res;
				break;
			}
		}
		return ret;
	}
	
	/**
	 * 
	 */
	public IFuture<Void> updateFiles(final ILocalResourceService localresser, final IResourceService resser, final Iterator<SyncTaskEntry> it)
	{
		final Future<Void> ret = new Future<Void>();
		
		if(it.hasNext())
		{
			updateFile(localresser, resser, it.next())
				.addResultListener(new DelegationResultListener<Void>(ret)
			{
				public void customResultAvailable(Void result)
				{
					updateFiles(localresser, resser, it);
				}
			});
		}
		else
		{
			ret.setResult(null);
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	public IFuture<Void> updateFile(ILocalResourceService localresser, IResourceService resser, SyncTaskEntry se)
	{
		return se.isIncluded()? updateFile(localresser, resser, se.getFileInfo()): IFuture.DONE;
	}
		
	/**
	 * 
	 */
	public IFuture<Void> updateFile(ILocalResourceService localresser, IResourceService resser, FileInfo fi)
	{
		final Future<Void> ret = new Future<Void>();
		
		localresser.updateFile(resser, fi)
			.addResultListener(new IIntermediateResultListener<BackupEvent>()
		{
			public void intermediateResultAvailable(BackupEvent result)
			{
				System.out.println("upfi: "+result);
			}
			
			public void exceptionOccurred(Exception exception)
			{
				System.out.println(exception);
				ret.setResult(null);
			}
			
			public void resultAvailable(Collection<BackupEvent> result)
			{
				System.out.println(result);
				ret.setResult(null);
			}
			
			public void finished()
			{
				System.out.println("fini");
				ret.setResult(null);
			}
		});
		
		return ret;
	}

	/**
	 *  Get the job.
	 *  @return The job.
	 */
	public SyncJob getJob()
	{
		return job;
	}
	
}
