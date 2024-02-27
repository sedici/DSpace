package ar.edu.unlp.sedici.dspace.authority;

import java.util.Map;

import org.dspace.content.authority.Choice;

public class InstitutionsRestAuthorityProvider extends RestAuthorityProvider {

    private final String CHOICES_FATHER_AUTH_PREFIX;
    private final String CHOICES_FATHER_AUTH_VALUE_PREFIX;
    private final String FATHER_AUTH_FIELD;
    private final String FATHER_AUTH_VALUE_FIELD;


    public InstitutionsRestAuthorityProvider() {
        super();
        this.CHOICES_FATHER_AUTH_PREFIX = "choices.fatherAuth.";
        this.CHOICES_FATHER_AUTH_VALUE_PREFIX = "choices.fatherAuthValue.";
        this.FATHER_AUTH_FIELD = "father_id";
        this.FATHER_AUTH_VALUE_FIELD = "";
    }

    /**
    *
    * @param field metadata field responsible for the query
    * @return the attribute containing the father_id by which we will do exact match
    *         filtering
    */
   protected final String getFatherAuthField() {
       String metadataField = this.field;
       // Gets the value from conf file if set, else uses default value
       String fatherAuthField = configurationService.getProperty(CHOICES_FATHER_AUTH_PREFIX + metadataField, this.FATHER_AUTH_FIELD);
       return fatherAuthField;
   };

   /**
    * @param field metadata field responsible for the query
    * @return the attribute containing the father_id_value by which we will do exact match
    *         filtering
    */
   protected final String getFatherAuthValueField() {
       String metadataField = this.field;
       // Gets the value from conf file if set, else uses default value
       String fatherAuthValueField = configurationService.getProperty(CHOICES_FATHER_AUTH_VALUE_PREFIX + metadataField, this.FATHER_AUTH_VALUE_FIELD);
       return fatherAuthValueField;
   };

   @Override
   protected void addExtraQueryTextParams(Map<String, String> params) {
       String fatherAuthField = getFatherAuthField();
       String filter = getFatherAuthValueField();
       params.put(fatherAuthField, filter);
   }

    @Override
    protected Choice extractChoice(Map<String, Object> singleResult, boolean searchById) {
        String value = singleResult.get(this.getFilterField()).toString();
        String label = value;
        // If searching by id (in example, if indexing using Discovery, then don't show acronym)...
        if (!searchById) {
            if (singleResult.containsKey("acronym") && !singleResult.get("acronym").toString().isEmpty()) {
                label += " (" + singleResult.get("acronym")  + ")";
            }
        }
        return new Choice(singleResult.get(this.getIdField()).toString(), value, label);
    }

}
