java -cp /home/vkaraisk/Projects/docbook_stuff/docbook_files/lib:\
/home/vkaraisk/Projects/docbook_stuff/docbook_files/lib/bsf.jar:\
$FOP_HOME/lib/xalan-2.4.1.jar:\
$FOP_HOME/lib/xercesImpl-2.2.1.jar:\
$FOP_HOME/lib/xml-apis.jar:\
$DOCBOOK_XSL_HOME/extensions/xalan2.jar:\
$FOP_HOME/build/fop.jar:\
$FOP_HOME/lib/avalon-framework-cvs-20020806.jar:\
$FOP_HOME/lib/JimiProClasses.zip:\
$FOP_HOME/lib/batik.jar:\
/group/ltg/projects/lcontrib/share/lib/xml/xml-commons-resolver-1.1/resolver.jar:\
/group/ltg/projects/lcontrib/share/lib/xml \
org.apache.fop.apps.Fop \
-fo fo_output/documentation.fo \
-pdf fo_output/documentation.pdf 
