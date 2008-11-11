/*
 * DString.java
 * Copyright (c) 2005 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by walczak on Jan 17, 2006.  
 * Last revision $Revision: 6926 $ by:
 * $Author: braubach $ on $Date: 2008-09-28 22:16:58 +0200 (So, 28 Sep 2008) $.
 */
package nuggets.delegate2;

import java.math.BigInteger;

import nuggets.IAssembler;
import nuggets.ICruncher;
import nuggets.delegate.ADelegate;


/** DString 
 * @author walczak
 * @since  Jan 17, 2006
 */
public class DBigInteger extends ADelegate
{

   /** 
    * @param clazz
    * @param asm
    * @return a big integer 
    * @throws Exception
    */
   public Object getInstance(Class clazz, IAssembler asm) throws Exception
   {
	  String t = asm.nextToken();
      return new BigInteger(t);
   }

   /** 
    * @param o
    * @param mill
    * @see nuggets.delegate.ASimpleDelegate#persist(java.lang.Object, nuggets.ICruncher)
    */
   public void persist(Object o, ICruncher mill, ClassLoader classloader)
   {
		  mill.startConcept(o);
	  mill.addToken(o.toString());
   }
   
//   
//	/** 
//	 * @param clazz
//	 * @param value
//	 * @return the boolean expression
//	 * @see nuggets.delegate.ASimpleDelegate#unmarshall(java.lang.Class, java.lang.Object)
//	 */
//	public Object unmarshall(Class clazz, Object value)
//	{
//		return new BigInteger((String)value);
//	}

}