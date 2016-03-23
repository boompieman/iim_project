/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
// NConstants.java
package net.sourceforge.nite.nstyle;


import java.awt.Color;
import javax.swing.plaf.ColorUIResource;
/** NITE Constants.
 * Mainly constants used in the stylesheet language
 *
 * @author Jonathan Kilgour
 * @see NHandler
 */
public class NConstants {

    // NOM Attribute names - should be defined as metadata!
    public static final String nomStartTime="start";
    public static final String nomEndTime="end";

    // Interface Specification Attribute names
    public static final String objectType="type";
    public static final String sourceID="nitesourceid";
    public static final String objectTitle="title";
    public static final String objectName="name";  // shouldn't use both!
    public static final String splitPaneSplit="split";
    public static final String panePosition="place";
    public static final String layout="layout";
    public static final String fontSize="FontSize";
    public static final String fontStyle="FontStyle";
    public static final String style = "style";
    public static final String Name = "name";
    public static final String font="Font";
    public static final String toolTip = "tooltip";
    public static final String tabStop ="tabstop";
    public static final String foregroundColour="textcolour";
    public static final String backgroundColour="background";
    public static final String attributeName="attribute";
    public static final String actionID="actionID";
    public static final String targetID="target_id";
    public static final String id="id";
    public static final String name="name";
    public static final String number="number";
    public static final String primary1="primary1";
    public static final String primary2="primary2";
    public static final String primary3="primary3";
    public static final String secondary1="secondary1";
    public static final String secondary2="secondary2";
    public static final String secondary3="secondary3";
    public static final String type="type";
    public static final String description="description";
    public static final String mouseButton="button";

    // Attribute values
    // fonts
    public static final String bold="bold";
    public static final String italic="italic";
    public static final String boldItalic="bolditalic";
    // gui display object names    
    public static final String Border = "Border";
    public static final String Button="Button";
    public static final String SplitPane="SplitPane";
    public static final String TextArea="TextArea";
    public static final String TextElement="TextElement";
    public static final String InternalFrame="InternalFrame";
    public static final String ToolBar = "ToolBar";
    public static final String TabbedPane = "TabbedPane";
    public static final String ScrollPane = "ScrollPane";
    public static final String Menu = "Menu";
    public static final String RadioButton = "RadioButton";
    public static final String Checkbox = "Checkbox";
    public static final String Label = "Label";
    public static final String ListItem = "ListItem";
    public static final String MenuItem = "MenuItem";
    public static final String Pane="Pane";
    public static final String List="List";
    public static final String GridPanel = "GridPanel";
    public static final String GridPanelEntry = "GridPanelEntry";
    public static final String Columns = "Columns";
    public static final String RowSpan = "RowSpan";
    public static final String ColSpan= "ColSpan";
    public static final String Tree = "Tree";
    public static final String TimedLabel ="TimedLabel";
    public static final String InformationLabel = "InformationLabel";
    public static final String ImagePath = "ImagePath";
    public static final String LeafImage = "LeafImage";
    public static final String OpenImage = "OpenImage";
    public static final String ClosedImage = "ClosedImage";
    public static final String ExpandedImage = "ExpandedImage";
    public static final String CollapsedImage = "CollapsedImage";
    public static final String TreeRoot = "TreeRoot";
    public static final String TreeNode = "TreeNode";
    
   
    // other attribute values
    public static final String left="left";
    public static final String right="right";
    public static final String centre ="centre";
    public static final String position = "position";
    public static final String fill = "fill";
    // gui element names
    public static final String root="root";
    public static final String action="action";
    public static final String actions="actions";
    public static final String actionRef="actionref";
    public static final String target="target";
    public static final String targets="targets";
    public static final String mutation="mutation";
    public static final String MutationContent = "MutationContent";
    public static final String MutationSwingTargetId = "MutationSwingTargetId";
    public static final String SwingTargetType = "SwingTargetType";
    public static final String SwingTargetId = "SwingTargetId";
    public static final String SwingTargetData ="SwingTargetData";
    public static final String MutationChildId = "MutationChildId";
    public static final String mutations="mutations";
    public static final String addAttributeName="addAttributeName";
     public static final String addAttributeValue="addAttributeValue";
    public static final String display="display";
    public static final String lookAndFeel="lookandfeel";
    public static final String displayObject="displayobject";
    public static final String key="key";
    public static final String keyBinding="keybinding";
    public static final String keyBindings="keybindings";
    public static final String keyPress="keypress";
    public static final String mouseClick="mouseclick";
    public static final String mousePress="mousepress";
    public static final String actionId = "ActionId";
    public static final String userInput = "UserInput";

    // Miscelaneous methods
   
    public static String DisplayType = "DisplayType";
    public static String ComponentID ="ComponentID";
    public static String constant = "Constant";
    public static String value = "Value";
    public static String arity ="Arity";
    public static String source = "source";
    public static String dialoguebox = "dialoguebox";
    public static Color getColour(String colour) {
    	
	if (colour==null) return null;
	if (colour.equalsIgnoreCase("Blue"))     return Color.blue;
	
	if (colour.equalsIgnoreCase("Cyan"))      return Color.cyan;
	if (colour.equalsIgnoreCase("DarkGray"))  return Color.darkGray;
	if (colour.equalsIgnoreCase("Gray") || colour.equalsIgnoreCase("Grey")) return Color.gray;
	if (colour.equalsIgnoreCase("Green"))     return Color.green;
	if (colour.equalsIgnoreCase("LightGray")) return Color.lightGray;
	if (colour.equalsIgnoreCase("Magenta"))   return Color.magenta;
	if (colour.equalsIgnoreCase("Orange"))    return Color.orange;
	if (colour.equalsIgnoreCase("Pink"))      return Color.pink;
	if (colour.equalsIgnoreCase("Red"))       return Color.red;
	if (colour.equalsIgnoreCase("White"))     return Color.white;
	if (colour.equalsIgnoreCase("Yellow"))    return Color.yellow;
	if (colour.equalsIgnoreCase("Black"))   return Color.black;
	if (colour.equalsIgnoreCase("Purple"))   new Color(127,0,127);
	if (colour.startsWith("#") && colour.length()==7) {  // html format
	    try {
		// The indices for substring seem to have changed!
		return new ColorUIResource(new Color(Integer.parseInt(colour.substring(1,3),16),
				 Integer.parseInt(colour.substring(3,5),16),
				 Integer.parseInt(colour.substring(5,7),16)));
	    } catch (NumberFormatException e) {
		e.printStackTrace();
		System.exit(0);
	    }
	}
	return null;
    }

}


