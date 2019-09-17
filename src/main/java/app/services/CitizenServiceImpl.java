package app.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import app.models.Citizen;
import app.models.CitizenAddress;
import app.models.CitizenBook;
import app.models.CitizenPhone;
import app.util.PubUtilitiesFileHelper;

public class CitizenServiceImpl implements CitizenService {

  private static final Logger log = LoggerFactory.getLogger(CitizenServiceImpl.class);
  private static final int PHONE_INFO = 1;
  private static final int ADDRESS_INFO = 2;

  @Override
  public Long maninput(JSONObject citizenJson) {
    String name = citizenJson.getString("name");
    String position = citizenJson.getString("position");
    String company = citizenJson.getString("company");
    String category = citizenJson.getString("category");
    String version = citizenJson.getString("version");
    JSONArray citizenPhones = citizenJson.getJSONArray("citizen_phones");
    JSONArray citizenAddresses = citizenJson.getJSONArray("citizen_addresses");
    if (version == null) {
      version = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    log.debug("version", version);

    Citizen citizen = new Citizen();
    citizen.setName(name);
    citizen.setPosition(position);
    citizen.setCompany(company);
    citizen.setCategory(category);
    citizen.setVersion(version);
    citizen.setCitizenBookId(-1L);

    try {
      Base.openTransaction();
      if (citizen.saveIt()) {
        if (citizenPhones != null) {
          for (int i = 0; i < citizenPhones.size(); i++) {
            JSONObject citizenPhoneJson = citizenPhones.getJSONObject(i);
            CitizenPhone citizenPhone = citizenPhoneJson.toJavaObject(CitizenPhone.class);
            citizenPhone.setCitizenBookid(-1L);
            citizen.add(citizenPhone);
          }
        }
        if (citizenAddresses != null) {
          for (int i = 0; i < citizenAddresses.size(); i++) {
            JSONObject citizenAddressJson = citizenAddresses.getJSONObject(i);
            CitizenAddress citizenAddress = citizenAddressJson.toJavaObject(CitizenAddress.class);
            citizenAddress.setCitizenBookid(-1L);
            citizen.add(citizenAddress);
          }
        }
        Base.commitTransaction();
        return citizen.getLongId();
      } else {
        Base.rollbackTransaction();
        return -1L;
      }
    } catch (Exception e) {
      Base.rollbackTransaction();
      log.error(e.getMessage(), e);
      return -1L;
    }
  }

  private Map<String, Object> maninput(Map map) {
    Map<String, Object> m = new LinkedHashMap<String, Object>();
    String name = (String) map.get("name");
    m.put("name", name);
    Object position = map.get("position");
    m.put("position", position);
    Object company = map.get("company");
    m.put("company", company);
    Object position_as = map.get("position_as");
    m.put("position_as", position_as);
    Object area_code = map.get("area_code");
    m.put("area_code", area_code);
//  Object banbennum = map.get("banbennum");
    Object ven_num = map.get("ven_num");
    m.put("ven_num", ven_num);
    Object ven_name = map.get("ven_name");
    m.put("ven_name", ven_name);
    Citizen citizen = new Citizen();
    citizen.fromMap(m);
    citizen.saveIt();
    long id = (Long) citizen.getId();

    Object phone = map.get("phone");
    CitizenPhone citizenPhone1 = new CitizenPhone();
    citizenPhone1.set("citizen_id", id);
    citizenPhone1.set("num", phone);
    citizenPhone1.set("memo", "办电");
    citizenPhone1.saveIt();
    String mobile = (String) map.get("mobile");
    CitizenPhone citizenPhone2 = new CitizenPhone();
    citizenPhone2.set("citizen_id", id);
    citizenPhone2.set("num", mobile);
    citizenPhone2.set("memo", "手机");
    citizenPhone2.insert();
//    String ven_num = (String)map.get("ven_num");
//    CitizenPhone citizenPhone3 = new CitizenPhone();
//    citizenPhone3.set("citizen_id", id);
//    citizenPhone3.set("num", ven_num);
//    citizenPhone3.set("memo", "短号");
//    citizenPhone3.insert();
    String zhainum = (String) map.get("zhainum");
    CitizenPhone citizenPhone4 = new CitizenPhone();
    citizenPhone4.set("citizen_id", id);
    citizenPhone4.set("num", zhainum);
    citizenPhone4.set("memo", "宅电");
    citizenPhone4.insert();
    String mobile3 = (String) map.get("mobile3");
    CitizenPhone citizenPhone5 = new CitizenPhone();
    citizenPhone5.set("citizen_id", id);
    citizenPhone5.set("num", mobile3);
    citizenPhone5.set("memo", "手机3");
    citizenPhone5.insert();
    String phone2 = (String) map.get("phone2");
    CitizenPhone citizenPhone6 = new CitizenPhone();
    citizenPhone6.set("citizen_id", id);
    citizenPhone6.set("num", phone2);
    citizenPhone6.set("memo", "办电2");
    citizenPhone6.insert();

    String loc = (String) map.get("loc");
    CitizenAddress citizenAddress = new CitizenAddress();
    citizenAddress.set("citizen_id", id);
    citizenAddress.set("loc", loc);
    citizenAddress.set("memo", "区县");
    citizenAddress.insert();
    return map;
  }

  @Override
  public int upload(InputStream inputStream, CitizenBook citizenBook) throws IOException {

    Long citizenBookId = citizenBook.getLongId();
    String category = citizenBook.getCategory();
    String version = citizenBook.getVersion();
    String phoneSql = "INSERT INTO citizen_phones SET citizen_id = ?,citizen_book_id = ?, num = ?, memo = ?, ven_name = ?,created_at = NOW(),updated_at = NOW()";
    String addrSql = "INSERT INTO citizen_addresses SET citizen_id = ?,citizen_book_id = ?, loc = ?, memo = ?, created_at = NOW(),updated_at = NOW()";

    Map<String, String> header = new HashMap<>();
    header.put("身份证", "social_no");
    header.put("姓名", "name");
    header.put("单位", "company");
    header.put("职务", "position");
    header.put("虚拟网", "phone.ven_name");
    header.put("^号码", "phone.num");
    header.put("^地址", "address.loc");
    header.put("^短号", "phone.ven_num");

    List<Citizen> citizens = PubUtilitiesFileHelper.read(header, inputStream, 1);

    for (Citizen citizen : citizens) {
      citizen.setCitizenBookId(citizenBookId);
      citizen.setCategory(category);
      citizen.setVersion(version);
      if (!citizen.saveIt()) {
        continue;
      }

      Base.openTransaction();
      PreparedStatement phonePs = Base.startBatch(phoneSql);
      PreparedStatement addrPs = Base.startBatch(addrSql);


      List<CitizenPhone> phones = citizen._getPhones();
      List<CitizenAddress> addresses = citizen._getAddresses();
      log.debug("phones : {}", phones);
      if (phones != null && phones.size() > 0) {
        for (CitizenPhone phone : phones) {
          if (phone.isVenNum()) {
            Base.addBatch(phonePs, citizen.getId(), citizenBookId, phone.getNum(), phone.getMemo(), phone.getVenName());
          } else {
            Base.addBatch(phonePs, citizen.getId(), citizenBookId, phone.getNum(), phone.getMemo(), null);
          }
        }

      }

      if (addresses != null && addresses.size() > 0) {
        for (CitizenAddress address : addresses) {
          Base.addBatch(addrPs, citizen.getId(), citizenBookId, address.getLoc(), address.getMemo());
        }
      }

      Base.executeBatch(phonePs);
      Base.executeBatch(addrPs);
      Base.commitTransaction();
    }

    return citizens.size();
  }

}
