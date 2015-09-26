# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table service (
  id                        bigint not null,
  user_email                varchar(255),
  user_name                 varchar(255),
  date_created              timestamp,
  constraint pk_service primary key (id))
;

create table service_song (
  id                        integer not null,
  index                     integer,
  song_name                 varchar(255),
  song_id                   bigint,
  lyrics_id                 bigint,
  song_key                  varchar(255),
  service_id                bigint,
  song_lyrics               TEXT,
  constraint pk_service_song primary key (id))
;

create table song (
  id                        bigint not null,
  song_name                 varchar(255),
  song_original_title       varchar(255),
  song_author               varchar(255),
  song_link                 varchar(255),
  song_importer             varchar(255),
  song_last_modified_by     varchar(255),
  song_book_id              integer,
  date_created              timestamp,
  date_modified             timestamp,
  constraint pk_song primary key (id))
;

create table song_lyrics (
  id                        bigint not null,
  song_id                   bigint,
  song_lyrics               TEXT,
  song_key                  varchar(255),
  constraint pk_song_lyrics primary key (id))
;

create table user_account (
  email                     varchar(255) not null,
  name                      varchar(255),
  password                  varchar(255),
  constraint pk_user_account primary key (email))
;

create sequence service_seq;

create sequence service_song_seq;

create sequence song_seq;

create sequence song_lyrics_seq;

create sequence user_account_seq;

alter table service_song add constraint fk_service_song_service_1 foreign key (service_id) references service (id) on delete restrict on update restrict;
create index ix_service_song_service_1 on service_song (service_id);
alter table song_lyrics add constraint fk_song_lyrics_song_2 foreign key (song_id) references song (id) on delete restrict on update restrict;
create index ix_song_lyrics_song_2 on song_lyrics (song_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists service;

drop table if exists service_song;

drop table if exists song;

drop table if exists song_lyrics;

drop table if exists user_account;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists service_seq;

drop sequence if exists service_song_seq;

drop sequence if exists song_seq;

drop sequence if exists song_lyrics_seq;

drop sequence if exists user_account_seq;

