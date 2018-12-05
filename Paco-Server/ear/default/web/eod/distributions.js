
function isoDateString(d){
  function pad(n){
    return n<10 ? '0'+n : n;
  }

  return pad(d.getMonth()+1)+'/'
  + pad(d.getDate()) + '/'
  + String(d.getFullYear()).slice(2) + ' '
}


function truncString(str, len) {
  if (str.length > len) {
    return str.substr(0,len);
  } else {
    return str;
  }  
}

function meanLocation(arr) {
  var sumLat = 0;
  var sumLng = 0;
  var exclude = 0;
  for(var i=0; i<arr.length; i++) {
    try{
      if (!(isNaN(arr[i][0])|| isNaN(arr[i][1]))){
        sumLat+=(arr[i][0]-0);
        sumLng+=(arr[i][1]-0);
      } else {
        exclude +=1;
      }
    }catch(err){
      exclude +=1;
    }
  }
  try{
    return [(sumLat/(arr.length-exclude)), (sumLng/(arr.length-exclude))];
  } catch (err){
    return;
  }
}

function minMaxLocation(arr) {
  var swLat = 180;
  var swLng = 180;
  var neLat = -180;
  var neLng = -180;
  for(var i=0; i<arr.length; i++) {
    try{
      if (!(isNaN(arr[i][0]))){
        if (swLat>(arr[i][0]-0)){
          swLat = (arr[i][0]-0);
        }if (neLat<(arr[i][0]-0)){
          neLat = (arr[i][0]-0);
        }
      }
      if (!(isNaN(arr[i][1]))){
        if (swLng>(arr[i][1]-0)){
          swLng = (arr[i][1]-0);
        }if (neLng<(arr[i][1]-0)){
          neLng = (arr[i][1]-0);
        }
      }
    }catch(err){

    }
  }
  return [swLat, swLng, neLat, neLng];
}     

function meanNumber(arr) {
  if(arr.length == 0){
    return null;}
  var total = 0;
  var exclude = 0;
  for(var i=0; i<arr.length; i++) {
    if (!isNaN(arr[i])){
      total+=arr[i];
    } else {
      exclude +=1;
    }
  }
  try{
    return round2Dec(total/(arr.length-exclude));
  } catch (err){
    return;
  }
}

function medianNumber(arr){
  if(arr.length == 0){
    return null;
  }
  return arr[Math.floor(arr.length/2)];
}

function modeNumber(arr){
  if(arr.length == 0){
    return null;}
  var modeMap = {};
  var maxNumber = [];
  maxNumber.push(arr[0]); var maxCount = 1;
  for(var i = 0; i < arr.length; i++) {
    var num = arr[i];
    if(modeMap[num] == null){
      modeMap[num] = 1;
    } else{
      modeMap[num]++;
    }  
    if(modeMap[num] > maxCount){
      maxNumber = [];
      maxNumber.push(num);
      maxCount = modeMap[num];
    }
    else if(modeMap[num] == maxCount && num!=maxNumber[0]){
      if (maxNumber.length<=1){
        maxNumber.push(" "+num);
      }else{
        maxNumber = "There are more than two modes, so mode may not be a useful statistic.";
      }
    }
  }
  return [maxNumber, maxCount];
}

function minMaxNumber(arr){
  var min = arr[0];
  var max = arr[0];
  for (var i = 1; i < arr.length; i++){
    if (arr[i] < min){
      min = arr[i];
    }
    if (arr[i] > max){
      max = arr[i];
    }
  }
  return [min, max];
}

function round2Dec(n) {
  var ans = n * 1000 
  ans = Math.round(ans /10);
  return (ans/100);
} 

