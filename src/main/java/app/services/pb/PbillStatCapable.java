package app.services.pb;

import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.ModelDelegate;

import app.models.search.Options;
import app.util.UniversalQueryHelper;

public interface PbillStatCapable {

  default List<Map> doStat(String json, String selectSql, String groupBy, String condPrefix, Map conds)
      throws Exception {
    return doStat(json, selectSql, groupBy, null, condPrefix, conds);
  }

  default List<Map> doStat(String json, String selectSql, String groupBy, String orderBy, String condPrefix,
      Map conds) throws Exception {
    Options options = UniversalQueryHelper.normalize(json, condPrefix, conds);
    if (orderBy != null)
      options.setOrderBy(orderBy);
    if (groupBy != null)
      options.setGroupBy(groupBy);
//    return doStat(options, selectSql, conds);
    return doStat(options, selectSql);
  }

  default List<Map> doStat(Options options, String selectSql, Object... moreParams) throws Exception {

    List<Object> sqlAndVals = UniversalQueryHelper.getSqlAndBoundVals(options, true);
    String query = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);
    if (moreParams != null) {
      for (Object p : moreParams) {
        vals.add(p);
      }
    }
    Object[] param = vals.stream().toArray(Object[]::new);
    String sql = String.join(" ", selectSql, query);
    return Base.findAll(sql, param);
  }
  
  default List<Map> doStat(Options options, String selectSql, String groupBy, String orderBy,
      String moreSql, Object... moreParams) throws Exception {

    List<Object> sqlAndVals = UniversalQueryHelper.getSqlAndBoundVals(options, true);
    String query = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);
    if (moreParams != null) {
      for (Object p : moreParams) {
        vals.add(p);
      }
    }
    Object[] param = vals.stream().toArray(Object[]::new);
    String sql = String.join(" ", selectSql, query, groupBy, orderBy, moreSql);
    return Base.findAll(sql, param);
  }

  default <T extends Model> LazyList<T> findAll(Class<T> clazz, String json, String selectSql, String condPrefix,
      Map conds) throws Exception {
    return findAll(clazz, json, selectSql, "", "", condPrefix, conds);
  }

  default <T extends Model> LazyList<T> findAll(Class<T> clazz, Options options, String selectSql) throws Exception {
    List<Object> sqlAndVals = UniversalQueryHelper.getSqlAndBoundVals(options, true);
    String query = (String) sqlAndVals.get(0);
    String fullQuery = String.join(" ", selectSql, query);
    List vals = (List) sqlAndVals.get(1);

    Object[] params = vals.stream().toArray(Object[]::new);

    return ModelDelegate.findBySql(clazz, fullQuery, params);
  }
  /**
   * 
   * @param <T>
   * @param clazz
   * @param json
   * @param selectSql
   * @param groupBy
   * @param condPrefix
   * @param conds
   * @return
   * @throws Exception
   */
  default <T extends Model> LazyList<T> findAll(Class<T> clazz, String json, String selectSql, String groupBy,
      String condPrefix, Map conds) throws Exception {
    return findAll(clazz, json, selectSql, groupBy, "", condPrefix, conds);
  }

  default <T extends Model> LazyList<T> findAll(Class<T> clazz, String json, String selectSql, String groupBy,
      String orderBy, String condPrefix, Map conds) throws Exception {
    List<Object> sqlAndVals = UniversalQueryHelper.getSqlAndBoundVals(json, condPrefix, true, conds);
    String query = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);
    Object[] params = vals.stream().toArray(Object[]::new);
    String fullQuery = String.join(" ", selectSql, query, groupBy, orderBy);

    return ModelDelegate.findBySql(clazz, fullQuery, params);
  }

}
