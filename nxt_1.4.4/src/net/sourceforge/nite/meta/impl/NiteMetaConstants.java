/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
// NiteMetaConstants.java
package net.sourceforge.nite.meta.impl;


/** 
 * Constants used to read and write metadata files.
 *
 * @author Jonathan Kilgour
 * @see NiteMetaData
 */
public class NiteMetaConstants {

    // Some generic attribute names
    public static final String fileName="filename";
    public static final String description="description";
    public static final String extension="extension";
    public static final String application="application";
    public static final String sound="sound";
    public static final String objectType="type";
    public static final String objectNumber="number";
    public static final String objectTitle="title";
    public static final String objectFormat="format";
    public static final String objectExtension="extension";
    public static final String objectName="name";
    public static final String objectNameRef="nameref";
    public static final String objectPath="path";
    public static final String objectRole="role";
    public static final String objectTarget="target";
    public static final String valuetype="value-type";
    public static final String value="value";
    public static final String coder="coder";
    public static final String checker="checker";
    public static final String agent="agent";
    public static final String date="date";
    public static final String status="status";
    public static final String objectDefault="default";
    public static final String elementName="element-name";
    public static final String attributeName="attribute-name";
    public static final String displayColour="display-colour";

    public static final String corpus_element = "corpus";
    public static final String corpus_path = "/" + corpus_element;

    public static final String reservedAttributesName = "reserved-attributes";
    public static final String reservedAttributes = corpus_path + "/" + reservedAttributesName;
    public static final String reservedElementsName = "reserved-elements";
    public static final String reservedElements = corpus_path + "/" + reservedElementsName;
    public static final String reservedIDName = "identifier";
    public static final String reservedID = reservedAttributes + "/" + reservedIDName + "/@" + objectName;
    public static final String reservedStartTimeName = "starttime";
    public static final String reservedStartTime = reservedAttributes + "/" + reservedStartTimeName + "/@" + objectName;
    public static final String reservedEndTimeName = "endtime";
    public static final String reservedEndTime = reservedAttributes + "/" + reservedEndTimeName + "/@" + objectName;
    public static final String reservedAgentName = "agentname";
    public static final String reservedAgent = reservedAttributes + "/" + reservedAgentName + "/@" + objectName;
    public static final String reservedResourceName = "resourcename";
    public static final String reservedResource = reservedAttributes + "/" + reservedResourceName + "/@" + objectName;
    public static final String reservedObservationName = "observationname";
    public static final String reservedObservation = reservedAttributes + "/" + reservedObservationName + "/@" + objectName;
    public static final String reservedGVMName = "gvm";
    public static final String reservedGVM = reservedAttributes + "/" + reservedGVMName + "/@" + objectName;
    public static final String reservedKeyName = "keystroke";
    public static final String reservedKey = reservedAttributes + "/" + reservedKeyName + "/@" + objectName;
    public static final String reservedCommentName = "commentname";
    public static final String reservedComment = reservedAttributes + "/" + reservedCommentName + "/@" + objectName;
    public static final String reservedChildName = "child";
    public static final String reservedChild = reservedElements + "/" + reservedChildName + "/@" + objectName;
    public static final String reservedPointerName = "pointer";
    public static final String reservedPointer = reservedElements + "/" + reservedPointerName + "/@" + objectName;
    public static final String reservedExternalPointerName = "externalpointer";
    public static final String reservedExternalPointer = reservedElements + "/" + reservedExternalPointerName + "/@" + objectName;
    public static final String reservedStreamName = "stream";
    public static final String reservedStream = reservedElements + "/" + reservedStreamName + "/@" + objectName;
    public static final String reservedText = reservedElements + "/text/@name";
    

    public static final String cvsinfoName = "cvsinfo";
    public static final String cvsinfopath = corpus_path + "/" + cvsinfoName;
    public static final String cvsprotocol = "protocol";
    public static final String cvsprotocolpath = cvsinfopath + "/@" + cvsprotocol;
    public static final String cvsserver = "server";
    public static final String cvsserverpath = cvsinfopath + "/@" + cvsserver;
    public static final String cvsmodule = "module";
    public static final String cvsmodulepath = cvsinfopath + "/@" + cvsmodule;
    public static final String cvsrepository = "repository";
    public static final String cvsrepositorypath = cvsinfopath + "/@" + cvsrepository;
    public static final String cvsconnection = "connection";
    public static final String cvsconnectionpath = cvsinfopath + "/@" + cvsconnection;
    // valid CVS connection attribute values
    public static final String rsh="rsh";
    public static final String ssh="ssh";
    // Return values for connection method
    public static final int RSH=0;
    public static final int SSH=1;
    

