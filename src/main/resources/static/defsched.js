function getScorePopoverContent(constraint_list) {
    var popover_content = "";
    constraint_list.forEach((constraint) => {
          if (getHardScore(constraint.score) == 0) {
             popover_content = popover_content + constraint.name + " : " + constraint.score + "<br>";
          } else {
             popover_content = popover_content + "<b>" + constraint.name + " : " + constraint.score + "</b><br>";
          }
    })
    return popover_content;
}

function getEntityPopoverContent(entityId, indictmentMap) {
    var popover_content = "";
    const indictment = indictmentMap[entityId];
    if (indictment != null) {
        popover_content = popover_content + "Total score: <b>" + indictment.score + "</b> (" + indictment.matchCount + ")<br>";
        indictment.constraintMatches.forEach((match) => {
                  if (getHardScore(match.score) == 0) {
                     popover_content = popover_content + match.constraintName + " : " + match.score + "<br>";
                  } else {
                     popover_content = popover_content + "<b>" + match.constraintName + " : " + match.score + "</b><br>";
                  }
            })
    }
    return popover_content;
}

function getHardScore(score) {
   return score.slice(0,score.indexOf("hard"))
}

function getMediumScore(score) {
   return score.slice(score.indexOf("hard/"),score.indexOf("medium"))
}

function getSoftScore(score) {
   return score.slice(score.indexOf("medium/"),score.indexOf("soft"))
}

$(document).ready(function () {
    const urlParams = new URLSearchParams(window.location.search);
    const solutionId = urlParams.get('id');

    $.getJSON("/defsched/score?id=" + solutionId, function(analysis) {
            var badge = "badge bg-danger";
            if (getHardScore(analysis.score)==0) { badge = "badge bg-success"; }
            $("#score_a").attr({"title":"Score Brakedown","data-bs-content":"" + getScorePopoverContent(analysis.constraints) + "","data-bs-html":"true"});
            $("#score_text").text(analysis.score);
            $("#score_text").attr({"class":badge});
    });

    $.getJSON("/defsched/solution?id=" + solutionId, function(solution) {
        $.getJSON("/defsched/indictments?id=" + solutionId, function(indictments) {
                        renderDefscheds(solution, indictments);
                        $(function () {
                          $('[data-toggle="popover"]').popover()
                        })
        })
    });

});

function renderDefscheds(solution, indictments) {
    var indictmentPersonMap = {};
    var indictmentThesisMap = {};
    var indictmentSessionMap = {};
    var personMap = {};

    solution.persons.forEach((person) => {
        personMap[person.personId] = person;
    })

    indictments.forEach((indictment) => {
         if (indictment.indictedObjectClass == "Person") {
            indictmentPersonMap[indictment.indictedObjectID] = indictment;
         }
         if (indictment.indictedObjectClass == "Thesis") {
            indictmentThesisMap[indictment.indictedObjectID] = indictment;
         }
         if (indictment.indictedObjectClass == "Session") {
            indictmentSessionMap[indictment.indictedObjectID] = indictment;
         }

    })

    const session_div = $("#session_container");
    solution.sessions.forEach((session) => {

        var v_badge = "badge bg-danger";
        if (indictmentSessionMap[session.sessionId]==null || getHardScore(indictmentMap[session.sessionId].score)==0) { v_badge = "badge bg-success"; }
        session_div.append($('<a data-toggle="popover" data-bs-html="true" data-bs-content="' +
        "room" + session.room + " " + session.startingAt + "(" + session.slotDurationMinutes + ")" +
        '<hr>' +
        getEntityPopoverContent(session.sessionId, indictmentSessionMap) +
        '" data-bs-original-title="'+ session.sessionId + ' (' + session.slotDurationMinutes + ')' +'"><span class="'+ v_badge +'">'+
        session.sessionId + ": room" + session.room + " " + session.startingAt + "(" + session.slotDurationMinutes + ")" + '</span></a>'));
        session_div.append($('<br>'))

        var thesis_nr = 1;
        session.thesisList.forEach((thesis) => {
            var thesis_badge = "badge bg-danger";
            if (indictmentThesisMap[thesis.thesisId] == null || getHardScore(indictmentThesisMap[thesis.thesisId].score)==0) { thesis_badge = "badge bg-success"; }
            if (indictmentThesisMap[thesis.thesisId] != null && getSoftScore(indictmentThesisMap[thesis.thesisId].score)!=0) {thesis_badge = "badge bg-warning";}
            session_div.append($('<a data-toggle="popover" data-bs-html="true" data-bs-content="'+
            thesis.title +
            '<br>starts=' + thesis.startsAt +
            '<hr>' +
            getEntityPopoverContent(thesis.thesisId, indictmentThesisMap) +
            '" data-bs-original-title="'+
            '#' + thesis_nr + ' ' + thesis.title + '"><span class="'+thesis_badge+'">'+
            '#' + thesis_nr + ' ' + thesis.title + ' ' +'</span></a>'));

            var author_badge = "badge bg-danger";
            var author = personMap[thesis.author];
            if (indictmentPersonMap[thesis.author] == null || getHardScore(indictmentPersonMap[thesis.author].score)==0) { author_badge = "badge bg-success"; }
            if (indictmentPersonMap[thesis.author] != null && getSoftScore(indictmentPersonMap[thesis.author].score)!=0) {author_badge = "badge bg-warning";}
            session_div.append($('<a data-toggle="popover" data-bs-html="true" data-bs-content="'+
                getEntityPopoverContent(thesis.author, indictmentPersonMap) +
                '" data-bs-original-title="'+
                "autors: " + author.name + '"><span class="'+author_badge+'">'+
                "autors: " + author.name +'</span></a>'));

            var supervisor_badge = "badge bg-danger";
            var supervisor = personMap[thesis.supervisor];
            if (indictmentPersonMap[thesis.supervisor] == null || getHardScore(indictmentPersonMap[thesis.supervisor].score)==0) { supervisor_badge = "badge bg-success"; }
            if (indictmentPersonMap[thesis.supervisor] != null && getSoftScore(indictmentPersonMap[thesis.supervisor].score)!=0) {supervisor_badge = "badge bg-warning";}
            session_div.append($('<a data-toggle="popover" data-bs-html="true" data-bs-content="'+
                 getEntityPopoverContent(thesis.supervisor, indictmentPersonMap) +
                 '" data-bs-original-title="'+
                 "vadītājs: " + supervisor.name + '"><span class="'+supervisor_badge+'">'+
                 "vadītājs: " + supervisor.name +'</span></a>'));

            var reviewer_badge = "badge bg-danger";
            var reviewer = personMap[thesis.reviewer];
            if (indictmentPersonMap[thesis.reviewer] == null || getHardScore(indictmentPersonMap[thesis.reviewer].score)==0) { reviewer_badge = "badge bg-success"; }
            if (indictmentPersonMap[thesis.reviewer] != null && getSoftScore(indictmentPersonMap[thesis.reviewer].score)!=0) {reviewer_badge = "badge bg-warning";}
            session_div.append($('<a data-toggle="popover" data-bs-html="true" data-bs-content="'+
                  getEntityPopoverContent(thesis.reviewer, indictmentPersonMap) +
                  '" data-bs-original-title="'+
                  "recenzents: " + reviewer.name + '"><span class="'+reviewer_badge+'">'+
                  "recenzents: " + reviewer.name +'</span></a>'));
            session_div.append($('<br>'))
            thesis_nr = thesis_nr + 1;
        })

        session_div.append($('<br>'));
    })
}



