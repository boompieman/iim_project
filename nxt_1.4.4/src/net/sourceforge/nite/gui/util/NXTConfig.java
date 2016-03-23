/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, 
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;
import javax.swing.*;
import net.sourceforge.nite.util.Debug;

/**
 * This class facilitates access to the configuration file that is used to customize subclasses of 
 * {@link AbstractCallableTool}.
 * <br>
 * NXTConfig is used for accessing the customizations to the tools that can be made by the <i>user</i>.
 * These customizations are stored in XML (default in the file nxtConfig.xml, somewhere on the classpath).
 * Each tool defines its own element directly below the XML root of the file. For example, the AMI
 * DialogueActCoder defines the element DACoderConfig. In this element three types of children are allowed:
 * <ul>
 * <li><code>&lt;corpussettings&gt;</code> elements contain tool-specific information. This may include information about 
 *     a specific corpus, such as the name of the layer containing the speech transcriptions, or the name of the
 *     dialogue act ontology. Such information is usually stored in the attributes of the corpussettings element.
 * <li><code>&lt;guisettings&gt;</code> elements contain gui related settings information. This may include things
 *     like window positions or keyboard shortcuts.
 * <li><code>&lt;metadatafile&gt;</code> elements relate certain metadata filenames to the correct corpussettings 
 *     elements and the correct guisettings element.
 * </ul>
 * A tool can access these settings directly, or through the appropriate AbstractCallableToolConfig subclass.
 * The documentation of the class {@link AbstractCallableToolConfig} provides some more documentation
 * of the configuration possibilities for AbstractCallableTool.
 * <br>
 * <h3>Method summary</h3>
 * By default, the NXTConfig will use the settings in the file "nxtConfig.xml", if it can be found on the classpath.
 * <br>{@link loadConfig(String fileName)} allows you to load a different configuration file.
 * <br>{@link reloadConfig()} will reload the current configuration file. This is useful if the config file was
 * modified externally.
 * <br>{@link setConfigRoot(String newRoot)} determines which configuration element should be used.
 * <br>{@link setMetaDataFile(String fileName)} determines which metadata file is currently opened.
 * <br>The 'active' corpus and gui settings are those contained in the correct root element (configRoot),
 * that are linked to the current metadata file through a <code>metadatafile</code> element.
 * <br>{@link getCorpusSettings()} returns an XML Node for the active corpussettings.
 * <br>{@link getGuiSettings()} returns an XML Node for the active guisettings.
 * <h3>Use pattern</h3>
 * <ul>
 * <li>Initialization: Create a NXTConfig object, possibly load a different configuration file
 * <li>Initialization: Set the configuration rootname (e.g. DACoderConfig); set the metadatafilename.
 * <li>Retrieve XML Nodes for the corpussettings and/or guisettings and use the information contained in them.
 * </ul>
 * NB: There is no suppport yet for MODIFYING the configFile, at the moment that should be done in a text editor.
 * This might be added in the future.
 *
 * @author Dennis Reidsma, UTwente
 */
public class NXTConfig {
/*
 * <NXTConfig>
      <ToolRoot> <!-- this element name is specific for the tool for which this eelement contains the settings -->
        <corpussettings   ...attributes.. />
        <guisettings      ...attributes.. />
        <metadatafile filename corpussettingsID guisettingsID />
      </ToolRoot>
      <ToolRoot2 >
        ... etc
      </...>
 * </NXTConfig>
 */

    public static String DEFAULT_CONFIG = "/nxtConfig.xml";
    
    protected String configFileName = DEFAULT_CONFIG;

    String corpusSettingsId = "";
    String guiSettingsId = "";


    /**
     * INIT: no longer loads the default config file.
     * jonathan 5.4.5 - we only attempt to load config once: when
     * initializeCorpus is called.
     */
    public NXTConfig() {
    }

    /** 
     * set the ID of the corpus-settings node to use. This is normally
     * done via an argument to AbstractCallableTool, and means we can
     * avoid depending on a correct metadata entry in the config file
     * (if we get it right!) - jonathan 8.4.5 */
    public void setCorpusSettings (String csi) {
	corpusSettingsId=csi;
    }

