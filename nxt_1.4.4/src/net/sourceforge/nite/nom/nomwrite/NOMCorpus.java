/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite;

import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.NOMException;

import java.util.List;
import java.util.Iterator;

/**
 * NOMCorpus is similar to the nomread version. The additions are
 * methods that change the way the NOM is serialized.
 *
 * @author jonathan 
 */
public interface NOMCorpus extends net.sourceforge.nite.nom.link.NOMControl {
    public static final double UNTIMED=Double.NaN;    

    /** Load all data for the corpus into the NOMCorpus. Incremental
     * loading of data is the default, so a new call to loadData will
     * not zero-out the data loaded in a previous call.  */
    public void loadData() throws NOMException;

    /** Load data for a specific set of observations into the
       NOMCorpus. Incremental loading of data is the default, so a new
       call to loadData will not zero-out the data loaded in a
       previous call. If the list of codings is non-null, it will be
       expected to be a list of NCodings that is the maximal set to be
       loaded whether lazy loading is on or off. */
    public void loadData(List observations,
			 List codings) throws NOMException;

    /** Load data for a single observation into the
       NOMCorpus. Incremental loading of data is the default, so a new
       call to loadData will not zero-out the data loaded in a
       previous call. */
    public void loadData(NObservation observation) throws NOMException;

    /** Load data for the purpose of comparing different coders'
     * data. The layer 'top' is where we start the per-coder
     * information and 'top_common' is the layer at which we expect
     * everything to be common between coders. 'coder_attribute_name'
     * is ised as the name of the attribute that gets the name of the
     * coder and 'path' is where the coder data is. We assume the data
     * will use standard NXT-filenames but be held in a directory per
     * coder (the name of the coder is assumed to be the name of the
     * directory) under 'path'. If 'observation' is null we attempt to
     * load all the data, otherwise we only attempt to load one
     * observation.  loadReliability is incompatible with lazy loading.
     */
    public void loadReliability(NLayer top, NLayer top_common, String coder_attribute_name, String path, List observations) throws NOMException;

    /** Load data for the purpose of comparing different coders'
     * data. This is the same as the other call except for the final
     * argument which is a list of Strings: names of layers that
     * should be loaded in 'gold-standard' mode */ 
    public void loadReliability(NLayer top, NLayer top_common, String coder_attribute_name, String path, List observations, List other_layers) throws NOMException;

    /** Set the preferred annotator for *all* codings that is used on
     * subsequent loadData calls. This will be overridden by any
     * codings that are forced to a specific annotator using
     * 'forceAnnotatorCoding'. Note that this is the preferred
     * annotator only, and if there is no annotator data for any coding
     * but gold-standard data is present, that will be loaded
     * instead. */
    public void setDefaultAnnotator(String annotator);

    /** Force one coding to be loaded for a specific annotator when
     * loadData is called. This loads from the annotator's directory
     * even if it's empty, and there is gold-standard data
     * available. */
    public void forceAnnotatorCoding(String annotator, String coding) throws NOMException;

    /** Prefer one coding to be loaded for a specific annotator when
     * loadData is called. This means if there's no annotator data for
     * the coding we take any 'gold-standard' data instead. */
    public void preferAnnotatorCoding(String annotator, String coding) throws NOMException;

    /** Removes all data in the NOM */
    public void clearData();

    /** Removes any currently loaded data relating to the given observation */
    public void clearDataForObservation(NObservation ob);

    /** Removes any currently loaded data relating to the named observation */
    public void clearDataForObservation(String ob);

    /** returns the metadata associated with this NOM */
    public NMetaData getMetaData();

    /** returns a List of NObservation elements - each one the name of an
     * observation that has been asked to be loaded (how much, if any
     * of the observation data actually loaded depends on lazy
     * loading). */
    public List getLoadedObservations();

    /** Provides an iterator which visits each element in the NOM
      exactly once. We guarantee to traverse each "document" in
      document order, where "document" refers to a file that is read
      in or a pseudo-file that is created internally when data is
      loaded for a particular purpose. These "documents" are not
      considered to be ordered. */
    public Iterator NOMWalker();

    /** returns true if the corpus is validating (i.e. if it is
        checking against the metadata whether changes are valid). The
        default value for validation is true */
    public boolean isValidating();

    /** Set validation for the corpus. The default value for
        validation is true. */
    public void setValidation(boolean validate);

    /** Used by NOM program - not for client program use */
    public boolean getBatchMode();

    /** Returns true if data is currently being loaded from file. */
    public boolean isLoadingFromFile();

    /** Return the deepest nesting of elements in this recursive layer
        (if the layer is not recursive, returns 1 or 0) */
    public int getMaxDepth(NLayer layer);

    /** Return a list of NOMWriteElements which have the given element name. */
    public List getElementsByName(String name);

    /* Return the element with the given 'colour' (i.e. filename) and ID */
    public NOMElement getElementByID(String colour, String id);

    /** Return a NOMWriteElement which has the given element ID: you
     * can either pass an unadorned ID in which case NXT searches for
     * the element in all already-loaded files, or you can specify the
     * 'full' ID like this: colour#id (e.g. q4nc4.f.moves#move.3 would refer to
     * element 'move.3' in the file q4nc4.f.moves.xml) */
    public NOMElement getElementByID(String id);

    /** returns a List of NOMElements: the top level "stream" elements */
    public List getRootElements();

    /** returns a root NOMElement which has the given colour */
    public NOMElement getRootWithColour(String colour);

    /** returns the root NOMElement which has the given colour and
        resource */
    public NOMElement getRootWithColour(String colour, NResource resource);