function main() {
   var jsondata = window.eventLoader.loadAllEvents();
   var experimentData = $.parseJSON(jsondata);
   if (!experimentData) {
     // hack for samsung tmobile phones
     experimentData = eval('(' + jsondata + ')');
   }

   
   var inputId = parseInt(window.env.getValue("inputId"));

   if (!experimentData) {
      alert("No Data");
      return;
   } 

   var d=[]; 
   var meanInfo = ""; 
   var modeInfo = ""; 
   var medianInfo = ""; 
   var minInfo = ""; 
   var MaxInfo = "";
   
   var responseType = null;
   var title = "No Title";
   var foundAMatch = false;
   var sampleAnswer;
   for (var j = 0; j < experimentData.length; j++) {
     if (foundAMatch) {
       break;
     }  
     for (var i = 0; i < experimentData[j].responses.length; i++) {  
       var response = experimentData[j].responses[i];
       if (response.inputId == inputId) {   
         foundAMatch = true;
         responseType = response.responseType;
         title = response.prompt;
         sampleAnswer = response.answer;
       }
     }    
   }  
   
   if (responseType == null){  // not found, i.e. no data input
     $("#meanHtml").html('<br><p>This variable does not have any data recorded. Please input data before trying this option.<p>');
     return;
   }
   if (responseType == "number" ||
     (responseType == "open text" && sampleAnswer != null && sampleAnswer != "" && !isNaN(sampleAnswer)) || 
     responseType == "likert" ||
     responseType == "likert_smileys"||
     responseType == "location"||
     responseType == "list") {
     
     var counts = []; // counts for each answer-used in lists and likerts
     var orderedKeys = []; // keep track of the response value ordering - used
                            // in lists and likerts
     for (var j = 0; j < experimentData.length; j++) {
       var responses = experimentData[j].responses;
       for (var r = 0; r < responses.length; r++) {
         if (responses[r].inputId == inputId) {
           if (!responses[r].answer) {
             continue;
           }  
           if (responseType=="location"){
             try{
               var latLngStr = responses[r].answer;
               var lat = Number(latLngStr.substring(0, latLngStr.indexOf(',')))
               var lng = Number(latLngStr.substring(latLngStr.indexOf(',')+1))
               d.push([lat, lng]);
             } catch(err){}
           }else if (responseType=="list"){
             d.push([responses[r].answer]);
           }else{
             d.push(responses[r].answer-0);
           } 
    // /taken from time to make count chart for lists or likerts
           if (responseType=="list" || responseType == "likert" || responseType == "likert_smileys") {              
             var answer = responses[r].answer;
             var answers = [];
             var answerOrders = [];
             if (responseType == "list" && responses[r].isMultiselect) {                
               answers = answer.split(',');
               answerOrders = responses[r].answerOrder.split(',');                                
             } else {
               answers.push(answer);
               answerOrders.push(responses[r].answerOrder);
             }
             
             for (var currentAnswerIndex in answers) if (answers.hasOwnProperty(currentAnswerIndex)) {
              var currentAnswer = answers[currentAnswerIndex];  
                       
               var currentCount = counts[currentAnswer];
               if (currentCount) {
                   counts[currentAnswer] = currentCount + 1;
               } else {
                   counts[currentAnswer] = 1;
                   orderedKeys[answerOrders[answers.indexOf(currentAnswer)]] = currentAnswer;
               }
             }
           }
    // /////
         }
       }
     }
     
     if (responseType == "location"){
       meanInfo = "<p> <b>Mean</b> location: ("+meanLocation(d)[0] + ", " +meanLocation(d)[1] + ")</p>";
       minMaxLoc = minMaxLocation(d);
       minInfo = "<br>Points <b>bounded</b> by <br/>("+minMaxLoc[0] + ", " +minMaxLoc[1] + ")";
       maxInfo = "and <br/>("+minMaxLoc[2] + ", " +minMaxLoc[3] + ")";
       $("#placeholder").html("");
     }else if (responseType == "list"){
       modeInfo = "<p><b>Mode</b> response: "+modeNumber(d)[0] + " with "+modeNumber(d)[1]+" counts</p>";
     }else{
       d = d.sort(function(a,b) {
         return a - b;
         });
       meanInfo = "<p><b>Mean</b> response: "+meanNumber(d) + "</p>";
       medianInfo = "<p><b>Median</b> response: "+medianNumber(d) + "</p>";
       modeInfo = "<p><b>Mode</b> response: "+modeNumber(d)[0] + " with "+modeNumber(d)[1]+" counts</p>";
       var minMaxNum = [null, null];
       minMaxNum = minMaxNumber(d);
       minInfo = "<p><b>Minimum</b> response: "+minMaxNum[0] + "</p>";
       maxInfo = "<p><b>Maximum</b> response: "+minMaxNum[1] + "</p>";
       $("#placeholder").html("");
     }
     
// ///also taken from time to plot counts chart
     if (responseType=="list" || responseType == "likert" || responseType == "likert_smileys") {
       var keyIndexes = [];
       var keyCount = 0;
       var ticks = [];
       var maxCount = 0;
       var graphData = [];

       for (var key in orderedKeys) { // here use the answer indexes
                                      // (list|likert), not the insertion order
                                      // in counts
         if (!orderedKeys.hasOwnProperty(key)) {
           continue;
         }
         var countForKey = counts[orderedKeys[key]];
         if (!countForKey) {
           countForKey = 0;
         } 
         if (countForKey > maxCount) {
           maxCount = countForKey;
         }
         keyIndexes[keyCount++] = { count : countForKey, key : orderedKeys[key] };        
       }
       // need to assign numbers to each key and store stuff by that number
       for (var i = 0; i < keyCount; i++) {
         graphData.push([i, keyIndexes[i].count]);
         ticks.push([i, truncString(keyIndexes[i].key, 6)]);  
       }
       var data = [ {label: 'count', data : graphData} ]; 
       var options = {series : {
                    lines: {show: false, steps: false },
                    bars: {show: true, barWidth: 0.7, align: 'center',},
                  },
                  xaxis: {ticks: ticks},
                  yaxis : {min: 0, max: maxCount, minTickSize : 1, tickDecimals : 0 }
                  };  
       $.plot($("#placeholder"), data, options);
     }
   }
          
   var totalResponsesInfo = "<b>Total</b> responses: "+d.length+"<br><b>from</b> "+isoDateString(new Date(experimentData[0].responseTime))+" to "+isoDateString(new Date(experimentData[experimentData.length-1].responseTime));
   if (meanInfo == "") {
     meanInfo = "<p>No mean information is given for "+responseType + " data.</p>";
   }
   if (medianInfo == "") {
     medianInfo = "<p>No median information is given for "+responseType + " data.</p>";
   }
   if (modeInfo == "") {
     modeInfo = "<p>No mode information is given for "+responseType + " data.</p>";
   }
   if (minInfo == "") {
     minInfo = "<p>No min or max information is given for "+responseType + " data.</p>";
     maxInfo = " ";
   }
   $("#titleHtml").html("<p>"+title+"</p>");
   $("#meanHtml").html(meanInfo);
   $("#medianHtml").html(medianInfo);
   $("#modeHtml").html(modeInfo);
   $("#minHtml").html(minInfo);
   $("#totalResponses").html(totalResponsesInfo);

}