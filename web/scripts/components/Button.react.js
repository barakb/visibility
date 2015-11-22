var React = require('react');
var VisibilityStore = require('../stores/VisibilityStore');
var VisibilityActions = require('../actions/VisibilityActions');


var Button = React.createClass({

      render: function() {
        return (
            <button className="btn btn-default" type="button" onClick={this.props.onClick}>{this.props.name}</button>
        )
      }

});

module.exports = Button