<%@page import="
java.util.Date,
java.util.Calendar
"%>

<%@include file="header.jsp"%>

<script>

function save(){
	var newDb=document.getElementById("database").value;
	var payload=JSON.parse(newDb);
	var payload2=JSON.stringify(payload);
	post("/database/save", payload);
}

function post(uri, data){
  var xhr = new XMLHttpRequest();
  var ctx = "${pageContext.request.contextPath}";
  var url=ctx+"/api"+uri;
  xhr.open("POST", url, true);
  if (data != undefined){
    xhr.send(JSON.stringify(data));
  }else{
    xhr.send();
  }
  xhr.onloadend = function () {
  	showSuccess();
  	load();
  };
}
$(document).ready(function() {
	load();
});

function load(){
  var xhr = new XMLHttpRequest();
  var ctx = "${pageContext.request.contextPath}";
  xhr.open("GET", ctx+"/api/database/get", true);
  xhr.send();
  xhr.onloadend = function () {
    var json=JSON.parse(xhr.responseText);
    var obj=JSON.stringify(json, null, "\t");
    document.getElementById("database").value=obj;
  }
}

$(document).ready (function(){
  $("#success-alert").hide();
});

function showSuccess(){
  $("#success-alert").fadeTo(2000, 500).slideUp(500, function(){
		$("#success-alert").slideUp(500);
	}); 
};
</script>
	
	<style>
		textarea {
		  width: 100%;
		  height: 85%;
		}
	</style>
		
  <%@include file="nav.jsp"%>
  
	<div class="alert alert-success" id="success-alert">
    <button type="button" class="close" data-dismiss="alert">x</button>
    <strong>Success!</strong> database saved.
	</div>
	
  <div id="solutions">
  	<textarea id="database" name="database"></textarea>
  	<button name="save" onclick="save();">Save</button>
  </div>

</div>