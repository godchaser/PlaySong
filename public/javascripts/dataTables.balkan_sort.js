/**
 * Created by samuel on 5/11/15.
 */
function character_substitute(string) {
    var first_char = string.replace( /<.*?>/g, "" ).toLowerCase().charAt(0);
    var chars = /[šđžčć]/g;

    if (first_char.match(chars)) {
        if (first_char == "š") { first_char = first_char.replace("š", "s"); return first_char; }
        if (first_char == "ž") { first_char = first_char.replace("ž", "z"); return first_char; }
        if (first_char == "č") { first_char = first_char.replace("č", "c"); return first_char; }
        if (first_char == "ć") { first_char = first_char.replace("ć", "c"); return first_char; }
        if (first_char == "đ") { first_char = first_char.replace("đ", "d"); return first_char; }
    }

    return first_char;
}

jQuery.fn.dataTableExt.oSort['balkan_sort-asc']  = function(a,b) {
    x = character_substitute(a);
    y = character_substitute(b);

    return ((x < y) ? -1 : ((x > y) ? 1 : 0));
};

jQuery.fn.dataTableExt.oSort['balkan_sort-desc'] = function(a,b) {
    x = character_substitute(a);
    y = character_substitute(b);

    return ((x < y) ? 1 : ((x > y) ? -1 : 0));
};