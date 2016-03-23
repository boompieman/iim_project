<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="2.0">

<!-- This stylesheet is supposed to add a table of contents list to
the start of a DocBook document for the Print version. Prince does not
auto-generate such things. Another weakness is handling of intenal
links within the document which have to be processed in some way.. -->

<xsl:output method="xml" indent="no" doctype-public="-//OASIS//DTD DocBook XML V4.4//EN" doctype-system="http://www.oasis-open.org/docbook/xml/4.4/docbookx.dtd"/>

<xsl:template match="/">
  <xsl:processing-instruction name="xml-stylesheet">type="text/css" href="wysiwygdocbook1.02/print.css"</xsl:processing-instruction>
  <article>
    <xsl:apply-templates select="article/(title|articleinfo)" mode="copy"/>    
    <toc>
<!--
      <xsl:if test="article/(section|appendix)[not(@status='private')]">
          <xsl:apply-templates select="article/(section|appendix)[not(@status='private')]" mode="navigation"/>
-->
      <xsl:if test="article/(section|appendix)">
          <xsl:apply-templates select="article/(section|appendix)" mode="navigation"/>
      </xsl:if>
    </toc>
    <xsl:apply-templates select="article/(section|appendix)" mode="copy"/>
  </article>
</xsl:template>

<xsl:template match="section|appendix" mode="navigation">
  <xsl:if test="count(ancestor::*)&lt;4">
  <xsl:variable name="elname">
    <xsl:choose>
      <xsl:when test='count(ancestor::*)&lt;=1'>tocpart</xsl:when>
      <xsl:when test='count(ancestor::*)=2'>tocchap</xsl:when>
      <xsl:when test='count(ancestor::*)=3'>toclevel1</xsl:when>
      <xsl:when test='count(ancestor::*)=4'>toclevel2</xsl:when>
      <xsl:when test='count(ancestor::*)=5'>toclevel3</xsl:when>
      <xsl:when test='count(ancestor::*)=6'>toclevel4</xsl:when>
      <xsl:otherwise>toclevel5</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:element name="{$elname}">
      <xsl:if test="name(.)='appendix'">
        <xsl:attribute name="class">appendix</xsl:attribute>
      </xsl:if>
    <tocentry>
      <xsl:attribute name="linkend">#<xsl:value-of select="@id"/></xsl:attribute>
      <xsl:value-of select="title/(text()|node())"/>
    </tocentry>
<!--
    <xsl:if test="section|appendix[not(@status='private')]">
        <xsl:apply-templates select="section|appendix[not(@status='private')]" mode="navigation"/> -->
    <xsl:if test="section|appendix">
        <xsl:apply-templates select="section|appendix" mode="navigation"/>
    </xsl:if>
  </xsl:element>
  </xsl:if>
</xsl:template>

<!--
<xsl:template match="xref" mode="copy">
  <xsl:variable name="endterm" select="@endterm" />
  <ulink>
    <xsl:attribute name="url">#<xsl:value-of select="@endterm"/></xsl:attribute>
     Section '<xsl:value-of select="//*[@id=$endterm]/text()"/>'
  </ulink>
</xsl:template>
-->

<!-- Why do we do this? Well, Prince expects cross references to start
with a hash character, and we also go and retrive the value of any
specified linkend as that's hard to do in CSS - not sure what the
intended difference is between link and xref elements so I'm
conflating them --> 
<xsl:template match="xref|link" mode="copy">
  <xsl:variable name="linkend" select="@linkend" />
  <xref>
    <xsl:if test="@endterm">
      <xsl:attribute name="endterm">#<xsl:value-of select="@endterm"/></xsl:attribute>
    </xsl:if>
    <xsl:attribute name="linkend">#<xsl:value-of select="@linkend"/></xsl:attribute>
    <xsl:attribute name="content"><xsl:value-of select="//*[@id=$linkend]/@xreflabel"/></xsl:attribute>
  </xref>
</xsl:template>

<!-- print version of ordered lists don't work - replace with itemized for now -->
<xsl:template match="orderedlist" mode="copy">
 <itemizedlist>
     <xsl:apply-templates select="*" mode="copy"/>
 </itemizedlist>
</xsl:template>

<xsl:template match="@* | node()" mode="copy">
   <xsl:copy>
     <xsl:apply-templates select="@* | node()" mode="copy"/>
   </xsl:copy>
</xsl:template>


</xsl:stylesheet>
