# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table connector_configuration (
  id                        bigint auto_increment not null,
  connector_name            varchar(255),
  name                      varchar(255),
  value                     varchar(255),
  scrum_master_id           bigint,
  constraint pk_connector_configuration primary key (id))
;

create table connector_data (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  value                     varchar(255),
  constraint pk_connector_data primary key (id))
;

create table employee (
  id                        bigint auto_increment not null,
  scrum_master_id           bigint,
  first_name                varchar(255),
  last_name                 varchar(255),
  email                     varchar(255),
  jira_id                   varchar(255),
  description               varchar(255),
  default_story_points_per_sprint integer,
  constraint pk_employee primary key (id))
;

create table employee_sprint (
  id                        bigint auto_increment not null,
  employee_id               bigint,
  sprint_id                 bigint,
  story_points_available    integer,
  story_points_completed    integer,
  num_reopens               integer,
  constraint pk_employee_sprint primary key (id))
;

create table scrum_master (
  id                        bigint auto_increment not null,
  first_name                varchar(255),
  last_name                 varchar(255),
  email                     varchar(255),
  hashed_password           varchar(255),
  salt                      varchar(255),
  constraint pk_scrum_master primary key (id))
;

create table sprint (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  description               varchar(2048),
  start_date                datetime,
  locked                    tinyint(1) default 0,
  scrum_master_id           bigint,
  constraint pk_sprint primary key (id))
;

alter table connector_configuration add constraint fk_connector_configuration_scrumMaster_1 foreign key (scrum_master_id) references scrum_master (id) on delete restrict on update restrict;
create index ix_connector_configuration_scrumMaster_1 on connector_configuration (scrum_master_id);
alter table employee add constraint fk_employee_scrumMaster_2 foreign key (scrum_master_id) references scrum_master (id) on delete restrict on update restrict;
create index ix_employee_scrumMaster_2 on employee (scrum_master_id);
alter table employee_sprint add constraint fk_employee_sprint_employee_3 foreign key (employee_id) references employee (id) on delete restrict on update restrict;
create index ix_employee_sprint_employee_3 on employee_sprint (employee_id);
alter table employee_sprint add constraint fk_employee_sprint_sprint_4 foreign key (sprint_id) references sprint (id) on delete restrict on update restrict;
create index ix_employee_sprint_sprint_4 on employee_sprint (sprint_id);
alter table sprint add constraint fk_sprint_scrumMaster_5 foreign key (scrum_master_id) references scrum_master (id) on delete restrict on update restrict;
create index ix_sprint_scrumMaster_5 on sprint (scrum_master_id);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table connector_configuration;

drop table connector_data;

drop table employee;

drop table employee_sprint;

drop table scrum_master;

drop table sprint;

SET FOREIGN_KEY_CHECKS=1;

