BEGIN TRANSACTION;
create table ls_actions (
  id                            integer not null,
  timestamp                     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  unique_user_id                varchar(255),
  type                          varchar(255),
  service                       varchar(255),
  provider                      varchar(255),
  constraint pk_ls_actions primary key (id)
);

create table ls_players (
  id                            integer not null,
  unique_user_id                varchar(128),
  last_name                     varchar(16),
  ip_address                    varchar(255),
  password                      varchar(512),
  hashing_algorithm             integer,
  last_login                    timestamp,
  registration_date             date,
  optlock                       integer not null,
  constraint uq_ls_players_unique_user_id unique (unique_user_id),
  constraint pk_ls_players primary key (id)
);

INSERT INTO ls_players (unique_user_id, ip_address, password, hashing_algorithm)
SELECT unique_user_id, ip, password, encryption FROM users;

INSERT INTO ls_upgrades (version, description, applied_at) VALUES('1', 'Initial', CURRENT_TIMESTAMP);

DROP TABLE users;
COMMIT;