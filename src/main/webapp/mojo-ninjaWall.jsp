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
  
  .belt-blue { color: #a4dbea; }
  .belt-red  { color: #a21c20; }
  .belt-grey { color: #999999; }
  .belt-black{ color: #000000; }
  
</style>
<script>
	Utils = {
		getParameterByName: function(name, url) {
			if (!url) url = window.location.href;
			name = name.replace(/[\[\]]/g, "\\$&");
			var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
			    results = regex.exec(url);
			if (!results) return undefined;
			if (!results[2]) return '';
			return decodeURIComponent(results[2].replace(/\+/g, " "));
		},
		
		findAncestor: function findAncestor (el, cls) {
			while ((el = el.parentElement) && !el.classList.contains(cls));
			return el;
		}
	}
	String.prototype.format = function() {
	  a = this;
	  for (k in arguments) {
	    a = a.replace("{" + k + "}", arguments[k])
	  }
	  return a
	}
	
	var color="";
	
	function toColor(color){
		if ("BLUE"==color) return "#a4dbea";
		if ("GREY"==color) return "#999999";
		if ("RED"==color) return "#c10000";
		if ("BLACK"==color) return "#000000";
		if ("ZERO"==color) return "#ffffff";
	}
	
	var badgeTemplate2=`<svg xmlns="http://www.w3.org/2000/svg" width="34" height="34" viewBox="0 0 24 24"><title>Earned {2}pts in {3}</title>
		<path fill="{0}" d="M12 .587l3.668 7.568 8.332 1.151-6.064 5.828 1.48 8.279-7.416-3.967-7.417 3.967 1.481-8.279-6.064-5.828 8.332-1.151z"/>
		<text font-weight="bold" stroke="#000" xml:space="preserve" text-anchor="start" font-family="Helvetica, Arial, sans-serif" font-size="5.5" y="15" x="5.5" stroke-width="0" fill="#ffffff">{1}</text>
	</svg>`;
	
  var DEFAULT_CTX="https://ninja-graphs-ninja-graphs.6923.rh-us-east-1.openshiftapps.com/ninja-graphs";
  var ctx=(Utils.getParameterByName("source")!=undefined?Utils.getParameterByName("source"):DEFAULT_CTX);
  //var ctx = "https://ninja-graphs-ninja-graphs.6923.rh-us-east-1.openshiftapps.com/ninja-graphs";
  //var ctx = "http://localhost:8082/community-ninja-board";
  var xhr = new XMLHttpRequest();
  xhr.open("GET", ctx+"/api"+(Utils.getParameterByName("source")!=undefined?"":"/proxy")+"/ninjas", true);
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
     
     if (json['custom1'][i].split("|").length!=3){
   	  console.log("ERROR: Input format is incorrect");
   	  break;
     }
     
     var name=json['labels'][i];
     var points=json['datasets'][0]['data'][i]+"pts";
     var username=json['custom1'][i].split("|")[0];
     var belt=json['custom1'][i].split("|")[1];
     var geo=json['custom1'][i].split("|")[2];
     belt=(belt=="zero"?"No":"<span class='belt-"+belt+"'>"+uCase(belt)) +" Belt</span>";
     var NL="<br/>";
     
     
     var badges="";
     if (""!=json['custom2'][i]){
	     var priorYears=json['custom2'][i].split(",");
	     for(j in priorYears){
	       var split=priorYears[j].split("|");
	       var year=split[0];
	       var beltColor=split[1];
	       var pts=split[2];
	       //if ("ZERO"==beltColor) continue;
	       //console.log(name+" -> Year="+year+", belt="+beltColor+", "+pts+"pts");
	       
	       badges+=badgeTemplate2.format(toColor(beltColor), year, pts, year);
	     }
     }else{
    	 badges+="<div style='width:34px;height:34px;'></div>";
     }
     
     td.innerHTML="<img src='https://mojo.redhat.com/people/"+username+"/avatar/200.png?a=925089' class='avatar' /><div>"+NL+name+NL+geo+NL+belt+NL+points+NL+badges+"</div>";
     
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
