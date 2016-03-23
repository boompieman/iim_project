package net.sourceforge.nite.tools.videolabeler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Vector;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import net.sourceforge.nite.gui.util.AgentConfiguration;
import net.sourceforge.nite.gui.util.CheckSave;
import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NElement;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.meta.NCoding;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.meta.NObjectSet;
import net.sourceforge.nite.meta.NObservation;
import net.sourceforge.nite.meta.NOntology;
import net.sourceforge.nite.meta.NPointer;
import net.sourceforge.nite.meta.impl.NiteMetaData;
import net.sourceforge.nite.meta.impl.NiteMetaException;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMAttribute;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMPointer;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteAnnotation;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteCorpus;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWritePointer;
import net.sourceforge.nite.nxt.NOMObjectModelElement;
import net.sourceforge.nite.nxt.ObjectModelElement;
import net.sourceforge.nite.search.Engine;
import net.sourceforge.nite.time.Clock;
import net.sourceforge.nite.time.ClockFace;
import net.sourceforge.nite.time.DefaultClock;

import org.w3c.dom.Node;

/**
 * <p>A document is a global singleton object. Before it is used it should be
 * created with
 * {@link #createInstance(java.lang.String, java.lang.String, java.lang.String)
 * createInstance()}. From then on the same document can be retrieved with
 * {@link #getInstance() getInstance()}.</p>
 *
 * <p>The document makes use of the NITE XML Toolkit. It is assumed that you
 * are familiar with the concepts of this toolkit. A document is created with
 * the path to a metadata file, the name of an observation and the name of an
 * annotator. The metadata and observation data for the specified annotator will
 * be read into this document object. The metadata can later be retrieved with
 * {@link #getMetaData() getMetaData()} and the observation is retrieved with
 * {@link #getObservation() getObservation()}.</p>
 *
 * <p>There are three base methods to obtain the available agents, layers and
 * signals from the metadata. These methods are
 * {@link #getAgents() getAgents()}, {@link #getLayers() getLayers()} and
 * {@link #getSignals() getSignals()}. The document only returns layers for
 * which there is a <code>layerinfo</code> element in the configuration
 * file.</p>
 *
 * <p>The only possible document changes are creating, adding and deleting
 * annotations. The respective methods are
 * {@link #createAnnotation(java.lang.String, double, net.sourceforge.nite.meta.NAgent) createAnnotation()},
 * {@link #insertAnnotation(net.sourceforge.nite.tools.videolabeler.AnnotationLayer, net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement) insertAnnotation}
 * and
 * {@link #deleteAnnotation(net.sourceforge.nite.tools.videolabeler.AnnotationLayer, net.sourceforge.nite.nxt.NOMObjectModelElement) deleteAnnotation()}.
 * These methods ensure that the annotations do not overlap. If the GUI setting
 * {@link CSLConfig#makeContinuous() makeContinuous} is true, it is also ensured
 * that the annotations remain continuous, i.e. there are no gaps between
 * them.</p>
 *
 * <p>The document is {@link java.util.Observable Observable}. Observers (see
 * {@link java.util.Observer Observer} can register themselves with the document
 * to be notified of any changes. When the document notifies an observer of a
 * change, it will pass an argument, which is a {@link java.util.List List}
 * that consists of two {@link java.lang.Double Double} objects. The first one
 * contains a start time, the second an end time. The document changed between
 * that start and end time.</p>
 *
 * <p>The document is saved with {@link #save() save()}. To check whether a
 * document has been saved and show a prompt if not, use
 * {@link #checkSave() checkSave()}.</p>
 */
public class Document extends Observable {
    private static Document instance = null;

    private String metadataFile;
    private NiteMetaData metadata;
    private NObservation observation;
    private NOMWriteCorpus corpus;
    private Engine searchEngine = new Engine();
    private List layers = null;
    private String annotatorName=null;
    private Clock clock;

