INSERT INTO PUBLIC.PLAY_EVOLUTIONS (ID, HASH, APPLIED_AT, APPLY_SCRIPT, REVERT_SCRIPT, STATE, LAST_PROBLEM) VALUES (1, 'ccc5c012ff7aea9c400eba6ed97e618a071fc134', '2015-05-12 00:00:00.000000000', 'create table song (
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

create sequence song_seq;

create sequence song_lyrics_seq;

alter table song_lyrics add constraint fk_song_lyrics_song_1 foreign key (song_id) references song (id) on delete restrict on update restrict;
create index ix_song_lyrics_song_1 on song_lyrics (song_id);', 'SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists song;

drop table if exists song_lyrics;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists song_seq;

drop sequence if exists song_lyrics_seq;', 'applied', '');
