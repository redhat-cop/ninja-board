Http = {
  send: function(action, uri, data, callback){
    var xhr = new XMLHttpRequest();
    var ctx = "${pageContext.request.contextPath}";
    var url=uri;
    
    xhr.open(action, url, true);
    xhr.timeout=20000; //20 second timeout
    if (data!=undefined){
      xhr.setRequestHeader("Content-type", "application/json");
      xhr.send((typeof data==='string')?data:JSON.stringify(data)); // stringify only if 'data' is not a string already
    }else{
      xhr.send();
    }
    xhr.onprogress = function (p){
      console.log("onprogress:"+p);
    }
    xhr.ontimout = function (p){
      console.log("ontimeout:"+p);
    }
    xhr.onloadend = function () {
      console.log("http::send:: onloadend ... status = "+this.status);
      
      if (this.status == 200){
        console.log("http::send:: returned 200");
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
  httpGet: function(url, callback, onError){
    var xhr = new XMLHttpRequest();
    xhr.open("GET", url, true);
    xhr.send();
    xhr.onloadend = function () {
      if (this.status==200){
        callback(xhr.responseText, this.status);
      }else
        onError(this.status);
      
      //callback(xhr.responseText, this.status);
      //callback(JSON.parse(xhr.responseText));
    };
  },
  httpGetObject: function httpGetObject(url, callback, onError){
    var xhr = new XMLHttpRequest();
    xhr.open("GET", url, true);
    xhr.send();
    xhr.onloadend = function () {
    if (this.status==200){
      callback(this.status, JSON.parse(xhr.responseText));
    }else
      onError(this.status);
    };
  }
}

