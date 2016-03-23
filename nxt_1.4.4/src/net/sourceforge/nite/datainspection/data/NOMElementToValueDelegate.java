package net.sourceforge.nite.datainspection.data;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import net.sourceforge.nite.search.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import java.util.*;
import net.sourceforge.nite.query .*;

/**
 * This interface is used to get Values from NOMElements: Values are used in combination with a
 * DistanceMetric to determine kappa and alpha for annotations. See the package documentation for more
 * information about Values and their role in reliability calculations.
 */
public interface NOMElementToValueDelegate {
    /**
     * Return a Value as used in the calc package for building CoincidenceMatrices, for the given annotation element.
     */
    public Value getValueForNOMElement(NOMElement nme);

    /**
     * Return a Value as used in the calc package for building CoincidenceMatrices, representing an empty element or a gap.
     * This method is necessary to make sure that the Value associated with an empty element is always of the same class as the Value
     * returned by getValueForNOMElement. E.g., if getValueForNOMElement returns a SetValue, getGapValue should also return a SetValue (empty) 
     * to make sure that the two values can be compared.
     */
    public Value getGapValue();
}