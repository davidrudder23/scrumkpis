CREATE TABLE `jira_bookmark` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) DEFAULT NULL,
  `jira_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_jira_bookmark_employee_2` (`employee_id`),
  CONSTRAINT `fk_jira_bookmark_employee_2` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;
