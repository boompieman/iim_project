package net.sourceforge.nite.tools.videolabeler;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>This class arranges internal frames in a designated area of a desktop
 * pane. All frames will have the same size. At construction of the frame
 * arranger, the number of frames and the area can be specified as well as the
 * space that should be between the frames, and the minimum and maximum
 * dimensions of a frame.</p>
 *
 * <p>The frames are layed out in a grid. The frame arranger determines the
 * optimal grid dimensions and the size of each frame. This is done in three
 * steps.</p>
 *
 * <p>First it is determined what grid dimensions are possible with the
 * specified number of frames. Iterating over the number of frames
 * (<i>i</i> = 1..<i>n</i>), the frames can be layed out in a grid of <i>i</i>
 * rows and Math.ceil(<i>n</i>/<i>i</i>) columns. Some of the arrangements
 * can result into one or more empty rows. These arrangements are not
 * considered in further steps. In some arrangements the frames cannot be
 * large enough for the specified minimum dimension. These arrangements are
 * not further considered either.</p>
 *
 * <p>Now it is possible that no grid arrangement is possible. In that case all
 * frames will be layed out overlapping each other. Each frame will have the
 * minimum dimension and one frame will be located 10 pixels to the right
 * bottom of another frame.</p>
 *
 * <p>Given a set of possible arrangements, the second step is to determine
 * the arrangements in which the frame size can be as large as possible. With
 * the available desktop area, required space between the frames and minimum
 * and maximum frame dimensions, the frame size in an arrangement can be
 * calculated and this size can be maximised.</p>
 *
 * <p>In the third step it is attempted to find an arrangement in which the
 * width/height proportion of the frames is optimal. This frame arranger
 * considers the proportion optimal when the width equals the height (square
 * proportion). It is determined which arrangement approaches the square
 * proportion most closely. There may be two such arrangements (so that
 * width1/height1 = height2/width2). In that case the frame arranger chooses
 * the arrangement with maximum width.</p>
 *
 * <p>After constructing the frame arranger, the bounding rectangle of a frame
 * can simply be obtained with
 * {@link #getBoundsForFrame(int) getBoundsForFrame()}.</p>
 */
public class FrameArranger {
    private int nboxes;
    private Rectangle area;
    private int space;
    private int minWidth;
    private int maxWidth;
    private int minHeight;
    private int maxHeight;

    private int bestNRows = 0;
    private int bestNCols = 0;
    private double bestWidth;
    private double bestHeight;

    /**
     * <p>Constructs a new frame arranger. The arranger will lay out
     * <code>nboxes</code> frames in the specified area of a desktop pane. There
     * will be a space of <code>space</code> pixels between the frames and the
     * frames will not be smaller than
     * (<code>minWidth</code>,<code>minHeight</code>) and not be larger than
     * (<code>maxWidth</code>,<code>maxHeight</code>).</p>
     *
     * @param nboxes the number of frames to lay out
     * @param area the available area to lay out the frames
     * @param space the amount of space between the frames
     * @param minWidth the minimum width of a frame
     * @param maxWidth the maximum width of a frame
     * @param minHeight the minimum height of a frame
     * @param maxHeight the maximum height of a frame
     */
    public FrameArranger(int nboxes, Rectangle area, int space, int minWidth, int maxWidth, int minHeight, int maxHeight) {
        this.nboxes = nboxes;
        this.area = area;
        this.space = space;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        bestWidth = minWidth;
        bestHeight = minHeight;
        Vector arrangements = getPossibleArrangements();
        arrangements = getMaxSizeArrangements(arrangements);
        Dimension best = getBestArrangement(arrangements);
        if (best != null) {
            bestNRows = best.height;
            bestNCols = best.width;
            bestWidth = getBoxWidth(bestNCols);
            bestHeight = getBoxHeight(bestNRows);
        }
    }

    /**
     * Returns the possible grid arrangements for the specified number of
     * frames. This method returns a vector with Dimension objects. Each
     * dimension specifies the number of rows and columns in a possible grid
     * arrangement. The vector may be empty if no grid arrangement is possible
     * because of the specified minimum frame size.
     */
    private Vector getPossibleArrangements() {
        Vector result = new Vector();
        for (int nrows = 1; nrows <= nboxes; nrows++) {
            int ncols = (int)Math.ceil((double)nboxes/nrows);
            int emptyPlaces = nrows*ncols-nboxes;
            if (emptyPlaces >= ncols) continue;
            int requiredWidth = ncols*(minWidth+space)-space;
            if (requiredWidth > area.width) continue;
            int requiredHeight = nrows*(minHeight+space)-space;
            if (requiredHeight > area.height) continue;
            result.add(new Dimension(ncols,nrows));
        }
        return result;
    }

    /**
     * Filters a set of arrangements and returns the subset of arrangements in
     * which the frame size is maximal. This method can take the output of
     * getPossibleArrangements. It returns a vector with Dimension objects,
     * which may be empty.
     */
    private Vector getMaxSizeArrangements(Vector arrangements) {
        Iterator it = arrangements.iterator();
        Vector bestDims = new Vector();
        double maxSize = 0.0;
        while (it.hasNext()) {
            Dimension dim = (Dimension)it.next();
            double width = getBoxWidth(dim.width);
            double height = getBoxHeight(dim.height);
            double size = width*height;
            if (size == maxSize) {
                bestDims.add(dim);
            } else if (size > maxSize) {
                bestDims.clear();
                bestDims.add(dim);
                maxSize = size;
            }
        }
        return bestDims;
    }

    /**
     * Returns the best arrangement from a set of arrangements. The best
     * arrangement is the arrangement in which a frame's width/height proportion
     * approaches the optimal square proportion most closely. This may result
     * into two arrangements, in which case the one with the widest frames is
     * chosen.
     * This method can take the output of getMaxSizeArrangements. If the
     * specified set of arrangements is empty, this method returns null.
     */
    private Dimension getBestArrangement(Vector arrangements) {
        Iterator it = arrangements.iterator();
        Dimension bestDim = null;
        // bestProp and bestPropDev initialised to -1.0 (undefined value)
        double bestProp = -1.0; // best proportion: width/height
        double bestPropDev = -1.0; // deviance from the optimal square proportion
                          // bestPropDev >= 1, the optimal square proportion is 1
        while (it.hasNext()) {
            Dimension dim = (Dimension)it.next();
            double prop = (double)dim.width/(double)dim.height;
            double propDev;
            if (prop >= 1)
                propDev = prop/1.0;
            else
                propDev = 1.0/prop;
            if ((bestProp == -1.0) || // bestProp still undefined
            (propDev < bestPropDev) || // prop is closer to optimal proportion than bestProp
            ((propDev == bestPropDev) && (prop > bestProp))) { // prop is equally close to
                              // optimal proportion as bestProp, but prop has larger width
                bestProp = prop;
                bestPropDev = propDev;
                bestDim = dim;
            }
        }
        return bestDim;
    }

    /**
     * Returns the width of a frame if the frames are layed out in ncols
     * columns. This method takes into account the space between frames and the
     * minimum and maximum frame dimensions.
     */
    private double getBoxWidth(int ncols) {
        double availableWidth = area.width - (ncols-1)*space;
        double boxWidth = availableWidth/ncols;
        if (boxWidth < minWidth)
            boxWidth = minWidth;
        else if (boxWidth > maxWidth)
            boxWidth = maxWidth;
        return boxWidth;
    }

    /**
     * Returns the height of a frame if the frames are layed out in nrows
     * rows. This method takes into account the space between frames and the
     * minimum and maximum frame dimensions.
     */
    private double getBoxHeight(int nrows) {
        double availableHeight = area.height - (nrows-1)*space;
        double boxHeight = availableHeight/nrows;
        if (boxHeight < minHeight)
            boxHeight = minHeight;
        else if (boxHeight > maxHeight)
            boxHeight = maxHeight;
        return boxHeight;
    }

    /**
     * <p>Returns the bounding rectangle (specifying size and location) of the
     * frame with the specified index.</p>
     *
     * @param index the index of a frame
     * @return the bounding rectangle of the specified frame
     */
    public Rectangle getBoundsForFrame(int index) {
        if ((bestNCols > 0) && (bestNRows > 0)) {
            // frames layed out in a grid
            double x = index % bestNCols;
            double y = index/bestNCols;
            int left = area.x + (int)(x*(bestWidth+(double)space));
            int top = area.y + (int)(y*(bestHeight+(double)space));
            return new Rectangle(left,top,(int)bestWidth,(int)bestHeight);
        } else {
            // frames cannot be layed out in a grid
            int left = area.x + index*10;
            if (left + bestWidth > area.x + area.width)
                left = area.x + area.width - (int)bestWidth;
            if (left < area.x)
                left = area.x;
            int top = area.y + index*10;
            if (top + bestHeight > area.y + area.height)
                top = area.y + area.height - (int)bestHeight;
            if (top < area.y)
                top = area.y;
            return new Rectangle(left,top,(int)bestWidth,(int)bestHeight);
        }
    }
}
