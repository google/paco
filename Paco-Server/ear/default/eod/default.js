function getGroupByName(experiment, name) {
  var groups = experiment.groups;  
  for(var i = 0; i < groups.length; i++) {
    var group = groups[i];    
    if (group.name === name) {
      return group;
    }
  }
  return null;
}

function mapInputs(experiment) {
  var inputsByName = [];
  var groupName =  window.env.getValue("experimentGroupName");
  var group = getGroupByName(experiment, groupName);
  var inputs = group.inputs;
  for (var i = 0; i< inputs.length; i++) {
    var input = inputs[i];
    inputsByName[input.name] = input;
  }
  return inputsByName;
}

function defaultPage(){
  var jsondata = window.env.getValue("lastResponse");
  var experiment = paco.experimentService.getExperiment();
  //alert(JSON.stringify(experiment, null, 2));
  var inputsByName = mapInputs(experiment);
  
  //alert(JSON.stringify(inputs, null, 2));
  var experimentData = $.parseJSON(jsondata);
  if (!experimentData) {
    // hack for samsung tmobile phones
    experimentData = eval('(' + jsondata + ')');
  }
  
  if (!experimentData) {
    alert("No Data");
    return;
  }
  var responsesHtml = "";
  
  var latestEvent = experimentData[0];
  try{
    var responses = latestEvent.responses;
  } catch(err){
    $("#responses").html('<br><br>You have not input any data! Please input data before exploring this option.');
    return;
  }
  for ( var i = 0; i < responses.length; i++) {
    var response = responses[i];
    if (response.answer == null || response.answer.length == 0) {
      response.answer = "No Answer";
    }
    var input = inputsByName[response.name];
    responsesHtml += "<div>";
    responsesHtml += "<div style='text-align:left;line-height:1.5;font-size:20;'>";
    responsesHtml += input.text;
    responsesHtml += "</div><br/>";
    responsesHtml += "<div style='color:#333333;text-align:center;line-height:1.5;font-size:18;'>";
    if (input.responseType === "photo") {
      responsesHtml += "<img src='data:image/jpg;base64," + response["answer"] + "' width=150>";
    } else if (input.responseType === "location") {
      responsesHtml += "&nbsp;&nbsp;&nbsp;<a href='file:///android_asset/map.html?inputId=" + response["name"] + "'>Maps</a>";
    } else {
      responsesHtml += response["answer"];
      responsesHtml += "&nbsp;&nbsp;&nbsp;<a href='file:///android_asset/time.html?inputId=" + response["name"] + "'>Chart</a>";
    }
    responsesHtml += "</div><br/></div>";
  }
  if (responsesHtml == "") {
    responsesHtml = "No responses found!";
  }
  $("#responses").html(responsesHtml);
}