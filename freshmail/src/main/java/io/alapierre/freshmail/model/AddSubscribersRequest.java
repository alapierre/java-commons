package io.alapierre.freshmail.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 13.06.18
 */
@Data
@Builder
public class AddSubscribersRequest {

    @JsonProperty("list")
    public String list;

    public int state;

    public int confirm;

    @JsonProperty("subscribers")
    @Singular
    public List<Subscriber> subscribers;
}
