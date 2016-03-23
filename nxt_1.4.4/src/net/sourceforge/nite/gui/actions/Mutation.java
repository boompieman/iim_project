/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

/**
 * @author judyr
 *Mutation represent the changes which can be made to the xml object model when the
 * user interacts with the interface. You can either add or delete an element. If adding
 * and element, you can add an attribute or some new content. A new attribute can have a name and a value associated
 * with it. To modify an attribute in an existing element, add an attribute with the same name with the new 
 * value and it will replace the old attribute. When adding content, you can either 
 * add a new textual value for the element, or give it a new child by specifying the id of the new child
 * This design is taken from Jonathan Kilgour's document "Actions within the NITE GUI specification"
 * written on 26/03/02
 */
public class Mutation {
	
	public static final String add_element = "ADD_ELEMENT";
	public static final String add_attribute = "ADD_ATTRIBUTE";
	public static final String add_content = "ADD_CONTENT";
	public static final String delete_element = "DELETE_ELEMENT";
	private String type;
	private Content content;
	private Attribute attribute;
	
	
	public Mutation(String t){
		type = t;
	}
	
	
	public void setContent(String textcontent, String child){
		content = new Content();
		if (textcontent != null) content.setStringContent(textcontent);
		else if (child != null) content.setChildId(child);
	}
	
	public void setAttribute(String name, String value){
		attribute = new Attribute();
		if (name != null) attribute.setAttributeName(name);
		if (value != null) attribute.setAttributeValue(value);
	}
	class Content{
		String stringContent = "";
		String childId;	
		
        /**
         * Returns the childId.
         * @return String
         */
        public String getChildId() {
            return childId;
        }

        /**
         * Returns the stringContent.
         * @return String
         */
        public String getStringContent() {
            return stringContent;
        }

        /**
         * Sets the childId.
         * @param childId The childId to set
         */
        public void setChildId(String childId) {
            this.childId = childId;
        }

        /**
         * Sets the stringContent.
         * @param stringContent The stringContent to set
         */
        public void setStringContent(String stringContent) {
            this.stringContent = stringContent;
        }

	}
	class Attribute{
		String attributeName;
		String attributeValue;
		
        /**
         * Returns the attributeName.
         * @return String
         */
        public String getAttributeName() {
            return attributeName;
        }

        /**
         * Returns the attributeValue.
         * @return String
         */
        public String getAttributeValue() {
            return attributeValue;
        }

        /**
         * Sets the attributeName.
         * @param attributeName The attributeName to set
         */
        public void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        /**
         * Sets the attributeValue.
         * @param attributeValue The attributeValue to set
         */
        public void setAttributeValue(String attributeValue) {
            this.attributeValue = attributeValue;
        }

	}

    /**
     * Returns the attribute.
     * @return Attribute
     */
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * Returns the content.
     * @return Content
     */
    public Content getContent() {
        return content;
    }

    /**
     * Sets the attribute.
     * @param attribute The attribute to set
     */
    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    /**
     * Sets the content.
     * @param content The content to set
     */
    public void setContent(Content content) {
        this.content = content;
    }

}
