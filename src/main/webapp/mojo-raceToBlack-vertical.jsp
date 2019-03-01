<html>
<center>
<table id="race" cellspacing="0" cellpadding="0">
<tr><td colspan="4" class="header"><center><h2>"Race to Black!"</h2></center></td></tr>
<!-- 
<tr class="header"><td class="col">#</td><td class="col"></td><td class="col">Person</td><td class="col">Pts</td></tr>
-->
</table>
</center>

<style>
  body{
    font-family: Overpass;
    color: #333;
  }
  table{
    border: 1px black;
    cellspacing: 0;
    cellpadding: 0;
  }
  /*
  table tr td:not(:first-child){
  table tr td{
  */
  table tr:not(:first-child) td{
    padding: 4px;
    border-bottom: solid 1px #ccc;
  }
  table tr{
  }
  .header{
    background-color: #EEE;
    padding-top: 15px;
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
</style>

<script>
  var topX=10;
  
  var ctx = "https://ninja-graphs-ninja-graphs.6923.rh-us-east-1.openshiftapps.com/ninja-graphs";
  
  var xhr = new XMLHttpRequest();
  xhr.open("GET", ctx+"/api/proxy/leaderboard_10", true);
  xhr.send();
  xhr.onloadend = function () {
    var json=JSON.parse(xhr.responseText);
    
    var tableRef = document.getElementById('race');
    
    for(var i=0;i<topX;i++){
      var newRow   = tableRef.insertRow(tableRef.rows.length);
      // add numeric
      var c1  = newRow.insertCell(0);
      var c1t  = document.createTextNode(i+1);
      c1.className="col col-1";
      c1.appendChild(c1t);
      
      // add Image
      var c2  = newRow.insertCell(1);
      var c2t  = document.createElement('img');
      c2t.src="https://mojo.redhat.com/people/"+json['custom1'][i]+"/avatar/200.png?a=925089";
      c2t.style="width: 50px; height: 50px"
    	c2t.className="avatar";
      c2.className="col col-2";
      c2.appendChild(c2t);
      
      // add Name
      var c3  = newRow.insertCell(2);
      var c3t  = document.createTextNode(json['labels'][i]);
      c3.className="col col-3";
      c3.appendChild(c3t);
      
      // add Pts
      var c4  = newRow.insertCell(3);
      var c4t  = document.createTextNode(json['datasets'][0]['data'][i]+"pts");
      c4.className="col col-4";
      c4.appendChild(c4t);
    }
    
  }

</script>
</html>