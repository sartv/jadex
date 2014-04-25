package jadex.bpmn.tools.ui;

import jadex.bpmn.editor.gui.BpmnGraph;
import jadex.bpmn.editor.gui.BpmnGraphComponent;
import jadex.bpmn.editor.gui.GuiConstants;
import jadex.bpmn.editor.gui.ModelContainer;
import jadex.bpmn.editor.gui.ZoomSlider;
import jadex.bpmn.editor.model.visual.BpmnVisualModelReader;
import jadex.bpmn.editor.model.visual.VActivity;
import jadex.bpmn.editor.model.visual.VElement;
import jadex.bpmn.editor.model.visual.VExternalSubProcess;
import jadex.bpmn.editor.model.visual.VSubProcess;
import jadex.bpmn.model.MActivity;
import jadex.bpmn.model.MBpmnModel;
import jadex.bpmn.model.io.SBpmnModelReader;
import jadex.bpmn.runtime.BpmnInterpreter;
import jadex.bpmn.tools.ProcessThreadInfo;
import jadex.bridge.BulkMonitoringEvent;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.monitoring.IMonitoringEvent;
import jadex.bridge.service.types.monitoring.IMonitoringService.PublishEventLevel;
import jadex.commons.IBreakpointPanel;
import jadex.commons.IFilter;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.FutureTerminatedException;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.commons.gui.JSplitPanel;
import jadex.commons.gui.future.SwingDefaultResultListener;
import jadex.commons.gui.future.SwingIntermediateResultListener;
import jadex.commons.gui.jtable.ResizeableTableHeader;
import jadex.commons.gui.jtable.TableSorter;
import jadex.commons.transformation.annotations.Classname;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.handler.mxConnectionHandler;

/**
 * 
 */
public class VisualProcessViewPanel extends JPanel
{
	//------- attributes --------
	
	/** The process. */
	protected IExternalAccess access;
		
	/** The list model for the activations. */
	protected ProcessThreadModel ptmodel;

	/** The list model for the history. */
	protected HistoryModel	hmodel;
	
	/** The list for the activations. */
	protected JTable threads;
	
	/** The list for the history. */
	protected JTable history;
	
	/** The breakpoint panel. */
	protected IBreakpointPanel	bpp;

	/** The model container. */
	protected ModelContainer modelcontainer;
	
	/** The change listener. */
	protected ISubscriptionIntermediateFuture<IMonitoringEvent> sub;
	
	//------- constructors --------
	
