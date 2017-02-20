import React from "react";
import "./LanguageSelect.css";
import { Button, Dropdown, NavItem, Icon } from 'react-materialize';

const LanguageSelect = React.createClass({

	getInitialState: function() {
		let selectedLanguage = localStorage.getItem("language");
		console.log('Language from localstorage', selectedLanguage);
		if (!selectedLanguage) {
			selectedLanguage = getNavigatorLanguage();
		}
		if (!selectedLanguage) {
			selectedLanguage = 'es';
		}
		localStorage.setItem("language", selectedLanguage);

		return {
			language: selectedLanguage,
			available: ['ru', 'es', 'de', 'ro', 'pt', 'it', 'fr', 'zh']
		}
	},

	componentDidMount: function() {
		// fetch('/rest/lang').then(result => {
		// 	result.json().then(languages => {
		// 		this.setState({
		// 			available: languages
		// 		})
		// 	});
		// });
	},

	componentWillUnmount: function() {
	},

	onLanguageSelected: function(lang) {
		localStorage.setItem("language", lang);
		this.setState({
			language: lang
		});
		console.log('Language selected', lang)
	},

	render: function() {
		return (
			<Dropdown trigger={
				<Button waves='light'>{this.state.language}<Icon left>language</Icon></Button>
			}>
				{
					this.state.available.map(lang => <NavItem key={lang} onClick={()=>this.onLanguageSelected(lang)}>{lang}</NavItem>)
				}
			</Dropdown>

		);
	}
});

function getNavigatorLanguage() {
	let locale;
	if (navigator.languages !== undefined) {
		locale = navigator.languages[0];
	} else {
		locale = navigator.language;
	}
	if (locale) {
		return locale.substr(0, 2);
	}
	return undefined;
}

export default LanguageSelect;
