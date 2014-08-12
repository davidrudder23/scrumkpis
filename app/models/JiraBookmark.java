package models;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.ebean.Model;
import utils.StringUtils;

@Entity
public class JiraBookmark extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4090140235165671986L;

	@Id
	public Long id;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne
	public Employee employee;
	
	public String jiraKey;
	
	public static Finder<Long, JiraBookmark> find = new Finder<Long, JiraBookmark>(Long.class, JiraBookmark.class);
	
	
}
