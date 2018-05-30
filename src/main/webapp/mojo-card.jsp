<html>
	<head>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
		<!--
		<script src="https://github.com/chartjs/Chart.js/releases/download/v2.6.0/Chart.min.js"></script>
		-->
		
		<script src="http://www.chartjs.org/dist/2.7.2/Chart.bundle.js"></script>
		
		<style>
			body{
				font-family: Arial;
			}
		</style>
	</head>
	<body>

		<script>
			//var ctx = "https://community-ninja-board-community-ninja-board.apps.d2.casl.rht-labs.com/community-ninja-board";
			//var ctx = "http://localhost:8082/community-ninja-board";
			var ctx = "${pageContext.request.contextPath}";
		</script>
		
		
		<!--######-->
		<!-- CARD -->
		<!--######-->
		<style>
		//.card{
		//	border:1px solid #BBB;
		//	width:80%;
		//	border-radius: 5px;
		//	padding: 10px;
		//	background: #EEE;
		//	position: relative;
		//	top: 45px;
		//}
		//.card tr td{
		//}
		//.cardField{
		//	color: #444;
		//	font-weight: bold;
		//}
		.card2{
			border:1px solid #BBB;
			width:100%;
			border-radius: 5px;
			padding: 10px;
			background: #EEE;
			font-family: Arial;
			font-size: 14pt;
			color: #444;
			/*
			position: relative;
			top: 45px;
			*/
		}
		//.cardName{
		//	vertical-align: top;
		//  font-weight: bold;
		//	font-size: 28pt;
		//	color: #444;
		//}
		.cardScore{
			vertical-align: top;
			text-align: center;
			font-weight: bold;
			font-size: 34pt;
			color: #444;
		}
		//.cardRow{
		//	width:20%;
		//}
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
		.graph-label{
			font-size: 18pt;
			font-family: Arial;
			color: #888888;
		}
		</style>

		
		<table border=0>
			<tr>
				<td colspan="2">
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
				</td>
			</tr>
			
			<tr>
				<td style="width:50%;">
					<!-- #################### -->
					<!-- BOTTOM LEFT DOUGHNUT -->
					<!-- #################### -->
					<script>
						function leaderboardRefresh(){ return refreshGraph0('points', 'Doughnut'); }
					</script>
					<div id="leaderboard_container" class="graph" >
						<canvas id="points"></canvas>
						<center><span class="graph-label">Next Level</span></center>
					</div>
				</td>
				
				<td>
					<!-- ##################### -->
					<!-- BOTTOM RIGHT DOUGHNUT -->
					<!-- ##################### -->
					<script>
						function breakdownRefresh(){ return refreshGraph0('breakdown', 'Doughnut'); }
					</script>
					<div id="breakdown_container" class="graph" >
						<canvas id="breakdown"></canvas>
						<center><span class="graph-label">Points Breakdown</span></center>
			    </div>
				</td>
			</tr>
		</table>
		
		
		
	</body>
</html>


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




		

<script>
	Chart.pluginService.register({
		beforeDraw: function (chart) {
			if (chart.config.options.elements.center) {
        //Get ctx from string
        var ctx = chart.chart.ctx;
        
				//Get options from the center object in options
        var centerConfig = chart.config.options.elements.center;
      	var fontStyle = centerConfig.fontStyle || 'Arial';
				var txt = centerConfig.text;
        var color = centerConfig.color || '#000';
        var sidePadding = centerConfig.sidePadding || 20;
        var sidePaddingCalculated = (sidePadding/100) * (chart.innerRadius * 2)
        //Start with a base font of 30px
        ctx.font = "30px " + fontStyle;
        
				//Get the width of the string and also the width of the element minus 10 to give it 5px side padding
        var stringWidth = ctx.measureText(txt).width;
        var elementWidth = (chart.innerRadius * 2) - sidePaddingCalculated;

        // Find out how much the font can grow in width.
        var widthRatio = elementWidth / stringWidth;
        var newFontSize = Math.floor(30 * widthRatio);
        var elementHeight = (chart.innerRadius * 2);

        // Pick a new font size so it will not be larger than the height of label.
        var fontSizeToUse = Math.min(newFontSize, elementHeight);

				//Set font settings to draw it correctly.
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        var centerX = ((chart.chartArea.left + chart.chartArea.right) / 2);
        var centerY = ((chart.chartArea.top + chart.chartArea.bottom) / 2);
        ctx.font = fontSizeToUse+"px " + fontStyle;
        ctx.fillStyle = color;
        
        //Draw text in center
        ctx.fillText(txt, centerX, centerY);
			}
		}
	});

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
    var data=JSON.parse(xhr.responseText);
    var ctx = document.getElementById(chartElementName).getContext("2d");
    
    
    if ("points"==chartElementName){
	    var total=data.datasets[0].data[0]+data.datasets[0].data[1];
	    var current=data.datasets[0].data[0];
	    var percentage=Math.round((current/total)*100)+"%";
    }else{
    	var percentage="";
    }
    
    if (type=="Doughnut"){
	    var myDoughnutChart = new Chart(ctx, {
			    type: 'doughnut',
			    data: data,
			    options: {
						elements: {
							center: {
								text: percentage,
			          color: '#FF6384', // Default is #000000
			          fontStyle: 'Arial', // Default is Arial
			          sidePadding: 20 // Default is 20 (as a percentage)
							}
						},
						legend:{
							display:false
						}
					}
			});
    }
  }
}

var graphs={
  "points":         "/api/scorecard/nextlevel/"+getUsername(),
  "breakdown":      "/api/scorecard/breakdown/"+getUsername(),
};

function refreshGraph0(graphName, type){
  buildChart(graphs[graphName], graphName, type);
}
function refresh(){
	leaderboardRefresh();
	breakdownRefresh();
}
refresh();
</script>