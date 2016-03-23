package net.sourceforge.nite.gui.timelineviewer;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Container;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.logging.Logger;

import net.sourceforge.nite.gui.abstractviewer.NiteAbstractViewer;
import net.sourceforge.nite.gui.transcriptionviewer.TranscriptionToTextDelegate;
import net.sourceforge.nite.gui.util.AbstractCallableTool;
import net.sourceforge.nite.gui.util.ElementToTextDelegate;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.time.Clock;
import net.sourceforge.nite.util.*;


/**
 * The NiteTimeline provides a scrolling window containing blobs arranged by time
 * on one axis, and by layer on the other. When NOMElements are added to the viewer
 * they are assigned to blobs, but blobs do not necessarily need to be assigned a
 * NOMElement (if, for example, they represent a signal, although this is not yet
 * implemented).
 * 
 * A layer groups together a set of NOMElements, and each layer may contain a series
 * of numbered sub-layers. The layers are defined by strings, and each NOMElement is
 * assigned to a layer via the {@link #getElementToLayerDelegate()} and
 * {@link #setElementToLayerDelegate(ElementToTextDelegate)} methods. 
 * 
 * @author Craig Nicol
 *
 */
public class NiteTimeline extends NiteAbstractViewer {
    private TimeGrid tg;
    private int timescale = 100;
    private JInternalFrame frame;
    private boolean assignedtogrid = false; // WE have to defer this as we need a 'this' pointer
	
    private ElementToLayerDepthDelegate eltodepth;

    /**
     * @return the current ElementToLayerDepthDelegate
     */
    public ElementToLayerDepthDelegate getElementToLayerDepthDelegate() {
	return eltodepth;
    }

    /**
     * @param eltodepth the ElementToLayerDepthDelegate to use on this timeline
     */
    public void setElementToLayerDepthDelegate(ElementToLayerDepthDelegate eltodepth) {
	this.eltodepth = eltodepth;
    }
	
    private ElementToTextDelegate eltolayerdelegate = new ElementToTextDelegate();

    public ElementToTextDelegate getElementToLayerDelegate() {
	return eltolayerdelegate;
    }
	
    public void setElementToLayerDelegate(ElementToTextDelegate eeld) {
	eltolayerdelegate = eeld;
    }
	
    /** <p>Divide elements into layers. Each element on the timeline is passed
	through this delegate class to make a String that names the layer;
	elements returning the same String will be placed in the same layer.</p>
      
	<p><strong>NOTE:</strong> This will override anything set by 
	{@link #setLayerTextAttribute(String)} unless delegate is null.</p>
      
	@param The delegate to assign, or null
      
	@see #getElementLayerDepth(NOMElement)
	@see ElementToTextDelegate
    */
    public void setLayerTextDelegate(TranscriptionToTextDelegate delegate) {
	if (eltolayerdelegate != null) {
	    eltolayerdelegate.setTranscriptionToTextDelegate(delegate);
	}
    }

    /** 
     * <p>Divide elements into layers. Each element on the timeline is passed
     * through this delegate class to make a String that names the layer;
     * elements returning the same String will be placed in the same layer.</p>
     * <p><strong>NOTE:</strong> This will be overridden by anything set by 
     * {@link #setLayerTextDelegate(TranscriptionToTextDelegate)}</p>
     *
     *       
     * @param attr The attribute to use, or null
     * @see #getElementLayerDepth(NOMElement)
     * @see ElementToTextDelegate
     */
    public void setLayerTextAttribute(String attr) {
	if (eltolayerdelegate != null) {
	    eltolayerdelegate.setTranscriptionAttribute(attr);
	}
    }
     
    /** <p>Provide the String to be displayed on individual blobs on the
	timeline.</p> 

	<p><strong>NOTE:</strong> This will override anything set by 
	{@link #setBlobTextAttribute(String)} unless delegate is null.</p>
      
	@param delegate The delegate to use in this timeline.
	@see ElementToTextDelegate
    */
    public void setBlobTextDelegate(TranscriptionToTextDelegate delegate) {
	if (getElementToTextDelegate() != null) {
	    getElementToTextDelegate().setTranscriptionToTextDelegate(delegate);
	}
    }

