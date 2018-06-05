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
			//var ctx = "${pageContext.request.contextPath}";
			
			function leaderboardRefresh(){
				return refreshGraph0('leaderboard', 'HorizontalBar');
			}
		</script>
		
		<table border=0 style="width:100%">
			<tr>
				<td style="vertical-align: top;">
					<!--#######-->
					<!-- GRAPH -->
					<!--#######-->
					<div id="leaderboard_container" class="graph" style="width:1200px; height:100%;">
						<div class="graph_header">
							<div class="graph_header_main cardField">Top 10 Ninjas!</div>
						</div>
						<canvas id="leaderboard" width="1200" height="800"></canvas>
					</div>
				</td>
				
				
				<td style="vertical-align:top;width:100%">
					<!--######-->
					<!-- CARD -->
					<!--######-->
					<div style="float:right;width:100%;text-align:center">
						<style>
						.card{
							border:1px solid #BBB;
							width:80%;
							border-radius: 5px;
							padding: 10px;
							background: #EEE;
							position: relative;
							top: 45px;
						}
						.card tr td{
						}
						.cardField{
							color: #444;
							font-weight: bold;
						}
						.card2{
							border:1px solid #BBB;
							width:80%;
							border-radius: 5px;
							padding: 10px;
							background: #EEE;
							font-family: Arial;
							font-size: 14pt;
							color: #444;
							position: relative;
							top: 45px;
						}
						.cardName{
							vertical-align: top;
						  font-weight: bold;
							font-size: 28pt;
							color: #444;
						}
						.cardScore{
							vertical-align: top;
							text-align: center;
							font-weight: bold;
							font-size: 34pt;
							color: #444;
						}
						.cardRow{
							width:20%;
						}
						.cardScoreText{
							vertical-align: top;
							text-align: center;
							font-size:8pt;
						}
						.icon{
							height: 25px;
						}
						.icon2{
							clip: rect(0px,50px,25px,0px);
							height: 60px;
						}
						</style>
						<table class="card2" border=0>
							<tr>
								<td class="cardName" colspan="2"><span id="_displayName"></span></td>
								<td class="cardScore"><img class="icon2" id="_level"></img></td>
								<td class="cardScore"><span id="_Trello">0</span></td>
								<td class="cardScore"><span id="_Github">0</span></td>
								<td class="cardScore">0</td>
							</tr>
							<tr>
								<td class="cardRow">
									<img class="icon" src="https://www.redhat.com/profiles/rh/themes/redhatdotcom/img/logo.png">
								</td>
								<td><span id="_userId"></span></td>
								<td class="cardScoreText" rowspan="3"></td>
								<td class="cardScoreText" rowspan="3">trello</td>
								<td class="cardScoreText" rowspan="3">github</td>
								<td class="cardScoreText" rowspan="3">chat</td>
							</tr>
							<tr>
								<td class="cardRow"><img class="icon" src="https://d2k1ftgv7pobq7.cloudfront.net/meta/u/res/images/brand-assets/Logos/0099ec3754bf473d2bbf317204ab6fea/trello-logo-blue.png"></td>
								<td><span id="_githubId"></span></td>
							</tr>
							<tr>
								<td class="cardRow"><img class="icon" src="https://assets-cdn.github.com/images/modules/logos_page/GitHub-Mark.png"></td>
								<td><span id="_trelloId"></span></td>
							</tr>
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
  xhr.open("GET", ctx+uri+"?user="+getUsername(), true);
  xhr.send();
  xhr.onloadend = function () {
    var json=JSON.parse(xhr.responseText);
    var income = document.getElementById(chartElementName).getContext("2d");
    if (type=="HorizontalBar"){
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

function getUsername(){
	if(undefined!=window.parent._jive_current_user){
		var username=window.parent._jive_current_user.username;
		var displayName=window.parent._jive_current_user.displayName;
	}
  if(username==undefined) username="mallen";
	return username;
}


var xhr = new XMLHttpRequest();
xhr.open("GET", ctx+"/api/scorecard/summary/"+getUsername(), true);
xhr.send();
xhr.onloadend = function () {
	var json=JSON.parse(xhr.responseText);
	Object.keys(json).forEach(function(key) {
    value = json[key];
    
    if (null!=document.getElementById("_"+key)){
    	console.log("setting [_"+key+"] to ["+value+"]");
    	
    	if (key=="level"){
    		var base="https://mojo.redhat.com/servlet/JiveServlet/downloadImage/102-1152994-22-12306";
    		var blue=base+"12/rh-services-communities-practice-icon-f9689kc-201710_blue_belt.png";
				var grey=base+"13/rh-services-communities-practice-icon-f9689kc-201710_grey_belt.png";
				var red=base+"14/rh-services-communities-practice-icon-f9689kc-201710_red_belt.png";
				var black=base+"15/rh-services-communities-practice-icon-f9689kc-201710_black_belt.png";
				if (value.toLowerCase()=="blue") document.getElementById("_level").src=blue;
				if (value.toLowerCase()=="grey") document.getElementById("_level").src=grey;
				if (value.toLowerCase()=="red") document.getElementById("_level").src=red;
				if (value.toLowerCase()=="black") document.getElementById("_level").src=black;
				
    	}else{
    		document.getElementById("_"+key).innerText=value;
    	}
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