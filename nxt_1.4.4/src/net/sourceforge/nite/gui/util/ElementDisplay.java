/**
 * A utility class to display an element and (optionally) to edit it.
 * Updates are made through the shared NOM interface so that listeners
 * can be notified of changes (though you can also just call isChanged
 * depending on your interaction approach).
 * 
 * 
 * Jonathan Kilgour 18/4/08
 */
package net.sourceforge.nite.gui.util;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Container;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.util.Debug;

import com.jgoodies.looks.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

/** a utility class for viewing and editing arbitrary elements in the
 * NOM. Note that nothing is written to the NOM until applyChanges is
 * called from the calling program */
public class ElementDisplay implements NOMView {
    private boolean editable=true;
    NOMElement element;    // the element to edit
    private JPanel thePanel = null; // panel containing the control buttons plus..
    private JPanel internalPanel = null; // internal panel containing the main
    boolean changed=false; // individual element changes
    boolean globalchange=false; // any element changed in this panel
    Set changes;
    Hashtable attributes = new Hashtable(); // maps components to NAttributes (or names of attrs)
    CellConstraints cc = new CellConstraints();
    NMetaData metadata;

    /** return the Panel containing the edit / display GUI */
    public JComponent getPanel() {
        return thePanel;
    }

    /** set the editability of this window */
    public void setEditable(boolean edit) {
	System.out.println("Set editable: " + edit);
	editable=edit;
    }

    /** have we made changes to the element or any since this window has opened? */
    public boolean isChanged() {
	return globalchange;
    }


    /** apply any changes to the NOM that have accumulated and
     * save the changes to disk */
    public void applyChanges() {
	for (Iterator cit=changes.iterator(); cit.hasNext(); ) {
	    JComponent component = (JComponent)cit.next();
	    applyChange(component, attributes.get(component));
	}
	clearChanges();
    }

    /** clear changes */
    public void clearChanges() {
	changed=false;
	changes=new HashSet();
    }

    /** change element we're displaying */
    public void changeElement(NOMElement element) {
	if (changed) {
	    if (checkExit()==JOptionPane.CANCEL_OPTION) { return; }
	}
	thePanel.setVisible(false);
	thePanel.remove(internalPanel);
	this.element=element;
	createGUI();
	thePanel.add(internalPanel, cc.xyw(1, 1, 3));
	thePanel.setVisible(true);
    }
    
    /** Simple constructor that constructs a display that has the
     * default edit-ability (editable) for the given element */
    public ElementDisplay(NOMElement element) {
	constructorCommon(element, true);
    }

    /** Simple constructor specifying element and editability of the
     * window. */
    public ElementDisplay(NOMElement element, boolean editable) {
	constructorCommon(element, editable);
    }

    private void constructorCommon(NOMElement element, boolean editable) {
	this.editable=editable;
	this.element=element;
	createGUI();
	createMainPanel();
    }