    /** <p>Provide the String to be displayed on individual blobs on the
	timeline.</p> 

	<p><strong>NOTE:</strong> This will be overridden by anything set by 
	{@link #setBlobTextDelegate(TranscriptionToTextDelegate)}.</p>
     
	@param attr The NOMElement attribute to use in this timeline.
	@see ElementToTextDelegate
    */
    public void setBlobTextAttribute(String attr) {
	if (getElementToTextDelegate() != null) {
	    getElementToTextDelegate().setTranscriptionAttribute(attr);
	}
    }
     
    /** Returns the depth of the NOMElement within the current layer. 
     * 
     * @param el the element to be displayed
     * @return depth to display (or 0 if no display)
     * @see #setElementToLayerDepthDelegate(ElementToLayerDepthDelegate) 
     * to change what this method returns.
     * @see ElementToLayerDepthDelegate
     * @see #setLayerTextDelegate(TranscriptionToTextDelegate) 
     * for more details on layers
     * 
     */
    public int getElementLayerDepth(NOMElement el) {
	if (eltodepth == null)
	    return el.getRecursiveDepth() + 1;
	else
	    return eltodepth.getElementLayerDepth(el);
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
   
    /** Create a NiteTimeline without a clock
     **/
    public NiteTimeline() {
	initialise("Timeline", null);
    }
	
    /** Create a NiteTimeline synchronised with the provided clock
     *
     * @param c A Clock to synchronise with
     * */
    public NiteTimeline(Clock c) {
	initialise("Timeline", c);
    }

    public void resetGrid() {
	getGrid().reset();
    }
	
    /**
     * Create a NiteTimeline with the given window title, synchronised
     * with the provided clock.
     * 
     * @param title The Window title
     * @param c A Clock to synchronise with
     */
    public NiteTimeline(String title, Clock c) {
	initialise(title, c);
    }
	
    /**
     * Create a NiteTimeline with the given window title.
     * 
     * @param title The Window title
     */
    public NiteTimeline(String title) {
	initialise(title, null);
    }

    
	
    private void initialise(String title, Clock c) {
	frame = new JInternalFrame(title);
	tg = new TimeGrid();
	frame.addMouseListener(tg);
	frame.setResizable(true);
	frame.setIconifiable(true);
	//frame.setClosable(true);

	setClock(c);
	//tg.setHorizontal(false);
	//tg.setBlobFatness(30);
		
	//TODO: Make scale interactively setable
	tg.setScale(timescale);
	tg.setBlobLabel(true);				

	frame.setSize(new Dimension(900, 600));
	JScrollPane jsp = new JScrollPane(tg);
	tg.addTimeHeader(jsp);
	tg.addLayerHeader(jsp);
	frame.getContentPane().add(jsp);
	jsp.addMouseListener(tg);
	frame.setVisible(true);
	//getDesktop().add(frame);
	Debug.print("Finished loading data.", Debug.DEBUG);
    }
	
    /**
     * Return the frame for this timeline
     */
    public JInternalFrame getFrame() {
	return frame;
    }
	
    /** Get the current TimeGrid
     * @return the TimeGrid associated with this timeline
     */
    public TimeGrid getGrid() {
	if (!assignedtogrid) {
	    tg.setTimeline(this);
	    assignedtogrid = true;
	}
	return tg;
    }

    /**
     * Add a clock to synchronise to
     * @param c
     */
    public void setClock(Clock c) {
	tg.setClock(c);
    }

    /**
     * 
     * @return The Clock currently used for synchronisation
     */
    public Clock getClock() {
	return tg.getClock();
    }
	
    /**
     * Add a new blob to the timeline based on an element
     * @param element
     */
    public void addElement(NOMElement element) {
	TimeBlob tb = new TimeBlob(tg, element, this);
    }
	
    /**
     * Add new blobs to the timeline based on NOMElements
     * 
     * @param elements An Iterator over NOMElements
     */
    public void setDisplayedElements(Iterator elements) {
	// TODO Auto-generated method stub
	while (elements.hasNext()) {
	    NOMElement ne = (NOMElement) elements.next();
	    TimeBlob tb = new TimeBlob(tg, ne, this);
	}
    }
	
}
