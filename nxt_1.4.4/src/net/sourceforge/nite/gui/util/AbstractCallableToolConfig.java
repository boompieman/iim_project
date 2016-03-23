/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, 
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.io.*;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.util.Debug;

/**
 * AbstractCallableToolConfig subclasses define all the different
 * settings needed in the abstract callable tool class.  Subclasses of
 * this config class can define hardcoded settings (things that the
 * user should NOT be able to modify, such as whether you are allowed
 * to select text from multiple agents) and customizable settings
 * (through the nxtConfig.xml file, see also the {@link NXTConfig}
 * class.).
 *
 * By overriding this class you can switch a setting from being
 * user-configurable to being hardcoded and vice versa.
 * <br>
 * The best example of the different possibilities of these
 * configurations is given by the DACoderConfig subclass, which shows
 * both the use of hardcoded settings and user-customizable settings
 * (e.g. name of dialogue act elements).
 * <br>
 * <h3>Use pattern</h3>
 * <ul>
 * <li>create Config object
 * <li>if needed, set different config file name;  (redirected to internal NXTConfig object)
 * <li>set metadatafilename (!!!) (this MUST be called.)  (redirected to internal NXTConfig object)
 * <li>Retrieve relevant settings.
 * </ul>
 *
 *
 * now all settings are available. Some getmethods will internally get
 * the requested setting from the NXTConfig object.  Subclasses off
 * this config class MUST document which of those settings are taken
 * from config file. Some getMethods will return hard-coded values (if
 * for a certain tool a certain value should ALWAYS be the same, such
 * as getAllowMultiAgentSelect or getHelpSetName for the AMI Dialogue
 * act coder.)
 * <br>
 * This construction allows a programmer to have part of the settings
 * <i>hard coded</i>, i.e. the user cannot modify them, and part of
 * the settings <i>user-customizable</i>, i.e. the user can change
 * them by modifying the configuration XML file.
 * <p>
 * See also {@link NXTConfig}
 *
 * @@@TODO TODO TODO investigate possibilities for caching
 * @author Dennis Reidsma, UTwente
 */
public class AbstractCallableToolConfig {
    /**
     * Clears any cached values. Extend this method
     * if your config class caches extra values!
     */
    public void clearCachedValues() {
        tttDelegate = null;
        asCodings = null;
        displayedAnnotationNames = null;
    }
    
    
    /**
     * NEVER access directly, always use getXMLConfig.
     */
    private NXTConfig nxtConfig;
    /** 
     * Returns the config object. If needed, load and create the object.
     */
    public NXTConfig getNXTConfig() {
        if (nxtConfig == null) {
            initNXTConfig();
        }
        return nxtConfig;
    }
    /**
     * Create and initialize NXTconfig object. Sets rootname of config object.
     * Doesn't set config file name, or metadata filename!
     */
    protected void initNXTConfig() {
        nxtConfig = new NXTConfig();
        nxtConfig.setConfigRoot(getNXTConfigRootName());
        clearCachedValues();
    }
    /**
     * Override if you need other rootname
     */
    public String getNXTConfigRootName() {
        return "DefaultConfig";
    }
    /**
     * Set the file name of the config file. If called, settings will be reloaded.
     * Passes on to NXTConfig.loadConfig(fileName)
     */
    public void loadConfig(String newFile) throws IOException, SAXException {
        getNXTConfig().loadConfig(newFile);
        clearCachedValues();
    }
    /**
     * Reload the current config file. If called, settings will be reloaded.
     * Useful when config file was externally modified.
     * Passes on to NXTConfig.reloadConfig()
     */
    public void reloadConfig() throws IOException, SAXException {
        getNXTConfig().reloadConfig();
        clearCachedValues();
    }
    /**
     * Set the file name of the metadata file. If called, settings will be reloaded.
     * Passes on to NXTConfig.setMetaDataFile()
     */
    public void setMetaDataFile(String newFile) {
        getNXTConfig().setMetaDataFile(newFile);
        clearCachedValues();
    }
    
