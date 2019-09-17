package app.config;

import org.javalite.activeweb.AbstractRouteConfig;
import org.javalite.activeweb.AppContext;

import app.controllers.BackgroundTaskController;
import app.controllers.CaseBreakpointsController;
import app.controllers.CaseEventsController;
import app.controllers.CasesController;
import app.controllers.ChineseCitiesController;
import app.controllers.CitizensController;
import app.controllers.DownloadsController;
import app.controllers.FavoriteMenusController;
import app.controllers.LabelGroupsController;
import app.controllers.LoginController;
import app.controllers.MockDataController;
import app.controllers.PingController;
import app.controllers.SVGController;
import app.controllers.SearchesController;
import app.controllers.TrackApiController;
import app.controllers.UserController;
import app.controllers.admin.AccountsController;
import app.controllers.admin.AuditLogsController;
import app.controllers.admin.BackupsController;
import app.controllers.admin.LicenseController;
import app.controllers.admin.MissedCtRequestsController;
import app.controllers.admin.SettingsController;
import app.controllers.pb.CellTowersController;
import app.controllers.pb.CtLabelsController;
import app.controllers.pb.PbillAnalyzeController;
import app.controllers.pb.PbillCellTowerReportController;
import app.controllers.pb.PbillDatetimeReportController;
import app.controllers.pb.PbillDurationReportController;
import app.controllers.pb.PbillMutualController;
import app.controllers.pb.PbillOverviewReportController;
import app.controllers.pb.PbillOwnerNumReportController;
import app.controllers.pb.PbillPeerNumReportController;
import app.controllers.pb.PbillRecordsController;
import app.controllers.pb.PbillsController;
import app.controllers.pb.PnumConnectionsController;
import app.controllers.pb.PnumLabelsController;
import app.controllers.pb.PnumsController;
import app.controllers.pb.RelNumbersController;
import app.controllers.pb.VenNumbersController;
import app.controllers.zuper.LicensesController;

