package app.services.pb;

import static org.javalite.common.Collections.map;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import app.exceptions.InvalidFormatFileException;
import app.models.pb.Pbill;
import app.models.pb.PbillRecord;
import app.models.pb.PubServiceNum;
import app.models.pb.RelNumber;
import app.models.pb.VenNumber;
import app.models.search.CriteriaTuple;
import app.models.search.Op;
import app.models.search.Options;
import app.util.UniversalQueryHelper;
import app.util.collections.CounterMap;

/**
 * @author
 */
public class PbillServiceImpl implements PbillService, PbillStatCapable{

  private static final Logger log = LoggerFactory.getLogger(PbillServiceImpl.class);
  private static final List<String> PLAIN_FILE_EXTS =
      new ArrayList<>(Arrays.asList("txt", "csv", "dat", "data"));

  @Inject
  private PbillStatService pbillStatService;
  /**
   *
   */
  @Override
  public List<Map> doImport(String pbillImportId, Long caseId, String fileName, InputStream inputStream) {
    List<Map> result = new ArrayList<>();
    Map<String, Pbill> pbills = new HashMap<>();

    try {
      String fileExt = FilenameUtils.getExtension(fileName);
      if(PLAIN_FILE_EXTS.contains(fileExt)) {
        log.info("Handle plain text");
        Map<String, Object> m1 = new HashMap<>();
        List<Map> lm1 = handlePlainFile(pbillImportId, caseId, inputStream);
        m1.put("file", fileName);
        m1.put("counter", lm1.get(0));
        result.add(m1);
        pbills.putAll(lm1.get(1));
      } else {
        log.info("Handle zip file");

        // GBK fix Chinese character file name
        ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("gbk"));
        ZipEntry ze;
        while (( ze = zis.getNextEntry()) != null) {
          if( !ze.isDirectory() ) {
            Map<String, Object> m2 = new HashMap<>();
            List<Map> lm2 = handlePlainFile(pbillImportId, caseId, zis);
            m2.put("file", fileName);
            m2.put("counter", lm2.get(0));
            result.add(m2);
            pbills.putAll(lm2.get(1));
          }
        }
      }
      //案件导入完成
      afterImport(caseId, pbills);
      
    } catch(Exception e) {
      log.error("Error in import pbill:", e);
      
      Map<String, String> m3 = new HashMap<>();
      m3.put("file", fileName);
      m3.put("error", e.getMessage());
      result.add(m3);
    }
    return result; 
  }  
  
  public List<Map> connections(String ownerNum, int topN, int callLimit) {
    List<Map> results = new ArrayList<Map>();
    //@formatter:off
    String sql = "SELECT peer_num, count(1) as count FROM pbill_records WHERE owner_num = ? " + 
                 "GROUP BY peer_num " + 
                 "ORDER BY count DESC LIMIT ?";
    String connSql = "SELECT owner_num, peer_num, count(1) as count " + 
                     "FROM pbill_records WHERE owner_num = ? AND peer_num = ? having count > ?";
    //@formatter:on
    List<Map> lm1 = Base.findAll(sql, ownerNum, topN);
    if (lm1 != null) {
      for (int i = 0; i < lm1.size(); i++) {
        Map m1 = lm1.get(i);
        for (int j = i + 1; j < lm1.size(); j++) {
          Map m2 = lm1.get(j);
          Object num1 = m1.get("peer_num");
          Object num2 = m2.get("peer_num");
          List<Map> conn1 = Base.findAll(connSql, num1, num2, callLimit);
          List<Map> conn2 = Base.findAll(connSql, num2, num1, callLimit);
          results.addAll(conn1);
          results.addAll(conn2);
        }
      }
    }
    return results;
  }

  /**
   *
   */
  private List<Map> handlePlainFile(String pbillImportId, Long caseId, InputStream inputStream)
  throws Exception {
    // StringReader srd = null;
    // String theString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    Reader in = new InputStreamReader(inputStream);

    Iterable<CSVRecord> records = CSVFormat.RFC4180.withTrim().parse(in);

    String sql = "INSERT IGNORE INTO pbill_records " + 
                 "SET pbill_id = ?, owner_num = ?, peer_num = ?, peer_short_num = ?, peer_num_type = ?, " + 
                 "peer_num_attr = ?, peer_num_isp = ?,  ven = ?, bill_type = ?, started_at = ?, " +
                 "ended_at = ?, weekday = ?, started_day = ?, alyz_day = ?, alyz_day_type = ?, started_time = ?, " +
                 "started_time_l1_class = ?, started_time_l2_class = ?, started_hour_class = ?, time_class = ?, " +
                 "duration = ?, duration_class = ?, comm_direction = ?, owner_num_status = ?, owner_comm_loc = ?, " +
                 "peer_comm_loc = ?, long_dist = ?, owner_lac = ?, owner_ci = ?, owner_mnc = ?, owner_ct_code = ?";
    
    PreparedStatement ps = Base.startBatch(sql);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd.HHmmss");
    SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat daySdf2 = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat hourSdf = new SimpleDateFormat("HH:mm");

    Pbill pbill = null;
    int totalCount = 0;
    int batchSize = 20000;

    Map<String, Pbill> pbills = new HashMap<>();
    CounterMap counter = new CounterMap();
    List<Object> vals = new ArrayList<>();
    Date startedAt = null;
    String startedAtStr = null;

    for (CSVRecord record : records) {
      if(totalCount == 0) { // get first row and create pbill if possibile
        Base.openTransaction();
      }
      String ownerNum = record.get(1).replace("'", "");
      pbill = pbills.get(ownerNum);
      if (pbill == null) {
        pbill = Pbill.findOrCreate(ownerNum);
        pbills.put(ownerNum, pbill);
      }
      if(pbill == null) throw new InvalidFormatFileException();

      vals.add(String.valueOf(pbill.getLongId()));
      // vals.add(String.valueOf(caseId));

      // handle cell
      for(int i=1; i<31; i++) {
        String cellVal = record.get(i).replace("'", "");

        if(i == 3) { // 开始时间
          startedAt = sdf.parse(cellVal);
          startedAtStr = cellVal;
        }  else if(i == 10) { // 开始时间
          vals.add(startedAtStr);
        } else if(i == 11) { // 结束时间
					vals.add(sdf.format(sdf2.parse(cellVal)));
				} else if (i == 13) { // 开始日期(天)
          vals.add(daySdf.format(startedAt)); 
        } else if(i == 14) { // 名义日期(天)
			   	vals.add(daySdf.format(daySdf2.parse(cellVal)));
				} else if(i == 16) { // 开始时间(时秒)
					vals.add(hourSdf.format(startedAt));
        } else if(i == 28) { // lac (hex -> decimal)
          if("".equals(cellVal)) {
            vals.add(-1);
          } else {
            vals.add(Long.parseLong(cellVal, 16));
          }
        } else if( i == 29) { // ci (hex -> decimal)
          if("".equals(cellVal)) {
            vals.add(-1);
          } else {
            vals.add(Long.parseLong(cellVal, 16));
          }
				} else if (i == 30) { // ct_code
          String ctCode = cellVal.replace(")", ""); 
          String[] ctItems = ctCode.split(":");

          if (ctItems.length == 3) {
            // normalized cell-tower code
            long lac = Long.parseLong(ctItems[0].toString(), 16);
            long ci = Long.parseLong(ctItems[1].toString(), 16);
            long mnc = Long.parseLong(ctItems[2].toString(), 16);
            vals.add(mnc); // mnc
            ctCode = Long.toHexString(lac).toUpperCase() + ":" + Long.toHexString(ci).toUpperCase() + ":"
                     + Long.toHexString(mnc).toUpperCase();
          } else {
            vals.add(-1);
          }
					vals.add(ctCode);
				} else {
					vals.add(cellVal);
				}
      }

      Base.addBatch(ps, vals.stream().toArray(Object[]::new));
      totalCount ++;
      counter.incr(ownerNum);
      vals.clear();
      
      if(totalCount % batchSize == 0) {
        Base.executeBatch(ps);
        Base.commitTransaction();

        Base.openTransaction(); // next batch
      }
    }  

    if( totalCount % batchSize != 0) { // run last batch
      Base.executeBatch(ps);
      Base.commitTransaction();
    }

    // if(in != null) in.close();
    // TODO: refactoring the code using model
    log.debug("Create association between cases and pbills");
    String casePbillSql =
        "INSERT IGNORE cases_pbills SET case_id = ?, pbill_id = ?, created_at = NOW(), updated_at = NOW()";
    PreparedStatement ps2 = Base.startBatch(casePbillSql);
    Base.openTransaction();
    for (Pbill pb : pbills.values()) {
      Base.addBatch(ps2, caseId, pb.getLongId());
    }
    Base.executeBatch(ps2);
    Base.commitTransaction();

    List<Map> ret = new ArrayList<>();
    ret.add(counter.toMap());
    ret.add(pbills);
    return ret;
  }

  /**
   * @param caseId
   */
  public void afterImport(Long caseId, Map<String, Pbill> pbills) {
    if (pbills == null || pbills.size() == 0) {
      return;
    }
    //@formatter:off
    String notEqualSql = "SELECT pr.peer_num, pr.peer_short_num " +
                         "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                         "WHERE cp.case_id = ? AND pr.owner_num = ? AND pr.peer_num != pr.peer_short_num " +
                         "AND pr.peer_num IS NOT null AND pr.peer_short_num IS NOT null " +
                         "GROUP BY pr.peer_num, pr.peer_short_num";
    String eaualSql = "SELECT pr.peer_num, pr.peer_short_num " +
                      "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                      "WHERE cp.case_id = ? AND pr.owner_num = ? AND pr.peer_num = pr.peer_short_num " +
                      "AND pr.peer_num IS NOT null AND pr.peer_short_num IS NOT null " +
                      "GROUP BY pr.peer_num, pr.peer_short_num";
    String relOrVenSql = "num = ? AND short_num = ?";
    //@formatter:on
    Set<String> ownerNums = pbills.keySet();
    Base.openTransaction();
    for (String ownerNum : ownerNums) {
      List<Map> lm1 = Base.findAll(eaualSql, caseId, ownerNum);
      List<Map> lm2 = Base.findAll(notEqualSql, caseId, ownerNum);
      if (lm1 != null && lm2 != null && lm1.size() > 0 && lm2.size() > 0) {
        for (Map equalm1 : lm1) {
          String peerShortNum = equalm1.get("peer_short_num").toString();
          for (Map notEquelm2 : lm2) {
            if (peerShortNum.equals(notEquelm2.get("peer_short_num"))) {
              String peerNum = notEquelm2.get("peer_num").toString();
              PbillRecord.update("peer_num = ?",
                  "peer_short_num = ? AND owner_num = ? ", peerNum,
                  peerShortNum, ownerNum);
              RelNumber relNumber =
                  RelNumber.findFirst(relOrVenSql, peerNum, peerShortNum);
              VenNumber venNumber =
                  VenNumber.findFirst(relOrVenSql, peerNum, peerShortNum);
              if (venNumber != null) {
                VenNumber venNumber2 = new VenNumber();
                venNumber2.copyFrom(venNumber);
                venNumber2.setCaseId(caseId);
                venNumber2.saveIt();
              }
              if (relNumber != null) {
                RelNumber relNumber2 = new RelNumber();
                relNumber2.copyFrom(relNumber);
                relNumber2.setCaseId(caseId);
                relNumber2.saveIt();
              }
            }
          }
        }
      }
    }
    Base.commitTransaction();
    List<Long> vals0 = new ArrayList<>();
    vals0.add(caseId);
    List<String> ph = new ArrayList<>();
    for (Pbill pb : pbills.values()) {
      ph.add("?");
      vals0.add(pb.getLongId());
    }
    String pbillConds = "cp.pbill_id IN (" + String.join(",", ph) + ") ";
    // @formatter:off
    // update pbill record
    String statPbRecsSql = "SELECT cp.pbill_id as pbill_id, count(1) as count, count(distinct(pr.peer_num)) as peer_num_count, " +
                                  "max(pr.started_at) as max_started_at, min(pr.started_at) as min_started_at, " + 
                                  "max(pr.alyz_day) as max_alyz_day, min(pr.alyz_day) as min_alyz_day  " +
                          "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id " + 
                          "WHERE cp.case_id = ? AND " + pbillConds + 
                          "GROUP BY cp.pbill_id";
    String updatePbillSql = "UPDATE pbills " + 
                            "SET alyz_day_start = ?, alyz_day_end = ?, started_at = ?, ended_at = ?, " + 
                                "peer_num_count = ?, total = ?, updated_at = NOW() " + 
                            "WHERE id = ?";
    // @formatter:on
    List<Map> statList = Base.findAll(statPbRecsSql, vals0.stream().toArray(Object[]::new));

    Base.openTransaction();
    PreparedStatement updatePbillPs = Base.startBatch(updatePbillSql);
    List<Object> vals = new ArrayList<>();
    for (Map<String, Object> statMap : statList) {
      vals.add(statMap.get("min_alyz_day"));
      vals.add(statMap.get("max_alyz_day"));
      vals.add(statMap.get("min_started_at"));
      vals.add(statMap.get("max_started_at"));
      vals.add(statMap.get("peer_num_count"));
      vals.add(statMap.get("count"));
      vals.add(statMap.get("pbill_id"));
      Base.addBatch(updatePbillPs, vals.stream().toArray(Object[]::new));
      vals.clear();
    }
    Base.executeBatch(updatePbillPs);
    updateCaseStat(caseId);
    Base.commitTransaction();
  }

  public void updateCaseStat(Long caseId) {
    //@formatter:off
    String statPbRecsSql = "SELECT count(1) as count, count(distinct(pr.owner_num)) as owner_num_count, " + 
                            "count(distinct(pr.peer_num)) as peer_num_count, " +
                            "max(pr.started_at) as max_started_at, min(pr.started_at) as min_started_at, " + 
                            "max(pr.alyz_day) as max_alyz_day, min(pr.alyz_day) as min_alyz_day  " +
                            "FROM pbill_records as pr LEFT JOIN cases_pbills as cp ON pr.pbill_id = cp.pbill_id " + 
                            "WHERE cp.case_id = ? "; 
    String ownerCommLocCount = "SELECT pr.owner_comm_loc, COUNT(*) AS c " +
                               "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id " +
                               "WHERE cp.case_id = ? AND pr.owner_comm_loc IS NOT NULL AND pr.owner_comm_loc != '' " +
                               "GROUP BY pr.owner_comm_loc ORDER BY c DESC LIMIT 1";
    String updateCaseSql =  "UPDATE cases " +
                            "SET pb_started_at = ?, pb_ended_at =?, pb_alyz_day_start = ?, pb_alyz_day_end = ?, " +
                            "pb_rec_count= ?, owner_num_count = ?, peer_num_count = ?, pb_city = ? " +
                            "WHERE id = ?";
    //@formatter:on
    List<Map> ownerCommLocList = Base.findAll(ownerCommLocCount, caseId);
    Map<String, String> ownerCommLoc = ownerCommLocList.get(0);
    List<Map> r = Base.findAll(statPbRecsSql, caseId);
    Map<String, Object> statMap = r.get(0);
    List<Object> vals = new ArrayList<>();
    vals.add(statMap.get("min_started_at"));
    vals.add(statMap.get("max_started_at"));
    vals.add(statMap.get("min_alyz_day"));
    vals.add(statMap.get("max_alyz_day"));
    vals.add(statMap.get("count"));
    vals.add(statMap.get("owner_num_count"));
    vals.add(statMap.get("peer_num_count"));
    vals.add(ownerCommLoc.get("owner_comm_loc"));
    vals.add(caseId);

    Base.exec(updateCaseSql, vals.stream().toArray(Object[]::new));
  }

  // 补全基站数据: 相同ci的补充lac数据
  private void washCellTowers(Long caseId) {
    String sql = "SELECT * FROM pbill_records as pr WHERE pr.lac = 0 OR pr.lac = -1 OR pr.lac IS NULL  ";
  }

  /**
   * 通话亲密关系
   */
  @Override
  public List<Map> connections(String json, Long caseId) throws Exception {
    Options options = UniversalQueryHelper.normalize(json, "pr", map("cp.case_id", caseId.toString()));
    List<Map> results = new ArrayList<Map>();
    // 满足条件的前20个对方号码
    CriteriaTuple criteria = options.getCriteria("pr.owner_num");
    Integer topN = (Integer) options.getAdhocParam("top_n");
    Integer callLimit = (Integer) options.getAdhocParam("call_limit");
    Integer showPeerNum = (Integer) options.getAdhocParam("show_peer_num");
    if (callLimit == null || criteria == null) {
      return null;
    }
    List<Object> ownerNums = criteria.getValues();
    //@formatter:off
    String sql = "SELECT pr.peer_num, count(1) as count, pr.owner_num " + 
        "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String groupBy = "GROUP BY peer_num ";
    String orderBy = "ORDER BY count DESC ";
    String limitSql = "LIMIT ?";
    
    // 查出满足相互通话次数的sql
    String sql2 = "SELECT pr.owner_num, pr.peer_num, count(1) as count, pr.owner_cname, pr.peer_cname " +
             "FROM pbill_records pr LEFT JOIN cases_pbills cp ON pr.pbill_id = cp.pbill_id ";
    String having = "HAVING count > ?"; 
    //@formatter:on
    List<PubServiceNum> pubServiceNums = PubServiceNum.findAll();// 可不可以把特殊号码放进缓存
    List<Object> unContain = new ArrayList<Object>();

    options.delCriteria("pr.owner_num");
    for (int i = 0; i < ownerNums.size(); i++) {
      Object num0 = ownerNums.get(i);
      for (int j = i + 1; j < ownerNums.size(); j++) {
        Object num1 = ownerNums.get(j);
        options.addCriteria(new CriteriaTuple("pr.owner_num", num0, CriteriaTuple.ONCE_QUERY));
        options.addCriteria(new CriteriaTuple("pr.peer_num", num1, CriteriaTuple.ONCE_QUERY));
        List<Map> conn = doStat(options, sql2, "", "", having, callLimit);

        if (conn.size() > 0) {
          long inConnonCnt = Pbill.peerNumsCommonDegree(caseId, num0.toString(), num1.toString());
          
          conn.get(0).put("in_commons_cnt", inConnonCnt);
        }
        results.addAll(conn);
      }
    }
    
    if (showPeerNum == SHOW_PEER_NUM && topN != null) {
      List<Map> lm1 = new ArrayList<Map>();
      List<Map> lm2 = new ArrayList<Map>();
      // TODO:SQL的长度限制需加入考虑
      if (pubServiceNums != null && pubServiceNums.size() > 0) {
        for (PubServiceNum pubServiceNum : pubServiceNums) {
          unContain.add(pubServiceNum.getNum());
        }
        options.addCriteria(new CriteriaTuple("pr.peer_num", Op.NOT_IN, unContain)); 
      }

      // 要查询的本方号码
      for (Object object : ownerNums) {
        options.addCriteria(new CriteriaTuple("pr.owner_num", object,
            CriteriaTuple.ONCE_QUERY));
        lm2 = doStat(options, sql, groupBy, orderBy, limitSql, topN);
        lm1.addAll(lm2);
      }
      // 获得所有的号码（去除重复的对方号码）
      Set<String> nums = new HashSet<String>();
      for (Map map : lm1) {
        nums.add(map.get("owner_num").toString());
        nums.add(map.get("peer_num").toString());
      }
      log.debug("nums.size : {}", nums.size());
      log.debug("nums : {}", nums);
      Object[] numbers = nums.toArray();
      log.debug("numbers.length{}", numbers.length);

      if (lm1 != null) {
        // 所有号码之间满足通话次数的结果
        for (int i = 0; i < numbers.length; i++) {
          Object num0 = numbers[i];
          log.debug("numsi{}:{}", i, num0);
          for (int j = i + 1; j < numbers.length; j++) {
            Object num1 = numbers[j];
            log.debug("numsj{}:{}", j, num1);
            options.addCriteria(new CriteriaTuple("pr.owner_num", num0, CriteriaTuple.ONCE_QUERY));
            options.addCriteria(new CriteriaTuple("pr.peer_num", num1, CriteriaTuple.ONCE_QUERY));
            List<Map> conn = doStat(options, sql2, "", "", having, callLimit);
            options.addCriteria(new CriteriaTuple("pr.owner_num", num1, CriteriaTuple.ONCE_QUERY));
            options.addCriteria(new CriteriaTuple("pr.peer_num", num0, CriteriaTuple.ONCE_QUERY));
            List<Map> conn1 = doStat(options, sql2, "", "", having, callLimit);

            results.addAll(conn);
            results.addAll(conn1);
          }
        }
      }
    }
    return results;
  }

}