    public static final String signalpathxpath = "//signals/@path";
    public static final String signalmodifierxpath = "//signals/@pathmodifier";
    public static final String ontologypathxpath = "//ontologies/@path";
    public static final String objectsetpathxpath = "//object-sets/@path";
    public static final String corpusresourcepathxpath = "//corpus-resources/@path";
    public static final String interactionsignalxpath = "//signals/interaction-signals/signal";
    public static final String agentsignalxpath = "//signals/agent-signals/signal";

    public static final String pathModifier = "pathmodifier";
    public static final String codingrefxpath = "coding-ref";
    public static final String ontologyrefxpath = "ontology-ref";
    public static final String objectsetrefxpath = "object-set-ref";

    public static final String stylesheetpathxpath = "//stylesheets/@path";
    public static final String codingsxpath = "/corpus/codings";
    public static final String codingspathxpath = codingsxpath + "/@path";
    public static final String programsxpath = "/corpus/callable-programs";
    public static final String programspathxpath = programsxpath + "/@path";
    public static final String programxpath = programsxpath + "/callable-program";
    public static final String requiredargumentxpath = "required-argument";
    public static final String optionalargumentxpath = "optional-argument";
    public static final String usessignalxpath = "uses-signal/signal-ref";
    public static final String usescodingxpath = "uses-data/coding-ref";
    public static final String changescodingxpath = "changes-data/coding-ref";
    public static final String usesobjectsetxpath = "uses-data/object-set-ref";
    public static final String changesobjectsetxpath = "changes-data/object-set-ref";
    public static final String usesontologyxpath = "uses-data/ontology-ref";
    public static final String observations_element = "observations";
    public static final String observationsxpath = "//" + observations_element;
    public static final String observation_element = "observation";
    public static final String observationxpath = observationsxpath + "/" + observation_element;
    public static final String variables_element = "variables";
    public static final String variable_element = "variable";
    public static final String variablexpath = variables_element + "/" + variable_element;
    public static final String obsvarxpath = observationxpath + "/" + variablexpath;
    public static final String observationvariablexpath = "//observation-variables/observation-variable";
    public static final String codings_element = "user-codings";
    public static final String coding_element = "user-coding";
    public static final String usercodingxpath = codings_element + "/" + coding_element;
    public static final String agentxpath = "//agents/agent";
    public static final String interfacexpath = "//stylesheets/stylesheet";

    public static final String annotationspecxpath = "//annotation-specs/annotation-spec";
    public static final String annotationspecpathxpath = "//annotation-specs/@path";
    public static final String stylexpath = "//styles/style";
    public static final String stylepathxpath = "//styles/@path";

    public static final String viewxpath = "//views/view";
    public static final String styledwindowxpath = "styled-window";
    public static final String audiowindowxpath = "audio-window";
    public static final String videowindowxpath = "video-window";

    public static final String intercodingpathxpath = "//codings/interaction-codings/@path";
    public static final String intercodingxpath = "//codings/interaction-codings/coding-file";
    public static final String agentcodingpathxpath = "//codings/agent-codings/@path";
    public static final String agentcodingxpath = "//codings/agent-codings/coding-file";
    public static final String ontologyxpath = "//ontologies/ontology";
    public static final String objectsetxpath = "//object-sets/object-set-file";
    public static final String corpusresourcexpath = "//corpus-resources/corpus-resource-file";

    public static final String structlayer = "structural-layer";
    public static final String timedlayer = "time-aligned-layer";
    public static final String featlayer = "featural-layer";
    public static final String externallayer = "external-reference-layer";

    public static final String layertype = "layer-type";
    public static final String structural = "structural";
    public static final String featural = "featural";
    public static final String program = "program";
    public static final String contenttype = "content-type";
    public static final String extpointerrole = "external-pointer-role";
    public static final String inheritstime = "inherits-time";
    public static final String inheritstimefalse = "false";

