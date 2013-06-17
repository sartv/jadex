package jadex.platform.service.email;

import jadex.base.Starter;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.email.Email;
import jadex.bridge.service.types.email.IEmailService;
import jadex.commons.future.ISuspendable;
import jadex.commons.future.ThreadSuspendable;

public class EmailTest
{
	public static void	main(String[] args)
	{
		args	= new String[]
		{
			"-gui", "false"
		};
		
		ISuspendable.SUSPENDABLE.set(new ThreadSuspendable());
		IExternalAccess	exta	= Starter.createPlatform(args).get();
		IComponentManagementService	cms	= SServiceProvider.getService(exta.getServiceProvider(),
			IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
		cms.createComponent(null, "jadex/platform/service/email/EmailAgent.class", null, null).get();
		
		IEmailService	ems	= SServiceProvider.getService(exta.getServiceProvider(),
			IEmailService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
		
		try
		{
			ems.sendEmail(new Email(null, "test", "email test", "pokahr@gmx.net"), null).get();
		}
		finally
		{
			exta.killComponent();
		}
	}
}
