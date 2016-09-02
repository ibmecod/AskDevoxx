package com.devoxx.watson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author Stephan Janssen
 */
@Component
public class RetrieveAndRankController {

    private static final String RETRIEVE_AND_RANK_URL = "https://gateway.watsonplatform.net/retrieve-and-rank/api/v1/solr_clusters/";

    private final AskDevoxxProperties askDevoxxProperties;

    @Autowired
    public RetrieveAndRankController(AskDevoxxProperties askDevoxxProperties) {
        this.askDevoxxProperties = askDevoxxProperties;
    }

    JsonObject call(final String question) {

        final String login = askDevoxxProperties.getRetrieveUsername() + ":" + askDevoxxProperties.getRetrievePassword();
        final String base64login = new String(Base64.encodeBase64(login.getBytes()));

        try {

            // "start=0&rows=2" = start at row 0 and only return 2 results
            String url = RETRIEVE_AND_RANK_URL +
                         askDevoxxProperties.getRetrieveClusterName() + "/solr/" +
                         askDevoxxProperties.getRetrieveCollectionName()+
                         "/select?q=" + URLEncoder.encode(question, "UTF-8") + "&wt=json&fl=score,searchText&start=0&rows=2";

            final Document doc =
                    Jsoup.connect(url)
                            .timeout(10_000)
                            .header("Authorization", "Basic " + base64login)
                            .method(Connection.Method.GET)
                            .data("outputMode", "json")
                            .data("extract", "authors, doc-emotion, pub-date, doc-sentiment, title")
                            .ignoreContentType(true)
                            .execute()
                            .parse();

            return new JsonParser().parse(doc.text()).getAsJsonObject();

        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new IllegalArgumentException("No response found");
    }
}
