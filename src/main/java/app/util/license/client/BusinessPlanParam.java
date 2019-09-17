package app.util.license.client;

import de.schlichtherle.license.LicenseContentException;

public class BusinessPlanParam extends VersionizedParam {

  private static final long serialVersionUID = 7207306376659720325L;
  private static final String EXC_PLAN_TYPE_INVALID = "";
  private static final String EXC_ACCT_LIMIT_POSITIVE = "";

  public static final String TRIVAL = "Trival";     //试用版
  public static final String PERSONAL = "Personal"; //个人版
  public static final String PRO = "Pro";           //专业版
  public static final String ENTERPRISE = "Enterprise"; //企业版

  private String plan;
  private Long acctLimit;

  public BusinessPlanParam() {
    this.plan = TRIVAL;
  }

  public BusinessPlanParam(String version, String plan) {
    setVersion(version);
    this.plan = plan;
  }

  public void setPlan(String plan) {
    this.plan = plan;
  }
  
  public String getPlan() {
    return this.plan;
  }

  public void setAcctLimit(Long acctLimit) {
    this.acctLimit = acctLimit;
  }

  public Long getAcctLimit() {
    return this.acctLimit;
  }
  
  public void validate() throws LicenseContentException {
    super.validate();
    if (!TRIVAL.equalsIgnoreCase(this.plan) && !PERSONAL.equalsIgnoreCase(this.plan) && !PRO.equalsIgnoreCase(this.plan)
        && !ENTERPRISE.equalsIgnoreCase(this.plan)) {
      throw new LicenseContentException(EXC_PLAN_TYPE_INVALID);
    }

    if (this.getAcctLimit() == null || this.acctLimit < 1) {
      throw new LicenseContentException(EXC_ACCT_LIMIT_POSITIVE);
    }
  }
}
