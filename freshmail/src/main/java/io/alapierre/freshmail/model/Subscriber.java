package io.alapierre.freshmail.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Map;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 12.06.18
 */
@Data
@Builder
public class Subscriber {

    private String email;
    private String list;
    private Integer state;
    private Integer confirm;

    @JsonProperty("custom_fields")
    @Singular
    private Map<String, String> customFields;
}