    /**
     * Constructs a new document. The metadata is read from the metadata file
     * and the observation data is read into the corpus.
     */
    private Document(String metadataFile, String observationName, String annotatorName) throws NiteMetaException, NOMException {
        this.metadataFile = metadataFile;
	    this.annotatorName = annotatorName;
        metadata = new NiteMetaData(metadataFile);
        if (metadata.getAgentAttributeName() == null)
            metadata.setAgentAttributeName("who");
        if (metadata.getCorpusType() == NMetaData.STANDOFF_CORPUS) {
            System.out.println("This is a standoff corpus: NOM being loaded");
            corpus = new NOMWriteCorpus(metadata);
            observation = metadata.getObservationWithName(observationName);
	        forceAnnotatorSpecificCodings();
            corpus.loadData(observation);
            clock = new DefaultClock(metadata,observationName);//DR 2005.05.18: clockface should have these two arguments to be able to show signal selection dropdownbox
            JInternalFrame disp = clock.getDisplay();
            if ((disp != null) && (disp instanceof ClockFace)) {
                ClockFace clockFace = (ClockFace)disp;
                clockFace.setSyncRate(CSLConfig.getInstance().getSyncRate());
                System.out.println("set syncrate " + CSLConfig.getInstance().getSyncRate());
            }
            System.out.println("Finished loading.");
        } else {
            throw new NiteMetaException("This is a standalone or simple corpus: no NOM has been loaded");
        }
    }

    /**
     * <p>Initialises the singleton document. This method reads the metadata
     * from the specified metadata file and the data for the specified
     * observation into the document.</p>
     *
     * <p>If the singleton document has already been initialised, this method
     * does not create a new document, but simple returns the existing
     * singleton document.</p>
     *
     * @param metadata the path to the metadata file
     * @param observation the name of the observation
     * @return the singleton document
     * @exception NiteMetaException if the metadata could not be loaded
     * @exception NOMException if the observation data could not be loaded
     */
    public static Document createInstance(String metadata, String observation, String annotator) throws NiteMetaException, NOMException {
        if (instance == null)
            instance = new Document(metadata,observation,annotator);
        return instance;
    }

    /**
     * <p>Returns the singleton document. The document should have been
     * initialised first with
     * {@link #createInstance(java.lang.String, java.lang.String, java.lang.String)
     * createInstance()}. If the document has not been initialised yet, this
     * method returns null.</p>
     *
     * @return the singleton document or null
     */
    public static Document getInstance() {
        return instance;
    }

    /**
     * <p>Returns the corpus. Normally the corpus should not be edited outside
     * this class. The methods createAnnotation, insertAnnotation and
     * deleteAnnotation ensure that annotations remain non-overlapping and
     * continuous, and that observers are notified.</p>
     *
     * @return the corpus
     */
    public NOMWriteCorpus getCorpus() {
        return corpus;
    }

    /**
    * <p>Returns the metadata.</p>
    *
    * @return the metadata
    */
    public NMetaData getMetaData() {
        return metadata;
    }

    /**
     * <p>Returns the observation.</p>
     *
     * @return the observation
     */
    public NObservation getObservation() {
        return observation;
    }

    /**
     * <p>Returns a list with the available agents. The objects in the list are
     * instances of {@link net.sourceforge.nite.meta.NAgent NAgent}.</p>
     *
     * @return a list with the available agents
     */
    public List getAgents() {
        return metadata.getAgents();
    }
    
    /**
     * <p>Returns the clock for this document.</p>
     *
     * @return the clock
     */
    public Clock getClock() {
        return clock;
    }

    /** added by jonathan 21.4.05.
     * Called pre-loadData, this makes sure we have the correct
     * annotator-specific data (i.e. any time-aligned layer we have
     * config for and thus any one that appears in the list of
     * layers). This can't be done at the same time as getLayers
     * though the code is similar. We can't call getLayers before
     * loadData as the as AnnotationLayer constructor throws an
     * exception. */
    private void forceAnnotatorSpecificCodings() {
	if (annotatorName==null) { return; }
	CSLConfig cfg = CSLConfig.getInstance();
	List nxtLayers = metadata.getLayersByType(NLayer.TIMED_LAYER);
	for (int i = 0; i < nxtLayers.size(); i++) {
	    NLayer layer = (NLayer)nxtLayers.get(i);
	    Node layerInfo = cfg.getLayerInfo(layer.getName());
	    if (layerInfo != null) {
		if (layer.getContainer() instanceof NCoding) {
		    try {
			corpus.forceAnnotatorCoding(annotatorName, ((NCoding)layer.getContainer()).getName());
		    } catch (NOMException nex) {
			System.err.println("FAILED TO FORCE ANNOTATOR SPECIFIC LAYER\n");
		    }
		}
	    }
        }
    }
	
