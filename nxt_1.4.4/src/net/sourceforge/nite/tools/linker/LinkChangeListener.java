package net.sourceforge.nite.tools.linker;

import java.util.EventListener;

/**
 * Listener interface to get notification of changes in the selection of an NTranscriptionView.
 */
public interface LinkChangeListener extends EventListener {

    /** notification of a change in the links */
    public void linkChanged();
}
