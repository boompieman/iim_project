/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.query;

import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.query.*;

/**
 * Represents the query handler for the synchronized set of display
 * windows.
 *
 * @author jonathan 
 */
public interface QueryHandler {
    public void registerResultHandler(QueryResultHandler display);
    public void deregisterResultHandler(QueryResultHandler display);
    public void performQuery(String query);
    public void popupSearchWindow();
} 
