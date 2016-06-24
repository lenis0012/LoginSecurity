alter table ls_players add column uuid_mode varchar(1) check ( uuid_mode in ('M','U','O'));
