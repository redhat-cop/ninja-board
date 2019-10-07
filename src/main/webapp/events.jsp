<%@page import="
java.util.Date,
java.util.Calendar
"%>

<%@include file="header.jsp"%>
<%@include file="datatables-dependencies.jsp"%>

<script>
function escapeRegExp(str) {
    return str.replace(/([.*+?^=!:\${}()|\[\]\/\\])/g, "\\$1");
}
function loadDataTable(){
  var userFilter=Utils.getParameterByName("id");
  var textFilter=Utils.getParameterByName("filter")!=undefined?Utils.getParameterByName("filter"):"";
  
  $('#example').DataTable( {
        "ajax": {
            "url": '${pageContext.request.contextPath}/api/events/'+(undefined!=userFilter?"?user="+userFilter:""),
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
              if (/.*\[.+\].*/.test(row['text'])){ // look for a set of square brackets
                var before=/.*\[(.+)\].*/.exec(row['text']);
                
                var split=before[1].split("|");
                var title=split[0];
                var link=split[1];
                link="<a href='"+link+"'>"+title+"</a>";
                
                var find1=escapeRegExp(("["+before[1]+"]").replace(/\[/g,'\\[').replace(/\]/g,'\\]').replace(/\|/g,'\\|'));
                var find2=("["+before[1]+"]").replace(/\[/g,'\\[').replace(/\]/g,'\\]').replace(/\|/g,'\\|');
                
                var after=row['text'].replace(new RegExp(find2, 'g'), link);
                
                result=after;
              }
              
              return result;
            }},  
            { "targets": 3, "orderable": true, "render": function (data,type,row){
              return row['user'];
            }}
        ]
    } );
  
    var btnExport=`
      <div style="left:-20px;float:left;" class="dropdown export">
        <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
          Export
        </button>
        <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
          <a class="dropdown-item" href="api/events/export/csv">... as CSV</a><br/>
          <a class="dropdown-item" href="api/events/export/xls">... as XLS</a><br/>
          <a class="dropdown-item" href="api/events/export/json">... as JSON</a>
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

$(document).ready(function() {
  if (null!=Utils.getParameterByName("name")){
    document.getElementById("title-user").innerText=": "+Utils.getParameterByName("name");
  }
  loadDataTable();
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


