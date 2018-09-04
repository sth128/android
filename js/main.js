// Save Bookmark
function saveBookmark(properties){
  if(localStorage.getItem('bookmarks') === null){
    var bookmarks = [];
  } else {
    var bookmarks = JSON.parse(localStorage.getItem('bookmarks'));
  }
  // Add bookmark to array
  bookmarks.push(properties);
  // Re-set back to localStorage
  localStorage.setItem('bookmarks', JSON.stringify(bookmarks));

  fetchBookmarks();
}

function centerOnMarkerIndex(i){
  var location = mapMarkers[i].position;
  map.panTo(location);
  map.setZoom(13);
}

// Delete bookmark and map marker
function deleteBookmark(i){
  var bookmarks = JSON.parse(localStorage.getItem('bookmarks'));
  bookmarks.splice(i, 1);
  mapMarkers[i].setMap(null);
  mapMarkers.splice(i, 1);
  // Re-set back to localStorage
  localStorage.setItem('bookmarks', JSON.stringify(bookmarks));

  // Re-fetch bookmarks
  fetchBookmarks();
}

// Fetch bookmarks
function fetchBookmarks(addMapMarker){
  var bookmarks = JSON.parse(localStorage.getItem('bookmarks'));
  var bookmarksResults = document.getElementById('bookmarksResults');

  bookmarksResults.innerHTML = '';
  for(let i = 0; i < bookmarks.length; i++){
    var content = bookmarks[i].content;
    var showBtn = document.createElement('a');
    showBtn.className = 'btn btn-default btn-sm float-right';
    showBtn.addEventListener("click", function(){centerOnMarkerIndex(i);});
    showBtn.appendChild(document.createTextNode('show'));

    var deleteBtn = document.createElement('a');
    deleteBtn.className = 'btn btn-danger btn-sm float-right delete';
    deleteBtn.addEventListener("click", function(){deleteBookmark(i);});
    deleteBtn.appendChild(document.createTextNode('X'));

    var text = document.createElement('strong');
    text.appendChild(document.createTextNode(content));
    text.appendChild(document.createTextNode('  '));
    text.appendChild(showBtn);
    text.appendChild(document.createTextNode('  '));
    text.appendChild(deleteBtn);

    var div = document.createElement('div');
    div.className = 'well';
    div.appendChild(text);
    // bookmarksResults.innerHTML += '<div class="well">'+
    //                               '<h3>'+content+
    //                               ' <a onclick="centerOnMarkerIndex(\''+i+'\')" class="btn btn-default float-right">Show</a>' +
    //                               '<a onclick="deleteBookmark(\''+i+'\')" class="btn btn-danger btn-sm float-right delete">X</a> ' +
    //                               '</h3>'+
    //                               '</div>';
    bookmarksResults.appendChild(div);
    if(addMapMarker == true) {
      addMarker(bookmarks[i]);
    }
  }
}

// GOOGLE MAP
var map;
var mapMarkers = [];
// Load the Visualization API and the columnchart package.
google.load('visualization', '1', {packages: ['columnchart']});

// invoked by google map callback in html
function initMap(){
  var directionsService = new google.maps.DirectionsService;
  var directionsDisplay = new google.maps.DirectionsRenderer;
  var elevator = new google.maps.ElevationService;
  var toronto = {lat: 43.6532, lng: -79.3832};
  map = new google.maps.Map(document.getElementById('map'), {
    zoom: 10,
    center: toronto,
    mapTypeId: 'roadmap',
    fullscreenControl: false,
    streetViewControl: false,
    mapTypeControl: false
  });
  var onChangeHandler = function() {
    calculateAndDisplayRoute(directionsService, directionsDisplay, elevator);
  };
  document.getElementById('route').addEventListener('click', onChangeHandler);
  directionsDisplay.setMap(map);
  //directionsDisplay.setPanel(document.getElementById('directionsPanel'));

  fetchBookmarks(true);
}

