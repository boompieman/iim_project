package net.sourceforge.nite.tools.videolabeler;

import java.awt.Rectangle;
import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.util.Vector;
import javax.swing.JDesktopPane;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;

import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.gui.timelineviewer.*;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.search.*;
import net.sourceforge.nite.query.*;

/**
 * <p>A timeline factory is a global singleton object. Before it is used it
 * should be created with
 * {@link #createInstance(javax.swing.JDesktopPane, java.awt.Rectangle) ViewFrameFactory.createInstance()}.
 * From then on the same factory can be retrieved with
 * {@link #getInstance() TimelineFactory.getInstance()}.</p>
 *
 * <p>A timeline factory displays selected annotation layers on a timeline.
 * A timeline is an instance of {@link net.sourceforge.nite.gui.timelineviewer.NiteTimeline NiteTimeline}. This
 * factory does not only create the timeline, but it also serves as a repository
 * of the current timeline and it is responsible for laying out the timeline
 * in a designated area of a desktop pane.</p>
 *
 * <p>The factory implements the {@link SelectionListener SelectionListener}
 * interface. The user should be able to select and deselect the layers to
 * be shown on the timeline. Whenever the user selects or deselects an
 * annotation layer, the method
 * {@link #layerSelected(net.sourceforge.nite.tools.videolabeler.AnnotationLayer, boolean) layerSelected()}
 * should be called, so that the timeline blobs for the selected/deselected layer
 * is created/removed and the timeline is refreshed.</p>
 */
public class TimelineFactory implements SelectionListener {
    private NiteTimeline timeline = null;
    private HashSet selectedLayers = new HashSet();
    private JDesktopPane desktop;
    private Rectangle area;
    private Clock niteclock = null;
    private NOMWriteCorpus nom;
    private final int maxscale = 100; // Largest allowable number of ms per pixel
    CSLConfig cfg = CSLConfig.getInstance();

    /**
     * Constructs a new view frame factory. See createInstance()
     */
    private TimelineFactory(JDesktopPane desktop, Rectangle area, NOMWriteCorpus corpus) {
        this.desktop = desktop;
        this.area = area;
        this.nom = corpus;
    }

    private static TimelineFactory instance = null;

    /**
     * <p>Initialises the singleton timeline factory. The factory will display
     * the timline on the specified desktop. The timeline will be layed out in
     * the specified area of the desktop.</p>
     *
     * <p>If the singleton timeline factory has already been initialised, this
     * method does not create a new factory, but simply returns the existing
     * singleton factory.</p>
     *
     * @param desktop the desktop where the view frames will be displayed
     * @param area the area of the desktop in which the view frames will be
     * layed out
     * @return the singleton view frame factory
     */
    public static TimelineFactory createInstance(JDesktopPane desktop, Rectangle area, NOMWriteCorpus corpus) {
        if (instance == null)
            instance = new TimelineFactory(desktop,area,corpus);
        return instance;
    }


    private void initialiseTimeline() {
	if (timeline != null) return;
		
	timeline = new NiteTimeline("Timeline view", getClock());

	timeline.getGrid().setScale(100);
	timeline.getGrid().setBlobLabel(true);
		
	if(search != null){
	    search.registerResultHandler(timeline.getGrid());
	}
		
	timeline.setLayerTextDelegate(
				      new TranscriptionToTextDelegate() {

					  public String getTextForTranscriptionElement(NOMElement nme) {
					      String rstr = nme.getName();
					      String ln=null;
					      try { ln = nme.getLayer().getName(); } 
					      catch (Exception ex) { }
					      if (ln!=null) { rstr=ln; }
					      String a = nme.getAgentName();
					      if (a!=null) { rstr += ": "+ a; }
					      NResource nr = nme.getResource();
					      // if (nr!=null) { rstr += " ("+ nr.getID() + ")"; }
					      return rstr + "  ";
					  }
				      } );

        ElementToLayerDepthDelegate separateAgentByDepth = 
            new ElementToLayerDepthDelegate() {
                public int getElementLayerDepth(NOMElement nme) {
                    return nme.getAttribute("agent").getStringValue().charAt(0) - 'a' + 1; //index from 1
                }
	    };
        // timeline.setElementToLayerDepthDelegate(separateAgentByDepth);

	TranscriptionToTextDelegate idOfElement = 
	    new TranscriptionToTextDelegate() {
		public String getTextForTranscriptionElement(NOMElement nme) {
		    AnnotationLayer al = null;
		    try { al=getLayerWithName(nme.getLayer().getName()); }
		    catch (Exception ex) {}
		    if (al==null) { 
			return nme.getID();
		    } else {
			return al.getLabel(nme);
		    }
		}
		
	    };
	
	timeline.getElementToTextDelegate().setTranscriptionToTextDelegate(idOfElement);
	
	if(area == null) {
	    timeline.getFrame().setLocation(200,0);
	    timeline.getFrame().setSize(new Dimension(700, 600));
	} else {
	    timeline.getFrame().setBounds(area.x, area.y, area.width, area.height);
	}
		
	desktop.add(timeline.getFrame());	
    }

