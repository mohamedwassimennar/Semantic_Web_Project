import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Food } from '../models/food'; // Adjust the path as needed
import { foodclass } from '../models/foodclass'; // Adjust the path as needed


@Injectable({
  providedIn: 'root'
})
export class FoodService {
  private apiUrl = 'http://localhost:8085'; // Update this with your API URL

  constructor(private http: HttpClient) { }

  // Get all restaurants
  getRestaurants(): Observable<Food[]> {
    return this.http.get<Food[]>(`${this.apiUrl}/food`);
  }

  // Method to add a new food item
  addFood(food: Food): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/addFood`, food, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }

  addRestaurant2(uni: foodclass): Observable<any> {
    return this.http.post('http://localhost:8085/addFood', uni, { responseType: 'text' });
  }

  // Method to update an existing food item
  updateFood(food: Food): Observable<string> {
    return this.http.put<string>(`${this.apiUrl}/updateFood`, food, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }
  modifyRestaurant2(uni: foodclass): Observable<any> {
    return this.http.put('http://localhost:8085/updateFood', uni, { responseType: 'text' });
  }

  // Method to delete a food item
  deleteFood(food: Food): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/deleteFood`, {
      body: food,
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }
  // Delete a restaurant
  deleteRestaurant(restaurant: Food): Observable<string> {
    console.log(restaurant);
    return this.http.delete<string>(`${this.apiUrl}/deleteFood`, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: restaurant
    });
  }
}
