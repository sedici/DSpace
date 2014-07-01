/**
 * Copyright (C) 2011 SeDiCI <info@sedici.unlp.edu.ar>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ar.edu.unlp.sedici.dspace.xoai.filters;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.xoai.data.DSpaceDatabaseItem;
import org.dspace.xoai.data.DSpaceItem;
import org.dspace.xoai.exceptions.InvalidMetadataFieldException;
import org.dspace.xoai.filter.DSpaceFilter;
import org.dspace.xoai.filter.DatabaseFilterResult;
import org.dspace.xoai.filter.SolrFilterResult;
import org.dspace.xoai.util.MetadataFieldManager;

/**
 * 
 * @author Lyncode Development Team <dspace@lyncode.com>
 */
public class DSpaceAnyMetadataExistsFilter extends DSpaceFilter
{
    private static Logger log = LogManager
            .getLogger(DSpaceAnyMetadataExistsFilter.class);

    private List<String> _fields;

    private List<String> getFields()
    {
        if (this._fields == null)
        {
            _fields = super.getParameters("fields");
        }
        return _fields;
    }

    @Override
    public DatabaseFilterResult getWhere(Context context)
    {
        try
        {
        	String where = "(";
        	List<String> fields = this.getFields();
        	List<Object> args = new ArrayList<Object>(fields.size());
        	for (int i = 0; i < fields.size(); i++) {
				where += "EXISTS (SELECT tmp.* FROM metadatavalue tmp WHERE tmp.item_id=i.item_id AND tmp.metadata_field_id=?)";
				if (i < fields.size() -1 )
					where += " OR " ;
        		args.add(MetadataFieldManager.getFieldID(context, fields.get(i)));
			}
        	where += ")";
        	
        	return new DatabaseFilterResult(where, args);
        }
        catch (InvalidMetadataFieldException e)
        {
            log.error(e.getMessage(), e);
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
        return new DatabaseFilterResult();
    }

    @Override
    public boolean isShown(DSpaceItem item)
    {
    	for (String field : this.getFields()) {
				
	        if (item.getMetadata(field).size() > 0)
	            return true;

		}
        return false;
    }

    @Override
    public SolrFilterResult getQuery()
    {
    	String cond = "(";
    	List<String> fields = this.getFields();
    	for (int i = 0; i < fields.size(); i++) {
    		cond += "metadata." + fields.get(i) + ":[* TO *]";
			if (i < fields.size() -1 )
				cond += " OR " ;
		}
    	cond += ")";
    	
    	return new SolrFilterResult(cond);
    }

}
