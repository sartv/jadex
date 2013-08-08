package jadex.bdiv3;

import jadex.bdiv3.asm.ClassNodeWrapper;
import jadex.bdiv3.asm.FieldNodeWrapper;
import jadex.bdiv3.asm.IClassNode;
import jadex.bdiv3.asm.IInsnList;
import jadex.bdiv3.asm.IMethodNode;
import jadex.bdiv3.asm.InsnListWrapper;
import jadex.bdiv3.asm.MethodNodeWrapper;
import jadex.bdiv3.asm.instructions.IAbstractInsnNode;
import jadex.bdiv3.asm.instructions.IFieldInsnNode;
import jadex.bdiv3.asm.instructions.ILabelNode;
import jadex.bdiv3.asm.instructions.ILdcInsnNode;
import jadex.bdiv3.asm.instructions.ILineNumberNode;
import jadex.bdiv3.asm.instructions.IMethodInsnNode;
import jadex.bdiv3.asm.instructions.LabelNodeWrapper;
import jadex.bdiv3.model.BDIModel;
import jadex.bdiv3.model.MBelief;
import jadex.commons.SReflect;
import jadex.commons.SUtil;

import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kohsuke.asm4.AnnotationVisitor;
import org.kohsuke.asm4.ClassReader;
import org.kohsuke.asm4.ClassVisitor;
import org.kohsuke.asm4.ClassWriter;
import org.kohsuke.asm4.FieldVisitor;
import org.kohsuke.asm4.Label;
import org.kohsuke.asm4.MethodVisitor;
import org.kohsuke.asm4.Opcodes;
import org.kohsuke.asm4.Type;
import org.kohsuke.asm4.tree.ClassNode;
import org.kohsuke.asm4.tree.FieldInsnNode;
import org.kohsuke.asm4.tree.FieldNode;
import org.kohsuke.asm4.tree.InsnList;
import org.kohsuke.asm4.tree.InsnNode;
import org.kohsuke.asm4.tree.LabelNode;
import org.kohsuke.asm4.tree.LdcInsnNode;
import org.kohsuke.asm4.tree.MethodInsnNode;
import org.kohsuke.asm4.tree.MethodNode;
import org.kohsuke.asm4.tree.TypeInsnNode;
import org.kohsuke.asm4.tree.VarInsnNode;
import org.kohsuke.asm4.util.ASMifier;
import org.kohsuke.asm4.util.TraceClassVisitor;


/**
 * 
 */
public class ASMBDIClassGenerator extends AbstractAsmBdiClassGenerator
{
    protected static Method methoddc1; 
    protected static Method methoddc2;

