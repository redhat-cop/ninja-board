// v1.1 - added SameSite=None & Secure allowin for post August 2020 Chrome rules for setting cookie
Cookie = {
	uuid: function() {
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
			return v.toString(16);
		});
	},
	set: function(cname, cvalue, exdays, secure) {
		var dt=new Date();
		dt.setTime(dt.getTime() + (exdays*24*60*60*1000));
		var expires="expires="+ dt.toUTCString();
		var d=";"; // delimiter
		var ss=document.location.host.includes("local") || document.location.host.includes("dev")?"Lax":"None"; // SameSite: localhost required Lax or the cookie wont write
		//document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/;SameSite=None;"+(secure?"Secure;":"");
		document.cookie = cname + "=" + cvalue + d + expires + d+"path=/"+d+"SameSite="+ss+d+(secure?"Secure"+d:"");
	},
	get: function(cname) {
		var name = cname + "=";
		var ca = document.cookie.split(';');
		for(var i = 0; i < ca.length; i++) {
			var c = ca[i];
			while (c.charAt(0) == ' ') {
				c = c.substring(1);
			}
			if (c.indexOf(name) == 0) {
				return c.substring(name.length, c.length);
			}
		}
		return "";
	},
	remove: function(cname){
		document.cookie = cname + "=;Thu, 01 Jan 1970 00:00:00 UTC;path=/"; // just set to a date in the past and it's removed
	}
}