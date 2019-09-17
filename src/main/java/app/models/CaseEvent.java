package app.models;

/**
 *
 */
public class CaseEvent extends CaseAwareModel {
  
  static {
    validatePresenceOf("name").message("Please provide caseEvent name"); 
    validatePresenceOf("started_at").message("Please provide caseEvent started_at"); 
    validatePresenceOf("ended_at").message("Please provide caseEvent ended_at"); 
  }

}