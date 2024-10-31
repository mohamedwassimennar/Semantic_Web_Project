import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Restaurant } from '../models/restaurant'; // Adjust the path as necessary
import { restclass } from '../models/restclass'; // Adjust the path as necessary


@Injectable({
  providedIn: 'root'
})
export class RestaurantService {
  private apiUrl = 'http://localhost:8085'; // Adjust the URL to your Spring Boot backend
  private baseUrl: string = 'http://localhost:8085'; // Base URL for your API

  constructor(private http: HttpClient) { }

  // Get all restaurants
  getRestaurants(): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(`${this.apiUrl}/restaurant`);
  }


  getSortedRestaurants(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/restaurants/sorted`);
  }
  
  addRestaurant2(uni: restclass): Observable<any> {
    return this.http.post('http://localhost:8085/addRestaurant', uni, { responseType: 'text' });
  }
  
  modifyRestaurant2(uni: restclass): Observable<any> {
    return this.http.put('http://localhost:8085/modifyRestaurant', uni, { responseType: 'text' });
  }

  modifyRestaurant(restaurant: restclass): Observable<any> {
    // Specify the correct content type
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    
    // Send the request with the appropriate headers
    return this.http.put(`${this.baseUrl}/modifyRestaurant`, restaurant, { headers: headers, responseType: 'text' });
  }

  /* Modify an existing restaurant
  modifyRestaurant(restaurant: Restaurant): Observable<string> {
    return this.http.put<string>(`${this.apiUrl}/modifyRestaurant`, restaurant, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    });
  }*/

  // Delete a restaurant
  deleteRestaurant(restaurant: Restaurant): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/deleteRestaurant`, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: restaurant
    });
  }
}
