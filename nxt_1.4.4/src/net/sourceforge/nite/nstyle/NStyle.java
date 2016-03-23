/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle;
import java.io.*;

// Import Serializer classes
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;



// Parser import - change this to use a different parser

// Imported JAVA API for XML Parsing 1.0 classes
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;

import org.jdom.JDOMException;
import org.jdom.input.*;

import org.jdom.output.DOMOutputter;
import org.jdom.transform.*;
import org.w3c.dom.Document;

// NITE
//import nite.gui.*;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.nom.nomwrite.NOMCorpus;
import net.sourceforge.nite.time.*;

/**
 * NStyle is the top level application which takes an XSL interface spec and 
 * some data to populate it and produces the interface.
 */
public class NStyle {
    /** Name of classpath-relative stylesheet file. */
    private static final String STYLESHEET_FILE = "add_ids_to_stylesheet.xsl";
    private Clock niteclock;
    private SAXBuilder sbuilder;
    private JDesktopPane component;
    private NMetaData metadata;

    private TransformerFactory tFactory;
    private DOMResult domResult1;
    private JDomParser jdp;
    private org.jdom.Document source_doc;
    private SerializerFactory sf;
    /** Constructor providing both a clock with which the NIE display
        will synchronize and a desktop pane into which the NIE display
        will be added as an internal frame */
    public NStyle(Clock c, JDesktopPane component) {
        niteclock = c;
        if (component == null) {
            this.component = makeDesktop();
        } else {
            this.component = component;
        }
        sbuilder = new SAXBuilder();
    }

    /** Constructor where we're given a desktop pane to which the NIE
        display should be added as an internal frame. No
        synchronization is attempted. */
    public NStyle(JDesktopPane component) {
        niteclock = new DefaultClock();
        if (component == null) {
            this.component = makeDesktop();
        } else {
            this.component = component;
        }
        sbuilder = new SAXBuilder();
    }

    /** Contstructor for the case where we want a separate desktop for
        the NIE display but we should be synchronized with an existing
        clock */
    public NStyle(Clock c) {
        niteclock = c;
        this.component = makeDesktop();
        sbuilder = new SAXBuilder();
    }

    /** Constructor with no arguments - creates its own desktop pane
        and does not need to be synchronized. */
    public NStyle() {
        niteclock = new DefaultClock();
        this.component = makeDesktop();
        sbuilder = new SAXBuilder();
    }

    /** produce our own desktop pane to be added to */
    private JDesktopPane makeDesktop() {
        JFrame jframe = new JFrame();
        JDesktopPane desktop = new JDesktopPane();
        jframe.getContentPane().add(desktop);
        jframe.setSize(700, 700);
        jframe.setVisible(true);
        return desktop;
    }

    public JDesktopPane getDesktop() {
	return component;
    }

    /** The transform method for standoff corpora. It is assumed the
        NOM is already loaded, and the XSL filename is the second
        argument */
    public void transform(NOMCorpus input, String xslfilename) {
        // I guess we need to let some NQL entity know what NOM we're
        // using like this...  nql_engine.registerNOM(input); Then we
        // make a bogus Document and pass through to the normal
        // transform.
        try {
            org.jdom.Document bogusdoc = sbuilder.build(xslfilename);
            metadata = input.getMetaData();
            transform(metadata, bogusdoc, xslfilename);
        } catch (org.jdom.JDOMException jex) {
            jex.printStackTrace();
        }
    }

    private void transformStepOne(
        org.jdom.Document input,
        String xslfilename) {
	//        System.out.println("Start transformation\n");
        tFactory = TransformerFactory.newInstance();
        //		tFactory.setAttribute(org.apache.xalan.transformer.XalanProperties.SOURCE_LOCATION, (Object) "true");

        // STEP 1. I want to transform the Stylesheet to add a rule so that
        // each object in the Nite-DO namespace passes the source ID through
        // to the result tree as a "nitesourceid" attribute.
        // Get the XSL file into a StreamSource
	// System.out.println("Transform the user's stylesheet adding nitesourceid rules\n");
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(
                STYLESHEET_FILE);
        StreamSource xslstream1 = new StreamSource(in);
	//        System.out.println("made a new stream source");
        try {
            Transformer trans1 = tFactory.newTransformer(xslstream1);

            // Get the XSL input as DOM
            //System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
            // "net.sf.saxon.om.DocumentBuilderFactoryImpl");
            DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
            dFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
            Document xmlInitDoc1 = dBuilder.parse(xslfilename);
	    //  System.out.println("parsed the xsl file " + xslfilename);
            DOMSource xmlDomSource1 = new DOMSource(xmlInitDoc1);

            //Make a DOM result and do the transform
            //		System.out.println("Do the transform\n");
            domResult1 = new DOMResult();
            trans1.transform(xmlDomSource1, domResult1);
            Document xmlDoc1 = (Document) domResult1.getNode();

	    /*

            sf = SerializerFactory.getSerializerFactory("xml");
            Serializer s = sf.makeSerializer(System.out, new OutputFormat());
            s.asDOMSerializer().serialize((Document) domResult1.getNode());

            */

        } catch (TransformerConfigurationException tce) {
            //		tce.printStackTrace();
            System.out.println(
                "ERROR: transformer configuration error in NStyle - failed to find XSL file "
                    + STYLESHEET_FILE);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            System.out.println("A parser configuration error in NStyle");
        } catch (org.xml.sax.SAXException se) {
            se.printStackTrace();
            System.out.println("A SAX error in NStyle");
        } catch (TransformerException te) {
            System.out.println("A transformer error in NStyle");
            te.printStackTrace();
        } catch (IOException ioe) {
            System.out.println("An io problem");
            ioe.printStackTrace();
        }
    }

