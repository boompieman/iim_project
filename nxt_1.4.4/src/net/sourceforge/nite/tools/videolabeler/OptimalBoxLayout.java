package net.sourceforge.nite.tools.videolabeler;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * <p>An optimal box layout can lay out equal-size components in a grid so that
 * the preferred component proportion is best preserved. This layout is an
 * extension of the grid layout, but the user does not specify the number of
 * rows and columns, as the layout determines the optimal number of rows and
 * columns automatically.</p>
 *
 * <p>At construction the preferred component proportion is specified as a
 * width/height value. The layout gets the number of components from the
 * container that uses the layout. With that information a certain number of
 * possible grid configurations is made. The possible grid configurations are
 * <i>n</i> rows x Math.ceil(<i>nboxes</i>/<i>n</i>) columns, with <i>n</i>
 * varying from 1 to <i>nboxes</i> (the number of components). There may be
 * empty grid cells, because <i>n</i>*Math.ceil(<i>nboxes</i>/<i>n</i>) can be
 * greater than <i>nboxes</i>. To make optimal use of the available space, the
 * number of empty grid cells is minimised, so only those configurations with
 * a minimal number of empty cells are considered.</p>
 *
 * <p>For each of the remaining configurations it is calculated how large each
 * component can be if the preferred proportion is required to be preserved and
 * as much of the available container space is used. This results into a
 * component scale factor, which is maximised. There will be 1 or 2
 * configurations with a maximal scale factor.</p>
 *
 * <p>If there are 2 configurations, it means that in one of them the
 * components are scaled so that they cover the entire container width, and in
 * the other the components are scaled so that they cover the entire container
 * height. In the end however, because of the grid layout, the entire container
 * space will be filled, so in the former configuration the components will be
 * stretched vertically (to cover the remaining container height), while in the
 * latter configuration the component will be stretched horizontally (to cover
 * the remaining container width). At construction of the layout it is
 * specified which of the two is preferred.</p>
 *
 * <p>When an optimal configuration is found, the result is an optimal number
 * of rows and columns, which is used to lay out the components in a grid using
 * the grid layout from which this layout extends.</p>
 */
public class OptimalBoxLayout extends GridLayout {

    private double boxProportion;
    private boolean preferWidthStretch;
	
    /**
     * <p>Constructs a new optimal box layout. The parameters specify the
     * preferred component proportion as a double value of a component's width
     * divided by its height.</p>
     *
     * <p>The layout may find two optimal grid configurations. In one of them
     * the components will be stretched horizontally so they will be wider than
     * the preferred proportion. In the other configuration the components will
     * be stretched vertically so they will be higher than preferred. The
     * <code>preferWidthStretch</code> parameter determines which of the two
     * configurations will be chosen.</p>
     *
     * @param boxProportion the preferred component proportion (width/height)
     * @param preferWidthStretch true if it is preferred that components are
     * stretched horizontally rather than vertically, false if it is preferred
     * that components are stretched vertically rather than horizontally
     */
    public OptimalBoxLayout(double boxProportion, boolean preferWidthStretch) {
        super(1,1); // 0 rows or columns is illegal in GridLayout
        this.boxProportion = boxProportion;
        this.preferWidthStretch = preferWidthStretch;
    }

    /**
     * <p>Calculates the optimal number of rows and columns and adjusts the grid
     * layout.</p>
     *
     * @param parent the container in which the components should be layed out
     */
    private void setDimension(Container parent) {
        int nboxes = parent.getComponentCount();
        double areaWidth = parent.getWidth();
        double areaHeight = parent.getHeight();
        if ((areaWidth == 0) || (areaHeight == 0)) {
            setRows(1);
            setColumns(1);
            return;
        }
        int minEmptyPlaces = Integer.MAX_VALUE; // the minimum number of empty grid cells
        double maxScale = 0.0; // the maximum component scale factor if the preferred proportion is preserved
        int bestNRows = 0;
        int bestNCols = 0;
        // iterate over possible configurations
        for (int nrows = 1; nrows <= nboxes; nrows++) {
            int ncols = (int)Math.ceil((double)nboxes/nrows);
            int emptyPlaces = nrows*ncols-nboxes;
            // minimise number of empty grid cells
            if (emptyPlaces <= minEmptyPlaces) {
                minEmptyPlaces = emptyPlaces;
                double xscale = areaWidth/((double)ncols*boxProportion);
                double yscale = areaHeight/(double)nrows;
                double scale = Math.min(xscale,yscale);
                // maximise component scale factor
                if ((scale > maxScale) || (preferWidthStretch && (scale == maxScale))) {
                    // preferWidthStretch -> maximise number of rows (nrows increases in iteration)
                    bestNRows = nrows;
                    bestNCols = ncols;
                    maxScale = scale;
                }
            }
        }
        setRows(bestNRows);
        setColumns(bestNCols);
    }

    ////////////////////////////////////////////////////////////////////////////
    // LayoutManager methods
    // (components are added to the container, not to the layout manager)
    public void layoutContainer(Container parent) {
        setDimension(parent);
        super.layoutContainer(parent);
    }

    public Dimension minimumLayoutSize(Container parent) {
        setDimension(parent);
        return super.minimumLayoutSize(parent);
    }

    public Dimension preferredLayoutSize(Container parent) {
        setDimension(parent);
        return super.preferredLayoutSize(parent);
    }
}
