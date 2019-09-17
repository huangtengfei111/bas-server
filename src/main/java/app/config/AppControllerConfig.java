package app.config;


import org.javalite.activeweb.AbstractControllerConfig;
import org.javalite.activeweb.AppContext;
import org.javalite.activeweb.controller_filters.DBConnectionFilter;

import app.controllers.BackgroundTaskController;
import app.controllers.CaseBreakpointsController;
import app.controllers.CaseEventsController;
import app.controllers.CasesController;
import app.controllers.CitizensController;
import app.controllers.FavoriteMenusController;
import app.controllers.LabelGroupsController;
import app.controllers.LoginController;
import app.controllers.SearchesController;
import app.controllers.TrackApiController;
import app.controllers.UserController;
import app.controllers.admin.AccountsController;
import app.controllers.admin.AuditLogsController;
import app.controllers.admin.BackupsController;
import app.controllers.admin.LicenseController;
import app.controllers.admin.MissedCtRequestsController;
import app.controllers.admin.SettingsController;
import app.controllers.filters.CaseOwnershipFilter;
import app.controllers.filters.CatchAllFilter;
import app.controllers.filters.LogActionFilter;
import app.controllers.filters.RequiredLicenseFilter;
import app.controllers.filters.RequiredRoleFilter;
import app.controllers.filters.TrackApiFilter;
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
import app.controllers.pb.PubServiceNumsController;
import app.controllers.pb.RelNumbersController;
import app.controllers.pb.VenNumbersController;
import app.controllers.zuper.LicensesController;
import app.models.Role;

/**
 * @author
 */

@SuppressWarnings("unchecked")
public class AppControllerConfig extends AbstractControllerConfig {


  public void init(AppContext context) {
    RequiredRoleFilter requireSuperFilter = new RequiredRoleFilter(Role.SUPER);
    RequiredRoleFilter requireAdminFilter = new RequiredRoleFilter(Role.ADMIN);
    RequiredLicenseFilter licFilter = new RequiredLicenseFilter();

    LogActionFilter logActionFilter = new LogActionFilter();

    add(new CatchAllFilter());
    // add(new StatisticsFilter());

    //@formatter:off
    add(/*requireAdminFilter, */new DBConnectionFilter()).to(SettingsController.class);
    add(new DBConnectionFilter()/*, requireAdminFilter*/).to(AccountsController.class);
    add(requireAdminFilter, new DBConnectionFilter()).to(AuditLogsController.class);
    add(/* requireSuperFilter, */new DBConnectionFilter()).to(LicensesController.class);
    
  	add(new DBConnectionFilter()).to(LoginController.class);
  	add(new DBConnectionFilter(), new CaseOwnershipFilter()).to(PbillsController.class);
    add(new DBConnectionFilter(), new CaseOwnershipFilter()).to(PbillRecordsController.class);
    add(new DBConnectionFilter(), new CaseOwnershipFilter()).to(PbillAnalyzeController.class);
    add(new DBConnectionFilter()).to(PbillOverviewReportController.class);
  	add(new DBConnectionFilter()).to(RelNumbersController.class);
  	add(new DBConnectionFilter()).to(VenNumbersController.class);
  	add(new DBConnectionFilter()).to(PnumLabelsController.class);
  	add(new DBConnectionFilter()).to(CtLabelsController.class);
  	add(new DBConnectionFilter(), logActionFilter).to(CitizensController.class).forActions("search");
  	add(new DBConnectionFilter()).to(CitizensController.class).excludeActions("search");;
    
  	add(new DBConnectionFilter()).to(SearchesController.class);
    add(new DBConnectionFilter(), new TrackApiFilter()).to(CasesController.class).forActions("summary");;
    add(new DBConnectionFilter()).to(CasesController.class).excludeActions("summary");;
    add(new DBConnectionFilter()).to(CaseEventsController.class);
    add(new DBConnectionFilter()).to(CaseBreakpointsController.class);
    add(new DBConnectionFilter()).to(CellTowersController.class);

    add(new DBConnectionFilter()).to(PbillDurationReportController.class);
    add(new DBConnectionFilter()).to(PbillDatetimeReportController.class);
    add(new DBConnectionFilter()).to(PbillOwnerNumReportController.class);
    add(new DBConnectionFilter()).to(PbillPeerNumReportController.class);
    add(new DBConnectionFilter()).to(PbillCellTowerReportController.class);
    add(new DBConnectionFilter()).to(FavoriteMenusController.class);
    add(new DBConnectionFilter()).to(LicenseController.class);
    add(new DBConnectionFilter()).to(BackgroundTaskController.class);
    add(new DBConnectionFilter()).to(PubServiceNumsController.class);
    add(new DBConnectionFilter()).to(PbillMutualController.class);
    add(new DBConnectionFilter()).to(LabelGroupsController.class);
    add(new DBConnectionFilter()).to(PnumConnectionsController.class);
    add(new DBConnectionFilter()).to(app.controllers.admin.CellTowersController.class);
    add(new DBConnectionFilter()).to(app.controllers.zuper.CellTowersController.class);
    add(new DBConnectionFilter()).to(PnumsController.class);
    add(new DBConnectionFilter()).to(UserController.class);
    add(new DBConnectionFilter()).to(BackupsController.class);
    add(new DBConnectionFilter()).to(TrackApiController.class);
    
    add(new DBConnectionFilter()).to(MissedCtRequestsController.class);
    //@formatter:on
  }
}