    private AnnotationLayer getLayerWithName(String metalayername) {
	if (metalayername==null) { return null; }
	for (Iterator sit=selectedLayers.iterator(); sit.hasNext(); ) {
	    AnnotationLayer ala = (AnnotationLayer)sit.next();
	    if (ala==null) { continue; }
	    if (ala.layer.getName().equals(metalayername)) {
		return ala;
	    }
	}
	return null;
    }

    public Clock getClock()
    {
        return niteclock;
    }

    public void setClock(Clock c)
    {
        niteclock = c;
        if(timeline != null) {
            timeline.setClock(c);
        }
    }

    private void addTimedResults(List queryresults) 
    {
	//System.out.println("addTimedResults: " + queryresults.size() + " results found.");
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
				addTimedResults((List) nextitem);
			    }
			else 
			    {
				NOMElement ne = (NOMElement) nextitem;
				if (ne.getStartTime() == NOMElement.UNTIMED || ne.getEndTime() == NOMElement.UNTIMED) 
				    {
					continue;
				    }

				//System.out.println("Adding element: " + ne);
				timeline.addElement(ne);
			    }

		    }	     
	    }
    }

    /**
     * <p>Returns the singleton timeline factory. The factory should have been
     * initialised first with
     * {@link #createInstance(javax.swing.JDesktopPane, java.awt.Rectangle) createInstance}.
     * If the factory has not been initialised yet, this method returns
     * null.</p>
     *
     * @return the singleton timeline factory or null
     */
    public static TimelineFactory getInstance() {
        return instance;
    }

    /**
     * <p>Sets the area of the desktop in which the view frames are layed out.
     * This method will set the area and lay out the current view frames in the
     * specified area.</p>
     *
     * @param area an area of the desktop
     */
    public void setArea(Rectangle area) {
        this.area = area;
        layoutFrames();
    }

    /**
     * Lays out the current view frames in the set desktop area.
     */
    private void layoutFrames() {
	if (timeline == null) { return; }
	timeline.getFrame().setBounds(area.x, area.y, area.width, area.height);
    }

    /**
     * Adds a layer to the selected layers, creates the blobs for that
     * layer and lays out the current view frames.
     */
    private void addLayer(AnnotationLayer layer) {
    	if(timeline == null) { initialiseTimeline(); }
    	List nelems = layer.getNLayer().getContentElements(); // List of NElements
    	String query = "($e ";
    	Iterator nei = nelems.iterator();
    	while(nei.hasNext()) {
	    NElement n = (NElement) nei.next();
	    query += n.getName() + "|"; 
    	}
    	query = query.substring(0, query.length() - 1) + " )";
    	//System.out.println("TimelineFactory query: " + query);
        selectedLayers.add(layer);
        addTimedResults(doSearch(query));

        TimeGrid tg = timeline.getGrid();
	int fittogrid = (int) (tg.getMaxTimeInMilliseconds() / tg.getParent().getWidth());
        if (fittogrid < maxscale) {
            tg.setScale(fittogrid);
        } else {
            tg.setScale(maxscale);
        }

        timeline.getFrame().setVisible(true);
	timeline.getFrame().repaint();
    }

    /**
     * Removes a layer from the selected layers, removes the blobs for that
     * layer and lays out the current timeline.
     */
    private void removeLayer(AnnotationLayer layer) {
        selectedLayers.remove(layer);
	timeline.resetGrid();
	//timeline = null;
	if (selectedLayers.size()==0) {
	    timeline.getFrame().setVisible(false);
	} else {
	    for (Iterator lit=selectedLayers.iterator(); lit.hasNext(); ) {
		AnnotationLayer al = (AnnotationLayer)lit.next();
		addLayer(al);
	    }
	    timeline.getFrame().repaint();
	}
    }

    /**
     * <p>This method does nothing, because view frames are only associated with
     * a layer, not with an agent.</p>
     */
    public void agentSelected(NAgent agent, boolean selected) {}

    /* added by jonathan 21.4.05 */
    protected net.sourceforge.nite.search.GUI search=null;
    /** set the search gui with which individial NTextAreas can be registered */
    protected void setSearch(net.sourceforge.nite.search.GUI search) {
	this.search=search;
    }

    public List doSearch(String query) {
        List result = null;
    	try {
    	    result = search.getEngine().search(nom, query); 
    	} catch (Throwable e) {
    	    e.printStackTrace();
    	}
    	return result;
    }

    /**
     * <p>Selects or deselects a layer. If a layer is selected, the view frame
     * for that layer is created on the desktop. If a layer is deselected, the
     * view frame for that layer is removed from the desktop. In either case
     * the new set of selected view frames are layed out in the designated
     * desktop area.</p>
     *
     * @param layer the layer that is selected or deselected
     * @param selected true if the layer is selected, false if it is deselected
     */
    public void layerSelected(AnnotationLayer layer, boolean selected) {
        if (selected) {
            if (!selectedLayers.contains(layer))
                addLayer(layer);
        } else {
            removeLayer(layer);
        }
    }
}
