$(document).ready(function() {
	$('#submit').click(function() {
		const text = $('#text').val();
		$.ajax('/rest/translate', {
			'data': 'text='+text,
			'type': 'POST',
			'processData': false
		})
			.done(function(data) {
				console.log(data);
			});
	});
});
