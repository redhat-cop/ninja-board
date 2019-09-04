<script src="js/utils.js"></script>
<center>
<table id="wall" cellspacing="0" cellpadding="0">
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
    /*
    width:210px;
    */
  }
  table tr td{
    /*          T R B L*/
    padding: 10px 30px 10px 30px;
    border-bottom: solid 1px #ddd;
  }
  table tr{
  }
  .header{
    background-color: #007a87;
    color: white;
    padding-top: 15px;
    text-align: center;
    font-family: Overpass, San-Serif;
    font-size: 22pt;
    font-weight: bold;
    padding: 10px;
  }
  .header td{
    font-weight: bold; 
  }
  .avatar{
    border: 0px solid black;
    width:  140px;
    height: 140px;
    border-radius:         70px;
    -webkit-border-radius: 70px;
    -moz-border-radius:    70px;
  }
  .col{
    text-align: center;
  }
  
  .belt-blue { color: blue;  }
  .belt-red  { color: red;   }
  .belt-grey { color: grey;  }
  .belt-black{ color: black; }
  
</style>
<script>
  
  var ctx=(Utils.getParameterByName("source")!=undefined?Utils.getParameterByName("source"):"https://ninja-graphs-ninja-graphs.6923.rh-us-east-1.openshiftapps.com/ninja-graphs");
  //var ctx = "https://ninja-graphs-ninja-graphs.6923.rh-us-east-1.openshiftapps.com/ninja-graphs";
  //var ctx = "http://localhost:8082/community-ninja-board";
	
  var xhr = new XMLHttpRequest();
  xhr.open("GET", ctx+"/api"+(ctx.indexOf('localhost')>0?"":"/proxy")+"/ninjas", true);
  xhr.send();
  xhr.onloadend = function () {
    var json=JSON.parse(xhr.responseText);
    
    var tableRef = document.getElementById('wall');
    
    var cols=4; // how many cols to display
    
    // Header
    var hdr_tr = tableRef.insertRow(tableRef.rows.length);
    var hdr_td  = hdr_tr.insertCell(0);
    var hdr_n  = document.createTextNode("Ninja Wall");
    hdr_td.className="header";
    hdr_td.colSpan=cols;
    hdr_td.appendChild(hdr_n);
    hdr_tr.appendChild(hdr_td);
    
    for(var i=0;i<json['datasets'][0]['data'].length;i++){
    	
    	var newRow;
    	if (0==(i%cols))
        newRow = tableRef.insertRow(tableRef.rows.length);
    	
      var td = newRow.insertCell(i%cols);
      td.className="col";
      
      var name=json['labels'][i];
      var points=json['datasets'][0]['data'][i]+"pts";
      var username=json['custom1'][i].split("|")[0];
      var belt=json['custom1'][i].split("|")[1];
      var geo=json['custom1'][i].split("|")[2];
      belt=(belt=="zero"?"No":"<span class='belt-"+belt+"'>"+uCase(belt)) +" Belt</span>";
      var NL="<br/>";
      td.innerHTML="<img src='https://mojo.redhat.com/people/"+username+"/avatar/200.png?a=925089' class='avatar' /><div>"+NL+name+NL+geo+NL+belt+NL+points+"</div>";
      
    }
    setTimeout(function(){ resizeParent(); }, 500);
  }
  
  function uCase(string){
	  return string.charAt(0).toUpperCase() + string.slice(1);
  }
  
  function resizeParent() {
	  if (null!=window.frameElement)
	    window.frameElement.style.height=window.frameElement.contentWindow.document.body.scrollHeight+'px';
	}
</script>
