package net.sourceforge.nite.gui.util;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.gui.util.AbstractCallableToolConfig;
import net.sourceforge.nite.gui.transcriptionviewer.TranscriptionToTextDelegate;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>This class contains settings for the AMIGram tool. See also superclass and
 * NXTConfig class. The root name of the AMIGram configuration element is
 * "AMIGramConfig".</p>
 */
public class AmigramConfig extends AbstractCallableToolConfig {

    public static final int TEXT=0;
    public static final int ATTRIBUTE=1;    
    public static final int DELEGATE=2;

    private static AmigramConfig instance = null;
    
    /**
     * <p>Use the static method getInstance()</p>
     */
    private AmigramConfig() {
        super();
    }
    
    /**
     * <p>Returns the singleton AMIGram configuration object. Don't forget to set
     * the metadata file.</p>
     *
     * @return the singleton AMIGram configuration object
     */
    public static AmigramConfig getInstance() {
        if (instance == null)
            instance = new AmigramConfig();
        return instance;
    }

    /**
     * <p>Returns the root name of the Amigram configuration element.</p>
     *
     * @return the root name of the Amigram configuration element
     */
    public String getNXTConfigRootName() {
        return "AMIGramConfig";
    }

    /**
     * <p>Returns a list with all filter elements (Node objects) for the
     * current corpus.</p>
     *
     * @return a list with filter elements
     */
    public List getFilterList() {
        ArrayList result = new ArrayList();
        Node corpusCfg = getNXTConfig().getCorpusSettings();
        NodeList children = corpusCfg.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("filter"))
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
     * <p>Returns the filter element for the specified element name. The element
     * name should be specified in the "elementname" attribute. If there is no
     * such filter element, this method returns null.</p>
     *
     * @param elementname the element name
     * @return the filter element for the specified element or null
     */
    public Node getFilter(String elementname) {
        List list = getFilterList();
        Node result = null;
        for (int i = 0; (result == null) && (i < list.size()); i++) {
            Node filter = (Node)list.get(i);
            if (getFilterElementName(filter).equals(elementname))
                result = filter;
        }
        return result;
    }


    /**
     * <p>Returns the element name of the specified filter element. The
     * elementname is specified in the "elementname" attribute.</p>
     *
     * @param filter a filter element
     * @return the element name of the specified filter element
     */
    public String getFilterElementName(Node filter) {
        return getAttributeValue(filter,"elementname");
    }

    /** 
     * Return the constant TEXT if the given element name should
     * present their text content in AMIGram; ATTRIBUTE if an
     * attribute should be used, and DELEGATE if we should pass the
     * node to a delegate class. This first checks for a specific
     * filter for this element, then the default values, and finally
     * defaults to TEXT if neither is present.
     *
     * @param elementname the element name
     * @return the integer TEXT, ATTRIBUTE or DELEGATE
     */
    public int getFilterType(String elementname) {
	Node fn=getFilter(elementname);
	if (fn!=null) { return getFilterType(fn); }
	return defaultFilterType();
    }     
    
    /**
     * Return the constant TEXT if the given element name should
     * present their text content in AMIGram, or ATTRIBUTE if an
     * attribute should be used. Note that DELEGATE is never returned
     * by this method as if a delegate class is to be used, it should
     * be defined in the defaultfilterdelegate class attribute of the
     * corpussettings element. The filter type should be specified in
     * the "filtertype" attribute and be either "attribute" or
     * "text".</p>
     *
     * @param filter a filter element
     * @return the filter type of the filter element
     */
    public int getFilterType(Node filter) {
	String atval = getAttributeValue(filter,"filtertype");
	if (atval.equalsIgnoreCase("attribute")) {
	    return ATTRIBUTE;
	} 
	return TEXT;
    }

    /**
     * <p>Returns the filter attribute name for the given element
     * name. If the element has its own filter node, we use that
     * value, and otherwise the default. This method returns null if
     * neither is present or if it's a TEXT or DELEGATE filter.
     *
     * @param elementname an element name
     * @return the attribute name for the filter element, or null
     */
    public String getFilterAttributeName(String elementname) {
	Node fn=getFilter(elementname);
	if (fn!=null) { 
	    return getFilterAttributeName(fn); 
	}
	return defaultFilterAttributeName();
    }

