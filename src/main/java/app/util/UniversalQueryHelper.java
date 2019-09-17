package app.util;

import static org.javalite.common.Collections.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.models.search.CriteriaTuple;
import app.models.search.Op;
import app.models.search.Options;

@SuppressWarnings("unchecked")
public class UniversalQueryHelper {

	public static List<Object> getSqlAndBoundVals(String options) throws Exception {
		return getSqlAndBoundVals(options, false, null);
	}
	
	public static List<Object> getSqlAndBoundVals(String options, String... moreConds) 
	throws Exception {
		Map<String, String> _moreConds = null;
		if(moreConds != null) {
			_moreConds = map(moreConds);
		}
		Options _options = normalize(options, null, _moreConds);
		return getSqlAndBoundVals(_options, false);
	}

	public static List<Object> getSqlAndBoundVals(String options, boolean withWhere, String... moreConds) 
	throws Exception {
		Map<String, String> _moreConds = null;
		if(moreConds != null) {
		 	_moreConds = map(moreConds);
		}
		Options _options = normalize(options, null, _moreConds);
		return getSqlAndBoundVals(_options, withWhere);
	}

  public static List<Object> getSqlAndBoundVals(String options, String tableAlia, boolean withWhere,
      Map<String, String> moreConds) throws Exception {
    return getSqlAndBoundVals(options, tableAlia, withWhere, moreConds, null, null);
  }

  public static List<Object> getSqlAndBoundVals(String options, String tableAlia, boolean withWhere,
      Map<String, String> moreConds, String moreSql, String... moreParams) 
	throws Exception {
		
	  Options _options = normalize(options, tableAlia, moreConds);
    return getSqlAndBoundVals(_options, withWhere, "1 = 1", moreSql, moreParams);
	}

  public static List<Object> getSqlAndBoundVals(Options options, boolean withWhere) {
    return getSqlAndBoundVals(options, withWhere, "1 = 1");
  }

  public static List<Object> getSqlAndBoundVals(Options options, boolean withWhere, String nullSqlStmt) {
    return getSqlAndBoundVals(options, withWhere, nullSqlStmt, null, null);
  }

  public static List<Object> getSqlAndBoundVals(Options options, boolean withWhere, String nullSqlStmt,
      String moreSql, String... moreParams) {
		ArrayList<Object> bindVals = new ArrayList<>();
		ArrayList<String> sqlItems = new ArrayList<>();

		Set<CriteriaTuple> cTuples = options.getCriterias();

    if (cTuples != null) {
      for(CriteriaTuple cTuple: cTuples) {
  			List ceriteriaValues = cTuple.getValues();
  
  			switch (cTuple.getOp()) {
  			case Op.FUZZY:
          // Op.fuzzy(criteria)
  				StringBuffer fuzzyTempSql = new StringBuffer();
  				String joint = " OR ";
  				ceriteriaValues.forEach((value) -> {
  					fuzzyTempSql.append(cTuple.getField() + " LIKE ? ");
  					bindVals.add("%" + value + "%");
  					fuzzyTempSql.append(joint);
  				});
  				fuzzyTempSql.delete(fuzzyTempSql.length() - joint.length(), fuzzyTempSql.length());
          sqlItems.add("( " + fuzzyTempSql.toString() + " )");
  				break;
        case Op.END_WITH:
          // Op.fuzzy(criteria)
          StringBuffer endWithSql = new StringBuffer();
          String joint2 = " OR ";
          ceriteriaValues.forEach((value) -> {
            endWithSql.append(cTuple.getField() + " LIKE ? ");
            bindVals.add("%" + value);
            endWithSql.append(joint2);
          });
          endWithSql.delete(endWithSql.length() - joint2.length(), endWithSql.length());
          sqlItems.add("( " + endWithSql.toString() + " )");
          break;
        case Op.NOT_CONTAIN:
          // Op.NOT_CONTAIN(criteria)
          StringBuffer notContainTempSql = new StringBuffer();
          String joinAnd = " AND ";
          ceriteriaValues.forEach((value) -> {
            notContainTempSql.append("(" + cTuple.getField() + " NOT LIKE ? )");
            bindVals.add("%" + value + "%");
            notContainTempSql.append(joinAnd);
          });
          notContainTempSql.delete(notContainTempSql.length() - joinAnd.length(),
              notContainTempSql.length());
          sqlItems.add("( " + notContainTempSql.toString() + " )");
          break;
  			case Op.IN:
  				// Op.in(ceriteria)
  				StringBuffer inTempSql = new StringBuffer();
  				String inJoin = " ?,";
  				inTempSql.append(cTuple.getField() + " IN (");
  				ceriteriaValues.forEach((value) -> {
  					inTempSql.append(inJoin);
  					bindVals.add(value);
  				});
  				inTempSql.delete(inTempSql.toString().lastIndexOf(","), inTempSql.length());
  				inTempSql.append(" )");
          sqlItems.add("( " + inTempSql.toString() + " )");
  				break;
  			case Op.NOT_IN:
          // Op.NOT_IN(ceriteria)
          StringBuffer notInTempSql = new StringBuffer();
          String notInJoin = " ?,";
          notInTempSql.append(cTuple.getField() + " NOT IN (");
          ceriteriaValues.forEach((value) -> {
            notInTempSql.append(notInJoin);
            bindVals.add(value);
          });
          notInTempSql.delete(notInTempSql.toString().lastIndexOf(","), notInTempSql.length());
          notInTempSql.append(" )");
          sqlItems.add("(" + notInTempSql.toString() + ")");
          break;
  			case Op.BETWEEN:
  				StringBuffer betweenTempSql = new StringBuffer();
  				if (ceriteriaValues.size() % 2 != 0)
  					throw new IllegalArgumentException("BETWEEN 的参数异常");
  
  				for (int i = 0; i < ceriteriaValues.size(); i = i + 2) {
  					betweenTempSql.append(cTuple.getField() + " BETWEEN ? AND ?");
  					bindVals.add(ceriteriaValues.get(i));
  					bindVals.add(ceriteriaValues.get(i + 1));
  					if (i + 2 < ceriteriaValues.size())
  						betweenTempSql.append(" OR ");
  				}
          sqlItems.add("( " + betweenTempSql.toString() + " )");
  				break;
        case Op.GT:
          ceriteriaValues.forEach((value) -> {
            sqlItems.add(cTuple.getField() + " > ? ");
            bindVals.add(value);
          });
          break;
        case Op.GT_EQ:
          ceriteriaValues.forEach((value) -> {
            sqlItems.add(cTuple.getField() + " >= ? ");
            bindVals.add(value);
          });
          break;
        case Op.LT:
          ceriteriaValues.forEach((value) -> {
            sqlItems.add(cTuple.getField() + " < ? ");
            bindVals.add(value);
          });
          break;
        case Op.LT_EQ:
          ceriteriaValues.forEach((value) -> {
            sqlItems.add(cTuple.getField() + " <= ? ");
            bindVals.add(value);
          });
          break;
  			default: // default is EQ
  				ceriteriaValues.forEach((value) -> {
  					sqlItems.add(cTuple.getField() + " = ? ");
  					bindVals.add(value);
  				});
  			}
  			
        if (cTuple.isOnceQuery()) {
          options.getCriterias().remove(cTuple);
        }
      }
		}

    String sql = null;
    if (sqlItems.size() > 0) {
      sql = String.join(" " + options.getCondJoint() + " ", sqlItems) + " ";
    } else {
      sql = nullSqlStmt;
    }

    if (sql != null) {
      if (withWhere) {
        sql = " WHERE " + sql;
      }
      if (options.getNullFields() != null) {
        List<String> nullFields = options.getNullFields();
        List<String> nf = new ArrayList<>();
        for (String field : nullFields) {
          nf.add(field + " IS NULL");
        }
        sql = sql + " AND (" + String.join(" AND ", nf) + ")";
      }
      if (options.getNotNullFields() != null) {
        List<String> notNullFields = options.getNotNullFields();
        List<String> nnf = new ArrayList<>();
        for (String field : notNullFields) {
          nnf.add(field + " IS NOT NULL");
        }
        sql = sql + " AND (" + String.join(" AND ", nnf) + ")";
      }
      if (options.getGroupBy() != null) {
        sql = sql + " " + options.getGroupBy();
      }
      if (options.getOrderBy() != null) {
        sql = sql + " " + options.getOrderBy();
      }
      if (moreSql != null) {
        sql = sql + moreSql;
        bindVals.addAll(Arrays.asList(moreParams));
      }
      if (options.getLimit() > 0) {
        sql = sql + " LIMIT " + options.getLimit();
      }
    }
		return Arrays.asList(sql, bindVals);
	}