	/**
	 *  Create an agenda panel.
	 */
	public VisualProcessViewPanel(final IExternalAccess access, IBreakpointPanel bpp) 
	{
		try
		{
			this.access = access;
			this.bpp	= bpp;
			
			this.ptmodel = new ProcessThreadModel();
			this.hmodel	= new HistoryModel();
			this.modelcontainer = new ModelContainer(null);
			
			BpmnStylesheetSelections sheet = new BpmnStylesheetSelections();
			final BpmnGraph graph = new BpmnGraph(modelcontainer, sheet);
			graph.setCellsMovable(false);
			graph.setCellsResizable(false);
			graph.setCellsLocked(true);
			final BpmnVisualModelReader vreader = new BpmnVisualModelReader(graph)
			{
				public VActivity createActivity() 
				{
					return new VActivity(graph)
					{
						public String getStyle()
						{
							return getStyleHelper(super.getStyle(), getBpmnElement().getId());
						}
					};
				}
				
				public VSubProcess createSuboprocess()
				{
					return new VSubProcess(graph)
					{
						public String getStyle()
						{
							return getStyleHelper(super.getStyle(), getBpmnElement().getId());
						}
					};
				}
				
				public VExternalSubProcess createExternalSuboprocess()
				{
					return new VExternalSubProcess(graph)
					{
						public String getStyle()
						{
							return getStyleHelper(super.getStyle(), getBpmnElement().getId());
						}
					};
				}
				
				protected String getStyleHelper(String ret, String myid)
				{
					boolean w = false;
					boolean r = false;
					for(ProcessThreadInfo pti: ptmodel.getThreadInfos())
					{
						if(myid.equals(pti.getActId()))
						{
							if(pti.isWaiting())
							{
								w = true;
							}
							else
							{
								r = true;
								break;
							}
						}
					}
					
					if(r)
					{
						ret += "_ready";
					}
					else if(w)
					{
						ret += "_waiting";
					}
					return ret;
				}
			};

			modelcontainer.setGraph(graph);

			BpmnGraphComponent bpmncomp = new BpmnGraphComponent(graph)
			{
				// Do not allow connection drawing
				protected mxConnectionHandler createConnectionHandler()
				{
					return null;
				}
			};
			JPanel bpmnpan = new JPanel(new BorderLayout());
			bpmnpan.add(bpmncomp, BorderLayout.CENTER);
			
			ZoomSlider zs = new ZoomSlider(bpmncomp, modelcontainer);
//			zs.setMinimumSize(zs.getPreferredSize());
//			zs.setPreferredSize(zs.getPreferredSize());
			zs.setMaximumSize(zs.getPreferredSize());
			JPanel tp = new JPanel();
//			tp.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			BoxLayout bl = new BoxLayout(tp, BoxLayout.LINE_AXIS);
			tp.setLayout(bl);
			tp.add(new JPanel(new GridLayout(1,1))); // necessary to show zoomslider correctly
			tp.add(zs);
			bpmnpan.add(tp, BorderLayout.SOUTH);
			graph.getView().setScale(GuiConstants.DEFAULT_ZOOM);
			
			final ListSelectionListener sellistener = new ListSelectionListener()
		    {
		        public void valueChanged(ListSelectionEvent e)
		        {
		        	int vrow = threads.getSelectedRow();
		        	if(vrow!=-1)
		        	{
		        		int row = threads.convertRowIndexToModel(vrow);
		        		ProcessThreadInfo pti = (ProcessThreadInfo)threads.getModel().getValueAt(row, -1);
		        		mxICell cell = (mxICell)graph.getModel().getRoot();
		        		VElement elem = getVElement(cell, pti.getActId());
		        		if(elem!=null)
		        		{
		        			graph.setEventsEnabled(false);
		        			graph.setSelectionCell(elem);
		        			graph.setEventsEnabled(true);
		        		}
		        	}
		        }
		        
		        /**
		         *  Find the velement of the graph that fits to the bpmn id.
		         *  @param cell The start cell.
		         *  @param actid The activity id.
		         *  @return The element.
		         */
		        protected VElement getVElement(mxICell cell, String actid)
		        {
		        	VElement ret = null;
		        	if(cell instanceof VElement)
	        		{
	        			VElement ve = (VElement)cell;
	        			if(ve.getBpmnElement()!=null && ve.getBpmnElement().getId().equals(actid))
	        			{
	        				ret = ve;
	        			}
	        		}
		        	
		        	if(ret==null)
		        	{
		        		for(int i=0; i<cell.getChildCount() && ret==null; i++)
		        		{
		        			ret = getVElement(cell.getChildAt(i), actid);
		        		}
		        	}
		        	
		        	return ret;
		        }
		    };
			
			bpmncomp.init(modelcontainer);
			
			// Not possible to use the selection listener because if selected a click on the selected element is not detected
//			modelcontainer.getGraph().getSelectionModel().addListener(mxEvent.CHANGE, new mxIEventListener()
//			{
//				public void invoke(Object sender, mxEventObject evt)
//				{
//				}
//			});
			
			modelcontainer.getGraphComponent().getGraphControl().addMouseListener(new MouseAdapter() 
			{
				public void mouseClicked(MouseEvent e) 
				{
//					System.out.println("clicked: "+e);
					Object cell = modelcontainer.getGraphComponent().getCellAt(e.getX(), e.getY());
					if(cell instanceof VElement)
					{
						VElement elem = (VElement)cell;
//						System.out.println("Cell: "+ve.getBpmnElement()); 
						
						if(elem!=null)
						{
							String id = elem.getBpmnElement().getId();
							boolean set = false;
							List<Integer> sels = new ArrayList<Integer>();
							for(int row=0; row<threads.getModel().getRowCount() && !set; row++)
							{
								ProcessThreadInfo pti = (ProcessThreadInfo)threads.getModel().getValueAt(row, -1);
								if(pti.getActId().equals(id))
								{
									sels.add(Integer.valueOf(row));
								}
							}
							
							if(sels.size()==1)
							{
								int sel = sels.get(0).intValue();
//								System.out.println("sel0: "+sel);
								threads.getSelectionModel().removeListSelectionListener(sellistener);
								threads.setRowSelectionInterval(sel, sel);
								threads.getSelectionModel().addListSelectionListener(sellistener);
							}
							else if(sels.size()>1)
							{
								int sel = -1;
								int curs = getSelectedThreadRow();
								for(int i=0; i<sels.size() && sel==-1; i++)
								{
									int nexts = sels.get(i).intValue();
									if(nexts==curs || curs==-1)
									{
										if(i+1<sels.size())
										{
											sel = sels.get(i+1);
										}
										else
										{
											sel = sels.get(i-1);
										}
									}
								}
								threads.getSelectionModel().removeListSelectionListener(sellistener);
								threads.setRowSelectionInterval(sel, sel);
								threads.getSelectionModel().addListSelectionListener(sellistener);
							}
							else
							{
								threads.getSelectionModel().removeListSelectionListener(sellistener);
								threads.clearSelection();
								threads.getSelectionModel().addListSelectionListener(sellistener);
							}
							
							// Step when double click on thread
							if(sels.size()>0 && e.getClickCount()==2 && getStepInfo()!=null)
							{
								doStep();
							}
							
							// Double click on element toggles breakpoint
							if(sels.size()==0 && e.getClickCount()==2 && elem.getBpmnElement() instanceof MActivity)
							{
								toggleBreakPoint(((MActivity)elem.getBpmnElement()).getBreakpointId());
							}
						}
					}
				}
			});
			
			modelcontainer.getGraphComponent().refresh();
			
			TableSorter sorter = new TableSorter(ptmodel);
			sorter.setSortingStatus(0, TableSorter.ASCENDING);
			this.threads = new JTable(sorter);
			ResizeableTableHeader header = new ResizeableTableHeader(threads.getColumnModel());
			header.setIncludeHeaderWidth(true);
	//		threads.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			threads.setTableHeader(header);
			threads.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			sorter.setTableHeader(header);
//			threads.getColumnModel().setColumnMargin(10);
		    threads.getSelectionModel().addListSelectionListener(sellistener);
	
			sorter = new TableSorter(hmodel);
			this.history = new JTable(sorter);
			header = new ResizeableTableHeader(history.getColumnModel());
			header.setIncludeHeaderWidth(true);
	//		history.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			history.setTableHeader(header);
			history.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			sorter.setTableHeader(header);
//			history.getColumnModel().setColumnMargin(10);
			
			final JCheckBox hon = new JCheckBox("Store History");
			hon.setSelected(true);
				
			// todo: initial/current state
			
			sub = access.subscribeToEvents(new IFilter<IMonitoringEvent>()
			{
				@Classname("eventfilter")
				public boolean filter(IMonitoringEvent ev)
				{
					return true;	
				}
			}, true, PublishEventLevel.FINE);
			sub.addResultListener(new SwingIntermediateResultListener<IMonitoringEvent>(new IntermediateDefaultResultListener<IMonitoringEvent>()
			{
				public void intermediateResultAvailable(IMonitoringEvent event)
				{
					if(event==null)
						return;
					
					// todo: hide decomposing bulk events
					if(event instanceof BulkMonitoringEvent)
					{
						BulkMonitoringEvent bev = (BulkMonitoringEvent)event;
						if(bev.getBulkEvents().length>0)
						{
							IMonitoringEvent[] events = bev.getBulkEvents();
							for(int i=0; i<events.length; i++)
							{
								intermediateResultAvailable(events[i]);
							}
						}
					}
					else if(event.getType().endsWith(BpmnInterpreter.TYPE_THREAD))
					{
						ProcessThreadInfo pti = (ProcessThreadInfo)event.getProperty("details");
						if(event.getType().startsWith(IMonitoringEvent.EVENT_TYPE_CREATION))
						{
//							System.out.println("created thread: "+pti);
							ptmodel.addValue(pti);
						}
						else if(event.getType().startsWith(IMonitoringEvent.EVENT_TYPE_DISPOSAL))
						{
//							System.out.println("removed thread: "+pti);
							ptmodel.removeValue(pti);
//							threadinfos.remove(pti);
						}
						else if(event.getType().startsWith(IMonitoringEvent.EVENT_TYPE_MODIFICATION))
						{
//							System.out.println("changed thread: "+pti);
							ptmodel.updateValue(pti);
						}
					}
					else if(event.getType().endsWith(BpmnInterpreter.TYPE_ACTIVITY))
					{
						if(event.getType().startsWith(IMonitoringEvent.EVENT_TYPE_DISPOSAL))
						{
							hmodel.addValue(0, (ProcessThreadInfo)event.getProperty("details"));
//							historyinfos.add(0, (ProcessThreadInfo)event.getProperty("details"));
						}
					}
	//				System.out.println("ti: "+threadinfos.size()+" "+cce.getSourceName()+" "+cce.getSourceType()+" "+cce.getEventType());
					updateViews();
				}
				
				public void exceptionOccurred(Exception exception)
				{
					// Todo: why future terminated exception thrown?
					if(!(exception instanceof FutureTerminatedException))
					{
						super.exceptionOccurred(exception);
					}
				}
			}));
							
			JButton clear = new JButton("Clear");
			clear.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					hmodel.clear();
//					historyinfos.clear();
//					history.repaint();
				}
			});
			
			JPanel	procp	= new JPanel(new BorderLayout());
			procp.add(new JScrollPane(threads));
			procp.setBorder(BorderFactory.createTitledBorder("Processes"));
			
			JPanel	historyp	= new JPanel(new BorderLayout());
			historyp.add(new JScrollPane(history));
			historyp.setBorder(BorderFactory.createTitledBorder("History"));
			
			JPanel buts = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buts.add(hon);
			buts.add(clear);
			historyp.add(buts, BorderLayout.SOUTH);

			JSplitPanel tmp2 = new JSplitPanel(JSplitPane.VERTICAL_SPLIT);
			tmp2.add(bpmnpan);
			tmp2.add(procp);
			tmp2.setDividerLocation(0.7);
			
