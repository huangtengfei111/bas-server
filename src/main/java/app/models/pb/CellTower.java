package app.models.pb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.javalite.activejdbc.LazyList;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Rectangle;

import app.models.CaseAwareModel;
import app.util.ct.CoordinateTransformUtil;
import io.vavr.Tuple3;

/**
 *
 */
public class CellTower extends CaseAwareModel {
  public static final Integer COMPLEMENT_SAME_CT_CODE = 1;

	public CellTower() {
	}
	
  public CellTower(int mcc, Long mnc, Long lac, Long ci) {
	  setMcc(mcc);
	  setMnc(mnc);
	  setLac(lac);
	  setCi(ci);
	}

  /**
   * 
   * @param radius
   * @return
   */
  public LazyList<CellTower> nearby(Double radius) {
    double lng = this.getLng().doubleValue();
    double lat = this.getLat().doubleValue();
    return nearby(lat, lng, radius);
  }

	public static LazyList<CellTower> nearby(Double lat, Double lng, Double radius) {
    SpatialContext geo = SpatialContext.GEO;
    Rectangle rectangle =
        geo.getDistCalc().calcBoxByDistFromPt(geo.makePoint(lng, lat), radius * DistanceUtils.KM_TO_DEG, geo, null);

//    System.out.println(rectangle.getMinX() + " - " + rectangle.getMaxX());  // 经度范围   
//    System.out.println(rectangle.getMinY() + " - " + rectangle.getMaxY());  // 纬度范围   
    
    //@formatter:off
    String sql = "SELECT * FROM cell_towers WHERE (lng BETWEEN ? and ?) AND (lat BETWEEN ? and ?)";
    //@formatter:on
    return CellTower.findBySQL(sql, rectangle.getMinX(), rectangle.getMaxX(), rectangle.getMinY(), rectangle.getMaxY());
  
	}

  public static CellTower findOne(String code, int fmt) {
    if (code == null)
      return null;

    String[] segs = code.split(":");
    if (segs.length > 2) {
      long lacDec = Long.parseLong(segs[0], fmt);
      long ciDec = Long.parseLong(segs[1], fmt);
      long mncDec = Long.parseLong(segs[2], fmt);
      return CellTower.findFirst("mnc = ? AND lac = ? AND ci = ?", mncDec,
          lacDec, ciDec);
    }
    return null;
  }

  public static CellTower findOne(Tuple3<Long, Long, Long> tuple) {
    Long mnc = tuple._1;
    Long lac = tuple._2;
    Long ci = tuple._3;
    return CellTower.findFirst("mnc = ? AND lac = ? AND ci = ?", mnc, lac, ci);
  }

  public static List<Object> normalize(String code, int fmt) {
    if (code == null)
      return null;

    String[] segs = code.split(":");
    if (segs.length > 2) {
      List<Object> l = new ArrayList<>();
      long lacDec = Long.parseLong(segs[0], fmt);
      long ciDec = Long.parseLong(segs[1], fmt);
      Long mncDec = Long.parseLong(segs[2], fmt);
      l.add(mncDec);
      l.add(lacDec);
      l.add(ciDec);
      return l;
    }
    return null;
  }

  public void upsert() {
    CellTower one = CellTower.findFirst("mnc = ? AND lac = ? AND ci = ?", getMnc(), getLac(), getCi());
    if (one != null) {
      one.copyFrom(this);
    } else {
      one = this;
    }
    one.saveIt();
  }

  public static String normalizeCode(String code) {
    List<Object> normalize = normalize(code, 16);
    if (normalize != null) {
      long mnc = (long) normalize.get(0);
      long lac = (long) normalize.get(1);
      long ci = (long) normalize.get(2);
      StringBuilder codeBuilder = new StringBuilder();
      codeBuilder.append(Long.toHexString(lac)).append(":")
          .append(Long.toHexString(ci)).append(":")
          .append(Long.toHexString(mnc));
      code = codeBuilder.toString().toUpperCase();
      return code;
    }
    return null;
  }

  public void setMcc(int mcc) {
	  set("mcc", mcc);
	}
	
  public void setMnc(Long mnc) {
    setLong("mnc", mnc);
	}
	
  public void setLac(Long lac) {
	  set("lac", lac);
	}
	
  public void setCi(Long ci) {
	  set("ci", ci);
	}
	
  public void setCoord(BigDecimal lat, BigDecimal lng) {
    set("lat", lat);
    set("lng", lng);
  }

  public void setGCoord(BigDecimal glat, BigDecimal glng) {
    set("glat", glat);
    set("glng", glng);
  }

  public void setBCoord(BigDecimal blat, BigDecimal blng) {
    set("blat", blat);
    set("blng", blng);
  }

	public void setLat(BigDecimal lat) {
	  set("lat", lat);
	}

  public double[] getBasCoord() {
    double lng = getBigDecimal("lng").doubleValue();
    double lat = getBigDecimal("lat").doubleValue();
    double[] c = CoordinateTransformUtil.gcj02tobd09(lng, lat);
    return c;
  }

  public BigDecimal getLat() {
    return getBigDecimal("lat");
  }

	public void setLng(BigDecimal lng) {
	  set("lng", lng);
	}

  public BigDecimal getLng() {
    return getBigDecimal("lng");
  }

  public BigDecimal getXLat() {
    return getBigDecimal("xlat");
  }

  public BigDecimal getXLng() {
    return getBigDecimal("xlng");
  }

  public void setGLat(BigDecimal glat) {
    set("glat", glat);
	}

  public void setGLng(BigDecimal glng) {
    set("glng", glng);
  }

  public void setBLat(BigDecimal blat) {
    set("blat", blat);
  }

  public void setBLng(BigDecimal blng) {
    set("blng", blng);
  }

  public void setXLat(BigDecimal xlat) {
    set("xlat", xlat);
  }

  public void setXLng(BigDecimal xlng) {
    set("xlng", xlng);
  }

  public void setProvince(String province) {
    set("province", province);
  }

  public String getCity() {
    return getString("city");
  }

  public void setCity(String city) {
    set("city", city);
  }

  public String getDistrict() {
    return getString("district");
  }

  public String getTown() {
    return getString("town");
  }

  public void setXaddr(String address) {
    set("xaddr", address);
  }

  public String getXaddr() {
    return getString("xaddr");
  }

	public void setAddress(String address) {
	  set("addr", address);
	}

  public String getAddress() {
    return getString("addr");
  }

  public void setSource(String source) {
    set("source", source);
  }
  
  public void setDistrict(String district) {
    set("district", district);
  }
  
  public void setTown(String town) {
    set("town", town);
  }
  
  public String getCode() {
    String code = getString("code");
    if (code == null) {
      code = getHexLac() + ":" + getHexCi() + ":" + getHexMnc();
    }
    return code;
  }

  public Long getLac() {
    return getLong("lac");
  }

  public Long getCi() {
    return getLong("ci");
  }

  public Long getMnc() {
    return getLong("mnc");
  }

  public String getHexLac() {
    return Long.toHexString(getLac());
  }

  public String getHexCi() {
    return Long.toHexString(getCi());
  }

  public String getHexMnc() {
    return Long.toHexString(getMnc());
  }

  public Long getMcc() {
    return getLong("mcc");
  }

}