    /**
     * <p>Returns a list with the available annotation layers. This method
     * iterates through the list of all time-aligned layers defined in the
     * metadata. For each layer it is checked whether there is a layerinfo
     * element in the configuration file (see
     * {@link CSLConfig#getLayerInfo(java.lang.String) CSLConfig.getLayerInfo()}).
     * If so, the {@link AnnotationLayerFactory AnnotationLayerFactory} is used
     * to create an annotation layer (instance of
     * {@link AnnotationLayer AnnotationLayer}). This method returns a list of
     * those annotation layers. If an error occurs, a message is printed to
     * standard output.</p>
     *
     * @return a list with the available annotation layers (instances of
     * {@link AnnotationLayer AnnotationLayer})
     */
    public List getLayers() {
        if (layers == null) {
            CSLConfig cfg = CSLConfig.getInstance();
            layers = new ArrayList();
            List nxtLayers = metadata.getLayersByType(NLayer.TIMED_LAYER);
            AnnotationLayerFactory alf = AnnotationLayerFactory.getInstance();
            for (int i = 0; i < nxtLayers.size(); i++) {
                NLayer layer = (NLayer)nxtLayers.get(i);
                Node layerInfo = cfg.getLayerInfo(layer.getName());
                if (layerInfo != null) {
                    String layerCls = cfg.getLayerClass(layerInfo);
                    String codeName = cfg.getLayerCodeName(layerInfo);
                    String panelCls = cfg.getLayerControlPanelClass(layerInfo);
                    boolean ok = layerCls != null;
                    if (!ok)
                        System.out.println("ERROR: layerinfo without layerclass attribute");
                    ok &= codeName != null;
                    if (!ok)
                        System.out.println("ERROR: layerinfo without codename attribute");
                    ok &= panelCls != null;
                    if (!ok)
                        System.out.println("ERROR: layerinfo without controlpanelclass attribute");
                    if (ok) {
                        AnnotationLayer annLayer = alf.createAnnotationLayer(
                            layer,layerCls,codeName,panelCls,layerInfo);
                        if (annLayer != null)
                            layers.add(annLayer);
                    }
                }
            }
        }
        return layers;
    }

    /**
     * <p>Returns a list with the available signals. The objects in the list are
     * instances of {@link net.sourceforge.nite.meta.NSignal NSignal}.</p>
     *
     * @return a list with the available signals
     */
    public List getSignals() {
        return metadata.getSignals();
    }

    /**
     * <p>Performs a search in the corpus. This method takes a query in the
     * NITE Query Language. It should specify only one variable for the search
     * results. This method returns a simple list of annotations (instances of
     * {@link net.sourceforge.nite.nom.nomwrite.NOMElement NOMElement}) rather
     * than the more complex result of
     * {@link net.sourceforge.nite.search.Engine#search(net.sourceforge.nite.search.SearchableCorpus, java.lang.String) Engine.search()}.
     * The annotations will be sorted on their start times.</p>
     *
     * @param query a query in the NITE Query Language
     * @return a sorted list of annotations
     * @exception Throwable if an error occurs while searching the corpus
     */
    public List searchAnnotations(String query) throws Throwable {
        List sortedElems = new ArrayList();
        List elems = searchEngine.search(corpus,query);
        for (int i = 1; i < elems.size(); i++) {
            List searchItem = (List)elems.get(i);
            NOMElement elem = (NOMElement)searchItem.get(0);
            int insertIndex = sortedElems.size();
            boolean found = false;
            while (!found && insertIndex > 0) {
                NOMElement cmpElem = (NOMElement)sortedElems.get(insertIndex-1);
                if (elem.getStartTime() < cmpElem.getStartTime()) {
                    insertIndex--;
                } else if (elem.getStartTime() == cmpElem.getStartTime()) {
                    if (Double.isNaN(elem.getEndTime()) && Double.isNaN(cmpElem.getEndTime())) {
                        found = true;
                    } else if (Double.isNaN(elem.getEndTime())) {
                        if (elem.getStartTime() < cmpElem.getEndTime())
                            insertIndex--;
                        else // elem.getStartTime() == cmpElem.getEndTime()
                            found = true;
                    } else if (Double.isNaN(cmpElem.getEndTime())) {
                        if (elem.getEndTime() == cmpElem.getStartTime())
                            insertIndex--;
                        else // elem.getEndTime() > cmpElem.getStartTime()
                            found = true;
                    } else {
                        if (elem.getEndTime() < cmpElem.getEndTime())
                            insertIndex--;
                        else
                            found = true;
                    }
                } else { // elem.getStartTime() > cmpElem.getStartTime()
                    found = true;
                }
            }
            sortedElems.add(insertIndex,elem);
        }
        return sortedElems;
    }
    
