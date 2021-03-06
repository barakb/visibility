var React = require('react');
var ReactDOM = require('react-dom');
global.jQuery = require('jquery');

var VisibilityApp = require('./components/Visibility.react');

ReactDOM.render(
  <VisibilityApp />,
  document.getElementById('visibilityApp')
);


var VisibilityActions = require('./actions/VisibilityActions');

jQuery.get("../api/discovery/all", function(data){
    var fireRandomEvents = data.fireRandomEvents;
    VisibilityActions.setFireRandomEvents(fireRandomEvents)
    var items = data.items;
    Object.keys(items).forEach(function (key) {
                                  VisibilityActions.created(items[key]);
                                });
});


if (!!window.EventSource) {
  var source = new EventSource("../api/discovery/subscribe");

  source.addEventListener('open', function(e) {
    console.log("open", e);
  }, false);

  source.addEventListener('error', function(e) {
    if (e.readyState == EventSource.CLOSED) {
        console.log("closed", e);
    }else{
        console.log("error", e);
    }
  }, false);

   source.addEventListener("CREATED", function(e){
      VisibilityActions.created(JSON.parse(e.data))
   }, false);
   source.addEventListener("REMOVED", function(e){
      VisibilityActions.removed(JSON.parse(e.data).id)
   }, false);
   source.addEventListener("UPDATED", function(e){
      var item = JSON.parse(e.data);
      VisibilityActions.updated(item.id, item)
   }, false);
   source.addEventListener("GENERATE_RANDOM_EVENTS", function(e){
      var item = JSON.parse(e.data);
      VisibilityActions.setFireRandomEvents(item["GENERATE_RANDOM_EVENTS"])
   }, false);

  source.onmessage = function (event) {
    console.info("sse event", event);
  };
}