function calculateAndDisplayRoute(directionsService, directionsDisplay, elevator) {
  directionsService.route({
    origin: document.getElementById('start-select').value,
    destination: document.getElementById('end-select').value,
    travelMode: 'DRIVING'
  }, function(response, status) {
    if (status === 'OK') {
      let path = response.routes[0].overview_path;
      console.log(path);
      directionsDisplay.setDirections(response);
      displayPathElevation(path, elevator)
    } else {
      window.alert('Directions request failed due to ' + status);
    }
  });
}

function displayPathElevation(path, elevator) {
  // Display a polyline of the elevation path.
  // new google.maps.Polyline({
  //   path: path,
  //   strokeColor: '#0000CC',
  //   strokeOpacity: 0.4,
  //   map: map
  // });

  // Create a PathElevationRequest object using this array.
  // Ask for 256 samples along that path.
  // Initiate the path request.
  elevator.getElevationAlongPath({
    'path': path,
    'samples': 256
  }, plotElevation);
}

function plotElevation(elevations, status) {
  var chartDiv = document.getElementById('elevation_chart');
  if (status !== 'OK') {
    // Show the error code inside the chartDiv.
    chartDiv.innerHTML = 'Cannot show elevation: request failed because ' +
        status;
    return;
  }
  // Create a new chart in the elevation_chart DIV.
  var chart = new google.visualization.ColumnChart(chartDiv);

  // Extract the data from which to populate the chart.
  // Because the samples are equidistant, the 'Sample'
  // column here does double duty as distance along the
  // X axis.
  var data = new google.visualization.DataTable();
  data.addColumn('string', 'Sample');
  data.addColumn('number', 'Elevation');
  for (var i = 0; i < elevations.length; i++) {
    data.addRow(['', elevations[i].elevation]);
  }

  // Draw the chart using the data within its DIV.
  chart.draw(data, {
    height: 150,
    legend: 'none',
    titleY: 'Elevation (m)'
  });
}

// Adds a marker to the map and push to the array.
// {
//   position:{lat:42.4668,lng:-70.9495},
//   icon:'https://developers.google.com/maps/documentation/javascript/examples/full/images/beachflag.png',
//   content:'<h1>Lynn MA</h1>'
// }
function addMarker(properties) {
  var marker = new google.maps.Marker({
    position: properties.position,
    map: map
  });
  if(properties.icon){
    marker.setIcon(properties.icon);
  }
  if(properties.content){
    var infoWindow = new google.maps.InfoWindow({
      content:'<strong>' + properties.content + '</strong>'
    });

    marker.addListener('click', function(){
      infoWindow.open(map, marker);
    });
  }
  mapMarkers.push(marker);
}

// GEOCODE
var locationForm = document.getElementById('location-form');
locationForm.addEventListener('submit', submitGeocode);

function submitGeocode(e){
  // Prevent actual submit
  e.preventDefault();
  var address = document.getElementById('address-input').value;

  axios.get('https://maps.googleapis.com/maps/api/geocode/json',{
    params:{
      address:address,
      key:'AIzaSyC9Yr7m1MOsNko1dfHNT3go5yGAAnCvEoc'
    }
  })
  .then(function(response){
    // Log full response
    //console.log(response);
    var place = response.data.results[0];
    var formattedAddress = '';
    if (place.address_components) {
      formattedAddress = [
        (place.address_components[0] && place.address_components[0].short_name || ''),
        (place.address_components[1] && place.address_components[1].short_name || ''),
        (place.address_components[2] && place.address_components[2].short_name || '')
      ].join(' ');
    }
    var location = place.geometry.location;
    var properties = {
      position:location,
      content:`${formattedAddress}`
    };
    var startSelect = document.getElementById('start-select');
    var endSelect = document.getElementById('end-select');
    console.log(place);
    var latlng = location.lat + ',' + location.lng;
    startSelect.options[startSelect.options.length] = new Option(formattedAddress, latlng);
    endSelect.options[endSelect.options.length] = new Option(formattedAddress, latlng);
    addMarker(properties);
    saveBookmark(properties);

    if (!map.getBounds().contains(location)) {
      map.panTo(location);
      map.setZoom(13);
    }
  })
  .catch(function(error){
    console.log(error);
  });
  // Clear form
  locationForm.reset();
}
