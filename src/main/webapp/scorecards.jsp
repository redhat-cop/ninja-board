<%@page import="
java.util.Date,
java.util.Calendar
"%>

<%@include file="header.jsp"%>

<script>

var idFieldName="userId";

function edit2(id){
  document.getElementById("edit-ok").innerHTML="Update";
  var xhr = new XMLHttpRequest();
  var ctx = "${pageContext.request.contextPath}";
  xhr.open("GET", ctx+"/api/scorecard/"+id, true);
  xhr.send();
  xhr.onloadend = function () {
    var json=JSON.parse(xhr.responseText);
    var form=document.getElementById("myform");
    
    $("#editFieldsDiv").empty();
    for (var propertyName in json) {
    	if (json.hasOwnProperty(propertyName)) {
    	  if (propertyName === idFieldName || propertyName === "displayName") continue;
	    	var fieldName=propertyName;
	    	var fieldValue=json[propertyName];
	    	var displayName=propertyName;
	      $("#editFieldsDiv").append('<div class="form-group"><label for="'+fieldName+'" class="control-label">'+displayName+':</label><input id="'+fieldName+'" name="'+fieldName+'" type="text" value="'+fieldValue+'" class="form-control"></div>');
      }    
    }
    
    for (var i = 0, ii = form.length; i < ii; ++i) {
      if (typeof json[form[i].name] == "undefined"){
        form[i].value="";
      }else{
        form[i].value=json[form[i].name];
      }
    }
  }
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
    document.getElementById(idFieldName).value="NEW";
}

function update(){
  var data = {};
  var op="";
  var form=document.getElementById("myform");
  for (var i = 0, ii = form.length; i < ii; ++i) {
    var input = form[i];
    if (input.name==idFieldName) op=input.value;
    
    if (input.name) {
      data[input.name] = input.value;
    }
  }
  if (op=="") alert("ERROR: OP is empty!");
  post("/scorecard/"+op, data);
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
    //$('#example').dataTable().fnReloadAjax();
    
    $('#example').DataTable().destroy();
		loadDataTable();
    
  };
}

function loadDataTable(){
	$('#example').DataTable( {
				bSort: false,
        "ajax": {
            "url": '${pageContext.request.contextPath}/api/scorecards/',
            "success": function(json){
            		//console.log("json="+JSON.stringify(json));
	            	var tableHeaders;
	            	var tableColumns=[];
	            	$.each(json.columns, function(i, val){
	              	tableHeaders += "<th>" + val.title + "</th>";
	              	if (val.data=="level"){
	              		tableColumns.push({data: val.data, render: function(data,type,row){return "<span style='width:25px;height:25px;background-color:"+row['level'].toLowerCase()+"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;"+row['level'].toLowerCase();}});
	              	}else{
	              		tableColumns.push({data: val.data});
	              	}
	              });
	              $("#tableDiv").empty();
                $("#tableDiv").append('<table id="example" class="display" cellspacing="0" width="100%"><thead><tr>' + tableHeaders + '</tr></thead></table>');
                
                $('#example').DataTable({
                		"data": json.data,
                		"columns": tableColumns,
						        "scrollCollapse": true,
						        "paging":         true,
						        "lengthMenu": [[10, 25, 50, 100, 200, -1], [10, 25, 50, 100, 200, "All"]], // page entry options
						        "pageLength" : 25, // default page entries
						        "searching" : true,
						        "order" : [[5,"desc"]],
              	});
                
            },
            "dataType": "json"
        },
        "scrollCollapse": true,
        "paging":         false,
        "lengthMenu": [[10, 25, 50, 100, 200, -1], [10, 25, 50, 100, 200, "All"]], // page entry options
        "pageLength" : 25, // default page entries
        "columnDefs": [
        	{"targets": 1, "render": function (data,type,row){
        		return "XXXXXXXX";
        	}}
        ]
    } );
}

$(document).ready(function() {
    //alert(oTable);
    //oSettings=oTable.fnSettings();
    
    loadDataTable();
    
//    $('#example').DataTable( {
//        "ajax": {
//            "url": '${pageContext.request.contextPath}/api/scorecards/',
//            "success": function(json){
//	            	var tableHeaders;
//	            	$.each(json.columns, function(i, val){
//	              	tableHeaders += "<th>" + val + "</th>";
//	              });
//	              $("#tableDiv").empty();
//                $("#tableDiv").append('<table id="example" class="display" cellspacing="0" width="100%"><thead><tr>' + tableHeaders + '</tr></thead></table>');
//                $('#example').dataTable(json);
//            },
//            "dataType": "json"
//        },
//        
////        "scrollY":        "540px",
//        "scrollCollapse": true,
//        "paging":         false,
//        
//        "lengthMenu": [[10, 25, 50, 100, 200, -1], [10, 25, 50, 100, 200, "All"]], // page entry options
//        "pageLength" : 10, // default page entries
////        "columns": [
////            { "data": "id" },
////            { "data": "name" },
////            { "data": "trello" },
////            { "data": "githubPullRequests" },
////            { "data": "githubReviewedPullRequests" },
////            { "data": "githubClosedIssues" },
////            { "data": "total"},
////            { "data": "id" }
////        ]
//        
////        ,"columnDefs": [
////            { "targets": 0, "orderData": [1,2,3] }
////           ,{ "targets": 1, "orderData": [1,2,3] }
////           ,{ "targets": 2, "orderData": [1,2,3] }
////           ,{ "targets": 3, "orderData": [1,2,3] }
////           ,{ "targets": 7, "render": function (data,type,row){
////							return "<div class='btn btn-image' title='Edit' onclick='edit2(\""+row["id"]+"\");' data-toggle='modal' data-target='#exampleModal' style='background-image: url(images/edit-icon-grey-30-active.png)'></div>";
////						}}
////        ]
//    } );
} );