    /**
     * Default: segmentationelementname attribute in corpussettings
     */
    public String getSegmentationElementName() {
        return getNXTConfig().getCorpusSettingValue("segmentationelementname");
    }
    /**
     * Default: transcriptionlayername attribute in corpussettings
     */
    public String getTranscriptionLayerName() {
        return getNXTConfig().getCorpusSettingValue("transcriptionlayername");
    }
    /**
     * Default: transcriptionattribute attribute in corpussettings
     */
    public String getTranscriptionAttribute() {
        return getNXTConfig().getCorpusSettingValue("transcriptionattribute");
    }
    
    protected TranscriptionToTextDelegate tttDelegate = null;
    /**
     * Default: new instance of class defined in transcriptiondelegateclassname attribute in corpussettings
     */
    public TranscriptionToTextDelegate getTranscriptionToTextDelegate() {
        if (tttDelegate == null) {
            String delegateName = getNXTConfig().getCorpusSettingValue("transcriptiondelegateclassname");
            try {
                tttDelegate = (TranscriptionToTextDelegate)Class.forName(delegateName).newInstance();
            } catch (Exception e) {
                System.out.println("can't instantiate delegate class '" + delegateName + "'.");
            }
        }
        return tttDelegate;
    }

    protected TranscriptionToTextDelegate segmentTextDelegate = null;
    /**
     * Default: new instance of class defined in segmenttextdelegateclassname attribute in corpussettings
     */
    public TranscriptionToTextDelegate getSegmentToTextDelegate() {
        if (segmentTextDelegate == null) {
            String delegateName = getNXTConfig().getCorpusSettingValue("segmenttextdelegateclassname");
	    if (delegateName!=null) {
		try {
		    segmentTextDelegate = (TranscriptionToTextDelegate)Class.forName(delegateName).newInstance();
		} catch (Exception e) {
		    Debug.print("can't instantiate segment delegate class '" + delegateName + "'.", Debug.ERROR);
		}
	    }
        }
        return segmentTextDelegate;
    }

    /**
     * Default: transcriptionattribute attribute in corpussettings
     */
    public String getSegmentToTextAttribute() {
        return getNXTConfig().getCorpusSettingValue("segmenttextattribute");
    }
    
    /**
     * Default: applicationtitle attribute in guisettings
     */
    public String getApplicationName() {
        // oh well, why not...
        return getNXTConfig().getGuiSettingValue("applicationtitle");
    }    
     
    protected List asCodings = null;
    /*
     * By default, this list is determined by a ';' separated list of coding names in the corpussettingattribute 
     * `annotatorSpecificCodings'.
     */
    /*public List getAnnotatorSpecificCodings() {
        if (asCodings == null) {
            asCodings = new ArrayList();
            String list = getNXTConfig().getCorpusSettingValue("annotatorspecificcodings");
            if (list!=null) {
                StringTokenizer st = new StringTokenizer(list,";");
                while (st.hasMoreElements()) {
                    String next = st.nextToken();
                    asCodings.add(next.trim());
                }
            }
        }
        return asCodings;
    }   */ 
    /**
     * showlogwindow attribute in guisettings
     */
    public boolean showLogWindows() {
        return Boolean.valueOf(getNXTConfig().getGuiSettingValue("showlogwindow")).booleanValue();
    }        

    /*---- added by jonathan 8.4.5 ----*/

    /** 
     * set the name of the corpus-settings we will use
     */
    public void setCorpusSettings(String corpussets) {
        getNXTConfig().setCorpusSettings(corpussets);	
    }

    /** 
     * set the name of the gui-settings we will use
     */
    public void setGUISettings(String guisets) {
        getNXTConfig().setGUISettings(guisets);	
    }

    /*---- end of added by jonathan 8.4.5 ----*/
    
    /*==================================================================================
     * The rest of the settings are not customizable (yet?) through the config file!
     */

    protected Set displayedAnnotationNames = null;

