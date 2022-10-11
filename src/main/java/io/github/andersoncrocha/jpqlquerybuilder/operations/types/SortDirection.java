package io.github.andersoncrocha.jpqlquerybuilder.operations.types;

public enum SortDirection {

    ASC,
    DESC;

    public static SortDirection isAscending(boolean isAscending) {
        return isAscending ? ASC : DESC;
    }

}
