<?xml version="1.0" encoding="UTF-8"?>

<!-- Document : DIM2Crossref.xsl Description: Converts metadata from DSpace
	Intermediat Format (DIM) into metadata following the Crossref Schema -->
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dspace="http://www.dspace.org/xmlns/dspace/dim">

	<xsl:output method="xml" indent="yes" encoding="utf-8" />

	<xsl:template match="@* | text()" />

	<xsl:template match="/dspace:dim[@dspaceType='ITEM']">
		<!-- org.dspace.identifier.doi.CrossrefConnector uses this XSLT to transform
			metadata for the Crossref metadata store. -->

		<doi_batch version="4.4.2"
			xmlns="http://www.crossref.org/schema/4.4.2"
			xsi:schemaLocation="http://www.crossref.org/schema/4.4.2 https://www.crossref.org/schemas/crossref4.4.2.xsd">

			<head>
				<doi_batch_id>string(4,100)</doi_batch_id>
				<timestamp>now()</timestamp>

				<depositor>
					<depositor_name>Universidad Nacional de La Plata</depositor_name>
					<email_address>admin@sedici.unlp.edu.ar</email_address>
				</depositor>

				<registrant>Universidad Nacional de La Plata</registrant>
			</head>

			<body>
				<xsl:choose
					test="//dspace:field[@mdschema='dc' and @element='type']">
					<xsl:when test="string(text())='Tesis de doctorado'">
						<xsl:call-template
							name="setDissertationMetadata" />
					</xsl:when>
					<xsl:when test="string(text())='Tesis de maestria'">
						<xsl:call-template
							name="setDissertationMetadata" />
					</xsl:when>
					<xsl:when
						test="string(text())='Trabajo de especializacion'">
						<xsl:call-template
							name="setDissertationMetadata" />
					</xsl:when>
					<xsl:when test="string(text())='Tesis de grado'">
						<xsl:call-template
							name="setDissertationMetadata" />
					</xsl:when>
					<xsl:when test="string(text())='Trabajo final de grado'">
						<xsl:call-template
							name="setDissertationMetadata" />
					</xsl:when>
					<!-- <xsl:when test="string(text())='Documento de trabajo'"> -->
					<!-- documento de trabajo -->
					<!-- </xsl:when> -->
					<!-- No se exporta <xsl:when test="$subtype='Preprint'"> artículo </xsl:when> -->
					<!-- <xsl:when test="string(text())='Articulo'"> -->
					<!-- artículo -->
					<!-- </xsl:when> -->
					<!-- <xsl:when test="$subtype='Resumen'"> -->
					<!-- documento de conferencia -->
					<!-- </xsl:when> -->
					<!-- No se exporta <xsl:when test="$subtype='Comunicacion'"> artículo 
						<!-- </xsl:when> -->
					-->
					<!-- <xsl:when test="string(text())='Revision'"> -->
					<!-- reseña artículo -->
					<!-- </xsl:when> -->
					<!-- No se exporta <xsl:when test="$subtype='Contribucion a revista'"> 
						<!-- artículo </xsl:when> -->
					-->
					<!-- <xsl:when test="string(text())='Reporte'"> -->
					<!-- informe técnico -->
					<!-- </xsl:when> -->
					<!-- <xsl:when test="string(text())='Patente'"> -->
					<!-- patente -->
					<!-- </xsl:when> -->
					<!-- <xsl:when test="string(text())='Libro'"> -->
					<!-- libro -->
					<!-- </xsl:when> -->
					<!-- <xsl:when test="string(text())='Capitulo de libro'"> -->
					<!-- parte de libro -->
					<!-- </xsl:when> -->
					<!-- <xsl:when test="string(text())='Objeto de conferencia'"> -->
					<!-- documento de conferencia -->
					<!-- </xsl:when> -->
					<!-- <xsl:when test="string(text())='Conjunto de datos'"> -->
					<!-- conjunto de datos -->
					<!-- </xsl:when> -->
					<!-- No se exportan <xsl:when test="$subtype='Documento institucional'"> 
						otros </xsl:when> <xsl:when test="$subtype='Resolucion'"> otros </xsl:when> 
						<xsl:when test="$subtype='Imagen fija'"> fotografía </xsl:when> <xsl:when 
						test="$subtype='Video'"> videograbación </xsl:when> <xsl:when test="$subtype='Audio'"> 
						otros </xsl:when> -->
					<xsl:otherwise>
					</xsl:otherwise>
				</xsl:choose>
			</body>
		</doi_batch>

	</xsl:template>

	<xsl:template name="setDissertationMetadata">
		<dissertation language="" publication_type="full_text"
			reference_distribution_opts="none"
			xmlns="http://www.crossref.org/schema/4.4.2"
			xmlns:jats="http://www.ncbi.nlm.nih.gov/JATS1"
			xmlns:fr="http://www.crossref.org/fundref.xsd"
			xmlns:ai="http://www.crossref.org/AccessIndicators.xsd"
			xmlns:rel="http://www.crossref.org/relations.xsd">
			<person_name contributor_role="" language=""
				name-style="" sequence="">
				{1,unbounded}
			</person_name>
			<contributors>
				{1,1}
			</contributors>
			<titles>
				<title>
					<xsl:value-of
						select="//dspace:field[@mdschema='dc' and @element='title' and not(@qualifier='alternative')]" />
				</title>
				<subtitle>
					<xsl:value-of
						select="//dspace:field[@mdschema='sedici' and @element='title' and not(@qualifier='subtitlr')]" />
				</subtitle>
			</titles>
			<jats:abstract abstract-type="" xml:base="" id=""
				xml:lang="" specific-use="">
				{0,unbounded}
			</jats:abstract>
			<approval_date media_type="print">
				{1,10}
			</approval_date>
			<institution>
				{1,6}
			</institution>
			<degree>{0,10}</degree>
			<isbn media_type="print">{0,6}</isbn>
			<publisher_item>
				{0,1}
			</publisher_item>
			<crossmark>
				{0,1}
			</crossmark>
			<fr:program name="fundref">
				{0,1}
			</fr:program>
			<ai:program name="AccessIndicators">
				{0,1}
			</ai:program>
			<rel:program name="relations">
				{0,1}
			</rel:program>
			<archive_locations>
				{0,1}
			</archive_locations>
			<scn_policies>
				{0,1}
			</scn_policies>
			<doi_data>
				{1,1}
			</doi_data>
			<citation_list>
				{0,1}
			</citation_list>
			<component_list>
				{0,1}
			</component_list>
		</dissertation>
	</xsl:template>
</xsl:stylesheet>