    /**
     * <p>Returns the filter attribute name of the filter element. The
     * name should be specified in the "attname" attribute, but will
     * only be present if the filter type is ATTRIBUTE rather than
     * TEXT.
     *
     * @param filter a filter element
     * @return the attribute name for the filter element
     */
    public String getFilterAttributeName(Node filter) {
        return getAttributeValue(filter,"attname");
    }
    
    /**
     * Return "true" if all elements should default to presenting
     * their text content in AMIGram and "false" otherwise. This
     * default can be overridden by specific filters.
     *
     * @return "true" if AMIGram should default to text content as presentation mode.
     */
    public boolean defaultToText() {
        String val = getNXTConfig().getCorpusSettingValue("defaultfiltertext");
        if (val == null)
            return false;
        else
            return Boolean.valueOf(val).booleanValue();
    }

    protected TranscriptionToTextDelegate tttDelegate = null;
    /**
     * Default: new instance of class defined in
     * transcriptiondelegateclassname attribute in corpussettings
     */
    public TranscriptionToTextDelegate getFilterDelegate() {
        if (tttDelegate == null) {
            String delegateName = defaultDelegateName();
            try {
                tttDelegate = (TranscriptionToTextDelegate)Class.forName(delegateName).newInstance();
            } catch (Exception e) {
                System.err.println("FAILED TO INSTANTIATE DELEGATE CLASS '" + delegateName + "'.");
            }
        }
        return tttDelegate;
    }

    /**
     * Return a String representing th ename of a delegate class: the
     * value of the defaultfilterdelegateclass attribute of the
     * corpussettings element.
     *
     * @return the name of the default delegate class
     */
    public String defaultDelegateName() {
        return getNXTConfig().getCorpusSettingValue("defaultfilterdelegateclass");
    }


    /**
     * Return the constant TEXT if all elements should default to
     * presenting their text content; ATTRIBUTE if an attribute should
     * be used; or DELEGATE if a delegate class is teh default
     * behaviour. This default can be overridden by specific
     * filters. The default if nothing is declared should be TEXT.
     *
     * @return TEXT if AMIGram should default to text content as
     * presentation mode and ATTRIBUTE if an attribute should be used
     * or DELEGATE if a delegate class is the default handler.
     */
    public int defaultFilterType() {
	if (getFilterDelegate()!=null) { return DELEGATE; }
	if (defaultFilterAttributeName()!=null) { return ATTRIBUTE; }
	//if (defaultToText()) { return TEXT; }
	return TEXT;
    }

    /**
     * Return the name of the attribute AMIGram should use as the
     * default presentation attribute. This default can be null and
     * can be overridden by specific filters.
     *
     * @return the default attribute AMIGram should use for display.
     */
    public String defaultFilterAttributeName() {
        return getNXTConfig().getCorpusSettingValue("defaultfilterattribute");
    }

    /**
     * Return the name of the attribute AMIGram should use as the
     * default presentation attribute. This default can be null and
     * can be overridden by specific filters.
     *
     * @return the default attribute AMIGram should use for display.
     */
    public String defaultFilterDelegateClass() {
        return getNXTConfig().getCorpusSettingValue("defaultfilterdelegateclass");
    }

    /**********************
     *  APPLY FILTER      *
     **********************/

    /** Return the String to be displayed for the NOM element
     *
     * @ param nel the NOMElement that we are displaying
     * @return the String to be displayed for the NOMElement
     */
    public String applyFilter(NOMElement nel) {
	if (nel==null || nel.getName()==null) { return (String)null; }
	int ftype = getFilterType(nel.getName());
	if (ftype==ATTRIBUTE) {
	    return (String)nel.getAttributeComparableValue(getFilterAttributeName(nel.getName()));
	}
	if (ftype==DELEGATE) {
	    return tttDelegate.getTextForTranscriptionElement(nel);
	}
	return nel.getText();
    }
    
}
