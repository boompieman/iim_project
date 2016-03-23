package net.sourceforge.nite.datainspection.example;
import net.sourceforge.nite.datainspection.timespan.BoundaryBasedInspection;
import net.sourceforge.nite.datainspection.timespan.BoundaryBasedInspection2;
import net.sourceforge.nite.datainspection.timespan.QuickScan;

public class AMIBoundaryInspections {
    
    public static void main(String[] args) {
        if (args.length <=0) {
            System.out.println("Usage: AMIBoundaryInspection <corpus>");
            System.exit(0);
        }
        //one test... eventually you want all cross coded meetings, all agents, all relevant layers to be called like this...
        QuickScan scan = 
           new QuickScan(
                       args[0], /* corpus */
                       "IS1001a",           /* observation */
                       "foa",              //the name of the Coding in which the boundaries are to be found
                       "foa-layer",        //the name of the Layer in that Coding in which the boundaries are to be found
                       "foa",              //the name of the Elements in the Layer in that Coding in which the boundaries are to be found
                       "A"                  //the name of the agent for which you want to analyse the annotations. If you want to analyse 
                                            //an interaction coding, this parameter should be null
                            );

        BoundaryBasedInspection inspection = 
           new BoundaryBasedInspection(
                       args[0], /* corpus */
                       "IS1001a",           /* observation */
                       "foa",              //the name of the Coding in which the boundaries are to be found
                       "foa-layer",        //the name of the Layer in that Coding in which the boundaries are to be found
                       "foa",              //the name of the Elements in the Layer in that Coding in which the boundaries are to be found
                       "A",                 //the name of the agent for which you want to analyse the annotations. If you want to analyse 
                                            //an interaction coding, this parameter should be null
                       0d,               //thMin, thMax and thSteps together determine which threshold variations will be used to attempt 
                       0.5d,               //to align two boundary annotations.
                       10
                            );


//           new BoundaryBasedInspection(
//                       "E:/AMI/UEDINCVS/Data/AMI/NXT-format/AMI-metadata.xml", /* corpus */
//                       "IS1008a",           /* observation */
//                       "hand",              //the name of the Coding in which the boundaries are to be found
//                       "hand-layer",        //the name of the Layer in that Coding in which the boundaries are to be found
//                       "hand",              //the name of the Elements in the Layer in that Coding in which the boundaries are to be found
//                       "A",                 //the name of the agent for which you want to analyse the annotations. If you want to analyse 
//                                            //an interaction coding, this parameter should be null
//                       0d,               //thMin, thMax and thSteps together determine which threshold variations will be used to attempt 
//                       1d,               //to align two boundary annotations.
//                       10
//                            );

    }
//
//        BoundaryBasedInspection2 inspection = 
//           new BoundaryBasedInspection2(
//                       "E:/AMI/UEDINCVS/Data/AMI/NXT-format/AMI-metadata.xml", /* corpus */
//                       "IS1001a",           /* observation */
//                       "foa",              //the name of the Coding in which the boundaries are to be found
//                       "foa-layer",        //the name of the Layer in that Coding in which the boundaries are to be found
//                       "foa",              //the name of the Elements in the Layer in that Coding in which the boundaries are to be found
//                       "A",                 //the name of the agent for which you want to analyse the annotations. If you want to analyse 
//                                            //an interaction coding, this parameter should be null
//                       0.1d,               //thMin, thMax and thSteps together determine which threshold variations will be used to attempt 
//                       0.5d,                 //to align two boundary annotations.
//                       5
//                            );

}