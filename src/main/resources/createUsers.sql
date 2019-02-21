create extension pgcrypto;

create table if not exists users (
  username varchar(32) NOT NULL,
  password varchar(64) NOT NULL,
  rolename varchar(32) NOT NULL
);

insert into users(username, password, rolename) VALUES('Joe', crypt('opensesame', gen_salt('md5')), 'READER');
insert into users(username, password, rolename) VALUES('Admin', crypt('verysecure', gen_salt('md5')), 'WRITER');
