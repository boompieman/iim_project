package net.sourceforge.nite.tools.videolabeler;

import net.sourceforge.nite.gui.textviewer.NTextArea;
import net.sourceforge.nite.gui.textviewer.NTextElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nxt.NOMObjectModelElement;
import net.sourceforge.nite.nxt.ObjectModelElement;

/*
 * Default format.
 * Simply displays ID, and attributes...
 */
public class GenericElementFormatter implements ElementFormatter {
    public void showElement(NOMWriteElement nwe, NTextArea nta) {
    	if (nwe==null) { return; }
	String text = nwe.startElementString() + nwe.getText() + nwe.endElementString() + "\n";
	
	NTextElement nte = new NTextElement(text, null,
					    nwe.getStartTime(), nwe.getEndTime());
	NOMObjectModelElement nome= new NOMObjectModelElement(nwe);
	nte.setDataElement((ObjectModelElement)nome);
	nta.addElement(nte);
    }                       
}