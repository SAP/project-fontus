package com.sap.fontus.sql.tainter;

enum WhereExpressionKind {
    REGULAR,
    QUERY_SUBSELECT_WHERE,
    IN_SUBSELECT_WHERE,
    IN_ASSIGNMENT_SUBSELECT
}