//			JSplitPane tmp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//			tmp.add(procp);
//			tmp.add(tmp2);
//			tmp.setDividerLocation(200); // Hack?!
			
			setLayout(new BorderLayout());
			add(tmp2, BorderLayout.CENTER);
			
			// Asynchronously load the visual model (maybe from remote).
			access.scheduleImmediate(new IComponentStep<String>()
			{
				@Classname("loadModel")
				public IFuture<String> execute(IInternalAccess ia)
				{
					Future<String>	ret	= new Future<String>();
					try
					{
						File f	= new File(ia.getModel().getFilename());
						String	content	= new Scanner(f, "UTF-8").useDelimiter("\\Z").next();
						ret.setResult(content);
					}
					catch(FileNotFoundException fnfe)
					{
						ret.setException(fnfe);
					}
					return ret;
				}
			})
				.addResultListener(new SwingDefaultResultListener<String>(this)
			{
				public void customResultAvailable(String content)
				{
					try
					{
						graph.deactivate();
						graph.setEventsEnabled(false);
						graph.getModel().beginUpdate();
						MBpmnModel mmodel = SBpmnModelReader.readModel(new ByteArrayInputStream(content.getBytes("UTF-8")), access.getModel().getFilename(), vreader);
						graph.getModel().endUpdate();
						graph.setEventsEnabled(true);
						graph.activate();
						modelcontainer.setBpmnModel(mmodel);
//						modelcontainer.setFile(new File(filename));	// file not available locally?
						
						updateViews();
					}
					catch(Exception e)
					{
						customExceptionOccurred(e);
					}
				}
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	//-------- methods --------
	
	/**
	 *  Dispose the panel and remove any listeners.
	 */
	public void	dispose()
	{
		sub.terminate();
	}
	
	//-------- helper methods --------
	
	/**
	 *  Update views.
	 */
	protected void	updateViews()
	{
//		ProcessThreadInfo sel = null;
//		int row = getSelectedThredRow();
//		if(row!=-1)
//		{
//			sel = (ProcessThreadInfo)threads.getModel().getValueAt(row, -1);
//		}
//		
//		ptmodel.fireTableDataChanged();
//		hmodel.fireTableDataChanged();
//		threads.repaint();
//		history.repaint();
//		
//		if(sel!=null)
//		{
//			for(row=0; row<threads.getModel().getRowCount(); row++)
//			{
//				ProcessThreadInfo pti = (ProcessThreadInfo)threads.getModel().getValueAt(row, -1);
//				if(sel.equals(pti))
//				{
//					threads.setRowSelectionInterval(row, row);
//					break;
//				}
//			}
//		}
		
		if(bpp!=null)
		{
			List<String> sel_bps = new ArrayList<String>();
			for(Iterator<ProcessThreadInfo> it=ptmodel.getThreadInfos().iterator(); it.hasNext(); )
			{
				ProcessThreadInfo info = it.next();
				if(info.getActivity()!=null)
				{
					sel_bps.add(info.getActivity());
				}
			}
			bpp.setSelectedBreakpoints((String[])sel_bps.toArray(new String[sel_bps.size()]));
		}
		
		modelcontainer.getGraph().getView().invalidate();
		modelcontainer.getGraph().getView().clear(modelcontainer.getGraph().getModel().getRoot(), true, true);
		modelcontainer.getGraph().getView().validate();
		modelcontainer.getGraphComponent().refresh();
	}
	
	//-------- helper classes --------
	
	/**
	 *  List model for activations.
	 */
	protected class ProcessThreadModel extends AbstractTableModel
	{
		protected String[] colnames = new String[]{"Process-Id", "Parent-Id", "Activity", "Pool", "Lane", "Exception", "Data", "Status"};
		
		/** The displayed process threads. */
		protected List<ProcessThreadInfo> threadinfos;
		
		public ProcessThreadModel()
		{
			this.threadinfos	= new ArrayList<ProcessThreadInfo>();
		}
		
		public String getColumnName(int column)
		{
			return colnames[column];
		}

		public int getColumnCount()
		{
			return colnames.length;
		}
		
		public int getRowCount()
		{
			return threadinfos.size();
		}
		
		public List<ProcessThreadInfo> getThreadInfos()
		{
			return threadinfos;
		}
		
		public void addValue(ProcessThreadInfo pti)
		{
			int idx = threadinfos.indexOf(pti);
			if(idx!=-1)
			{
				threadinfos.set(idx, pti);
				fireTableRowsUpdated(idx, idx);
			}
			else
			{
				threadinfos.add(pti);
				fireTableRowsInserted(threadinfos.size()-1, threadinfos.size()-1);
			}
			
		}
		
		public void removeValue(ProcessThreadInfo pti)
		{
			int idx = threadinfos.indexOf(pti);
			if(idx!=-1)
			{
//				System.out.println("Removed: "+pti);
				threadinfos.remove(idx);
				fireTableRowsDeleted(idx, idx);
			}
//			else
//			{
//				System.out.println("Cannot remove: "+pti);
//			}
		}
		
		public void updateValue(ProcessThreadInfo pti)
		{
			int idx = threadinfos.indexOf(pti);
			if(idx!=-1)
			{
//				System.out.println("Updated: "+pti);
				threadinfos.remove(idx);
				threadinfos.add(idx, pti);
				fireTableRowsUpdated(idx, idx);
			}
//			else
//			{
//				System.out.println("Cannot update: "+pti);
//			}
		}
		
		public Object getValueAt(int row, int column)
		{
			Object ret = null;
			ProcessThreadInfo info = (ProcessThreadInfo)threadinfos.toArray()[row];
			
			if(column==0)
			{
				ret = info.getThreadId();
			}
			else if(column==1)
			{
				ret = info.getParentId();
			}
			else if(column==2)
			{
				ret = info.getActivity();
			}
			else if(column==3)
			{
				ret = info.getPool(); 
			}
			else if(column==4)
			{
				ret = info.getLane(); 
			}
			else if(column==5)
			{
				ret = info.getException();
			}
			else if(column==6)
			{
				ret = info.getData();
			}
			else if(column==7)
			{
				ret = info.isWaiting() ? "waiting" : "ready";
			}
			else
			{
				ret = info;
			}
			return ret;
		}
	}
	
	/**
	 *  List model for history.
	 */
	protected class HistoryModel extends AbstractTableModel
	{
		protected String[] colnames = new String[]{"Process-Id", "Activity", "Pool", "Lane"};
		
		/** The previous process thread steps. */
		protected List<ProcessThreadInfo> historyinfos;
		
		public HistoryModel()
		{
			this.historyinfos = new ArrayList<ProcessThreadInfo>();
		}
		
		public String getColumnName(int column)
		{
			return colnames[column];
		}

		public int getColumnCount()
		{
			return colnames.length;
		}
		
		public int getRowCount()
		{
			return historyinfos.size();
		}
		
		public Object getValueAt(int row, int column)
		{
			Object ret = null;
			ProcessThreadInfo	info	= (ProcessThreadInfo)historyinfos.get(row);
			if(column==0)
			{
				ret = info.getThreadId();
			}
			else if(column==1)
			{
				ret = info.getActivity();
			}
			else if(column==2)
			{
				ret = info.getPool(); 
			}
			else if(column==3)
			{
				ret = info.getLane(); 
			}
			else
			{
				ret = info;
			}

			return ret;
		}
		
		public void addValue(int idx, ProcessThreadInfo pti)
		{
			historyinfos.add(idx, pti);
			fireTableRowsInserted(idx, idx);
		}
		
		public void clear()
		{
			int size = historyinfos.size();
			historyinfos.clear();
			fireTableRowsDeleted(0, size-1);
		}
	}
	
	/**
	 *  Get the step info. Help to decide which component step to perform next.
	 *  @return Step info for debugging.
	 */
	public String getStepInfo()
	{
		String ret = null;
		int row = getSelectedThreadRow();
		if(row!=-1)
		{
			ProcessThreadInfo pti = (ProcessThreadInfo)threads.getModel().getValueAt(row, -1);
			ret = pti.getThreadId();
		}
		return ret;
	}
	
	/**
	 * 
	 */
	protected int getSelectedThreadRow()
	{
		int ret = -1;
		int vrow = threads.getSelectedRow();
    	if(vrow!=-1)
    	{
    		ret = threads.convertRowIndexToModel(vrow);
    	}
    	return ret;
    }
	
	/**
	 *  Perform a step.
	 */
	protected void doStep()
	{
		SServiceProvider.getServiceUpwards(access.getServiceProvider(), IComponentManagementService.class)
			.addResultListener(new SwingDefaultResultListener<IComponentManagementService>(this)
		{
			public void customResultAvailable(final IComponentManagementService cms)
			{
				IFuture<Void> ret = cms.stepComponent(access.getComponentIdentifier(), getStepInfo());
				ret.addResultListener(new IResultListener<Void>()
				{
					public void resultAvailable(Void result)
					{
						updateViews();
					}
					
					public void exceptionOccurred(Exception exception)
					{
						exception.printStackTrace();
					}
				});
			}
		});
	}
	
	/**
	 * 
	 */
	public IFuture<Void> toggleBreakPoint(final String bp)
	{
		final Future<Void> ret = new Future<Void>();
		
		List<String> bps = Arrays.asList(access.getModel().getBreakpoints());
		
		if(bps.contains(bp))
		{
			getActiveBreakpoints().addResultListener(new ExceptionDelegationResultListener<List<String>, Void>(ret)
			{
				public void customResultAvailable(final List<String> abps)
				{
					System.out.println("active breakpoints: "+abps);
					
					if(abps.contains(bp))
					{
						abps.remove(bp);
					}
					else
					{
						abps.add(bp);
					}
					
					SServiceProvider.getService(access.getServiceProvider(), IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
						.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Void>(ret)
					{
						public void customResultAvailable(final IComponentManagementService cms)
						{
							cms.setComponentBreakpoints(access.getComponentIdentifier(), (String[])abps.toArray(new String[abps.size()]))
								.addResultListener(new DelegationResultListener<Void>(ret));
						}
					});
				}
			});
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	protected IFuture<List<String>> getActiveBreakpoints()
	{
		final Future<List<String>> ret = new Future<List<String>>();
		
		SServiceProvider.getService(access.getServiceProvider(), IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, List<String>>(ret)
		{
			public void customResultAvailable(final IComponentManagementService cms)
			{
				cms.getComponentDescription(access.getComponentIdentifier())
					.addResultListener(new ExceptionDelegationResultListener<IComponentDescription, List<String>>(ret)
				{
					public void customResultAvailable(IComponentDescription desc) 
					{
						List<String> bps = new ArrayList<String>(Arrays.asList(desc.getBreakpoints()));
						ret.setResult(bps);
					}
				});
			}
		});
				
		return ret;
	}
}


