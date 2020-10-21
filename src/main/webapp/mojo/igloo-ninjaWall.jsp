<div>
	<style type="text/css">
		body {
			font-family: Overpass, Sans-serif; color: #333;
		}
		
		table {
			border: solid 1px #ddd; cellspacing: 0px; cellpadding: 0px;
		}
		
		table tr td {
			padding: 10px 30px 10px 30px; border-bottom: solid 1px #ddd;
		}
		
		table tr {}
		
		.header {
			background-color: #000000; color: white; padding-top: 15px; text-align: center; font-family: Overpass, San-Serif; font-size: 22pt; font-weight: bold; padding: 10px;
		}
		
		.header td {
			font-weight: bold;
		}
		
		.avatar {
			border: 0px solid black; width: 140px; height: 140px; border-radius: 70px; -webkit-border-radius: 70px; -moz-border-radius: 70px;
		}
		
		.col {
			text-align: center;
		}
		
		.belt-blue {
			color: #a4dbea;
		}
		
		.belt-red {
			color: #a21c20;
		}
		
		.belt-grey {
			color: #999999;
		}
		
		.belt-black {
			color: #000000;
		}

	</style>

	<table id="wall" cellspacing="0" cellpadding="0"></table>
	<script type="text/javascript">
		/*<![CDATA[*/
		NUtils = {
			getParameterByName: function(name, url)
			{
				if (!url) url = window.location.href;
				name = name.replace(/[\[\]]/g, "\\$&");
				var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
					results = regex.exec(url);
				if (!results) return undefined;
				if (!results[2]) return '';
				return decodeURIComponent(results[2].replace(/\+/g, " "));
			},

			findAncestor: function findAncestor(el, cls)
			{
				while ((el = el.parentElement) && !el.classList.contains(cls));
					return el;
			}
		}
		String.prototype.format = function()
		{
			a = this;
			for (k in arguments)
			{
				a = a.replace("{" + k + "}", arguments[k])
			}
			return a
		}

		var color = "";

		function toColor(color)
		{
			if ("BLUE" == color) return "#a4dbea";
			if ("GREY" == color) return "#999999";
			if ("RED" == color) return "#c10000";
			if ("BLACK" == color) return "#000000";
			if ("ZERO" == color) return "#ffffff";
		}

		var badgeTemplate2 = `<svg xmlns="http://www.w3.org/2000/svg" width="34" height="34" viewBox="0 0 24 24"><title>Earned {2}pts in {3}<\/title>
                <path fill="{0}" d="M12 .587l3.668 7.568 8.332 1.151-6.064 5.828 1.48 8.279-7.416-3.967-7.417 3.967 1.481-8.279-6.064-5.828 8.332-1.151z"/>
                <text font-weight="bold" stroke="#000" xml:space="preserve" text-anchor="start" font-family="Helvetica, Arial, sans-serif" font-size="5.5" y="15" x="5.5" stroke-width="0" fill="#ffffff">{1}<\/text>
        <\/svg>`;

		var DEFAULT_CTX = "https://ninja-graphs-ninja-graphs.6923.rh-us-east-1.openshiftapps.com/ninja-graphs";
		var ctx = (NUtils.getParameterByName("source") != undefined ? NUtils.getParameterByName("source") : DEFAULT_CTX);
			//var ctx = "https://ninja-graphs-ninja-graphs.6923.rh-us-east-1.openshiftapps.com/ninja-graphs";
			//var ctx = "http://localhost:8082/community-ninja-board";
		var xhr = new XMLHttpRequest();
		var users = [];
		xhr.open("GET", ctx + "/api" + (NUtils.getParameterByName("source") != undefined && NUtils.getParameterByName("source").includes("localhost") ? "" : "/proxy") + "/ninjas", true);
		xhr.send();
		xhr.onloadend = function()
		{
			var json = JSON.parse(xhr.responseText);

			var tableRef = document.getElementById('wall');

			var cols = 4; // how many cols to display

			// Header
			var hdr_tr = tableRef.insertRow(tableRef.rows.length);
			var hdr_td = hdr_tr.insertCell(0);
			var hdr_n = document.createTextNode("NINJA WALL");
			hdr_td.className = "header";
			hdr_td.colSpan = cols;
			hdr_td.appendChild(hdr_n);
			hdr_tr.appendChild(hdr_td);

			for (var i = 0; i < json['datasets'][0]['data'].length; i++)
			{

				var newRow;
				if (0 == (i % cols))
					newRow = tableRef.insertRow(tableRef.rows.length);

				var td = newRow.insertCell(i % cols);
				td.className = "col";

				if (json['custom1'][i].split("|").length != 3)
				{
					console.log("ERROR: Input format is incorrect");
					break;
				}

				var name = json['labels'][i];
				var points = json['datasets'][0]['data'][i] + "pts";
				var username = json['custom1'][i].split("|")[0];
				users.push(username);
				var belt = json['custom1'][i].split("|")[1];
				var geo = json['custom1'][i].split("|")[2];
				belt = (belt == "zero" ? "No" : "<span class='belt-" + belt + "'>" + uCase(belt)) + " Belt<\/span>";
				var NL = "<br/>";

				var badges = "";
				if ("" != json['custom2'][i])
				{
					var priorYears = json['custom2'][i].split(",");
					for (j in priorYears)
					{
						if (typeof priorYears[j] == "string")
						{
							var split = priorYears[j].split("|");
							var year = split[0];
							var beltColor = split[1];
							var pts = split[2];
								//if ("ZERO"==beltColor) continue;
								//console.log(name+" -> Year="+year+", belt="+beltColor+", "+pts+"pts");

							badges += badgeTemplate2.format(toColor(beltColor), year, pts, year);
						}
					}
				}
				else
				{
					badges += "<div style='width:34px;height:34px;'><\/div>";
				}

				td.innerHTML = "<img src='/cmedia/img/none.gif' class='avatar user-" + username + "' /><div>" + NL + name + NL + geo + NL + belt + NL + points + NL + badges + "<\/div>";

			}
			getUserPics(users);
				// setTimeout(function(){ resizeParent(); }, 500);
		}

		function uCase(string)
		{
			return string.charAt(0).toUpperCase() + string.slice(1);
		}

		function getUserPics(users)
		{
			var jqxhr = jQuery.getJSON("/.api2/api/v1/communities/10/search/members?query=" + users.join("+OR+") + "&memberSearchType=Namespace&limit=1000", function(data)
			{
				usersinfo = data.results;
				for (i in usersinfo)
				{
					var email = usersinfo[i].email;
					if (undefined != email)
					{
						var el = jQuery('.user-' + usersinfo[i].email.split('@')[0]);
						if (el.length > 0)
						{
							jQuery('.user-' + usersinfo[i].email.split('@')[0])[0].src = "/download-profile/{" + usersinfo[i].id + "}/profile/crxlarge?noCache=" + Math.round(new Date().getTime() / 1000)

						}
					}
				}
			})
		}
		/*]]]]]]]]]]]>*/

	</script>
</div>
