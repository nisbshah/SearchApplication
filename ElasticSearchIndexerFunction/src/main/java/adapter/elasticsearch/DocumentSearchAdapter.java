package adapter.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import adapter.elasticsearch.adapter.elasticsearch.dto.PlanDetails;
import aws.helpers.AWSRequestSigningApacheInterceptor;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DocumentSearchAdapter {

  private static final String SERVICE = "es";
  private static final String REGION = "us-west-2";
  private static final String aesEndpoint =
      "https://search-manual-v7e5z2ptfxutqy2rvte2s7qfnm.us-west-2.es.amazonaws.com";
  private static final String INDEX = "plans";
  private static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

  private ObjectMapper objectMapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public List<PlanDetails> searchByPlan(String planName) throws Exception {

    RestHighLevelClient client = getEsClient(SERVICE, REGION);

    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("PLAN_NAME", planName));
    sourceBuilder.query(queryBuilder);
    sourceBuilder.size(5);

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.indices(INDEX);
    searchRequest.source(sourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    if (searchResponse == null || searchResponse.getHits() == null || searchResponse.getHits().getHits() == null)
      return null;

    return getSearchResult(searchResponse);
  }

  public List<PlanDetails> searchBySponsorName(String sponsorName) throws Exception {

    RestHighLevelClient client = getEsClient(SERVICE, REGION);

    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    QueryBuilder queryBuilder =
        QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("SPONSOR_DFE_NAME", sponsorName));
    sourceBuilder.query(queryBuilder);
    sourceBuilder.size(5);

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.indices(INDEX);
    searchRequest.source(sourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    if (searchResponse == null || searchResponse.getHits() == null || searchResponse.getHits().getHits() == null)
      return null;

    return getSearchResult(searchResponse);
  }

  public List<PlanDetails> searchByState(String stateCode) throws Exception {

    RestHighLevelClient client = getEsClient(SERVICE, REGION);

    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(QueryBuilders.termQuery("SPONS_DFE_MAIL_US_STATE", stateCode));

    sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.indices(INDEX);
    searchRequest.source(sourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    if (searchResponse == null || searchResponse.getHits() == null || searchResponse.getHits().getHits() == null)
      return null;

    return getSearchResult(searchResponse);
  }

  private RestHighLevelClient getEsClient(String serviceName, String region) {
    AWS4Signer signer = new AWS4Signer();
    signer.setServiceName(serviceName);
    signer.setRegionName(region);

    HttpRequestInterceptor interceptor =
        new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
    return new RestHighLevelClient(RestClient.builder(HttpHost.create(aesEndpoint))
        .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
  }

  private List<PlanDetails> getSearchResult(SearchResponse response) {

    SearchHit[] searchHit = response.getHits().getHits();
    List<PlanDetails> PlanDetailss = new ArrayList<>();
    if (searchHit.length > 0) {

      Arrays.stream(searchHit)
          .forEach(hit -> PlanDetailss.add(objectMapper.convertValue(hit.getSourceAsMap(), PlanDetails.class)));
    }

    return PlanDetailss;
  }
}
