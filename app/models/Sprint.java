package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
public class Sprint extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3529154498300657232L;

	@Id
	public Long id;
	
	public String name;

	@Column(length = 2048)
	public String description;

	public Date startDate;

	public Boolean active = false;

	public List<Employee> employees;
	
	@ManyToOne
	public ScrumMaster scrumMaster;

	public static Finder<Long, Sprint> find = new Finder<Long, Sprint>(Long.class, Sprint.class);

}
