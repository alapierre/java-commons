package io.alapierre.freshmail;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import io.alapierre.freshmail.model.AddSubscribersRequest;
import io.alapierre.freshmail.model.Subscriber;
import io.alapierre.freshmail.model.SubscriberStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 13.06.18
 */
@Slf4j
public class FreshMailClient {

    private final String apiKey;
    private final String apiSecret;
    private final FreshMailRest freshMail;

    public FreshMailClient(String apiKey, String apiSecret, String url) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;

        freshMail = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .requestInterceptor(new AuthInterceptor(apiKey, apiSecret))
                .target(FreshMailRest.class, url);
    }

    public FreshMailClient(String apiKey, String apiSecret) {
        this(apiKey, apiSecret, "https://api.freshmail.com");
    }

    public FreshMailRest getRestClient() {
        return freshMail;
    }

    public void ping() {
        freshMail.ping();
    }

    public void deleteSubscriber(String email, String list) {
        freshMail.subscriberDelete(Subscriber.builder()
                .email(email)
                .list(list)
                .build());
    }

    public void addSubscriber(String listHash, String name, String email) {
        freshMail.subscriberAdd(prepareSubscriber(listHash, name, email, SubscriberStatus.TO_ACTIVATE));
    }

    public void addSubscriber(String listHash, String name, String email, SubscriberStatus status) {
        freshMail.subscriberAdd(prepareSubscriber(listHash, name, email, status));
    }

    public void addSubscriberMultiple(String listHash, SubscriberStatus status, boolean confirm, List<Subscriber> subscribers) {

        makePages(subscribers, 100).forEach(it -> freshMail.subscriberAddMultiple(
                AddSubscribersRequest.builder()
                .confirm(confirm ? 1 : 0)
                .list(listHash)
                .state(status.getCode())
                .subscribers(it)
                .build()));
    }

    public void addSubscriberMultiple(String listHash, SubscriberStatus status, boolean confirm, Subscriber... subscribers) {
        addSubscriberMultiple(listHash, status, confirm, Arrays.asList(subscribers));
    }


    private Subscriber prepareSubscriber(String listHash, String name, String email, SubscriberStatus status) {

        Map<String, String> fields = new HashMap<>();
        fields.put("imie", name);

        return Subscriber
                .builder()
                .list(listHash)
                .email(email)
                .state(status.getCode())
                .customFields(fields)
                .confirm(1)
                .build();
    }

    private <T> Stream<List<T>> makePages(List<T> list, int pageSize) {
        return IntStream.range(0, (list.size() + pageSize - 1) / pageSize)
                .mapToObj(i -> list.subList(i * pageSize, Math.min(pageSize * (i + 1), list.size())));
    }

}
