package jadex.bdi.examples.shop;

import jadex.base.fipa.IDF;
import jadex.base.fipa.IDFComponentDescription;
import jadex.base.fipa.IDFServiceDescription;
import jadex.bdi.runtime.Plan;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentManagementService;
import jadex.bridge.IExternalAccess;
import jadex.commons.IFuture;
import jadex.service.SServiceProvider;

/**
 * 
 */
public class BuyItemPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		IDF df = (IDF)SServiceProvider.getService(getScope().getServiceProvider(), IDF.class).get(this);
		IDFServiceDescription sd = df.createDFServiceDescription(null, "shop", null);
		IDFComponentDescription cd = df.createDFComponentDescription(null, sd);
		IDFComponentDescription[] ret = (IDFComponentDescription[])df.search(cd, null).get(this);
		
		if(ret.length>0)
		{
			IComponentManagementService cms = (IComponentManagementService)SServiceProvider
				.getServiceUpwards(getScope().getServiceProvider(), IComponentManagementService.class).get(this);
			IComponentIdentifier cid = ret[0].getName();
			IExternalAccess exa = (IExternalAccess)cms.getExternalAccess(cid).get(this);
			IShop shop = (IShop)SServiceProvider.getService(exa.getServiceProvider(), IShop.class).get(this);
			String name	= (String)getParameter("name").getValue();
			System.out.println(getComponentName()+" buying item: "+name);
			IFuture	future	= shop.buyItem(name);
			System.out.println(getComponentName()+" getting item: "+future);
			Object item = future.get(this);
			System.out.println(getComponentName()+" bought item: "+item);
			getParameter("result").setValue(item);
		}
		else
		{
			System.out.println("No seller found.");
		}
	}
}
