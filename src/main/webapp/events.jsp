<%@page import="
java.util.Date,
java.util.Calendar
"%>

<%@include file="header.jsp"%>
<%@include file="datatables-dependencies.jsp"%>
	<!-- markdown library -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/showdown/1.6.4/showdown.min.js"></script>
	
	
<script>
var table=undefined;
function escapeRegExp(str) {
    return str.replace(/([.*+?^=!:\${}()|\[\]\/\\])/g, "\\$1");
}
function getUrlVars(){
	var queryDict = {}
	location.search.substr(1).split("&").forEach(function(item) {
	    queryDict[item.split("=")[0]] = item.split("=")[1]
	});
	return queryDict;
}
var baseUrl=window.location.href.substr(0, window.location.href.indexOf('${pageContext.request.contextPath}'));
var ajaxUrl="${pageContext.request.contextPath}/api/v2/events";

function loadDataTable(){
  var textFilter=Utils.getParameterByName("filter")!=undefined?Utils.getParameterByName("filter"):"";
  
  if (table!=undefined){
	  // push any params to the Nav
	  var params="";
	  $("input[type=text],input[type=number],input[type=hidden]").each(function(){
		  if (Utils.isNotBlank($(this).val())){
			  params+="&"+$(this).attr("id")+"="+$(this).val();
		  }
	  });
	  params=params.substring(1);
	  
	  // reload
	  ajaxUrl="${pageContext.request.contextPath}/api/v2/events?"+params;
	  
	  $('.export-type').each(function(){
		  $(this).attr("href", $(this).attr("base-href")+"?"+params)
	  });
	  
	  window.history.pushState("object or string", "Title", "?"+params);
	  
	  table.clear().draw();
	  table.ajax.url( ajaxUrl ).load();
	  //table.ajax.reload()
	  
	  
  }else{
  	table=$('#example').DataTable( {
	        "ajax": {
	            "url": ajaxUrl,
	            "dataSrc": ""
	        },
	        "scrollY":        "1300px",
	        "scrollCollapse": true,
	        "paging":         false,
	        "lengthMenu": [[10, 25, 50, 100, 200, -1], [10, 25, 50, 100, 200, "All"]], // page entry options
	        "pageLength" : 5, // default page entries
	        "order" : [[0,"desc"]],
	        "oSearch": {"sSearch": textFilter},
	        "columns": [
	            { "data": "timestamp" },
	            { "data": "type" },
	            { "data": "text" },
	            { "data": "user" }
	        ],"columnDefs": [
	            { "targets": 2, "orderable": true, "render": function (data,type,row){
	              
	              var result=row['text'];
	              
	              if (typeof showdown !== 'undefined'){
	            		var markDownConverter = new showdown.Converter();
	        				var str = markDownConverter.makeHtml(result);
	        				//remove root paragraphs <p></p>
	        				if (str!=undefined){
		        				if (str==undefined || str.length<=3) console.log("error: str="+result)
		        				str = str.substring(3);
		        				str = str.substring(0, str.length - 4);
		        				result = str;
	        				}
	            	}
	              
	              return result;
	            }},  
	            { "targets": 3, "orderable": true, "render": function (data,type,row){
	              return row['user'];
	            }}
	        ]
	    } );
  	}
  
    
}

function addExportButton(){
   var btnExport=`
      <div style="left:-20px;float:left;" class="dropdown export">
      	<button class="btn btn-secondary dropdown-toggle" type="button" onclick="copyToClipboard(baseUrl+ajaxUrl);">Copy Url</button>
      	
        <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
          Export
        </button>
        <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
          <a class="dropdown-item export-type" id="export-csv"  base-href="api/v2/events/export/csv">... as CSV</a><br/>
          <a class="dropdown-item export-type" id="export-xls"  base-href="api/v2/events/export/xls">... as XLS</a><br/>
          <a class="dropdown-item export-type" id="export-json" base-href="api/v2/events/export/json">... as JSON</a>
        </div>
      </div>
      
	      
      `;
	    
     // Insert Export button next to Search box
    var searchBoxDiv=document.querySelector("#example_filter");
    var wrapper=searchBoxDiv.parentNode;
    var newNode = document.createElement("span");
    newNode.innerHTML=btnExport+"&nbsp;";
    searchBoxDiv.appendChild(newNode);
}

