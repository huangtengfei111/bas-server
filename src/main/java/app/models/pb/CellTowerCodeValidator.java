package app.models.pb;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.ValidatorAdapter;

public class CellTowerCodeValidator extends ValidatorAdapter {

  @Override
  public void validate(Model m) {
    boolean valid = true;
    // perform whatever validation logic, then add errors to model if validation did
    // not pass:
    if (!valid)
      m.addValidator(this, "custom_error");
  }

}
