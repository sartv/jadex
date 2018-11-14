package jadex.platform.service.registryv2;

import jadex.micro.annotation.Agent;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;

/**
 *  Dummy agent for test service.
 */
@Agent
@ProvidedServices(@ProvidedService(type=ITestService.class, scope=RequiredService.SCOPE_NETWORK))
public class NetworkProviderAgent	implements ITestService
{
//	@AgentBody
//	void body(IInternalAccess agent)
//	{
//		while(true)
//		{
//			System.out.println(agent+" alive");
//			agent.waitForDelay(1000).get();
//		}
//	}
}
