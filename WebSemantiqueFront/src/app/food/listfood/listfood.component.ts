import { Component, OnInit } from '@angular/core';
import { FoodService } from '../../service/food.service'; // Adjust the path as necessary

import { Food } from '../../models/food'; // Adjust the path as necessary
import { foodclass } from '../../models/foodclass'; // Adjust the path as necessary

import { Router } from '@angular/router'; // Import Router

@Component({
  selector: 'app-listfood',
  templateUrl: './listfood.component.html',
  styleUrls: ['./listfood.component.css']
})
export class ListfoodComponent implements OnInit {

  restaurants: { food: string; foodType: string; quantity: string; expiryDate: string; }[] = []; // Array to hold the restaurant data
  loading: boolean = true; // Loading state
  listUnis: Food[] = [];
  result = 0;
  newUni: foodclass = { food: '', foodType: '', quantity: '', expiryDate: '' };
  showForm = false;

  constructor(private restaurantService: FoodService, private router: Router) { }

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
                    food: restaurant.food.value, // Access the 'value' for URI
                    foodType: restaurant.foodType.value,             // Access the 'value' for name
                    quantity: restaurant.quantity.value,       // Access the 'value' for contact
                    expiryDate: restaurant.expiryDate.value         // Access the 'value' for address
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

addfoyer(uni: foodclass) {
  this.restaurantService.addRestaurant2(uni).subscribe({
    next: (addedUni) => {
      this.listUnis.push(addedUni as foodclass);
      this.showForm = false; 
      this.getRestaurants(); // Call getRestaurants to refresh the list
      this.router.navigate(['/listfood']); 

    },
    error: (err) => console.log(err),
  });
}
navigateToUpdatefoyer(restaurant: any) {
  this.router.navigate(['update', restaurant.restaurant]); // Pass the restaurant ID or URI
}
}