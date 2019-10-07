package lambda;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.MDC;
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
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import service.search.SearchService;

public class SearchFunction implements RequestStreamHandler {

  private ObjectMapper objectMapper = new ObjectMapper();

  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
      throws IOException {

    JSONParser parser = new JSONParser();
    JSONObject responseJson = new JSONObject();
    String responseBody = null;

    try {
      String esEndpoint = System.getenv("ES_ENDPOINT");
      if (StringUtils.isNullOrEmpty(esEndpoint)) {
        throw new IllegalArgumentException("Bad setup");
      }
      MDC.put("esEndpoint", esEndpoint);

      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      JSONObject event = (JSONObject) parser.parse(reader);
      if (event.get("queryStringParameters") == null) {
        throw new IllegalArgumentException("Bad request");
      }

      JSONObject queryStringParameters = (JSONObject) event.get("queryStringParameters");
      if (queryStringParameters.get("key") == null || queryStringParameters.get("value") == null) {
        throw new IllegalArgumentException("Bad request");
      }

      String searchKey = (String) queryStringParameters.get("key");
      String searchValue = (String) queryStringParameters.get("value");

      SearchService searchService = new SearchService();
      List<PlanDetails> planDetailsList = searchService.getPlanDetails(searchKey, searchValue);
      responseBody = objectMapper.writeValueAsString(planDetailsList);

      responseJson.put("statusCode", 200);
      responseJson.put("body", responseBody);

    } catch (Exception ex) {
      responseJson.put("statusCode", 400);
      responseJson.put("body", ex.getMessage());
    } finally {
      OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
      writer.write(responseJson.toString());
      writer.close();
    }
  }
}
