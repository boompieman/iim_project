package net.sourceforge.nite.tools.videolabeler;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import net.sourceforge.nite.gui.textviewer.NTextArea;
import net.sourceforge.nite.gui.textviewer.NTextElement;
import net.sourceforge.nite.gui.util.ValueColourMap;
import net.sourceforge.nite.meta.NAttribute;
import net.sourceforge.nite.meta.NElement;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.meta.NObjectSet;
import net.sourceforge.nite.meta.NOntology;
import net.sourceforge.nite.meta.NPointer;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMAttribute;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMPointer;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteAnnotation;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWritePointer;
import net.sourceforge.nite.nstyle.handler.TextStyleHandler;
import net.sourceforge.nite.nxt.NOMObjectModelElement;
import net.sourceforge.nite.nxt.ObjectModelElement;
import net.sourceforge.nite.util.Debug;
import org.w3c.dom.Node;

/**
 * <p>In this annotation layer the annotation targets are elements from an
 * object set or ontology. Each label is assigned its own colour using a
 * {@link net.sourceforge.nite.gui.util.ValueColourMap ValueColourMap}. The
 * element formatter displays the label of an annotation target. The label is
 * displayed between brackets [] and the text will have the colour that was
 * assigned to the label.</p>
 */
public class LabelAnnotationLayer extends AnnotationLayer {
    private NPointer pointer = null;
    private String labelattribute = null;
    private NObjectSet objectSet = null;
    private NOntology ontology = null;
    private NAttribute attribute = null;
    
    // maps targets (NOMElement) to colour (Color)
    private ValueColourMap targetColourMap = null;
    
    // maps targets (NOMElement) to style names (String)
    private HashMap targetStyleMap = new HashMap();
    
    // maps style names (String) to styles (Style)
    private HashMap styleMap = new HashMap();

    /**
     * <p>Constructs a new label annotation layer. The layerInfo parameter is
     * the layerinfo element from the configuration file. It should have the
     * following two attributes:</p>
     *
     * <ul>
     * <li>pointerrole: the role of the pointer that points from the code
     * element with the specified code name to the object set or ontology that
     * contains the possible targets</li>
     * <li>labelattribute: the attribute of an object set or ontology element
     * that contains the label of the annotation target</li>
     * </ul>
     *
     * @param layer the NXT layer
     * @param codeName the name of the code elements that represent annotations
     * in this layer
     * @param panelType the qualified class name of the target control panel
     * that should be used with this layer
     * @param layerInfo the layerinfo element
     */
    public LabelAnnotationLayer(NLayer layer, String codeName, String panelType, Node layerInfo)
    throws ClassNotFoundException, NoSuchMethodException, Exception {
        super(layer,codeName,panelType,layerInfo);
        
        // get layer-specific layerinfo attributes
        CSLConfig cfg = CSLConfig.getInstance();
        String enumatt = cfg.getAttributeValue(layerInfo,"enumeratedattribute");
        String pointerrole = cfg.getAttributeValue(layerInfo,"pointerrole");
        labelattribute = cfg.getAttributeValue(layerInfo,"labelattribute");
	Iterator it=null;

	// JAK attempted to make this work with enumerated attributes too
	if (enumatt!=null) {
	    NAttribute nat = codeElement.getAttributeByName(enumatt);
	    if (nat==null) {
		throw new Exception("Video labeller configuration error: Enumerated attribute '"+enumatt+"' does not exist for element '"+codeElement.getName()+"'.");
	    }
	    if (nat.getType()!=NAttribute.ENUMERATED_ATTRIBUTE) {
		throw new Exception("Video labeller configuration error: attribute '"+enumatt+"' on element '"+codeElement.getName()+"' is not enumerated.");
	    }
	    Debug.print("CVL config is attribute: " + enumatt, Debug.DEBUG);
	    this.attribute=nat;
	} else {
	    if (pointerrole == null)
		throw new Exception("pointerrole (and enumeratedattribute) attribute not found");
	    if (labelattribute == null)
		throw new Exception("labelattribute attribute not found");
	    
	    // find pointer
	    List pointers = codeElement.getPointers();
	    it = pointers.iterator();
	    while ((pointer == null) && it.hasNext()) {
		NPointer ptr = (NPointer)it.next();
		if (ptr.getRole().equals(pointerrole))
		    pointer = ptr;
	    }
	    if (pointer == null)
		throw new Exception("no pointer found for specified pointerrole");
	    
	    // find objectset or ontology
	    Document doc = Document.getInstance();
	    String targetSet = pointer.getTarget();
	    List objectSets = doc.getMetaData().getObjectSets();
	    it = objectSets.iterator();
	    while ((objectSet == null) && it.hasNext()) {
		NObjectSet set = (NObjectSet)it.next(); 
		if (targetSet.equals(set.getName()))
		    objectSet = set;
	    }
	    if (objectSet == null) {
		List ontologies = doc.getMetaData().getOntologies();
		it = ontologies.iterator();
		while ((ontology == null) && it.hasNext()) {
		    NOntology set = (NOntology)it.next();
		    if (targetSet.equals(set.getName()))
			ontology = set;
		}
	    }
	    if ((objectSet == null) && (ontology == null))
		throw new Exception("pointer does not point to an object set or ontology");
	}
	    
	elementFormatter = new LabelElementFormatter();
	    
        // create colour and style maps
        targetColourMap = ValueColourMap.getLocalColourMap(true);
        List targets = getTargets();
        it = targets.iterator();
        int i = 0;
        while (it.hasNext()) {
	    Object target = it.next();
            //NOMElement target = (NOMElement)it.next();
            Color textColour = targetColourMap.getValueTextColour(target);
            String styleName = "targetcolour" + i;
            targetStyleMap.put(target, styleName);
            TextStyleHandler styleHandler = new TextStyleHandler();
            styleHandler.init("", null);
            styleHandler.setName(styleName);
            styleHandler.makeNewStyle();
            Style style = styleHandler.getStyle();
            StyleConstants.setForeground(style,textColour);
            StyleConstants.setBackground(style,textColour);
            styleMap.put(styleName,style);
            i++;
        }
    }

