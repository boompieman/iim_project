package net.sourceforge.nite.tools.videolabeler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import net.sourceforge.nite.meta.NLayer;
import org.w3c.dom.Node;

/**
 * <p>An annotation layer factory is a singleton object that can create
 * annotation layers, which are wrappers around NXT layers (see
 * {@link AnnotationLayer AnnotationLayer}). The factory is obtained with
 * the static method getInstance(). An annotation layer is created with
 * createAnnotationLayer().</p>
 */
public class AnnotationLayerFactory {
    private static AnnotationLayerFactory instance = null;
    
    /**
     * <p>Use getInstance() to get the singleton annotation factory.</p>
     */
    private AnnotationLayerFactory() {
    }
    
    /**
     * <p>Returns the singleton annotation layer factory. The first time this
     * method is called, the factory is created.</p>
     *
     * @return the singleton annotation layer factory
     */
    public static AnnotationLayerFactory getInstance() {
        if (instance == null)
            instance = new AnnotationLayerFactory();
        return instance;
    }
    
    /**
     * <p>Creates a new annotation layer. It is wrapped around the specified
     * NXT layer. This method takes three further arguments: the type of the
     * annotation layer (layerType), the type of the target control panel
     * (panelType) and the layer info element from the configuration file
     * (layerInfo), which may contain further layer-dependent settings. The
     * arguments layerType and panelType should be specified as qualified class
     * names. This method uses Java Reflection to create an instance of the
     * annotation layer.</p>
     *
     * <p>An annotation layer should be a subclass of
     * {@link AnnotationLayer AnnotationLayer} and have a constructor of the
     * same type as the constructor of AnnotationLayer. The target control
     * panel should be a subclass of
     * {@link TargetControlPanel TargetControlPanel} and have a constructor
     * of the same type as the constructor of TargetControlPanel.</p>
     *
     * <p>If an exception occurs, an error message is printed to standard
     * output and this method returns null.</p>
     *
     * <p>NOTE: not every combination of annotation layer type and target
     * control panel type may work together.</p>
     */
    public AnnotationLayer createAnnotationLayer(NLayer layer, String layerType,
    String codeName, String panelType, Node layerInfo) {
        try {
            Class annLayerClass = Class.forName(layerType);
            Class nxtLayerClass = Class.forName("net.sourceforge.nite.meta.NLayer");
            Class stringClass = Class.forName("java.lang.String");
            Class nodeClass = Class.forName("org.w3c.dom.Node");
            Class[] constrArgTypes = new Class[4];
            constrArgTypes[0] = nxtLayerClass;
            constrArgTypes[1] = stringClass;
            constrArgTypes[2] = stringClass;
            constrArgTypes[3] = nodeClass;
            Constructor layerConstr = annLayerClass.getConstructor(constrArgTypes);
            Object[] constrArgs = new Object[4];
            constrArgs[0] = layer;
            constrArgs[1] = codeName;
            constrArgs[2] = panelType;
            constrArgs[3] = layerInfo;
            return (AnnotationLayer)layerConstr.newInstance(constrArgs);
        } catch (ClassNotFoundException ex) {
            System.out.println("ERROR: Annotation layer class not found: " + ex.getMessage());
        } catch (NoSuchMethodException ex) {
            System.out.println("ERROR: Annotation layer constructor not found: " + ex.getMessage());
        } catch (InstantiationException ex) {
            System.out.println("ERROR: Annotation layer constructor could not be invoked: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println("ERROR: Annotation layer constructor does not have public access: " + ex.getMessage());
        } catch (InvocationTargetException ex) {
            Throwable ex2 = ex.getTargetException();
            if (ex2 instanceof ClassNotFoundException)
                System.out.println("ERROR: Target control panel class not found: " + ex2.getMessage());
            else if (ex2 instanceof NoSuchMethodException)
                System.out.println("ERROR: Target control panel constructor not found: " + ex2.getMessage());
            else
                System.out.println("ERROR: Annotation layer constructor threw an exception: " + ex2.getMessage());
        }
        return null;
    }
}
