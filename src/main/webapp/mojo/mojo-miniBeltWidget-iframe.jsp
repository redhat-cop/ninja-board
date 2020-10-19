<div>
	<script src="https://code.jquery.com/jquery-3.5.1.min.js" type="text/javascript">


	</script>

	<p>
		<iframe id="frame" src="https://community-ninja-board-ninja-board.int.open.paas.redhat.com/community-ninja-board/mojo/mojo-miniBeltWidget.jsp" style="width: 100%; height:300px;" border="0"></iframe>
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
