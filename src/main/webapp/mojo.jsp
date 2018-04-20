<html>
	<head>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
		<script src="https://github.com/chartjs/Chart.js/releases/download/v2.6.0/Chart.min.js"></script>
		
		<style>
			.graph_header_main{
				font-family: Arial;
				font-size: 24pt;
				text-align: center;
			}
			body{
				font-family: Arial;
			}
		</style>
	</head>
	<body onload="refresh();">

		<script>
			var ctx = "https://community-ninja-board-community-ninja-board.apps.d2.casl.rht-labs.com/community-ninja-board";
			//var ctx = "http://localhost:8082/community-ninja-board";
			
			function leaderboardRefresh(){
				return refreshGraph0('leaderboard', 'HorizontalBar');
			}
		</script>
		
		<table border=0 style="width:100%">
			<tr>
				<td style="vertical-align: top;">
					<div id="leaderboard_container" class="graph" style="width:1200px; height:100%;">
						<div class="graph_header">
							<div class="graph_header_main">Top 10 Ninjas!</div>
						</div>
						<canvas id="leaderboard" width="1200" height="800"></canvas>
					</div>
				</td>
				<td style="vertical-align: top;width:100%">
					<div style="float: right;width:100%">
						

						
						<table style="border:1px solid black;width:60%">
							<tr><td>Name:</td>           <td><span id="_displayName"></span></td></tr>
							<tr><td>Username:</td>       <td><span id="_userId"></span></td></tr>
							<tr><td>GitHub ID:</td>      <td><span id="_githubId"></td></tr>
							<tr><td>Trello ID:</td>      <td><span id="_trelloId"></td></tr>
							<tr><td>Email:</td>          <td></td></tr>
							<tr><td>Ninja Level:</td>    <td></td></tr>
							<tr><td>To Next Level:</td>  <td></td></tr>
						</table>
						
					</div>
				</td>
			</tr>
		</table>
		
	</body>
</html>


<script>
var hbarOptions = {
  annotateDisplay : true,
  yAxisMinimumInterval:1,
  barBorderRadius: 5,
  inGraphDataXPosition: 3,
  inGraphDataTmpl : "<\%=v1\%> (<\%=v3\%>)" ,
  graphMin : 0,
}

function resetCanvas(chartElementName){
  $('#'+chartElementName).remove(); // this is my <canvas> element
  $('#'+chartElementName+'_container').append('<canvas id="'+chartElementName+'"><canvas>');
  var canvas = document.querySelector('#'+chartElementName);
  var ctx = canvas.getContext('2d');
  ctx.canvas.width = $('#'+chartElementName+'_container').width();
  ctx.canvas.height = $('#'+chartElementName+'_container').height()+100;
}
function buildChart(uri, chartElementName, type){
  var xhr = new XMLHttpRequest();
  xhr.open("GET", ctx+uri, true);
  xhr.send();
  xhr.onloadend = function () {
    var json=JSON.parse(xhr.responseText);
    var income = document.getElementById(chartElementName).getContext("2d");
    if (type=="Bar"){
	    new Chart(income).Bar(json, barOptions);
	  }else if (type=="BarNoLabels"){
	    var barOptions2=JSON.parse(JSON.stringify(barOptions));
	    barOptions2.inGraphDataShow=false;
	    new Chart(income).Bar(json, barOptions2);
	  }else if (type=="HorizontalBar"){
	    var barOptions3=JSON.parse(JSON.stringify(hbarOptions));
	    barOptions3.inGraphDataShow=true;
	    barOptions3.inGraphDataAlign="right";
	    barOptions3.inGraphDataVAlign="top";
	    barOptions3.inGraphDataPaddingX=10;
	    barOptions3.inGraphDataAlign="left"
	    
	    resetCanvas(chartElementName);
	    income = document.getElementById(chartElementName).getContext("2d");
	    
	    new Chart(income, {
                type: 'horizontalBar', 
                data: json,
                options: {"scales":{"xAxes":[{"ticks":{"beginAtZero":true}}]},legend: {display:false}}
            });
	  }else if (type=="BarShortLabels"){
	    var barOptions3=JSON.parse(JSON.stringify(barOptions));
	    barOptions3.inGraphDataTmpl="<\%=rename(v1)\%>";
	    new Chart(income).Bar(json, barOptions3);
    }else if (type=="Pie"){
	    new Chart(income).Pie(json, pieOptions);
    }else if (type=="Line"){
      resetCanvas(chartElementName);
      income = document.getElementById(chartElementName).getContext("2d");
	    new Chart(income, {
                type: 'line', 
                data: json,
                options: {"scales":{"xAxes":[{"ticks":{"beginAtZero":true}}]},legend: {display:true}}
            });
    }
  }
}

var graphs={
  "leaderboard":         "/api/leaderboard/10",
};

function refreshGraph0(graphName, type){
  buildChart(graphs[graphName], graphName, type);
}
function refresh(){
	leaderboardRefresh();
}
</script>


<script>

if(undefined!=window.parent._jive_current_user){
	var username=window.parent._jive_current_user.username;
	var displayName=window.parent._jive_current_user.displayName;
}
if(username==undefined) username="mallen";

var xhr = new XMLHttpRequest();
xhr.open("GET", ctx+"/api/scorecard/"+username, true);
xhr.send();
xhr.onloadend = function () {
	var json=JSON.parse(xhr.responseText);
	Object.keys(json).forEach(function(key) {
    value = json[key];
    
    if (null!=document.getElementById("_"+key)){
    	console.log("setting [_"+key+"] to ["+value+"]");
    	document.getElementById("_"+key).innerText=value;
    }else{
    	console.log("NOT setting [_"+key+"] to ["+value+"]");
    }
    //console.log(value);
	});
}


//alert('Display Name: ' + window.parent._jive_current_user.displayName +  
//'\nAnonymous: ' + window.parent._jive_current_user.anonymous+  
//'\nUsername: ' + window.parent._jive_current_user.username +  
//'\nID: ' + window.parent._jive_current_user.ID+  
//'\nEnabled: ' + window.parent._jive_current_user.enabled +  
//'\nAvatar ID: ' + window.parent._jive_current_user.avatarID);  
</script> 