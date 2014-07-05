package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class ConnectorData extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	public Long id;
	public String name;
	
	public String value;
	
	public static Finder<Long, ConnectorData> find = new Finder<Long, ConnectorData>(Long.class, ConnectorData.class);

}
