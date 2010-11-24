package jadex.micro.examples.mandelbrot;

import jadex.commons.IFuture;

/**
 *  Service for generating a specific area.
 */
public interface IGenerateService
{
	/**
	 *  Generate a specific area using a defined x and y size.
	 */
	public IFuture generateArea(AreaData data);
}
