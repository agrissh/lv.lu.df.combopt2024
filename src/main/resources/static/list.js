$(document).ready(function () {
    $.getJSON("/defsched/list", function(defscheds) {
        var listofdefscheds = $("#listofdefscheds");
        $.each(defscheds, function(idx, value) {
              listofdefscheds.append($('<li><a href="defsched.html?id='+ value.scheduleId + '">' +
               value.score +'</a>' +
               ' sessions: ' + value.sessions.length + ', thesis:' + value.thesis.length + '</li>'));
        });
    });
});