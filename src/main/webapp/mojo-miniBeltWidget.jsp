<center>
<style>
table{
  width:210px; /* width of the mojo widget in Overview mode, remove for Tiles mode*/
}
.text{
  font-family: Overpass, Sans-serif;
  text-align: center;
}
.text a{
  color: #333333;
  text-decoration: none;
}

a:hover, a:active{
  text-decoration: underline;
}
</style>
<table id="mini-dashboard">
<tr><td class="text"><span id="_name"></span>, <span id="_wrapText"></span></td></tr>
<tr><td class="text"><!--a class="jivecontainerTT-hover-container jive-link-community-small" href="/community/communities-at-red-hat/communities-of-practice-operations/communities-of-practice-ninja-program"--><img class="ninjaIcon" id="_level"><!--/a--></td></tr>
<tr><td class="text"><!--a class="jivecontainerTT-hover-container jive-link-community-small" href="/community/communities-at-red-hat/communities-of-practice-operations/communities-of-practice-ninja-program">My Dashboard</a--></td></tr>
</table>
</center>

<script>
  window.frameElement.style.height="247px"; // default window height
  
  var ctx = "https://ninja-graphs-ninja-graphs.6923.rh-us-east-1.openshiftapps.com/ninja-graphs";
  
  setTimeout(function(){ displ(); }, 200);
  
  function displ(){
	  username=getUsername();
	  if (username==undefined){
		  setTimeout(function(){ displ(); }, 200);
		  return;
	  }
	  
	  var xhr = new XMLHttpRequest();
	  xhr.open("GET", ctx+"/api/proxy/summary_"+username, true);
	  xhr.send();
	  xhr.onloadend = function () {
	    var json=JSON.parse(xhr.responseText);
	    document.getElementById("_name").innerHTML=json.displayName;
	    if (json.level.toUpperCase() == "ZERO"){
	      document.getElementById("_wrapText").innerHTML="you have<br/>No Belt";
	    }else{
	      document.getElementById("_wrapText").innerHTML="you're a";
	    }
	    
	    document.getElementById("_level").src=ctx+"/images/"+json.level.toLowerCase()+"_belt_icon.png";
	    
	    setTimeout(function(){ resizeParent(); }, 500);
	  }
  }
  
  function getUsername(){
	  var username;
	  if (undefined!=window.parent._jive_current_user)
		  username=window.parent._jive_current_user.username;
	  if (undefined!=window._jive_current_user)
		  username=window._jive_current_user.username;
	  return username;
  }
  
  function resizeParent() {
	  //var e=window.parent.document.getElementsByClassName("htmlWidgetIframe");
	  //for (var i=0;i<e.length;i++){
    //  e[i].style.height = e[i].contentWindow.document.body.scrollHeight + 'px';
	  //}
	  
	  //var THIS=this;
	  //var par=THIS.parent;
	  //var gpar=THIS.parent.parent;
	  //var fe=window.frameElement;
	  
	  window.frameElement.style.height=window.frameElement.contentWindow.document.body.scrollHeight+'px';
	}
  
</script>
