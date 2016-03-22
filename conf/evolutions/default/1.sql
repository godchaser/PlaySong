# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table service (
  id                            varchar(255) not null,
  user_email                    varchar(255),
  user_name                     varchar(255),
  service_name                  varchar(255),
  date_created                  timestamp,
  constraint pk_service primary key (id)
);

create table service_song (
  id                            varchar(255) not null,
  song_name                     varchar(255),
  song_id                       varchar(255),
  lyrics_id                     varchar(255),
  song_key                      varchar(255),
  service_id                    varchar(255),
  song_lyrics                   TEXT,
  constraint pk_service_song primary key (id)
);

create table song (
  id                            varchar(255) not null,
  song_name                     varchar(255),
  song_original_title           varchar(255),
  song_author                   varchar(255),
  song_link                     varchar(255),
  song_importer                 varchar(255),
  song_last_modified_by         varchar(255),
  private_song                  boolean,
  date_created                  timestamp,
  date_modified                 timestamp,
  constraint pk_song primary key (id)
);

create table song_book (
  id                            varchar(255) not null,
  song_book_name                varchar(255),
  private_songbook              boolean,
  constraint pk_song_book primary key (id)
);

create table song_book_song (
  song_book_id                  varchar(255) not null,
  song_id                       varchar(255) not null,
  constraint pk_song_book_song primary key (song_book_id,song_id)
);

create table song_book_user_account (
  song_book_id                  varchar(255) not null,
  user_account_email            varchar(255) not null,
  constraint pk_song_book_user_account primary key (song_book_id,user_account_email)
);

create table song_lyrics (
  id                            varchar(255) not null,
  song_id                       varchar(255),
  song_lyrics                   TEXT,
  song_key                      varchar(255),
  constraint pk_song_lyrics primary key (id)
);

create table user_account (
  email                         varchar(255) not null,
  name                          varchar(255),
  password                      varchar(255),
  constraint pk_user_account primary key (email)
);

create table user_account_song_book (
  user_account_email            varchar(255) not null,
  song_book_id                  varchar(255) not null,
  constraint pk_user_account_song_book primary key (user_account_email,song_book_id)
);

alter table service_song add constraint fk_service_song_service_id foreign key (service_id) references service (id) on delete restrict on update restrict;
create index ix_service_song_service_id on service_song (service_id);

alter table song_book_song add constraint fk_song_book_song_song_book foreign key (song_book_id) references song_book (id) on delete restrict on update restrict;
create index ix_song_book_song_song_book on song_book_song (song_book_id);

alter table song_book_song add constraint fk_song_book_song_song foreign key (song_id) references song (id) on delete restrict on update restrict;
create index ix_song_book_song_song on song_book_song (song_id);

alter table song_book_user_account add constraint fk_song_book_user_account_song_book foreign key (song_book_id) references song_book (id) on delete restrict on update restrict;
create index ix_song_book_user_account_song_book on song_book_user_account (song_book_id);

alter table song_book_user_account add constraint fk_song_book_user_account_user_account foreign key (user_account_email) references user_account (email) on delete restrict on update restrict;
create index ix_song_book_user_account_user_account on song_book_user_account (user_account_email);

alter table song_lyrics add constraint fk_song_lyrics_song_id foreign key (song_id) references song (id) on delete restrict on update restrict;
create index ix_song_lyrics_song_id on song_lyrics (song_id);

alter table user_account_song_book add constraint fk_user_account_song_book_user_account foreign key (user_account_email) references user_account (email) on delete restrict on update restrict;
create index ix_user_account_song_book_user_account on user_account_song_book (user_account_email);

alter table user_account_song_book add constraint fk_user_account_song_book_song_book foreign key (song_book_id) references song_book (id) on delete restrict on update restrict;
create index ix_user_account_song_book_song_book on user_account_song_book (song_book_id);


# --- !Downs

alter table service_song drop constraint if exists fk_service_song_service_id;
drop index if exists ix_service_song_service_id;

alter table song_book_song drop constraint if exists fk_song_book_song_song_book;
drop index if exists ix_song_book_song_song_book;

alter table song_book_song drop constraint if exists fk_song_book_song_song;
drop index if exists ix_song_book_song_song;

alter table song_book_user_account drop constraint if exists fk_song_book_user_account_song_book;
drop index if exists ix_song_book_user_account_song_book;

alter table song_book_user_account drop constraint if exists fk_song_book_user_account_user_account;
drop index if exists ix_song_book_user_account_user_account;

alter table song_lyrics drop constraint if exists fk_song_lyrics_song_id;
drop index if exists ix_song_lyrics_song_id;

alter table user_account_song_book drop constraint if exists fk_user_account_song_book_user_account;
drop index if exists ix_user_account_song_book_user_account;

alter table user_account_song_book drop constraint if exists fk_user_account_song_book_song_book;
drop index if exists ix_user_account_song_book_song_book;

drop table if exists service;

drop table if exists service_song;

drop table if exists song;

drop table if exists song_book;

drop table if exists song_book_song;

drop table if exists song_book_user_account;

drop table if exists song_lyrics;

drop table if exists user_account;

drop table if exists user_account_song_book;