    /**
     * <p>Notifies all observers that the document has changed between the
     * specified start and end time.</p>
     */
    public void notifyObservers(double start, double end) {
        List args = new ArrayList();
        args.add(new Double(start));
        args.add(new Double(end));
        setChanged();
        notifyObservers(args);
    }

    /**
     * <p>Creates a new annotation for the specified code name, start time and
     * agent. The annotation without an end time (end time is Double.NaN) is
     * added to the corpus. When the end time is set, call insertAnnotation. If
     * an error occurs, this method throws an exception and the document won't
     * be changed.</p>
     *
     * @param code the code name
     * @param startTime the start time
     * @param agent the agent
     * @return the new annotation
     * @exception NOMException if an error occurs
     */
    public NOMWriteElement createAnnotation(String code, double startTime, NAgent agent)
    throws NOMException {
        NOMWriteAnnotation ann = null;
        try {
            ann = new NOMWriteAnnotation(corpus,code,observation.getShortName(),agent);
            ann.setStartTime(startTime);
            ann.addToCorpus();
            notifyObservers(startTime,startTime);
            return ann;
        } catch (NOMException ex) {
            if (ann != null) {
                NOMObjectModelElement nome = new NOMObjectModelElement(ann);
                nome.deleteElement();
            }
            throw ex;
        }
    }
	
