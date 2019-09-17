package app.jobs.events;

import java.util.List;

public class PbillAddedEvent extends CaseRelatedEvent {

  private List<String> ownerNums;

  public PbillAddedEvent(Long caseId) {
    super(caseId);
  }

  public PbillAddedEvent(Long caseId, List<String> ownerNums) {
    super(caseId);
    this.ownerNums = ownerNums;
  }

  public List<String> getOwnerNums() {
    return this.ownerNums;
  }
}