function copyToClipboard(text) {
  window.prompt("Copy to clipboard: Ctrl+C, Enter", text);
}
	
//function copyUrl(url) {
//	//var text=table.ajax.url();
//	copyToClipboard(text);
//	  var copyTextarea = document.querySelector('.copyUrl');
//	  copyTextarea.value=table.ajax.url();
//	  copyToClipboard(copyTextarea.value);
//	  copyTextarea.focus();
//	  copyTextarea.select();
//	  try {
//	    var successful = document.execCommand('copy');
//	    var msg = successful ? 'successful' : 'unsuccessful';
//	    console.log('Copying text command was ' + msg);
//	  } catch (err) {
//	    console.log('Oops, unable to copy');
//	  }
//}

$(document).ready(function() {
  if (null!=Utils.getParameterByName("name")){
    document.getElementById("title-user").innerText=": "+Utils.getParameterByName("name");
  }
  loadDataTable();
  addExportButton();
});


</script>
	
    <%@include file="nav.jsp"%>

    <div class="navbar-connector"></div>
    <div class="navbar-title">
    	<h2><span class="navbar-title-text">Events<span id="title-user"></span></span></h2>
    </div>
    
    <style>
    table tr td:nth-child(1), table tr td:nth-child(2){
    	white-space: nowrap;
    }
    /*
    table:nth-child(0), table:nth-child(1){
    	white-space: nowrap;
    }
    */
    
    .export button{
    	height: 26px;
	    padding: 0px;
	    padding-left: 11px;
	    padding-right: 12px;
    }
		.export div .dropdown-item{
			padding-left: 10px;
		}
    
    </style>
    
    <style>
    #filtersBar{
	    padding-left: 15px;
			display: grid;
			grid-template-columns: 1fr 8fr;
			grid-gap: 2px 8px;}
			label{margin-right:10px;}
    </style>
    <div id="filtersBar">
    	<div><label for="user">User ID (Kerberos)</label></div>             <div><input type="text" id="user"/></div>
    	<div><label for="manager">Line Manager ID (Kerberos)</label></div>  <div><input type="text" id="manager"/></div>
    	<div><label for="daysOld">Days old</label></div>                    <div><input type="number" id="daysOld"/></div>
    	<div><label for="events">Event types</label></div>                  <div><input type="hidden" id="events"/>

   		<label for="pointsIncrement"><input type="checkbox" class="events" id="pointsIncrement" checked onclick="return group('events');" value="Points Increment"/>   Points Increment            </label>
   		<label for="userPromotion">  <input type="checkbox" class="events" id="userPromotion"   checked onclick="return group('events');" value="User Promotion"/>     User Promotion              </label>
   		<label for="newUser">        <input type="checkbox" class="events" id="newUser"         checked onclick="return group('events');" value="New User"/>                   New User                    </label>
   		<label for="lostPoints">     <input type="checkbox" class="events" id="lostPoints"      checked onclick="return group('events');" value="Lost Points"/>                Lost Points                 </label>
   		<label for="scriptSucceeded"><input type="checkbox" class="events" id="scriptSucceeded" checked onclick="return group('events');" value="Script Execution Succeeded"/> Script Execution Succeeded  </label>
   		<label for="scriptFailed">   <input type="checkbox" class="events" id="scriptFailed"    checked onclick="return group('events');" value="Script Execution FAILED"/>    Script Execution FAILED     </label>

    	</div>
    	
    	<script>
    	group("events");
    	function group(selector){
		    $('#'+selector).val("")
		    var allVals = $('input.'+selector+':checked').map(function() {return this.value;}).get().join();
		    $('#'+selector).val(allVals)
    	}
    	
    	</script>
    	
			<div></div><div><button onclick="return loadDataTable();">Filter</button></div>
    </div>
	
    <div id="solutions">
		    <div id="solutions-buttonbar">
		    </div>
		    <div id="tableDiv">
			    <table id="example" class="display" cellspacing="0" width="100%">
			        <thead>
			            <tr>
			                <th align="left">Timestamp</th>
			                <th align="left">Type</th>
			                <th align="left"></th>
			                <th align="left">User</th>
			            </tr>
			        </thead>
			    </table>
			  </div>
    </div>


