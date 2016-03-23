java -cp /home/vkaraisk/Projects/docbook_stuff/docbook_files/lib:\
/home/vkaraisk/Projects/docbook_stuff/docbook_files/lib/bsf.jar:\
$FOP_HOME/lib/xalan-2.4.1.jar:\
$FOP_HOME/lib/xercesImpl.jar:\
$FOP_HOME/lib/xml-apis.jar:\
$DOCBOOK_XSL_HOME/extensions/xalan2.jar:\
$FOP_HOME/build/fop.jar:\
$FOP_HOME/lib/JimiProClasses.zip:\
/group/ltg/projects/lcontrib/share/lib/xml/xml-commons-resolver-1.1/resolver.jar:\
/group/ltg/projects/lcontrib/share/lib/xml \
org.apache.xalan.xslt.Process \
-ENTITYRESOLVER org.apache.xml.resolver.tools.CatalogResolver \
-URIRESOLVER org.apache.xml.resolver.tools.CatalogResolver \
-in nxtdoc.top.total.xml \
-xsl html_customization.xsl \
-param base.dir /home/vkaraisk/Projects/docbook_stuff/docbook_files/html_output/
