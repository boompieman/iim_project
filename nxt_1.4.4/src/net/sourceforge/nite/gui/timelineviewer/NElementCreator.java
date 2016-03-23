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
 * @author Craig Nicol
 *
 */
public class NElementCreator implements TimelineNOMElementCreator {

	private NOMWriteCorpus corpus = null;

	private String forcedname = null;
	private String forcedagent = null;
	private int forceddepth = -1;
	
	public NElementCreator(NOMWriteCorpus c) {
		corpus = c;
	}

	/* Override the Timeline-provided element name with the one provided for 
	 * all future NOMElements.
	 */
	public void forceName(String name) {
		forcedname = name;
	}
	
	/* Override the Timeline-provided element agent with the one provided for 
	 * all future NOMElements.
	 */
	public void forceAgent(String agent) {
		forcedagent = agent;
	}

	/* Override the Timeline-provided element depth with the one provided for 
	 * all future NOMElements.
	 *
	 * NOTE: Currently has no effect.
	 */
	public void forceDepth(int depth) {
		forceddepth = depth;
	}
	
	/* (non-Javadoc)
	 * @see net.sourceforge.nite.gui.timelineviewer.TimelineNOMElementCreator#createNewElement(float, float, java.lang.String, java.lang.String, int)
	 */
	public NOMElement createNewElement(float start, float end, String label,
			String layer, int depth) {
		if (corpus == null) return null;
		NOMElement ne = null;
		String nename = (forcedname == null ? layer : forcedname);
		String neagent = (forcedagent == null ? label : forcedagent);
		int nedepth = (forceddepth < 0 ? depth : depth);		

		try {
			ne = new NOMWriteAnnotation(corpus, 
						    nename,
						    ((NiteObservation) corpus.getLoadedObservations().get(0)).getShortName(),
						    neagent
						    );
			ne.setStartTime(start);
			ne.setEndTime(end);
			ne.addToCorpus();
		} catch (NOMException e) {
			e.printStackTrace();
		}

		return ne;
	}

}
