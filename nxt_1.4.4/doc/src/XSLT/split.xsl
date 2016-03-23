<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="2.0">

<xsl:output method="text"/>
<xsl:output method="xml" indent="no" name="xml" doctype-system="http://www.oasis-open.org/docbook/xml/4.4/docbookx.dtd" standalone="yes"/>
<xsl:output method="xml" indent="yes" omit-xml-declaration="yes" name="xml2"/>

<xsl:template match="/">
<xsl:for-each select="article//(section|appendix)[not(@status='private')]">
<xsl:variable name="depth" select="count(ancestor::*)" />
<xsl:if test="$depth &lt; 3">
<xsl:variable name="filename" select="concat('site/doc',@id,'.xml')" />
<xsl:variable name="thisid" select="@id" />
<!-- <xsl:value-of select="$filename" />   Creating  -->
<xsl:result-document href="{$filename}" format="xml">
    <xsl:processing-instruction name="xml-stylesheet">type="text/css" href="wysiwygdocbook1.02/navigate.css"</xsl:processing-instruction>
    <book xmlns:xhtml="http://www.w3.org/1999/xhtml">
      <!-- Header material same for all documents -->
      <title>NXTdoc</title>
      <!-- this set of navigation links are common to all pages -->
      <sidebar id="staticlinks">
         <para><xhtml:a href="http://groups.inf.ed.ac.uk/nxt/">NXT Home</xhtml:a><xsl:text>
	 </xsl:text><xhtml:a href="http://groups.inf.ed.ac.uk/nxt/search.shtml">Search</xhtml:a><xsl:text>
	 </xsl:text><xhtml:a href="http://groups.inf.ed.ac.uk/nxt/nxtdoc.pdf">Download doc as PDF</xhtml:a></para>
      </sidebar>

      <!-- Navigation gets repeated for every file so we can distinguish
      where we are in the tree. Of course this would be better done with
      parameterized inclusion or perhaps JavaScript or something.. -->
      <sidebar id="navigation">
       	<itemizedlist>
      <xsl:for-each select="/article/(section|appendix)[not(@status='private')]">
       <xsl:variable name="filename" select="concat('site/doc',@id,'.xml')" />
       <xsl:variable name="param" select="concat('doc',@id,'.xml')" />
        <listitem><para>
        <xsl:element name="xhtml:a">
         <xsl:attribute name="href" select="$param"/>
         <xsl:if test="$thisid=@id">
          <xsl:attribute name="current">yes</xsl:attribute>
         </xsl:if>
         <xsl:value-of select="title/(text()|node())"/>
        </xsl:element>
        <xsl:if test="$thisid=@id or $thisid=(section|appendix)/@id">
          <xsl:if test="section|appendix">
          <itemizedlist>
           <xsl:apply-templates select="section|appendix" mode="navigation">
            <xsl:with-param name="id" select="$thisid"/>
           </xsl:apply-templates>
          </itemizedlist>
          </xsl:if>
        </xsl:if>

        </para></listitem>
       </xsl:for-each>
       </itemizedlist>
      </sidebar>

    <article>
      <xsl:copy>
        <xsl:apply-templates select="@* | node()" mode="copy">
          <xsl:with-param name="depth" select="$depth"/>
        </xsl:apply-templates>
      </xsl:copy>
    </article>
    </book>
</xsl:result-document>
</xsl:if>
</xsl:for-each>
</xsl:template>

<xsl:template match="section|appendix" mode="navigation">
   <xsl:param name="id"/>
   <listitem xmlns:xhtml="http://www.w3.org/1999/xhtml"><para>
    <xsl:variable name="thisid" select="@id" />
    <!-- <xsl:variable name="sectionref" select="concat($url,'#',@id)" /> -->
    <xsl:variable name="filename" select="concat('site/doc',@id,'.xml')" />
    <xsl:variable name="param" select="concat('doc',@id,'.xml')" />
<!--    <xsl:variable name="sectionref" select="concat('#',@id)" /> -->
    <xsl:element name="xhtml:a">
      <xsl:attribute name="href" select="$param"/>
      <xsl:if test="$id=@id">
        <xsl:attribute name="current">yes</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="title/(text()|node())"/>
    </xsl:element>
   </para></listitem>
</xsl:template>

<xsl:template match="@* | node()" mode="copy">
  <xsl:param name="depth"/>
  <!-- this xsl:if tells us to not copy any subsections of top-level elements -->
  <xsl:if test="number($depth) &gt; 1 or name(.)!='section'">
   <xsl:copy>
     <xsl:apply-templates select="@* | node()" mode="copy">
       <xsl:with-param name="depth" select="$depth"/>
     </xsl:apply-templates>
   </xsl:copy>
  </xsl:if>
</xsl:template>


<!-- The changes below are due to the stylesheet's weaknesses or poor
browser support -->

<!-- OK - instead of adding xlink info we're transforming ulinks into
xhtml:a. This gives us cross-browser link behaviour (not IE of course!) -->
<xsl:template match="ulink" mode="copy">
  <xsl:param name="depth"/>
  <xhtml:a xmlns:xhtml="http://www.w3.org/1999/xhtml" >
      <xsl:attribute name="href" select="@url"/>
      <xsl:apply-templates select="@* | node()" mode="copy">
        <xsl:with-param name="depth" select="$depth"/>
      </xsl:apply-templates>
  </xhtml:a>
</xsl:template>

<!-- Similarly for cross-browser images we'll transform imageobject's
(which display OK in Firefox) to xhtml:img which display anywhere (not
IE of course!) -->
<xsl:template match="imagedata" mode="copy">
  <xhtml:img xmlns:xhtml="http://www.w3.org/1999/xhtml" >
      <xsl:attribute name="src" select="@fileref"/>
  </xhtml:img>
</xsl:template>

<!-- Grab the xref endpoint info because it might end up in a different document -->
<xsl:template match="xref|link" mode="copy">
  <xsl:variable name="endterm" select="@endterm" />
  <xsl:variable name="linkend" select="@linkend" />
  <xsl:variable name="count" select="count(//*[@id=$linkend]/ancestor::*)"/>
  <xhtml:a xmlns:xhtml="http://www.w3.org/1999/xhtml" >
       <!-- make sure we get a valid link by knowing how files are split! -->
       <!-- it's possible it'll go wrong still, but at least it shows the section name -->
       <xsl:choose>
        <xsl:when test="$count &gt; 2">
         <xsl:attribute name="href">doc<xsl:value-of select="//*[@id=$linkend]/ancestor::*[number($count)-2]/@id"/>.xml</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
         <xsl:attribute name="href">doc<xsl:value-of select="@linkend"/>.xml</xsl:attribute>
        </xsl:otherwise>
       </xsl:choose>
       Section '<xsl:choose>
        <xsl:when test="@endterm">
         <xsl:value-of select="//*[@id=$endterm]/text()"/>
        </xsl:when>
        <xsl:otherwise>
         <xsl:value-of select="//*[@id=$linkend]/@xreflabel"/>
        </xsl:otherwise>
       </xsl:choose>'
  </xhtml:a>
</xsl:template>

<!-- web version of ordered lists don't work - replace with itemized for now -->
<xsl:template match="orderedlist" mode="copy">
 <itemizedlist>
     <xsl:apply-templates select="*" mode="copy"/>
 </itemizedlist>
</xsl:template>

</xsl:stylesheet>
