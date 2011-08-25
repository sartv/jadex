package jadex.simulation.analysis.application.standalone;

import jadex.bridge.service.annotation.GuiClass;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.NameValue;
import jadex.micro.annotation.Properties;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.simulation.analysis.common.defaultViews.controlComponent.ComponentServiceViewerPanel;
import jadex.simulation.analysis.service.dataBased.engineering.IAEngineerDataobjectService;

/**
 *  Agent just offering the dataObject engineering service..
 */
@Description("Agent just offering the dataObject engineering service.")
@ProvidedServices({@ProvidedService(type=IAEngineerDataobjectService.class, implementation=@Implementation(expression="new jadex.simulation.analysis.application.standalone.ADatenobjekteErstellenService($component.getExternalAccess())"))})
@GuiClass(ComponentServiceViewerPanel.class)
@Properties(
{
	@NameValue(name="viewerpanel.componentviewerclass", value="\"jadex.simulation.analysis.common.defaultViews.controlComponent.ControlComponentViewerPanel\"")
})
public class ADatenobjekteErstellenAgent extends MicroAgent
{	

}
