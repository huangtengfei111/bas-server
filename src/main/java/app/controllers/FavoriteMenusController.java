package app.controllers;

import java.io.IOException;
import java.util.List;

import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;

import com.alibaba.fastjson.JSONObject;

import app.exceptions.ErrorCodes;
import app.models.FavoriteMenu;

public class FavoriteMenusController extends APIController {

  /**
   * 收藏夹菜单列表显示
   */
  @GET
  public void index() {
    List<FavoriteMenu> menus = FavoriteMenu.findAll().orderBy("id ASC");

    setOkView("list favorite menus");
    view("menus", menus);
    render();
  }

  /**
   * 添加到收藏夹菜单
   * 
   * @throws IOException
   */
  @POST
  public void add() throws IOException {
    String json = getRequestString();
    JSONObject jsonObject = JSONObject.parseObject(json);
    String mkey = jsonObject.getString("mkey");
    Long userId = userIdInSession();

    FavoriteMenu favoriteMenu = new FavoriteMenu();
    favoriteMenu.setMkey(mkey);
    favoriteMenu.setUserId(userId);

    if (favoriteMenu.saveIt()) {
      setOkView("add succeed");
      view("menu", favoriteMenu);
      render("_menu");
    } else {
      setErrorView("save menu failed", 400);
      render("/common/error");
    }
  }

  /**
   * 取消收藏
   * 
   * @throws IOException
   */
  @POST
  public void remove() throws IOException {
    String json = getRequestString();
    JSONObject jsonObject = JSONObject.parseObject(json);
    String mkey = jsonObject.getString("mkey");
    Long userId = userIdInSession();

    int deleted = FavoriteMenu.delete("mkey = ? AND user_id = ?", mkey, userId);
    if (deleted == 0) {
      setErrorView("no such menu", ErrorCodes.NO_RECORD_IN_DB);
      render("/common/error");
    } else {
      setOkView("removed");
      view("id", 0);
      render("/common/ok");
    }
  }

}
