package app.services;

import java.io.IOException;
import java.io.InputStream;

import com.alibaba.fastjson.JSONObject;

import app.models.CitizenBook;

public interface CitizenService {
  public int upload(InputStream inputStream, CitizenBook citizenBook) throws IOException;

  public Long maninput(JSONObject citizenJson);
}