    /**
     * Initializes value from settings...
     * Override for your application! Determines which annotation elements should be displayed.
     * Set of strings... maybe a config setting, later
     * Default is empty set.
     */
    public void initDisplayedAnnotationNames() {
        displayedAnnotationNames = new HashSet();
    }
    /*
     * Set of strings... maybe a config setting, later
     * Default is empty set.
     */
    public Set getDisplayedAnnotationNames() {
        if (displayedAnnotationNames == null) {
            initDisplayedAnnotationNames();
        }
        return displayedAnnotationNames;
    }
    
    /**
     * This String determines what units are selectable on the speech
     * transcriptions (assuming transcriptselection is not
     * false). This value is read from the 'wordlevelselectiontype'
     * attribute. The are currently five valid strings - anything else
     * will result in the default behaviour: 'in_segment_phrase'.
     * <ul>
     * <li>one_word
     * <li>one_segment
     * <li>multiple_segments
     * <li>in_segment_phrase
     * <li>cross_segment_phrase
     * </ul>
     */
    public int getWordlevelSelectionType() {
	int wval = NTranscriptionView.IN_SEGMENT_PHRASE;
	String wselect = getNXTConfig().getGuiSettingValue("wordlevelselectiontype");
	if (wselect==null) { return wval; }
	else if (wselect.equalsIgnoreCase("one_word")) { return NTranscriptionView.ONE_WORD; }
	else if (wselect.equalsIgnoreCase("one_segment")) { return NTranscriptionView.ONE_SEGMENT; }
	else if (wselect.equalsIgnoreCase("multiple_segments")) { return NTranscriptionView.MULTIPLE_SEGMENTS; }
	else if (wselect.equalsIgnoreCase("in_segment_phrase")) { return NTranscriptionView.IN_SEGMENT_PHRASE; }
	else if (wselect.equalsIgnoreCase("cross_segment_phrase")) { return NTranscriptionView.CROSS_SEGMENT_PHRASE; }
	return wval;
    }

    /**
     * This boolean determines whether you can select speech
     * transcription elements. It is read from the configuration file
     * attribute 'transcriptselection'. Return false only if this is
     * present and set to the string 'false'; otherwise return
     * 'true'. If this one is false, no speechtext selection will take
     * place, regardless of settings such as 'allowMultiAgentSelect'
     * or 'wordlevelSelectionType'.  <p>default true.  <p>See also the
     * general documentation on NTranscriptionView selection
     */
    public boolean getAllowTranscriptSelect() {
	String transelect = getNXTConfig().getGuiSettingValue("transcriptselection");
	if (transelect==null) return true;
	if (transelect.equalsIgnoreCase("false")) { return false; }	
        return true;
    }
    /**
     * This boolean determines whether you can select annotation
     * elements. It is read from the configuration file attribute
     * 'annotationselection'. Return false only if this is present and
     * set to the string 'false'; otherwise return 'true'. If this is
     * false, no annotation-element-selection will take place,
     * regardless of other settings.
     * <p>default true.
     * <p>See also the general documentation on NTranscriptionView selection.
     */
    public boolean getAllowAnnotationSelect() {
	String anselect = getNXTConfig().getGuiSettingValue("annotationselection");
	if (anselect==null) return true;
	if (anselect.equalsIgnoreCase("false")) { return false; }	
        return true;
    }
    /**
     * If true, user can select a span of text, or set of annotation
     * elements, that contains more than one agent's data. If false,
     * then not. It is read from the configuration file attribute
     * 'multiagentselection'. Return true only if this is present and
     * set to the string 'true'; otherwise return 'false'. 
     * <p>default false.
     * <p>See also the general documentation on NTranscriptionView selection.
     */
    public boolean getAllowMultiAgentSelect() {
	String agselect = getNXTConfig().getGuiSettingValue("multiagentselection");
	if (agselect==null) return false;
	if (agselect.equalsIgnoreCase("true")) { return true; }	
        return false;
    }
    public String getHelpSetName() {
        return null;//maybe this should be a helpset explaininng how to extend this class :-) Using the example of NECoder
    }
}
