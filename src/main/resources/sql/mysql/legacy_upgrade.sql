BEGIN TRANSACTION
DROP TABLE ls_actions;
DROP TABLE ls_players;

create table ls_actions (
  id                            integer auto_increment not null,
  timestamp                     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  unique_user_id                varchar(255),
  type                          varchar(255),
  service                       varchar(255),
  provider                      varchar(255),
  constraint pk_ls_actions primary key (id)
);

create table ls_upgrades (
  id                            integer auto_increment not null,
  version                       varchar(255),
  description                   varchar(255),
  applied_at                    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  constraint uq_ls_upgrades_version unique (version),
  constraint pk_ls_upgrades primary key (id)
);

create table ls_players (
  id                            integer auto_increment not null,
  unique_user_id                varchar(128),
  last_name                     varchar(16),
  ip_address                    varchar(255),
  password                      varchar(512),
  hashing_algorithm             integer,
  last_login                    datetime(6),
  registration_date             date,
  optlock                       bigint not null,
  constraint uq_ls_players_unique_user_id unique (unique_user_id),
  constraint pk_ls_players primary key (id)
);

INSERT INTO ls_players (unique_user_id, ip_address, password, hashing_algorithm)
SELECT unique_user_id, ip, password, encryption FROM users;

INSERT INTO ls_upgrades (version, description, applied_at) VALUES('1', 'Initial', CURRENT_TIMESTAMP);

DROP TABLE users;
COMMIT;