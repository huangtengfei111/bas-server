package app.services.pb;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import app.models.pb.PbillRecord;
import app.models.search.Options;

public interface BackupNumsAnalyzer {
  public static final int ON_SAME_LAC = 1;
  public static final int ON_SAME_CI = 2;
  public static final int ON_CT_DISTANCE = 3;
  
  public Map<String,Collection<PbillRecord>> doAnalyze(Options options,
      List<PbillRecord> pbillRecords);

  public Map summary(List<PbillRecord> pbillRecords);

}