import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';
import { restclass } from '../../models/restclass'; // Update with your actual path
import { RestaurantService } from '../../service/restaurant.service'; // Update with your actual path

@Component({
  selector: 'app-modifrestaurant',
  templateUrl: './modifrestaurant.component.html',
  styleUrls: ['./modifrestaurant.component.css']
})
export class ModifrestaurantComponent implements OnInit {
  restaurantForm: FormGroup; // Form group to manage the form
  isSubmitting: boolean = false; // To track submission state
  successMessage: string = ''; // For displaying success messages
  errorMessage: string = ''; // For displaying error messages
  restaurantId!: string; // Store the restaurant ID

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private restaurantService: RestaurantService,
    private formBuilder: FormBuilder
  ) {
    // Initialize the form without validators
    this.restaurantForm = this.formBuilder.group({
      restaurant: [''], // Restaurant ID/URI (should be populated)
      name: [''],       // Restaurant name
      contact: [''],    // Contact details
      address: ['']     // Address
    });
  }

  ngOnInit(): void {
    // Retrieve the restaurant ID from the route parameters
    this.activatedRoute.params.subscribe(params => {
      this.restaurantId = params['id']; // Assumes your route has a parameter named 'id'
      
      // If you have the restaurant data available, populate the form here
      this.restaurantForm.patchValue({
        restaurant: this.restaurantId, // Set the restaurant ID
        // You can set other values if you already have them (e.g. name, contact, address)
      });
    });
  }

  onSubmit() {
    // Set the submitting state
    this.isSubmitting = true;

    // Prepare the restaurant object for modification
    const modifiedRestaurant: restclass = this.restaurantForm.value;

    // Call the service to modify the restaurant
    this.restaurantService.modifyRestaurant(modifiedRestaurant).subscribe(
      response => {
        console.log('Restaurant modified successfully:', response);
        this.successMessage = 'Restaurant modified successfully!'; // Set success message
        this.router.navigate(['/listrestaurant']); // Adjust the route as necessary
      },
      error => {
        console.error('Error modifying restaurant:', error);
        this.errorMessage = 'Error modifying restaurant: ' + error.error; // Use error.error for detailed message
      },
      () => {
        this.isSubmitting = false; // Reset the submitting state
      }
    );
  }


}
