var React = require('react');
global.jQuery = require('jquery');

var VisibilityStore = require('../stores/VisibilityStore');
var VisibilityActions = require('../actions/VisibilityActions');
var Button = require('../components/Button.react');
var Inspector = require('react-json-inspector');

var ReactBsTable  = require('react-bootstrap-table');
var BootstrapTable = ReactBsTable.BootstrapTable;
var TableHeaderColumn = ReactBsTable.TableHeaderColumn;

function getVisibilityState() {
   var all = VisibilityStore.getAll();
   var registrarsMap = all.registrars ? all.registrars : {};
   var fireRandomEvents = all.fireRandomEvents;
   var registrars = Object.keys(registrarsMap).map(function (key) {
                                            return registrarsMap[key];
                                          });
   return {registrarsMap : registrarsMap, registrars: registrars, fireRandomEvents : fireRandomEvents, selected:all.selectedRow}
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
        var that = this;
        function onRowSelect(row, isSelected){
//          that.state.selected = row;
//          that.setState(that.state);
            VisibilityActions.rowSelected(row.id);
        }

        var selectRowProp = {
          mode: "radio",
          clickToSelect: true,
          bgColor: "rgb(238, 193, 213)",
          onSelect: onRowSelect
        };

//          var buttonName = this.state.fireRandomEvents ? "FiringRandomEvents" : "Quite";
//          var onToggleFiringRandomEvents = function(){
//             global.jQuery.post("../api/discovery/toggleFiringRandomEvent", function(){
//               console.info("got back", arguments);
//             });
//          }
//                <BootstrapTable data={this.state.registrars} height="462" pagination={true} selectRow={selectRowProp} search={true} columnFilter={true}>
//                <Button name={buttonName} onClick={onToggleFiringRandomEvents}></Button>
          function isExpanded(){
            return true;
          }
          return (
                <div>
                <BootstrapTable data={this.state.registrars} height="462" selectRow={selectRowProp} search={true}>
                    <TableHeaderColumn isKey={true} dataField="id" dataSort={true} width="300px">Registrar Id</TableHeaderColumn>
                    <TableHeaderColumn dataField="name" dataSort={true} width="250px">Name</TableHeaderColumn>
                    <TableHeaderColumn dataField="cls" dataSort={true}>Class</TableHeaderColumn>
                </BootstrapTable>
                <Inspector data={this.state.selected} isExpanded={isExpanded} />
                </div>
          );
      },

      _onChange: function() {
          this.setState(getVisibilityState());
      }
});

module.exports = VisibilityApp