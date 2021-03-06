/**
 * Created by samuel on 23.02.15..
 */
/*
 * ! jQuery Chord Transposer plugin v1.0
 * http://codegavin.com/projects/transposer
 * 
 * Copyright 2010, Jesse Gavin Dual licensed under the MIT or GPL Version 2
 * licenses. http://codegavin.com/license
 * 
 * Date: Sat Jun 26 21:27:00 2010 -0600
 */
(function($) {

    $.fn.transpose = function(options) {
        var opts = $.extend({}, $.fn.transpose.defaults, options);

        var currentKey = null;
        
        var backupHtml = null;
        
        var currentMasterChord = null;
        
        var keys = [
            { name: 'Ab',  value: 0,   type: 'F' },
            { name: 'A',   value: 1,   type: 'N' },
            { name: 'A#',  value: 2,   type: 'S' },
            { name: 'Bb',  value: 2,   type: 'F' },
            { name: 'B',   value: 3,   type: 'N' },
            { name: 'C',   value: 4,   type: 'N' },
            { name: 'C#',  value: 5,   type: 'S' },
            { name: 'Db',  value: 5,   type: 'F' },
            { name: 'D',   value: 6,   type: 'N' },
            { name: 'D#',  value: 7,   type: 'S' },
            { name: 'Eb',  value: 7,   type: 'F' },
            { name: 'E',   value: 8,   type: 'N' },
            { name: 'F',   value: 9,   type: 'N' },
            { name: 'F#',  value: 10,  type: 'S' },
            { name: 'Gb',  value: 10,  type: 'F' },
            { name: 'G',   value: 11,  type: 'N' },
            { name: 'G#',  value: 0,   type: 'S' }
        ];

        var getKeyByName = function (name) {
            if (name.charAt(name.length-1) == "m") {
                name = name.substring(0, name.length-1);
            }
            for (var i = 0; i < keys.length; i++) {
                if (name == keys[i].name) {
                    return keys[i];
                }
            }
        };

        var getChordRoot = function (input) {
            if (input.length > 1 && (input.charAt(1) == "b" || input.charAt(1) == "#"))
                return input.substr(0, 2);
            else
                return input.substr(0, 1);
        };

        var getNewKey = function (oldKey, delta, targetKey) {
            var keyValue = getKeyByName(oldKey).value + delta;

            if (keyValue > 11) {
                keyValue -= 12;
            } else if (keyValue < 0) {
                keyValue += 12;
            }

            var i=0;
            if (keyValue == 0 || keyValue == 2 || keyValue == 5 || keyValue == 7 || keyValue == 10) {
                // Return the Flat or Sharp Key
                switch(targetKey.name) {
                    case "A":
                    case "A#":
                    case "B":
                    case "C":
                    case "C#":
                    case "D":
                    case "D#":
                    case "E":
                    case "F#":
                    case "G":
                    case "G#":
                        for (;i<keys.length;i++) {
                            if (keys[i].value == keyValue && keys[i].type == "S") {
                                return keys[i];
                            }
                        }
                    default:
                        for (;i<keys.length;i++) {
                            if (keys[i].value == keyValue && keys[i].type == "F") {
                                return keys[i];
                            }
                        }
                }
            }
            else {
                // Return the Natural Key
                for (;i<keys.length;i++) {
                    if (keys[i].value == keyValue) {
                        return keys[i];
                    }
                }
            }
        };

        var getChordType = function (key) {
            switch (key.charAt(key.length - 1)) {
                case "b":
                    return "F";
                case "#":
                    return "S";
                default:
                    return "N";
            }
        };

        var getDelta = function (oldIndex, newIndex) {
            if (oldIndex > newIndex)
                return 0 - (oldIndex - newIndex);
            else if (oldIndex < newIndex)
                return 0 + (newIndex - oldIndex);
            else
                return 0;
        };

        var transposeSong = function (target, key) {
            var newKey = getKeyByName(key);

            if (currentKey.name == newKey.name) {
                return;
            }

            var delta = getDelta(currentKey.value, newKey.value);
            
            $("span.c", target).each(function (i, el) {
                transposeChord(el, delta, newKey);
            });

            currentKey = newKey;
        };

        var transposeChord = function (selector, delta, targetKey) {
            var el = $(selector);
            var oldChord = el.text();
            var oldChordRoot = getChordRoot(oldChord);
            var newChordRoot = getNewKey(oldChordRoot, delta, targetKey);
            var newChord = newChordRoot.name + oldChord.substr(oldChordRoot.length);
            el.text(newChord);

            var sib = el[0].nextSibling; 
            if (sib && sib.nodeType == 3 && sib.nodeValue.length > 0 && sib.nodeValue.charAt(0) != "/" && sib.nodeValue.trim() != "-/" && sib.nodeValue.trim() != "(" && sib.nodeValue.charAt(0) != ")"){
                var wsLength = getNewWhiteSpaceLength(oldChord.length, newChord.length, sib.nodeValue.length);
                sib.nodeValue = makeString(" ", wsLength);
            }
        };

        var getNewWhiteSpaceLength = function (a, b, c) {
            if (a > b)
                return (c + (a - b));
            else if (a < b)
                return (c - (b - a));
            else
                return c;
        };

        var makeString = function (s, repeat) {
            var o = [];
            for (var i = 0; i < repeat; i++) o.push(s);
            return o.join("");
        };

        var isChordLine = function (input) {
            var tokens = input.replace(/\s+/, " ").split(" ");
            // Try to find tokens that aren't chords
            // if we find one we know that this line is not a 'chord' line.
            for (var i = 0; i < tokens.length; i++) {
                // match -/,(,)," " and replace them with ""
                tokens[i]=tokens[i].replace(/(\()|(\))|(\-\/)|(\s+)/g,"");
                //console.log("token");
                //console.log(tokens[i]);
                // i should replace ( ) also, maybe with one regex
                if (!$.trim(tokens[i]).length == 0 && !tokens[i].match(opts.chordRegex)){
                    return false;
                }
            }
            return true;
        };

        var getFirstChordInLine = function (input) {
            var tokens = input.replace(/\s+/, " ").split(" ");
            // Try to find tokens that aren't chords
            // if we find one we know that this line is not a 'chord' line.
            for (var i = 0; i < tokens.length; i++) {
                // match -/,(,)," " and replace them with ""
                tokens[i]=tokens[i].replace(/(\()|(\))|(\-\/)|(\s+)/,"");
                if ($.trim(tokens[i]).length != 0 && tokens[i].match(opts.chordRegex)){
                    var tokenString = tokens[i];
                    if (tokenString.indexOf("/") !=-1){
                        tokenString = tokenString.substring(0,tokenString.indexOf("/"));
                    }
                    tokenString = tokenString.replace(/[0-9]/g, '');
                    var firstChord = getKeyByName(tokenString);
                    return firstChord;
                }
            }
            // default
            var firstChord = getKeyByName("C");
            return firstChord;
        };

        var wrapChords = function (input) {
            return input.replace(opts.chordReplaceRegex, "<span class='c'>$1</span>");
        };
        
        return $(this).each(function() {
            var output = [];
            //console.log($(this));
            //console.log("transposer received: " + $(this).text());
            var lines = $(this).text().split("\n");
            var line = "";
            var initialChordSet = false;
            var thisIsChorusText = false;
            var verseTypes = ["Verse", "Chorus", "Bridge", "Intro", "Ending"];

            for (var i = 0; i < lines.length; i++) {
            	var lineRecognized = false;
                line = lines[i];
                if (isChordLine(line)){
                    if (!initialChordSet) {
                        currentKey=getFirstChordInLine(line);
                        initialChordSet = true;
                        currentMasterChord = currentKey.name;
                        //console.log("Current master chord: " + currentMasterChord);
                    }
                    output.push("<span class='chordLine'>" + wrapChords(line) + "</span>");
                    lineRecognized = true;
                }
                // check if this is verse type
	            else if (line.charAt(line.length-1) == "]") {
	            	 switch(line.charAt(1)) {
		                 case "C": line = line.replace("C", "Chorus "); 
		                 			//starting chorus
		                 			thisIsChorusText=true; 
		                 			break;
		                 case "V": line = line.replace("V", "Verse ");
			              			thisIsChorusText=false; 
			              			break;
		                 case "B": line = line.replace("B", "Bridge "); 
				                   thisIsChorusText=false; 
			              		   break;
		                 case "I": line = line.replace("I", "Intro ");
				                   thisIsChorusText=false; 
			              		   break;
		                 case "E": line = line.replace("E", "Ending "); 
				                   thisIsChorusText=false; 
			              		   break;
		                 default:  break;
	            	 }
	            	output.push("<span class='verseType'>" + line.substring(1,line.length-1).trim() + "</span>");
	            	lineRecognized = true;
	            }            
	            else if (!lineRecognized){
		            for(var j=0; j< verseTypes.length; j++){
		            	if(line.indexOf(verseTypes[j]) != -1){
		        	  	  output.push("<span class='verseType'>" + line + "</span>");
		        	  	  lineRecognized = true;
		        	  	  break;
		            	  }
		            }
		            if (!lineRecognized){
		            	if (thisIsChorusText){
		            		output.push("<span class='lyrics-bold'>" + line + "</span>");
		            	}
		            	else {
		            		output.push("<span class='lyrics'>" + line + "</span>");
		            	}	                
		                lineRecognized = true;
		            }
	            }
            };

            if (!initialChordSet){
                currentKey =  getKeyByName("C");
            }

            // Build transpose links ===========================================
            var keyLinks = [];
            $(keys).each(function(i, key) {
                if (currentKey.name == key.name)
                    keyLinks.push("<a href='#' class='selected'>" + key.name + "</a>");
                else
                    keyLinks.push("<a href='#'>" + key.name + "</a>");
            });
            var hideChords = "<button id='hideChordsButton' type='button' class='btn btn-xs'>Hide Chords</button>";
            keyLinks.push(hideChords);
            
            var printSong = "  <button id='printSongButton' type='button' class='btn btn-xs'><i class='fa fa-print' aria-hidden='true'></i> Print</button>"; 
            keyLinks.push(printSong);

            var $this = $(this);
            var keysHtml = $("<div class='transpose-keys'></div>");
            keysHtml.html(keyLinks.join(""));
            $("a", keysHtml).click(function(e) {
                e.preventDefault();
                transposeSong($this, $(this).text());
                $(".transpose-keys a").removeClass("selected");
                $(this).addClass("selected");
                currentMasterChord = $(this).text();
                //console.log("Current master chord: " + currentMasterChord);
                return false;
            });


            $(this).before(keysHtml);
            $(this).html(output.join("\n"));
            //console.log("transposer output: " + output);
            
            $('#hideChordsButton').click(function(event) {
            	// hide
            	if (backupHtml == null){
            		backupHtml = $("pre[id*='songLyrics']").html();
            		$('.chordLine').remove();
	                var output = $();
	
	                $.each($("pre[id*='songLyrics']").html().split(/[\n\r]+/g), function(i, el) {
	                    if (el) {
	                        output = output.add($("<span class='lyricsOnly'>" + el + "\n</span>"));
	                    }
	                });
	                $("pre[id*='songLyrics']").html(output);
	                $(this).text("Unhide Chords");
            	} 
            	// unhide
            	else {
            		$("pre[id*='songLyrics']").html(backupHtml);
            		backupHtml = null;
            		$(this).text("Hide Chords");
            	}
            });
            
            $('#printSongButton').click(function(event) {
            	//console.log("Print song!");
            	var link = "/song/print/";
            	var songId = $('.selected-row').attr('id');
				link = link.concat(songId);
				var excludeChords = ("Unhide Chords" == $('#hideChordsButton').text());
				link = link.concat("?excludeChords=" + excludeChords);
				link = link.concat("&currentChord=" + currentMasterChord);
				window.open(link, '_self');
            });
            
        });
    };


    $.fn.transpose.defaults = {
        chordRegex: /^[A-G][b\#]?(2|5|6|7|9|11|13|6\/9|7\-5|7\-9|7\#5|7\#9|7\+5|7\+9|7b5|7b9|7sus2|7sus4|add2|add4|add9|aug|dim|dim7|m\/maj7|m6|m7|m7b5|m9|m11|m13|maj7|maj9|maj11|maj13|mb5|m|sus|sus2|sus4)*(\/[A-G][b\#]*)*$/,
        chordReplaceRegex: /([A-G][b\#]?(2|5|6|7|9|11|13|6\/9|7\-5|7\-9|7\#5|7\#9|7\+5|7\+9|7b5|7b9|7sus2|7sus4|add2|add4|add9|aug|dim|dim7|m\/maj7|m6|m7|m7b5|m9|m11|m13|maj7|maj9|maj11|maj13|mb5|m|sus|sus2|sus4)*)/g
    };

})(jQuery);