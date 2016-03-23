/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;
import java.util.Set;
import net.sourceforge.nite.meta.impl.NiteMetaException;

/**
 * Handles the metadata associated with a corpus: during validation
 * this structure stores important information about the parsed
 * metadata file.
 *
 * @author jonathan 
 */
public interface NMetaData {
    public static final int SIMPLE_CORPUS=1;
    public static final int STANDOFF_CORPUS=2;
    public static final int LTXML1_LINKS=1;
    public static final int XPOINTER_LINKS=2;

    /** returns the corpus type - one of SIMPLE_CORPUS or STANDOFF_CORPUS */
    public int getCorpusType();

    /** returns a List of "NCoding"s in the corpus */
    public List getCodings();

    /** get the NCoding with the given name or null if it doesn't exist */ 
    public NCoding getCodingByName(String name);

    /** get the NFile with the given name or null if it doesn't exist */ 
    public NFile getNFileByName(String name);

    /** get the NLayer with the given name or null if it doesn't exist */ 
    public NLayer getLayerByName(String name);

    /** get the NObjectSet with the given name or null if it doesn't exist */ 
    public NObjectSet getObjectSetByName(String name);

    /** get the NOntology with the given name or null if it doesn't exist */ 
    public NOntology getOntologyByName(String name);

    /** get the NCorpusResource with the given name or null if it doesn't exist */ 
    public NCorpusResource getCorpusResourceByName(String name);

    /** returns a List of "NAgent"s */
    public List getAgents();

    /** returns a List of all the "NElement"s in all the codings in
        the corpus */
    public List getAllElements();

    /** returns a List of "NSignal"s */
    public List getSignals();
    /** Returns a list of NAttributes since the data is exactly the same */
    public List getObservationVariables();
    /** returns a List of "NObservation"s */
    public List getObservations();
    /** returns the named observation or null if it doesn't exist */
    public NObservation getObservationWithName(String obsname);
    /** returns a List of "NOntology"s */
    public List getOntologies();
    /** returns a List of "NObjectSet"s */
    public List getObjectSets();
    /** returns a List of "NCorpusResource"s */
    public List getCorpusResources();
    /** returns a single NResource or null if there's no resource file */
    public NResourceData getResourceData();

    /** returns a List of "NCallableProgram"s - programs that can be
     * called on this corpus */
    public List getPrograms();

    /** returns a List of "NDataViews"s */
    public List getDataViews();
    /** returns a List of "NStyle"s */
    public List getStyles();
    /** returns an "NStyle" with the given name, or null*/
    public NStyle getStyleWithName(String name);

    /** returns a List of "NStylesheet"s 
    public List getStylesheets();
    ** returns a List of "NAnnotationSpec"s 
	public List getAnnotationSpecs(); */

    /** get the name of the reserved attribute for start times in this
        corpus */
    public String getStartTimeAttributeName();
    /** get the name of the reserved attribute for end times in this
        corpus */
    public String getEndTimeAttributeName();
    /** get the name of the reserved attribute for IDs in this corpus */
    public String getIDAttributeName();
    /** get the name of the reserved attribute for agents in this corpus */
    public String getAgentAttributeName();
    /** get the name of the reserved attribute for observations in this corpus */
    public String getObservationAttributeName();
    /** Return the name of the reserved resource attribute for the
        corpus - this attribute will not be expected on the input
        files but will be added to all elements on import. If the
        value is null (i.e. there is no 'resourcename' element in
        the metadata file), no resource attributes are added. This
        is a convenience function to allow access to resources from
        the query language. */
    public String getResourceAttributeName();
    /** get the name of the reserved attribute for Graphical Visual
        Markup in this corpus */
    public String getGVMAttributeName();
    /** get the name of the reserved attribute for keystrokes in this
        corpus (these are used mainly for ontologies to allow
        user-settable keystrokes associated with each member of an
        ontology) */
    public String getKeyStrokeAttributeName();
    /** get the name of the reserved attribute for comments in this corpus */
    public String getCommentAttributeName();
    /** get the name of the reserved element for NITE streams in this corpus */
    public String getStreamElementName();
    /** get the name of the reserved element for NITE pointers in this corpus */
    public String getPointerElementName();
    /** get the name of the reserved element for external NITE
     * pointers in this corpus (this is only used for elements in
     * EXTERNAL_POINTER_LAYER type layers)  */
    public String getExternalPointerElementName();
    /** get the name of the reserved element for NITE children in this corpus */
    public String getChildElementName();
    /** get the name of the reserved element for NITE text in this corpus */
    public String getTextElementName();

