import models.ScrumMaster;
import models.Sprint;
import play.Application;
import play.GlobalSettings;


public class Global extends GlobalSettings {
    public void onStart(Application app) {
        int numScrumMasters = ScrumMaster.find.findRowCount();
        if (numScrumMasters<=0) {
        	ScrumMaster scrumMaster = new ScrumMaster();
        	scrumMaster.email = "david.rudder@doortodoororganics.com";
        	scrumMaster.firstName = "David";
        	scrumMaster.lastName = "Rudder";
        	scrumMaster.salt = ScrumMaster.generateSalt();
        	scrumMaster.hashedPassword = ScrumMaster.hashPassword(scrumMaster.email, scrumMaster.salt, "password");
        	scrumMaster.save();
        }
        ScrumMaster scrumMaster = ScrumMaster.find.all().get(0);

        int numSprints = Sprint.find.findRowCount();
        if (numSprints<=0) {
        	Sprint sprint = new Sprint();
        	sprint.active = true;
        	sprint.name = "0.1";
        	sprint.description = "Auto-generated template";
        	sprint.scrumMaster = scrumMaster;
        	sprint.save();
        }
        
    }
}
