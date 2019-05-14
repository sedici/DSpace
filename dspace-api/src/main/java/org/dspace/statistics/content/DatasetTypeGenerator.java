/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics.content;


/**
 * Represents a simple string facet for filtering.
 * Doesn't offer any special interaction.
 *
 * @author kevinvandevelde at atmire.com
 * Date: 23-dec-2008
 * Time: 12:44:27
 * 
 */
public class DatasetTypeGenerator extends DatasetGenerator {

    /** The type of our generator (EXAMPLE: country) **/
    private String type;
    /** The number of values shown (max) **/
    private int max;
    /** The type of dso to search (item, bitstream, etc)
     *  -1 means no type
     **/
    protected int searchDsoType = -1;


    public DatasetTypeGenerator() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getSearchDsoType() {
		return searchDsoType;
    }

    public void setSearchDsoType(int searchDsoType) {
		this.searchDsoType = searchDsoType;
    }
}
