package app.controllers.zuper;

import java.math.BigDecimal;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.javalite.activeweb.annotations.GET;

import app.controllers.APIController;
import app.models.pb.CellTower;
import app.util.ct.CellTowerWitch;

public class CellTowersController extends APIController {

  @GET
  public void toxify() throws Exception {
    int batchSize = 10000;
    long lastId = 0;
    Base.connection().setAutoCommit(false);

    //@formatter:off
    String sql =
        "SELECT * " +
        "FROM cell_towers " +
        "WHERE id > ? AND (lat is not null AND lng is not null) " +
        "ORDER BY id LIMIT ?";
//    String sql =
//        "SELECT * " +
//        "FROM cell_towers " +
//        "WHERE (id BETWEEN 11246 AND 11255) AND xlat is null AND (lat is not null AND lng is not null) " +
//        "ORDER BY id";
    //@formatter:on
    while (true) {
      LazyList<CellTower> cts = CellTower.findBySQL(sql, lastId, batchSize);
      // LazyList<CellTower> cts = CellTower.findBySQL(sql);
      if (cts.size() > 0) {
        Base.openTransaction();
        for (CellTower ct : cts) {
          lastId = ct.getLongId();
          final double lat = ct.getLat().doubleValue();
          final double lng = ct.getLng().doubleValue();
          final double[] ds = CellTowerWitch.toxifyCoord(lng, lat);
//          final String xAddress = toxifyAddress(ct.getAddress());
          ct.setXLat(new BigDecimal(ds[1]));
          ct.setXLng(new BigDecimal(ds[0]));
//          ct.setXaddr(xAddress);
          ct.saveIt();
        }
        Base.commitTransaction();
      } else {
        break;
      }
    }
    Base.connection().setAutoCommit(true);
  }

}