    /**
     * <p>Returns the settable enumerated attribute as a NAttribute.</p>
     *
     * @return the attribute that gets set for an annotation
     */
    public NAttribute getEnumeratedAttribute() {
        return attribute;
    }
    
    /**
     * <p>Returns the pointer that points from an annotation to its target.</p>
     *
     * @return the pointer that points from an annotation to its target
     */
    public NPointer getPointer() {
        return pointer;
    }
    
    /**
     * <p>Returns the attribute of an object set or ontology element that
     * contains the label of the annotation target.</p>
     *
     * @return the label attribute
     */
    public String getLabelAttribute() {
        return labelattribute;
    }
    
    /**
     * <p>Returns the root ID of the target object set or ontology. The ID
     * consists of the name of the object set or ontology, a # character, and
     * the ID of the root element of the object set or ontology. If the
     * targets of this layer are in an ontology, the returned root ID can be
     * used to create an
     * {@link net.sourceforge.nite.gui.util.OntologyTreeView OntologyTreeView}.</p>
     *
     * @return the root ID of the target object set or ontology
     */
    public String getTargetRootID() {
        Document doc = Document.getInstance();
        NOMElement element = null;
        String setName = null;
        if (ontology != null) {
            setName = ontology.getName();
            element = doc.getCorpus().getRootWithColour(setName);
        } else {
            setName = objectSet.getName();
            element = doc.getCorpus().getRootWithColour(objectSet.getName());
        }
        String rootID = setName + "#" + element.getID();
        return rootID;
    }

    /**
     * <p>Returns a colour map that maps targets (instances of
     * {@link net.sourceforge.nite.nom.nomwrite.NOMElement NOMElement}) to
     * colours.</p>
     *
     * @return the colour map
     */
    public ValueColourMap getColourMap() {
        return targetColourMap;
    }

    /**
     * <p>Returns the possible targets of this layer. This method returns a
     * list with the elements in the object set or ontology that this layer
     * points to. The objects in the list are instances of
     * {@link net.sourceforge.nite.nom.nomwrite.NOMElement NOMElement}.</p>
     *
     * @return a list with the targets of the layer
     */
    public List getTargets() {
        if (objectSet != null) {
            return getElements(objectSet);
	} else if (ontology!=null) {
            return getElements(ontology);
	} else {
	    return attribute.getEnumeratedValues();
	}
    }

    /**
     * Returns a list with the elements in an object set. The objects in the
     * list are instances of NOMElement.
     */
    private List getElements(NObjectSet set) {
        ArrayList elements = new ArrayList();
        Document doc = Document.getInstance();
        NOMElement element = doc.getCorpus().getRootWithColour(set.getName());
        while (element.hasNextElement()) {
            element = element.getNextElement();
            elements.add(element);
        }
        return elements;
    }

    /**
     * Returns a list with the elements in an ontology. The objects in the list
     * are instances of NOMElement.
     */
    private List getElements(NOntology ontology) {
        ArrayList elements = new ArrayList();
        Document doc = Document.getInstance();
        NOMElement element = doc.getCorpus().getRootWithColour(ontology.getName());
        while (element.hasNextElement()) {
            element = element.getNextElement();
            elements.add(element);
        }
        return elements;
    }

    /**
     * <p>Returns a hash map that maps style names to styles.</p>
     *
     * @return a hash map that maps style names to styles
     */
    public HashMap getStyleMap() {
        return styleMap;
    }
        
    /**
     * <p>Returns the label of the specified target element. The target element
     * should have an attribute with the name of the labelattribute specified
     * in the layerinfo element from the configuration file. This method
     * returns the value of that attribute. If there is no such attribute,
     * this method returns null.</p>
     *
     * @return the label of the specified target element or null
     */
    public String getTargetName(NOMElement target) {
        NOMAttribute attribute = target.getAttribute(labelattribute);
        if (attribute == null)
            return null;
        return attribute.getStringValue();
    }
    