    // create the main Panel that doesn't change when element changes
    private void createMainPanel() {
	try {
	    FormLayout layout = new FormLayout(
	       "left:pref, 3dlu:grow, min",
	       "pref, 3dlu, pref, 3dlu, pref"); 
	    thePanel = new JPanel(layout);
	    thePanel.add(internalPanel, cc.xyw(1, 1, 3));
	    if (editable) {
		JButton okbutton=new JButton("OK");
		okbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    applyChanges();
			}});
		thePanel.add(okbutton, cc.xy(1,3));
		JButton cancelbutton=new JButton("Cancel");
		cancelbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    clearChanges();
			    changeElement(element);
			}			
		    });
		thePanel.add(cancelbutton, cc.xy(3,3));
	    }
	} catch (Exception ex) {
	    Debug.print("Failed to set up main GUI", Debug.ERROR);
	    ex.printStackTrace();
	}	
    }

    /** check for changes and if there are any pop up a question to
     * user */
    public int checkExit() {
	return new CheckExit().popupDialog();
    }

    // create the JPanel containing all the attribute displays
    private void createGUI() {
	try {
	    changes = new HashSet(); // reset list of changed JComponents
	    NElement metaelement = element.getMetadataElement();
	    metadata = element.getCorpus().getMetaData();
	    List metaatts = metaelement.getAttributes();
	    // Number of slots to show = ID + children + pointers (3 in
	    // total) + start and end times (2 if present) + attributes +
	    // text content (if present)
	    int displayelements = 3; 
	    boolean timed=false;
	    NLayer layer = metaelement.getLayer();
	    if (layer!=null && (layer.getLayerType()==NLayer.TIMED_LAYER || layer.getLayerType()==NLayer.STRUCTURAL_LAYER)) {
		displayelements+=2;
		timed=true;
	    }
	    if (metaatts!=null) { displayelements += metaatts.size(); }
	    if (metaelement.textContentPermitted()) { displayelements+=1; }
	    FormLayout layout = new FormLayout(
	       "left:pref, 3dlu, fill:100dlu:grow, 3dlu, pref",
	       "p, 3dlu, p, 3dlu, p, 3dlu, center:min:grow, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); 	    
	    PanelBuilder builder = new PanelBuilder(layout);
	    builder.setDefaultDialogBorder();
	    CellConstraints cc = new CellConstraints();
	    String agentstring="";
	    if (element.getAgentName()!=null) {
		agentstring=" / speaker " + element.getAgentName();
	    }
            builder.addSeparator(element.getName()+agentstring, cc.xyw(1, 1, 5));
	    builder.addLabel("ID:", cc.xy(1, 3));
	    int numdone=2; 
	    int children=0;
	    if (element.getChildren()!=null) { children=element.getChildren().size(); }
	    int pointers=0;
	    if (element.getPointers()!=null) { pointers=element.getPointers().size(); }
	    builder.addLabel(element.getID() + " (" +children+" children; "+pointers+" pointers)", cc.xy(3, 3));
	    int ypos = (numdone*2)+1; numdone++;	    

	    // Start and end times (probably a bad idea to allow edits!!
	    if (timed) {
		builder.addLabel("Start time:", cc.xy(1, ypos));
		JFormattedTextField stText = new JFormattedTextField(java.text.NumberFormat.getInstance());
		// question: do we ever want to edit start and ends this way??
		stText.setEditable(false);
		if (!Double.isNaN(element.getStartTime())) {
		    stText.setValue(new Float(element.getStartTime()));
		}
		builder.add(stText, cc.xy(3, ypos,"fill,fill"));
		attributes.put(stText, "starttime");
		ypos = (numdone*2)+1; numdone++;
		builder.addLabel("End time:", cc.xy(1, ypos));
		JFormattedTextField etText = new JFormattedTextField(java.text.NumberFormat.getInstance());
		etText.setEditable(false);
		if (!Double.isNaN(element.getEndTime())) {
		    etText.setValue(new Float(element.getEndTime()));
		}
		builder.add(etText, cc.xy(3, ypos,"fill,fill"));
		attributes.put(etText, "endtime");
	    }

	    // text content if legal
	    if (metaelement.textContentPermitted()) {
		ypos = (numdone*2)+1; numdone++;
		builder.addLabel("Text content:", cc.xy(1, ypos));
		// easier as a text field!
		JTextField elText = new JTextField();
		elText.setText(element.getText());
		if (!editable) { elText.setEditable(false); }
		builder.add(elText, cc.xy(3, ypos,"fill,fill"));
		attributes.put(elText, "text_content");
		if (editable) {
		    elText.addActionListener(new AttributeChangeActionListener(elText));
		}
	    }

	    // attributes
	    if (metaatts==null) { System.out.println(" No attributes. "); }
	    else {
		for (Iterator ait=metaatts.iterator(); ait.hasNext(); ) {
		    NAttribute att = (NAttribute)ait.next();
		    ypos = (numdone*2)+1; numdone++;
		    builder.addLabel(att.getName()+":", cc.xy(1, ypos));
		    
		    //System.err.println("Adding attribute " + att.getName() + " which is editable: " + editable);
		    
		    if (!editable) {
			JTextField attfield=new JTextField();
			attfield.setText((String)element.getAttributeComparableValue(att.getName()));
			attfield.setEditable(false);
			builder.add(attfield, cc.xy(3, ypos,"fill,fill"));
			attributes.put(attfield, att);
		    } else {
			if (att.getType()==NAttribute.STRING_ATTRIBUTE) {
			    JTextField jtf=new JTextField();
			    String val=(String)element.getAttributeComparableValue(att.getName());
			    jtf.setText(val);
			    jtf.addKeyListener(new editKeyListener());
			    builder.add(jtf, cc.xy(3, ypos,"fill,fill"));
			    attributes.put(jtf, att);
			} else if (att.getType()==NAttribute.NUMBER_ATTRIBUTE) {
			    JFormattedTextField jftf=new JFormattedTextField(java.text.NumberFormat.getInstance());
			    Float val = new Float(element.getAttribute(att.getName()).getDoubleValue().floatValue());
			    try { jftf.setValue(val); } 
			    catch (Exception ex) { }
			    jftf.addKeyListener(new editKeyListener());
			    builder.add(jftf, cc.xy(3, ypos,"fill,fill"));
			    attributes.put(jftf, att);
			} else if (att.getType()==NAttribute.ENUMERATED_ATTRIBUTE) {
			    JComboBox jcb=new JComboBox();
			    jcb.addItem("");
			    for (Iterator vit=att.getEnumeratedValues().iterator(); vit.hasNext(); ) {
				jcb.addItem((String)vit.next());
			    }
			    String val=(String)element.getAttributeComparableValue(att.getName());
			    jcb.setSelectedItem(val);
			    jcb.addActionListener(new AttributeChangeActionListener(jcb));
			    builder.add(jcb, cc.xy(3, ypos,"fill,fill"));
			    attributes.put(jcb, att);
			}
		    }
		}
	    }

	    // keystroke associated with element (only if it's not an annotation)
	    if (metaelement.getContainerType()!=NElement.CODING) {
		ypos = (numdone*2)+1; numdone++;
		builder.addLabel("keystroke:", cc.xy(1, ypos));
		JTextField jtf=new JTextField();
		jtf.setText(element.getKeyStroke());
		jtf.addKeyListener(new editKeyListener());
		builder.add(jtf, cc.xy(3, ypos,"fill,fill"));
		attributes.put(jtf, metadata.getKeyStrokeAttributeName());
	    }

	    // comment associated with element
	    ypos = (numdone*2)+1; numdone++;
	    builder.addLabel("comment:", cc.xy(1, ypos));
	    JTextField jtf=new JTextField();
	    jtf.setText(element.getComment());
	    jtf.addKeyListener(new editKeyListener());
	    builder.add(jtf, cc.xy(3, ypos,"fill,fill"));
	    attributes.put(jtf, metadata.getCommentAttributeName());
	    
	    internalPanel = builder.getPanel();
	} catch (Exception ex) {
	    Debug.print("Failed to set up Element GUI", Debug.ERROR);
	    ex.printStackTrace();
	}
    }

    /** apply a single change to the NOM */
    private void applyChange(JComponent component, Object attr) {
	if (component==null || attr==null) {
	    Debug.print("Failed to set attribute value in ElmentDisplay", Debug.ERROR);
	    return;
	}
	NAttribute attribute=null;
	String attributename=null;
	if (attr instanceof NAttribute) { 
	    attribute=(NAttribute)attr; 
	    attributename=attribute.getName();
	} else if (attr instanceof String) { 
	    attributename=(String)attr; 
	}
	if (component instanceof JTextField) {
	    try {
		if (attribute==null) {
		    if (attributename.equals("text_content")) {
			element.getShared().setText(this, ((JTextField)component).getText());
		    } else if (attributename.equals(metadata.getKeyStrokeAttributeName())) {
			element.setKeyStroke(((JTextField)component).getText());
		    } else if (attributename.equals(metadata.getCommentAttributeName())) {
			element.setComment(((JTextField)component).getText());
		    }
		} else if (attribute.getType()==NAttribute.NUMBER_ATTRIBUTE) {
		    element.getShared().setDoubleAttribute(this, attributename, new Double(((JTextField)component).getText()));
		} else {
		    element.getShared().setStringAttribute(this, attributename, ((JTextField)component).getText());
		}
		//changed=true;
	    } catch (NOMException nex) {
		Debug.print("Failed to set "+attributename+" attribute for element "+element.getID()+": "+ ((JTextField)component).getText(), Debug.ERROR);
	    }
	} else if (component instanceof JComboBox) {
	    try {
		element.getShared().setStringAttribute(this, attributename, (String)((JComboBox)component).getSelectedItem());
		//changed=true;
	    } catch (NOMException nex) {
		Debug.print("Failed to set "+attributename+" attribute for element "+element.getID()+": "+ ((JComboBox)component).getSelectedItem(), Debug.ERROR);
	    } 
	} else if (component instanceof JTextArea) {
	    try {
		element.getShared().setText(this, ((JTextArea)component).getText());
		changed=true;
	    } catch (Exception ex) { }
	}
	System.out.println("Set global change");
	globalchange=true;
    }


    // Fror text elements, we decided to use a KeyListener to detect
    // changes, but there's one type we need ActionListener for:
    // combo-boxes (for enumerated attributes).
    class AttributeChangeActionListener implements ActionListener {
	JComponent component;

	public AttributeChangeActionListener(JComponent component) {
	    this.component=component;
	}

	public void actionPerformed(ActionEvent e) {
	    if (component instanceof JComboBox) {
		changes.add(component);
		changed=true;
	    } 
	}
    }

    /** KeyListener implementation that improves navigation between
     * elements on the display and notes any changes in text elements
     * for the whole display */
    class editKeyListener extends KeyAdapter {
	DefaultFocusManager focusManager = new DefaultFocusManager();

	public void keyPressed(KeyEvent e) {
	    int iKey = e.getKeyCode();
	    JComponent component = (JComponent)e.getComponent();
	    if ((iKey == KeyEvent.VK_ENTER) || (iKey == KeyEvent.VK_DOWN) ||
		(iKey == KeyEvent.VK_PAGE_DOWN) || (iKey == KeyEvent.VK_TAB)) {
		focusManager.focusNextComponent(component);
	    } else if (iKey == KeyEvent.VK_UP) {
		focusManager.focusPreviousComponent(component);
	    } 
	}

	public void keyTyped(KeyEvent e) {
	    int iKey = e.getKeyCode();
	    JComponent component = (JComponent)e.getComponent();
	    if ((iKey != KeyEvent.VK_ENTER) && (iKey != KeyEvent.VK_TAB)) {
		changes.add(component);
		//System.out.println("Change");
		changed=true;
	    }
	}
    }


    class CheckExit extends JDialog {

	/* Saves the changes if yes clicked. Returns
	   JOptionPane.YES_OPTION, JOtionPane.NO_OPTION or
	   JOptionPane.CANCEL_OPTION */
	public int popupDialog() {
	    if (!changed) { return JOptionPane.YES_OPTION; }
	    int ret= JOptionPane.showConfirmDialog(thePanel,"Save changes?","Save changes?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	    if (ret==JOptionPane.YES_OPTION) {
		applyChanges();
	    } else if (ret==JOptionPane.NO_OPTION) {
		clearChanges();
	    } 
	    return ret;
	}

    }

    /** Implementation of NOMView interface - just ignore edits for now */
    public void handleChange(NOMEdit edit) {

    }

}
