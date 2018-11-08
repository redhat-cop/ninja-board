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
	$('#example').DataTable( {
        "ajax": {
            "url": '${pageContext.request.contextPath}/api/events/',
            "dataSrc": ""
        },
        "scrollY":        "1300px",
        "scrollCollapse": true,
        "paging":         false,
        "lengthMenu": [[10, 25, 50, 100, 200, -1], [10, 25, 50, 100, 200, "All"]], // page entry options
        "pageLength" : 5, // default page entries
        "order" : [[0,"desc"]],
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
            		var cardId=split[1];
            		var link="<a href='https://trello.com/c/"+cardId+"'>"+title+"</a>";
            		
            		var find=escapeRegExp(("["+before[1]+"]").replace(/\[/g,'\\[').replace(/\]/g,'\\]').replace(/\|/g,'\\|'));
            		
            		var after=row['text'].replace(new RegExp(find, 'g'), link);
            		
            		result=after;
            	}
            	
              return result;
            }},  
            { "targets": 3, "orderable": true, "render": function (data,type,row){
              return row['user'];
            }}
        ]
    } );
    
}

$(document).ready(function() {
    loadDataTable();
} );


</script>
	
    <%@include file="nav.jsp"%>
    
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

