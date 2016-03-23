package net.sourceforge.nite.tools.necoder;
import net.sourceforge.nite.util.*;
import org.w3c.dom.*;
import net.sourceforge.nite.tools.dacoder.*;
import java.util.Set;
import java.util.HashSet;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.util.*;

/**
 * Soft config: see DACoderConfig. Hardcoded settings: see overrides below.
 *
 * @author Dennis Reidsma, UTwente
 */
public class NECoderConfig extends DACoderConfig {

    /**
     * Override for your application! Determines which annotation elements should be displayed.
     * Set of strings... maybe a config setting, later
     */
    public Set getDisplayedAnnotationNames() {
        Set result = new HashSet();
        //result.add(getSegmentationElementName());
        result.add(getNEElementName());
        return result;
    }

    /* use config choice if available, otherwise default to
     * NTranscriptionView.IN_SEGMENT_PHRASE */
    public int getWordlevelSelectionType() {
	String wselect = getNXTConfig().getGuiSettingValue("wordlevelselectiontype");
	if (wselect!=null) { return super.getWordlevelSelectionType(); }
        return NTranscriptionView.IN_SEGMENT_PHRASE;
    }

    /* These are handled by AbstractCallableToolConfig with appropriate defaults.
    public boolean getAllowTranscriptSelect() {
        return true;
    }
    public boolean getAllowAnnotationSelect() {
        return true;
    }
    public boolean getAllowMultiAgentSelect() {
        return false;
    }
    */
    public String getHelpSetName() {
        return "necoder.hs";
    }
    /** Determines whether nested named-entities are permitted (set
     * the corpus setting attribute 'nenesting' to 'true' to permit
     * nesting. */
    public boolean getAllowNestedNamedEntities() {
        String nest =  getNXTConfig().getCorpusSettingValue("nenesting");
	if (nest!=null && nest.equalsIgnoreCase("true")) { return true;	}
	return false;
    }
    /** Named-entity tool specific setting for controlling the
     * ontology tree. It is occasionally useful to force the ontology
     * to remain un-expanded and if you set the corpus setting
     * 'neontologyexpanded' to 'false', this effect is achieved */
    public boolean expandOntologyTree() {
        String nest =  getNXTConfig().getCorpusSettingValue("neontologyexpanded");
	if (nest!=null && nest.equalsIgnoreCase("false")) { return false;	}
	return true;
    }
}
