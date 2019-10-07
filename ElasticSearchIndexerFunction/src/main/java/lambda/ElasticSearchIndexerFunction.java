package lambda;

import org.slf4j.MDC;
import java.util.List;
import java.util.Map;
import adapter.elasticsearch.DocumentsLoaderAdapter;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.StringUtils;
import utilities.ParserUtils;

public class ElasticSearchIndexerFunction implements RequestHandler<S3Event, String> {

  //private static final Logger logger = LoggerFactory.getLogger(ElasticSearchIndexerFunction.class);
  private final AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

  /*
    1. Read file from S3
    2. Read parts of the files and convert it to JSON
    3. Send it to ES
    REPEAT

   */

  @Override
  public String handleRequest(S3Event event, Context context) {
    System.out.println("Received event: " + event);
    String esEndpoint = System.getenv("ES_ENDPOINT");
    System.out.println("esEndpoint: " + esEndpoint);
    if (StringUtils.isNullOrEmpty(esEndpoint)) {
      throw new IllegalArgumentException("Bad setup");
    }
    MDC.put("esEndpoint", esEndpoint);

    int totalDocsCount = 0;

    try {
      String bucket = event.getRecords().get(0).getS3().getBucket().getName();
      String fileName = event.getRecords().get(0).getS3().getObject().getKey();
      S3Object s3Object = s3.getObject(new GetObjectRequest(bucket, fileName));
      System.out.println("filenName: " + fileName);

      List<Map<String, String>> jsonStringMapList = ParserUtils.getJsonStringMap(s3Object);
      if (jsonStringMapList == null || jsonStringMapList.isEmpty())
        throw new IllegalStateException("Failed to convert the input CSV to list of JSON");
      System.out.println("Size of the list of maps: " + jsonStringMapList.size());

      for (Map<String, String> jsonMap : jsonStringMapList) {
        DocumentsLoaderAdapter.sendDocsToES(jsonMap);
        totalDocsCount += jsonMap.size();
        System.out.println("Total Items loaded to ES until now: " + totalDocsCount);
      }
    } catch (Exception ex) {
      System.out.println(ex);
      return "ERROR";
    }
    return "SUCCESS";
  }

}
