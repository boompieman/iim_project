/**
 * 
 */
package net.sourceforge.nite.gui.util;

import java.util.Iterator;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JTextPane;

import net.sourceforge.nite.nom.nomwrite.NOMAttribute;
import net.sourceforge.nite.nom.nomwrite.NOMElement;

/**
 * @author cnicol1
 *
 */
public class GenericNOMElementViewer extends JInternalFrame implements
		NOMElementSelectionListener {

	JTextPane text;
	
	/**
	 * 
	 */
	public GenericNOMElementViewer() {
		super();
		// TODO Auto-generated constructor stub
		text = new JTextPane();
		this.add(text);
	}


	private String printAttributes(List atts) {
		String out = "";
		Iterator it = atts.iterator();
		while (it.hasNext()) {
			NOMAttribute a = (NOMAttribute) it.next();
			out += a.getName() + " : " + a.getStringValue() + "\n";
		}
		return out;
	}
	
	/* (non-Javadoc)
	 * @see net.sourceforge.nite.gui.util.NOMElementSelectionListener#selectionChanged(java.util.List)
	 */
	public void selectionChanged(List newSelection) {
		//System.out.println("selectionChanged: " + newSelection.size() + " element(s).");
		StringBuffer t = new StringBuffer();
		Iterator it = newSelection.iterator();
		while(it.hasNext()) {
			NOMElement ne = (NOMElement) it.next();
			t.append(ne.getName() + " : " + ne.getID() + "\n(" + ne.getStartTime() + " - " + ne.getEndTime() + ")\n" + printAttributes(ne.getAttributes()) + "---------------------\n\n");
		}
		text.setText(t.toString());
		text.repaint();
	}

}
