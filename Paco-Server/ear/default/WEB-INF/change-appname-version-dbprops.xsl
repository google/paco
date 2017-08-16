<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:qw="http://java.sun.com/xml/ns/javaee"
  xmlns:w="http://appengine.google.com/ns/1.0"
  xmlns:xalan="http://xml.apache.org/xslt"
>
  <xsl:param name="appName"/>
  <xsl:param name="appVersion"/>
   <xsl:param name="localSqlUrl"/>
  <xsl:param name="cloudSqlUrl"/>
   <xsl:param name="localSqlUsername"/>
  <xsl:param name="cloudSqlUsername"/>
   <xsl:param name="localSqlPassword"/>
  <xsl:param name="cloudSqlPassword"/>
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
  <xsl:template match="/w:appengine-web-app/w:system-properties/w:property[@name='ae-cloudsql.database-url']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$cloudSqlUrl"/>
     </xsl:attribute>
  </xsl:template>
  <xsl:template match="/w:appengine-web-app/w:system-properties/w:property[@name='ae-cloudsql.database-username']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$cloudSqlUsername"/>
     </xsl:attribute>
  </xsl:template>
  <xsl:template match="/w:appengine-web-app/w:system-properties/w:property[@name='ae-cloudsql.database-password']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$cloudSqlPassword"/>
     </xsl:attribute>
  </xsl:template>
  <xsl:template match="/w:appengine-web-app/w:system-properties/w:property[@name='ae-cloudsql.local-database-url']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$localSqlUrl"/>
     </xsl:attribute>
  </xsl:template>
  <xsl:template match="/w:appengine-web-app/w:system-properties/w:property[@name='ae-cloudsql.local-database-username']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$localSqlUsername"/>
     </xsl:attribute>
  </xsl:template>
  <xsl:template match="/w:appengine-web-app/w:system-properties/w:property[@name='ae-cloudsql.local-database-password']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$localSqlPassword"/>
     </xsl:attribute>
  </xsl:template>
</xsl:stylesheet>
