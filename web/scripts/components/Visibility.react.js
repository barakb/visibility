var React = require('react');
var VisibilityStore = require('../stores/VisibilityStore');
var VisibilityActions = require('../actions/VisibilityActions');

var ReactBsTable  = require('react-bootstrap-table');
var BootstrapTable = ReactBsTable.BootstrapTable;
var TableHeaderColumn = ReactBsTable.TableHeaderColumn;


function getVisibilityState() {
   var registrarsMap = VisibilityStore.getAll();
   var registrars = Object.keys(registrarsMap).map(function (key) {
                                            return registrarsMap[key];
                                          });
    return {registrarsMap : registrarsMap, registrars: registrars}
}


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

        function onRowSelect(row, isSelected){
          console.log(row);
          console.log("selected: " + isSelected)
        }

        var selectRowProp = {
          mode: "radio",
          clickToSelect: true,
          bgColor: "rgb(238, 193, 213)",
          onSelect: onRowSelect
        };

          return (
                <BootstrapTable data={this.state.registrars} height="462" pagination={true} selectRow={selectRowProp} search={true} columnFilter={true}>
                    <TableHeaderColumn isKey={true} dataField="id" dataSort={true} width="300px">Registrar Id</TableHeaderColumn>
                    <TableHeaderColumn dataField="name" dataSort={true} width="250px">Name</TableHeaderColumn>
                    <TableHeaderColumn dataField="cls" dataSort={true}>Class</TableHeaderColumn>
                </BootstrapTable>
          );
      },

      _onChange: function() {
          this.setState(getVisibilityState());
      }
});

module.exports = VisibilityApp