package io.alapierre.gpg;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 21.06.18
 */
@Slf4j
public class Hkp {

    private String host;
    private int port;

    public Hkp(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public void publish(String publicKey) throws IOException {

        URL url = new URL("http", host, port, "/pks/add");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        String urlParameters = "keytext=" + URLEncoder.encode(publicKey, "UTF-8");

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        try {
            int responseCode = con.getResponseCode();
            System.out.println("response code " + responseCode);
        } catch (Exception ignore) {

        }

        log.debug(readResponce(con));
    }

    private String readResponce(HttpURLConnection con) throws IOException {

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();


        return response.toString();
    }

}
