<center>
<table id="wall" cellspacing="0" cellpadding="0">
<!--tr><td colspan="4" class="header"><center><h2>Ninja Wall</h2></center></td></tr-->
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
  /*
  table tr:not(:first-child) td{
  */
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
  /*
    padding-left:  10px;
    padding-right: 10px;
  */
    text-align: center;
  }
  /*
  .col-3{
  }
  .even td{
    background-color: f9f9f9;
  }
  */
  
  .belt-blue { color: blue;  }
  .belt-red  { color: red;   }
  .belt-grey { color: grey;  }
  .belt-black{ color: black; }
  
</style>

<script>
  
  var ctx = "https://ninja-graphs-ninja-graphs.6923.rh-us-east-1.openshiftapps.com/ninja-graphs";
  //var url="http://localhost:8082/community-ninja-board/api/ninjas";
	  
  var xhr = new XMLHttpRequest();
  xhr.open("GET", "/api/proxy/ninjas", true);
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
      var belt=(json['custom2'][i]=="zero"?"No":"<span class='belt-"+json['custom2'][i]+"'>"+uCase(json['custom2'][i])) +" Belt</span>";
      var NL="<br/>";
      td.innerHTML="<img src='https://mojo.redhat.com/people/"+json['custom1'][i]+"/avatar/200.png?a=925089' class='avatar' /><div>"+NL+name+NL+belt+NL+points+"</div>";
      
      //// add Name
      //var c3  = newRow.insertCell(cell++);
      ////var c3t  = document.createTextNode("");
      //c3.innerHTML=json['labels'][i] +"<br/>"+json['datasets'][0]['data'][i]+"pts";
      ////c3.className="col col-3";
      //c3.className="col";
      ////c3.appendChild(c3t);
      
      // add Pts
      //var c4  = newRow.insertCell(cell++);
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
  
  function uCase(string){
	  return string.charAt(0).toUpperCase() + string.slice(1);
  }
  
  function resizeParent() {
	  if (null!=window.frameElement)
	    window.frameElement.style.height=window.frameElement.contentWindow.document.body.scrollHeight+'px';
	}
</script>
