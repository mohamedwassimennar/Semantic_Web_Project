import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ManagerResponse, Manager } from '../models/manager';
import { ManagerClass } from '../models/managerclass';

interface ApiResponse {
  results: Manager[];
}

@Injectable({
  providedIn: 'root'
})

export class ManagerService {
  private apiUrl = 'http://localhost:8085'; // Update with your API URL

  constructor(private http: HttpClient) { }

  // Get all managers
  getManagers(): Observable<Manager[]> {
    return this.http.get<ApiResponse>(`${this.apiUrl}/manager`).pipe(
      map(response => response.results || [])
    );
  }

  // Add a new manager
  addManager(manager: Manager): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/manager`, manager, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }

  // Update an existing manager
  updateManager(manager: Manager): Observable<string> {
    const newName = prompt('Enter new name:', manager.name);
    const newContact = prompt('Enter new contact:', manager.contact);

    const updatedManager = {
      ...manager,
      name: newName,
      contact: newContact
    }
    return this.http.put<string>(`${this.apiUrl}/manager/Manager${manager.manager.replace('http://rescuefood.org/ontology#Manager', '')}`, updatedManager, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }

  // Delete a manager
  deleteManager(manager: Manager): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/manager/Manager${manager.manager.replace('http://rescuefood.org/ontology#Manager', '')}`, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }
} 