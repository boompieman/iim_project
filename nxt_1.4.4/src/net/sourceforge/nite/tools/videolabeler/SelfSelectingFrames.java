package net.sourceforge.nite.tools.videolabeler;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * <p>With this class you can make an {@linkplain javax.swing.JInternalFrame
 * internal frame} self-selecting or make all frames in a
 * {@linkplain javax.swing.JDesktopPane desktop pane} self-selecting. That means
 * that an internal frame will be selected when the mouse is moved over the
 * frame or any of its contents. This will also work for any components that are
 * added to the frame later and for any frames that are added to the desktop
 * later.</p>
 *
 * <p>The functionality for an internal frame is implemented with two listeners.
 * A mouse listener for the frame and all its components, and a container
 * listener for all containers in the frame. When the user moves the mouse over
 * the frame or a component in the frame, the mouse listener will be activated,
 * which will select the frame. If a component is added to a container in the
 * frame, the mouse listener and container listener (if possible) will be added
 * to the new component. When a component is removed from a container, the mouse
 * listener and container listener will be removed from that component.</p>
 *
 * <p>The functionality for a desktop pane is implemented with a container
 * listener. When a frame is added to the desktop, it will be made
 * self-selecting as described above. When a frame is removed from the desktop,
 * the self-selecting functionality will be removed from the frame.</p>
 */
public class SelfSelectingFrames {

    /**
     * <p>Makes all internal frames on the specified desktop self-selecting or
     * removes the self-selecting function from all frames.</p>
     *
     * @param desktop the desktop
     * @param set true if the frames should become self-selecting, false if they
     * should not be self-selecting anymore
     */
    public static void setFramesSelfSelecting(JDesktopPane desktop, boolean set) {
        ContainerListener containerListener = null;
        ContainerListener[] containerListeners = desktop.getContainerListeners();
        for (int i = 0; containerListener == null && i < containerListeners.length; i++) {
            if (containerListeners[i] instanceof FrameSelectingDesktopListener)
                containerListener = containerListeners[i];
        }
        if (set) {
            Component[] children = desktop.getComponents();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof JInternalFrame)
                    setSelfSelecting((JInternalFrame)children[i],true);
            }
            if (containerListener != null) return;
            desktop.addContainerListener(new FrameSelectingDesktopListener());
        } else {
            Component[] children = desktop.getComponents();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof JInternalFrame)
                    setSelfSelecting((JInternalFrame)children[i],false);
            }
            if (containerListener == null) return;
            desktop.removeContainerListener(containerListener);
        }
    }
    
    /**
     * <p>Makes an internal frame self-selecting or removes the self-selecting
     * function.</p>
     *
     * @param frame the frame
     * @param set true if the frame should become self-selecting, false if it
     * should not be self-selecting anymore
     */
    public static void setSelfSelecting(JInternalFrame frame, boolean set) {
        MouseListener mouseListener = null;
        ContainerListener containerListener = null;
        MouseListener[] mouseListeners = frame.getMouseListeners();
        for (int i = 0; mouseListener == null && i < mouseListeners.length; i++) {
            if (mouseListeners[i] instanceof FrameSelectingMouseListener)
                mouseListener = mouseListeners[i];
        }
        ContainerListener[] containerListeners = frame.getContentPane().getContainerListeners();
        for (int i = 0; containerListener == null && i < containerListeners.length; i++) {
            if (containerListeners[i] instanceof FrameSelectingContainerListener)
                containerListener = containerListeners[i];
        }
        if (set) {
            if (mouseListener != null) return;
            mouseListener = new FrameSelectingMouseListener(frame);
            containerListener = new FrameSelectingContainerListener(mouseListener);
            frame.addMouseListener(mouseListener);
        } else {
            if (mouseListener == null) return;
            frame.removeMouseListener(mouseListener);
        }
        setSelfSelecting(mouseListener,containerListener,frame.getContentPane(),set);
    }
    
    /**
     * <p>Adds or removes the specified mouse listener (a
     * FrameSelectingMouseListener) to the specified component. If the component
     * is a container, this method will also add or remove the specified
     * container listener (a FrameSelectingContainerListener) and recursively
     * call this method on all components in the container.</p>
     */
    private static void setSelfSelecting(MouseListener mouseListener,
            ContainerListener containerListener, Component c, boolean set) {
        if (set)
            c.addMouseListener(mouseListener);
        else
            c.removeMouseListener(mouseListener);
        if (c instanceof Container) {
            Container container = (Container)c;
            if (set)
                container.addContainerListener(containerListener);
            else
                container.removeContainerListener(containerListener);
            Component[] children = container.getComponents();
            for (int i = 0; i < children.length; i++) {
                setSelfSelecting(mouseListener,containerListener,children[i],set);
            }
        }
    }
    
    /**
     * <p>This container listener adds itself and the specified mouse listener
     * (a FrameSelectingMouseListener) to components that are added to a
     * container. It removes itself and the mouse listener from components that
     * are removed.</p>
     */
    private static class FrameSelectingContainerListener implements ContainerListener {
        private MouseListener mouseListener;
        
        public FrameSelectingContainerListener(MouseListener l) {
            this.mouseListener = l;
        }
        
        public void componentAdded(ContainerEvent e) {
            setSelfSelecting(mouseListener,this,e.getChild(),true);
        }
        
        public void componentRemoved(ContainerEvent e) {
            setSelfSelecting(mouseListener,this,e.getChild(),false);
        }
    }
    
    /**
     * <p>This mouse listener selects the specified internal frame when the
     * mouse enters a component.</p>
     */
    private static class FrameSelectingMouseListener extends MouseAdapter {
        private JInternalFrame frame;
        
        public FrameSelectingMouseListener(JInternalFrame frame) {
            this.frame = frame;
        }
        
        public void mouseEntered(MouseEvent e) {
            try {
                if (!frame.isSelected())
                    frame.setSelected(true);
            } catch (PropertyVetoException ex) {}
        }
    }
    
    private static class FrameSelectingDesktopListener implements ContainerListener {
        
        public FrameSelectingDesktopListener() {
        }
        
        public void componentAdded(ContainerEvent e) {
            Component child = e.getChild();
            if (child instanceof JInternalFrame) {
                setSelfSelecting((JInternalFrame)child,true);
            }
        }
        
        public void componentRemoved(ContainerEvent e) {
            Component child = e.getChild();
            if (child instanceof JInternalFrame) {
                setSelfSelecting((JInternalFrame)child,false);
            }
        }
    }
}
