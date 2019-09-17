package app.jobs.events;

public class CaseRelatedEvent extends AppEvent {
  private Long caseId;

  public CaseRelatedEvent() {
  }

  public CaseRelatedEvent(Long caseId) {
    this.caseId = caseId;
  }

  public Long getCaseId() {
    return this.caseId;
  }
}
