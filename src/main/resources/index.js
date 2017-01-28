$(document).ready(function() {
	let translateResults = $('.translate-results');
	let translateWordType = $('.translate-word-type');
	let article = $('.article');
	let meanings = $('.meanings');
	meanings.hide();


	$('#submit').click(() => {
		translateWordType.empty();
		translateResults.empty();
		article.empty();
		meanings.empty();
		meanings.hide();

		const text = $('#text').val();
		$.ajax('/rest/translate', {
			'data': 'text='+text,
			'type': 'POST',
			'processData': false
		})
		.done(function(data) {
			data.forEach(langResponseString => {
				let langResponse = JSON.parse(langResponseString);
				translateResults.append(`<div class="col s6">${langResponse.text[0]}</div>`);
			});
		});

		let words = text.trim().split(/[\s]+/);
		let separateWords = $('.separate-words');
		separateWords.empty();
		console.log(words);
		words.forEach(word => {
			separateWords.append(`<a class="word waves-effect waves-light btn" style="margin-right:5px; margin-top: 5px;">${word}</a>`)
		});

		$('.word').off().click((event) => {
			let word = event.target.innerText;
			translateWordType.empty();
			$.ajax('/rest/ordnet', {
				'data': 'text='+word,
				'type': 'POST',
				'processData': false
			})
			.done(function(data) {

				data.options.forEach(option => {
					translateWordType.append(`<div class="chip">${option.text}</div>`)
				});

				if (data.article) {
					if (data.article.wordType) {
						article.append(`<span class="left badge new blue" data-badge-caption="${data.article.wordType}"></span>`)
					}

					if (data.article.bending) {
						article.append(`<span class="left badge new blue" data-badge-caption="${data.article.bending}"></span>`)
					}

					if (data.article.meanings) {
						meanings.show();
						data.article.meanings.forEach(meaning => {
							meanings.append(`<li class="collection-item">${meaning}</li>`)
						});
					}
				}

			});
		});

	});

	$('.btn-clear').click(() => {
		$('#text').val('');
	})
});
