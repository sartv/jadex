package jadex.bdiv3;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jadex.bdiv3.model.BDIModel;
import jadex.commons.IFilter;
import jadex.commons.SClassReader.AnnotationInfo;
import jadex.commons.SClassReader.ClassFileInfo;
import jadex.commons.SReflect;
import jadex.commons.SUtil;
import jadex.micro.annotation.Agent;


/**
 *  This class generator keeps generated byte-code as byte[] to be post-processed
 *  by other classes. Currently, this is used by compile-time BDI-enhancing with
 *  maven/gradle-plugins (for android).
 */
public class ByteKeepingASMBDIClassGenerator extends ASMBDIClassGenerator
{
	/** Map containing all bytes of all classes ever computed **/
	private Map<String, byte[]>	classbytes;

	/** Map containing all classes computed since last clear call **/
	private Map<String, byte[]>	recentclassbytes;

	/**
	 * Constructor.
	 */
	public ByteKeepingASMBDIClassGenerator()
	{
		classbytes = new HashMap<String, byte[]>();
		recentclassbytes = new HashMap<String, byte[]>();
	}

	@Override
	public Class<?> toClass(String name, byte[] data, ClassLoader loader, ProtectionDomain domain)
	{
		Class<?> result = null;
		if(!classbytes.containsKey(name))
		{
			// return null if this class has already been enhanced before to avoid duplicates.
			result = super.toClass(name, data, loader, domain); // maybe this isn't needed? just return null?
			
			if(result != null)
			{
				classbytes.put(name, data);
				recentclassbytes.put(name, data);
			}
		}
		return result;
	}

	/**
	 * Get the recently generated classes as byte array.
	 * @return recently generated classes
	 */
	public Map<String, byte[]> getRecentClassBytes()
	{
		return recentclassbytes;
	}

	/**
	 * Clear the list of recently generated classes.
	 */
	public void clearRecentClassBytes()
	{
		recentclassbytes.clear();
	}
}
