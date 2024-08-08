package org.dspace.importer.external.metadatamapping.contributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.importer.external.metadatamapping.MetadataFieldConfig;
import org.dspace.importer.external.metadatamapping.MetadataFieldMapping;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;

public class CombinedJsonPathMetadataContributor implements MetadataContributor<String> {

    private final static Logger log = LogManager.getLogger();

    private List<String> queries;
    private String separator;
    private MetadataFieldConfig field;

    public void setField(MetadataFieldConfig field) {
        this.field = field;
    }

    public void setQueries(List<String> queries) {
        this.queries = queries;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public void setMetadataFieldMapping(MetadataFieldMapping<String, MetadataContributor<String>> rt) {
        // No implementation needed for this example
    }

    @Override
    public Collection<MetadatumDTO> contributeMetadata(String fullJson) {
        Collection<MetadatumDTO> metadata = new ArrayList<>();
        StringBuilder combinedValue = new StringBuilder();
        JsonNode jsonNode = convertStringJsonToJsonNode(fullJson);

        for (String query : queries) {
            JsonNode node = jsonNode.at(query);
            String value = getStringValue(node);
            if (StringUtils.isNotBlank(value)) {
                if (combinedValue.length() > 0) {
                    combinedValue.append(separator);
                }
                if (query.equals("/volume")) {
                    value = "vol. " + value;
                }
                else if (query.equals("/journal-issue/issue")) {
                    value = "no. " + value;
                }
                combinedValue.append(value);
            }
        }

        if (combinedValue.length() > 0) {
            MetadatumDTO metadatumDto = new MetadatumDTO();
            metadatumDto.setValue(combinedValue.toString());
            metadatumDto.setElement(field.getElement());
            metadatumDto.setQualifier(field.getQualifier());
            metadatumDto.setSchema(field.getSchema());
            metadata.add(metadatumDto);
        }

        return metadata;
    }

    private String getStringValue(JsonNode node) {
        if (node.isTextual()) {
            return node.textValue();
        }
        if (node.isNumber()) {
            return node.numberValue().toString();
        }
        log.error("It wasn't possible to convert the value of the following JsonNode:" + node.asText());
        return StringUtils.EMPTY;
    }

    private JsonNode convertStringJsonToJsonNode(String json) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode body = null;
        try {
            body = mapper.readTree(json);
        } catch (Exception e) {
            log.error("Unable to process json response.", e);
        }
        return body;
    }
}