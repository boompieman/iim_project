package net.sourceforge.nite.tools.videolabeler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.sourceforge.nite.gui.textviewer.NTextArea;
import net.sourceforge.nite.gui.textviewer.NTextElement;
import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NElement;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.nom.nomwrite.NOMAttribute;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteAnnotation;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteAttribute;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nxt.NOMObjectModelElement;
import net.sourceforge.nite.nxt.ObjectModelElement;
import org.w3c.dom.Node;

/**
 * <p>In this annotation layer the annotation targets are emotions, consisting
 * of evaluation and activation. Both evaluation and activation are double
 * values between -1.0 and +1.0. These values are specified in attributes of an
 * annotation element. An emotion, defined as a two-dimensional point, can be
 * displayed in a circle with radius 1 and origin (0,0), where the X value is
 * the evaluation and the Y value is the activation. These coordinates define
 * the label of an emotion. The layer uses a {@link FeeltraceColourMap
 * FeeltraceColourMap} to map emotions to colours. This map can be obtained with
 * {@link #getColourMap() getColourMap()}.</p>
 */
public class FeeltraceAnnotationLayer extends AnnotationLayer {
    
    private String activationattribute;
    private String evaluationattribute;
    private FeeltraceColourMap colourMap = new FeeltraceColourMap();

    /**
     * <p>Constructs a new feeltrace annotation layer. The layerInfo parameter
     * is the layerinfo element from the configuration file. It should have the
     * following two attributes:</p>
     *
     * <ul>
     * <li>activationattribute: the name of the attribute that contains the
     * activation value of an annotation</li>
     * <li>evaluationattribute: the name of the attribute that contains the
     * evaluation value of an annotation</li>
     * </ul>
     *
     * @param layer the NXT layer
     * @param codeName the name of the code elements that represent annotations
     * in this layer
     * @param panelType the qualified class name of the target control panel
     * that should be used with this layer
     * @param layerInfo the layerinfo element
     */
    public FeeltraceAnnotationLayer(NLayer layer, String codeName, String panelType, Node layerInfo)
    throws ClassNotFoundException, NoSuchMethodException, Exception {
        super(layer,codeName,panelType,layerInfo);
        
        // get layer-specific layerinfo attributes
        CSLConfig cfg = CSLConfig.getInstance();
        activationattribute = cfg.getAttributeValue(layerInfo,"activationattribute");
        evaluationattribute = cfg.getAttributeValue(layerInfo,"evaluationattribute");
        if (activationattribute == null)
            throw new Exception("activationattribute attribute not found");
        if (evaluationattribute == null)
            throw new Exception("evaluationattribute attribute not found");
        
        elementFormatter = new FeeltraceElementFormatter();
    }
    
    /**
     * <p>Returns the name of the attribute that contains the evaluation value
     * of an annotation.</p>
     *
     * @return the name of the evaluation attribute
     */
    public String getEvaluationAttribute() {
        return evaluationattribute;
    }

    /**
     * <p>Returns the name of the attribute that contains the activation value
     * of an annotation.</p>
     *
     * @return the name of the activation attribute
     */
    public String getActivationAttribute() {
        return activationattribute;
    }
    
    /**
     * <p>Returns the Feeltrace colour map that maps emotions to colours.</p>
     *
     * @return the Feeltrace colour map
     */
    public FeeltraceColourMap getColourMap() {
        return colourMap;
    }

    /**
     * <p>Returns a map that maps style names to styles.</p>
     *
     * @return a map that maps style names to styles
     */
    public HashMap getStyleMap() {
        return colourMap.getStyleMap();
    }
    
    /**
     * <p>Returns false. An annotation frame for this layer should not show
     * the annotation area, because annotations can be visualised in the target
     * control panel.</p>
     *
     * @return false
     */
    public boolean showAnnotationArea() {
        return false;
    }
    
    /**
     * <p>Returns the double value of the specified attribute in the specified
     * annotation. If the annotation does not contain that attribute or if its
     * value is not a double or cannot be converted to a double, an error will
     * be printed to standard output and this method returns Double.NaN.</p>
     *
     * @param annotation the annotation element that contains the desired
     * attribute
     * @param attrName the name of the desired attribute
     * @return the value of the specified attribute or Double.NaN.
     */
    private double getDoubleAttributeValue(NOMElement annotation, String attrName) {
        NOMAttribute attr = annotation.getAttribute(attrName);
        if (attr == null) {
            System.out.println("ERROR: Attribute \"" + attrName + "\" not found");
            return Double.NaN;
        }
        Comparable val = attr.getComparableValue();
        Double d = null;
        if (val instanceof String) {
            String s = (String)val;
            try {
                d = Double.valueOf(s);
            } catch (NumberFormatException ex) {
                System.out.println("ERROR: Invalid double value \"" + s +
                        "\" for attribute \"" + attrName + "\"");
            }
        } else if (val instanceof Double) {
            d = (Double)val;
        } else {
            System.out.println("ERROR: Invalid value for attribute \"" + attrName + "\"");
        }
        if (d == null)
            return Double.NaN;
        else
            return d.doubleValue();
    }

