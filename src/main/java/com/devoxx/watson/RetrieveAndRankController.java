package com.devoxx.watson;

import com.devoxx.watson.model.RetrieveAndRankDocument;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

  List<RetrieveAndRankDocument> call(final String question) {

    final String login = askDevoxxProperties.getRetrieveUsername() + ":" + askDevoxxProperties.getRetrievePassword();
    final String base64login = new String(Base64.encodeBase64(login.getBytes()));

    List<RetrieveAndRankDocument> retrieveAndRankDocumentList = new ArrayList<>();

    try {

      // "start=0&rows=2" = start at row 0 and only return 2 results
      String url = RETRIEVE_AND_RANK_URL +
          askDevoxxProperties.getRetrieveClusterName() + "/solr/" +
          askDevoxxProperties.getRetrieveCollectionName()+
          "/select?q=" + URLEncoder.encode(question, "UTF-8") + "&wt=json&fl=id,title,body,searchText,score&start=0&rows=30";

      final Document doc =
          Jsoup.connect(url)
              .timeout(10_000)
              .header("Authorization", "Basic " + base64login)
              .method(Connection.Method.GET)
              .data("outputMode", "json")
              .ignoreContentType(true)
              .execute()
              .parse();

      final JsonObject entireJsonObject = new JsonParser().parse(doc.text()).getAsJsonObject();
      if (entireJsonObject.has("response")) {
        JsonObject responseJsonObject = entireJsonObject.getAsJsonObject("response");

        if (responseJsonObject.has("docs")) {
          final JsonArray docsJsonArray = responseJsonObject.get("docs").getAsJsonArray();
          Iterator iterator = docsJsonArray.iterator();

          while (iterator.hasNext()) {
            JsonObject docJsonObject = (JsonObject) iterator.next();
            RetrieveAndRankDocument retrieveAndRankDocument = new RetrieveAndRankDocument();

            // Get the id
            if (docJsonObject.has("id")) {
              retrieveAndRankDocument.setId(docJsonObject.get("id").toString());
            }

            // Get the score
            if (docJsonObject.has("score")) {
              retrieveAndRankDocument.setScore(docJsonObject.get("score").toString());
            }

            // Get the title
            if (docJsonObject.has("title")) {
              retrieveAndRankDocument.setTitle(docJsonObject.get("title").toString());
            }

            // Get the body
            if (docJsonObject.has("body")) {
              retrieveAndRankDocument.setBody(docJsonObject.get("body").toString());
            }

            // Get the document name (this is the third element in the searchText JSON element)
            if (docJsonObject.has("searchText")) {
              final JsonArray searchTextArray = docJsonObject.get("searchText").getAsJsonArray();
              if (searchTextArray.size() >= 3) {
                retrieveAndRankDocument.setDocName(searchTextArray.get(2).toString());
              }
            }

            retrieveAndRankDocumentList.add(retrieveAndRankDocument);
          }
        }

      }

      return retrieveAndRankDocumentList;


    } catch (IOException e) {
      e.printStackTrace();
    }

    throw new IllegalArgumentException("No response found");
  }
}