	static
	{
		try
		{
			AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
			{
				public Object run() throws Exception
				{
					Class<?> cl = Class.forName("java.lang.ClassLoader");
					methoddc1 = cl.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
					methoddc2 = cl.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class, ProtectionDomain.class});
					return null;
				}
			});
		}
		catch(PrivilegedActionException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 *  Generate class.
	 */
	public Class<?> generateBDIClass(final String clname, final BDIModel model, final ClassLoader cl)
	{
		return generateBDIClass(clname, model, cl, new HashSet<String>());
	}
	
	/**
	 *  Generate class.
	 */
	public Class<?> generateBDIClass(final String clname, final BDIModel model, 
		final ClassLoader cl, final Set<String> done)
	{
		Class<?> ret = null;
		
//		System.out.println("Generating with cl: "+cl+" "+clname);
		
		final List<String> todo = new ArrayList<String>();
		done.add(clname);
		
		try
		{
	//		String clname = cma.getName()+BDIModelLoader.FILE_EXTENSION_BDIV3_FIRST;
			
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			ClassNode cn = new ClassNode();
//			TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out))
//			TraceClassVisitor tcv = new TraceClassVisitor(cw, new ASMifier(), new PrintWriter(System.out));
//			CheckClassAdapter cc = new CheckClassAdapter(cw);
			
			final String iclname = clname.replace(".", "/");
			
			ClassVisitor cv = new ClassVisitor(Opcodes.ASM4, cn)
			{
				boolean isagentorcapa = false;
//				Set<String> fields = new HashSet<String>();
				
	//			public void visit(int version, int access, String name,
	//				String signature, String superName, String[] interfaces)
	//			{
	//				super.visit(version, access, name, null, superName, interfaces);
	//			}
				
//				public FieldVisitor visitField(int access, String name,
//						String desc, String signature, Object value)
//				{
//					fields.add(name);
//					return super.visitField(access, name, desc, signature, value);
//				}
				
			    public AnnotationVisitor visitAnnotation(String desc, boolean visible) 
			    {
			    	if(visible && (desc.indexOf("Ljadex/micro/annotation/Agent;")!=-1
			    		|| desc.indexOf("Ljadex/bdiv3/annotation/Capability;")!=-1))
			    	{
			    		isagentorcapa = true;
			    	}
			    	return super.visitAnnotation(desc, visible);
			    }

			    public MethodVisitor visitMethod(int access, final String methodname, String desc, String signature, String[] exceptions)
				{
//					if(clname.indexOf("PlanPrecondition")!=-1)
//						System.out.println(desc+" "+methodname);
					
					return new MethodVisitor(api, super.visitMethod(access, methodname, desc, signature, exceptions))
					{
						public void visitFieldInsn(int opcode, String owner, String name, String desc)
						{
							// if is a putfield and is belief and not is in init (__agent field is not available)
							if(ophelper.isPutField(opcode) && model.getCapability().hasBelief(name) 
								&& model.getCapability().getBelief(name).isFieldBelief()
								&& (isagentorcapa || !owner.equals(iclname))) // either is itself agent/capa or is not field of non-agent
							{
								// possibly transform basic value
								if(SReflect.isBasicType(SReflect.findClass0(Type.getType(desc).getClassName(), null, cl)))
									visitMethodInsn(Opcodes.INVOKESTATIC, "jadex/commons/SReflect", "wrapValue", "("+desc+")Ljava/lang/Object;");
								
								visitInsn(Opcodes.SWAP);
								
								// fetch bdi agent value from field

								// this pop aload is necessary in inner classes!
								if(isagentorcapa)
								{
									visitInsn(Opcodes.POP);
									visitVarInsn(Opcodes.ALOAD, 0);
									super.visitFieldInsn(Opcodes.GETFIELD, iclname, "__agent", Type.getDescriptor(BDIAgent.class));
								}
								else
								{
									visitInsn(Opcodes.POP);
									visitInsn(Opcodes.ACONST_NULL);
								}
								
								// add field name	
								visitLdcInsn(name);
								visitInsn(Opcodes.SWAP);
								// add this
								visitVarInsn(Opcodes.ALOAD, 0);
								visitInsn(Opcodes.SWAP);
								
								visitMethodInsn(Opcodes.INVOKESTATIC, "jadex/bdiv3/BDIAgent", "writeField", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;Ljadex/bdiv3/BDIAgent;)V");
							}
							else
							{
								super.visitFieldInsn(opcode, owner, name, desc);
							}
						}
						
//						public void visitInsn(int opcode)
//						{
//							if(Opcodes.AASTORE==opcode)
//							{
//								// on stack: arrayref, index, value 
//								visitVarInsn(Opcodes.ALOAD, 0);
//								super.visitFieldInsn(Opcodes.GETFIELD, iclname, "__agent", Type.getDescriptor(BDIAgent.class));
//
//								// invoke method
//								visitMethodInsn(Opcodes.INVOKESTATIC, "jadex/bdiv3/BDIAgent", "writeArrayField", "(Ljava/lang/Object;ILjava/lang/Object;Ljadex/bdiv3/BDIAgent;)V");
//							}
//							else
//							{
//								super.visitInsn(opcode);
//							}
////							super.visitInsn(opcode);
//						}
					};
				}
				
				public void visitInnerClass(String name, String outerName, String innerName, int access)
				{
//					System.out.println("vic: "+name+" "+outerName+" "+innerName+" "+access);
					String icln = name.replace("/", ".");
					if(!done.contains(icln))
						todo.add(icln);
					super.visitInnerClass(name, outerName, innerName, access);//Opcodes.ACC_PUBLIC); does not work
				}
				
				public void visitEnd()
				{
					if(isagentorcapa)
						visitField(Opcodes.ACC_PUBLIC, "__agent", Type.getDescriptor(BDIAgent.class), null, null);
					visitField(Opcodes.ACC_PUBLIC, "__globalname", Type.getDescriptor(String.class), null, null);
					super.visitEnd();
				}
			};
			
			InputStream is = null;
			try
			{
				String fname = clname.replace('.', '/') + ".class";
				is = SUtil.getResource(fname, cl);
				ClassReader cr = new ClassReader(is);

//				TraceClassVisitor tcv2 = new TraceClassVisitor(cv, new PrintWriter(System.out));
//				TraceClassVisitor tcv3 = new TraceClassVisitor(null, new PrintWriter(System.out));
//				cr.accept(tcv2, 0);
				cr.accept(cv, 0);
				transformClassNode(ClassNodeWrapper.wrap(cn), iclname, model);
				cn.accept(cw);
				byte[] data = cw.toByteArray();
				
//				CheckClassAdapter.verify(new ClassReader(data), false, new PrintWriter(System.out));
				
				// Find correct cloader for injecting the class.
				// Probes to load class without loading class.
				
				List<ClassLoader> pas = new LinkedList<ClassLoader>();
				ClassLoader tmp = cl;
				while(tmp!=null)
				{
					pas.add(0, tmp);
					tmp = tmp.getParent();
				}
				
				ClassLoader found = null;
				for(ClassLoader tmpcl: pas)
				{
					if(tmpcl.getResource(fname)!=null)
					{
						found = tmpcl;
						break;
					}
				}
				
//				System.out.println("toClass: "+clname+" "+found);
				ret = toClass(clname, data, found, null);
				
//				if(ret.getName().indexOf("$")!=-1)
//				{
//					try
//					{
//						Method m = ret.getMethod("__getLineNumber", new Class[0]);
//						Object o = m.invoke(null, new Object[0]);
//						System.out.println("Line is: "+ret.getName()+" "+o);
//					}
//					catch(Exception e)
//					{
//					}
//				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if(is!=null)
						is.close();
				}
				catch(Exception e)
				{
				}
			}
			
			for(String icl: todo)
			{
				generateBDIClass(icl, model, cl, done);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	protected void replaceNativeGetter(String iclname, IMethodNode mn, String belname)
	{
		Type	ret	= Type.getReturnType(mn.getDesc());

		mn.setAccess(mn.getAccess()-Opcodes.ACC_NATIVE);
		InsnList nl = new InsnList();
		nl.add(new VarInsnNode(Opcodes.ALOAD, 0));
		nl.add(new FieldInsnNode(Opcodes.GETFIELD, iclname, "__agent", "Ljadex/bdiv3/BDIAgent;"));
		nl.add(new VarInsnNode(Opcodes.ALOAD, 0));
		nl.add(new FieldInsnNode(Opcodes.GETFIELD, iclname, "__globalname", "Ljava/lang/String;"));
		nl.add(new LdcInsnNode(belname));
		
		if(ret.getClassName().equals("byte"))
		{
			nl.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jadex/bdiv3/BDIAgent", "getAbstractBeliefValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
			nl.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I"));
			nl.add(new InsnNode(Opcodes.I2B));
			nl.add(new InsnNode(Opcodes.IRETURN));
		}
		else if(ret.getClassName().equals("short"))
		{
			nl.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jadex/bdiv3/BDIAgent", "getAbstractBeliefValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
			nl.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I"));
			nl.add(new InsnNode(Opcodes.I2S));
			nl.add(new InsnNode(Opcodes.IRETURN));
		}
		else if(ret.getClassName().equals("int"))
		{
			nl.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jadex/bdiv3/BDIAgent", "getAbstractBeliefValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
			nl.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I"));
			nl.add(new InsnNode(Opcodes.IRETURN));
		}
		else if(ret.getClassName().equals("char"))
		{
			nl.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jadex/bdiv3/BDIAgent", "getAbstractBeliefValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
			nl.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Character"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C"));
			nl.add(new InsnNode(Opcodes.IRETURN));
		}
		else if(ret.getClassName().equals("boolean"))
		{
			nl.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jadex/bdiv3/BDIAgent", "getAbstractBeliefValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
			nl.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z"));
			nl.add(new InsnNode(Opcodes.IRETURN));
		}
		else if(ret.getClassName().equals("long"))
		{
			nl.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jadex/bdiv3/BDIAgent", "getAbstractBeliefValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
			nl.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J"));
			nl.add(new InsnNode(Opcodes.LRETURN));							
		}
		else if(ret.getClassName().equals("float"))
		{
			nl.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jadex/bdiv3/BDIAgent", "getAbstractBeliefValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
			nl.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F"));
			nl.add(new InsnNode(Opcodes.FRETURN));							
		}
		else if(ret.getClassName().equals("double"))
		{
			nl.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jadex/bdiv3/BDIAgent", "getAbstractBeliefValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
			nl.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Number"));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "doubleValue", "()D"));
			nl.add(new InsnNode(Opcodes.DRETURN));
		}
		else // Object
		{
			nl.add(new LdcInsnNode(ret));
			nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jadex/bdiv3/BDIAgent", "getAbstractBeliefValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
			nl.add(new TypeInsnNode(Opcodes.CHECKCAST, ret.getInternalName()));
			nl.add(new InsnNode(Opcodes.ARETURN));
		}
		
		mn.setInstructions(InsnListWrapper.wrap(nl));
	}
	
	/**
	 * 
	 */
	protected void replaceNativeSetter(String iclname, IMethodNode mn, String belname) 
	{
		Type	arg	= Type.getArgumentTypes(mn.getDesc())[0];

		mn.setAccess(mn.getAccess()-Opcodes.ACC_NATIVE);
		InsnList nl = new InsnList();
		nl.add(new VarInsnNode(Opcodes.ALOAD, 0));
		nl.add(new FieldInsnNode(Opcodes.GETFIELD, iclname, "__agent", "Ljadex/bdiv3/BDIAgent;"));
		nl.add(new VarInsnNode(Opcodes.ALOAD, 0));
		nl.add(new FieldInsnNode(Opcodes.GETFIELD, iclname, "__globalname", "Ljava/lang/String;"));
		nl.add(new LdcInsnNode(belname));
		
		if(arg.getClassName().equals("byte"))
		{
			nl.add(new VarInsnNode(Opcodes.ILOAD, 1));
			nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;"));
		}
		else if(arg.getClassName().equals("short"))
		{
			nl.add(new VarInsnNode(Opcodes.ILOAD, 1));
			nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;"));
		}
		else if(arg.getClassName().equals("int"))
		{
			nl.add(new VarInsnNode(Opcodes.ILOAD, 1));
			nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));

		}
		else if(arg.getClassName().equals("char"))
		{
			nl.add(new VarInsnNode(Opcodes.ILOAD, 1));
			nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;"));
		}
		else if(arg.getClassName().equals("boolean"))
		{
			nl.add(new VarInsnNode(Opcodes.ILOAD, 1));
			nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;"));
		}
		else if(arg.getClassName().equals("long"))
		{
			nl.add(new VarInsnNode(Opcodes.LLOAD, 1));
			nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;"));
		}
		else if(arg.getClassName().equals("float"))
		{
			nl.add(new VarInsnNode(Opcodes.FLOAD, 1));
			nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;"));
		}
		else if(arg.getClassName().equals("double"))
		{
			nl.add(new VarInsnNode(Opcodes.DLOAD, 1));
			nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;"));
		}
		else // Object
		{
			nl.add(new VarInsnNode(Opcodes.ALOAD, 1));
		}
		
		nl.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jadex/bdiv3/BDIAgent", "setAbstractBeliefValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"));
		nl.add(new InsnNode(Opcodes.RETURN));
		
		mn.setInstructions(InsnListWrapper.wrap(nl));
	}

	/**
	 * 
	 */
	protected void enhanceSetter(String iclname, IMethodNode mn, String belname)
	{
		System.out.println("method acc: "+mn.getName()+" "+mn.getAccess());
		
		IInsnList l = mn.getInstructions();
		
//		System.out.println("icl: "+iclname);
		
		InsnList nl = new InsnList();
		nl.add(new VarInsnNode(Opcodes.ALOAD, 0)); // loads the object
		nl.add(new FieldInsnNode(Opcodes.GETFIELD, iclname, "__agent", Type.getDescriptor(BDIAgent.class)));
		nl.add(new LdcInsnNode(belname));
		nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/bdiv3/BDIAgent", "unobserveValue", 
//			"(Ljava/lang/String;)V"));
			"(Ljadex/bdiv3/BDIAgent;Ljava/lang/String;)V"));
		l.insertBefore(l.getFirst(), InsnListWrapper.wrap(nl));
		
		nl = new InsnList();
		nl.add(new VarInsnNode(Opcodes.ALOAD, 1)); // loads the argument (=parameter0)
		nl.add(new VarInsnNode(Opcodes.ALOAD, 0)); // loads the object
		nl.add(new FieldInsnNode(Opcodes.GETFIELD, iclname, "__agent", Type.getDescriptor(BDIAgent.class)));
		nl.add(new LdcInsnNode(belname));
		nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/bdiv3/BDIAgent", "createEvent", 
			"(Ljava/lang/Object;Ljadex/bdiv3/BDIAgent;Ljava/lang/String;)V"));
