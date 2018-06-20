<%@page import="java.io.File"%>
<%@page import="
java.util.Set,
java.util.LinkedHashSet
"%>
<%@page contentType="text/html"%>

<%
String graphHeight="100%";
%>
<html>
<head>
   <meta http-equiv="cache-control" content="no-cache"/>
   <meta charset="utf-8"/>
   <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
   <meta name="viewport" content="width=device-width, initial-scale=1"/>
   <title>Communities of Practice</title>
   <link href="css/style2.css" type="text/css" rel="stylesheet"/>
   <link href="css/bootstrap.min.css" rel="stylesheet"/>
    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    
    <script src="js/jquery-1.11.3.min.js"></script>
    <!--
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    -->
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/bootstrap.min.js"></script>
    <!--
    <script src="js/Chart.min.js"></script>
    <script type="text/javascript" src="https://cdn.rawgit.com/bebraw/Chart.js.legend/master/src/legend.js"></script>
    <script type="text/javascript" src="https://cdn.rawgit.com/Regaddi/Chart.js/master/Chart.min.js"></script>
    <script type="text/javascript" src="https://cdn.rawgit.com/Regaddi/Chart.js/0.2/Chart.min.js"></script>
    <script type="text/javascript" src="https://cdn.rawgit.com/nnnick/Chart.js/master/Chart.min.js"></script>
    -->
    <!--
    <script type="text/javascript" src="https://cdn.rawgit.com/FVANCOP/ChartNew.js/master/ChartNew.js"></script>
    -->
    
    <!-- ChartJS v1 
    <script src="js/ChartNew.js"></script>
    -->
    
    <!-- ChartJS v2 
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.2.1/Chart.min.js"></script>
    https://github.com/chartjs/Chart.js/releases
    <script src="http://www.chartjs.org/dist/2.7.2/Chart.bundle.js"></script>

    <script src="https://github.com/chartjs/Chart.js/releases/download/v2.6.0/Chart.min.js"></script>
    -->
    <script src="js/Chart-2.6.0.min.js"></script>
    
    
    
    
    <style>
			td{
				padding-top:40px;
			}
    </style> 
    
    
</head>
<body onload="refresh();">
  <%@include file="nav.jsp"%>
  <center>
  <table style="width:70%">
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


