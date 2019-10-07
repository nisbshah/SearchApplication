package adapter.elasticsearch.adapter.elasticsearch.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public class PlanDetails {

  private String ACK_ID;
  private String PLAN_NAME;
  private String SPONSOR_DFE_NAME;
  private String SPONS_DFE_MAIL_US_STATE;

  // Getter Methods
  public String getACK_ID() {
    return ACK_ID;
  }

  public String getPLAN_NAME() {
    return PLAN_NAME;
  }

  public String getSPONSOR_DFE_NAME() {
    return SPONSOR_DFE_NAME;
  }

  public String getSPONS_DFE_MAIL_US_STATE() {
    return SPONS_DFE_MAIL_US_STATE;
  }

  // Setter Methods
  @JsonProperty("ACK_ID")
  public void setACK_ID(String ACK_ID) {
    this.ACK_ID = ACK_ID;
  }

  @JsonProperty("PLAN_NAME")
  public void setPLAN_NAME(String PLAN_NAME) {
    this.PLAN_NAME = PLAN_NAME;
  }

  @JsonProperty("SPONSOR_DFE_NAME")
  public void setSPONSOR_DFE_NAME(String SPONSOR_DFE_NAME) {
    this.SPONSOR_DFE_NAME = SPONSOR_DFE_NAME;
  }

  @JsonProperty("SPONS_DFE_MAIL_US_STATE")
  public void setSPONS_DFE_MAIL_US_STATE(String SPONS_DFE_MAIL_US_STATE) {
    this.SPONS_DFE_MAIL_US_STATE = SPONS_DFE_MAIL_US_STATE;
  }

}