package io.alapierre.freshmail;


import feign.Headers;
import feign.RequestLine;
import io.alapierre.freshmail.model.AddSubscribersRequest;
import io.alapierre.freshmail.model.Status;
import io.alapierre.freshmail.model.Subscriber;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 12.06.18
 */
public interface FreshMailRest {

    @RequestLine("GET /rest/ping")
    @Headers({"Content-Type: application/json"})
    Status ping();

    @RequestLine("POST /rest/subscriber/add")
    @Headers({"Content-Type: application/json"})
    Status subscriberAdd(Subscriber subscriber);

    @RequestLine("POST /rest/subscriber/addMultiple")
    @Headers({"Content-Type: application/json"})
    String subscriberAddMultiple(AddSubscribersRequest subscriber);

    @RequestLine("POST /rest/subscriber/delete")
    @Headers({"Content-Type: application/json"})
    String subscriberDelete(Subscriber subscriber);
}
