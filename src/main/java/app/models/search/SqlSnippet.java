package app.models.search;

import java.util.List;

public class SqlSnippet {
  private String sql;
  private List values;

  public SqlSnippet() {
  }

  public SqlSnippet(String sql, List values) {
    this.sql    = sql;
    this.values = values;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public List getValues() {
    return values;
  }

  public void setValues(List values) {
    this.values = values;
  }
}
