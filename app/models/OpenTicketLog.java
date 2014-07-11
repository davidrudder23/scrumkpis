package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
public class OpenTicketLog extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2678269898929021667L;
	public static Finder<Long, OpenTicketLog> find = new Finder<Long, OpenTicketLog>(Long.class, OpenTicketLog.class);

	@Id
	public Long id;
	
	@ManyToOne
	public ScrumMaster scrumMaster;
	public Date date;
	
	public int numOpenTickets;
	
}
