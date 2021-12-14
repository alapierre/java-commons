package io.alapierre.jdbc.model;

import lombok.Value;
import lombok.val;

import java.util.Optional;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2021.12.13
 */
@Value
public class ColumnMetadata {

    String name;
    String tableName;
    int ordinalPosition;
    int dataType;
    String internalDataTypeName;
    Integer size;
    boolean nullable;
    boolean autoincrement;
    Integer scale;
    Integer precision;

}
