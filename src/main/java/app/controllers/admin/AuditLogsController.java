package app.controllers.admin;

import java.util.List;

import org.javalite.activejdbc.Paginator;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

import app.controllers.APIController;
import app.models.AuditLog;
import app.util.UniversalQueryHelper;

public class AuditLogsController extends APIController {

  /**
   * 审计日志列表显示
   */
	@GET
	public void index() {

    // 获取前端传过来的分页信息,如:当前页,页面大小
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));

    // 用Paginator类去查询对应的分页数据
    Paginator<AuditLog> p = new Paginator<>(AuditLog.class, pageSize, "1 = 1");

    // 获取当前页面的所有数据
    List<AuditLog> auditLogs = p.getPage(currentPage);
    
    setOkView();

    view("page_total", p.pageCount(), 
         "page_current", currentPage,
         "audit_logs", auditLogs);
    render();

	}

  /**
   * 审计日志搜索
   * 
   * @throws Exception
   */
  @POST
  public void search() throws Exception {
    // 获取前端传过来的数据及分页信息,如:当前页,页面大小
    String json = getRequestString();
    int currentPage = getCurrentPage(param("page"));
    int pageSize = getPageSize(param("pagesize"));

    List<Object> sqlAndVals = UniversalQueryHelper.getSqlAndBoundVals(json);
    String sqlCond = (String) sqlAndVals.get(0);
    List vals = (List) sqlAndVals.get(1);
    Object[] params = vals.stream().toArray(Object[]::new);

    // 用Paginator类去查询符合条件的分页数据
    Paginator<AuditLog> paginator =
        new Paginator<>(AuditLog.class, pageSize, sqlCond, params);
    
    
    // 获取当前页面的所有数据
    List<AuditLog> auditLogs = paginator.getPage(currentPage);
    
    setOkView();

    view("page_total", paginator.pageCount(), 
         "page_current", currentPage,
         "audit_logs", auditLogs);
    render("index");

  }
}