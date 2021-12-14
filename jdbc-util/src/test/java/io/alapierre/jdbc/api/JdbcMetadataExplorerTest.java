package io.alapierre.jdbc.api;

import io.alapierre.jdbc.model.TableMetadata;
import lombok.val;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2021.12.13
 */
class JdbcMetadataExplorerTest {

    private static DataSource dataSource;

    @BeforeAll
    static void init() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;INIT=RUNSCRIPT FROM 'src/test/resources/create.sql'");
        dataSource = ds;
    }

    @Test
    void processTablesMetadata() throws SQLException {

        JdbcMetadataExplorer explorer = new JdbcMetadataExplorer(dataSource);

        Set<TableMetadata> tables = explorer.processTablesMetadata("%");

        tables.forEach(System.out::println);

    }

    @Test
    void procesTableColumnsMetadata() throws SQLException {

        JdbcMetadataExplorer explorer = new JdbcMetadataExplorer(dataSource);
        val columns = explorer.procesTableColumnsMetadata("MY_TABLE");

        columns.forEach(System.out::println);
    }
}
