package jadex.tools.testcenter;

import jadex.base.SComponentFactory;
import jadex.base.test.Testcase;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.modelinfo.IArgument;
import jadex.bridge.modelinfo.IModelInfo;
import jadex.bridge.service.SServiceProvider;
import jadex.bridge.service.library.ILibraryService;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.xml.annotation.XMLClassname;

/**
 *  Helper class to identify test cases.
 */
public class STestCenter
{
	/**
	 *  Check if a component model can be started as test case.
	 */
	public static IFuture	isTestcase(final String model, IExternalAccess access, final IResourceIdentifier rid)
	{
		return access.scheduleImmediate(new IComponentStep<Boolean>()
		{
			@XMLClassname("isTestcase")
			public IFuture<Boolean> execute(IInternalAccess ia)
			{
				final Future	ret	= new Future();
				final IExternalAccess access	= ia.getExternalAccess();
				SComponentFactory.isLoadable(access, model, rid).addResultListener(new DelegationResultListener(ret)
				{
					public void customResultAvailable(Object result)
					{
						if(((Boolean)result).booleanValue())
						{
							SComponentFactory.loadModel(access, model, rid).addResultListener(new DelegationResultListener(ret)
							{
								public void customResultAvailable(Object result)
								{
									final IModelInfo model = (IModelInfo)result;
									
									if(model!=null && model.getReport()==null)
									{
										final IArgument[]	results	= model.getResults();
										access.scheduleStep(new IComponentStep<Void>()
										{
											public IFuture<Void> execute(IInternalAccess ia)
											{
												SServiceProvider.getServiceUpwards(ia.getServiceContainer(), ILibraryService.class)
													.addResultListener(new DelegationResultListener(ret)
												{
													public void customResultAvailable(Object lib)
													{
														ILibraryService ls = (ILibraryService)lib;
														boolean	istest	= false;
														for(int i=0; !istest && i<results.length; i++)
														{
															if(results[i].getName().equals("testresults") 
																&& results[i].getClassname().equals("jadex.base.test.Testcase"))
//																&& Testcase.class.equals(results[i].getClazz(ls.getClassLoader(model.getResourceIdentifier()), model.getAllImports())))
															{	
																istest	= true;
															}
														}
														ret.setResult(istest? Boolean.TRUE: Boolean.FALSE);
													}
												});
												
												return IFuture.DONE;
											}
										});
									}
									else
									{
										ret.setResult(Boolean.FALSE);
									}
									
//									ret.setResult(Boolean.valueOf(istest));
								}
							});
						}
						else
						{
							ret.setResult(Boolean.FALSE);
						}
					}
				});
				return ret;
			}
		});
	}
}

