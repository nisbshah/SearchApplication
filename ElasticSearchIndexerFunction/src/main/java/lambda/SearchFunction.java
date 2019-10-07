/*
 * Copyright (c) 2019 Financial Engines, Inc.  All Rights Reserved.
 * Sunnyvale, CA
 *
 * File: SearchFunction.java
 * Author: nishah
 */

package lambda;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import adapter.elasticsearch.adapter.elasticsearch.dto.PlanDetails;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import service.search.SearchService;

public class SearchFunction implements RequestStreamHandler {

  private ObjectMapper objectMapper = new ObjectMapper();

  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
      throws IOException {

    JSONParser parser = new JSONParser();
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    JSONObject responseJson = new JSONObject();
    String responseBody = null;

    try {
      JSONObject event = (JSONObject) parser.parse(reader);
      if (event.get("queryStringParameters") == null) {
        throw new IllegalArgumentException("Bad request");
      }

      JSONObject qps = (JSONObject) event.get("queryStringParameters");
      if (qps.get("key") == null || qps.get("value") == null) {
        throw new IllegalArgumentException("Bad request");
      }

      String searchKey = (String) qps.get("key");
      String searchValue = (String) qps.get("value");

      SearchService searchService = new SearchService();
      List<PlanDetails> planDetailsList = searchService.getPlanDetails(searchKey, searchValue);
      responseBody = objectMapper.writeValueAsString(planDetailsList);


      //JSONObject headerJson = new JSONObject();
      //headerJson.put("x-custom-header", "my custom header value");

      responseJson.put("statusCode", 200);
      //responseJson.put("headers", headerJson);
      responseJson.put("body", responseBody);

    } catch (Exception ex) {
      responseJson.put("statusCode", 400);
      responseJson.put("exception", ex);
    }

    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
    writer.write(responseJson.toString());
    writer.close();
  }
}
