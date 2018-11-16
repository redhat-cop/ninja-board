<%@page import="java.io.File"%>
<%@page contentType="text/html"%>

<%@include file="header.jsp"%>
<script src="js/Chart-2.6.0.min.js"></script>

<%
String graphHeight="100%";
%>
    
    
    
    <style>
			td{
				padding-top:40px;
			}
    </style> 
    
    
</head>
<body onload="refresh();">
  <%@include file="nav.jsp"%>
  <table style="width:70%; margin: 0 auto;">
	  <tr>
	  	<td align="center">
				<script>
					function leaderboardRefresh(){
						return refreshGraph0('leaderboard', 'HorizontalBar');
					}
				</script>
				<div id="leaderboard_container" class="graph" style="width:1200px; height:<%=graphHeight%>">
					<div class="graph_header">
						<div class="graph_header_main">Ninja Leaderboard - Top 10</div>
					</div>
					<canvas id="leaderboard" width="1200" height="800"></canvas>
				</div>
				
	  	</td>
	  </tr>
	</table>

<script>


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
  var ctx = "${pageContext.request.contextPath}";
  xhr.open("GET", ctx+uri, true);
  xhr.send();
  xhr.onloadend = function () {
    var json=JSON.parse(xhr.responseText);
    var income = document.getElementById(chartElementName).getContext("2d");
	  if (type=="HorizontalBar"){
	    
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

</body>
</html>



