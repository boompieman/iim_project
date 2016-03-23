/* @author Dennis Hofs
 * @version  0, revision $Revision: 1.1 $,
 * $Date: 2004/12/10 16:08:22 $
 */
// Last modification by: $Author: reidsma $
// $Log: AgentConfiguration.java,v $
// Revision 1.1  2004/12/10 16:08:22  reidsma
// DR: Upmigration of AMI code into sourceforge
//
// Revision 1.2  2004/11/02 15:25:01  hofs
// Reads configurations from NXT data
//
// Revision 1.1  2004/08/23 14:24:09  hofs
// Initial version
//

package net.sourceforge.nite.gui.util;

import java.awt.Dimension;
import java.awt.Point;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteCorpus;
import net.sourceforge.nite.search.Engine;

/**
 * <p>An agent configuration contains two-dimensional matrices that map agent
 * positions in a media signal to agent names. There is one matrix for each
 * signal. Signals are identified by a name and they represent recorded video
 * or audio for a meeting with multiple agents. With a fixed set of agents,
 * each video shows a particular configuration of agent positions.</p>
 * Used mostly in video labeler.
 * see constructor for more information 
 * 
 */
public class AgentConfiguration {
    /**
     * Maps signal names to matrices of agent names. A matrix is represented by
     * a two-dimensional array of strings (String[][]). All rows must have the
     * same number of elements, although an element value may be null.
     */
    private Hashtable configs = null;
    
    /**
     * The default configuration contains one row with an alphabetically
     * ordered list of all agents.
     */
    private String[][] defaultConfig;
    
