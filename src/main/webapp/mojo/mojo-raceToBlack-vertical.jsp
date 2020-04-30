<center>
<table id="race" cellspacing="0" cellpadding="0">
<tr><td colspan="4" class="header"><center><h2>Race to<br/> Black Belt!</h2><div class="subtitle">First to 75 points wins the race!</div></center></td></tr>
</table>
</center>

<style>
  body{
    font-family: Overpass, Sans-serif;
    color: #333;
  }
  table{
    border: solid 1px #ddd;
    cellspacing: 0px;
    cellpadding: 0px;
    width:210px; /* width of the mojo widget in Overview mode, remove for Tiles mode*/
  }
  /*
  table tr td:not(:first-child){
  table tr td{
  */
  table tr:not(:first-child) td{
    padding: 4px;
    border-bottom: solid 1px #ddd;
  }
  table tr{
  }
  .header{
    background-color: #007a87;
    color: white;
    padding-top: 15px;
  }
  .header h2{
    margin-bottom: 3px;
  }
  .subtitle{
  	font-size: 10pt;
  	color: white;
  	padding-bottom: 3px;
  }
  .header td{
    font-weight: bold; 
  }
  .avatar{
    border: 0px solid black;
    border-radius:         50px;
    -webkit-border-radius: 50px;
    -moz-border-radius:    50px;
  }
  .col{
    padding-left:  10px;
    padding-right: 10px;
  }
  .col-3{
    text-align: center;
  }
  .even td{
    background-color: f9f9f9;
  }
  .belt{ color: white; }
  .belt-black{ background-color: black !important;}
  .belt-red{   background-color: #a21c20  !important;}
  .belt-grey{  background-color: #aaaaaa  !important;}
  .belt-blue{  background-color: #a4dbea  !important;}
</style>

<script>
  var topX=10;
  
  var ctx = "https://ninja-graphs-ninja-graphs.6923.rh-us-east-1.openshiftapps.com/ninja-graphs";
  
  var xhr = new XMLHttpRequest();
  //xhr.open("GET", ctx+"/api/proxy/leaderboard_10", true);
  xhr.open("GET", ctx+"/api/proxy/ninjas", true);
  xhr.send();
  xhr.onloadend = function () {
    var json=JSON.parse(xhr.responseText);
    
    var tableRef = document.getElementById('race');
    
    //for(var i=0;i<topX;i++){
    //	if (json['custom1'][i]=='mallen'){ json['custom1'].splice(i); json['labels'].splice(i); }
    //	if (json['custom1'][i]=='ablock'){ json['custom1'].splice(i); json['labels'].splice(i); }
    //	if (json['custom1'][i]=='esauer'){ json['custom1'].splice(i); json['labels'].splice(i); }
    //}
    var xx=1;
    for(var i=0;i<json['labels'].length;i++){
    	if (xx>10) break;
    	var custom1=json['custom1'][i].split("|");
    	if (custom1[0]=='mallen') continue;
    	if (custom1[0]=='ablock') continue;
    	if (custom1[0]=='esauer') continue;
    	
    	
      var newRow   = tableRef.insertRow(tableRef.rows.length);
      newRow.className=i%2?"even":"odd";
      // add numeric
      var c1  = newRow.insertCell(0);
      //var c1t  = document.createTextNode(i+1);
      var c1t  = document.createTextNode(xx++);
      c1.className="col col-1";
      c1.appendChild(c1t);
      
      // add Image
      var c2  = newRow.insertCell(1);
      var c2t  = document.createElement('img');
      c2t.src="https://mojo.redhat.com/people/"+custom1[0]+"/avatar/200.png?a=925089";
      c2t.style="width: 50px; height: 50px"
    	c2t.className="avatar";
      c2.className="col col-2";
      c2.appendChild(c2t);
      
      // add Name
      var c3  = newRow.insertCell(2);
      var c3t  = document.createTextNode("");
      c3.innerHTML=json['labels'][i] +"<br/>"+json['datasets'][0]['data'][i]+"pts";
      c3.className="col col-3 belt belt-"+custom1[1];
      c3.appendChild(c3t);
      
      // add Pts
      //var c4  = newRow.insertCell(3);
      //var c4t  = document.createTextNode(json['datasets'][0]['data'][i]+"pts");
      //c4.className="col col-4";
      //c4.appendChild(c4t);
    }
    setTimeout(function(){ resizeParent(); }, 500);
  }
  
  //function resizeParent() {
	//  var e=window.parent.document.getElementsByClassName("htmlWidgetIframe");
	//  for (var i=0;i<e.length;i++){
  //    e[i].style.height = e[i].contentWindow.document.body.scrollHeight + 'px';
	//  }
	//}
  
  function resizeParent() {
	  window.frameElement.style.height=window.frameElement.contentWindow.document.body.scrollHeight+'px';
	}
</script>
