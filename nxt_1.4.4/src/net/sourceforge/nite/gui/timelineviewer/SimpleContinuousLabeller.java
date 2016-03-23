/**
 * 
 */
package net.sourceforge.nite.gui.timelineviewer;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.meta.impl.*;

/**
 * A very simple, naive implementation that doesn't have any domain-specific
 * information. For demonstration purposes only.
 * 
 * TODO: Currently fixed to only work on a specific corpus, will add
 * CSLConfig support later.
 * 
 * @author Craig Nicol
 *
 */
public class SimpleContinuousLabeller implements TimelineNOMElementCreator {

	NOMWriteCorpus corpus = null;
    String coding = "contlook";
    String agent = "a";
   
	public SimpleContinuousLabeller(NOMWriteCorpus c) {
		corpus = c;
	}
	
	/* (non-Javadoc)
	 * @see net.sourceforge.nite.gui.timelineviewer.TimelineNOMElementCreator#createNewElement(float, float, java.lang.String, java.lang.String, int)
	 */
	public NOMElement createNewElement(float start, float end, String label,
			String layer, int depth) {
		if (corpus == null) return null;
		NOMElement ne = null;
		try {
			ne = new NOMWriteAnnotation(corpus, 
						    coding, // name
						    ((NiteObservation) corpus.getLoadedObservations().get(0)).getShortName(),
						    agent //agent
						    );
			ne.setStartTime(start);
			ne.setEndTime(end);
	        if (label.equals("Agent")) {
			   ne.setStringAttribute("object", "Agent");
			} else {
		       ne.setStringAttribute("object", "__None__");
	        }
		   
			ne.addToCorpus();
		} catch (NOMException e) {
			e.printStackTrace();
		}

		return ne;
	}

}
