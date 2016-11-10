<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:ical="http://www.w3.org/2002/12/cal/ical#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:gaap="http://edgarwrap.ontologycentral.com/vocab/us-gaap#" xmlns:sdmx-measure="http://purl.org/linked-data/sdmx/2009/measure#" xmlns:qb="http://purl.org/linked-data/cube#" xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:sdmx="http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message" xmlns:common="http://www.SDMX.org/resources/SDMXML/schemas/v2_0/common" xmlns:compact="http://www.SDMX.org/resources/SDMXML/schemas/v2_0/compact" xmlns:cross="http://www.SDMX.org/resources/SDMXML/schemas/v2_0/cross" xmlns:generic="http://www.SDMX.org/resources/SDMXML/schemas/v2_0/generic" xmlns:query="http://www.SDMX.org/resources/SDMXML/schemas/v2_0/query" xmlns:structure="http://www.SDMX.org/resources/SDMXML/schemas/v2_0/structure" xmlns:utility="http://www.SDMX.org/resources/SDMXML/schemas/v2_0/utility" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message SDMXMessage.xsd" xmlns:custom="http://ontologycentral.com/2009/01/eurostat/ns#" version="1.0">
	<xsl:output method="xml" encoding="utf-8"/>
	<xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'"/>
	<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
	<xsl:variable name="id" select="//sdmx:ID"/>
	<xsl:template match="sdmx:CompactData">
		<rdf:RDF>
			<rdf:Description rdf:about="">
				<rdfs:comment>No guarantee of correctness! USE AT YOUR OWN RISK!</rdfs:comment>
				<dcterms:publisher>Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/)</dcterms:publisher>
				<rdfs:seeAlso rdf:resource="http://estatwrap.ontologycentral.com/table_of_contents.rdf"/>
				<foaf:maker rdf:resource="http://cbasewrap.ontologycentral.com/company/ontologycentral#id"/>
				<foaf:topic rdf:resource="#ds"/>
				<dcterms:date>
					<xsl:value-of select="current-dateTime()"/> 
				</dcterms:date>
			</rdf:Description>
			<qb:DataSet>
				<xsl:attribute name="rdf:about">/id/<xsl:value-of select="$id"/>#ds</xsl:attribute>
				<rdfs:comment>Source: Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/).</rdfs:comment>
				<rdfs:seeAlso rdf:resource="http://epp.eurostat.ec.europa.eu/portal/page/portal/about_eurostat/corporate/copyright_licence_policy"/>
				<rdfs:seeAlso rdf:resource="http://ontologycentral.com/2009/01/eurostat/"/>
				<foaf:page rdf:resource=""/>
				<qb:structure>
					<xsl:attribute name="rdf:resource">/dsd/<xsl:value-of select="$id"/>#dsd</xsl:attribute>
				</qb:structure>
			</qb:DataSet>
			<!-- TODO Remove restrictions [position()&lt;=10][position()&lt;=5] -->
			<xsl:for-each select="*[local-name()='DataSet']/*[local-name()='Series']">
				<xsl:variable name="series" select="."/>
				<xsl:for-each select="*[local-name()='Obs']">
					<xsl:variable name="obs" select="."/>
					<qb:Observation>
						<qb:dataSet>
							<xsl:attribute name="rdf:resource">/id/<xsl:value-of select="$id"/>#ds</xsl:attribute>
						</qb:dataSet>
						<xsl:for-each select="$series/@*">
							<xsl:element name="custom:{translate(name(), $uppercase, $lowercase)}">
								<xsl:attribute name="rdf:resource">/dic/<xsl:value-of select="translate(name(), $uppercase, $lowercase)"/>#<xsl:value-of select="."/></xsl:attribute>
							</xsl:element>
						</xsl:for-each>
						<xsl:for-each select="$obs/@*">
							<xsl:choose>
								<xsl:when test="name() = 'OBS_VALUE'">
									<sdmx-measure:obsValue>
										<xsl:choose>
											<xsl:when test=". = ':'">
												<xsl:attribute name="rdf:datatype">http://www.w3.org/2001/XMLSchema#string</xsl:attribute>
											</xsl:when>
											<xsl:otherwise>
												<xsl:attribute name="rdf:datatype">http://www.w3.org/2001/XMLSchema#double</xsl:attribute>
												<xsl:value-of select="."/>
											</xsl:otherwise>
										</xsl:choose>
									</sdmx-measure:obsValue>
								</xsl:when>
								<xsl:when test="name() = 'TIME_PERIOD'">
									<dcterms:date>
										<xsl:value-of select="replace(replace(replace(replace(replace(replace(., 'M', '-'), 'D', '-'), 'Q1', '-03-31'), 'Q2', '-06-30'), 'Q3', '-09-30'), 'Q4', '-12-31')"/>
									</dcterms:date>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="custom:{translate(name(), $uppercase, $lowercase)}">
										<xsl:attribute name="rdf:resource">/dic/<xsl:value-of select="translate(name(), $uppercase, $lowercase)"/>#<xsl:value-of select="."/></xsl:attribute>
									</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</qb:Observation>
				</xsl:for-each>
			</xsl:for-each>
		</rdf:RDF>
	</xsl:template>
</xsl:stylesheet>
