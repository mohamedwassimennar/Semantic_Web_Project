import { Component, OnInit } from '@angular/core';
import { InventoryService } from '../../service/inventory.service';
import { Inventory } from '../../models/inventory';
import { InventoryClass } from '../../models/inventoryclass';
import { Router } from '@angular/router';
import { Food } from 'src/app/models/food';
import { FoodService } from 'src/app/service/food.service';
import { foodclass } from 'src/app/models/foodclass';

@Component({
  selector: 'app-listinventory',
  templateUrl: './listinventory.component.html',
  styleUrls: ['./listinventory.component.css']
})
export class ListinventoryComponent implements OnInit {
  inventories: Inventory[] = [];
  loading: boolean = true;
  newInventory: NewInventoryInput = { currentQuantity: '', foodId: '' };
  showForm = false;

  constructor(private inventoryService: InventoryService, private restaurantService: FoodService, private router: Router) { }

  ngOnInit(): void {
    this.getRestaurants(); // Fetch restaurants on initialization
    this.getInventories();
  }

  getInventories() {
    this.loading = true;
    this.inventoryService.getInventories().subscribe({
      next: (inventories: Inventory[]) => {
        this.inventories = inventories;
        this.loading = false;
      },
      error: error => {
        console.error('Error fetching inventory:', error);
        this.loading = false;
      }
    });
  }

  deleteInventory(inventory: Inventory) {
    this.inventoryService.deleteInventory(inventory).subscribe({
      next: () => this.getInventories(),
      error: error => {console.log('deleted inventory successfully'); this.getInventories();}
    });
  }

  addInventory(food: Food, inventory: NewInventoryInput) {
    this.inventoryService.addInventory(food, inventory).subscribe({
      next: () => {
        this.getInventories(); // Refresh the list after adding
      },
      error: (err) => {
        alert('Added inventory successfully.');
        this.getInventories(); // Refresh the list after adding
      },
    });
  }

  navigateToUpdateInventory(inventory: any) {
    this.router.navigate(['updateinventory', inventory.inventory]);
  }

  sortInventories() {
    this.inventories.sort((a, b) => {
      return a.currentQuantity - b.currentQuantity;
    });
  }

  restaurants: { food: string; foodType: string; quantity: string; expiryDate: string; }[] = []; // Array to hold the restaurant data
  listUnis: Food[] = [];
  result = 0;
  newUni: foodclass = { food: '', foodType: '', quantity: '', expiryDate: '' };


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

  addInventoryForRestaurant(restaurant: any) {
    const newQuantity = prompt('How much quantity would you like to add?');
    if (newQuantity === null || newQuantity.trim() === '') {
        return; // User cancelled or entered empty value
    }

    // Validate that the input is a positive number
    const quantity = Number(newQuantity);
    if (isNaN(quantity) || quantity <= 0) {
        alert('Please enter a valid positive number for quantity');
        return;
    }

    // Extract the food ID from the full URI
    const foodId = restaurant.food.replace('http://rescuefood.org/ontology#Food', '');

    this.newInventory = {
        currentQuantity: newQuantity,
        foodId: foodId // Using just the ID part
    };
    console.log(this.newInventory);
    this.addInventory(foodId, this.newInventory);
  }

  promptAndUpdateInventory(inventory: Inventory): void {
    const newQuantity = prompt('Enter new quantity:', inventory.currentQuantity.toString());
    
    if (newQuantity !== null) {
      const quantity = parseFloat(newQuantity);
      if (!isNaN(quantity) && quantity >= 0) {
        this.inventoryService.updateInventory(inventory, quantity).subscribe({
          next: () => {
            // Update the local inventory list
            inventory.currentQuantity = quantity;
          },
          error: (error) => {
            alert('Updated inventory successfully');
            this.getInventories(); // Refresh the list after adding
          }
        });
      } else {
        alert('Please enter a valid number');
      }
    }
  }
}
export interface NewInventoryInput {
  currentQuantity: string;
  foodId: string;
}