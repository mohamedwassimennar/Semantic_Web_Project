import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { directorclass } from '../models/directorclass';

@Injectable({
  providedIn: 'root'
})
export class DirectorService {
  private apiUrl = 'http://localhost:8085'; // Base URL de l'API

  constructor(private http: HttpClient) { }

  addDirector(director: directorclass): Observable<any> {
    return this.http.post(`${this.apiUrl}/addDirector`, director); // Adjust endpoint as needed
  }

  getDirectors(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/director`);
  }

  deleteDirector(directorUri: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/deleteDirector`, { body: { director: directorUri } });
  }

  updateDirector(director: directorclass): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/updateDirector`, director);
  }
}
