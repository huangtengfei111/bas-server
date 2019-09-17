package app.models;

import org.javalite.activejdbc.Model;

public class Search extends Model{
  
  static {
    validatePresenceOf("name").message("Please provide search name"); 
  }
  
  public Search() {}
  
  public Search(String caseId, String accountId, String name, String subject, String value) {
    manageTime(false);
    setCaseId(caseId);
    setAccountId(accountId);
    setName(name);
    setSubject(subject);
    setValue(value);
  }
  
  public void setCaseId(String caseId){
    set("case_id", caseId);
  }

  public int getCaseId(){
    return getInteger("case_id");
  }
  
  public void setAccountId(String accountId) {
    set("account_id", accountId);
  }
  
  public int getAccountId() {
    return getInteger("account_id");
  }
  
  public void setName(String name) {
    set("name", name);
  }
  
  public String getName() {
    return getString("name");
  }
  
  public void setSubject(String subject) {
    set("subject", subject);
  }
  
  public String getSubject() {
    return getString("subject");
  }
  
  public void setValue(String value) {
    set("value", value);
  }
  
  public String getValue() {
    return getString("value");
  }
  
  public String getCreatedAt() {
    return getString("created_at");
  }

  public String getUpdatedAt() {
    return getString("updated_at");
  }
}
