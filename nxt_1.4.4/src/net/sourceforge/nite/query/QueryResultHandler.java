/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.query;

// import net.sourceforge.nite.query.QueryHandler;
import java.util.List;
import java.awt.Color;
import net.sourceforge.nite.nom.nomwrite.NOMElement;

/**
 * Represents a display element that can handle the results of a query.
 * This will normally be a separate window in which results are displayed
 * and from which some interaction (e.g. highlighting text areas) with the
 * display element is possible.
 * 
 * @author jonathan 
 */
public interface QueryResultHandler {
    /** accept a query result as a list of NOMElements */
    public void acceptQueryResults(List results);
    /** accept a query result as an individual element */
    public void acceptQueryResult(NOMElement result);
    /** set the colour of the highlighting for queries */
    public void setQueryHighlightColor(Color color);
    //    public void sendQuery(String query);
    //    public QueryHandler getQueryHandler();
}




