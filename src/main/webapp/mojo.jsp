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
		</style>
	</head>
	<body onload="refresh();">

		<script>
			function leaderboardRefresh(){
				return refreshGraph0('leaderboard', 'HorizontalBar');
			}
		</script>
		<div id="leaderboard_container" class="graph" style="width:1200px; height:100%">
			<div class="graph_header">
				<div class="graph_header_main">Top 10 Ninjas!</div>
			</div>
			<canvas id="leaderboard" width="1200" height="800"></canvas>
		</div>

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
//  var ctx = "/community-ninja-board";
  var ctx = "https://community-ninja-board-community-ninja-board.apps.d2.casl.rht-labs.com/community-ninja-board";
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