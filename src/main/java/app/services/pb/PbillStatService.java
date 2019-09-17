package app.services.pb;

import java.util.Set;

import app.models.pb.PnumMeet;

/**
 * @author
 */
public interface PbillStatService {
  public static final int MUTUAL_CALL_TRUE = 1;
  public static final int MUTUAL_CALL_FALSE = 0;

  public Set peerNumsInCommon(Long caseId, String num1, String num2);

  public long peerNumsCommonDegree(Long caseId, String num1, String num2);

  public long peerNumsCommonDegree(String json, String num1, String num2,
      Long caseId) throws Exception;

  public PnumMeet meets(Long caseId, String json) throws Exception;

}
