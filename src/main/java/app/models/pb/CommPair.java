package app.models.pb;

public class CommPair {

  private Long caseId;
  private String peerNumA;
  private String peerNumB;

  public CommPair() {
  }

  public CommPair(Long caseId, String peerNumA, String peerNumB) {
    this.caseId   = caseId;
    this.peerNumA = peerNumA;
    this.peerNumB = peerNumB;
  }

  public CommPair shadow() {
    return new CommPair(this.caseId, this.peerNumB, this.peerNumA);
  }

  public Long getCaseId() {
    return caseId;
  }

  public void setCaseId(Long caseId) {
    this.caseId = caseId;
  }

  public String getPeerNumA() {
    return peerNumA;
  }

  public void setPeerNumA(String peerNumA) {
    this.peerNumA = peerNumA;
  }

  public String getPeerNumB() {
    return peerNumB;
  }

  public void setPeerNumB(String peerNumB) {
    this.peerNumB = peerNumB;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((caseId == null) ? 0 : caseId.hashCode());
    result = prime * result + ((peerNumA == null) ? 0 : peerNumA.hashCode());
    result = prime * result + ((peerNumB == null) ? 0 : peerNumB.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CommPair other = (CommPair) obj;
    if (caseId == null) {
      if (other.caseId != null)
        return false;
    } else if (!caseId.equals(other.caseId))
      return false;
    if (peerNumA == null) {
      if (other.peerNumA != null)
        return false;
    } else if (!peerNumA.equals(other.peerNumA))
      return false;
    if (peerNumB == null) {
      if (other.peerNumB != null)
        return false;
    } else if (!peerNumB.equals(other.peerNumB))
      return false;
    return true;
  }

}
