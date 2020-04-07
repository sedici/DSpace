<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
        xmlns:epdcx="http://purl.org/eprint/epdcx/2006-11-16/"
        version="1.0">

<!-- NOTE: This stylesheet is a work in progress, and does not
     cover all aspects of the SWAP and EPDCX specification/schema.
     It is used principally to demonstrate the SWORD ingest
     process -->

<!-- This stylesheet converts incoming DC metadata in a SWAP
     profile into the DSpace Interal Metadata format (DIM) -->

	<!-- Catch all.  This template will ensure that nothing
	     other than explicitly what we want to xwalk will be dealt
	     with -->
	<xsl:template match="text()"></xsl:template>
    
    <!-- match the top level descriptionSet element and kick off the
         template matching process -->
    <xsl:template match="/epdcx:descriptionSet">
    	<dim:dim>
		<xsl:call-template name="the_title"/>
    		<xsl:apply-templates/>
    	</dim:dim>
    </xsl:template>
    
    <!-- general matcher for all "statement" elements -->
    <xsl:template match="/epdcx:descriptionSet/epdcx:description/epdcx:statement">
    
	  	<!-- abstract element: dc.description.abstract -->
    	<xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/abstract'">
    		<dim:field mdschema="dc" element="description" qualifier="abstract">
			<xsl:attribute name="lang">
                        	<xsl:value-of select="./epdcx:valueString/@lang"/>
                	</xsl:attribute>
    			<xsl:value-of select="epdcx:valueString"/>
    		</dim:field>
    	</xsl:if>

       	<!-- UNLP fix: issue number: dc.terms.isPartOf -->
    	<xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/isPartOf'">
    		<dim:field mdschema="sedici" element="relation" qualifier="journalVolumeAndIssue">
    			<xsl:value-of select="substring-before(epdcx:valueString,'*')"/>
    		</dim:field>
		<dim:field mdschema="sedici" element="relation" qualifier="journalTitle">
			<xsl:value-of select="substring-after(epdcx:valueString,'*')"/>
		</dim:field>
    	</xsl:if>

    	<!-- creator element: dc.contributor.author -->
        <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/creator'">
        	<dim:field mdschema="sedici" element="creator" qualifier="person">
            	<xsl:value-of select="epdcx:valueString"/>
            </dim:field>
        </xsl:if>


		<!--UNLP fix  identifier elements: dc.identifier.*, adapt to the specific sedici metadata -->
    	<xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/identifier'">
    		<xsl:choose>
			<xsl:when test="substring-before(epdcx:valueString,':')='doi'">
				<dim:field mdschema="sedici" element="identifier" qualifier="doi">
    					<xsl:value-of select="epdcx:valueString"/>
    				</dim:field>
			</xsl:when>
			<xsl:when test="substring-before(epdcx:valueString,':')='http'">
				<dim:field mdschema="sedici" element="identifier" qualifier="uri">
					<xsl:value-of select="epdcx:valueString"/>
				</dim:field>
			</xsl:when>
			<xsl:otherwise>
				<dim:field mdschema="sedici" element="identifier" qualifier="other">
					<xsl:value-of select="epdcx:valueString"/>
				</dim:field>
			</xsl:otherwise>
		</xsl:choose>
    	</xsl:if>
    	
		<!-- language element: dc.language -->
    	<xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/language' and ./@epdcx:vesURI='http://purl.org/dc/terms/RFC3066'">
    		<dim:field mdschema="dc" element="language" language="es">
    			<xsl:value-of select="epdcx:valueString"/>
    		</dim:field>
    	</xsl:if>    

	
	 <!-- UNLP fix add license: license element: sedici.rights.uri -->
	<xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/license'">
                <dim:field mdschema="sedici" element="rights" qualifier="uri">
                        <xsl:value-of select="epdcx:valueString"/>
                </dim:field>
        </xsl:if>

	 <!-- UNLP fix add keywords: keywords  element: sedici.subject.keyword -->
	 <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/subject'">
                <dim:field mdschema="sedici" element="subject" qualifier="keyword">
                <xsl:value-of select="epdcx:valueString"/>
            </dim:field>
        </xsl:if>


    	
    	<!-- item type element: dc.type  Se setea Article por defecto si viene como JournalArticle -->
   		<xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/type' and ./@epdcx:vesURI='http://purl.org/eprint/terms/Type'">
   	 		<xsl:if test="./@epdcx:valueURI='http://purl.org/eprint/type/JournalArticle'">
   	 			<dim:field mdschema="dc" element="type">
   	 				Articulo
				</dim:field>
   	 		</xsl:if>
   		</xsl:if>
    	
    	<!-- date available element: dc.date.issued--> 
    		<xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/available'">
			<xsl:choose>
          			<xsl:when test="/epdcx:descriptionSet/epdcx:description/epdcx:statement[@epdcx:valueURI='http://purl.org/eprint/type/JournalArticle']">
            				<dim:field mdschema="dc" element="date" qualifier="issued">
						<xsl:value-of select="substring-before(epdcx:valueString,'-')"/>-<xsl:value-of select="substring-before(substring-after(epdcx:valueString,'-'),'-')"/>
					</dim:field>
          			</xsl:when>
        		 	<xsl:otherwise>
            				<dim:field mdschema="dc" element="date" qualifier="issued">
						<xsl:value-of select="epdcx:valueString"/>	
					</dim:field>
          			</xsl:otherwise>
        		</xsl:choose>
    	
    		</xsl:if>
    	
	<!-- publication status element: sedici.description.peerReview -->
    	<xsl:if test="./@epdcx:propertyURI='http://purl.org/eprint/terms/status' and ./@epdcx:vesURI='http://purl.org/eprint/terms/Status'">
    		<xsl:if test="./@epdcx:valueURI='http://purl.org/eprint/status/PeerReviewed'">
	    		<dim:field mdschema="sedici" element="description" qualifier="peerReview">
	    			Peer Reviewed
	    		</dim:field>
    		</xsl:if>
    	</xsl:if>    
    	 	
    </xsl:template>

    <!---title and alternative title -->
    <xsl:template name="the_title">
	<xsl:if test="epdcx:description/epdcx:statement[@epdcx:propertyURI='http://purl.org/dc/elements/1.1/title'][1]">
	    <dim:field mdschema="dc" element="title">
		<xsl:attribute name="lang">
			<xsl:value-of select="epdcx:description/epdcx:statement[@epdcx:propertyURI='http://purl.org/dc/elements/1.1/title'][1]/epdcx:valueString/@lang"/>
		</xsl:attribute>
		<xsl:value-of select="epdcx:description/epdcx:statement[@epdcx:propertyURI='http://purl.org/dc/elements/1.1/title'][1]/epdcx:valueString"/>
	    </dim:field>
	</xsl:if>
    </xsl:template>
    
    <xsl:template match="/epdcx:descriptionSet/epdcx:description/epdcx:statement[@epdcx:propertyURI='http://purl.org/dc/elements/1.1/title']">
	<dim:field mdschema="dc" element="title" qualifier="alternative">
		 <xsl:attribute name="lang">
                        <xsl:value-of select="epdcx:valueString/@lang"/>
                </xsl:attribute>
		<xsl:value-of select="epdcx:valueString"/>
	</dim:field>
    </xsl:template>    
    
    <xsl:template match="/epdcx:descriptionSet/epdcx:description/epdcx:statement[@epdcx:propertyURI='http://purl.org/dc/elements/1.1/title'][1]">
    </xsl:template>
    
</xsl:stylesheet>
