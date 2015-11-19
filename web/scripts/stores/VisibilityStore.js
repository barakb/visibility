var AppDispatcher = require('../dispatcher/AppDispatcher');
var EventEmitter = require('events').EventEmitter;
var VisibilityConstants = require('../constants/VisibilityConstants');
var assign = require('object-assign');

var CHANGE_EVENT = 'change';

var _registrars = {};


function create(registrar) {
  var id = registrar.id;
  _registrars[id] = {
    id: id,
    cls : registrar.cls,
    name: registrar.name
  };
}

function update(id, updates) {
  _registrars[id] = assign({}, _registrars[id], updates);
}

function destroy(id) {
  delete _registrars[id];
}

var VisibilityStore = assign({}, EventEmitter.prototype, {

  getAll: function() {
    return _registrars;
  },

  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }

});

AppDispatcher.register(function(action) {
  var registrar;
   switch(action.actionType) {
    case VisibilityConstants.CREATED:
      registrar = action.registrar;
      create(registrar);
      VisibilityStore.emitChange();
      break;

    case VisibilityConstants.UPDATED:
      update(action.id, {registrar: registrar});
      VisibilityStore.emitChange();
      break;

    case VisibilityConstants.REMOVED:
      destroy(action.id);
      VisibilityStore.emitChange();
      break;

    default:
         // no op
    }
});

module.exports = VisibilityStore;