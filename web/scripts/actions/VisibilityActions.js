var AppDispatcher = require('../dispatcher/AppDispatcher');
var VisibilityConstants = require('../constants/VisibilityConstants');

var VisibilityActions = {

  /**
   * @param  {string} text
   */
  created: function(registrar) {
    AppDispatcher.dispatch({
      actionType: VisibilityConstants.CREATED,
      registrar: registrar
    });
  },

  /**
   * @param  {string} id The ID of the Visibility item
   * @param  {string} text
   */
  updated: function(id, registrar) {
    AppDispatcher.dispatch({
      actionType: VisibilityConstants.UPDATED,
      id: id,
      registrar: registrar
    });
  },

  /**
     * @param  {string} id
   */
  removed: function(id) {
      AppDispatcher.dispatch({
        actionType: VisibilityConstants.REMOVED,
        id: id
      });
   },

  rowSelected: function(id){
      AppDispatcher.dispatch({
        actionType: VisibilityConstants.ROW_SELECTED,
        id: id
      });
  },

  setFireRandomEvents: function(fireRandomEvents){
     AppDispatcher.dispatch({actionType : "setFireRandomEvents",  fireRandomEvents : fireRandomEvents
     });
  },
}

module.exports = VisibilityActions;