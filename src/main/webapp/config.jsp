<%@page import="
java.util.Date,
java.util.Calendar
"%>

<%@include file="header.jsp"%>

<script>

function edit2(id){
  document.getElementById("edit-ok").innerHTML="Update";
  var xhr = new XMLHttpRequest();
  var ctx = "${pageContext.request.contextPath}";
  xhr.open("GET", ctx+"/api/scorecard/get/"+id, true);
  xhr.send();
  xhr.onloadend = function () {
    var json=JSON.parse(xhr.responseText);
    var form=document.getElementById("myform");
    for (var i = 0, ii = form.length; i < ii; ++i) {
      if (typeof json[form[i].name] == "undefined"){
        form[i].value="";
      }else{
        form[i].value=json[form[i].name];
      }
    }
  }
}
function copyToClipboard(text) {
  window.prompt("Copy to clipboard: Ctrl+C, Enter", text);
}
function deleteItem(id){
  post("/analytics/delete/"+id);
}
function reset(){
    document.getElementById("edit-ok").innerHTML="Create";
    
    var form=document.getElementById("myform");
    for (var i = 0, ii = form.length; i < ii; ++i) {
      var input = form[i];
      input.value="";
    }
    document.getElementById("id").value="NEW";
}

function update(){
  var data = {};
  var op="";
  var form=document.getElementById("myform");
  for (var i = 0, ii = form.length; i < ii; ++i) {
    var input = form[i];
    if (input.name=="id")op=input.value;
    
    if (input.name=="tags"){
      data[input.name] = input.value.split(",");
    }else if (input.name) {
      data[input.name] = input.value;
    }
  }
  if (op=="") alert("ERROR: OP is empty!");
  post("/analytics/update/"+op, data);
  reset();
  
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
    //$('#example').dataTable().fnReloadAjax();
  };
}
$(document).ready(function() {
	load();
});

function load(){
  var xhr = new XMLHttpRequest();
  var ctx = "${pageContext.request.contextPath}";
  xhr.open("GET", ctx+"/api/config/get", true);
  xhr.send();
  xhr.onloadend = function () {
    var json=JSON.parse(xhr.responseText);
    var cfg=JSON.stringify(json, null, "\t");
    document.getElementById("config").value=cfg;
  }
}

function save(){
	var newConfig=document.getElementById("config").value;
	var payload=JSON.parse(newConfig);
	var payload2=JSON.stringify(payload);
	post("/config/save", payload2);
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
	    <strong>Success!</strong> config saved.
	</div>
	
  <div id="solutions">
  	<textarea id="config" name="config"></textarea>
  	<button id="save" name="save" onclick="save();">Save</button>
  </div>

</div>