Http = {
	send: function(action, uri, data, callback){
	  var xhr = new XMLHttpRequest();
	  var ctx = "${pageContext.request.contextPath}";
	  var url=uri;
	  
	  xhr.open(action, url, true);
	  if (data != undefined){
	    xhr.setRequestHeader("Content-type", "application/json");
	    xhr.send(JSON.stringify(data));
	  }else{
	    xhr.send();
	  }
	  xhr.onloadend = function () {
		console.log("datatables-functions::send:: onloadend ... status = "+this.status);
		
	  	if (this.status == 200){
		  	console.log("datatables-functions::send:: returned 200");
	  	}else if(xhr.status>=400){
	  		
	  	}
	  	
	  	if (undefined!=callback){
	  		callback(xhr, this.status);
	  	}
	  };
	},
	httpPost: function(uri, data){
		return Http.send("POST", uri, data);
	},
	httpPost: function(uri, data, callback){
		return Http.send("POST", uri, data, callback);
	},
	httpDelete: function(uri, data){
		return Http.send("DELETE", uri, data);
	},
	httpDelete: function(uri, data, callback){
		return Http.send("DELETE", uri, data, callback);
	},
	httpGet: function(url, callback){
		var xhr = new XMLHttpRequest();
		xhr.open("GET", url, true);
		xhr.send();
		xhr.onloadend = function () {
			callback(xhr.responseText, this.status);
			//callback(JSON.parse(xhr.responseText));
		};
	},
	httpGetObject: function httpGetObject(url, callback, onError){
		var xhr = new XMLHttpRequest();
		xhr.open("GET", url, true);
		//xhr.setRequestHeader("Accept", "application/json");
		xhr.send();
		xhr.onloadend = function () {
			if (this.status==200){
				callback(JSON.parse(xhr.responseText));
			}else{
				onError(this.status, xhr.responseText);
			}
			if (this.status==403){
				console.log("Error: xhr call returned 403, redirecting to '.'. Request url was "+url);
				//self.location.href=".";
			}
		};
	}
}