public class RouteConfig extends AbstractRouteConfig {
  public void init(AppContext appContext) {

    //@formatter:off
    route("/ping").to(PingController.class).get().action("index");
    
    //客户使用情况统计
    route("/track-api/top-active").to(TrackApiController.class).get().action("topActive");
    route("/track-api/top-inactive").to(TrackApiController.class).get().action("topInactive");
    
    route("/user/login").to(LoginController.class).get().action("index");
    route("/user/login").to(LoginController.class).post().action("login");
    route("/user/logout").to(LoginController.class).get().action("logout");
    route("/user/session").to(LoginController.class).get().action("currentSession");
    route("/user/profile").to(UserController.class).get().action("profile");
    route("/user/account").to(UserController.class).put().action("updateAcct");
    route("/user/account/password").to(UserController.class).post().action("updatePassword");
    route("/user/favorites").to(FavoriteMenusController.class).get().action("index");
    route("/user/favorites/add").to(FavoriteMenusController.class).post().action("add");
    route("/user/favorites/remove/{id}").to(FavoriteMenusController.class).get().action("remove");

    // 案件
    route("/cases/search").to(CasesController.class).post().action("search");
    route("/cases/filter/{status}").to(CasesController.class).get().action("filter");
    route("/cases/{case_id}/summary").to(CasesController.class).get().action("summary");

    // 案件分割点
    route("/cases/{case_id}/case_breakpoints").to(CaseBreakpointsController.class).get().action("index");
    route("/cases/{case_id}/case_breakpoints").to(CaseBreakpointsController.class).post().action("create");
    route("/cases/{case_id}/case_breakpoints/{id}").to(CaseBreakpointsController.class).get().action("show");
    route("/cases/{case_id}/case_breakpoints/{id}").to(CaseBreakpointsController.class).put().action("update");
    route("/cases/{case_id}/case_breakpoints/{id}").to(CaseBreakpointsController.class).delete().action("destroy");

    // 时间标注
    route("/cases/{case_id}/case_events").to(CaseEventsController.class).get();
    route("/cases/{case_id}/case_events").to(CaseEventsController.class).post().action("create");
    route("/cases/{case_id}/case_events/{id}").to(CaseEventsController.class).get().action("show");
    route("/cases/{case_id}/case_events/{id}").to(CaseEventsController.class).put().action("update");
    route("/cases/{case_id}/case_events/{id}").to(CaseEventsController.class).delete().action("destroy");

    // 虚拟网
    route("/cases/{case_id}/ven_numbers").to(VenNumbersController.class).get();
    route("/cases/{case_id}/ven_numbers").to(VenNumbersController.class).post().action("create");
    route("/cases/{case_id}/ven_numbers/search").to(VenNumbersController.class).post().action("search");
    route("/cases/{case_id}/ven_numbers/upload").to(VenNumbersController.class).post().action("upload");
    route("/cases/{case_id}/ven_numbers/do-import").to(VenNumbersController.class).post().action("doImport");
    route("/cases/{case_id}/ven_numbers/abort-import").to(VenNumbersController.class).post().action("abortImport");
    route("/cases/{case_id}/ven_numbers/networks").to(VenNumbersController.class).get().action("networks");
    route("/cases/{case_id}/ven_numbers/{id}").to(VenNumbersController.class).get().action("show");
    route("/cases/{case_id}/ven_numbers/{id}").to(VenNumbersController.class).put().action("update");
    route("/cases/{case_id}/ven_numbers/{id}").to(VenNumbersController.class).delete().action("destroy");

    // 亲情网
    route("/cases/{case_id}/rel_numbers").to(RelNumbersController.class).get();
    route("/cases/{case_id}/rel_numbers").to(RelNumbersController.class).post().action("create");
    route("/cases/{case_id}/rel_numbers/search").to(RelNumbersController.class).post().action("search");
    route("/cases/{case_id}/rel_numbers/upload").to(RelNumbersController.class).post().action("upload");
    route("/cases/{case_id}/rel_numbers/do-import").to(RelNumbersController.class).post().action("doImport");
    route("/cases/{case_id}/rel_numbers/abort-import").to(RelNumbersController.class).post().action("abortImport");
    route("/cases/{case_id}/rel_numbers/networks").to(RelNumbersController.class).get().action("networks");
    route("/cases/{case_id}/rel_numbers/{id}").to(RelNumbersController.class).get().action("show");
    route("/cases/{case_id}/rel_numbers/{id}").to(RelNumbersController.class).put().action("update");
    route("/cases/{case_id}/rel_numbers/{id}").to(RelNumbersController.class).delete().action("destroy");

    // 模板下载
    route("/downloads/ven_numbers").to(DownloadsController.class).get().action("venNumbers");
    route("/downloads/rel_numbers").to(DownloadsController.class).get().action("relNumbers");
    route("/downloads/pnum_labels").to(DownloadsController.class).get().action("pnumLabels");
    route("/downloads/ct_labels").to(DownloadsController.class).get().action("ctLabels");
    route("/downloads/citizen-book").to(DownloadsController.class).get().action("citizenBook");

    // 号码标注
    route("/cases/{case_id}/pnum_labels").to(PnumLabelsController.class).get();
    route("/cases/{case_id}/pnum_labels").to(PnumLabelsController.class).post().action("create");
    route("/cases/{case_id}/pnum_labels/label-group").to(PnumLabelsController.class).get().action("labelGroup"); //分类标签
    route("/cases/{case_id}/pnum_labels/search").to(PnumLabelsController.class).post().action("search");
    route("/cases/{case_id}/pnum_labels/upload").to(PnumLabelsController.class).post().action("upload");
    route("/cases/{case_id}/pnum_labels/do-import").to(PnumLabelsController.class).post().action("doImport");
    route("/cases/{case_id}/pnum_labels/abort-import").to(PnumLabelsController.class).post().action("abortImport");
    route("/cases/{case_id}/pnum_labels/{num}").to(PnumLabelsController.class).get().action("show");
    route("/cases/{case_id}/pnum_labels/{id}").to(PnumLabelsController.class).put().action("update");
    route("/cases/{case_id}/pnum_labels/{id}").to(PnumLabelsController.class).delete().action("destroy");
    route("/cases/{case_id}/pnum_labels/smart-lookup").to(PnumLabelsController.class).post().action("smartLookup");

    // 基站标注
    route("/cases/{case_id}/ct_labels").to(CtLabelsController.class).get();
    route("/cases/{case_id}/ct_labels").to(CtLabelsController.class).post().action("create");
    route("/cases/{case_id}/ct_labels/label-group").to(CtLabelsController.class).get().action("labelGroup"); //分类标签
    route("/cases/{case_id}/ct_labels/upload").to(CtLabelsController.class).post().action("upload");
    route("/cases/{case_id}/ct_labels/do-import").to(CtLabelsController.class).post().action("doImport");
    route("/cases/{case_id}/ct_labels/abort-import").to(CtLabelsController.class).post().action("abortImport");
    route("/cases/{case_id}/ct_labels/{id}").to(CtLabelsController.class).put().action("update");
    route("/cases/{case_id}/ct_labels/{id}").to(CtLabelsController.class).delete().action("destroy");
    route("/cases/{case_id}/ct_labels/labels-on-scope").to(CtLabelsController.class).post().action("createLabelsOnScope");
    route("/cases/{case_id}/ct_labels/{ct_code}").to(CtLabelsController.class).get().action("show");
    route("/cases/{case_id}/ct_labels/search").to(CtLabelsController.class).post().action("search");
    
    // 号码
    route("/cases/{case_id}/pnums/info").to(PnumsController.class).post().action("numInfo");   
    route("/cases/{case_id}/pnums/comms-on-bps").to(PnumsController.class).post().action("commsOnBps"); 
    
    // 话单数据
    route("/cases/{case_id}/pbills").to(PbillsController.class).get();
    route("/cases/{case_id}/pbills").to(PbillsController.class).post().action("destroy");
    route("/cases/{case_id}/pbills/search").to(PbillsController.class).post().action("search");
    route("/cases/{case_id}/pbills/search-with-cases").to(PbillsController.class).post().action("searchWithCases");
    route("/cases/{case_id}/pbills/upload").to(PbillsController.class).post().action("upload");
    route("/cases/{case_id}/pbills/owner_nums").to(PbillsController.class).get().action("ownerNums");
    route("/cases/{case_id}/pbills/outlier-nums").to(PbillsController.class).get().action("outlierNums");
    route("/cases/{case_id}/pbills/days").to(PbillsController.class).get().action("days");
    route("/cases/{case_id}/pbills/alyz-days").to(PbillsController.class).get().action("alyzDays");
    route("/cases/{case_id}/pbills/geo-dist").to(PbillsController.class).get().action("geoDistOverview");
    route("/cases/{case_id}/pbills/{num}/geo-dist").to(PbillsController.class).get().action("geoDist");
    route("/cases/{case_id}/pbills/{num}/daily-cnt").to(PbillsController.class).get().action("dailyCount");
    route("/cases/{case_id}/pbills/{num}/hourly-cnt").to(PbillsController.class).get().action("hourlyCount");
    route("/cases/{case_id}/pbills/set-ven").to(PbillsController.class).post().action("setVen");
    route("/cases/{case_id}/pbills/set-rel-network").to(PbillsController.class).post().action("setRelNetwork");
    route("/cases/{case_id}/pbills/set-residence").to(PbillsController.class).post().action("setResidence");
    route("/cases/{case_id}/pbills/suggest-owner-nums").to(PbillsController.class).post().action("suggestOwnerNums");
    route("/cases/{case_id}/pbills/only-outliers").to(PbillsController.class).get().action("onlyOutliers");
    route("/cases/{case_id}/pbills/copy/{src_case_id}").to(PbillsController.class).get().action("copy");
    route("/cases/{case_id}/pbills/{num}/related").to(PnumConnectionsController.class).get().action("relatedPbills");
    route("/cases/{case_id}/pbills/{num}/conn-labeled-nums").to(PnumConnectionsController.class).get().action("connectWithLabeledNums");
    
    // 话单记录
    route("/cases/{case_id}/pbills/records/search").to(PbillRecordsController.class).post().action("search");
    route("/cases/{case_id}/pbills/records/count-by").to(PbillRecordsController.class).post().action("countBy");
    route("/cases/{case_id}/pbills/records/count-by-dist").to(PbillRecordsController.class).post().action("countByDist");
    route("/cases/{case_id}/pbills/records/count-by-weekhour").to(PbillRecordsController.class).post().action("countByHourInWeekday");
    route("/cases/{case_id}/pbills/records/copy").to(PbillRecordsController.class).post().action("copy");
    route("/cases/{case_id}/pbills/records/daily-cnt").to(PbillRecordsController.class).post().action("dailyCount");
    route("/cases/{case_id}/pbills/records/conv-ven-nums").to(PbillRecordsController.class).get().action("convertVenNums");
    route("/cases/{case_id}/pbills/records/conv-rel-nums").to(PbillRecordsController.class).get().action("convertRelNums");
    route("/cases/{case_id}/pbills/records/conv-short-nums").to(PbillRecordsController.class).get().action("covertShortNums");
    route("/cases/{case_id}/pbills/records/calc-on-sets").to(PbillRecordsController.class).post().action("calcOnSets");
    route("/cases/{case_id}/pbills/records/same-calls").to(PbillRecordsController.class).post().action("sameCalls");
    
    // 话单记录分析
    route("/cases/{case_id}/pbills/analyze/meets").to(PbillAnalyzeController.class).post().action("meets");    
    route("/cases/{case_id}/pbills/analyze/connections").to(PbillAnalyzeController.class).post().action("connections");
    route("/cases/{case_id}/pbills/analyze/backup-numbers").to(PbillAnalyzeController.class).post().action("backupNums");
    route("/cases/{case_id}/pbills/analyze/in-commons").to(PbillAnalyzeController.class).post().action("findCommons");
    route("/cases/{case_id}/pbills/analyze/find-new").to(PbillAnalyzeController.class).post().action("findNew");
    route("/cases/{case_id}/pbills/analyze/matrix").to(PbillAnalyzeController.class).post().action("matrix");
    route("/cases/{case_id}/pbills/analyze/matrix/drilldown/pbr").to(PbillAnalyzeController.class).post().action("matrixDrilldownPbillRecords");
    route("/cases/{case_id}/pbills/analyze/matrix/drilldown/cn").to(PbillAnalyzeController.class).post().action("matrixDrilldownCommonNums");
    route("/cases/{case_id}/pbills/analyze/rel-cluster").to(PbillAnalyzeController.class).post().action("relCluster");
    route("/cases/{case_id}/pbills/analyze/overlap").to(PbillAnalyzeController.class).post().action("overlap");    
    route("/cases/{case_id}/pbills/analyze/matrix-pbill-records").to(PbillAnalyzeController.class).post().action("matrixPbillRecords");
    route("/cases/{case_id}/pbills/analyze/matrix-common-nums").to(PbillAnalyzeController.class).post().action("matrixCommonNums");
    route("/cases/{case_id}/pbills/analyze/summary/backup-numbers").to(PbillAnalyzeController.class).post().action("summaryBackupNums");
    
    // 人员信息
    route("/citizens").to(CitizensController.class).get();
    route("/citizens/upload").to(CitizensController.class).post().action("upload");
    route("/citizens/maninput").to(CitizensController.class).post().action("maninput");
    route("/cases/{case_id}/citizens/search").to(CitizensController.class).post().action("search");
    route("/citizens/{id}/position").to(CitizensController.class).get().action("position");
    // route("/citizens/do-import").to(CitizensController.class).post().action("doImport");

    // 搜索条件
    route("/cases/{cases_id}/searches").to(SearchesController.class).get();
    route("/cases/{cases_id}/searches").to(SearchesController.class).post().action("create");
    route("/cases/{cases_id}/searches/{id}").to(SearchesController.class).put().action("update");
    route("/cases/{cases_id}/searches/{id}").to(SearchesController.class).delete().action("destroy");
    
    //一对一关系分析
    route("/cases/{case_id}/pbills/mutual/daily_count").to(PbillMutualController.class).post().action("dailyCount");
    route("/cases/{case_id}/pbills/mutual/hour_dist").to(PbillMutualController.class).post().action("hourDist");
    route("/cases/{case_id}/pbills/mutual/week_dist").to(PbillMutualController.class).post().action("weekDist");
    route("/cases/{case_id}/pbills/mutual/duration").to(PbillMutualController.class).post().action("duration");
    route("/cases/{case_id}/pbills/mutual/calls").to(PbillMutualController.class).post().action("calls");
    route("/cases/{case_id}/pbills/mutual/travel").to(PbillMutualController.class).post().action("travel");
    route("/cases/{case_id}/pbills/mutual/in_commons").to(PbillMutualController.class).post().action("inCommons");
    
    // 分类标签
    route("/cases/{case_id}/label_groups/nums").to(LabelGroupsController.class).post().action("nums");
  	route("/cases/{case_id}/label_groups/cell-towers").to(LabelGroupsController.class).post().action("cellTowers");
    
    // 统计
    // 1-计费类型
    route("/cases/{case_id}/pbills/overview/group-by-billtype").to(PbillOverviewReportController.class).post().action("groupByBillType");   
    // 2-联系类型
    route("/cases/{case_id}/pbills/overview/group-by-commdirection").to(PbillOverviewReportController.class).post().action("groupByCommDirection");
    // 3-通话状态
    route("/cases/{case_id}/pbills/overview/group-by-ownernumstatus").to(PbillOverviewReportController.class).post().action("groupByOwnerNumStatus");
    // 4-本方通话地
    route("/cases/{case_id}/pbills/overview/group-by-ownercommloc").to(PbillOverviewReportController.class).post().action("groupByOwnerCommLoc");
    // 5-时间段
    route("/cases/{case_id}/pbills/overview/group-by-durationclass").to(PbillDurationReportController.class).post().action("groupByDurationClass");
    // 6-一周分布
    route("/cases/{case_id}/pbills/overview/group-by-weekday").to(PbillDatetimeReportController.class).post().action("groupByWeekDay");
    // 8-对方通话地
    route("/cases/{case_id}/pbills/overview/group-by-peercommloc").to(PbillDurationReportController.class).post().action("groupByPeerCommLoc");
    // 11-通话时段    
    route("/cases/{case_id}/pbills/overview/group-by-startedtimel1class").to(PbillDurationReportController.class).post().action("groupByStartedTimeL1Class");
    // 12-通话时段(详细)
    route("/cases/{case_id}/pbills/overview/group-by-startedtimel2class").to(PbillDurationReportController.class).post().action("groupByStartedTimeL2Class");
    // 13-通话时段(小时)
    route("/cases/{case_id}/pbills/overview/group-by-startedhourclass").to(PbillDurationReportController.class).post().action("groupByStartedHourClass");
    // 通话时段
    // 14-通话时段vs通话时长
    route("/cases/{case_id}/pbills/overview/group-by-durationclassandstartedtimel1class").to(PbillDurationReportController.class).post().action("groupbyDurationClassAndStartedTimeL1Class");
    // 15-通话时段(详细)vs通话时长
    route("/cases/{case_id}/pbills/overview/group-by-durationclassandstartedtimel2class").to(PbillDurationReportController.class).post().action("groupbyDurationClassAndStartedTimeL2Class");
    // 通话日期
    // 21-日期 //数据未按时间排序
    route("/cases/{case_id}/pbills/overview/group-by-startedday").to(PbillDatetimeReportController.class).post().action("groupbyStartedDay");
    // 22-日期vs通话时间段
    route("/cases/{case_id}/pbills/overview/group-by-starteddayandstartedtimel1class").to(PbillDatetimeReportController.class).post().action("groupByStartedDayAndStartedTimeL1Class");
    // 23-日期vs通话时间段(详细)
    route("/cases/{case_id}/pbills/overview/group-by-starteddayandstartedtimel2class").to(PbillDatetimeReportController.class).post().action("groupByStartedDayAndStartedTimeL2Class");
    // 24-日期vs对方号码
    route("/cases/{case_id}/pbills/overview/group-by-starteddayandandpeernum").to(PbillDatetimeReportController.class).post().action("groupbyStartedDayAndPeerNum");
    // 25-日期vs基站
    route("/cases/{case_id}/pbills/overview/group-by-starteddayandctcode").to(PbillDatetimeReportController.class).post().action("groupbyStartedDayAndCtCode");
    // 31-对方号码
    route("/cases/{case_id}/pbills/overview/group-by-peernum").to(PbillPeerNumReportController.class).post().action("groupByPeerNum");
    // 32-对方号码(排除条件)
    route("cases/{case_id}/pbills/overview/group-by-peernumexclusioncondition").to(PbillPeerNumReportController.class).post().action("groupByPeerNumAndExclusionCondition");
    // 33-对方号码vs通话时长
    route("/cases/{case_id}/pbills/overview/group-by-peernumanddurationclass").to(PbillPeerNumReportController.class).post().action("groupByPeerNumAndDurationClass");
    // 34-对方号码vs通话时段
    route("/cases/{case_id}/pbills/overview/group-by-peernumandstartedtimel1class").to(PbillPeerNumReportController.class).post().action("groupByPeerNumAndStartedTimeL1Class");
    // 35-对方号码vs通话时段(详细)
    route("/cases/{case_id}/pbills/overview/group-by-peernumandstartedtimel2class").to(PbillPeerNumReportController.class).post().action("groupByPeerNumAndStartedTimeL2Class");
    //36-对方号码vs通话时段(小时)
    route("/cases/{case_id}/pbills/overview/group-by-peernumandstartedhourclass").to(PbillPeerNumReportController.class).post().action("groupByPeerNumAndStartedHourClass");
    //40-基站cellId(字段不全)
    route("/cases/{case_id}/pbills/overview/group-by-ownerctcode").to(PbillCellTowerReportController.class).post().action("groupByOwnerCtCode");
    // 41-基站vs通话时段(查询结果字段不全)
    route("/cases/{case_id}/pbills/overview/group-by-codeandstartedtimel1class").to(PbillCellTowerReportController.class).post().action("groupByCodeAndStartedTimeL1Class");
    //42- 基站vs通话时段(详细)
    route("/cases/{case_id}/pbills/overview/group-by-codeandstartedtimel2class").to(PbillCellTowerReportController.class).post().action("groupByCodeAndStartedTimeL2Class");
    //43-基站vs通话时段(小时)(查询结果字段不全)
    route("/cases/{case_id}/pbills/overview/group-by-codeandstartedhourclass").to(PbillCellTowerReportController.class).post().action("groupByCodeAndStartedHourClass");
    //46-小区号lac
    route("/cases/{case_id}/pbills/overview/group-by-ownerlac").to(PbillCellTowerReportController.class).post().action("groupByOwnerLac");
    //基站标注报表
    route("/cases/{case_id}/pbills/overview/group-by-ownernumandstartedhorclass").to(PbillCellTowerReportController.class).post().action("groupByOwnerNumAndStartedHourClass");
    //47-基站vs通话时长(查询结果字段不全)
    route("/cases/{case_id}/pbills/overview/group-by-codeandstarteddurationclass").to(PbillCellTowerReportController.class).post().action("groupByCodeAndDurationClass");
    // 48-本方号码与常用基站与通话时段(每小时)
    route("/cases/{case_id}/pbills/overview/group-by-ownernumandctandstartedhourclass").to(PbillOwnerNumReportController.class).post().action("groupByOwnerNumAndCtAndStartedHourClass");
    //热力图细化
    route("/cases/{case_id}/pbills/r/owner_num/freq-celltower").to(PbillOwnerNumReportController.class).post().action("freqCelltower");
    // 51本方号码(查询结果字段不全)
    route("/cases/{case_id}/pbills/overview/group-by-ownernum").to(PbillOwnerNumReportController.class).post().action("groupByOwnerNum");
    // 52-本方号码vs通话时段
    route("/cases/{case_id}/pbills/overview/group-by-ownernumandstartedtimel1class").to(PbillOwnerNumReportController.class).post().action("groupByOwnerNumAndStartedTimeL1Class");
    // 53-本方号码vs通话时段(详细)
    route("/cases/{case_id}/pbills/overview/group-by-ownernumandstartedtimel2class").to(PbillOwnerNumReportController.class).post().action("groupByOwnerNumAndStartedTimeL2Class");
    // 54-本方号码与通话时段(每小时)
    route("/cases/{case_id}/pbills/overview/group-by-ownernumandstartedhourclass").to(PbillOwnerNumReportController.class).post().action("groupByOwnerNumAndStartedHourClass");
    // 本方号码vs通话时长
    route("/cases/{case_id}/pbills/overview/group-by-ownernumanddurationclass").to(PbillOwnerNumReportController.class).post().action("groupByOwnerNumAndDurationClass");
    
    // 基站
    route("/cases/{case_id}/pbills/cell-towers/daily").to(CellTowersController.class).post().action("daily");
    route("/cases/{case_id}/pbills/cell-towers/top-freq").to(CellTowersController.class).post().action("topFreq");
    route("/cases/{case_id}/pbills/cell-towers/malformed").to(CellTowersController.class).get().action("malformed");
    route("/cases/{case_id}/pbills/cell-towers/suggest").to(CellTowersController.class).post().action("suggest");
    route("/cases/{case_id}/pbills/cell-towers/fix-malformed").to(CellTowersController.class).post().action("fixMalformed");
    route("/cell-towers/loc/transform").to(CellTowersController.class).post().action("transGeoLoc");
    route("/cell-towers/multi-locs").to(CellTowersController.class).post().action("multiGeoLoc");
    route("/cases/{case_id}/pbills/cell-towers/top-hots").to(CellTowersController.class).post().action("topHots");
    route("/cases/{case_id}/pbills/cell-towers/pbill-records").to(CellTowersController.class).post().action("pbillRecords");
    route("/cases/{case_id}/pbills/cell-towers/loc-city-ci").to(CellTowersController.class).post().action("locCityCi");
    
    // ===================================================
    // = 管理员 =
    // ===================================================
    // 用户信息
    route("/admin/accounts/search").to(AccountsController.class).post().action("search");
    route("/admin/accounts").to(AccountsController.class).get().action("index");
    route("/admin/accounts").to(AccountsController.class).post().action("create");
    route("/admin/accounts/reset-passwd").to(AccountsController.class).post().action("resetPasswd");
    route("/admin/accounts/{id}/revoke").to(AccountsController.class).post().action("revoke");
    route("/admin/accounts/locked").to(AccountsController.class).get().action("locked");
    route("/admin/cell_towers/sync").to(app.controllers.admin.CellTowersController.class).post().action("syncPbillCTs");
    route("/admin/missed-ct-requests").to(MissedCtRequestsController.class).get().action("index");
  
    // 软件许可证
    route("/license/installed").to(LicenseController.class).get().action("installed");
    route("/license/show-profile").to(LicenseController.class).get().action("showProfile");
    route("/license/download-profile").to(LicenseController.class).post().action("downloadProfile");
    route("/license/install").to(LicenseController.class).post().action("install");
    route("/license/uninstall").to(LicenseController.class).get().action("uninstall");
    
    route("/admin/licenses/upload-profile").to(LicensesController.class).post().action("uploadProfile");
    route("/admin/licenses").to(LicensesController.class).post().action("create");
    route("/admin/licenses").to(LicensesController.class).get().action("index");
    route("/admin/licenses/{license_id}").to(LicensesController.class).get().action("show");
    route("/admin/licenses/{license_id}").to(LicensesController.class).delete().action("destroy");
    route("/admin/licenses/{license_id}/download").to(LicensesController.class).get().action("download");
    route("/admin/licenses/{license_id}/upgrade").to(LicensesController.class).post().action("upgrade");
    
    // 系统设置
    route("/admin/settings/create").to(SettingsController.class).post().action("create");
    route("/admin/settings/global").to(SettingsController.class).get().action("global");
    route("/admin/settings/account").to(SettingsController.class).get().action("account");
    route("/admin/settings/destroy/{id}").to(SettingsController.class).delete().action("destroy");
    route("/admin/settings/update/{id}").to(SettingsController.class).put().action("update");
    
    // 审核日志
    route("/admin/audit-logs").to(AuditLogsController.class).get().action("index");
    route("/admin/audit-logs/search").to(AuditLogsController.class).post().action("search");
    
    // 后台任务
    route("/cases/{case_id}/connect-cell-towers").to(BackgroundTaskController.class).post().action("connectCellTower");
    route("/cases/{case_id}/connect-citizens").to(BackgroundTaskController.class).post().action("connectCitizen");
    route("/cases/{case_id}/find-outlier-nums").to(BackgroundTaskController.class).post().action("findOutlierNums");
    route("/cases/{case_id}/merge-pbills").to(BackgroundTaskController.class).post().action("mergePbills");
    route("/cases/{case_id}/background_tasks/{id}/progress").to(BackgroundTaskController.class).get().action("progress");
    
    // 工具
    route("/utils/svg/circle/{level}").to(SVGController.class).get().action("circle");
    route("/utils/provinces").to(ChineseCitiesController.class).get().action("provinces");
    route("/utils/provinces/{province_code}/cities").to(ChineseCitiesController.class).get().action("cities");
    
    //数据库备份
    route("/admin/backups/export").to(BackupsController.class).get().action("export");
    route("/admin/backups/restore").to(BackupsController.class).post().action("restore");
    
    // 模拟数据
    route("/mock/conns").to(MockDataController.class).get().action("connections");
    
    route("/super/cell_towers/toxify").to(app.controllers.zuper.CellTowersController.class).get().action("toxify");
    
    //@formatter:on
  }
}