package net.sourceforge.nite.tools.videolabeler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NElement;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import org.w3c.dom.Node;

/**
 * <p>An annotation layer encapsulates some layer-dependent properties. This
 * class acts like a wrapper around an NXT layer that is specified at
 * construction. The underlying layer and the code element that is used for
 * the annotations in this layer, are obtained with
 * {@link #getNLayer() getNLayer)} and
 * {@link #getCodeElement() getCodeElement()}.</p>
 *
 * <p>This class defines some properties that are used to display annotations
 * in an {@link AnnotationFrame AnnotationFrame} or {@link ViewFrame ViewFrame}.
 * An annotation frame consists of two or three parts. At the top there is an
 * annotation area (if {@link #showAnnotationArea() showAnnotationArea()}
 * returns true), which displays all annotations for the layer (and possibly an
 * agent). It uses the element formatter returned by
 * {@link #getElementFormatter() getElementFormatter()} to display an annotation
 * in an annotation area. The styles that are returned by {@link #getStyleMap()
 * getStyleMap()} are added to the annotation area, so they can be used by the
 * element formatter. In the middle of an annotation frame there is an
 * annotation control panel, which displays (among others) the current
 * annotation's start time, end time and label. The label is obtained with
 * {@link #getLabel(net.sourceforge.nite.nom.nomwrite.NOMElement) getLabel()}.
 * At the bottom of the annotation frame there is a target control panel, which
 * allows the user to make annotations. It is completely layer-dependent. The
 * panel is obtained with
 * {@link #createTargetControlPanel(net.sourceforge.nite.meta.NAgent) createTargetControlPanel()}.</p>
 *
 * <p>Two more methods are needed to update the corpus when annotations are
 * created or deleted. These methods are
 * {@link #createGap(net.sourceforge.nite.nom.nomwrite.NOMElement, double, double)
 * createGap()} and
 * {@link #sameTargets(net.sourceforge.nite.nom.nomwrite.NOMElement, net.sourceforge.nite.nom.nomwrite.NOMElement)
 * sameTargets()}.</p>
 *
 * <p>This abstract base class uses a {@link GenericElementFormatter
 * GenericElementFormatter}, which is stored in the protected variable
 * elementFormatter, and it handles the creation of target control panels
 * according to the guidelines of the class {@link AnnotationLayerFactory
 * AnnotationLayerFactory}. Subclasses only need to implement the abstract
 * methods, but they may also override {@link #getStyleMap() getStyleMap()} and
 * {@link #showAnnotationArea() showAnnotationArea()}.</p>
 */
public abstract class AnnotationLayer {
    // the underlying NXT layer, set in the constructor
    protected NLayer layer;
    
    // the code element for annotations
    protected NElement codeElement = null;
    
    // the element formatter, initialised to GenericElementFormatter in the
    // constructor, subclasses can set it to another element formatter
    protected ElementFormatter elementFormatter;
    
    // the layerinfo element from the configuration file
    protected Node layerInfo;
    
    // the constructor of the target control panel
    private Constructor panelConstructor = null;
    
    /**
     * <p>Constructs a new annotation layer for the specified NXT layer. The
     * <code>panelType</code> parameter should be the qualified name of the
     * class for the target control panels returned by
     * createTargetControlPanel(). The class should be a subclass of
     * {@link TargetControlPanel TargetControlPanel} and the constructor should
     * have the same type as the constructor of TargetControlPanel.</p>
     *
     * <p>If the specified target control panel class could not be found or
     * if the required constructor could not be found, this constructor
     * throws an exception. Subclasses may throw additional exceptions. Other
     * exceptions might occur in createTargetControlPanel().</p>
     *
     * @param layer an NXT layer
     * @param codeName the name of the code elements that represent the
     * annotations in this layer
     * @param panelType the qualified class name of the target control panels
     * @param layerInfo the layer info element from the configuration file
     * @exception ClassNotFoundException if the class specified in panelType
     * was not found
     * @exception NoSuchMethodException if the class specified in panelType
     * does not have a constructor that takes one argument of type
     * AnnotationLayer
     * @exception Exception if another error occurs
     */
    public AnnotationLayer(NLayer layer, String codeName, String panelType, Node layerInfo)
    throws ClassNotFoundException, NoSuchMethodException, Exception {
        this.layer = layer;
        this.layerInfo = layerInfo;
        
        // find code element
        List codeElems = layer.getContentElements();
        Iterator it = codeElems.iterator();
        while ((codeElement == null) && it.hasNext()) {
            NElement elem = (NElement)it.next();
            if (elem.getName().equals(codeName))
                codeElement = elem;
        }
        if (codeElement == null)
            throw new Exception("no code element \"" + codeName + "\" for layer \"" + layer.getName() + "\"");
        
        Class panelClass = Class.forName(panelType);
        Class[] constrArgTypes = new Class[4];
        constrArgTypes[0] = Class.forName("net.sourceforge.nite.tools.videolabeler.AnnotationFrame");
        constrArgTypes[1] = Class.forName("net.sourceforge.nite.tools.videolabeler.AnnotationLayer");
        constrArgTypes[2] = Class.forName("org.w3c.dom.Node");
        constrArgTypes[3] = Class.forName("net.sourceforge.nite.meta.NAgent");
        panelConstructor = panelClass.getConstructor(constrArgTypes);
        
        elementFormatter = new GenericElementFormatter();
    }
    
