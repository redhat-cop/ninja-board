<style>
.container {
    display: inline-block;
    cursor: pointer;
    width: 50px !important;
}
.bar1, .bar2, .bar3 {
    width: 35px;
    height: 4px;
    background-color: #333;
    margin: 6px 0;
    transition: 0.4s;
}
/* Rotate first bar */
.change .bar1 {
    -webkit-transform: rotate(-45deg) translate(-5px, 8px) ;
    transform: rotate(-45deg) translate(-5px, 8px) ;
}

/* Fade out the second bar */
.change .bar2 {
    opacity: 0;
}

/* Rotate last bar */
.change .bar3 {
    -webkit-transform: rotate(45deg) translate(-8px, -8px) ;
    transform: rotate(45deg) translate(-8px, -8px) ;
}

.adjustment{
	padding-top: 0px !important;
	padding-bottom: 0px !important;
}
.adjustment>div{
	padding: 10px 10px !important;
}
</style>
<script>
function myFunction(x) {
    x.classList.toggle("change");
}
</script>

<div class="navbar">
  <div class="navbar-header">
    <a href="<%=request.getContextPath()%>" class="navbar-brand2"><img src="images/redhat-logo.png"/>
    	<span class="navbar-brand3"></span>
    </a>
  </div>
  <div class="collapse navbar-collapse bs-example-js-navbar-collapse">
    <ul class="nav navbar-nav">
      <li class="dropdown">
        <a aria-expanded="false" aria-haspopup="true" role="button" data-toggle="dropdown" class="menu-title dropdown-toggle" href="#" id="drop2">
          Ninja Program
          <span class="caret"></span>
        </a>
        <ul aria-labelledby="drop2" class="dropdown-menu">
          <li><a href="scorecards.jsp">Scorecards</a></li>
          <li><a href="leaderboard.jsp">Leaderboard</a></li>
          <li><a href="events.jsp">Events</a></li>
          <li><a href="https://docs.google.com/a/redhat.com/forms/d/e/1FAIpQLSdWGcCks2zKKnVoZFQz3CieLQDc1lsSex_Knwh_-eyRm0ZQTg/viewform">Registration Form</a></li>
          <li><a href="https://docs.google.com/spreadsheets/d/1E91hT_ZpySyvhnANxqZ7hcBSM2EEd9TqfQF-cavB8hQ">Responses Spreadsheet</a></li>
        </ul>
      </li>
    </ul>
    
    <ul class="nav navbar-nav">
      <li class="dropdown">
        <a aria-expanded="false" aria-haspopup="true" role="button" data-toggle="dropdown" class="menu-title dropdown-toggle" href="#" id="drop2">
          Admin
          <span class="caret"></span>
        </a>
        <ul aria-labelledby="drop2" class="dropdown-menu">
          <li><a href="config.jsp">Config (Here be dragons!)</a></li>
          <li><a href="database.jsp">Database (Here be dragons!)</a></li>
        </ul>
      </li>
    </ul>
    
    
    <ul class="nav navbar-nav navbar-right">
      <li class="dropdown" id="fat-menu">
        <a aria-expanded="false" aria-haspopup="true" role="button" data-toggle="dropdown" class="dropdown-toggle adjustment" href="#" id="drop3">
        	<!--
          <image style="height:20px;"  src="https://cdn4.iconfinder.com/data/icons/wirecons-free-vector-icons/32/menu-alt-512.png"/>
        	-->
          
          <div class="container" onclick="myFunction(this)" style="height:56px">
					  <div class="bar1"></div>
					  <div class="bar2"></div>
					  <div class="bar3"></div>
					</div>
          <span class="caretx"></span>
          <!-- 
           -->
        </a>
        <ul aria-labelledby="drop3" class="dropdown-menu">
          <!--
          <li><a href="#">Settings</a></li>
          <li class="divider" role="separator"></li>
          -->
          <li><a href="api/logout">Log out</a></li>
        </ul>
      </li>
    </ul>
  </div>
</div>