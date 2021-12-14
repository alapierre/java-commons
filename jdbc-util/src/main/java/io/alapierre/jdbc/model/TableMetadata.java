package io.alapierre.jdbc.model;

import lombok.Value;

import java.util.Set;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2021.12.13
 */
@Value
public class TableMetadata {

    String name;
    String schema;
    String catalog;
    Set<String> relatedTables;

}
