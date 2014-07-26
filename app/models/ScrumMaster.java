package models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.Constraint;

import play.Logger;
import play.db.ebean.Model;
import utils.StringUtils;

@Entity
public class ScrumMaster extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6931346899946386981L;

	@Id
	public Long id;

	public String firstName;
	public String lastName;
	public String email;

	public String hashedPassword;
	public String salt;
	
	public int sprintLengthInDays = 14;
	
	@OneToMany(mappedBy="scrumMaster")
	public Set<Sprint> sprints;

	public static String hashPassword(String email, String salt, String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-512");
			digest.update(email.getBytes());
			digest.update(":".getBytes());
			digest.update(salt.getBytes());
			digest.update(":".getBytes());
			digest.update(password.getBytes());

			return StringUtils.hexEncode(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			Logger.warn(e.getMessage(), e);
		}
		return null;
	}

	public static String generateSalt() {
		// TODO
		return "SALT";
	}
	
	public List<Sprint> getSprints() {
		List<Sprint> sprints = Sprint.find.where().eq("scrumMaster", this).findList();
		return sprints;
	}

	public static Finder<Long, ScrumMaster> find = new Finder<Long, ScrumMaster>(Long.class, ScrumMaster.class);

}
