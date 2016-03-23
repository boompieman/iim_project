/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.query;

import java.util.List;

/**
 * A handler for query results.
 * 
 * @author Elaine Farrow
 */
public interface SimpleQueryResultHandler {
    /**
	 * Accept the given list of query results, which can contain a mixture of
	 * NOMElements and further lists.
	 * @param results the results.
	 */
    void acceptResults(List results);
}
