package io.alapierre.freshmail;

import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import io.alapierre.freshmail.model.Subscriber;
import io.alapierre.freshmail.model.SubscriberStatus;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 13.06.18
 */
@Slf4j
public class FreshMailClientTest {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

    private FreshMailClient freshMail = new FreshMailClient("", "", driver.getBaseUrl());

    @Test
    public void ping() {

        driver.addExpectation(
                onRequestTo("/rest/ping")
                        .withMethod(ClientDriverRequest.Method.GET),
                giveResponse("true", "text/plain"));


        freshMail.ping();
    }

    @Test
    public void addSubscriber() throws Exception{

        String content = readFile("src/test/resources/addSubscriber.json");
        log.debug(content);

        driver.addExpectation(
                onRequestTo("/rest/subscriber/add")
                        .withMethod(ClientDriverRequest.Method.POST)
                        .withBody(content, "application/json")
                        .withHeader("X-Rest-ApiKey", ""),
                giveResponse("true", "text/plain"));

        freshMail.addSubscriber("7lpa83ez6d", "Adrian Lapierre", "al@soft-project.pl");
    }

    String readFile(String file) throws IOException {
        return new String (Files.readAllBytes(Paths.get(file)),Charset.forName("UTF-8"));
    }

    @Test
    public void addSubscribers() throws Exception {

        String content = readFile("src/test/resources/addMultiple.json");

        driver.addExpectation(
                onRequestTo("/rest/subscriber/addMultiple")
                        .withMethod(ClientDriverRequest.Method.POST)
                        .withBody(content, "application/json")
                        .withHeader("X-Rest-ApiKey", ""),
                giveResponse("true", "text/plain"));

        freshMail.addSubscriberMultiple("7lpa83ez6d", SubscriberStatus.TO_ACTIVATE, true,
                Subscriber.builder()
                        .email("al@soft-project.pl")
                        .customField("imie", "Adrian Lapierre")
                        .build(),
                Subscriber.builder()
                        .email("adrian.lapierre@sidgroup.pl")
                        .customField("imie", "Jan Kowalski")
                        .build());

    }

}