# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table song (
  id                        bigint not null,
  song_name                 varchar(255),
  song_author               varchar(255),
  song_lyrics               varchar(255),
  constraint pk_song primary key (id))
;

create table task (
  id                        bigint not null,
  label                     varchar(255),
  constraint pk_task primary key (id))
;

create sequence song_seq;

create sequence task_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists song;

drop table if exists task;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists song_seq;

drop sequence if exists task_seq;

