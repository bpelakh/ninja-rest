create table if not exists devices (
  customer varchar(45) NOT NULL,
  id varchar(45) NOT NULL,
  name varchar(45) NOT NULL,
  type varchar(45) NOT NULL,
  PRIMARY KEY (customer, id)
  );

create table if not exists services (
  customer varchar(45) NOT NULL,
  service varchar(45) NOT NULL,
  PRIMARY KEY (customer, service)
  );

create table if not exists service_defs (
  service varchar(45) NOT NULL,
  type varchar(45) NOT NULL DEFAULT '',
  price integer not null,
  PRIMARY KEY (service, type)
  );


insert into service_defs(service, type, price) VALUES('Antivirus', 'MAC', 7);
insert into service_defs(service, type, price) VALUES('Antivirus', 'WINDOWS_SERVER', 5);
insert into service_defs(service, type, price) VALUES('Antivirus', 'WINDOWS_WORKSTATION', 5);
insert into service_defs(service, type, price) VALUES('Cloudberry', '', 3);
insert into service_defs(service, type, price) VALUES('PSA', '', 2);
insert into service_defs(service, type, price) VALUES('TeamViewer', '', 1);
insert into service_defs(service, type, price) VALUES('Device', '', 4);
