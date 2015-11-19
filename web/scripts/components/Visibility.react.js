var React = require('react');
var VisibilityStore = require('../stores/VisibilityStore');
var VisibilityActions = require('../actions/VisibilityActions');


function getVisibilityState() {
  return {
     registrars: VisibilityStore.getAll()
  };
}

var nextId = 0;

var VisibilityApp = React.createClass({

      getInitialState: function() {
          return getVisibilityState();
      },

      componentDidMount: function() {
          VisibilityStore.addChangeListener(this._onChange);
      },

      componentWillUnmount: function() {
        VisibilityStore.removeChangeListener(this._onChange);
      },

      render: function() {
          onClick = function(){
            console.info("click", VisibilityActions);
            VisibilityActions.created({"id":nextId++, "cls":"cls_" + nextId, "name" :"name_" + nextId});
          };

          var that = this;
          return (
            <div>
                <h1>
                  Barak
                </h1>
                <input type="button" onClick={onClick} value="Click Me!" />
                <ol>
                  {
                   Object.keys(this.state.registrars).map(function (key) {
                     var registrar = that.state.registrars[key]
                     return <li key={registrar.id}>{registrar.name}</li>;
                  })}
                </ol>
            </div>
          );
      },

      _onChange: function() {
          this.setState(getVisibilityState());
          console.info("state is ", this.state);
      }
});

module.exports = VisibilityApp