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
			xmlns:jats="http://www.ncbi.nlm.nih.gov/JATS1"
			xmlns:fr="http://www.crossref.org/fundref.xsd"
			xmlns:ai="http://www.crossref.org/AccessIndicators.xsd"
			xmlns:rel="http://www.crossref.org/relations.xsd">

			<!-- contributors -->
			<xsl:call-template name="setContributors" />

			<!-- titles -->
			<xsl:call-template name="setTitles" />

			<!-- jats:abstract -->
			<xsl:call-template name="setAbstract" />

			<!-- approval_date -->
			<xsl:call-template name="setApprovalDate" />

			<!-- institution -->
			<xsl:call-template name="setInstitution" />

			<!-- degree -->
			<degree>
				<xsl:value-of
					select="//dspace:field[@mdschema='thesis' and @element='degree' and @qualifier='name']" />
			</degree>

			<!-- isbn -->
			<xsl:call-template name="setISBN" />

			<!-- publisher_item -->
			<xsl:call-template name="setPublisherItem" />

			<!--
			<crossmark></crossmark>
			-->

			<!-- No se mapea, no tenemos información de fundRef
			<fr:program name="fundref"></fr:program>
			-->

			<!-- ai:program -->
			<xsl:call-template name="setAIProgram" />

			<!-- rel:program -->
			<xsl:call-template name="setRelationsProgram" />

			<!-- No se mapea porque no usamos ninguna red de preservación
			<archive_locations></archive_locations>
			-->

			<!-- No se mapea porque no utilizamos Scholarly Sharing Network (SCN) policies
			<scn_policies></scn_policies>
			-->

			<doi_data>
			<!-- To Do -->
			</doi_data>

			<!-- No se mapea, no tenemos esa información
			<citation_list></citation_list>
			-->

			<!-- No se mapea, no aplica a ningún metadato que tengamos
			<component_list></component_list>
			-->
		</dissertation>
	</xsl:template>

	<xsl:template name="setTitles">
		<titles>
			<title>
				<xsl:value-of
					select="//dspace:field[@mdschema='dc' and @element='title' and not(@qualifier='alternative')]" />
			</title>
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='title' and not(@qualifier='subtitlo')]">
				<subtitle>
					<xsl:value-of
						select="//dspace:field[@mdschema='sedici' and @element='title' and not(@qualifier='subtitlo')]" />
				</subtitle>
			</xsl:if>
		</titles>
	</xsl:template>

	<xsl:template name="setContributors">
		<contributors>

			<!-- <person-name role=author> -->
			<xsl:call-template name="setPersonName">
				<xsl:with-param name="person"
					select="//dspace:field[@mdschema='sedici' and @element='creator' and @qualifier='person']" />
				<xsl:with-param name="role">
					author
				</xsl:with-param>
				<xsl:with-param name="sequence">
					first
				</xsl:with-param>
			</xsl:call-template>

			<!-- <organization> -->
			<xsl:for-each
				select="//dspace:field[@mdschema='sedici' and @element='contributor' and @qualifier='corporate]']">
				<organization contributor_role="author"
					sequence="first">
					<xsl:value-of select="." />
				</organization>
			</xsl:for-each>

			<!-- <person-name role=editor> -->
			<xsl:for-each
				select="//dspace:field[@mdschema='sedici' and @element='contributor' and @qualifier='editor']" ">
				<xsl:call-template name="setPersonName">
					<xsl:with-param name="person" select="." />
					<xsl:with-param name="role">
						editor
					</xsl:with-param>
					<xsl:with-param name="sequence">
						first
					</xsl:with-param>
				</xsl:call-template>
			</xsl:for-each>
		</contributors>
	</xsl:template>

	<xsl:template name="setPersonName">
		<xsl:param name="person" />
		<xsl:param name="role" />
		<xsl:param name="sequence" />
		<person-name contributor_role="$role" sequence="$sequence"
			language="$person/@language">
			<xsl:if test="contains($person,',')">
				<given-name>
					<xsl:value-of
						select="substring(substring-after($person,','),1,60)" />
				</given-name>
			</xsl:if>
			<surname>
				<xsl:value-of
					select="substring(substring-before($person,','),1,60)" />
			</surname>
		</person-name>
	</xsl:template>

	<xsl:template name="setAbstract" m>
		<xsl:for-each
			select="//dspace:field[@mdschema='dc' and @element='description' and @qualifier='abstract']">
			<jats:abstract xml:lang="./@language">
				<jats:p>
					<xsl:value-of select="." />
				</jats:p>
			</jats:abstract>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="setApprovalDate">
		<xsl:for-each
			select="//dspace:field[@mdschema='sedici' and @element='date' and @qualifier='exposure']">
			<approval_date>
				<xsl:if test="string-length(./text()) &gt; 5">
					<month>
						<xsl:value-of select="substring(./text(),6,2)" />
					</month>
				</xsl:if>
				<xsl:if test="string-length(./text()) &gt; 8">
					<day>
						<xsl:value-of select="substring(./text(),9)" />
					</day>
				</xsl:if>
				<year>
					<xsl:value-of select="substring(./text(),1,4)" />
				</year>
			</approval_date>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="setInstitution">
		<xsl:if
			test="//dspace:field[@mdschema='thesis' and @element='degree' and @qualifier='grantor']">
			<institution>
				<institution_name>
					<xsl:value-of
						select="//dspace:field[@mdschema='thesis' and @element='degree' and @qualifier='grantor']" />
				</institution_name>
			</institution>
		</xsl:if>
	</xsl:template>

	<xsl:template name="setISBN">
		<xsl:for-each
			select="//dspace:field[@mdschema='sedici' and @element='identifier' and @qualifier='isbn']">
			<isbn>
				<xsl:value-of select="." />
			</isbn>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="setPublisherItem">
		<publisher_item>
			<!-- dc.identifier.uri -->
			<identifier id_type="other">
				<xsl:value-of
					select="//dspace:field[@mdschema='dc' and @element='identifier' and @qualifier='uri']" />
			</identifier>

			<!-- sedici.identifier.doi -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='identifier' and @qualifier='doi']">
				<identifier id_type="doi">
					<xsl:value-of
						select="//dspace:field[@mdschema='dc' and @element='identifier' and @qualifier='doi']" />
				</identifier>
			</xsl:if>

			<!-- dc.identifier.expendiente -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='identifier' and @qualifier='expediente']">
				<item_number item_number_type="record_number">
					<xsl:value-of
						select="//dspace:field[@mdschema='dc' and @element='identifier' and @qualifier='expendiente']" />
				</item_number>
			</xsl:if>
		</publisher_item>
	</xsl:template>

	<xsl:template name="setAIProgram">
		<ai:program name="AccessIndicators">
			<license_ref>
				<xsl:value-of
					select="//dspace:field[@mdschema='sedici' and @element='rights' and @qualifier='uri']" />
			</license_ref>
		</ai:program>
	</xsl:template>

	<xsl:template name="setRelationsProgram">
		<rel:program name="relations">
			<!-- sedici.relation.journalTitle -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='journalTitle']">
				<rel:related_item>
					<description>Journal title which the item is part of</description>
					<rel:inter_work_relation
						identifier-type="other" relationship-type="isPartOf">
						<xsl:value-of
							select="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='journalTitle']" />
					</rel:inter_work_relation>
				</rel:related_item>
			</xsl:if>

			<!-- sedici.relation.journalVolumeAndIssue -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='journalVolumeAndIssue']">
				<rel:related_item>
					<description>Journal issue or volume which the item is part of</description>
					<rel:inter_work_relation
						identifier-type="other" relationship-type="isPartOf">
						<xsl:value-of
							select="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='journalVolumeAndIssue']" />
					</rel:inter_work_relation>
				</rel:related_item>
			</xsl:if>

			<!-- sedici.relation.event -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='event']">
				<rel:related_item>
					<description>Event name the item is part of</description>
					<rel:inter_work_relation
						identifier-type="other" relationship-type="isPartOf">
						<xsl:value-of
							select="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='event']" />
					</rel:inter_work_relation>
				</rel:related_item>
			</xsl:if>

			<!-- sedici.relation.isRelatedWith -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='isRelatedWith']">
				<rel:related_item>
					<rel:inter_work_relation
						identifier-type="uri" relationship-type="isRelatedMaterial">
						<xsl:value-of
							select="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='isRelatedWith']" />
					</rel:inter_work_relation>
				</rel:related_item>
			</xsl:if>

			<!-- sedici.relation.ciclo -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='ciclo']">
				<rel:related_item>
					<description>Program name which the item is part of</description>
					<rel:inter_work_relation
						identifier-type="other" relationship-type="isPartOf">
						<xsl:value-of
							select="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='ciclo']" />
					</rel:inter_work_relation>
				</rel:related_item>
			</xsl:if>

			<!-- sedici.relation.isPartOfSeries -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='isPartOfSeries']">
				<rel:related_item>
					<description>Series which the item is part of</description>
					<rel:inter_work_relation
						identifier-type="other" relationship-type="isPartOf">
						<xsl:value-of
							select="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='isPartOfSeries']" />
					</rel:inter_work_relation>
				</rel:related_item>
			</xsl:if>

			<!-- sedici.relation.bookTitle -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='bookTitle']">
				<rel:related_item>
					<description>Book title which the item is part of</description>
					<rel:inter_work_relation
						identifier-type="other" relationship-type="isPartOf">
						<xsl:value-of
							select="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='bookTitle']" />
					</rel:inter_work_relation>
				</rel:related_item>
			</xsl:if>

			<!-- sedici.relation.isReviewOf -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='isReviewOf']">
				<rel:related_item>
					<rel:inter_work_relation
						identifier-type="uri" relationship-type="isReviewOf">
						<xsl:value-of
							select="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='isReviewOf']" />
					</rel:inter_work_relation>
				</rel:related_item>
			</xsl:if>

			<!-- sedici.relation.isReviewedBy -->
			<xsl:if
				test="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='isReviewedBy']">
				<rel:related_item>
					<rel:inter_work_relation
						identifier-type="uri" relationship-type="hasReview">
						<xsl:value-of
							select="//dspace:field[@mdschema='sedici' and @element='relation' and @qualifier='isReviewedBy']" />
					</rel:inter_work_relation>
				</rel:related_item>
			</xsl:if>
		</rel:program>
	</xsl:template>
</xsl:stylesheet>
