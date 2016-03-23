/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.transcriptionviewer;

import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nxt.*;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;


/**
 * The default implementation of TransToAnnoMap depends on a straightforward parent-child relation
 * between annotation elements and transcription text. For most applications this is sufficient.
 * <p>
 * A DefaultTransAnnoMap must be initialized with a NTranscriptionView. The selectableAnnotationTypes
 * of that NTranscriptionView determines which parents of a transcription element are actually returned
 * by a call to getAnnotationElementsForTransElement.
 *
 * @author Dennis Reidsma 
 * See also @link TransToAnnoMap
 */
public class DefaultTransToAnnoMap implements TransToAnnoMap {
 
    /**
     * Returns all transcription elements that are a descendant of the given NOMElement.
     * <p>NB: Only those elements are returned that are actually displayed in the NTranscriptionView
     * (useful e.g. if you display only speech for one participant...)
     * <!-- <p>[DR:]<b>Remark about ordering?</b>//can we guaratee an order here? -->
     */
    public Set getTransElementsForAnnotationElement(NOMElement nme) {
        return ntv.getTranscriptionDescendants(nme);
    }
    
    /**
     * Returns all annotation elements that are a ancestor of the given transcription NOMElement.
     */
    public Set getAnnotationElementsForTransElement(NOMElement nme) {   
        Set result = new TreeSet();
        Set selectableAnnotationTypes = ntv.getSelectableAnnotationTypes();
        if (ntv.isTranscriptionElement(nme)) {
            List parents = new ArrayList(nme.getParents());
            while (parents.size() > 0) {
                NOMElement nextParent = (NOMElement)parents.get(0);
                parents.remove(0);
                if ((selectableAnnotationTypes == null) || (selectableAnnotationTypes.contains(nextParent.getName()))) {
                    result.add(nextParent);
                } else {
                    List newP = nextParent.getParents();
                    if (newP != null) {
                        parents.addAll(newP);
                    }
                }
            }
        } else {
            result.add(nme);
        } 
        return result;
    }
    
    
    protected NTranscriptionView ntv;
    public DefaultTransToAnnoMap(NTranscriptionView ntv) {
        init(ntv);
    }        
    public void init(NTranscriptionView ntv) {
        this.ntv = ntv;
    }
    
}