    /**
     * <p>Constructs a new agent configuration. This constructor searches the
     * participant-config elements for the specified observation in the
     * specified corpus. A participant-config element specifies a signal name
     * and a configuration that consists of one list of visible agents and
     * one list of invisible agents. For example the configuration string
     * [p3,p1][p2,p0] means that agents p3 and p1 are visible (from left to
     * right) and agents p2 and p0 are invisible, but from the current point
     * of view p2 is at the left of p0. Either list may be empty. For instance
     * in the audio signal, no agent is visible and no order can be defined, so
     * the configuration would be [][p0,p1,p2,p3].</p>
     *
     * <p>All configurations are parsed into a matrix, so they can be displayed
     * in a grid. Each element in a matrix contains the name of an agent or a
     * null value. If the number of visible and invisible agents are equal,
     * the resulting matrix is obvious (the visible agents in the first row,
     * the invisible agents in the second row). If the numbers or not equals,
     * the agents in the shortest row will be centred and there will be null
     * values at the left and the right of the row. If one of the lists is
     * empty, the resulting matrix will contain only one row.</p>
     *
     * <p>This constructor also creates a default configuration, which is used
     * if no agent configuration is defined for some signal. The default
     * configuration contains just one row with the names of all agents in
     * alphabetical order.</p>
     *
     * <p>Any errors are printed to the standard output.</p>
     *
     * @param corpus the corpus
     * @param obsName the observation name
     */
    public AgentConfiguration(NOMWriteCorpus corpus, String obsName) {
        // first set the default configuration

        // try to get a list of the participants in this observation
        Engine engine = new Engine();
        String obsAttr = corpus.getMetaData().getObservationAttributeName();
        Vector agentNames = null;
        try {
            List participants = engine.search(corpus,"($p participant): $p@" + obsAttr + "==\"" + obsName + "\"");
            if (participants.size() > 1) {
                agentNames = new Vector();
                for (int i = 1; i < participants.size(); i++) {
                    List elemList = (List)participants.get(i);
                    NOMElement participant = (NOMElement)elemList.get(0);
                    String agentName = participant.getAttribute("agentname").getStringValue();
                    agentNames.add(agentName);
                }
            }
        } catch (Throwable ex) {
            System.err.println("ERROR: Could not search the corpus.");
            System.err.println("       " + ex.getMessage());
        }
        
        // if no participant elements were found, get all agents from the metadata
        if (agentNames == null) {
            List agents = corpus.getMetaData().getAgents();
            agentNames = new Vector();
            for (int i = 0; i < agents.size(); i++) {
                NAgent agent = (NAgent)agents.get(i);
                agentNames.add(agent.getShortName());
            }
        }
        
        // sort the agents and create the default configuration
        agentNames = sortStrings(agentNames);
        defaultConfig = new String[1][];
        defaultConfig[0] = new String[agentNames.size()];
        for (int i = 0; i < agentNames.size(); i++) {
            defaultConfig[0][i] = (String)agentNames.get(i);
        }
        
        // read and parse the participant-config elements, if any
        configs = new Hashtable();
        List configElems = null;
        try {
            configElems = engine.search(corpus,"($c participant-config): $c@" + obsAttr + "==\"" + obsName + "\"");
        } catch (Throwable ex) {
            System.err.println("ERROR: Could not search the corpus.");
            System.err.println("       " + ex.getMessage());
        }
        if ((configElems != null) && (configElems.size() > 1)) {
            for (int i = 1; i < configElems.size(); i++) {
                List elemList = (List)configElems.get(i);
                NOMElement configElem = (NOMElement)elemList.get(0);
                String signal = configElem.getAttribute("signal").getStringValue();
                String configString = configElem.getAttribute("config").getStringValue();
                try {
                    String[][] config = parseConfig(configString,agentNames);
                    configs.put(signal,config);
                } catch (ParseException ex) {
                    System.err.println("WARNING: Error parsing agent configuration \"" + configString + "\".");
                    System.err.println("         " + ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Sorts a vector with strings.
     */
    private Vector sortStrings(Vector v) {
        if (v.size() <= 1)
            return v;
        String s = (String)v.get(0);
        Vector less = new Vector();
        Vector equal = new Vector();
        equal.add(s);
        Vector greater = new Vector();
        for (int i = 1; i < v.size(); i++) {
            String cmp = (String)v.get(i);
            int cmpResult = cmp.compareTo(s);
            if (cmpResult < 0)
                less.add(cmp);
            else if (cmpResult == 0)
                equal.add(cmp);
            else
                greater.add(cmp);
        }
        Vector result = sortStrings(less);
        result.addAll(equal);
        result.addAll(sortStrings(greater));
        return result;
    }
    
    /**
     * Parses a configuration string, as specified in the config attribute of a
     * participant-config element. The string must consist of two lists of
     * agents (visible and invisible), each surrounded by [ and ]. Within the
     * brackets there should be an empty string or a comma-delimited list of
     * agent names. Every agent name must occur in the specified list of
     * existing agents.
     *
     * @param config a configuration string
     * @param agentNames the list of existing agents
     * @return a matrix of agent names and null values
     * @throws ParseException if the configuration string contains an error
     */
    private String[][] parseConfig(String config, Vector agentNames) throws ParseException {
        // parse first list (visible agents)
        if ((config.length() == 0) || (config.charAt(0) != '['))
            throw new ParseException("Expected '['.", 0);
        int end = config.indexOf(']');
        if (end == -1)
            throw new ParseException("'[' not closed with ']'.", 0);
        String visible = config.substring(1,end);
        String[] visibleAgents;
        if (visible.length() > 0) {
            visibleAgents = visible.split(",");
            // check the agent names
            for (int i = 0; i < visibleAgents.length; i++) {
                if (!agentNames.contains(visibleAgents[i]))
                    throw new ParseException("Unknown agent \"" + visibleAgents[i] + "\".", 1);
            }
        } else {
            visibleAgents = new String[0];
        }
        
        // parse second list (invisible agents)
        int start = end+1;
        if ((config.length() < start+1) || (config.charAt(start) != '['))
            throw new ParseException("Expected '['.", start);
        end = config.indexOf(']',start);
        if (end == -1)
            throw new ParseException("'[' not closed with ']'.", start);
        String invisible = config.substring(start+1,end);
        String[] invisibleAgents;
        if (invisible.length() > 0) {
            invisibleAgents = invisible.split(",");
            // check the agent names
            for (int i = 0; i < invisibleAgents.length; i++) {
                if (!agentNames.contains(invisibleAgents[i]))
                    throw new ParseException("Unknown agent \"" + invisibleAgents[i] + "\".", 1);
            }
        } else {
            invisibleAgents = new String[0];
        }
        
        // create the matrix
        String[][] result = null;
        if (visibleAgents.length == 0) {
            // matrix with one row: only invisible agents
            result = new String[1][];
            result[0] = invisibleAgents;
        } else if (invisibleAgents.length == 0) {
            // matrix with one row: only visible agents
            result = new String[1][];
            result[0] = visibleAgents;
        } else if (visibleAgents.length < invisibleAgents.length) {
            // matrix with two rows
            // the visible agents are centred in the top row
            // the bottom row is set to the invisible agents
            result = new String[2][];
            result[0] = new String[invisibleAgents.length];
            int offset = (invisibleAgents.length - visibleAgents.length)/2;
            for (int i = 0; i < visibleAgents.length; i++) {
                result[0][i+offset] = visibleAgents[i];
            }
            result[1] = invisibleAgents;
        } else {
            // matrix with two rows
            // the top row is set to the visible agents
            // the invisible agents are centred in the bottom row
            result = new String[2][];
            result[0] = visibleAgents;
            result[1] = new String[visibleAgents.length];
            int offset = (visibleAgents.length - invisibleAgents.length)/2;
            for (int i = 0; i < invisibleAgents.length; i++) {
                result[1][i+offset] = invisibleAgents[i];
            }
        }
        return result;
    }
    
    /**
     * Returns the configuration for the specified signal. If there is no such
     * configuration, this method returns the default configuration.
     */
    private String[][] getConfiguration(String signal) {
        String[][] config = null;
        if (signal != null)
            config = (String[][])configs.get(signal);
        if (config == null)
            config = defaultConfig;
        return config;
    }

    /**
     * <p>Returns the dimension of the agent matrix for the specified
     * signal.</p>
     *
     * @param signal a signal name
     * @return the matrix dimension
     */
    public Dimension getDimension(String signal)
    {
        String[][] config = getConfiguration(signal);
        return new Dimension(config[0].length,config.length);
    }

    /**
     * <p>Returns the name of the agent at the specified position in the matrix
     * for the specified signal.</p>
     *
     * @param signal a signal name
     * @param x the column position in the matrix
     * @param y the row position in the matrix
     * @return an agent name or null if there is no agent at the specified
     * position
     */
    public String getAgentAt(String signal, int x, int y)
    {
        String[][] config = getConfiguration(signal);
        Dimension dim = getDimension(signal);
        if ((x < 0) || (x >= dim.getWidth()))
            return null;
        if ((y < 0) || (y >= dim.getHeight()))
            return null;
        return config[y][x];
    }

    /**
     * <p>Returns the position of an agent in the matrix for the specified
     * signal.</p>
     *
     * @param signal a signal name
     * @param agent an agent name
     * @return the position of the agent or null if the signal or agent was not
     * found
     */
    public Point findAgent(String signal, String agent)
    {
        Dimension dim = getDimension(signal);
        if (dim == null) return null;
        boolean found = false;
        int y = 0;
        int x = 0;
        while (!found && (y < dim.getHeight()))
        {
            x = 0;
            while (!found && (x < dim.getWidth()))
            {
                found = getAgentAt(signal,x,y).equals(agent);
                if (!found) x++;
            }
            if (!found) y++;
        }
        if (found)
            return new Point(x,y);
        else
            return null;
    }
}