    /**
     * <p>Inserts a new annotation into the corpus. The annotation must have
     * been created with
     * {@link #createAnnotation(java.lang.String, double, net.sourceforge.nite.meta.NAgent)
     * createAnnotation()}. The annotation must have a valid start time and end
     * time! This method does not really add the annotation, as this was already
     * done in <code>createAnnotation()</code>. This method only enforces some
     * constraints as described below.</p>
     *
     * <p>Because the annotations cannot overlap, it may be necessary to make
     * space for the new annotation by deleting (parts of) existing annotations
     * in the period between the specified start and end time. This method
     * ensures that annotations will not overlap.</p>
     *
     * <p>If {@link CSLConfig#makeContinuous()
     * CSLConfig.makeContinuous()}</code> returns true, this method also ensures
     * that there are no gaps in the time line. This would happen if the start
     * time of the new annotation is greater than the end time of the previous
     * annotation, or if the end time of the new annotation is less than the
     * start time of the next annotation. In these cases, the start or end time
     * of the new annotation is changed so the annotation is connected to the
     * previous or next annotation.</p>
     *
     * <p>As a result there may be two annotations with the same target next to
     * each other. In that case the annotations are merged. Note therefore that
     * the specified annotation may be edited or deleted altogether!</p>
     *
     * DR:05.17.2005 merging made dependent on gui config var 'merge'
     *
     * <p>If <code>makeContinuous</code> is false, this method will execute
     * one corpus search. If <code>makeContinuous</code> is true, it will
     * execute three corpus searches.</p>
     *
     * @param layer the annotation layer
     * @param annotation the annotation to be inserted
     * @exception Throwable if the annotation cannot be inserted
     */
    public void insertAnnotation(AnnotationLayer layer, NOMWriteElement annotation) throws Throwable {
        boolean doMerge = CSLConfig.getInstance().merge();
        NAgent agent = annotation.getAgent();
        double startTime = annotation.getStartTime();
        double endTime = annotation.getEndTime();

        double startChange = annotation.getStartTime();
        double endChange = annotation.getEndTime();
        String query = "($a " + annotation.getName() + "): start($a) <= \"" +
                endTime + "\" && end($a) >= \"" + startTime + "\"";
        if (annotation.getAgent() != null) {
            query += " && $a@" + metadata.getAgentAttributeName() + " == \"" +
                    annotation.getAgentName() + "\"";
        }
        List elems = searchAnnotations(query);
        removeUnfinishedAnnotations(elems);
        elems.remove(annotation);
        Iterator it = elems.iterator();
        NOMWriteAnnotation lastBefore = null;
        NOMWriteAnnotation firstAfter = null;
        while (it.hasNext()) {
            NOMWriteAnnotation elem = (NOMWriteAnnotation)it.next();
            if (elem.getEndTime() == startTime) {
                if (layer.sameTargets(elem,annotation) && (lastBefore == null ||
                        elem.getStartTime() > lastBefore.getStartTime())) {
                    lastBefore = elem;
                }
            } else if (elem.getStartTime() < startTime && elem.getEndTime() <= endTime) {
                elem.setEndTime(startTime);
                startChange = elem.getStartTime();
                if (layer.sameTargets(elem,annotation)) {
                    if(doMerge)annotation = merge(elem,annotation);
                }
            } else if (elem.getStartTime() < startTime) {
                startChange = elem.getStartTime();
                endChange = elem.getEndTime();
                if (layer.sameTargets(elem,annotation)) {
                    NOMObjectModelElement nome = new NOMObjectModelElement(annotation);
                    nome.deleteElement();
                } else {
                    layer.createGap(elem,startTime,endTime);
                }
            } else if (elem.getStartTime() == endTime) {
                if (layer.sameTargets(annotation,elem) && (firstAfter == null ||
                        elem.getEndTime() < firstAfter.getEndTime())) {
                    firstAfter = elem;
                }
            } else if (elem.getEndTime() <= endTime) {
                NOMObjectModelElement nome = new NOMObjectModelElement(elem);
                nome.deleteElement();
            } else {
                elem.setStartTime(endTime);
                endChange = elem.getEndTime();
                if (layer.sameTargets(annotation,elem)) {
                    if(doMerge)annotation = merge(annotation,elem);
                }
            }
        }
        if (lastBefore != null) {
            startChange = lastBefore.getStartTime();
            if(doMerge)annotation = merge(lastBefore,annotation);
        }
        if (firstAfter != null) {
            endChange = firstAfter.getEndTime();
            if(doMerge)annotation = merge(annotation,firstAfter);
        }

        boolean makeContinuous = CSLConfig.getInstance().makeContinuous();
        if (makeContinuous) {
            NOMWriteAnnotation elem = getElementBefore(annotation);
            if (elem != null) { // connect new annotation to previous annotation
                startChange = elem.getEndTime();
                annotation.setStartTime(elem.getEndTime());
                if (layer.sameTargets(elem,annotation))
                    if(doMerge)annotation = merge(elem,annotation);
            }
            elem = getElementAfter(annotation);
            if (elem != null) { // connect new annotation to next annotation
                endChange = elem.getStartTime();
                annotation.setEndTime(elem.getStartTime());
                if (layer.sameTargets(annotation,elem))
                    if(doMerge)annotation = merge(annotation,elem);
            }
        }
        notifyObservers(startChange,endChange);
    }

