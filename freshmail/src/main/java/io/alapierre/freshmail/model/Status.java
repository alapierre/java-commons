package io.alapierre.freshmail.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 13.06.18
 */
@Data
@NoArgsConstructor
public class Status {

    public Status(boolean status) {
        this.status = status ? "OK" : "Error";
    }

    private String status;
    private String data;
}
