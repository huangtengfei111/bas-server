package app.jobs.events;

public class CaseDeletedEvent extends CaseRelatedEvent {

  public CaseDeletedEvent(Long caseId) {
    super(caseId);
  }
}