    /**
     * <p>Deletes an annotation from the corpus.</p>
     * 
     * <p>If {@link CSLConfig#makeContinuous() CSLConfig.makeContinuous()}
     * returns true, this method tries to fill the resulting space in the time
     * line with the previous or next annotation. If there is a previous
     * annotation, its end time will be set to the end time of the deleted
     * annotation. If there is no previous annotation, but there is a next
     * annotation, its start time will be set to the start time of the deleted
     * annotation.</p>
     *
     * <p>As a result there may be two annotations with the same target next to
     * each other. In that case the annotations are merged.</p>
     *
     * <p>If <code>makeContinuous</code> is false and the deleted annotation
     * has a duration greater than zero, this method will perform no corpus
     * search. Otherwise it will perform two corpus searches.</p>
     *
     * @param layer the layer that contains the specified annotation
     * @param annotation the annotation to be deleted
     * @exception NOMException if an error occurs while merging two annotations
     */
    public void deleteAnnotation(AnnotationLayer layer, NOMObjectModelElement annotation) throws Throwable {
        NOMWriteAnnotation nwa = (NOMWriteAnnotation) annotation.getElement();
        double startTime = nwa.getStartTime();
        double endTime = nwa.getEndTime();
        annotation.deleteElement();
        
        boolean keepContinuous = CSLConfig.getInstance().makeContinuous();
        if (!keepContinuous && (startTime < endTime)) { // can't fill or merge
            notifyObservers(startTime,endTime);
            return;
        }
        // keepContinuous || (startTime == endTime)
        
        // try to find a match of a previous and a next element, so they may
        // be merged
        List prev = getElementsConnectedBefore(nwa);
        List next = getElementsConnectedAfter(nwa);
        if (prev.size() > 1)
            removeNonZeroAnnotations(prev);
        if (next.size() > 1)
            removeNonZeroAnnotations(next);
        NOMWriteElement first = null;
        NOMWriteElement second = null;
        Iterator prevIt = prev.iterator();
        while ((first == null) && prevIt.hasNext()) {
            NOMWriteElement prevElem = (NOMWriteElement)prevIt.next();
            Iterator nextIt = next.iterator();
            while ((first == null) && nextIt.hasNext()) {
                NOMWriteElement nextElem = (NOMWriteElement)nextIt.next();
                if (layer.sameTargets(prevElem,nextElem)) {
                    first = prevElem;
                    second = nextElem;
                }
            }
        }
        if (first != null) { // found a match: fill and merge
            if (startTime < endTime)
                first.setEndTime(endTime);
            // first is connected to second
            NOMWriteElement result = merge(first,second);
            notifyObservers(result.getStartTime(),result.getEndTime());
            return;
        } else if (startTime < endTime) { // no match: try to fill with arbitrary annotation
            if (prev.size() > 0) {
                NOMWriteElement elem = (NOMWriteElement)prev.get(0);
                elem.setEndTime(endTime);
                notifyObservers(elem.getStartTime(),endTime);
                return;
            } else if (next.size() > 0) {
                NOMWriteElement elem = (NOMWriteElement)next.get(0);
                elem.setStartTime(startTime);
                notifyObservers(startTime,elem.getEndTime());
                return;
            }
        }
        notifyObservers(startTime,endTime);
    }
    
    /**
     * <p>Removes all annotations that are longer than zero (i.e. start time
     * does not equals end time). The specified list should contain instances
     * of NOMWriteElement.</p>
     */
    private void removeNonZeroAnnotations(List elems) {
        int i = 0;
        while (i < elems.size()) {
            NOMWriteElement elem = (NOMWriteElement)elems.get(i);
            if (elem.getStartTime() != elem.getEndTime())
                elems.remove(i);
            else
                i++;
        }
    }

    /**
     * <p>Removes all unfinished annotations (without an end time) from
     * the specified list of annotations (instances of NOMWriteElement).</p>
     */
    private void removeUnfinishedAnnotations(List elems) {
        int i = 0;
        while (i < elems.size()) {
            NOMWriteElement elem = (NOMWriteElement)elems.get(i);
            if (Double.isNaN(elem.getEndTime()))
                elems.remove(i);
            else
                i++;
        }
    }
	
    /**
     * <p>Removes all finished annotations (with an end time) from the specified
     * list of annotations (instances of NOMWriteElement).</p>
     */
    private void removeFinishedAnnotations(List elems) {
        int i = 0;
        while (i < elems.size()) {
            NOMWriteElement elem = (NOMWriteElement)elems.get(i);
            if (!Double.isNaN(elem.getEndTime()))
                elems.remove(i);
            else
                i++;
        }
    }
    
    /**
     * <p>Returns the last element e so that end(e) <= start(elem). If there is
     * no such element, this method returns null.</p>
     */
    private NOMWriteAnnotation getElementBefore(NOMElement elem) throws Throwable {
        String query = "($a " + elem.getName() + "): (end($a) <= \"" + elem.getStartTime() + "\")";
        if (elem.getAgent() != null) {
            query += " && $a@" + metadata.getAgentAttributeName() + " == \"" + elem.getAgentName() + "\"";
        }
        List elements = searchAnnotations(query);
        removeUnfinishedAnnotations(elements);
        if (elements.size() > 0)
            return (NOMWriteAnnotation)elements.get(elements.size()-1);
        else
            return null;
    }

