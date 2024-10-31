// src/app/service/event.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { eventclass } from '../models/collectionevent';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private apiUrl = 'http://localhost:8085'; // Base URL de l'API

  constructor(private http: HttpClient) { }

  getEvents(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/collectionevent`); // Adjust endpoint as needed
  }

  addEvent(event: eventclass): Observable<any> {
    return this.http.post(`${this.apiUrl}/addCollectionEvent`, event); // Adjust endpoint as needed
  }

  deleteEvent(eventUri: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/deleteCollectionEvent`, { body: { event: eventUri } });
  }

  updateEvent(event: eventclass): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/updateCollectionEvent`, event);
  }
}
