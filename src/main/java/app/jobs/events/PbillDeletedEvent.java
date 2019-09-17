package app.jobs.events;

import app.models.pb.Pbill;

public class PbillDeletedEvent extends CaseRelatedEvent {
  private Pbill pbill;

  public PbillDeletedEvent(Long caseId, Pbill ownerNum) {
    super(caseId);
    this.pbill = ownerNum;
  }

  public Pbill getPbill() {
    return this.pbill;
  }
}