//var barOptions = {
////  legend : true,
//  annotateDisplay : true,
//  showXLabels: true,
//  showYLabels: true,
//  yAxisMinimumInterval:1,
//  inGraphDataShow : true, 
////  inGraphDataAlign : "right",
////  inGraphDataVAlign : "bottom",
////  inGraphDataFontColor : "white",
////  inGraphDataTmpl : "" ,
////  inGraphDataRotate : "-85",
//  barBorderRadius: 5,
//    
//  inGraphDataAlign : "center",
//  inGraphDataVAlign : "bottom",
//  inGraphDataFontColor : "black",
//  inGraphDataTmpl : "" ,
//  inGraphDataRotate : "0",
//        inGraphDataPaddingX: 0,
//        inGraphDataPaddingY: 20,
//        inGraphDataAlign: "center",
//        inGraphDataVAlign: "top",
//        inGraphDataXPosition: 2,
//        inGraphDataYPosition: 1,
//        inGraphDataTmpl : "<\%=rename(v1)\%>" ,
//  inGraphDataFontSize : 14,
////  inGraphDataTmpl : "<\%=v1\%>  " ,
//  //inGraphDataTmpl : "<\%=v4\%> - <\%=v1\%>" ,
//  graphMin : 0,
//}
//
//var hbarOptions = {
////  legend : true,
//  annotateDisplay : true,
////  showXLabels: true,
////  showYLabels: true,
//  yAxisMinimumInterval:1,
////  inGraphDataShow : true, 
//  barBorderRadius: 5,
////  inGraphDataFontColor : "black",
////  inGraphDataRotate : "0",
////  inGraphDataPaddingX: 0,
////  inGraphDataPaddingY: 20,
////  inGraphDataAlign: "bottom",
////  inGraphDataVAlign: "bottom",
//  inGraphDataXPosition: 3,
////  inGraphDataYPosition: 1,
//  inGraphDataTmpl : "<\%=v1\%> (<\%=v3\%>)" , <!-- <\%=rename(v1)\%> -->
////  inGraphDataFontSize : 14,
//  graphMin : 0,
//}
//
//
//var lineOptions = {
//  yAxisMinimumInterval:1,
//	inGraphDataShow : true,
//	datasetFill : true,
//	scaleLabel: "<\%=value\%>",
//	scaleFontSize : 16,
//	canvasBorders : true,
//	graphTitleFontFamily : "'Arial'",
//	graphTitleFontSize : 24,
//	graphTitleFontStyle : "bold",
//	graphTitleFontColor : "#666",
////	footNote : "Footnote for the graph",
////	legend : true,
////	yAxisLabel : "Y Axis Label",
////	xAxisLabel : "X Axis Label",
////	yAxisUnit : "Y Unit",
//	annotateDisplay : true, 
//	dynamicDisplay : true
//}
//
//var lineOptions2 = {
////  legend : true,
//  annotateDisplay : true,
////  showXLabels: true,
////  showYLabels: true,
//  yAxisMinimumInterval:1,
//  inGraphDataShow : true, 
////  barBorderRadius: 5,
////  inGraphDataFontColor : "black",
////  inGraphDataTmpl : "" ,
////  inGraphDataRotate : "0",
////        inGraphDataPaddingX: 0,
////        inGraphDataPaddingY: 20,
////        inGraphDataAlign: "center",
////        inGraphDataVAlign: "top",
////        inGraphDataXPosition: 2,
////        inGraphDataYPosition: 1,
//        inGraphDataTmpl : "<\%=rename(v1)\%>" ,
////  inGraphDataFontSize : 14,
//  graphMin : 0,
////    graphMax : 125,
//}

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
//    if (type=="Bar"){
//	    new Chart(income).Bar(json, barOptions);
//	  }else if (type=="BarNoLabels"){
//	    var barOptions2=JSON.parse(JSON.stringify(barOptions));
//	    barOptions2.inGraphDataShow=false;
//	    new Chart(income).Bar(json, barOptions2);
//	  }else 
	  if (type=="HorizontalBar"){
//	    var barOptions3=JSON.parse(JSON.stringify(hbarOptions));
//	    barOptions3.inGraphDataShow=true;
//	    barOptions3.inGraphDataAlign="right";
//	    barOptions3.inGraphDataVAlign="top";
//	    barOptions3.inGraphDataPaddingX=10;
//	    barOptions3.inGraphDataAlign="left"
	    
	    resetCanvas(chartElementName);
	    income = document.getElementById(chartElementName).getContext("2d");
	    
	    new Chart(income, {
                type: 'horizontalBar', 
                data: json,
                options: {"scales":{"xAxes":[{"ticks":{"beginAtZero":true}}]},legend: {display:false}}
            });
//	  }else if (type=="BarShortLabels"){
//	    var barOptions3=JSON.parse(JSON.stringify(barOptions));
//	    barOptions3.inGraphDataTmpl="<\%=rename(v1)\%>";
//	    new Chart(income).Bar(json, barOptions3);
//    }else if (type=="Pie"){
//	    new Chart(income).Pie(json, pieOptions);
//    }else if (type=="Line"){
//      resetCanvas(chartElementName);
//      income = document.getElementById(chartElementName).getContext("2d");
//	    new Chart(income, {
//                type: 'line', 
//                data: json,
//                options: {"scales":{"xAxes":[{"ticks":{"beginAtZero":true}}]},legend: {display:true}}
//            });
    }
  }
}

var graphs={
  "leaderboard":         "/api/leaderboard/10",
};

function refreshGraph0(graphName, type){
  buildChart(graphs[graphName], graphName, type);
}

//function refreshGraph(graphName, months, filter, type){
//  buildChart(graphs[graphName].replace("{months}",months).replace("{geo}","").replace("{filter}",encodeURI(filter)), graphName, type);
//}
//function refreshGraph2(graphName, months, geo, filter, type){
//  buildChart(graphs[graphName].replace("{months}",months).replace("{geo}",geo).replace("{filter}",encodeURI(filter)), graphName, type);
//}
//function refreshGraph3(graphName, months, geo, filter, inclhome, type){
//  buildChart(graphs[graphName].replace("{months}",months).replace("{geo}",geo).replace("{filter}",encodeURI(filter)).replace("{inclhome}",inclhome), graphName, type);
//}

function refresh(){
	leaderboardRefresh();
}
		
</script>

</body>
</html>



