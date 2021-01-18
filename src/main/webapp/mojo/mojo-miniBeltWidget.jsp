<center>
<style>
table{
  width:174px; /* width of the mojo widget in Overview mode, remove for Tiles mode*/
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
<tr><td class="text"><span id="_name"></span> <span id="_wrapText"></span></td></tr>
<tr><td class="text"><!--a class="jivecontainerTT-hover-container jive-link-community-small" href="/community/communities-at-red-hat/communities-of-practice-operations/communities-of-practice-ninja-program"--><div class="ninjaIcon" id="_level"><!--/a--></td></tr>
<tr><td class="text"><!--a class="jivecontainerTT-hover-container jive-link-community-small" href="/community/communities-at-red-hat/communities-of-practice-operations/communities-of-practice-ninja-program">My Dashboard</a--></td></tr>
</table>
</center>

<script>
  
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
	      document.getElementById("_wrapText").innerHTML="<br/>No Star yet";
	    }else{
	      //document.getElementById("_wrapText").innerHTML="you're a";
	      document.getElementById("_level").innerHTML=badgeTemplate2.format(toColor(json.level.toUpperCase()));
	    }
	    
	    //document.getElementById("_level").src=ctx+"/images/"+json.level.toLowerCase()+"_belt_icon.png";
	    
	    setTimeout(function(){ resizeParent(); }, 500);
	  }
  }
  Utils = {
			getParameterByName: function(name, url) {
				if (!url) url = window.location.href;
				name = name.replace(/[\[\]]/g, "\\$&");
				var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
				    results = regex.exec(url);
				if (!results) return undefined;
				if (!results[2]) return '';
				return decodeURIComponent(results[2].replace(/\+/g, " "));
			}
	}
	String.prototype.format = function(){
		a = this;
		for (k in arguments)
			a = a.replace("{" + k + "}", arguments[k])
		return a
	}


	function getUsername(){
		return Utils.getParameterByName("username");
	}
  
	function toColor(color){
		if ("BLUE" == color)  return "#316EC2";//"#a4dbea";
		if ("GREY" == color)  return "#808080";//"#999999";
		if ("RED" == color)   return "#41A85F";//"#c10000";
		if ("BLACK" == color) return "#FAC51C";//"#000000";
		if ("ZERO" == color)  return "#ffffff";//"#ffffff";
	}
	var badgeTemplate2 = `<svg xmlns="http://www.w3.org/2000/svg" width="34" height="34" viewBox="0 0 24 24"><title><\/title>
        <path fill="{0}" d="M12 .587l3.668 7.568 8.332 1.151-6.064 5.828 1.48 8.279-7.416-3.967-7.417 3.967 1.481-8.279-6.064-5.828 8.332-1.151z"/>
        <text font-weight="bold" stroke="#000" xml:space="preserve" text-anchor="start" font-family="Helvetica, Arial, sans-serif" font-size="5.5" y="15" x="5.5" stroke-width="0" fill="#ffffff"><\/text>
				<\/svg>`;

  
  
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
