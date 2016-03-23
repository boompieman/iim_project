package net.sourceforge.nite.tools.comparison.timeline;

import java.awt.Dimension;
import java.awt.Color;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.logging.Logger;

import net.sourceforge.nite.gui.timelineviewer.TimeBlob;
import net.sourceforge.nite.gui.timelineviewer.TimeGrid;
import net.sourceforge.nite.gui.util.AbstractCallableTool;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.util.*;

public class TimelineComparisonDisplay extends AbstractCallableTool {
    private TimeGrid tg;
    private int timescale = 100;
    private String annotatorattr = "coder";
    private String pathtoannotators = ".";
	
    public void addTimedResults(List queryresults, int level, String queryname) 
    {
	for(Iterator qit = queryresults.iterator(); qit.hasNext(); )
	    {
		List nextresult = (List) qit.next();
		if (nextresult.get(0) instanceof String) 
		    {
			continue;
		    }
		for (Iterator eit = nextresult.iterator(); eit.hasNext(); )
		    {
			Object nextitem = eit.next();
			if (nextitem instanceof List) 
			    {
				addTimedResults((List) nextitem, level + 1, queryname);
			    }
			else 
			    {
				NOMElement ne = (NOMElement) nextitem;
				if (ne.getStartTime() == NOMElement.UNTIMED || ne.getEndTime() == NOMElement.UNTIMED) 
				    {
					continue;
				    }
					
				TimeBlob tb = new TimeBlob(tg, (int) (ne.getStartTime() * 1000), (int) (ne.getEndTime() * 1000), queryname, level, ne.getID() + " - " + printAttributes(ne.getAttributes() ));
			    }
				
		    }	     
	    }
    }
	
    public void addTimedResults(List queryresults, String queryname) 
    {
	addTimedResults(queryresults, 1, queryname);
    }
	
    private String printAttributes(List atts) {
	String out = "";
	Iterator it = atts.iterator();
	while (it.hasNext()) {
	    NOMAttribute a = (NOMAttribute) it.next();
	    out += a.getName() + " : " + a.getStringValue() + ", ";
	}
	return "[" + out + "]";
    }

    private String ZOOM_IN_ACTION = "Zoom in on timescale";
    private String ZOOM_OUT_ACTION = "Zoom out of timescale";
    private String ZOOM_WINDOW_ACTION = "Fit timescale to window";
   
    public class zoomAction extends AbstractAction 
    {
	protected double factor;
	
	public zoomAction(double zoomFactor) 
	{
	    super("Zoom " + (zoomFactor > 1 ? "in" : "out") );
	    factor = zoomFactor;
	};

	public void actionPerformed(ActionEvent ev) 
	{
	    timescale *= factor;
	    // If timescale is not an integer less than fit-to-window size
	    if (timescale <= 1 || timescale >= tg.getMaxTime() / tg.getParent().getWidth()) {
		timescale /= factor;
	    }
	    tg.setScale(timescale);
	    tg.repaint(tg.getBounds(null));
	    System.out.println("" + factor + " * zoom : scale is now " + timescale); 
	};
    }
   
    /* zoom to a fixed factor */
    public class fixedZoomAction extends AbstractAction 
    {
	public fixedZoomAction() 
	{
	    super("Fit to window.");
	}
	
