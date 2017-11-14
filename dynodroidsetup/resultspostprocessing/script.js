$(document).ready(function() {
        $("table").hide();
        $("h2.table-bottom").hide();
    });

function toggle(me, element) {
    if($(element).is(":visible")) {
        $(element).hide();
        $(element + "+h2").hide();
        $(me).css("border-radius", "20px 20px 20px 20px");
    } else {
        $(element).show();
        $(element + "+h2").show();
        $(me).css("border-radius", "20px 20px 0px 0px");
    }
}

function showImage(src) {
    // image display code
    html = "<html><head><title>Enlarged Image</title>" +  
        "</head><body style='margin: 0px 0; text-align:center; '>" +  
        "<IMG src=logs/TestStrategy/MonitoringLogs/screenshots/" + src + ".png" +
        " BORDER=0 NAME=image /></body></html>"; 

    popup=window.open(",‘image’,‘toolbar=0,location=0,  directories=0, " +
                      "menuBar=0, scrollbars=0,resizable=1′"); 
    popup.document.open();
    popup.document.write(html);
    popup.document.close();
}
