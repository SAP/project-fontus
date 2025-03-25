package com.sap.fontus.sql.tainter;

/**
 * The different parts in a query where a parameter might occur
 */
public enum ParameterType {
    /**
     * Inside an Insert Statement or an update assignment
     * Insert into a values(?);
     *                      ^
     * Update a set x = ?;
     *                  ^
     * <p>
     * Handling:
     * Taint column follows directly (idx+1)
     */
    ASSIGNMENT,
    /**
     * Inside a regular where clause
     * select * from a where id = ?;
     *                            ^
     * <p>
     * Handling:
     * No taint column
     */
    WHERE,
    /**
     * Inside a where clause in a subselect
     * select * from a where id = (select id from b where x = ?);
     *                                                        ^
     * <p>
     * Handling:
     * No taint column
     */
    SUBSELECT_WHERE,
    /**
     * A subselect adding a new column to a select statement
     * select x, (select y from b where y = ?) as bla from a
     *                                      ^
     * Handling:
     * No taint column
     */
    QUERY_SUBSELECT,
    /**
     * A parameter assignment where the value is retrieved via a subselect
     * INSERT INTO a VALUES ('a', (select y from b where y = ?))
     *                                                       ^
     * <p>
     * Handling:
     * We need to duplicate the subselect, one for the insert of the regular value and one for the tainted value
     * The tainted value thus can move further to the back, as the select might have several parameters in its where clause
     */
    ASSIGNMENT_SUBSELECT,

    ASSIGNMENT_UNTAINTED
}
