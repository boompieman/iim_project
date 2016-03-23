package net.sourceforge.nite.tools.videolabeler;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.nite.gui.util.AbstractCallableToolConfig;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>This class contains settings for the CSL tool. See also superclass and
 * NXTConfig class. The root name of the CSL configuration element is
 * "CSLConfig".</p>
 */
public class CSLConfig extends AbstractCallableToolConfig {
    private static CSLConfig instance = null;
    
    /**
     * <p>Use the static method getInstance()</p>
     */
    private CSLConfig() {
        super();
    }
    
    /**
     * <p>Returns the singleton CSL configuration object. Don't forget to set
     * the metadata file.</p>
     *
     * @return the singleton CSL configuration object
     */
    public static CSLConfig getInstance() {
        if (instance == null)
            instance = new CSLConfig();
        return instance;
    }

    /**
     * <p>Returns the root name of the CSL configuration element.</p>
     *
     * @return the root name of the CSL configuration element
     */
    public String getNXTConfigRootName() {
        return "CSLConfig";
    }

    /**
     * <p>Returns a list with all layerinfo elements (Node objects) for the
     * current corpus.</p>
     *
     * @return a list with layerinfo elements
     */
    public List getLayerInfoList() {
        ArrayList result = new ArrayList();
        Node corpusCfg = getNXTConfig().getCorpusSettings();
        NodeList children = corpusCfg.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("layerinfo"))
                result.add(child);
        }
        return result;
    }
    
    /**
     * <p>Returns the value of an attribute of the specified node. If the
     * node does not have the specified attribute, this method returns
     * null.</p>
     *
     * @param node the node that contains the attribute
     * @param attrName the name of the attribute
     * @return the attribute value or null
     */
    public String getAttributeValue(Node node, String attrName) {
        NamedNodeMap attrs = node.getAttributes();
        if (attrs == null)
            return null;
        Node attr = attrs.getNamedItem(attrName);
        if (attr == null)
            return null;
        return attr.getNodeValue();
    }
    
    /**
     * <p>Returns the layerinfo element for the specified layer. The layer
     * name should be specified in the "layername" attribute. If there is no
     * such layerinfo element, this method returns null.</p>
     *
     * @param layername the layer name
     * @return the layerinfo element for the specified layer or null
     */
    public Node getLayerInfo(String layername) {
        List list = getLayerInfoList();
        Node result = null;
        for (int i = 0; (result == null) && (i < list.size()); i++) {
            Node layerinfo = (Node)list.get(i);
            if (getLayerName(layerinfo).equals(layername))
                result = layerinfo;
        }
        return result;
    }
    
    /**
     * <p>Returns the layer name of the specified layerinfo element. The
     * layer name should be specified in the "layername" attribute.</p>
     *
     * @param layerinfo a layerinfo element
     * @return the layer name of the specified layerinfo element
     */
    public String getLayerName(Node layerinfo) {
        return getAttributeValue(layerinfo,"layername");
    }
    
    /**
     * <p>Returns the qualified class name of the AnnotationLayer
     * subclass for the specified layerinfo element. The class name
     * should be specified in the "layerclass" attribute.</p>
     *
     * @param layerinfo a layerinfo element
     * @return the class name of an AnnotationLayer subclass
     */
    public String getLayerClass(Node layerinfo) {
        return getAttributeValue(layerinfo,"layerclass");
    }
    
    /**
     * <p>Returns the qualified class name of the TargetControlPanel subclass
     * for the specified layerinfo element. The class name should be specified
     * in the "controlpanelclass" attribute.</p>
     *
     * @param layerinfo a layerinfo element
     * @return the class name of a TargetControlPanel subclass
     */
    public String getLayerControlPanelClass(Node layerinfo) {
        return getAttributeValue(layerinfo,"controlpanelclass");
    }
    
    /**
     * <p>Returns the name of the code elements that represent the annotations
     * for the specified layerinfo element. The code name should be specified
     * in the "codename" attribute.</p>
     *
     * @param layerinfo a layerinfo element
     * @return the name of the code elements
     */
    public String getLayerCodeName(Node layerinfo) {
        return getAttributeValue(layerinfo,"codename");
    }
    
    /**
     * <p>Returns the value of the "autokeystrokes" attribute. If it is true,
     * the tool should automatically create keystrokes for actions that do not
     * have a keystroke in the corpus data, that have an invalid keystroke or
     * whose keystroke is already in use. If the attribute is not available,
     * this method returns the default value false.</p>
     *
     * @return true if the tool should automatically create keystrokes, false
     * otherwise
     */
    public boolean autoKeyStrokes() {
        String val = getNXTConfig().getGuiSettingValue("autokeystrokes");
        if (val == null)
            return false;
        else
            return Boolean.valueOf(val).booleanValue();
    }
    
    /**
     * <p>Returns the value of the "continuous" attribute. If it is true, the
     * tool will ensure that the annotations remain continuous. Gaps in the
     * time line are prevented when new annotations are added or existing
     * annotations are deleted. If the attribute is not available, this method
     * return the default value true.</p>
     *
     * @return true if the tool should ensure that annotations remain
     * continuous
     */
    public boolean makeContinuous() {
        String val = getNXTConfig().getGuiSettingValue("continuous");
        if (val == null)
            return true;
        else
            return Boolean.valueOf(val).booleanValue();
    }
    
    /**
     * <p>Returns the value of the "merge" attribute. @@@@@DOC
     */
    public boolean merge() {
        String val = getNXTConfig().getGuiSettingValue("merge");
        if (val == null)
            return true;
        else
            return Boolean.valueOf(val).booleanValue();
    }    
    
    /**
     * <p>Returns the value of the "syncrate" attribute. That is the number
     * of milliseconds between time change events from the NXT clock. If
     * the attribute is not available, this method returns the default value
     * 200.</p>
     *
     * @return the synchronization rate
     */
    public int getSyncRate() {
        String val = getNXTConfig().getGuiSettingValue("syncrate");
        if (val == null)
            return 200;
        else {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException ex) {
                return 200;
            }
        }
    }

    /**
     * <p>Returns the value of the "showkeystrokes" attribute. This can be
     * one of three values.</p>
     *
     * <p><ul>
     * <li>off: the keystrokes won't be shown in the GUI</li>
     * <li>tooltip: a keystroke will be shown in the tooltip of a control</li>
     * <li>label: a keystroke will be shown in the label of a control (before
     * the actual label)</li>
     * </ul></p>
     *
     * <p>If the attribute value is not one of these three values, it defaults
     * to "off".</p>
     *
     * @return "off", "tooltip" or "label"
     */
    public String getShowKeyStrokes() {
        String result = getNXTConfig().getGuiSettingValue("showkeystrokes");
        if (result == null)
            return "off";
        if (!result.equals("off") && !result.equals("tooltip") && !result.equals("label"))
            return "off";
        return result;
    }

    /**
     * segmentreplayer attribute in config. if true, the segmentreplayer will be activated
     */
    public boolean getUseSegmentReplayer() {
        String setting = getNXTConfig().getCorpusSettingValue("segmentreplayer");
        if (setting == null)
            return false;
        if (setting.toLowerCase().equals("true"))
            return true;
        return false;
    }
        
    
    /**
     * <p>Returns the name of the help set.</p>
     *
     * @return the name of the help set
     */
    public String getHelpSetName() {
        return "videolabeler.hs";
    }

    /**
     * <p>Returns the type of time display we prefer ('seconds' or 'minutes').</p>
     *
     * @return the preferred time display
     */
    public String getPreferredTimeDisplay() {
        String result = getNXTConfig().getGuiSettingValue("timedisplay");
        if (result == null)
            return "seconds";
        if (!result.equals("minutes") && !result.equals("seconds"))
            return "seconds";
        return result;
    }
}
