/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, 
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.tools.dacoder;
import org.w3c.dom.*;
import java.util.Set;
import java.util.HashSet;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.util.*;

/**
 * See also superclass and NXTConfig class.
 *
 * name of root element of DACoder settings:
 * "DACoderConfig"
 * Extensive documentation in the tool help of the dialogue act coder, under the heading 'Customization'.
 
 annotator specific codings in dacoder:
 * @author Dennis Reidsma, UTwente
 */
public class DACoderConfig extends AbstractCallableToolConfig {
    /**
     * 
     */
    public String getNXTConfigRootName() {
        return "DACoderConfig";
    }
    /**
     * daelementname attribute in corpussettings
     */
    public String getDAElementName() {
        return getNXTConfig().getCorpusSettingValue("daelementname");
    }

    /* This was added by Jonathan Kilgour 22nd March 2006. We've been
     thinking about adding support for enumerated attributes before
     and this is my attempt: if daattributename s present, we use an
     enumerated attribute on daelementname and not an ontology to code
     DAs. */
    /** 
     * daattributename attribute in corpussettings
     */
    public String getDAAttributeName() {
        return getNXTConfig().getCorpusSettingValue("daattributename");
    }
    /**
     * daontology#daroot attributes in corpussettings
     */
    public String getDATypeRoot() {
        return getNXTConfig().getCorpusSettingValue("daontology")+ "#" + getNXTConfig().getCorpusSettingValue("daroot");
    }
    /**
     * daontology#dadefault attributes in corpussettings
     */
    public String getDATypeDefault() {
        return getNXTConfig().getCorpusSettingValue("daontology")+ "#" + getNXTConfig().getCorpusSettingValue("dadefault");
    }
    /**
     * datyperole attributes in corpussettings
     */
    public String getDATypeRole() {
        return getNXTConfig().getCorpusSettingValue("datyperole");

    }
    /**
     * dagloss attributes in corpussettings
     */
    public String getDAAGloss() {
        return getNXTConfig().getCorpusSettingValue("dagloss");
    }
    /**
     * apelementname attributes in corpussettings
     */
    public String getAPElementName() {
        return getNXTConfig().getCorpusSettingValue("apelementname");
    }
    /**
     * apontology#aproot attributes in corpussettings
     */
    public String getAPTypeRoot() {
        return getNXTConfig().getCorpusSettingValue("apontology")+ "#" + getNXTConfig().getCorpusSettingValue("aproot");
    }
    /**
     * apgloss attribute in corpussettings
     */
    public String getAPGloss() {
        return getNXTConfig().getCorpusSettingValue("apgloss");
    }
    /**
     * apontology#defaultaptype attributes in corpussettings
     */
    public String getDefaultAPType() {
        return getNXTConfig().getCorpusSettingValue("apontology")+ "#" + getNXTConfig().getCorpusSettingValue("defaultaptype");
    }

    /**
     * neelementname attributes in corpussettings
     */
    public String getNEElementName() {
        return getNXTConfig().getCorpusSettingValue("neelementname");
    }    


    /* This was added by Jonathan Kilgour 22nd March 2006. We've been
     thinking about adding support for enumerated attributes before
     and this is my attempt: if neattributename s present, we use an
     enumerated attribute and not an ontology to code NEs. */
    /** 
     * neattributename attribute in corpussettings
     */
    public String getNEAttributeName() {
        return getNXTConfig().getCorpusSettingValue("neattributename");
    }

    /* This was added by Jonathan Kilgour 8th September 2006. The
     * point is to make it settable whether the annotation of an
     * already encoded span of words means add another pointer or
     * change the existing pointer the default is to change the
     * existing pointer but if this attribute is true, we allow
     * multiple pointers to the same span. */
    /**
     * nemultiplepointers attributes in corpussettings
     */
    public boolean getNEMultiplePointers() {
	String mlt = getNXTConfig().getCorpusSettingValue("nemultipointers");
	if (mlt==null || !mlt.equalsIgnoreCase("true")) { return false; }
	return true;
    }    


    /**
     * neontology#neroot attributes in corpussettings
     */
    public String getNETypeRoot() {
        return getNXTConfig().getCorpusSettingValue("neontology")+ "#" + getNXTConfig().getCorpusSettingValue("neroot");
    }
    /**
     * neontology#nedefault attributes in corpussettings
     */
    public String getNETypeDefault() {
        return getNXTConfig().getCorpusSettingValue("neontology")+ "#" + getNXTConfig().getCorpusSettingValue("nedefault");
    }
    /**
     * nenameattribute attributes in corpussettings
     */
    public String getNEDisplayAttribute() {
        return getNXTConfig().getCorpusSettingValue("nenameattribute");
    }    
    /**
     * netyperole attributes in corpussettings
     */
    public String getNETypePointerRole() {
        return getNXTConfig().getCorpusSettingValue("netyperole");
    }    
    /**
     * abbrevattribute attributes in corpussettings
     */
    public String getNEAbbrevAttrib() {
        return getNXTConfig().getCorpusSettingValue("abbrevattribute");
    }    



    /**
     * addresseeignoreattribute:
     * that is the attribute that, if present on a dialogue act type label, will force the tool
     * to grey out the addressee checkboxes so the user is forced to NOT code addressee.
     */
    public String getAddresseeIgnoreAttribute() {
        String result =  getNXTConfig().getCorpusSettingValue("addresseeignoreattribute");
        
        if (result == null) {
            result =  "";
        }
        //System.out.println("addria:"+result);
        return result;
    }    
    
    
    /**
     * Override for your application! Determines which annotation elements should be displayed.
     * Set of strings... maybe a config setting, later
     */
    public void initDisplayedAnnotationNames() {
        super.initDisplayedAnnotationNames();
        displayedAnnotationNames.add(getDAElementName());
    }

    /** default to NTranscriptionView.CROSS_SEGMENT_PHRASE for
     * DACoder, but allow user configuration to override (return the
     * value from AbstractCallableToolConfig - i.e. the config file) */
    public int getWordlevelSelectionType() {
	String wselect = getNXTConfig().getGuiSettingValue("wordlevelselectiontype");
	if (wselect!=null) { return super.getWordlevelSelectionType(); }
        return NTranscriptionView.CROSS_SEGMENT_PHRASE;
    }
    /** default to true for DACoder, but allow user configuration to
     * override (return the value from AbstractCallableToolConfig) 
    public boolean getAllowTranscriptSelect() {
	String transelect = getNXTConfig().getGuiSettingValue("transcriptselection");
	if (transelect!=null) return super.getAllowTranscriptSelect();
        return true;
    }
    /** default to true for DACoder, but allow user configuration to
     * override (return the value from AbstractCallableToolConfig) 
    public boolean getAllowAnnotationSelect() {
	String anselect = getNXTConfig().getGuiSettingValue("annotationselection");
	if (anselect!=null) return super.getAllowAnnotationSelect();
        return true;
    }
    /** default to false for DACoder, but allow user configuration to
     * override (return the value from AbstractCallableToolConfig) 
    public boolean getAllowMultiAgentSelect() {
	String agselect = getNXTConfig().getGuiSettingValue("multiagentselection");
	if (agselect!=null) return super.getAllowMultiAgentSelect();
        return false;
    }
    */
    public String getHelpSetName() {
        return "dacoder.hs";
    }

    /**
     * showapwindow attribute in guisettings
     */
    public boolean showAdjacencyPairWindows() {
        return Boolean.valueOf(getNXTConfig().getGuiSettingValue("showapwindow")).booleanValue();
    }



}