    /**
     * <p>Returns the target of the specified annotation. If the annotation
     * does not have a target or the target could not be retrieved, this
     * method returns null.</p>
     */
    public Object getTarget(NOMElement annotation) {
	if (attribute!=null) {
	    return annotation.getAttributeComparableValue(attribute.getName());
	} else {
	    NOMPointer ptr = annotation.getPointerWithRole(pointer.getRole());
	    if (ptr == null)
		return null;
	    return ptr.getToElement();
	}
    }
    
    /**
     * <p>Returns the label of an annotation in this layer. If the label
     * could not be retrieved, this method returns null.</p>
     *
     * @param annotation an annotation in this layer
     * @return the label of the specified annotation or null
     */
    public String getLabel(NOMElement annotation) {
	if (attribute!=null) {
	    return (String)annotation.getAttributeComparableValue(attribute.getName());
	}
        Object target = getTarget(annotation);
        if (target == null)
            return null;
	if (target instanceof String) { return (String)target; }
	else if (target instanceof NOMElement) { return getTargetName((NOMElement)target); }
	else { return null; }
    }
    
    /**
     * <p>Splits an annotation, so there will be a gap between the specified
     * start time and end time. The specified annotation must start before
     * the start time and it must end after the end time.</p>
     *
     * @param annotation the annotation that will be split
     * @param startTime the time where the gap will start
     * @param endTime the time where the gap will end
     * @exception Exception if an error occurs
     */
    public void createGap(NOMElement annotation, double startTime, double endTime) throws Exception {
        Document doc = Document.getInstance();
        NOMPointer ptr = annotation.getPointerWithRole(pointer.getRole());
        NOMElement elemTarget = null;
        if (ptr != null)
            elemTarget = ptr.getToElement();
        NOMWriteElement newElement = new NOMWriteAnnotation(doc.getCorpus(),
                codeElement.getName(),
                doc.getObservation().getShortName(),
                annotation.getAgent());
        if (elemTarget != null) {
            NOMPointer point = new NOMWritePointer(doc.getCorpus(),pointer.getRole(),newElement,elemTarget);
            newElement.addPointer(point);
        }
        newElement.setStartTime(endTime);
        newElement.setEndTime(annotation.getEndTime());
        newElement.addToCorpus();
        annotation.setEndTime(startTime);
    }

    /**
     * <p>Compares two annotations and returns true if they have the same
     * target. If the two annotations are adjacent, it means they can be
     * merged into one annotation.</p>
     *
     * @param elem1 the first annotation
     * @param elem2 the second annotation
     * @return true if both annotations have the same target, false otherwise
     */
    public boolean sameTargets(NOMElement elem1, NOMElement elem2) {
        if ((elem1 == null) || (elem2 == null))
            return false;
        NOMPointer pointer1 = elem1.getPointerWithRole(pointer.getRole());
        NOMPointer pointer2 = elem2.getPointerWithRole(pointer.getRole());
        NOMElement target1 = null;
        if (pointer1 != null)
            target1 = pointer1.getToElement();
        NOMElement target2 = null;
        if (pointer2 != null)
            target2 = pointer2.getToElement();
        if ((target1 == null) && (target2 == null))
            return true;
        else if ((target1 == null) || (target2 == null))
            return false;
        else
            return target1.equals(target2);
    }

    /**
     * Element formatter for the LabelAnnotationLayer.
     */
    private class LabelElementFormatter extends GenericElementFormatter  {
        public void showElement(NOMWriteElement nwe, NTextArea nta) {
            double start = nwe.getStartTime();
            double end = nwe.getEndTime();
            double duration = end - start;

            String text  = " [ ";
            String label = getLabel(nwe);
            if (label == null)
                label = "";
            String text2 = label;
            if (text2.length() == 0)
                text2 = "EMPTY";
            if (end > 0.0) {
                for (int i = 0; i < duration*2; i++) {
                    text2 += ".";
                }
            } else {
                text2 += " UNFINISHED";
            }
            String text3 = " ]\n";
            NTextElement nte = new NTextElement(text,
                    "", nwe.getStartTime(), nwe.getEndTime());
            NOMObjectModelElement nome= new NOMObjectModelElement(nwe);
            nte.setDataElement((ObjectModelElement)nome);
            nta.addElement(nte);

	    Object target = getTarget(nwe);
            String styleName = "";
            if (target != null)
                styleName = (String)targetStyleMap.get(target);
            NTextElement nte2 = new NTextElement(text2, 
                    styleName,
                    nwe.getStartTime(), nwe.getEndTime());
            nte2.setDataElement((ObjectModelElement)nome);
            nta.addElement(nte2);

            NTextElement nte3 = new NTextElement(text3, 
                    "", nwe.getStartTime(), nwe.getEndTime());
            nte3.setDataElement((ObjectModelElement)nome);
            nta.addElement(nte3);
        }                       
    }
}