    /*    public String getStyleSheetPath();
	  public String getAnnotationSpecPath(); */

    /** get the protocol used by CVS (or null if there's no CVS information) */
    public String getCVSProtocol();
    /** get the CVS server (or null if there's no CVS information) */
    public String getCVSServer();
    /** get the module / base directory used by CVS (or null if
     * there's no CVS information) */
    public String getCVSModule();
    /** get the repository used by CVS (or null if
     * there's no CVS information) */
    public String getCVSRepository();
    /** get the connection method used by CVS: returns either NiteMetaConstants.RSH
     * or NiteMetaConstants.SSH */
    public int getCVSConnectionMethod();
    
    /** returns the description of the corpus */
    public String getCorpusDescription();
    /** returns the identifier of the corpus */
    public String getCorpusID();

    /** returns the directory path where ontologies are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file (or absolute, if metadata author prefers).  */
    public String getRelativeOntologyPath();
    /** returns the directory path where ontologies are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relontologypath is absolute) */
    public String getOntologyPath();

    /** returns the directory path where styles are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file (or absolute, if metadata author prefers).  */
    public String getRelativeStylePath();
    /** returns the directory path where styles are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relstylepath is absolute) */
    public String getStylePath();

    /** returns the directory path where signals are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file (or absolute, if metadata author prefers).  */
    public String getRelativeSignalPath();
    /** returns the directory path where signals are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relsignalpath is absolute) */
    public String getSignalPath();

    /** returns the modifier to the signal path, replacing the string
     * 'observation' with the provided Observation's name. This allows
     * a certain amount of flexibility in the placement of signal
     * files. Set using pathmodifier="obsrevation" which means signals
     * are assumed to be in subdirectories named exactly the same as
     * the observationname. */
    public String getSignalPathModifier(NObservation obs);

    /** returns the modifier to the signal path, replacing the string
     * 'observation' with the provided Observation's name. This allows
     * a certain amount of flexibility in the placement of signal
     * files. Set using pathmodifier="obsrevation" which means signals
     * are assumed to be in subdirectories named exactly the same as
     * the observationname. */
    public String getSignalPathModifier(String obs);

    /** returns the directory path where codings are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file (or absolute, if metadata author prefers).  */
    public String getRelativeCodingPath();
    /** returns the directory path where codings are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relcodingpath is absolute) */
    public String getCodingPath();

    /** returns the directory path where interaction codings are
        stored for the corpus, as it is in the metadata file -
        relative to the metadata file (or absolute, if metadata author
        prefers).  */
    public String getRelativeInteractionCodingPath();
    /** returns the directory path where interaction codings are
        stored for the corpus - relative to the working directory in
        which java is running (or absolute if relcodingpath is
        absolute) */
    public String getInteractionCodingPath();

    /** returns the directory path where agent codings are stored for
        the corpus, as it is in the metadata file - relative to the
        metadata file (or absolute, if metadata author prefers).  */
    public String getRelativeAgentCodingPath();
    /** returns the directory path where agent codings are stored for
        the corpus - relative to the working directory in which java
        is running (or absolute if relcodingpath is absolute) */
    public String getAgentCodingPath();

    /** returns the directory path where objectsets are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file (or absolute, if metadata author prefers).  */
    public String getRelativeObjectSetPath();
    /** returns the directory path where objectsets are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relobjectsetpath is absolute) */
    public String getObjectSetPath();

    /** returns the directory path where corpusresources are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file (or absolute, if metadata author prefers).  */
    public String getRelativeCorpusResourcePath();
    /** returns the directory path where corpusresources are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relcorpusresourcepath is absolute) */
    public String getCorpusResourcePath();

    /** returns LTXML1_LINKS or XPOINTER_LINKS depending on the link type
     *  specified on the top level metadata tag.
     */
    public int getLinkType();


