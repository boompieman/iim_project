package net.sourceforge.nite.tools.videolabeler;

import net.sourceforge.nite.gui.textviewer.NTextArea;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;

/*
 * Will format a certain NOMWriteElement in an NTextArea.
 * Implementation is responsible for being able to handle all types of elements.
 */
public interface ElementFormatter {
    public void showElement(NOMWriteElement nwe, NTextArea nta);
}