# HouseHunter
This Android application solves the traveling salesman problem for those looking to buy a new house. Addresses can be entered in to the application which will then show in google maps the fastest route to get to each open house. 
HouseHunter uses REST web services including the Google Maps API, the Google Geocoding API, and the Google Distance Matrix API. Using async tasks, this application loads data from several API's and uses Prim's algorithm to find the shortest spanning tree. The application then draws a polyline on Google Maps to display the route. 