    /** returns the earliest start time of any element in the corpus
        (or UNTIMED if there is no timed element) */
    public double getCorpusStartTime();

    /** returns the latest end time of any element in the corpus
        (or UNTIMED if there is no timed element) */
    public double getCorpusEndTime();

    /** returns the duration of the corpus (last end time - earliest start time)
        (or UNTIMED if there is are no timed elements) */
    public double getCorpusDuration();

    /** Link syntax information: get the String that separates a
        filename from an ID */
    public String getLinkFileSeparator();

    /** Link syntax information: get the String that appears before an ID */
    public String getLinkBeforeID();

    /** Link syntax information: get the String that appears after an ID */
    public String getLinkAfterID();

    /** Link syntax information: get the String that appears between
        IDs in a range */
    public String getRangeSeparator();

    /** Link syntax information: get the name of the 'href' attribute */    
    public String getHrefAttr();

    /** Resolve an individual xlink expression which points to exactly
       one NOM element - the second argument explicitly names the link
       type involved. It can be one of XPOINTER_LINKS or LTXML1_LINKS
       (defined in the NMetaData class) */
    public NOMElement resolveLink(String xlink, int linktype);

    /** Return the reverse index of pointers to the given element */
    public List getPointersTo(NOMElement to_element);

    /** Set to true (default) to lazy-load any future calls to load data;
     * false means everything in future load calls is loaded up-front. */
    public void setLazyLoading(boolean bool);

    /** Set to true (default) to lazy-load any future calls to load data;
     * false means everything in future load calls is loaded up-front. */
    public boolean isLazyLoading();

    /** finish loading *all* files we know about from the corpus: this
     * only makes sense if lazy loading is switched on, otherwise it
     * will do nothing. */
    public void completeLoad();

    /*-------------------------------------------------------------*/
    /* WRITE ONLY METHODS. These generally throw an exception or
     * return null if the NOM is in fact read-only. */
    /*-------------------------------------------------------------*/
    
    /** returns true if the corpus has unsaved edits */
    public boolean edited();

    /** Set to true to make future serialization calls serialize with
        inherited times on structural elements. Set to false (default)
        to only serialize start and end times on timed elemets. */
    public void setSerializeInheritedTimes(boolean bool);

    /** Set to true to make future serialization calls serialize with
        stream element names conforming to meta.getStreamElementName().
	Default is that stream elements will be output as they are input.
    */
    public void setForceStreamElementNames(boolean bool);

    /** If this method is used with a non-null argument, we make sure
        the schema instance namespace is output on every stream-like
        element on serialization along with this as the
        noNamespaceSchemaLocation */
    public void setSchemaLocation(String location);

    /** True if we should allow inherited times to be serialized */
    public boolean serializeInheritedTimes();

    /** Set to true (default) to make future serialization calls
        serialize with ranges where possible. Set to false to
        explicitly list all nite children. */
    public void setSerializeMaximalRanges(boolean bool);

    /** True if we should  serialize ranges */
    public boolean serializeMaximalRanges();

    /** Serialize all files which have been changed. */
    public void serializeCorpusChanged() throws NOMException;

    /** Serialize the entire loaded corpus */
    public void serializeCorpus() throws NOMException;

    /** Serialize all loaded files for the given list of observations */
    public void serializeCorpus(List observations) throws NOMException;

    /** generates an Identifier that's globally unique - used when
        creating elements. We use 'colour' here in an NXT-specific way:
        it's precisely the filename the element will be serailized
        into, without its the '.xml' extension: thus it comprises
        observation name; '.'; the agent name followed by '.' (if an
        agent coding); the coding name. */
    public String generateID(String colour);

    /** generates an Identifier that's globally unique - used when
        creating elements. We use 'colour' here in an NXT-specific
        way: it's precisely the filename the element will be
        serailized into, without its the '.xml' extension: thus it
        comprises observation name; '.'; the agent name followed by
        '.' (if an agent coding); the coding name. The resource is a
        metadata construct that will also affect the ID if
        non-null.  */
    public String generateID(String colour, NResource resource);

    /** If an element of a particular type and observation is being
     * created by a user action (i.e. not through loading a file),
     * this will decide if a resource should be associated with the
     * element, and if so, which resource */
    public NResource selectResourceForCreatedElement(String elementname, String observation);

    /** registers an Identifier as having been used and if necessary,
        notes an Integer in the ID hash for quick generation of IDs */
    public void registerID(String colour, String id);
    
    /** Return true if the corpus can be edited safely - for internal
        use. The corpus is always safe to edit if it is not shared; if
        the corpus is shared, edits are permitted only if a process
        has locked the corpus. */
    public boolean isEditSafe ();

    /** lock the corpus for edits - returns false if another view has
        locked the corpus. */
    public boolean lock (NOMView view);

    /** unlock the corpus - returns false if the view isn't the one
        that has the lock. */
    public boolean unlock(NOMView view);

    /** Resolve an individual xlink expression which points to exactly
       one NOM element. Note that the format of the link depends on the
       metadata link syntax setting. */
    public NOMElement resolveLink(String xlink);

    public void removePointerIndex(NOMPointer point);

    /** This is used by internal corpus-building routines to make
     * sure we always use the right constructors. */
    public NOMMaker getMaker();

    /** Return the actual file to which this data should be serialized
     * (including any annotator-specific subdirectory). */
    public String getCodingFilename(NObservation no, NCoding co, NAgent ag);

    /** Return the current 'context': the locations and types of files
     * loaded at this point in time. */
    public NOMContext getContext();

} 
