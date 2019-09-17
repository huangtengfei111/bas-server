package app.models;

import org.javalite.activejdbc.Model;
import java.util.List;

/**
 *
 */
public class CitizenAwareModel extends Model {
	public void setCitizenId(int citizenId) {
		set("citizen_id", citizenId);
	}

	public int getCitizenId() {
		return getInteger("citizen_id");
	}
}