    // A few "set" methods that allow us to move things about
    /** change the path where the codings are loaded and serialized
        from & to */
    public void setCodingPath(String path);
    /** change the path where the ontologies are loaded and serialized
        from & to */
    public void setOntologyPath(String path);
    /** change the path where the object sets are loaded and serialized
        from & to */
    public void setObjectSetPath(String path);
    /** change the path where the corpus resources are loaded and serialized
        from & to */
    public void setCorpusResourcePath(String path);
    /** change the path where the "styles" are loaded from - styles
        are stylesheets and annotation board displays. */
    public void setStylePath(String path);
    /** Set the identifier of the corpus */
    public void setCorpusID(String id);
    /** sets the description of the corpus  */
    public void setCorpusDescription(String description);
    /** Set the type of links this metadata has (or will be serialized
        with). Must be either LTXML1_LINKS or XPOINTER_LINKS */
    public void setLinkType(int linktype);
    /** Set validation on (true) or off (false). Validation applies to
        the metadata file itself (though only as far as comparing
        declared observation variables with actual ones) and the data
        itself. */
    public void setValidation(boolean validate);
    /** Returns true if validation is on (default) or false if it's
        off. Validation applies to the metadata file itself (though
        only as far as comparing declared observation variables with
        actual ones) and the data itself. */
    public boolean isValidating();


    /** set the name of the reserved attribute for start times in this
        corpus */
    public void setStartTimeAttributeName(String start);
    /** set the name of the reserved attribute for end times in this
        corpus */
    public void setEndTimeAttributeName(String end);
    /** set the name of the reserved attribute for IDs in this corpus */
    public void setIDAttributeName(String id);
    /** set the name of the reserved attribute for agents in this corpus */
    public void setAgentAttributeName(String agent);
    /** set the name of the reserved attribute for observations in this corpus */
    public void setObservationAttributeName(String agent);
    /** set the name of the reserved attribute for Graphical Visual
        Markup in this corpus */
    public void setGVMAttributeName(String gvm);
    /** set the name of the reserved attribute for KeyStrokes in this corpus */
    public void setKeyStrokeAttributeName(String key);
    /** set the name of the reserved attribute for comments in this corpus */
    public void setCommentAttributeName(String id);
    /** set the name of the reserved element for NITE streams in this corpus */
    public void setStreamElementName(String stream);
    /** change the name of a pointer element for serialization - the
        default value is "nite:pointer" */
    public void setPointerElementName(String pointer_name);
    /** change the name of an external pointer element for serialization - the
        default value is "nite:external_pointer" */
    public void setExternalPointerElementName(String pointer_name);
    /** change the name of a child element for serialization - the
        default value is "nite:child" */
    public void setChildElementName(String child_name);
    
    /** set all the distinguished element and attribute values to
        their defaults. */
    public void setElementsAndAttributesToDefaults();

    /** save the metadata to a file with the given filename. */
    public void writeMetaData(String filename);

    /** save the metadata to the current filename (by default the file
        it was created from, or set using setFilename). */
    public void writeMetaData();

    // some convenience functions

    /** Find the layer which contains the element called "element_name".
     * It is an error for more than one layer to contain the same element. 
     */ 
    public NElement getElementByName(String element_name);
    /** returns a "styled display" (either an annotation board or a
        stylesheet-produced interface) with a given name. */
    public NStyle findStyleWithName(String name);
    /** returns a signal with the given name. */
    public NSignal findSignalWithName(String name);
    /** Find the layers of a given layer type. The type must be one of
        FEATURAL_LAYER, STRUCTURAL_LAYER or TIMED_LAYER (see "NLayer") */
    public List getLayersByType(int type);
    /** returns the metadata filename  */
    public String getFilename();
    /** set the metadata filename for any future save */
    public void setFilename(String filename) throws NiteMetaException;

    /** find a metadata route between layers and return as a List of Steps */
    public List findPathBetween(NLayer top, NLayer bottom);

    /** get the Set of String NLayer names that the elements in this layer can
     * point to */
    public Set getValidPointersFrom (NLayer lay);

    /** get the Set of NLayers that can point to the elements in this named layer */
    public Set getValidPointersTo (String lay);
    /** returns the path to the metadata (can be a URL prefix or a directory path)
     */
    public String getPath();

    /** change the place where the resource file is serialized to */
    public void setResourceFilename(String resource_filename);
}
