package io.alapierre.jdbc.api;

import io.alapierre.jdbc.model.ColumnMetadata;
import io.alapierre.jdbc.model.TableMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2021.12.13
 */
@RequiredArgsConstructor
@Slf4j
public class JdbcMetadataExplorer {

    private final DataSource dataSource;

    @SuppressWarnings("unused")
    public Set<TableMetadata> processTablesMetadata(@NotNull String schemaPattern) throws SQLException {

        try (Connection connection = dataSource.getConnection()) {

            val meta = connection.getMetaData();
            val res = new LinkedHashSet<TableMetadata>();

            try (val rs = meta.getTables(null, schemaPattern, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    val catalog = rs.getString("TABLE_CAT");
                    val schema = rs.getString("TABLE_SCHEM");
                    val tableName = rs.getString("TABLE_NAME");
                    val foreignKeys = meta.getImportedKeys(catalog, schema, tableName);
                    val relatedTables = new LinkedHashSet<String>();

                    while (foreignKeys.next()) {
                        val fkTableName = foreignKeys.getString("PKTABLE_NAME");
                        if (!tableName.equals(fkTableName)) {
                            relatedTables.add(fkTableName);
                        }
                    }
                    res.add(new TableMetadata(tableName, schema, catalog, relatedTables));
                }
            }
            return res;
        }
    }

    @SuppressWarnings("unused")
    public Set<ColumnMetadata> procesTableColumnsMetadata(@NotNull String tableName) throws SQLException {

        val result = new LinkedHashSet<ColumnMetadata>();

        try (Connection connection = dataSource.getConnection()) {
            val meta = connection.getMetaData();

            try (val rs = meta.getColumns(null, null, tableName, "%")) {
                while (rs.next()) {
                    result.add(procesColumnMetadata(rs));
                }
            }
        }

        return result;
    }

    @Contract("_ -> new")
    private @NotNull ColumnMetadata procesColumnMetadata(@NotNull ResultSet rs) throws SQLException {
        // See: https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html#getColumns-java.lang.String-java.lang.String-java.lang.String-java.lang.String-
        return new ColumnMetadata(
                rs.getString("COLUMN_NAME"),
                rs.getString("TABLE_NAME"),
                rs.getInt("ORDINAL_POSITION"),
                rs.getInt("DATA_TYPE"),
                rs.getString("TYPE_NAME"),
                getValue(rs, "COLUMN_SIZE", Integer.class),
                "YES".equals(rs.getString("IS_NULLABLE")),
                "YES".equals(rs.getString("IS_AUTOINCREMENT")),
                getValue(rs, "NUMERIC_SCALE", Integer.class),
                getValue(rs, "DECIMAL_DIGITS", Integer.class));
    }

    private <T> @Nullable T getValue(@NotNull ResultSet rs, @NotNull String name, @SuppressWarnings("SameParameterValue") @NotNull Class<T> clazz) {
        try {
            return rs.getObject(name, clazz);
        } catch (SQLException ex) {
            log.debug("can't get value for column {} cause by {}", name, ex.getMessage());
            return null;
        }
    }

}
