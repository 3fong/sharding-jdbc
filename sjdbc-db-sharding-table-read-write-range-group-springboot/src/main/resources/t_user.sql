CREATE DATABASE `ds2` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
CREATE DATABASE `ds3` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';

CREATE TABLE `t_user0`(
	id bigint(64) not null,
	city varchar(20) not null,
	name varchar(20) not null,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `t_user1`(
	id bigint(64) not null,
	city varchar(20) not null,
	name varchar(20) not null,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `t_user2`(
	id bigint(64) not null,
	city varchar(20) not null,
	name varchar(20) not null,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
