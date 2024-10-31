import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notification } from '../models/notification';
import { NotificationClass } from 'src/app/models/notificationclass';
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8085';
  constructor(private http: HttpClient) { }

  getNotification(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/notification`);
  }

  addNotification(notification: Notification): Observable<NotificationClass> { // Changez le type de retour
    return this.http.post<NotificationClass>(`${this.apiUrl}/addNotification`, notification, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }


  updateNotification(notification: Notification): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/modifyNotification`, notification);
  }



  deleteNotification(notification: Notification): Observable<any> {
    console.log(notification);
    // Envoyer la requête DELETE à l'API
    return this.http.delete<any>(`${this.apiUrl}/deleteNotification`, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: notification,
    });
  }

}
