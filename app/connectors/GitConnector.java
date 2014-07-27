package connectors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import play.Logger;
import utils.StringUtils;
import models.ConnectorConfiguration;
import models.Employee;
import models.GitCommit;
import models.ScrumMaster;
import models.Sprint;

public class GitConnector extends Connector {

	public void run(ScrumMaster scrumMaster) {
		String filePath = ConnectorConfiguration.getValue(scrumMaster, getName(), "path");
		if (StringUtils.isEmpty(filePath)) {
			filePath = "/home/drig/workspace/home/.git";
		}
		FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
		Repository repo;
		try {
			repo = repoBuilder.setGitDir(new File(filePath)).readEnvironment().findGitDir().build();
			Git git = new Git(repo);
			PullCommand pullCommand = git.pull();
			PullResult pullResult = pullCommand.call();
			Logger.debug("Pull result=" + pullResult.hashCode());

			LogCommand logCommand = git.log();
			Iterable<RevCommit> revCommits = logCommand.all().call();

			List<Sprint> activeSprints = Sprint.find.where().eq("scrumMaster", scrumMaster).eq("locked", false).findList();
			long sprintLength = scrumMaster.sprintLengthInDays * 24 * 60 * 60 * 1000;
			for (RevCommit revCommit : revCommits) {
				for (Sprint sprint : activeSprints) {
					Logger.debug(sprint.toString());
					long sprintStart = sprint.startDate.getTime();
					long commitTime = ((long) revCommit.getCommitTime()) * 1000;

					if (commitTime >= sprintStart && commitTime <= (sprintStart + sprintLength)) {
						String commitId = revCommit.getName();
						String employeeEmail = revCommit.getCommitterIdent().getEmailAddress();

						Employee employee = null;
						if (!StringUtils.isEmpty(employeeEmail)) {
							for (Employee sprintEmployee : sprint.getEmployees()) {
								if (sprintEmployee.email.equalsIgnoreCase(employeeEmail)) {
									employee = sprintEmployee;
								}
							}
						}
						if (GitCommit.find.where().eq("sprint", sprint).eq("commitId", commitId).findRowCount() <= 0) {
							GitCommit commit = new GitCommit();
							commit.sprint = sprint;
							commit.commitId = commitId;
							commit.message = revCommit.getFullMessage();
							commit.commiterEmail = employeeEmail;
							commit.repoName = revCommit.toString();
							commit.date = new Date(commitTime);
							if (employee != null) {
								commit.employee = employee;
							}
							commit.save();
						}
					}

				}
			}
		} catch (Exception e) {
			Logger.warn("Could not run git connector", e);
		}
	}

	public List<String> getParameterNames() {
		List<String> paramNames = new ArrayList<String>();
		paramNames.add("path");
		paramNames.add("gitURL");
		return paramNames;
	}

	public String getName() {
		return "Git";
	}

}
