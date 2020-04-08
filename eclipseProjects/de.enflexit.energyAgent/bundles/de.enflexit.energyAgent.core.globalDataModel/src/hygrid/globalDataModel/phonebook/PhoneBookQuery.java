package hygrid.globalDataModel.phonebook;

import java.io.Serializable;

/**
 * This class specifies a phone book query
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class PhoneBookQuery implements Serializable{
	private static final long serialVersionUID = -5187447948503933057L;

	/**
	 * Possible types of queries
	 */
	public enum QueryType{
		AGENT_BY_LOCAL_NAME, AGENTS_BY_COMPONENT_TYPE, CONTROLLABLE_COMPONENTS 
	}
	
	private QueryType queryType;
	private String searchString;

	/**
	 * Gets the query type.
	 * @return the query type
	 */
	public QueryType getQueryType() {
		return queryType;
	}

	/**
	 * Sets the query type.
	 * @param queryType the new query type
	 */
	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	/**
	 * Gets the search string.
	 * @return the search string
	 */
	public String getSearchString() {
		return searchString;
	}

	/**
	 * Sets the search string.
	 * @param searchString the new search string
	 */
	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}
	
}
