import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Inventory, InventoryResponse } from '../models/inventory';
import { map } from 'rxjs/operators';
import { Food } from '../models/food';
import { NewInventoryInput } from '../inventory/listinventory/listinventory.component';

interface ApiResponse {
  results: Inventory[];
}

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private apiUrl = 'http://localhost:8085'; // Update with your API URL

  constructor(private http: HttpClient) { }

  // Get all inventory items
  getInventories(): Observable<Inventory[]> {
    return this.http.get<ApiResponse>(`${this.apiUrl}/inventory`).pipe(
      map(response => response.results || [])
    );
  }

  // Add a new inventory item
  addInventory(food: Food, inventory: NewInventoryInput): Observable<string> {
    const inventoryWithFood = {
      ...inventory,
      food: food  // This will set the food reference
    };
    console.log(inventoryWithFood);
    return this.http.post<string>(`${this.apiUrl}/inventory`, inventoryWithFood, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }

  // Update an existing inventory item
  updateInventory(inventory: Inventory, newQuantity: number): Observable<string> {
    const updatedInventory = {
      ...inventory,
      currentQuantity: newQuantity
    };
    updatedInventory.inventory = updatedInventory.inventory.replace('http://rescuefood.org/ontology#Inventory', '');
    console.log(updatedInventory.inventory.replace('http://rescuefood.org/ontology#Inventory', ''));
    return this.http.put<string>(`${this.apiUrl}/inventory/Inventory${updatedInventory.inventory}`, updatedInventory, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }

  // Delete an inventory item
  deleteInventory(inventory: Inventory): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/inventory/Inventory${inventory.inventory.replace('http://rescuefood.org/ontology#Inventory', '')}`, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }
} 