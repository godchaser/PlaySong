# PlaySong
===============
[![Build Status](https://travis-ci.org/godchaser/PlaySong.svg)](https://travis-ci.org/godchaser/PlaySong)
## Introduction
------------

**PlaySong** is a simple open source song/songbook (with chords) manager web service.
It has simple user interface and functionality that I've found lacking
in other song management software - e.g. [OpenSong](http://www.opensong.org/) 
(it is only local desktop application, no database sync, revisioning, 
bad multi-platform support, isn't easily customizable, no export to editable formats, 
no songbook management...). 

PlaySong main advantage is chord manipulation and centralized database that could be 
used for collaboration of multiple users which could add new songs to database.

Please have in mind that this whole project is still under development and has lots 
of bugs, discrepancies and possible/mandatory improvements. So I will continue to work on 
it as time allows me.

**Heroku test deployment** http://playsong.herokuapp.com/

## Features:
* Chord manipulation - chord transposition
* SCRUD functionality - search through song lyrics, read/add/delete/update songs
* Song export 2 docx and pdf functionality - export single song or export selection of songs
to create consistent and formatted songbook with numeration and song content
* Responsive design (suitable for mobile devices)

## Planned features:
* Import from other song format (currently opensongbook format works quick & dirty way)
* Export songs to [OpenLP](http://openlp.org/) projection software
* Public and private songbooks
* Song revisions
* User management
* Integration tests
* Many other goodies...

PlaySong

<div align="center">
<img width="100%" src="screenshots/PlaySong_Table_View_1.png" alt="PlaySong Main Table View" title="Main view"</img>
</div>

<div align="center">
<img width="100%" src="screenshots/PlaySong_Edit_Song_1.png" alt="PlaySong Edit Song View" title="Edit song"</img>
</div>

<div align="center">
<img width="100%" src="screenshots/PlaySong_Single_Song_1.png" alt="PlaySong Single Song View" title="Song view"</img>
</div>

<div align="center">
<img width="100%" src="screenshots/PlaySong_Single_Song_Youtube_1.png" alt="PlaySong Song View with Video" title="Song with video"</img>
</div>

<div align="center">
<img width="100%" src="screenshots/PlaySong_Full_Text_Search_1.png" alt="PlaySong Full Text Search" title="Search by anything - song name, lyrics, author..."</img>
</div>

<div align="center">
<img width="100%" src="screenshots/PlaySong_Playlist_Download_1.png" alt="PlaySong Download List" title="Download and share playlists"</img>
</div>

<div align="center">
<img width="100%" src="screenshots/PlaySong_Playlist_Maker_1.png" alt="PlaySong Maker" title="Make and publish Playlists"</img>
</div>

<div align="center">
<img width="100%" src="screenshots/PlaySong_Pdf_Playlist_1.png" alt="PlaySong Generated Pdf Playlist" title="Generated Pdf Playlist"</img>
</div>

<div align="center">
<img width="100%" src="screenshots/PlaySong_Pdf_Table_Of_Content_1.png" alt="PlaySong Generated Pdf Playlist Table Of Content" title="Pdf Table Of Content"</img>
</div>

<div align="center">
<img src="screenshots/PlaySong_Responsive_1.png" alt="PlaySong Responsive View" title="Mobile View"</img>
</div>

<div align="center">
<img src="screenshots/PlaySong_Responsive_2.png" alt="PlaySong Responsive View" title="Mobile View"</img>
</div>

<div align="center">
<img src="screenshots/PlaySong_Responsive_3.png" alt="PlaySong Responsive View" title="Mobile View"</img>
</div>


### Used stack
#### Development
* [Java 8](https://www.java.com/)
* [Play Framework](https://www.playframework.com)
* [Ebean](http://www.avaje.org/)
* [Apache POI](https://poi.apache.org/)
* [iText](http://itextpdf.com/)
* [Bootstrap](http://getbootstrap.com/)
* [Eclipse](https://eclipse.org/)
* [Docker](https://www.docker.com/)

#### CI
* [travis-ci](https://travis-ci.org/)

License
-------

PlaySong is open source software provided under under [Apache License 2.0](http://apache.org/licenses/LICENSE-2.0)
