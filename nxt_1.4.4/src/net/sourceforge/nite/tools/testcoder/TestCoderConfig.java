package net.sourceforge.nite.tools.testcoder;
import net.sourceforge.nite.util.*;
import java.util.Set;
import java.util.HashSet;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.util.*;
import org.w3c.dom.*;

/**
 */
public class TestCoderConfig extends AbstractCallableToolConfig {

    /**
     * Override for your application! Determines which annotation elements should be displayed.
     * Set of strings... maybe a config setting, later
     */
    public Set getDisplayedAnnotationNames() {
        Set result = new HashSet();
        result.add("dact");
        return result;
    }

  
    public String getHelpSetName() {
        return "testcoder.hs";
    }
}