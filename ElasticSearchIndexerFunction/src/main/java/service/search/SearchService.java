package service.search;

import java.util.List;
import adapter.elasticsearch.DocumentSearchAdapter;
import adapter.elasticsearch.adapter.elasticsearch.dto.PlanDetails;
import com.amazonaws.util.StringUtils;

public class SearchService {

  public List<PlanDetails> getPlanDetails(String searchKey, String searchValue) throws Exception {
    if (StringUtils.isNullOrEmpty(searchKey) || StringUtils.isNullOrEmpty(searchValue)) {
      return null;
    }

    DocumentSearchAdapter documentSearchAdapter = new DocumentSearchAdapter();
    if (searchKey.toLowerCase().contains("plan")) {
      return documentSearchAdapter.searchByPlan(searchValue);
    }

    if (searchKey.toLowerCase().contains("sponsor")) {
      return documentSearchAdapter.searchBySponsorName(searchValue);
    }

    if (searchKey.toLowerCase().contains("state")) {
      return documentSearchAdapter.searchByState(searchValue);
    }

    throw new IllegalArgumentException("Unsupported search key");
  }
}