	/**
   *
   *将json中的数据转到options中
   * 
   */
  public static Options normalize(String options, String tableAlia, Map<String, String> moreConds) 
	throws Exception {
		
		Map<String, Object> optMap = JsonHelper.toMap(options);
		LinkedHashMap<String, Object> criteriaMap = (LinkedHashMap<String, Object>) optMap.get("criteria");
		LinkedHashMap<String, Object> viewMap = (LinkedHashMap<String, Object>) optMap.get("view");
    LinkedHashMap<String, Object> adhocMap = (LinkedHashMap<String, Object>) optMap.get("adhoc");
    String condJoint = (String) optMap.get("condJoint");

		Options opt = new Options();

		if(criteriaMap != null) {
      criteriaMap.remove("case_id");
			// criteriaMap.entrySet().stream().collect(ArrayList::new, (accIn, e) -> { ... })
			criteriaMap.forEach((key, opValPair) -> {
				CriteriaTuple cTuple = new CriteriaTuple();				
				if (opValPair instanceof List) {
					List<Object> aList = (List<Object>) opValPair;

					if(tableAlia != null && !tableAlia.equals("")) {
						key = tableAlia + "." + key;
					}

					cTuple.setField(key);
					if(aList.size() > 1) {
						String op = (String) aList.get(0);
						Object vals = aList.get(1);
							
						cTuple.setOp(op);
						cTuple.setValues(vals);
					}
				} else if(opValPair instanceof String || 
				            opValPair instanceof Number) {
          if (tableAlia != null && !tableAlia.equals("")) {
            key = tableAlia + "." + key;
          }
					cTuple.setField(key);
					cTuple.setOp("=");
					cTuple.setValues(opValPair);
				} else {
					//throw RuntimeException("invalid input");
				}
				opt.addCriteria(cTuple);
			});
		}

		if(moreConds != null) {
			moreConds.forEach((key, value) -> {
				CriteriaTuple cTuple = new CriteriaTuple();
				cTuple.setField(key);
				cTuple.setOp("=");
				cTuple.setValues(value);

				opt.addCriteria(cTuple);
			});
		}

    opt.setViews(viewMap);
    opt.setAdhocParams(adhocMap);
    if (condJoint != null) {
      opt.setCondJoint(condJoint);
    }
		return opt;
	}

}