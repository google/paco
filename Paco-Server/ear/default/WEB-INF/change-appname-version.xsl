<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:qw="http://java.sun.com/xml/ns/javaee"
  xmlns:w="http://appengine.google.com/ns/1.0"
  xmlns:xalan="http://xml.apache.org/xslt"
>
  <xsl:param name="appName"/>
  <xsl:param name="appVersion"/>
  <xsl:output method="xml"  indent="yes"  xalan:indent-amount="2" encoding="utf-8" standalone="no"  />
  <xsl:strip-space elements="*"/>
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
    <xsl:text xml:space="preserve"/>
  </xsl:template>
  <xsl:template match="/w:appengine-web-app/w:application/text()">
    <xsl:value-of select="$appName"/>
  </xsl:template>
  <xsl:template match="/w:appengine-application/w:application/text()">
    <xsl:value-of select="$appName"/>
  </xsl:template>
  <xsl:template match="/w:appengine-web-app/w:version/text()">
    <xsl:value-of select="$appVersion"/>
  </xsl:template>
</xsl:stylesheet>
