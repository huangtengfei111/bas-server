package app.models.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Op {
	public static final String EQ = "=";
  public static final String GT = ">";
  public static final String GT_EQ = ">=";
  public static final String LT = "<";
  public static final String LT_EQ = "<=";
	public static final String IN = "IN";
  public static final String NOT_IN = "NOT_IN";
	public static final String BETWEEN = "BETWEEN";
	public static final String FUZZY = "FUZZY";
  public static final String END_WITH = "END_WITH";
  public static final String LIKE = "LIKE";
  public static final String NOT_CONTAIN = "NOT_CONTAIN";

  public static SqlSnippet fuzzyMatch(CriteriaTuple tuple) {
    final List<String> l = new ArrayList<>();
    final List<String> v = new ArrayList<>();

		tuple.getValues().forEach((value) -> {
				l.add(tuple.getField() + " LIKE ? ");
				v.add("%" + value.toString() + "%");
			});

    SqlSnippet snippet = new SqlSnippet();
    snippet.setSql(String.join(" OR ", l));
    snippet.setValues(v);
    return snippet;
	}

  public static SqlSnippet notContain(CriteriaTuple tuple) {
    final List<String> l = new ArrayList<>();
    final List<String> v = new ArrayList<>();

    tuple.getValues().forEach((value) -> {
      l.add(tuple.getField() + " NOT LIKE ? ");
      v.add("%" + value.toString() + "%");
    });

    SqlSnippet snippet = new SqlSnippet();
    snippet.setSql(String.join(" AND ", l));
    snippet.setValues(v);
    return snippet;
  }
	
	
  public static SqlSnippet in(CriteriaTuple tuple) {
    final List<String> sql = new ArrayList();
    final List<Object> values = new ArrayList();
		sql.add(" IN ( ");
		String join=" , ";
		tuple.getValues().forEach((value) -> {
			sql.add(tuple.getField());
			sql.add(join);
			values.add(value);
		});
		sql.remove(sql.size()-1);
		sql.add(")");
		
    return new SqlSnippet(String.join("", sql), values);
	}
  
  public static SqlSnippet notIn(CriteriaTuple tuple) {
    final List<String> sql = new ArrayList();
    final List<Object> values = new ArrayList();
    sql.add(" NOT IN ( ");
    String join=" , ";
    tuple.getValues().forEach((value) -> {
      sql.add(tuple.getField());
      sql.add(join);
      values.add(value);
    });
    sql.remove(sql.size()-1);
    sql.add(")");
    
    return new SqlSnippet(String.join("", sql), values);
  }
	
	public static List eq(CriteriaTuple tuple) {
    final List<String> sql = new ArrayList();
    final List<Object> values = new ArrayList();
    tuple.getValues().forEach((value) -> {
      sql.add(tuple.getField()+" = ? ");
      values.add(value);
    });
    return Arrays.asList(sql, values);
  }
	
  public static SqlSnippet between() {
    return null;
  }
	
}