    /* new syntax - now deprecated */
    public static final String drawschildren = "draws-children-from";
    public static final String recursedrawschildren = "recursive-draws-children-from";

    /* old syntax was points-to etc - now deprecated */
    public static final String pointsto = "points-to";
    public static final String pointstoontology = "points-to-ontology";
    public static final String pointstoobjectset = "points-to-object-set";
    public static final String pointstocorpusresource = "points-to-corpus-resource";
    public static final String recursepointsto = "recursive-points-to";
    public static final String recursive = "recursive";

    public static final String string = "string";
    public static final String number = "number";
    public static final String enumerated = "enumerated";
    
    public static final String layerxpath = structlayer + "|" + timedlayer + "|" +
	featlayer + "|" + externallayer;
    public static final String elementxpath = "code";
    public static final String textContent="text-content";
    public static final String attributexpath = "attribute";
    public static final String argumentxpath = "argument";
    public static final String pointerxpath = "pointer";
    public static final String valuexpath = "value";

    public static final String typexpath = corpus_path + "/@type";
    public static final String corpusTypeSimple = "simple";
    public static final String corpusTypeStandoff = "standoff";
    public static final String idxpath = corpus_path + "/@id";
    public static final String descriptionxpath = corpus_path + "/@description";
    public static final String links = "links";    
    public static final String linksyntaxxpath = corpus_path + "/@" + links;
    public static final String resources = "resource_file";    
    public static final String resourcesxpath = corpus_path + "/@" + resources;
    public static final String corpusLinksLTXML1 = "ltxml1";
    public static final String corpusLinksXPointer = "xpointer";
    

    // a few default values which are only used if the metadata file
    // fails to define them
    public static final String noNamespaceID="id";
    public static final String defaultReservedID="nite:id";
    public static final String defaultReservedStart="nite:start";
    public static final String defaultReservedEnd="nite:end";
    public static final String defaultReservedComment="comment";
    public static final String defaultReservedAgent="agent";
    public static final String defaultReservedGVM="gvm";
    public static final String defaultReservedKey="keystroke";
    public static final String defaultReservedChild="nite:child";
    public static final String defaultReservedPointer="nite:pointer";
    public static final String defaultReservedExternalPointer="nite:external_pointer";
    public static final String defaultReservedStream="nite:root";
    public static final String defaultReservedText="text";

    // a few attribute values for comparison
    public static final String audio="audio";
    public static final String video="video";

    public static final String display="display";
    public static final String editor="editor";
    public static final String otab="OTAB";
    public static final String nie="NIE";
    public static final String on="on";
    public static final String off="off";

    public static final String statusUnstarted="unstarted";
    public static final String statusDraft="draft";
    public static final String statusFinished="final";
    public static final String statusChecked="checked";


    //*************************
    // RESOURCE FILE CONSTANTS
    //*************************

    // top level resources element
    public static final String resourceRoot="resources";

    // resource-type element
    public static final String resourceType="resource-type";
    public static final String resourceTypeCodingAttr="coding";
    public static final String resourceTypePathAttr="path";
    public static final String resourceTypeDescriptionAttr="description";

    // virtual-resource element
    public static final String virtualResource="virtual-resource";
    public static final String virtualResourceIDAttr="id";
    public static final String virtualResourceDescriptionAttr="description";

    // resource element
    public static final String resource="resource";
    public static final String resourceIDAttr="id";
    public static final String resourceDescriptionAttr="description";
    public static final String resourceTypeAttr="type";
    public static final String resourceTypeManual="manual";
    public static final String resourceTypeAutomatic="automatic";
    public static final String resourceAnnotatorAttr="annotator";
    public static final String resourcePathAttr="path";
    public static final String resourceDefaultAttr="default";
    public static final String resourceResponsibleAttr="responsible";
    public static final String resourceManualAttr="manual";
    public static final String resourceQualityAttr="quality";
    public static final String resourceCoverageAttr="coverge";
    public static final String resourceLastEditAttr="lastedit";
    public static final String resourceIncompatibleAttr="notloadedwith";
    
    // dependency element
    public static final String dependency="dependency";
    public static final String dependencyObservationAttr="observation";
    public static final String dependencyIdrefAttr="idref";

    // observation element - not currently used but we may force
    // dependencies to have these as sub-elements.
    public static final String observation="observation";
    public static final String observationMatchAttr="match";
    
}