	public void actionPerformed(ActionEvent ev)
	{
	    // Problem here is that tg.getWidth() depends on initial scale... :-(
	    timescale = (int) (tg.getMaxTimeInMilliseconds() / tg.getParent().getWidth());
	    tg.setScale(timescale);
	    tg.repaint(tg.getBounds(null));
	    System.out.println("Zoom : fit to window.");
	}
	
    }
   
   
    protected void addActions() 
    {
	Action act = null;
	
	act = new zoomAction(2.0); 
	act.putValue(Action.SHORT_DESCRIPTION,"Show fewer events in the timeline.");
	act.putValue(Action.MNEMONIC_KEY,new Integer(java.awt.event.KeyEvent.VK_PLUS));
	act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PLUS, java.awt.event.InputEvent.CTRL_MASK ));
	act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ADD, java.awt.event.InputEvent.CTRL_MASK ));
	getActionMap().put(ZOOM_IN_ACTION, act);
	
	act = new zoomAction(0.5); 
	act.putValue(Action.SHORT_DESCRIPTION,"Show more events in the timeline.");
	act.putValue(Action.MNEMONIC_KEY,new Integer(java.awt.event.KeyEvent.VK_MINUS));
	act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, java.awt.event.InputEvent.CTRL_MASK ));
	act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SUBTRACT, java.awt.event.InputEvent.CTRL_MASK ));
	getActionMap().put(ZOOM_OUT_ACTION, act);
	
	act = new fixedZoomAction(); 
	act.putValue(Action.SHORT_DESCRIPTION,"Show all events in the timeline.");
	act.putValue(Action.MNEMONIC_KEY,new Integer(java.awt.event.KeyEvent.VK_ASTERISK));
	act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ASTERISK, java.awt.event.InputEvent.CTRL_MASK ));
	act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MULTIPLY, java.awt.event.InputEvent.CTRL_MASK ));
	getActionMap().put(ZOOM_WINDOW_ACTION, act);
	
    }
   
    private void addMenus() 
    {
	addActions();
	
        JMenu zoomM = new JMenu("Zoom");
    	getMenuBar().add(zoomM);
    	getMenuMap().put("Zoom",zoomM);
        zoomM.add(new JMenuItem(getActionMap().get(ZOOM_IN_ACTION)));
        zoomM.add(new JMenuItem(getActionMap().get(ZOOM_OUT_ACTION)));
        zoomM.add(new JMenuItem(getActionMap().get(ZOOM_WINDOW_ACTION)));
    }	
   
    public TimelineComparisonDisplay(String [] args) {
	parseArguments(args);
	initConfig();
	initializeCorpus(getCorpusName(),getObservationName()); //, getAnnotatorName()); //initialize the corpus, given the settings passed as arguments
	setupMainFrame(getConfig().getApplicationName()); //setup a main frame 
	initLnF(); // Look and feel. Can be ignored/left out if you happen to like the default metal look-and-feel
	setupDesktop(); //setup a desktop on which all the windows (media viewer, transcription view, etc) are added

	setupLog(Logger.global, 530, 535, 465, 90); //a logging window, useful for giving feedback to the user
        setupMediaPlayer(695,15,300,400); //a mediaplayer: necessary if you want video or audio players synchronized to the transcription view
        
	setupMenus();
	addMenus();
	   
	JInternalFrame frame = new JInternalFrame("Testing Time Trial");
	tg = new TimeGrid();
	//getDesktop().addMouseListener(tg);
	frame.addMouseListener(tg);
        tg.setClock(getClock());
	//tg.setHorizontal(false);
	//tg.setBlobFatness(30);
		
	//TODO: Make scale interactively setable
	tg.setScale(timescale);
	tg.setBlobLabel(true);
		
	//tg.setLayerColour("lay4", Color.green);
	//tg.setLayerColour("lay2", new Color(200,100,20));
	TimeBlob tb = new TimeBlob(tg, 100000, 700000, "ann1", 1, "Topic1");
	tb = new TimeBlob(tg, 100000, 500000, "ann1", 2, "Subtopic1.1");	
	tb = new TimeBlob(tg, 500000, 700000, "ann1", 2, "Subtopic1.2");	
	tb = new TimeBlob(tg, 100000, 200000, "ann1", 3, "Subtopic1.1.1");	
	tb = new TimeBlob(tg, 200000, 400000, "ann1", 3, "Subtopic1.1.2");	
	tb = new TimeBlob(tg, 700000, 1000000, "ann1", 1, "Topic2");	
	tb = new TimeBlob(tg, 100000, 600000, "ann2", 1, "Topic1");
	tb = new TimeBlob(tg, 600000, 1000000, "ann2", 1, "Topic2");
	TimeBlob tb6 = new TimeBlob(tg, 60000, 210000, "lay4", 1, "timeous");	
	tb6 = new TimeBlob(tg, 20000, 270000, "lay5", 1, "trap");	
	tb6 = new TimeBlob(tg, 35000, 155600, "lay6", 1, "tiger");
	tb6 = new TimeBlob(tg, 5000, 115600, "lay7", 1, "lion");
	tb6 = new TimeBlob(tg, 30000, 255600, "lay8", 1, "liger");
	tb6 = new TimeBlob(tg, 315000, 355600, "lay9", 1, "bear");
	tb6 = new TimeBlob(tg, 183300, 195600, "lay10", 1, "tart");
	tb6 = new TimeBlob(tg, 252000, 355600, "lay11", 1, "citron");
	tb6 = new TimeBlob(tg, 135000, 155600, "lay12", 1, "orange");
	tb6 = new TimeBlob(tg, 35000, 155600, "lay13", 1, "pair");
	//tg.addBlob(tb);
		
	//Debug.print("dact search Results: all - " + search("($a dact)").size() + ", dharshi - " + search("($a dact):($a@" + annotatorattr + " = 'dharshi')").size() + ", vkaraisk - " + search("($a dact):($a@" + annotatorattr + " = 'vkaraisk')").size());
	//NOMElement firstresult = ( (NOMElement) ( (List) search("($a dact)").get(1) ).get(0) );
	//Debug.print("First result atts: " + printAttributes(firstresult.getAttributes()) );
	//Debug.print("w search results: " + search("($a w)").size());
	//firstresult = ( (NOMElement) ( (List) search("($a w)").get(1) ).get(0) );
	//Debug.print("First result atts: " + printAttributes(firstresult.getAttributes()) );

	//addTimedResults(search("($a w)"), "words");
	//addTimedResults(search("($a dact)"), "dact");
	addTimedResults(search("($a dact):($a@who='A')"), "dactA");
	addTimedResults(search("($a dact):($a@who='B')"), "dactB");
	addTimedResults(search("($a dact):($a@who='C')"), "dactC");
	addTimedResults(search("($a dact):($a@who='D')"), "dactD");
	//addTimedResults(search("($a dact):($a@" + "resource" + " = 'da_ds')"), "dharshi");
	//addTimedResults(search("($a dact):($a@" + "resource" + " = 'da_vk')"), "vkaraisk");
	   
	frame.setSize(new Dimension(900, 600));
	JScrollPane jsp = new JScrollPane(tg);
	frame.getContentPane().add(jsp);
	jsp.addMouseListener(tg);
	frame.setVisible(true);
	getDesktop().add(frame);
	Debug.print("Finished loading data.");
    }
	
    protected void initNomAnnotatorSpecificLoads(NOMWriteCorpus nom) 
    {
	/*
	  String annlayername = "da-layer"; // config.getAnnotationLayer();
	  String commlayername = "words-layer"; // config.getCommonLayer();
		 
	  NLayer toplay = getMetaData().getLayerByName(annlayername);
	  NLayer commlay = null;
	  if (commlayername != null) {
	  commlay = getMetaData().getLayerByName(commlayername);
	  if (commlay == null) {
	  System.err.println("Can't find common layer (" + commlayername + ") in the metadata.\n"); 
	  System.exit(1);
	  }
	  }
		 
	  if (toplay==null) {
	  System.err.println("Can't find annotator layer (" + annlayername + ") in the metadata.\n"); 
	  System.exit(1);
	  }	
		 
	  ArrayList obslist = new ArrayList();
	  obslist.add(getObservationName());
		 
	  try { 
	  nom.loadReliability(toplay, commlay, annotatorattr, pathtoannotators, obslist);
	  } catch (NOMException e) {
	  e.printStackTrace();
	  }
	*/
		
	try {
	    nom.forceResourceLoad("AMIwordsref1");
	    nom.forceResourceLoad("AMIwordsASRa1");
	    nom.forceResourceLoad("da_vk");
	    nom.forceResourceLoad("da_ds");
	} catch (NOMException e) {
	    e.printStackTrace();
	}
	return;
    }
	
	
    public static void main(String[] args) {
	TimelineComparisonDisplay nt = new TimelineComparisonDisplay(args);
    }
	
}
