import React from 'react';
import ReactDOM from 'react-dom';
import Version from './App';
import './index.css';
import $ from 'jquery';
import 'react-materialize'

ReactDOM.render(
  <Version />,
  document.getElementById('version')
);

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

		let separateWordsArea = $('.separate-words');
		separateWordsArea.empty();
		let words = text.trim().split(/[\s]+/);
		if (words.length === 1 || (words.length === 2 && words[0].toLowerCase() === 'at')) {

			//if one word - show article
			searchOrdnetArticle(words[words.length - 1]);
		} else {
			words.forEach(word => {
				separateWordsArea.append(`<a class="word waves-effect waves-light btn" style="margin-right:5px; margin-top: 5px;">${word}</a>`)
			});
			$('.word').click((event) => {
				article.empty();
				meanings.empty();
				translateWordType.empty();
				let word = event.target.innerText;
				searchOrdnetArticle(word);
			});
		}

	});

	function searchOrdnetArticle(word) {
		$.ajax('/rest/ordnet', {
			'data': 'text='+word,
			'type': 'POST',
			'processData': false
		})
		.done(function(data) {

			data.options.forEach(option => {
				translateWordType.append(`<div class="chip word-type">
						<input type="hidden" value="${option.link}">
						${option.text}
					</div>`)
			});

			$('.word-type').off().click((event) => {
				article.empty();
				meanings.empty();
				$.ajax('/rest/ordnet', {
					'data': 'link=' + encodeURIComponent(event.target.children[0].value),
					'type': 'POST',
					'processData': false
				}).done((data) => {
					if (data.article) {
						if (data.article.wordType) {
							article.append(`<span class="left badge new blue" data-badge-caption="${data.article.wordType}"></span>`)
						}

						if (data.article.bending) {
							article.append(`<span class="left new blue" style="color: white; margin-left: 10px; padding: 0 6px; font-weight: 300; border-radius: 2px;">${data.article.bending}</span>`)
						}

						if (data.article.meanings) {
							meanings.show();
							data.article.meanings.forEach(meaning => {
								meanings.append(`<li class="collection-item meaning">${meaning}</li>`)
							});
							$('.meaning').off().click((event) => {
								$('#text').val(event.target.innerText);
								$('#submit').click();
							});
						}


					}
				})
			});

			if (data.article) {
				if (data.article.wordType) {
					article.append(`<span class="left badge new blue" data-badge-caption="${data.article.wordType}"></span>`)
				}

				if (data.article.bending) {
					article.append(`<span class="left new blue" style="color: white; margin-left: 10px; padding: 0 6px; font-weight: 300; border-radius: 2px;">${data.article.bending}</span>`)
				}

				if (data.article.meanings) {
					meanings.show();
					data.article.meanings.forEach(meaning => {
						meanings.append(`<li class="collection-item meaning">${meaning}</li>`)
					});
					$('.meaning').off().click((event) => {
						$('#text').val(event.target.innerText);
						$('#submit').click();
					});
				}
			}

		});
	}

	$('.btn-clear').click(() => {
		$('#text').val('');
	})
});
