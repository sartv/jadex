package jadex.simulation.analysis.common.superClasses.events.service;

import jadex.simulation.analysis.common.superClasses.events.AEvent;
import jadex.simulation.analysis.common.superClasses.service.analysis.IAnalysisService;
import jadex.simulation.analysis.common.util.AConstants;

/**
 * A event, which occur in a service
 * @author 5Haubeck
 *
 */
public class AServiceEvent extends AEvent
{
protected Object newValue; 
	
	@Override
	public String getEventType()
	{
		return AConstants.SERVICE_EVENT;
	}
    
    public AServiceEvent(IAnalysisService source, String eventCommand, Object newValue)
    {
    	super(source, eventCommand);
    	this.newValue = newValue;
    }
    
    /**
     * Gets the new value in the service
     * @return a object
     */
    public Object getValue()
	{
		return newValue;
	}
}