    /** 
     * set the ID of the gui-settings node to use. This is normally
     * done via an argument to AbstractCallableTool, and means we can
     * avoid depending on a correct metadata entry in the config file
     * (if we get it right!) - jonathan 8.4.5 */
    public void setGUISettings (String guid) {
        guiSettingsId=guid;
    }
    
    /**
     * Load the given config file. Necessary when the file has been changed.
     * NB: The tool (whichever) should probably also reload the corpus and re-initialize everyting
     * when the config file has been changed!
     * <br>
       IOException - If any IO errors occur. 
       SAXException - If any parse errors occur. 
       IllegalArgumentException - If the InputStream is null
     */
    public void loadConfig(String fileName) throws IOException, SAXException {
        checkSave();
        configFileName = fileName;
        reloadConfig();
    }
    /**
     * Reload the current config file. Necessary when the file has been changed externally.
     * NO SAVE CHECK HERE!
     * NB: The tool (whichever) should probably also reload the corpus and re-initialize everyting
     * when the config file has been changed!
     * <br>
       IOException - If any IO errors occur. 
       SAXException - If any parse errors occur. 
       IllegalArgumentException - If the InputStream is null
     */
    public void reloadConfig() throws IOException, SAXException {
        Debug.print("Loading configuration from " + configFileName, Debug.IMPORTANT);
        configDoc = null;
        clearSettingsNodes(); //settings nodes need to be retrieved anew
        
        InputStream is = getStream(configFileName);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            configDoc = db.parse(is);
        } catch (Exception ex) {
            Debug.print("Can't load config file: " + configFileName, Debug.ERROR);
            ex.printStackTrace();
            recoverFromLoadConfigError();
        }
    }
    /**
     * tries to resolve string into inputstream name
     */
    protected InputStream getStream(String name) {
        URL rt = getClass().getResource(configFileName);
	if (rt!=null) {
	    Debug.print("FILE: " + rt.getPath(), Debug.DEBUG);
	}
        InputStream result = getClass().getResourceAsStream(configFileName);
        if (result != null) {
            return result;
        }
        try {
            result = new FileInputStream(name);
            return result;
        } catch (Exception ex) {
        }
        //more attempts...
        return null;
    }
    /**
     * If an error occurs while loading the config file, this method will ask the user to select a new 
     * file using a FileChooser.
     */
    protected void recoverFromLoadConfigError() throws IOException, SAXException  {
        JTextArea msg = new JTextArea("The configuration file '"+configFileName+"' could not be loaded. \n The stacktrace was printed on the standard output. \n Please fix the problem and reload the proper configuration file in the next dialog.");
        JOptionPane.showMessageDialog(null, msg);
        JFileChooser jfc = new JFileChooser(configFileName);
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            loadConfig(jfc.getSelectedFile().getAbsolutePath());
        } else {
            JOptionPane.showMessageDialog(null, "Failed to load configuration.");            
        }
    }
    /**
     * The DOM Document containing the content of the configuration file.
     */
    protected Document configDoc = null;

    /**
     * Every tool can define its own settings. These settings are stored in an element
     * directly below the root <NXTConfig>. This method determines which element should be 
     * used for the settings of the 'current' tool.
     * <br>
     * If the root is unknown in the config file, getGUISettings or getCorpussettings will return null.
     * <br>
     * Example: The DACoder (AMI Dialogue Act Coder) stores its settings in the element
     * named "DACoderConfig". Therefore, somewhere in the initialization of that tool,
     * setConfigRoot("DACoderConfig") will be called.
     */
    public void setConfigRoot(String rootName) {
        if ((rootName == null) || (rootName.equals(""))) {
            return;
        }
        configRoot = rootName;
        clearSettingsNodes(); //settings nodes need to be retrieved anew
    }
    protected String configRoot = "none";
    
    /**
     * The config file contains separate entries to couple a metadata file to certain corpus settings and
     * gui settings, in ordr to allow several metadata files to share the same settings.
     * This method is used to determine which metadata file is currently loaded in the tool.
     * The NXTConfig object will use that information to retrieve the appropriate corpussettings
     * and guisettings Nodes (on request).
     * <br>
     * If the metadata file is unknown in the config file, getGUISettings or getCorpussettings will return null.
     * <br>
     */
    public void setMetaDataFile(String fileName) {
        metaDataFile = fileName;
        clearSettingsNodes(); //settings nodes need to be retrieved anew
    }
    protected String metaDataFile = "metadata.xml";

