<center>
<div style="height:500px;">
	
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
		<!--
		<script src="https://github.com/chartjs/Chart.js/releases/download/v2.6.0/Chart.min.js"></script>
		-->
		
		<script src="https://www.chartjs.org/dist/2.7.2/Chart.bundle.js"></script>
		
		<style>
			body{
				font-family: Arial;
			}
		</style>
	
	

		<script>
			var ctx = "https://community-ninja-board-community-ninja-board.apps.d1.casl.rht-labs.com/community-ninja-board";
			//var ctx = "http://localhost:8082/community-ninja-board";
			//var ctx = "${pageContext.request.contextPath}";
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
			width: 100%;
			border-radius: 12px;
			padding: 10px;
			background: rgb(0,65,83);
			//background: rgb(163,219,232);
			
			font-family: Arial;
			font-size: 14pt;
			color: #eee;
			/*
			position: relative;
			top: 45px;
			*/
		}
		.cardName{
			vertical-align: top;
			font-weight: bold;
			font-size: 28pt;
			color: #eee;
		}
		.cardScore{
			vertical-align: top;
			text-align: center;
			font-weight: bold;
			font-size: 34pt;
			color: #eee;
		}
		.cardRow{
			width:100px;
		}
		.cardScoreText{
			vertical-align: top;
			text-align: center;
			font-size:8pt;
		}
		.icon{
			height: 25px;
		}
		.ninjaIcon{
			clip: rect(0px,50px,25px,0px);
			height: 80px;
		}
		.graph-label{
			font-size: 22pt;
			font-family: Arial;
			color: #eee;
			font-weight: bold;
			position:relative;
			top:20px;
		}
		</style>

		
		<table border=0 style="width:1000px;">
			<tr>
				<td colspan="2">
					<table class="card2" border=0>
						<tr>
							<td colspan="6"><span class="cardName" id="_error"></span><br/><span id="_error2"></td>
						</tr>
						<tr>
							<td class="cardName" colspan="2"><span id="_displayName"></span></td>
							<td class="cardScore" rowspan="4"><img class="ninjaIcon" id="_level"></img></td>
							<td class="cardScore"><span id="_Trello">0</span></td>
							<td class="cardScore"><span id="_Github">0</span></td>
							<td class="cardScore"><!-- chat counter --></td>
						</tr>
						<tr>
							<td class="cardRow">
								<img class="icon" src="https://www.redhat.com/profiles/rh/themes/redhatdotcom/img/logo.png">
							</td>
							<td><span id="_userId"></span></td>
							<td class="cardScoreText" rowspan="3">trello</td>
							<td class="cardScoreText" rowspan="3">github</td>
							<td class="cardScoreText" rowspan="3"><!--chat--></td>
						</tr>
						<tr>
							<td class="cardRow"><img class="icon" src="https://d2k1ftgv7pobq7.cloudfront.net/meta/u/res/images/brand-assets/Logos/0099ec3754bf473d2bbf317204ab6fea/trello-logo-blue.png"></td>
							<td><span id="_trelloId"></span></td>
						</tr>
						<tr>
							<!--
							<td class="cardRow"><img class="icon" src="https://assets-cdn.github.com/images/modules/logos_page/GitHub-Mark.png"></td>
							<td class="cardRow"><img style="height: 32px;" src="https://www.virtualstacks.com/wp-content/uploads/2018/02/GitHub-Logo-Web-Development-Community.png"></td>
							-->
							<td class="cardRow"><img  style="height:20px;" src="https://assets-cdn.github.com/images/modules/logos_page/GitHub-Logo.png"></td>
							<td><span id="_githubId"></span></td>
						</tr>
						
						<tr>
							<td colspan="6" style="height: 20px;"><!-- SPACER ONLY --></td>
						</tr>
						
						<tr>
							<td colspan="6">
								
								<table border=0 style="width:100%;height:400px">
									<tr>
										<td style="width:50%;">
											<!-- #################### -->
											<!-- BOTTOM LEFT DOUGHNUT -->
											<!-- #################### -->
											<script>
												function leaderboardRefresh(){ return refreshGraph0('points', 'Doughnut', colors); }
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
												function breakdownRefresh(){ return refreshGraph0('breakdown', 'Doughnut', colorsReverse); }
											</script>
											<div id="breakdown_container" class="graph" >
												<canvas id="breakdown"></canvas>
												<center><span class="graph-label">Points Breakdown</span></center>
									    </div>
										</td>
									</tr>
								</table>
							</td>
						</tr>
						</tr>
					</table>
				</td>
			</tr>
		</table>
		
		
		
	



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
  console.log("statusCode="+xhr.status);
  
	var json=JSON.parse(xhr.responseText);
  if (xhr.status==500){
    // json['displayName'] is not just a username in the event of a 500 error. its a message. need to change this at some point
  	$("#_error").html(json['displayName']+". Please click <a style='color:white;' target='_new' href='https://mojo.redhat.com/external-link.jspa?url=https%3A%2F%2Fdocs.google.com%2Fforms%2Fd%2Fe%2F1FAIpQLSdWGcCks2zKKnVoZFQz3CieLQDc1lsSex_Knwh_-eyRm0ZQTg%2Fviewform'>here</a> to register");
  	$("#_error2").html("Please note registration takes approx. 24 hours");
  }
  
  if (xhr.status==200){
		if (json['displayName']==undefined){
		  $("#_displayName").text(json['username']);
		}else{
			$("#_displayName").text(json['displayName']);
		}
		Object.keys(json).forEach(function(key) {
	    value = json[key];
			
	    
	    if (null!=document.getElementById("_"+key)){
	    	console.log("setting [_"+key+"] to ["+value+"]");
		    $("#_"+key).text(value);
	    	
	    	if (key=="level"){
					document.getElementById("_level").src=ctx+"/images/"+value.toLowerCase()+"_belt_icon.png";
	    	}else{
	    	  //$("#_"+key).text(value);
	    		//document.getElementById("_"+key).innerText=value;
	    	}
	    }else{
	    	//console.log("NOT setting [_"+key+"] to ["+value+"]");
	    }
	    //console.log(value);
		});
	}
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