//			nl.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/bdiv3/BDIAgent", "createEvent", 
//				"()V"));

		// Find return and insert call before that
		IAbstractInsnNode n;
		for(n = l.getLast(); n.getOpcode()!=Opcodes.RETURN; n = n.getPrevious())
		{
		}
		l.insertBefore(n, InsnListWrapper.wrap(nl));
	}

	/**
	 * 
	 */
	protected void transformConstructor(IClassNode cn, IMethodNode mn, BDIModel model, List<String> tododyn)
	{
		IInsnList l = mn.getInstructions();
		ILabelNode begin = null;
		int foundcon = -1;
		
		for(int i=0; i<l.size(); i++)
		{
			IAbstractInsnNode n = l.get(i);
			
			if(begin==null && n instanceof ILabelNode)
			{
				begin = (ILabelNode)n;
			}
			
			// find first constructor call
			if(Opcodes.INVOKESPECIAL==n.getOpcode() && foundcon==-1)
			{
				foundcon = i;
				begin = null;
			}
			else if(n instanceof IMethodInsnNode && ((IMethodInsnNode)n).getName().equals("writeField"))
			{
				IMethodInsnNode min = (IMethodInsnNode)n;
				
//				System.out.println("found writeField node: "+min.name+" "+min.getOpcode());
				IAbstractInsnNode start = min;
				String name = null;
				List<String> evs = new ArrayList<String>(); 
				while(!start.equals(begin))
				{
					// find method name via last constant load
					if(name==null && start instanceof ILdcInsnNode)
						name = (String)((ILdcInsnNode)start).getCst();
					if(start.getOpcode()==Opcodes.GETFIELD)
					{
						String bn = ((IFieldInsnNode)start).getName();
						if(model.getCapability().hasBelief(bn))
						{
							evs.add(bn);
						}
					}
					start = start.getPrevious();
				}
				
				if(tododyn.remove(name))
				{
					MBelief mbel = model.getCapability().getBelief(name);
					mbel.getEvents().addAll(evs);
					
					MethodNode mnode = new MethodNode(Opcodes.ACC_PUBLIC, IBDIClassGenerator.DYNAMIC_BELIEF_UPDATEMETHOD_PREFIX
						+SUtil.firstToUpperCase(name), Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
					IMethodNode wrappedMNode = MethodNodeWrapper.wrap(mnode);
					
					// First labels are cloned
					IAbstractInsnNode cur = start;
					Map<ILabelNode, ILabelNode> labels = new HashMap<ILabelNode, ILabelNode>();
					while(!cur.equals(min))
					{
						if(cur instanceof ILabelNode)
							labels.put((ILabelNode)cur, new LabelNodeWrapper(new LabelNode(new Label())));
						cur = cur.getNext();
					}
					// Then code is cloned
					cur = start;
					while(!cur.equals(min))
					{
						IAbstractInsnNode clone = cur.clone(labels);
						wrappedMNode.getInstructions().add(clone);
						cur = cur.getNext();
					}
					wrappedMNode.getInstructions().add(cur.clone(labels));
					wrappedMNode.visitInsn(Opcodes.RETURN);
					
					cn.addMethod(wrappedMNode);
				}
				
				begin = null;
			}
		}
		
		// Move init code to separate method for being called after injections. 
		if(foundcon!=-1 && foundcon+1<l.size())
		{
			String iclname = cn.getName(); // in ASM, this is without 'L' and ';'
			String name	= IBDIClassGenerator.INIT_EXPRESSIONS_METHOD_PREFIX+"_"+iclname.replace("/", "_").replace(".", "_");
//			System.out.println("Init method: "+name);
			MethodNode mnode = new MethodNode(Opcodes.ACC_PUBLIC, name, mn.getDesc(), (String)mn.getSignature(), null);
			IMethodNode wrappedMNode = MethodNodeWrapper.wrap(mnode);
			cn.addMethod(wrappedMNode);

			while(l.size()>foundcon+1)
			{
				IAbstractInsnNode	node	= l.get(foundcon+1);
				if(ophelper.isReturn(node.getOpcode()))
				{
					break;
				}
				l.remove(node);
				wrappedMNode.getInstructions().add(node);
			}						
			mnode.visitInsn(Opcodes.RETURN);
			
			// Add code to store arguments in field.
			Type[]	args	= Type.getArgumentTypes(mn.getDesc());
			InsnList	init	= new InsnList();

			// obj param
			init.add(new VarInsnNode(Opcodes.ALOAD, 0));
			
			// clazz param
			init.add(new LdcInsnNode(Type.getType("L"+iclname+";")));
			
			// argtypes param
			init.add(new LdcInsnNode(args.length));
			init.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Class"));
			for(int i=0; i<args.length; i++)
			{
				init.add(new InsnNode(Opcodes.DUP));
				init.add(new LdcInsnNode(i));
				init.add(new LdcInsnNode(args[i]));
				init.add(new InsnNode(Opcodes.AASTORE));
			}
			
			// args param
			init.add( new LdcInsnNode(args.length));
			init.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
			for(int i=0; i<args.length; i++)
			{
				init.add(new InsnNode(Opcodes.DUP));
				init.add(new LdcInsnNode(i));
				init.add(new VarInsnNode(Opcodes.ALOAD, i+1));	// 0==this, 1==arg0, ...
				init.add(new InsnNode(Opcodes.AASTORE));
			}
			
			// Invoke method.
			init.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/bdiv3/BDIAgent", "addInitArgs", "(Ljava/lang/Object;Ljava/lang/Class;[Ljava/lang/Class;[Ljava/lang/Object;)V"));
			
			l.insertBefore(l.get(foundcon+1), InsnListWrapper.wrap(init));
		}
	}

	/**
	 * 
	 */
	protected void transformArrayStores(IMethodNode mn, BDIModel model, String iclname)
	{
		IInsnList ins = mn.getInstructions();
		LabelNode lab = null;
		List<String> belnames = new ArrayList<String>();
		
		for(IAbstractInsnNode n: ins)
		{
			if(lab==null && n instanceof LabelNode)
			{
				lab = (LabelNode)n;
				belnames.clear();
			}
			
			if(n.getOpcode()==Opcodes.GETFIELD)
			{
				String bn = ((IFieldInsnNode)n).getName();
				if(model.getCapability().hasBelief(bn))
				{
					belnames.add(bn);
				}
			}
			
			if(!belnames.isEmpty())
			{
				InsnList newins = null;
				
				if(Opcodes.IASTORE==n.getOpcode() || Opcodes.BASTORE==n.getOpcode()) // for int, byte and boolean :-((	
				{
					newins = new InsnList();
					newins.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/commons/SReflect", "wrapValue", "(I)Ljava/lang/Object;"));
				}
				else if(Opcodes.LASTORE==n.getOpcode())
				{
					newins = new InsnList();
					newins.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/commons/SReflect", "wrapValue", "(J)Ljava/lang/Object;"));
				}
				else if(Opcodes.FASTORE==n.getOpcode())
				{
					newins = new InsnList();
					newins.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/commons/SReflect", "wrapValue", "(F)Ljava/lang/Object;"));
				}
				else if(Opcodes.DASTORE==n.getOpcode())
				{
					newins = new InsnList();
					newins.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/commons/SReflect", "wrapValue", "(D)Ljava/lang/Object;"));
				}
				else if(Opcodes.CASTORE==n.getOpcode())
				{
					newins = new InsnList();
					newins.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/commons/SReflect", "wrapValue", "(C)Ljava/lang/Object;"));
				}
				else if(Opcodes.SASTORE==n.getOpcode())
				{
					newins = new InsnList();
					newins.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/commons/SReflect", "wrapValue", "(S)Ljava/lang/Object;"));
				}
				else if(Opcodes.AASTORE==n.getOpcode())
				{
					newins = new InsnList();
				}
				
				if(newins!=null)
				{
//					// on stack: arrayref, index, value 
//					System.out.println("found: "+belnames);
					String belname = belnames.get(0);
					
					newins.add(new VarInsnNode(Opcodes.ALOAD, 0));
					newins.add(new FieldInsnNode(Opcodes.GETFIELD, iclname, "__agent", Type.getDescriptor(BDIAgent.class)));
					newins.add(new LdcInsnNode(belname));
					newins.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jadex/bdiv3/BDIAgent", "writeArrayField", 
						"(Ljava/lang/Object;ILjava/lang/Object;Ljadex/bdiv3/BDIAgent;Ljava/lang/String;)V"));
					
					ins.insert(n.getPrevious(), InsnListWrapper.wrap(newins));
					ins.remove(n); // remove old Xastore
					
					lab = null;
					belnames.clear();
				}
			}
		}
	}
	
	/**
	 * 
	 */
	public static Class<?> toClass(String name, byte[] data, ClassLoader loader, ProtectionDomain domain)
	{
		Class<?> ret = null;
		
		try
		{
			Method method;
			Object[] args;
			if(domain == null)
			{
				method = methoddc1;
				args = new Object[]{name, data, new Integer(0), new Integer(data.length)};
			}
			else
			{
				method = methoddc2;
				args = new Object[]{name, data, new Integer(0), new Integer(data.length), domain};
			}

			method.setAccessible(true);
			try
			{
				ret = (Class<?>)method.invoke(loader, args);
			}
			catch(InvocationTargetException e)
			{
				if(e.getTargetException() instanceof LinkageError)
				{
//					e.printStackTrace();
					
					// when same class was already loaded via other filename wrong cache miss:-(
//					ret = SReflect.findClass(name, null, loader);
					ret = Class.forName(name, true, loader);
				}
			}
			finally
			{
				method.setAccessible(false);
			}
		}
		catch(RuntimeException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	public static void main(String[] args) throws Exception
	{
//		System.out.println(int.class.getName());
		
//		Method m = SReflect.getMethods(BDIAgent.class, "writeArrayField")[0];
//		Method[] ms = SReflect.getMethods(SReflect.class, "wrapValue");
//		for(Method m: ms)
//		{
//			System.out.println(m.toString()+" "+Type.getMethodDescriptor(m));
//		}
				
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//		TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out));
		TraceClassVisitor tcv = new TraceClassVisitor(cw, new ASMifier(), new PrintWriter(System.out));
//		CheckClassAdapter cc = new CheckClassAdapter(tcv);
		
//		final String classname = "lars/Lars";
//		final String supername = "jadex/bdiv3/MyTestClass";
		
//		final ASMifier asm = new ASMifier();
		
		ClassVisitor cv = new ClassVisitor(Opcodes.ASM4, tcv)
		{
//			public void visit(int version, int access, String name,
//				String signature, String superName, String[] interfaces)
//			{
//				super.visit(version, access, classname, null, superName, interfaces);
//			}
		
			public MethodVisitor visitMethod(int access, final String methodname, String desc, String signature, String[] exceptions)
			{
				return new MethodVisitor(api, super.visitMethod(access, methodname, desc, signature, exceptions))
				{
//					public void visitFieldInsn(int opcode, String owner, String name, String desc)
//					{
//						super.visitFieldInsn(opcode, owner, methodname, desc);
//					}
					public void visitInsn(int opcode)
					{
						super.visitInsn(opcode);
					}
				};
				
//				return super.visitMethod(access, methodname, desc, signature, exceptions);
//				
//				System.out.println("visit method: "+methodname);
				
//				if("<init>".equals(methodname))
//				{
//					return new TraceMethodVisitor(super.visitMethod(access, methodname, desc, signature, exceptions), asm);
//				}
//				else
//				{
//					return super.visitMethod(access, methodname, desc, signature, exceptions);
//				}
			}
		};
		
		ClassReader cr = new ClassReader("jadex.bdiv3.MyTestClass");
		cr.accept(cv, 0);
//		ClassNode cn = new ClassNode();
//		cr.accept(cn, 0);
//		
//		String prefix = "__update";
//		Set<String> todo = new HashSet<String>();
//		todo.add("testfield");
//		todo.add("testfield2");
//		todo.add("testfield3");
//		
//		MethodNode[] mths = cn.methods.toArray(new MethodNode[0]);
//		for(MethodNode mn: mths)
//		{
//			System.out.println(mn.name);
//			if(mn.name.equals("<init>"))
//			{
//				InsnList l = cn.methods.get(0).instructions;
//				for(int i=0; i<l.size() && !todo.isEmpty(); i++)
//				{
//					AbstractInsnNode n = l.get(i);
//					if(n instanceof LabelNode)
//					{
//						LabelNode ln = (LabelNode)n;
//						System.out.println(ln.getLabel());
//					}
//					else if(n instanceof FieldInsnNode)
//					{
//						FieldInsnNode fn = (FieldInsnNode)n;
//						
//						if(Opcodes.PUTFIELD==fn.getOpcode() && todo.contains(fn.name))
//						{
//							todo.remove(fn.name);
//							System.out.println("found putfield node: "+fn.name+" "+fn.getOpcode());
//							AbstractInsnNode start = fn;
//							while(!(start instanceof LabelNode))
//							{
//								start = start.getPrevious();
//							}
//
//							MethodNode mnode = new MethodNode(mn.access, prefix+SUtil.firstToUpperCase(fn.name), mn.desc, mn.signature, null);
//							
//							Map<LabelNode, LabelNode> labels = new HashMap<LabelNode, LabelNode>();
//							while(!start.equals(fn))
//							{
//								AbstractInsnNode clone;
//								if(start instanceof LabelNode)
//								{
//									clone = new LabelNode(new Label());
//									labels.put((LabelNode)start, (LabelNode)clone);
//								}
//								else
//								{
//									clone = start.clone(labels);
//								}
//								mnode.instructions.add(clone);
//								start = start.getNext();
//							}
//							mnode.instructions.add(start.clone(labels));
//							mnode.visitInsn(Opcodes.RETURN);
//							
//							cn.methods.add(mnode);
//						}
//					}
//					else
//					{
//						System.out.println(n);
//					}
//				}
//			}
//		}
		
//		cn.name = classname;
		
//		System.out.println("cn: "+cn);
		
//		System.out.println(asm.getText());
		
//		ClassWriter cw = new ClassWriter(0);
//		cw.accept(tcv);
//		byte[] data = cw.toByteArray();
		
//		ByteClassLoader bcl = new ByteClassLoader(ASMBDIClassGenerator.class.getClassLoader());
		
//		Class<?> cl = toClass("jadex.bdiv3.MyTestClass", data, new URLClassLoader(new URL[0], ASMBDIClassGenerator.class.getClassLoader()), null);
////		Class<?> cl = bcl.loadClass("lars.Lars", cw.toByteArray(), true);
//		Object o = cl.newInstance();
////		System.out.println("o: "+o);
////		Object v = cl.getMethod("getVal", new Class[0]).invoke(o, new Object[0]);
//		String mn = prefix+SUtil.firstToUpperCase("testfield");
//		Object v = cl.getMethod(mn, new Class[0]).invoke(o, new Object[0]);
//		cl.getMethod(mn, new Class[0]).invoke(o, new Object[0]);
//		cl.getMethod(mn, new Class[0]).invoke(o, new Object[0]);
//		System.out.println("res: "+cl.getDeclaredField("testfield").get(o));
		
//		System.out.println(SUtil.arrayToString(cl.getDeclaredMethods()));
		
//		System.out.println(cl);
//		Constructor<?> c = cl.getConstructor(new Class[0]);
//		c.setAccessible(true);
//		c.newInstance(new Object[0]);
//		Method m = cl.getMethod("main", new Class[]{String[].class});
//		m.setAccessible(true);
//		m.invoke(null, new Object[]{new String[0]});
	}
	
	

}
