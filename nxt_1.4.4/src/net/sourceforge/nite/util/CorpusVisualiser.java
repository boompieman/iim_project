package net.sourceforge.nite.util;

/** 
 * interface to be implemented by visualisations of a corpus
 *
 * @author Jonathan Kilgour, UEdin
 */
public interface CorpusVisualiser {
    /** perform visualisation and make ready for viewing */
    public void visualiseCorpus();

    /** return a handle to the top-level visualisation object: an HTML
     * file / text file / graphical object depending on the mode of
     * the visualisation. */
    public Object getVisualisationObject();
}
