package app.util.license.client;

import de.schlichtherle.license.LicenseContentException;

public class InvalidHostException extends LicenseContentException {

  private static final long serialVersionUID = 3243540531111159504L;
  private String hwPart;

  public InvalidHostException(String resourceKey) {
    super(resourceKey);
  }

  public InvalidHostException(String field, String resourceKey) {
    super(resourceKey);
    this.hwPart = field;
  }

  public String getHwPart() {
    return this.hwPart;
  }
}
