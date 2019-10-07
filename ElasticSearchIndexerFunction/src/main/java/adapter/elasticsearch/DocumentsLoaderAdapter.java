package adapter.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.MDC;
import java.io.IOException;
import java.util.Map;
import aws.helpers.AWSRequestSigningApacheInterceptor;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

public class DocumentsLoaderAdapter {
  private static final String SERVICE = "es";
  private static final String REGION = "us-west-2";
  /*private static final String aesEndpoint =
      "https://search-manual-v7e5z2ptfxutqy2rvte2s7qfnm.us-west-2.es.amazonaws.com";*/
  private static String aesEndpoint;
  private static final String INDEX = "plans";
  private static final String TYPE = "doc";
  private static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();


  public static void sendDocsToES(Map<String, String> jsonStringMap) throws Exception {

    aesEndpoint = "https://" + MDC.get("esEndpoint");
    RestHighLevelClient esClient = getEsClient(SERVICE, REGION);
    ensureIndexExist(esClient);
    esClient.close();

    int count = 0;
    int batch = 150;
    //int batch = 2;

    BulkRequest bulkRequest = new BulkRequest();

    for (Map.Entry<String, String> jsonStringEntry : jsonStringMap.entrySet()) {

      IndexRequest indexRequest = new IndexRequest(INDEX, TYPE, jsonStringEntry.getKey()).
          source(jsonStringEntry.getValue(), XContentType.JSON);
      bulkRequest.add(indexRequest);
      count++;

      if (count == batch) {
        esClient = getEsClient(SERVICE, REGION);
        BulkResponse response = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        count = 0;
        if (response.hasFailures()) {
          System.out.println("response from ES hasFailures - " + response.buildFailureMessage());
        }
        bulkRequest = new BulkRequest();
        esClient.close();
      }
    }

    if (bulkRequest.estimatedSizeInBytes() != 0) {
      esClient = getEsClient(SERVICE, REGION);
      BulkResponse response = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
      esClient.close();

      if (response.hasFailures()) {
        System.out.println("Last in batch, response from ES hasFailures - " + response.buildFailureMessage());
      }
    }

  }

  private static void ensureIndexExist(RestHighLevelClient esClient) throws IOException {
    GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX);
    boolean exists = esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

    if (exists == false) {
      CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX);
      CreateIndexResponse indexResponse = esClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
      System.out.println("Created new index - " + indexResponse.index());
    }
  }

  private static RestHighLevelClient getEsClient(String serviceName, String region) {
    AWS4Signer signer = new AWS4Signer();
    signer.setServiceName(serviceName);
    signer.setRegionName(region);

    HttpRequestInterceptor interceptor =
        new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
    return new RestHighLevelClient(RestClient.builder(HttpHost.create(aesEndpoint))
        .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
  }
}
