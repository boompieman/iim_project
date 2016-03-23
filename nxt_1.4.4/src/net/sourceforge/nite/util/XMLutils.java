package net.sourceforge.nite.util;

import java.util.*;
import net.sourceforge.nite.nom.nomwrite.NOMElement;

/** 
 * Some factored out utilities for dealing with XML, special
 * characters etc.
 *
 * @author Jonathan Kilgour, UEdin
 */
public class XMLutils {

    /** escape an attribute value - put quotes round it and escape any
     * special characters */
    static public String escapeAttributeValue(String in) {
	return '"' + XMLutils.escapeText(in) + '"';
    }

    /** escape text for serializing to XML - just escape any
     * special characters */
    static public String escapeText(String in) {
	if (in==null) { return ""; }
	char[] dest = new char[in.length()*8];
	int newlen = XMLutils.escapeChars(in.toCharArray(), 0, in.length(), dest);
	return new String (dest, 0, newlen);
    }

    /** Escape special characters for writing. 
     * @param ch The character array containing the string @param start
     * The start position of the input string within the character array
     *  @param length The length of the input string within the
     *  character array @param out Character array to receive the
     *  output. In the worst case, this should be 8 times the length
     *  of the input array.  @return * The number of characters used
     *  in the output array
     */
    static public int escapeChars(char chin[], int start, int length, char[] chout) {       
        int o = 0;
        for (int i = start; i < start+length; i++) {
            if (chin[i]=='<') {
                ("&lt;").getChars(0, 4, chout, o); o+=4;
            } else if (chin[i]=='>') {
                ("&gt;").getChars(0, 4, chout, o); o+=4;
            } else if (chin[i]=='&') {
                ("&amp;").getChars(0, 5, chout, o); o+=5;
	    } else if (chin[i]=='\"') {
                ("&#34;").getChars(0, 5, chout, o); o+=5;
	    } else if (chin[i]=='\'') {
                ("&#39;").getChars(0, 5, chout, o); o+=5;
            } else if (chin[i]<127) {
                chout[o++]=chin[i];
	    } else {
                // output character reference
                chout[o++]='&';
                chout[o++]='#';
                String code = Integer.toString(chin[i]);
                int len = code.length();
                code.getChars(0, len, chout, o); o+=len;
                chout[o++]=';';
            }

        }
        return o;
    }

}
