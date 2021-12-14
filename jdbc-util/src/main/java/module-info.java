/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2021.12.13
 */
module io.alapierre.jdbc {

 requires java.sql;
 requires lombok;
 requires java.naming;
 requires org.slf4j;
 requires transitive org.jetbrains.annotations;
 opens io.alapierre.jdbc.model;
 exports io.alapierre.jdbc.api;
 exports io.alapierre.jdbc.model;
}
