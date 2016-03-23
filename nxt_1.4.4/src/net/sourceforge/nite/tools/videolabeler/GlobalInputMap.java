package net.sourceforge.nite.tools.videolabeler;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

/**
 * <p>A global input map is a singleton object that consists of an
 * (@link javax.swing.ActionMap ActionMap} and an
 * {@link javax.swing.InputMap InputMap}. The action map and input map are
 * set in the desktop pane in the main frame of the application (see
 * {@link ContinuousVideoLabeling ContinuousVideoLabeling}. The input map
 * is used under the condition that the desktop pane is the ancestor of the
 * focused component. Since the desktop pane is the ancestor of all components
 * in this application (internal frames and their components), the input map
 * will be used application-wide.</p>
 */
public class GlobalInputMap {
    
    private static GlobalInputMap instance = null;
    
    private static final String autokeys = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    private ActionMap actionMap;
    private InputMap inputMap;
    
    /**
     * Use getInstance() to get the singleton global input map.
     */
    private GlobalInputMap() {
        actionMap = new ActionMap();
        inputMap = new InputMap();
    }
    
    /**
     * <p>Returns the singleton global input map.</p>
     *
     * @return the singleton global input map
     */
    public static GlobalInputMap getInstance() {
        if (instance == null)
            instance = new GlobalInputMap();
        return instance;
    }
    
    /**
     * <p>Returns the action map. The action map should NOT be edited
     * directly!</p>
     *
     * @return the action map
     */
    public ActionMap getActionMap() {
        return actionMap;
    }
    
    /**
     * <p>Returns the input map. The input map should NOT be edited
     * directly!</p>
     *
     * @return the input map
     */
    public InputMap getInputMap() {
        return inputMap;
    }
    
    /**
     * <p>Adds a keystroke to the input map. The keystroke will be mapped to
     * the specified action. In effect the action map will contain a mapping
     * from a unique action name (based on the name set in the action) to the
     * specified action. The input map will contain a mapping from the specified
     * keystroke to the unique action name.</p>
     * 
     * <p>The keystroke must be specified as documented in
     * {@link javax.swing.KeyStroke#getKeyStroke(java.lang.String) KeyStroke.getKeyStroke()},
     * but may also be null (see further on).</p>
     *
     * <p>If the keystroke could not be parsed, or if the input map already
     * contains a mapping for the same keystroke, this method prints a warning
     * to standard output. In this case, or if the specified keystroke is null,
     * this method may automatically make a unique keystroke. This depends on
     * the GUI setting <code>autokeystrokes</code> (see
     * {@link CSLConfig CSLConfig}). If the setting is true, this method will
     * try to find an unused keystroke for the keys 1 to 0 or A to Z (in that
     * order).</p>
     *
     * <p>This method returns the actual keystroke. This may be the same as
     * the specified keystroke, or a keystroke that was made automatically.
     * It may also be null. Use the returned keystroke (if not null) to remove
     * the keystroke later with
     * {@link #removeKeyStroke(java.lang.String) removeKeyStroke()}.</p>
     *
     * @param keystroke a keystroke or null
     * @param action the action
     * @return the actual keystroke or null
     */
    public String addKeyStroke(String keystroke, Action action) {
        KeyStroke key = null;
        boolean auto = (keystroke == null);
        if (!auto) {
            key = KeyStroke.getKeyStroke(keystroke);
            if (key == null) {
                System.out.println("WARNING: The keystroke \"" + keystroke + "\" could not be parsed.");
                auto = true;
            }
            if (inputMap.get(key) != null) {
                System.out.println("WARNING: The keystroke \"" + keystroke + "\" is already defined.");
                auto = true;
            }
        }
        String autokey = null;
        if (auto) {
            if (CSLConfig.getInstance().autoKeyStrokes()) {
                key = null;
                for (int i = 0; (i < autokeys.length()) && (key == null); i++) {
                    autokey = String.valueOf(autokeys.charAt(i));
                    key = KeyStroke.getKeyStroke(autokey);
                    if (inputMap.get(key) != null)
                        key = null;
                }
                if (key == null) return null;
            } else {
                return null;
            }
        }
	addKeyStroke(key, action);
        if (auto) {
            return autokey;
        } else {
            return keystroke;
        }
    }

    /**
     * <p>Adds a keystroke to the input map. The keystroke will be mapped to
     * the specified action. In effect the action map will contain a mapping
     * from a unique action name (based on the name set in the action) to the
     * specified action. The input map will contain a mapping from the specified
     * keystroke to the unique action name.</p>
     * 
     * <p>For this version of the call, the client program must pass
     * an already valid KeyStroke.
     *
     * <p> To remove the keystroke later use
     * {@link #removeKeyStroke(javax.swing.keystroke) removeKeyStroke()}.</p>
     *
     * @param keystroke a keystroke
     * @param action the action
     */
    public void addKeyStroke(KeyStroke key, Action action) {
	if (key==null) { return; }
        String actionName = (String)action.getValue(Action.NAME);
        if ((actionName == null) || (actionName.length() == 0))
            actionName = "Action";
        String name = actionName;
        int i = 0;
        while (actionMap.get(name) != null) {
            name = actionName + (i++);
        }
        actionMap.put(name,action);
        inputMap.put(key,name);
    }
    
    /**
     * <p>Removes a keystroke from the input map and the associated action from
     * the action map. This method MUST be called whenever an action is not used
     * anymore, for instance when an annotation frame is closed. But it should
     * ONLY be called if the keystroke was successfully added with
     * {@link #addKeyStroke(java.lang.String, javax.swing.Action) addKeyStroke()}.</p>
     *
     * @param keystroke the keystroke to be removed
     */
    public void removeKeyStroke(String keystroke) {
        KeyStroke key = KeyStroke.getKeyStroke(keystroke);
        if (key == null)
            return;
        Object actionName = inputMap.get(key);
        if (actionName != null) {
            inputMap.remove(key);
            actionMap.remove(actionName);
        }
    }

    /**
     * <p>Removes a keystroke from the input map and the associated action from
     * the action map. This method MUST be called whenever an action is not used
     * anymore, for instance when an annotation frame is closed. But it should
     * ONLY be called if the keystroke was successfully added with
     * {@link #addKeyStroke(javax.swing.KeyStroke, javax.swing.Action) addKeyStroke()}.</p>
     *
     * @param keystroke the javax.swing.KeyStroke to be removed
     */
    public void removeKeyStroke(KeyStroke key) {
        if (key == null)
            return;
        Object actionName = inputMap.get(key);
        if (actionName != null) {
            inputMap.remove(key);
            actionMap.remove(actionName);
        }
    }
}
