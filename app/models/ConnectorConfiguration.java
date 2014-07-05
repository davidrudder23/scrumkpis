package models;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Ebean;

import play.Logger;
import play.db.ebean.Model;
import utils.StringUtils;

@Entity
public class ConnectorConfiguration extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3037423067803504518L;

	@Id
	public Long id;
	
	public String connectorName;
	
	public String name;
	
	public String value;
	
	@ManyToOne
	public ScrumMaster scrumMaster;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public static String getValue(ScrumMaster scrumMaster, String connectorName, String name) {
		if (StringUtils.isEmpty(connectorName)) {
			return null;
		}
		
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		
		ConnectorConfiguration config = find.where().eq("scrumMaster", scrumMaster).eq("connectorName", connectorName).eq("name", name).findUnique();
		if (config == null) {
			return null;
		}
		
		return config.value;
	}
	
	public static void setValue (ScrumMaster scrumMaster, String connectorName, String name, String value) {
		if (StringUtils.isEmpty(connectorName)) {
			return;
		}
		
		if (StringUtils.isEmpty(name)) {
			return;
		}

		if (value == null) {
			return;
		}
		
		ConnectorConfiguration config = find.where().eq("scrumMaster", scrumMaster).eq("connectorName", connectorName).eq("name", name).findUnique();
		if (config == null) {
			config = new ConnectorConfiguration();
			config.connectorName = connectorName;
			config.name = name;
			config.scrumMaster = scrumMaster;
			Logger.debug("Setting config "+config.id+" value to "+value);
			config.value = value;			
			Logger.debug("Saving "+config.value);
			config.save();
		} else {
			Logger.debug("Setting config "+config.id+" value to "+value);
			config.value = value;			
			Logger.debug("Saving "+config.value);
			Ebean.update(config, new HashSet<String>(Arrays.asList("value")));
		}
		
	}
	
	public static Finder<Long, ConnectorConfiguration> find = new Finder<Long, ConnectorConfiguration>(Long.class, ConnectorConfiguration.class);
}
