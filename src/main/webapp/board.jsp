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
function zeroItem(id){
  post("/analytics/resetCounter/"+id);
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
    $('#example').dataTable().fnReloadAjax();
  };
}
$(document).ready(function() {
    //alert(oTable);
    //oSettings=oTable.fnSettings();
    
    $('#example').DataTable( {
        "ajax": {
            "url": '${pageContext.request.contextPath}/api/points/list',
            "dataSrc": ""
        },
        
//        "scrollY":        "540px",
        "scrollCollapse": true,
        "paging":         false,
        
        "lengthMenu": [[10, 25, 50, 100, 200, -1], [10, 25, 50, 100, 200, "All"]], // page entry options
        "pageLength" : 10, // default page entries
        "columns": [
            { "data": "id" },
            { "data": "name" },
            { "data": "githubPullRequests" },
            { "data": "githubReviewedPullRequests" },
            { "data": "githubClosedIssues" },
            { "data": "total"},
            { "data": "id" }
        ],
        "columnDefs": [
            { "targets": 0, "orderData": [1,2,3] }
           ,{ "targets": 1, "orderData": [1,2,3] }
           ,{ "targets": 2, "orderData": [1,2,3] }
           ,{ "targets": 3, "orderData": [1,2,3] }
           ,{ "targets": 6, "render": function (data,type,row){
							return "<div class='btn btn-image' title='Edit' onclick='edit2(\""+row["id"]+"\");' data-toggle='modal' data-target='#exampleModal' style='background-image: url(images/edit-icon-grey-30-active.png)'></div>";
						}}
        ]
    } );
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

    <%@include file="nav.jsp"%>
    

    <div id="solutions">
		    <div id="solutions-buttonbar">
		        <button style="position:relative;height:30px;width:75px;left:0px;top:0px;"   class="btn btn-primary" name="New"    onclick="reset();" type="button" data-toggle="modal" data-target="#exampleModal" data-whatever="@new" disabled>New</button>
		        <button style="position:relative;height:30px;width:75px;left:0px;top:0px;"   class="btn btn-primary" name="Export" onclick="window.location.href='<%=request.getContextPath()%>/api/analytics/export/xls';" disabled>Export</button>
		    </div>
		    <table id="example" class="display" cellspacing="0" width="100%">
		        <thead>
		            <tr>
		                <th align="left">User ID</th>
		                <th align="left">Name</th>
		                <th align="left">Github Pull Request Points</th>
		                <th align="left">Github Reviewed Pull Request Points</th>
		                <th align="left">Github Closed Issue Points</th>
		                <th align="left">Total Points</th>
		                <th align="left"></th>
		            </tr>
		        </thead>
		    </table>
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
            <label for="id" class="control-label">User ID:</label>
            <input id="id" disabled name="id" type="text" class="form-control"/>
          </div>
          <div class="form-group">
            <label for="name" class="control-label">Display Name:</label>
            <input id="name" name="name" type="text" class="form-control">
          </div>
          <div class="form-group">
            <label for="githubPullRequests" class="control-label">Github Pull Request Points:</label>
            <input id="githubPullRequests" name="githubPullRequests" type="text" class="form-control">
          </div>
          <div class="form-group">
            <label for="githubReviewedPullRequests" class="control-label">Github Reviewed Pull Request Points:</label>
            <input id="githubReviewedPullRequests" name="githubReviewedPullRequests" type="text" class="form-control">
          </div>
          <div class="form-group">
            <label for="githubClosedIssues" class="control-label">Github Closed Issue Points:</label>
            <input id="githubClosedIssues" name="githubClosedIssues" type="text" class="form-control">
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