    public void redisplay(org.jdom.Document d){
	transformStepTwo(d);
    }
	
    private void transformStepTwo(org.jdom.Document input) {
        try {
            //              STEP 2.
            // We have transformed the Stylesheet into a DOMResult (domResult1)
            // Now use it to transform the XML.
            // Get the XSL file into a StreamSource
	    //	    System.out.println("Before TRANSFORM");
            
	    /** SerializerFactory sf2 = SerializerFactory.getSerializerFactory("xml");
		Serializer s2;
		try {
		DOMOutputter d = new DOMOutputter();
		Document w3doc = d.output(input);
		s2 = sf.makeSerializer(System.out, new OutputFormat());
		s2.asDOMSerializer().serialize(w3doc);
		} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
		
                
		} catch (IOException ioe) {
		ioe.printStackTrace();
		} catch (JDOMException je) {
		je.printStackTrace();
		}**/

            Transformer trans = tFactory.newTransformer(new DOMSource(domResult1.getNode()));

            // Make a DOM result and do the transform
            //		System.out.println("Do the transform\n");
            DOMResult domResult = new DOMResult();
            try {
                trans.transform(new JDOMSource(input), domResult);
            } catch (TransformerException e) {
		System.err.println("A transformer exception from transformStepTwo in NStyle");
		e.printStackTrace();
            }
            Document xmlDoc = (Document) domResult.getNode();

            ///////

	    //	    System.out.println("XXXXXXXXXXXXXXXXXXX");
            /** SerializerFactory sf3 = SerializerFactory.getSerializerFactory("xml");
		Serializer s3;
		try {
                s3 = sf.makeSerializer(System.out, new OutputFormat());
                s3.asDOMSerializer().serialize(xmlDoc);
		} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
		} catch (IOException ioe) {
		ioe.printStackTrace();
            }**/
	    
            //////

            // Now we have a source document (xmlDomSource) and a result document
            // (xmlDoc) in memory. 
            // Now create a JDOM for the source and result trees. 
	    // System.out.println("Done; Create JDOM input and output trees\n");
            DOMBuilder builder = new DOMBuilder();
            org.jdom.Document result_jdoc = builder.build(xmlDoc);
            // org.jdom.Document source_jdoc = builder.build(xmlInitDoc);
	    
            // We now traverse the result document and build a
            // Swing representation.
	    // System.out.println("Traverse the JDOM output and create the SWING\n");
            jdp = null;
            
            jdp = new JDomParser(this, source_doc, result_jdoc, component, niteclock);
            jdp.setMetadata(getMetadata());
	    //  System.out.println("passed nhandler constructor");
            jdp.parse();

        } catch (TransformerConfigurationException tce) {
            //		tce.printStackTrace();
            System.out.println(
	    "ERROR: transformer configuration error in NStyle - failed to find XSL file "
                    + STYLESHEET_FILE);
        }
    }

    /** The transform method for simple corpora. This expects a JDOM
        Document and an xsl filename as input. It creates a display */
    public void transform(org.jdom.Document input, String xslfilename) {
        if ((xslfilename != null) && (xslfilename.length() > 0)) {
            //do the first stage of the transformation
            transformStepOne(input, xslfilename);
            source_doc = input;
	    //   System.out.println("Done; Now transform XML\n");
            transformStepTwo(input);
        }
    }

    /** The transform method for simple corpora. This expects a JDOM
        Document and an xsl filename as input. It creates a display */
    public void transform(NMetaData meta, org.jdom.Document input, String xslfilename) {
        metadata = meta;
        transform(input, xslfilename);
    }

    /** Main method to run from the command line.  This will only work
        for simple corpora and expects to be called using <em>java
        NStyle xmlfile xslfile </em>.  */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println(
	       "java NStyle XMLFILE STYLESHEET\n"
	       + "Reads filename.xml, applies stylesheet, creates Swing object.");
            return;
        }
        NStyle app = new NStyle();
        org.jdom.Document xmldoc = app.sbuilder.build(args[0]);
        app.transform(xmldoc, args[1]);
    }

    /**
     * Returns the metadata.
     * @return NMetaData
     */
    public NMetaData getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata.
     * @param metadata The metadata to set
     */
    public void setMetadata(NMetaData metadata) {
        this.metadata = metadata;
    }

}