/*
((//"getavailablemetadatafiles"? not until we also allow editing of config information.))
*/
    /**
     * Cached Node. Don't access directly; use getCorpusSettings().
     */
    protected Node corpusSettingsNode = null;
    /**
     * Cached Node. Don't access directly; use getGuiSettings().
     */
    protected Node guiSettingsNode = null;
    /** 
     * Clears the cached settings nodes. Called when root or metadatafile changes.
     */
    protected void clearSettingsNodes() {
        corpusSettingsNode = null;
        guiSettingsNode = null;
    }

    /** 
     * Give the current config root and metadata filename, returns the appropriate corpus settings Node.
     * If either is unknown or the config file was not loaded properly, this method returns null.
     */
    public Node getCorpusSettings() {
        if (corpusSettingsNode == null) {
            retrieveSettings();
        }
        return corpusSettingsNode;
    }
    /** 
     * Give the current config root and metadata filename, returns the appropriate gui settings Node.
     * If either is unknown or the config file was not loaded properly, this method returns null.
     */
    public Node getGuiSettings() {
        if (guiSettingsNode == null) {
            retrieveSettings();
        }
        return guiSettingsNode;
    }

    /**
     * Easy access method for attribute values of the corpus settings node
     */
    public String getCorpusSettingValue(String attribute) {
        Node csn = getCorpusSettings();
        if (csn == null) {
            return null;
        }
        Node n = csn.getAttributes().getNamedItem(attribute);
        if (n == null) {
            return null;
        }
        return n.getNodeValue();
    }
    /**
     * Easy access method for attribute values of the gui settings node
     */
    public String getGuiSettingValue(String attribute) {
        Node gsn = getGuiSettings();
        if (gsn == null) {
            return null;
        }
        Node n = gsn.getAttributes().getNamedItem(attribute);
        if (n == null) {
            return null;
        }
        return n.getNodeValue();
    }
            
    /**
     * Retrieve the current settings from the config file and cache
     * them in the variables corpusSettingsNode and guiSettingsNode.
     */
    protected void retrieveSettings() {
        if (configDoc == null) {
            Debug.print("No config file loaded", Debug.WARNING);
            return;
        }
        NodeList nl = configDoc.getElementsByTagName(configRoot);
        if (nl.getLength() < 1) {
            Debug.print("Config root not found", Debug.WARNING);
            return;
        }
        Node root  = nl.item(0);
        Map corpusSettingsMap = new HashMap(); //(id, node)
        Map guiSettingsMap = new HashMap();    //(id, node)
        Map metaDataFileMap = new HashMap();   //(filename, node)
    
        //get all children, store them temporarily in maps
        Node nextChild = root.getFirstChild();
        while (nextChild != null) {
            if (nextChild.getNodeName().equals("corpussettings")) {
                corpusSettingsMap.put(nextChild.getAttributes().getNamedItem("id").getNodeValue(), nextChild);
            }
            if (nextChild.getNodeName().equals("guisettings")) {
                guiSettingsMap.put(nextChild.getAttributes().getNamedItem("id").getNodeValue(), nextChild);
            }
            if (nextChild.getNodeName().equals("metadatafile")) {
                String canonicalFileName = nextChild.getAttributes().getNamedItem("file").getNodeValue();
                try {
                    canonicalFileName = new File(canonicalFileName).getCanonicalPath();
                } catch (IOException ex) {
                }
                metaDataFileMap.put(canonicalFileName, nextChild);
            }
            nextChild = nextChild.getNextSibling();
        }
        
        //get appropriate metadatanode
        String canonicalMetaFileName = metaDataFile;
        try {
            canonicalMetaFileName = new File(metaDataFile).getCanonicalPath();
        } catch (IOException ex) {
        }
        Node metaDataNode = (Node)metaDataFileMap.get(canonicalMetaFileName);

	// This is changed since we may have gui & corpus settings up
	// front.  Also we check the values are valid rather than just
	// checking they're non-null. jonathan 8.4.5. 
	// First check up-front settings:
	boolean upfront=false;
	String ocsi=""; String ogsi="";
	if (corpusSettingsId!=null && !corpusSettingsId.equals("") &&
	    guiSettingsId!=null && !guiSettingsId.equals("")) {
	    upfront=true;
	    corpusSettingsNode = (Node)corpusSettingsMap.get(corpusSettingsId);
	    guiSettingsNode =(Node)guiSettingsMap.get(guiSettingsId);
	    ogsi=guiSettingsId;
	    ocsi=corpusSettingsId;
	}

	if ((guiSettingsNode==null || corpusSettingsNode==null) && metaDataNode!=null) {
            //get id's of corpus node and gui node
            corpusSettingsId = metaDataNode.getAttributes().getNamedItem("corpussettings").getNodeValue();
            guiSettingsId = metaDataNode.getAttributes().getNamedItem("guisettings").getNodeValue();
	    corpusSettingsNode = (Node)corpusSettingsMap.get(corpusSettingsId);
	    guiSettingsNode =(Node)guiSettingsMap.get(guiSettingsId);
	}

        if (guiSettingsNode==null || corpusSettingsNode==null) {
	    String message="The configuration file '"+configFileName+"'\n does not contain an entry for this metadata file ('"+ metaDataFile +"')";
	    if (upfront) {
		message = "The corpus ('"+ ocsi +"') and gui ('"+ ogsi +"') settings passed to the tool were not found in config file \n   '" + configFileName + "'.";
	    }
            Debug.print(message, Debug.ERROR);
            JTextArea msg = new JTextArea(message + ".\nEither exit the tool (press no), change the configuration file and restart the tool,\nor select the appropriate settings in the following dialogs (press yes).\n Continue using the tool?.");
            if (JOptionPane.showConfirmDialog(null, msg, "Continue?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
            corpusSettingsId = selectId("Select the corpussettings that you want to use", corpusSettingsMap);
            guiSettingsId = selectId("Select the guisettings that you want to use", guiSettingsMap);
	    corpusSettingsNode = (Node)corpusSettingsMap.get(corpusSettingsId);
	    guiSettingsNode =(Node)guiSettingsMap.get(guiSettingsId);
        } 
        
        if (corpusSettingsNode == null) {
            Debug.print("No corpussettings found for the metadata file '" + metaDataFile + "' for the config root '" + configRoot + "' with id '" + corpusSettingsId + "'.", Debug.WARNING);
        }
        if (guiSettingsNode == null) {
            Debug.print("No guisettings found for the metadata file '" + metaDataFile + "' for the config root '" + configRoot + "' with id '" + guiSettingsId + "'.", Debug.WARNING);
        }
    }

    /**
     * Asks the user to select one of the keys from the given Map.
     */
    protected String selectId(String msg, Map m) {
        Object[] keys = m.keySet().toArray();
        if (keys.length <= 0) {
            return "";
        }
        return (String)JOptionPane.showInputDialog(null, msg, "choose settings", JOptionPane.QUESTION_MESSAGE, null, keys, keys[0]);
    }
    //modification?
    /**
     * Not yet implemented.
     * Should check whether the settings have been changed, and if so, ask the user whether the changes should be saved.
     */
    public void checkSave() {
        Debug.print("savecheck for config file not yet implemented. Changes may have been lost.", Debug.DEBUG);
    }    
    
    
/**
" Action getLoadConfigAction()", returnt een action die via dialoog een nieuwe config laadt. Deze zou evt gebruikt kunnen worden in e.g. dialogactcoder, MAAR dan moet er wel een goeie reloadcorpus enzo achteraan komen!!!!"
" Action getReloadConfigAction", een action die gewoon de huidige config opnieuw laad
*/

}
