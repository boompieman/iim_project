/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

/**
 * A single resource in a resource file, provides access methods for its
 * attributes and dependencies.
 *
 * @author jonathan 
 */
public interface NRealResource extends NResource {
    public static final int AUTOMATIC=0;
    public static final int MANUAL=1;

    /** return the value of the 'path' attribute for this
     * resource. This can be absolute or relative, or even a URL */
    public String getPath();

    /** Set the value of the 'path' attribute for this resource
     * group. This can be absolute or relative, or even a URL -
     * relative paths are relative to the resource file location
     * and/or the resource-type path */
    public void setPath(String path);

    /** return the textual description of this resource */
    public String getDescription();

    /** set the textual description of this resource */
    public void setDescription(String description);

    /** return the annotator for this resource - null if this is an
     * automatic process. Each separate annotator should have a
     * separate resource  */
    public String getAnnotator();

    /** set the annotator for this resource - should remain null if
     * this is an automatic process. Each separate annotator should
     * have a separate resource  */
    public void setAnnotator(String annotator);

    /** return the type of this resource: AUTOMATIC or MANUAL */
    public int getType();

    /** set the type of this resource: AUTOMATIC or MANUAL */
    public void setType(int type);

    /** return the responsible person or organisation as a String */
    public String getResponsible();

    /** set the responsible person or organisation as a String */
    public void setResponsible(String responsible);

    /** return the coding manual reference or URL as a String */
    public String getCodingManualReference();

    /** set the coding manual reference or URL as a String */
    public void setCodingManualReference(String manual);
    
    /** return the responsible person or organisation as a String */
    public String getQuality();

    /** set the responsible person or organisation as a String */
    public void setQuality(String quality);

    /** return the responsible person or organisation as a String */
    public String getCoverage();

    /** set the responsible person or organisation as a String */
    public void setCoverage(String coverage);

    /** return some details about the last edit to this resource as a String */
    public String getLastEdit();

    /** return some details about the last edit to this resource as a String */
    public void setLastEdit(String edit);

}