//var colors=["rgba(204,0,0,X)","rgba(0,65,83,X)","rgba(146,212,0,X)","rgba(163,219,232,X)","rgba(59,0,131,X)","rgba(240,171,0,X)","rgba(0,122,135,X)"];
//var colors2=["rgba(0,122,135,X)","rgba(240,171,0,X)","rgba(59,0,131,X)","rgba(163,219,232,X)","rgba(146,212,0,X)","rgba(0,65,83,X)","rgba(204,0,0,X)"];

var purple="rgba(59,0,131,X)";
var lightOrgange="rgba(240,171,0,X)";
var darkOrgange="rgba(236,122,8,X)";
var lightGreen="rgba(146,212,0,X)";
var darkGreen="rgba(63,156,53,X)";
var lightBlue="rgba(0,185,228,X)";
var vividBlue="rgba(0,136,206,X)";
var paleBlue="rgba(163,219,232,X)";
var tealBlue="rgba(0,122,135,X)";

var colors=[tealBlue,paleBlue,darkOrgange,darkGreen,lightBlue,lightOrgange,lightGreen,vividBlue]
var colorsReverse=colors.slice().reverse();
//var bcolors=["rgba(0,65,83,X)","rgba(0,136,206,X)","rgba(63,156,53,X)","rgba(236,122,8,X)","rgba(,X)","rgba(,X)"]


function buildChart(uri, chartElementName, type, clrs){
  var xhr = new XMLHttpRequest();
  xhr.open("GET", ctx+uri, true);
  xhr.send();
  xhr.onloadend = function () {
    var data=JSON.parse(xhr.responseText);
    var ctx = document.getElementById(chartElementName).getContext("2d");
    
  	var backgroundColor=[];
  	var borderColor=[];
  	for (i=0;i<data.datasets[0].data.length;i++){
  		backgroundColor[i]=clrs[i].replace("X","1"); // replace is the opacity
  		borderColor[i]=clrs[i].replace("X","0.8"); // replace is the opacity
  	}
  	data.datasets[0].backgroundColor=backgroundColor;// push({"backgroundColor2":backgroundColor});
  	data.datasets[0].borderColor=borderColor;
    
    if ("points"==chartElementName){
	    var total=data.datasets[0].data[0]+data.datasets[0].data[1];
	    var current=data.datasets[0].data[0];
	    if (0==current && 0==total){
	      var percentage="0%";
	    }else{
	    	var percentage=Math.round((current/total)*100)+"%";
	    }
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
			          color: '#eee', // Default is #000000
			          fontStyle: 'Arial', // Default is Arial
			          sidePadding: 20 // Default is 20 (as a percentage)
							}
						},
						legend:{
							display: true,
							position: "bottom",
							labels: {
								fontColor: "#eee",
								fontSize: 12
							}
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

function refreshGraph0(graphName, type, colors){
  buildChart(graphs[graphName], graphName, type, colors);
}
function refresh(){
	leaderboardRefresh();
	breakdownRefresh();
}
refresh();
</script>
</div>
</center>