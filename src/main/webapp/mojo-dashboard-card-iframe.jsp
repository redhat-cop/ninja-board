<div>
	<script src="https://code.jquery.com/jquery-3.5.1.min.js" type="text/javascript" unsafe.integrity="sha256-9/aliU8dGd2tb6OSsuzixeV4y/faTqgFtohetphbbj0=" unsafe.crossorigin="anonymous">


	</script>

	<p>
		<iframe id="frame" src="https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/mojo/mojo-dashboard-card.jsp" style="width: 100%; height:500px;" border="0"></iframe>
	</p>
	<script type="text/javascript">
		/*<![CDATA[*/
		$(document).ready(function()
			{
				$("#frame").prop("src", $("#frame").prop("src")+"?username=" + Igloo.currentUser.namespace);
			});
			/*]]>*/

	</script>
</div>