    /**
     * <p>Returns the first element e so that start(e) >= end(elem). If there is
     * no such element, this method returns null.</p>
     */
    private NOMWriteAnnotation getElementAfter(NOMElement elem) throws Throwable {
        String query = "($a " + elem.getName() + "): (start($a) >= \"" + elem.getEndTime() + "\")";
        if (elem.getAgent() != null) {
            query += " && $a@" + metadata.getAgentAttributeName() + " == \"" + elem.getAgentName() + "\"";
        }
        List elements = searchAnnotations(query);
        removeUnfinishedAnnotations(elements);
        if (elements.size() > 0)
            return (NOMWriteAnnotation)elements.get(0);
        else
            return null;
    }

    /**
     * <p>Returns all elements whose end time is the same as the specified
     * element's start time.</p>
     */
    private List getElementsConnectedBefore(NOMElement elem) throws Throwable {
        String query = "($a " + elem.getName() + "): (end($a) == \"" + elem.getStartTime() + "\")";
        if (elem.getAgent() != null) {
            query += " && $a@" + metadata.getAgentAttributeName() + " == \"" + elem.getAgentName() + "\"";
        }
        List elems = searchAnnotations(query);
        removeUnfinishedAnnotations(elems);
        return elems;
    }
    
    /**
     * <p>Returns all elements whose start time is the same as the specified
     * element's end time.</p>
     */
    private List getElementsConnectedAfter(NOMElement elem) throws Throwable {
        String query = "($a " + elem.getName() + "): (start($a) == \"" + elem.getEndTime() + "\")";
        if (elem.getAgent() != null) {
            query += " && $a@" + metadata.getAgentAttributeName() + " == \"" + elem.getAgentName() + "\"";
        }
        List elems = searchAnnotations(query);
        removeUnfinishedAnnotations(elems);
        return elems;
    }

    /**
     * <p>Returns a list with all unfinished annotations (without an end time)
     * for the specified layer and agent. If the layer contains an interaction
     * coding rather than an agent coding, the specified agent should be
     * null.</p>
     *
     * @param agent an agent or null
     * @param layer an annotation layer
     */
    private List getUnfinishedAnnotations(NAgent agent, AnnotationLayer layer) throws Throwable {
        String code = layer.getCodeElement().getName();
        String query = "($a " + code + ")";
        if (agent != null)
            query += ": $a@" + metadata.getAgentAttributeName() + " == \"" + agent.getShortName() + "\"";
        List elems = searchAnnotations(query);
        removeFinishedAnnotations(elems);
        return elems;
    }

    /**
     * <p>Merges the first annotation with the second annotation (the first
     * annotation should come before the second annotation in the time
     * line).</p>
     *
     * @param first the first annotation
     * @param second the second annotation
     * @return the merged annotation
     */
    private NOMWriteElement merge(NOMWriteElement first, NOMWriteElement second)
    throws NOMException {
        first.setEndTime(second.getEndTime());
        NOMObjectModelElement temp = new NOMObjectModelElement(second);
        temp.deleteElement();
        return first;
    }
    
    /**
     * <p>Saves the document (metadata and corpus).</p>
     *
     * @exception NOMException if the document could not be saved
     */
    public void save() throws NOMException {
        metadata.writeMetaData(metadataFile);
        corpus.serializeCorpusChanged();
    }

    /**
     * <p>Checks whether the document has been saved. If yes, this method
     * returns true. If not, this method shows a dialogue window to ask the user
     * whether the document should be saved. If the user clicks "Yes", the
     * document will be saved and this method returns true. If the user clicks
     * "No", the document will not be saved, but this method still returns true.
     * If the user clicks "Cancel", the document will not be saved and this
     * method returns false.</p>
     *
     * <p>This method should be called before the application is closed. If
     * this method returns true, the application can be closed.</p>
     *
     * @return true if the application can be closed (the document was saved or
     * the user does not want to save it), false otherwise
     */
    public boolean checkSave() {
        CheckSave co = new CheckSave(corpus); 
        return (co.popupDialog() != JOptionPane.CANCEL_OPTION);
    }
}