    /**
     * <p>Returns the underlying NXT layer of this annotation layer.</p>
     *
     * @return the underlying NXT layer of this annotation layer
     */
    public NLayer getNLayer() {
        return layer;
    }
    
    /**
     * <p>Returns the code element for annotations in this layer.</p>
     *
     * @return the code element for annotations in this layer
     */
    public NElement getCodeElement() {
        return codeElement;
    }
    
    /**
     * <p>Returns a hash map that maps style names (String) to styles (Style).
     * This method returns an empty hash map.</p>
     *
     * @return an empty hash map
     */
    public HashMap getStyleMap() {
        HashMap result = new HashMap();
        return result;
    }
    
    /**
     * <p>Determines whether the annotation area should be shown in the
     * annotation frame. By default this method returns true.</p>
     *
     * @return true if the annotation area should be shown, false otherwise
     */
    public boolean showAnnotationArea() {
        return true;
    }
        
    /**
     * <p>Returns the element formatter that is used to display annotations
     * in an annotation area. This method returns an instance of
     * GenericElementFormatter.</p>
     *
     * @return an element formatter
     */
    public ElementFormatter getElementFormatter() {
        return elementFormatter;
    }
    
    /**
     * <p>Returns the label of an annotation in this layer. If the label
     * could not be retrieved, an error is printed to standard output and this
     * method returns null.</p>
     *
     * @param annotation an annotation in this layer
     * @return the label of the specified annotation or null
     */
    public abstract String getLabel(NOMElement annotation);
    
    /**
     * <p>Splits an annotation, so there will be a gap between the specified
     * start time and end time. The specified annotation must start before
     * the start time and it must end after the end time. This method is called
     * when a new annotation is going to be added between the specified start
     * and end time. The method is called from the {@link Document Document}
     * class and implementations of this method are free to edit the corpus
     * without notifying document listeners.</p>
     *
     * @param annotation the annotation that will be split
     * @param startTime the time where the gap will start
     * @param endTime the time where the gap will end
     * @exception Exception if an error occurs
     */
    public abstract void createGap(NOMElement annotation, double startTime, double endTime)
    throws Exception;
    
    /**
     * <p>Compares two annotations and returns true if they have the same
     * target. If the two annotations are adjacent, it means they can be
     * merged into one annotation.</p>
     *
     * @param elem1 the first annotation
     * @param elem2 the second annotation
     * @return true if both annotations have the same target, false otherwise
     */
    public abstract boolean sameTargets(NOMElement elem1, NOMElement elem2);
    
    /**
     * <p>Creates a new target control panel for this layer and the specified
     * agent. The agent parameter may be null if the layer belongs to an
     * interaction coding rather than an agent coding. The target control panel
     * will be of the type that was specified at construction. If the panel
     * could not be created, an error is printed to standard output and this
     * method returns null.</p>
     *
     * @param frame the annotation frame that will contain the target control
     * panel
     * @param agent an agent (if the layer belongs to an agent coding) or null
     * (if the layer belongs to an interaction coding)
     * @return the new target control panel or null
     */
    public TargetControlPanel createTargetControlPanel(AnnotationFrame frame,
            NAgent agent) {
        Object[] constrArgs = new Object[4];
        constrArgs[0] = frame;
        constrArgs[1] = this;
        constrArgs[2] = layerInfo;
        constrArgs[3] = agent;
        TargetControlPanel result = null;
        try {
            result = (TargetControlPanel)panelConstructor.newInstance(constrArgs);
        } catch (InstantiationException ex) {
            System.out.println("ERROR: Target control panel constructor could not be invoked: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println("ERROR: Target control panel constructor does not have public access: " + ex.getMessage());
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            String msg = "ERROR: Target control panel constructor threw an exception";
            if (cause != null)
                msg += ": " + cause.getClass() + ": " + cause.getMessage();
            System.out.println(msg);
        }
        return result;
    }
}
