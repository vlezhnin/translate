$(document).ready(function() {
	$('#submit').click(function() {
		const text = $('#text').val();
		$.ajax('/rest/translate', {
			'data': 'text='+text,
			'type': 'POST',
			'processData': false
		})
		.done(function(data) {
			let translateResults = $('.translate-results');
			translateResults.empty();
			data.forEach(langResponseString => {
				let langResponse = JSON.parse(langResponseString);
				translateResults.append(`<div>${langResponse.lang}: ${langResponse.text[0]}</div>`);
			});

		});
	});
});
