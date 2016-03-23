///**
// * Natural Interactivity Tools Engineering
// * Copyright (c) 2004, 
// * See the README file in this distribution for licence.
// */
//package net.sourceforge.nite.gui.util;
//
////NXT imports
//import net.sourceforge.nite.util.*;
//import net.sourceforge.nite.nxt.*;
//import net.sourceforge.nite.meta.impl.*;
//import net.sourceforge.nite.meta.*;
//import net.sourceforge.nite.nom.*;
//import net.sourceforge.nite.nom.nomwrite.impl.*;
//import net.sourceforge.nite.time.*;
//
////general java imports
//import java.util.*;
//import java.io.*;
//import javax.swing.*;
//
////CVS
//import com.ice.cvsc.*;
//import com.ice.jcvsii.*;
//
///**
// * Initial implementation of the CVS access functionality integrated in the AbstractCallableTool class.
// * TODO
// * = make this a class which maintains the CVS (storing pwd etc) access throughout a session in a tool
// * = clean up code (a lot!)
// * = make (even if usecvs is true) this stuff optional: if user presses cancel on login, warning is issued that CVS 
// * access will not be done, that cvs commmit can still be done on a later session, and then cvs options are not used.
// * = some kind of check... we don't want the cvs update to override crucial codings, breaking the current work of the
// * annotator
// * = if we can base this cvs check on a kind of tag, to ensure that the update will only checkout everything with a 
// * certain tag ('partial views of the whole corpus, such as only emotion related layers')... (add should add same 
// * tag to files!)
// * this would save a lot of unneccesary downloading! The tag should be very specific and recognizable, as well as 
// * customizable! Example: corpussetting tag='ami-emotion-trial', with e.g. the DACT layers NOT having that tag.
// * = find (and work from) the PROPER CVS root (not current dir, but as high as possible). E.g. dagmar corpus: meta is in 
// * subdir of root, so codings are not below metadata!
// * = document the whole thing (this file, it's use in abstractcallabletool, its settings in the settings sections of the 
// * different documents, the corpus developer documentation...)
// * @author Dennis Reidsma, UTwente
// */
//public class CVSUtil {
//
//
//    public static void readCVS(String corpusname) {
//        System.out.println("vvvvvv\n>    CVS UPDATE");
//        try{
//            //where is metadatafile?
//            String path = new File(corpusname).getParent();
//            System.out.println(">   Metadata resides in path " + path);
//            //is this dir a CVS dir?
//            String adminPath = CVSProject.rootPathToAdminPath(path);
//            if (CVSProject.verifyAdminDirectory(adminPath)) {
//                System.out.println(">   CVS admindir found: " + adminPath);
//                CVSProject proj = new CVSProject();
//                proj.setProjectDef(CVSProjectDef.readDef(adminPath));
//                CVSClient client = new CVSClient(proj.getProjectDef().getHostName(), proj.getConnectionPort());
//                proj.setClient(client);
//                File rootFile = new File(new File(corpusname).getParent());
//                proj.openProject(rootFile);
//                
//                CVSRequest req = new CVSRequest();
//                //req.setCommand("update");
//                req.parseControlString("update:A:AEOIT:defut:");
//                req.setEntries(proj.readEntriesFile(proj.getRootEntry(),rootFile));
//                req.setArguments(new CVSArgumentVector());
//                req.setUserInterface(new CVSNullUI());
//                //CVSEntryVector entries = req.getEntries();
//                //how to get right password...
//                String userName = null;
//                String pwd = null;
//                while ((userName == null) || (pwd == null)) {
//                    userName = "d";//JOptionPane.showInputDialog(null, "username?");
//                    pwd = "r";//JOptionPane.showInputDialog(null, "password?");
//                }
//                proj.setUserName(userName);
//                proj.setPassword(CVSScramble.scramblePassword(pwd, 'A'));
//                CVSResponse resp = new CVSResponse();
//                boolean success = proj.performCVSRequest(req, resp);
//                if (!success) {
//                    System.out.println(">   THERE WAS AN ERROR DURING CVS UPDATE");
//                }
//                System.out.println(">   CVS update feedback:");
//                System.out.println(">   " + resp.getResultText().replace("\n","\n>   "));
//            } else {
//                System.out.println(">   This corpus is not a CVS repository");
//            }
//            System.out.println(">   END CVS UPDATE\n^^^^^^");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void writeCVS(NOMWriteCorpus nom, List codingsToCommit, String annotatorname, NObservation obs) {
//        System.out.println("vvvvvv\n>    CVS ADD/COMMIT");
//        try{
//            //where is metadatafile?
//            String path = new File(nom.getMetaData().getFilename()).getParent();
//            System.out.println(">   Metadata resides in path " + path);
//            //is this dir a CVS dir?
//            String adminPath = CVSProject.rootPathToAdminPath(path);
//            if (CVSProject.verifyAdminDirectory(adminPath)) {
//                System.out.println(">   CVS admindir found: " + adminPath);
//                CVSProject proj = new CVSProject();
//                proj.setProjectDef(CVSProjectDef.readDef(adminPath));
//                CVSClient client = new CVSClient(proj.getProjectDef().getHostName(), proj.getConnectionPort());
//                proj.setClient(client);
//                File rootFile = new File(new File(nom.getMetaData().getFilename()).getParent());
//                proj.openProject(rootFile);
//                
//                String userName = null;
//                String pwd = null;
//                while ((userName == null) || (pwd == null)) {
//                    userName = "dennisr";//JOptionPane.showInputDialog(null, "username?");
//                    pwd = "parle$vink";//JOptionPane.showInputDialog(null, "password?");
//                }
//                proj.setUserName(userName);
//                proj.setPassword(CVSScramble.scramblePassword(pwd, 'A'));
//                        
//                //find the annotatorspecificcodings
//                List codings = new ArrayList();
//                List agents = new ArrayList();
//                List allAgs = nom.getMetaData().getAgents();
//                for (int i  = 0; i < codingsToCommit.size(); i++ ) {
//                    NCoding c = (NCoding)nom.getMetaData().getCodingByName((String)codingsToCommit.get(i));
//                    if (c.getType()==NCoding.INTERACTION_CODING) {
//                        codings.add(c);
//                        agents.add(null);
//                    } else {
//                        for (int j = 0; j < allAgs.size(); j++) {
//                            codings.add(c);
//                            agents.add(allAgs.get(j));
//                        }
//                    }
//                }
//                for (int i = 0; i < codings.size(); i++) {
//                    NCoding nextcod = (NCoding)codings.get(i);
//                    NAgent nextag = (NAgent)agents.get(i);
//                    String fullCodingFileName = nom.getCodingFilename(obs, nextcod, nextag).replace("\\","/");
//                    if (!new File(fullCodingFileName).exists()) {
//                        continue;
//                    }
//                    String codingfilename = new File(fullCodingFileName).getName();
//                    String codingpath = new File(fullCodingFileName).getParent();
//                    //for each coding, get it's filename
//                    //we need to have the appropriate filename!!!
//                    //if ((annotatorname!=null) && (!annotatorname.equals(""))) {
//                        //codingpath += "/" + annotatorname; //@@@@@@@@@@@@@@@@WRONG this should be done different!
//                    //}
//                    String rootCan = rootFile.getCanonicalPath();
//                    String codingCan = new File(codingpath).getCanonicalPath();
//                    String codingdirname = new File(codingpath).getName();
//                    String reldir = codingCan.substring(rootCan.length());
//                    //force existence of directory on cvs
//                    if (!CVSProject.verifyAdminDirectory(CVSProject.rootPathToAdminPath(codingCan))) {
//                        System.out.println(">   Need to add directory, first check-in of files");
//                        CVSResponse resp = new CVSResponse();
//                        resp =
//                		proj.ensureRepositoryPath
//                			( new CVSNullUI(),
//                				"."+reldir.replace("\\","/")+"/", resp );
//                	    System.out.println(">   CVS feedback:");
//                        System.out.println(">   " + resp.getResultText().replace("\n","\n>   "));
//                        proj.writeAdminFiles();
//                    }
//
//                    //force this file to be cvs (if not, add)
//                    CVSEntryVector entries = new CVSEntryVector();
//                    CVSEntry fileentry = new CVSEntry();
//                    fileentry.setLocalDirectory("."+reldir.replace("\\","/"));
//                    fileentry.setName(codingfilename.replace("\\","/"));
//                    fileentry.setRepository((proj.getRootDirectory() + "/" + proj.getRepository()+reldir+"/").replace("\\","/"));
//                    fileentry.setDirty(true);
//                    fileentry.setVersion( "0" );
//                    fileentry.setTimestamp(new File(fullCodingFileName) );
//
//                    entries.appendEntry(fileentry);
//                   /* CVSEntry direntry = new CVSEntry();
//                    direntry.setLocalDirectory(reldir);
//                    direntry.setName(codingdirname);
//                    direntry.setRepository(proj.getRootDirectory() + "/" + proj.getRepository()+reldir.replace("\\","/")+"/");
//                    direntry.setDirty(true);
//                    direntry.setDirectoryEntryList(vector);
//                    entries.add(direntry);*/
//                    System.out.println("fileentr:"+fileentry);
//                    System.out.println("fileentrlocaldir:"+fileentry.getLocalDirectory());
//                    System.out.println("fileentrfull:"+fileentry.getFullName());
//                    System.out.println(codingpath+"/"+codingfilename);
//                    if (proj.locateEntry("."+reldir.replace("\\","/")+"/"+codingfilename)==null ) {
//                        System.out.println(">   Need to add file, first check-in");
//                        proj.addNewEntry(fileentry);
//                        proj.writeAdminFiles();
//                    }
//                    
//                    
//                    //System.out.println(">   Commit...");
//                    //request.parseControlString("add:G:SUAFX:due:");//A:EAFXOIT:duet:");
//
//                    //proj.performCVSRequest(request, resp);
//                    //System.out.println(">   CVS feedback:");
//                    //System.out.println(">   " + resp.getResultText().replace("\n","\n>   "));
//                    //proj.writeAdminFiles();                        
//                    CVSRequest request = new CVSRequest();
//                    request.setEntries(entries);
//                    //request.parseControlString("co:A:EMFU:deut:");//A:EAFXOIT:duet:");
//                    request.setArguments(new CVSArgumentVector());
//                    request.setUserInterface(new CVSNullUI());  
//                    CVSResponse resp = new CVSResponse();
//                    request.parseControlString("ci:g:EAUFGOIT:deou:-m 'a'"); 
//                            //dit commando is HEEL belangrijk!!!!!!! verkeerde vorm=onbegrijpeleijke errors!
//
//                    proj.performCVSRequest(request, resp);
//                    System.out.println(">   CVS feedback:");
//                    System.out.println(">   " + resp.getResultText().replace("\n","\n>   "));
//                    proj.writeAdminFiles();                        
//                    //ci this file                        
//                }
//
///*                CVSRequest req = new CVSRequest();
//                req.setCommand("ci");
//                req.parseControlString("ci:A:AEOIT:defut:");
//                //req.setEntries(proj.readEntriesFile(proj.getRootEntry(),rootFile));
//                //req.setEntries(hele lijst van entries die annotator specifiek zijn; of alles als geen annotator? zowiezo alleen de files die annotatorspecifiek zijn?
//                
//                //get appropriate entries
//                //check their existence as cvs file
//                //for each non-cvs entry: add
//                //for all entries: commit 
//                req.setArguments(new CVSArgumentVector());
//                req.setUserInterface(new CVSNullUI());
//                CVSEntryVector entries = req.getEntries();
//                proj.setUserName(userName);
//                proj.setPassword(CVSScramble.scramblePassword(pwd, 'A'));
//                CVSResponse resp = new CVSResponse();
//                boolean success = proj.performCVSRequest(req, resp);
//                if (!success) {
//                    System.out.println(">   THERE WAS AN ERROR DURING CVS COMMIT");
//                }
//                System.out.println(">   CVS commit feedback:");
//                System.out.println(">   " + resp.getResultText().replace("\n","\n>   "));
//                */
//            } else {
//                System.out.println(">   This corpus is not a CVS repository");
//            }
//            System.out.println(">   END CVS ADD/COMMIT\n^^^^^^");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    
//
//}