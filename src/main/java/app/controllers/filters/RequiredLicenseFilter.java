package app.controllers.filters;

import org.javalite.activeweb.controller_filters.HttpSupportFilter;

import app.util.license.client.BASLicenseManager;
import app.util.license.client.LicenseClientParam;

public class RequiredLicenseFilter extends HttpSupportFilter {

  @Override
  public void before() {
    LicenseClientParam param = BASLicenseManager.loadClientParam();
    if (param != null) {
      BASLicenseManager licenseManager = new BASLicenseManager(param);
      try {
        licenseManager.verify();
      } catch (Exception e) {
        throw new RuntimeException();
      }
    } else {
      throw new RuntimeException();
    }
  }

}
