package app.util.license.client;

import java.io.Serializable;

import de.schlichtherle.license.LicenseContentException;

public class VersionizedParam implements Serializable {

  public static final String V_1 = "1.0";

  private static final long serialVersionUID = -373378029015792287L;
  private static final String VERSION_REQUIRED = "";

  private String version;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void validate() throws LicenseContentException {

    if (this.getVersion() == null) {
      throw new LicenseContentException(VERSION_REQUIRED);
    }
  }
}
