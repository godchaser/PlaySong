# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table song (
  id                        bigint not null,
  song_name                 varchar(255),
  song_author               varchar(255),
  song_lyrics               TEXT,
  constraint pk_song primary key (id))
;

create sequence song_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists song;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists song_seq;