//////////////////// PLUGIN TO PERFORM AJAX REFRESH /////////////////////
jQuery.fn.dataTableExt.oApi.fnReloadAjax = function ( oSettings, sNewSource, fnCallback, bStandingRedraw )
{
    // DataTables 1.10 compatibility - if 1.10 then `versionCheck` exists.
    // 1.10's API has ajax reloading built in, so we use those abilities
    // directly.
    if ( jQuery.fn.dataTable.versionCheck ) {
        var api = new jQuery.fn.dataTable.Api( oSettings );
 
        if ( sNewSource ) {
            api.ajax.url( sNewSource ).load( fnCallback, !bStandingRedraw );
        }
        else {
            api.ajax.reload( fnCallback, !bStandingRedraw );
        }
        return;
    }
 
    if ( sNewSource !== undefined && sNewSource !== null ) {
        oSettings.sAjaxSource = sNewSource;
    }
 
    // Server-side processing should just call fnDraw
    if ( oSettings.oFeatures.bServerSide ) {
        this.fnDraw();
        return;
    }
 
    this.oApi._fnProcessingDisplay( oSettings, true );
    var that = this;
    var iStart = oSettings._iDisplayStart;
    var aData = [];
 
    this.oApi._fnServerParams( oSettings, aData );
 
    oSettings.fnServerData.call( oSettings.oInstance, oSettings.sAjaxSource, aData, function(json) {
        /* Clear the old information from the table */
        that.oApi._fnClearTable( oSettings );
 
        /* Got the data - add it to the table */
        var aData =  (oSettings.sAjaxDataProp !== "") ?
            that.oApi._fnGetObjectDataFn( oSettings.sAjaxDataProp )( json ) : json;
 
        for ( var i=0 ; i<aData.length ; i++ )
        {
            that.oApi._fnAddData( oSettings, aData[i] );
        }
 
        oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();
 
        that.fnDraw();
 
        if ( bStandingRedraw === true )
        {
            oSettings._iDisplayStart = iStart;
            that.oApi._fnCalculateEnd( oSettings );
            that.fnDraw( false );
        }
 
        that.oApi._fnProcessingDisplay( oSettings, false );
 
        /* Callback user function - for event handlers etc */
        if ( typeof fnCallback == 'function' && fnCallback !== null )
        {
            fnCallback( oSettings );
        }
    }, oSettings );
};
//////////////////// END OF PLUGINS ///////////////////////


</script>
	
	<style>
		.link{
			cursor: pointer;
			font-weight: bold;
			color: grey;
		}
		
		.link:hover{
		  font-weight: bold;
		  color: #333333;
		}
	</style>
	
    <%@include file="nav.jsp"%>
    

    <div id="solutions">
		    <div id="solutions-buttonbar">
		        <button style="position:relative;height:30px;width:75px;left:0px;top:0px;"   class="btn btn-primary" name="New"    onclick="reset();" type="button" data-toggle="modal" data-target="#exampleModal" data-whatever="@new" disabled>New</button>
		        <button style="position:relative;height:30px;width:75px;left:0px;top:0px;"   class="btn btn-primary" name="Export" onclick="window.location.href='<%=request.getContextPath()%>/api/analytics/export/xls';" disabled>Export</button>
		    </div>
		    <div id="tableDiv">
			    <table id="example" class="display" cellspacing="0" width="100%">
			        <thead>
			            <tr>
			                <th align="left">User ID</th>
			                <th align="left">Name</th>
			                <th align="left">A dynamic bunch of points fields go here</th>
			                <th align="left">Total Points</th>
			                <th align="left"></th>
			            </tr>
			        </thead>
			    </table>
			  </div>
    </div>

<div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
  <div class="modal-dialog" role="document"> <!-- make wider by adding " modal-lg" to class -->
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="exampleModalLabel">User Scorecard</h4>
      </div>
      <div class="modal-body">
        <form id="myform">
          <div id="form-id" class="form-group">
            <label for="userId" class="control-label">User ID:</label>
            <input id="userId" disabled name="userId" type="text" class="form-control"/>
          </div>
          <div class="form-group">
            <label for="displayName" class="control-label">Display Name:</label>
            <input id="displayName" name="displayName" type="text" class="form-control">
          </div>
          
          <div id="editFieldsDiv">
          </div>
          
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        <button id="edit-ok" type="button" class="btn btn-primary" data-dismiss="modal" onclick="update(); return false;">Create</button>
      </div>
    </div>
  </div>
</div>