import { Component, OnInit } from '@angular/core';
import { RestaurantService } from '../../service/restaurant.service'; // Adjust the path as necessary
import { Restaurant } from '../../models/restaurant'; // Adjust the path as necessary
import { restclass } from '../../models/restclass'; // Adjust the path as necessary

import { Router } from '@angular/router'; // Import Router

@Component({
  selector: 'app-listrestaurant',
  templateUrl: './listrestaurant.component.html',
  styleUrls: ['./listrestaurant.component.css']
})
export class ListrestaurantComponent implements OnInit {
  restaurants: { restaurant: string; name: string; contact: string; address: string; }[] = []; // Array to hold the restaurant data
  loading: boolean = true; // Loading state
  listUnis: Restaurant[] = [];
  result = 0;
  newUni: restclass = { restaurant: '', name: '', contact: '', address: '' };
  showForm = false;

  constructor(private restaurantService: RestaurantService, private router: Router) { }

  ngOnInit(): void {
    this.getRestaurants(); // Fetch restaurants on initialization
  }

  // Method to fetch the list of restaurants
  getRestaurants() {
    this.restaurantService.getRestaurants().subscribe(
        (data: any) => {
            // Access the array from the data structure
            const bindings = data.results.bindings; // Get the array of restaurant data

            // Check if bindings is an array
            if (Array.isArray(bindings)) {
                this.restaurants = bindings.map((restaurant: any) => ({
                    restaurant: restaurant.restaurant.value, // Access the 'value' for URI
                    name: restaurant.name.value,             // Access the 'value' for name
                    contact: restaurant.contact.value,       // Access the 'value' for contact
                    address: restaurant.address.value         // Access the 'value' for address
                }));
            } else {
                console.error('Expected an array but got:', bindings);
            }
        },
        error => {
            console.error('Error fetching restaurants:', error);
        }
    );
}
// Method to delete a restaurant
deleteRestaurant(restaurant: any) {
  this.restaurantService.deleteRestaurant(restaurant).subscribe(
    response => {
      console.log('Restaurant deleted:', response);
      // Refresh the list after deletion
      this.getRestaurants();

    },
    error => {
      console.error('Error deleting restaurant:', error);
      this.getRestaurants();

    }
  );
}
// Method to navigate to the AddRestaurantComponent
addRestaurant() {
  this.router.navigate(['addrest']); // Adjust the path as necessary
}
getSortedRestaurants() {
  this.restaurantService.getSortedRestaurants().subscribe(
      (data: any) => {
          const bindings = data.results.bindings; // Get the array of sorted restaurant data

          if (Array.isArray(bindings)) {
              this.restaurants = bindings.map((restaurant: any) => ({
                  restaurant: restaurant.restaurant.value,
                  name: restaurant.name.value,
                  contact: restaurant.contact.value,
                  address: restaurant.address.value
              }));
          } else {
              console.error('Expected an array but got:', bindings);
          }
      },
      error => {
          console.error('Error fetching sorted restaurants:', error);
      }
  );
}

addfoyer(uni: restclass) {
  this.restaurantService.addRestaurant2(uni).subscribe({
    next: (addedUni) => {
      this.listUnis.push(addedUni as restclass);
      this.showForm = false; 
      this.getRestaurants(); // Call getRestaurants to refresh the list

    },
    error: (err) => console.log(err),
  });
}



navigateToUpdatefoyer(restaurant: any) {
  this.router.navigate(['update', restaurant.restaurant]); // Pass the restaurant ID or URI
}
/*
navigateToUpdatefoyer(restaurant: any) {
  // Log the restaurant object to check its structure
  console.log('Restaurant data:', restaurant);

  // Check if restaurant and its URI are defined
  if (restaurant && restaurant.restaurant) {
    const restaurantId = restaurant.restaurant.split('#')[1]; // Extract the identifier after the '#'
    this.router.navigate(['update', restaurantId]); // Pass just the identifier
  } else {
    console.error('Invalid restaurant data provided:', restaurant);
    // Handle the case where restaurant data is invalid
    // Optionally, you might want to notify the user
  }
}*/
sortRestaurants() {
  this.restaurants.sort((a, b) => {
    const nameA = a.name.toLowerCase(); 
    const nameB = b.name.toLowerCase(); 

    if (nameA < nameB) {
      return -1; 
    }
    if (nameA > nameB) {
      return 1; 
    }
    return 0; 
  });
}

}
