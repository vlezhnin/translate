import React from "react";
import "./App.css";

const Version = React.createClass({
	getInitialState: function() {
		return {
			version: null
		}
	},

	componentDidMount: function() {
		fetch('/rest/version').then(result => {
			result.text().then(text => {
				this.setState({
					version: text
				})
			});
		});
	},

	componentWillUnmount: function() {
	},

	render: function() {
		return (
			<div className="App">
				version: {this.state.version}
			</div>
		);
	}
});

export default Version;
