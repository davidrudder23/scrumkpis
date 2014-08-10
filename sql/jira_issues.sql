CREATE TABLE `jira_issue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `author_name` varchar(255) DEFAULT NULL,
  `author_email` varchar(255) DEFAULT NULL,
  `resolution_sprint_id` bigint(20) DEFAULT NULL,
  `jira_key` varchar(255) DEFAULT NULL,
  `summary` varchar(255) DEFAULT '',
  `resolver_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_jira_issue_resolution_sprint_1` (`resolution_sprint_id`),
  KEY `ix_jira_issue_resolver_1` (`resolver_id`),
  CONSTRAINT `fk_jira_issue_resolution_sprint_1` FOREIGN KEY (`resolution_sprint_id`) REFERENCES `sprint` (`id`),
  CONSTRAINT `fk_jira_issue_resolver_1` FOREIGN KEY (`resolver_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6148 DEFAULT CHARSET=latin1;