    /**
     * <p>Returns the activation of an annotation. This should be a value
     * between -1.0 and +1.0. If no value could be retrieved, this method
     * returns <code>Double.NaN</code>.</p>
     *
     * @param annotation an annotation
     * @return the activation of the specified annotation or
     * <code>Double.NaN</code>
     */
    public double getActivation(NOMElement annotation) {
        return getDoubleAttributeValue(annotation,activationattribute);
    }
    
    /**
     * <p>Returns the evaluation of an annotation. This should be a value
     * between -1.0 and +1.0. If no value could be retrieved, this method
     * returns <code>Double.NaN</code>.</p>
     *
     * @param annotation an annotation
     * @return the evaluation of the specified annotation or
     * <code>Double.NaN</code>
     */
    public double getEvaluation(NOMElement annotation) {
        return getDoubleAttributeValue(annotation,evaluationattribute);
    }
    
    /**
     * <p>Returns the label of the specified annotation. The label will consist
     * of the evaluation and the activation, separated by a comma. If no
     * evaluation or activation could be retrieved from the annotation, this
     * method returns null.</p>
     *
     * @param annotation an annotation
     * @return the label of the annotation or null
     */
    public String getLabel(NOMElement annotation) {
        DecimalFormat format = new DecimalFormat("0.00",new DecimalFormatSymbols(new Locale("en")));
        double eval = getEvaluation(annotation);
        double activ = getActivation(annotation);
        if (Double.isNaN(eval) || Double.isNaN(activ))
            return null;
        else
            return format.format(eval) + ", " + format.format(activ);
    }
    
    /**
     * <p>Splits an annotation, so there will be a gap between the specified
     * start time and end time. The specified annotation must start before
     * the start time and it must end after the end time.</p>
     *
     * @param annotation the annotation that will be split
     * @param startTime the time where the gap will start
     * @param endTime the time where the gap will end
     * @exception Exception if an error occurs
     */
    public void createGap(NOMElement annotation, double startTime, double endTime)
    throws Exception {
        Document doc = Document.getInstance();
        NOMWriteElement newElement = new NOMWriteAnnotation(doc.getCorpus(),
                codeElement.getName(),
                doc.getObservation().getShortName(),
                annotation.getAgent());
        double eval = getEvaluation(annotation);
        if (!Double.isNaN(eval)) {
            NOMAttribute evalAttr = new NOMWriteAttribute(evaluationattribute,
                    new Double(eval));
            newElement.addAttribute(evalAttr);
        }
        double activ = getActivation(annotation);
        if (!Double.isNaN(activ)) {
            NOMAttribute activAttr = new NOMWriteAttribute(activationattribute,
                    new Double(activ));
            newElement.addAttribute(activAttr);
        }
        newElement.setStartTime(endTime);
        newElement.setEndTime(annotation.getEndTime());
        newElement.addToCorpus();
        annotation.setEndTime(startTime);
    }
    
    /**
     * <p>Compares two annotations and returns true if they have the same
     * evaluation and activation. If the two annotations are adjacent, it means
     * they can be merged into one annotation.</p>
     *
     * @param elem1 the first annotation
     * @param elem2 the second annotation
     * @return true if both annotations have the same evaluation and activation,
     * false otherwise
     */
    public boolean sameTargets(NOMElement elem1, NOMElement elem2) {
        double eval1 = getEvaluation(elem1);
        double eval2 = getEvaluation(elem2);
        if (Double.isNaN(eval1) != Double.isNaN(eval2))
            return false;
        if (!Double.isNaN(eval1) && (eval1 != eval2))
            return false;
        double activ1 = getActivation(elem1);
        double activ2 = getActivation(elem2);
        if (Double.isNaN(activ1) != Double.isNaN(activ2))
            return false;
        if (!Double.isNaN(activ1) && (activ1 != activ2))
            return false;
        return true;
    }

    /**
     * Element formatter for the FeeltraceAnnotationLayer.
     */
    private class FeeltraceElementFormatter extends GenericElementFormatter  {
        public void showElement(NOMWriteElement nwe, NTextArea nta) {
            double start = nwe.getStartTime();
            double end = nwe.getEndTime();
            double duration = end - start;

            String text  = " [ ";
            String label = getLabel(nwe);
            if (label == null)
                label = "";
            String text2 = label;
            if (text2.length() == 0)
                text2 = "EMPTY";
            text2 += " ";
            if (end > 0.0) {
                for (int i = 0; i < duration*2; i++) {
                    text2 += ".";
                }
            } else {
                text2 += "UNFINISHED";
            }
            String text3 = " ] ";
            NTextElement nte = new NTextElement(text,
                    "", nwe.getStartTime(), nwe.getEndTime());
            NOMObjectModelElement nome= new NOMObjectModelElement(nwe);
            nte.setDataElement((ObjectModelElement)nome);
            nta.addElement(nte);
            
            double eval = getEvaluation(nwe);
            double activ = getActivation(nwe);
            String styleName = colourMap.getStyleName(eval,activ);
            NTextElement nte2 = new NTextElement(text2,
                    styleName,
                    nwe.getStartTime(), nwe.getEndTime());
            nte2.setDataElement((ObjectModelElement)nome);
            nta.addElement(nte2);

            NTextElement nte3 = new NTextElement(text3, 
                    "", nwe.getStartTime(), nwe.getEndTime());
            nte3.setDataElement((ObjectModelElement)nome);
            nta.addElement(nte3);
        }                       
    }
}
