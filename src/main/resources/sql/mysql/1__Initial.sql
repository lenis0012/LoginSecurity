create table ls_actions (
  id                            integer auto_increment not null,
  timestamp                     datetime,
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

create table ls_inventories (
  id                            integer auto_increment not null,
  helmet                        varchar(255),
  chestplate                    varchar(255),
  leggings                      varchar(255),
  boots                         varchar(255),
  off_hand                      varchar(255),
  contents                      varchar(255) not null,
  constraint pk_ls_inventories primary key (id)
);

create table ls_locations (
  id                            integer auto_increment not null,
  world                         varchar(255),
  x                             double,
  y                             double,
  z                             double,
  yaw                           integer,
  pitch                         integer,
  constraint pk_ls_locations primary key (id)
);

create table ls_players (
  id                            integer auto_increment not null,
  unique_user_id                varchar(128),
  last_name                     varchar(16),
  ip_address                    varchar(64),
  password                      varchar(512),
  hashing_algorithm             integer,
  location_id                   integer,
  inventory_id                  integer,
  last_login                    datetime,
  registration_date             date,
  optlock                       bigint not null,
  constraint uq_ls_players_unique_user_id unique (unique_user_id),
  constraint uq_ls_players_location_id unique (location_id),
  constraint uq_ls_players_inventory_id unique (inventory_id),
  constraint pk_ls_players primary key (id)
);

alter table ls_players add constraint fk_ls_players_location_id foreign key (location_id) references ls_locations (id) on delete restrict on update restrict;

alter table ls_players add constraint fk_ls_players_inventory_id foreign key (inventory_id) references ls_inventories (id) on delete restrict on update restrict;

