package net.sourceforge.nite.tools.videolabeler;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NCoding;
import net.sourceforge.nite.meta.NLayer;

/**
 * <p>A view frame can display all annotations of a certain layer. If the layer
 * belongs to an agent coding, the frame will have tab sheets for all available
 * agents. Each tab sheet displays an annotation area (see
 * {@link AnnotationArea AnnotationArea}). If the layer is an interaction
 * coding, the frame will have just one annotation area. The annotation areas
 * are registered with the singleton document (see
 * {@link Document Document}) so they are updated whenever the document is
 * changed.</p>
 *
 * <p>The singleton view frame factory (see {@link ViewFrameFactory
 * ViewFrameFactory}) takes care of creating, removing and laying out view
 * frames on a desktop pane.</p>
 */
public class ViewFrame extends JInternalFrame {
    private AnnotationLayer layer;
    private Vector annotationAreas = new Vector();
    private JTabbedPane tabbedPane;
    /* added by jonathan 21.4.05 */
    protected net.sourceforge.nite.search.GUI search=null;

    /**
     * <p>Constructs a new view frame for the specified layer. The annotation
     * areas are registered with the singleton document to be notified of
     * document changes. If the frame is not needed anymore, call
     * <code>setClosed(true)</code>. That will unregister the frame with the
     * document.</p>
     *
     * @param layer a layer
     */
    public ViewFrame(AnnotationLayer layer) {
        super("",true,false,true,true);
	commonConstructor(layer);
    }

    /**
     * <p>Constructs a new view frame for the specified layer. The annotation
     * areas are registered with the singleton document to be notified of
     * document changes. If the frame is not needed anymore, call
     * <code>setClosed(true)</code>. That will unregister the frame with the
     * document.</p>
     *
     * @param layer a layer
     * @parem search - a search GUI with which we can intract
     */
    protected ViewFrame(AnnotationLayer layer, net.sourceforge.nite.search.GUI search) {
        super("",true,false,true,true);
	this.search=search;
	commonConstructor(layer);
    }

    /* jonathan moved this code so he could add a new constructor with the search argument */
    private void commonConstructor(AnnotationLayer layer) {
        this.layer = layer;
        setSize(300,300);
        setTitle(layer.getNLayer().getName());
        createGui();
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent e) {
                Document doc = Document.getInstance();
                Iterator it = annotationAreas.iterator();
                while (it.hasNext()) {
                    AnnotationArea area = (AnnotationArea)it.next();
		    if (search!=null) {
			search.deregisterResultHandler(area);
		    }
                    area.setVisible(false);
                }
            }
        });
    }
    

    /**
     * <p>Returns the layer whose annotations are displayed in this view
     * frame.</p>
     *
     * @return the layer for this view frame
     */
    public AnnotationLayer getViewLayer() {
        return layer;
    }

    /**
     * Creates the contents of this view frame.
     */
    private void createGui() {
        Document doc = Document.getInstance();
        NCoding coding = (NCoding)layer.getNLayer().getContainer();
        if (coding.getType() == NCoding.AGENT_CODING) {
            tabbedPane = new JTabbedPane();
            List agents = doc.getAgents();
            Iterator it = agents.iterator();
            while (it.hasNext()) {
                NAgent agent = (NAgent)it.next();
                AnnotationArea area = new AnnotationArea(agent,layer,true);
		if (search!=null) {
		    search.registerResultHandler(area);
		}
                annotationAreas.add(area);
                tabbedPane.addTab(agent.getShortName(),new JScrollPane(area));
            }
            getContentPane().add(tabbedPane);
        } else {
            AnnotationArea area = new AnnotationArea(null,layer,true);
	    if (search!=null) {
		search.registerResultHandler(area);
	    }
            getContentPane().add(area);
        }
    }
}
