<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:w="http://java.sun.com/xml/ns/javaee"
   xmlns:q="http://appengine.google.com/ns/1.0"
   xmlns:xalan="http://xml.apache.org/xslt"
>
  <xsl:output method="xml" indent="yes" xalan:indent-amount="2" encoding="utf-8" standalone="no" />
  <xsl:strip-space elements="*"/>
  <xsl:param name="localSqlUrl"/>
  <xsl:param name="cloudSqlUrl"/>
  <xsl:param name="localSqlUsername"/>
  <xsl:param name="cloudSqlUsername"/>
  <xsl:param name="localSqlPassword"/>
  <xsl:param name="cloudSqlPassword"/>

  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
    <xsl:text xml:space="preserve"/>
  </xsl:template>

  <xsl:template match="/q:appengine-web-app/q:system-properties/q:property[@name='ae-cloudsql.database-url']/@value">
     <xsl:attribute name="value">
    	<xsl:value-of select="$cloudSqlUrl"/>
  	 </xsl:attribute>
  </xsl:template>
  <xsl:template match="/q:appengine-web-app/q:system-properties/q:property[@name='ae-cloudsql.database-username']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$cloudSqlUsername"/>
     </xsl:attribute>
  </xsl:template>
  <xsl:template match="/q:appengine-web-app/q:system-properties/q:property[@name='ae-cloudsql.database-password']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$cloudSqlPassword"/>
     </xsl:attribute>
  </xsl:template>
  <xsl:template match="/q:appengine-web-app/q:system-properties/q:property[@name='ae-cloudsql.local-database-url']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$localSqlUrl"/>
     </xsl:attribute>
  </xsl:template>
  <xsl:template match="/q:appengine-web-app/q:system-properties/q:property[@name='ae-cloudsql.local-database-username']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$localSqlUsername"/>
     </xsl:attribute>
  </xsl:template>
  <xsl:template match="/q:appengine-web-app/q:system-properties/q:property[@name='ae-cloudsql.local-database-password']/@value">
     <xsl:attribute name="value">
      <xsl:value-of select="$localSqlPassword"/>
     </xsl:attribute>
  </xsl:template>
</xsl:stylesheet>
