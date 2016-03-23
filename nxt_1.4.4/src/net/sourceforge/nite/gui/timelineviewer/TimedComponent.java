package net.sourceforge.nite.gui.timelineviewer;

/** TimedComponent
 * This imterfaces abstracts the timing interfaces of TimeBlob
 * to allow other components to ask TimeGrid for their
 * sizes.
 *
 * @see TimeBlob
 * @see TimeGrid
 * @author Craig Nicol
 **/
interface TimedComponent {
    /** start time in milliseconds */
    public int getStart();

    /** end time in milliseconds */
    public int getEnd();

    /** length in milliseconds */
    public int getLength();

    /** Layer name */
    public String getLayerName();

	public int getRecursiveDepth();
}
