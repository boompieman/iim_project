<?xml version='1.0'?> 
<xsl:stylesheet  
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"> 

<xsl:import href="/group/ltg/projects/lcontrib/share/lib/xml/docbook-xsl-1.71.0/fo/docbook.xsl"/> 

<!--these customizations are on top of the ones included in the file below; they are common for both html and fo conversions-->
<xsl:include href="common_customizations.xsl"/>


<xsl:param name="paper.type" select="'A4'"></xsl:param>

<!-- page margin -->
<xsl:param name="page.margin.inner">0.7in</xsl:param>

<!-- If it is not set the text body is indented in relation to the section titles in the resulting pdf-->
<xsl:param name="body.start.indent">0pc</xsl:param>

<xsl:attribute-set name="table.cell.padding">
  <xsl:attribute name="padding-left">3pt</xsl:attribute>
  <xsl:attribute name="padding-right">3pt</xsl:attribute>
</xsl:attribute-set>

</xsl:stylesheet>
