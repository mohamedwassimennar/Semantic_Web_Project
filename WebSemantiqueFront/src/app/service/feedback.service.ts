import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Feedback } from '../models/feedback';
import { FeedbackClass } from 'src/app/models/feedbackclass';

@Injectable({
  providedIn: 'root'
})
export class FeedbackService {
  private apiUrl = 'http://localhost:8085'; // Mettez à jour avec l'URL de votre API

  constructor(private http: HttpClient) { }

  // Méthode pour récupérer tous les feedbacks
  getFeedbacks(): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(`${this.apiUrl}/feedback`);
  }

  // Méthode pour ajouter un nouveau feedback
  addFeedback(feedback: Feedback): Observable<FeedbackClass> { // Changez le type de retour
    return this.http.post<FeedbackClass>(`${this.apiUrl}/addFeedback`, feedback, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }


  updateFeedback(feedback: Feedback): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/modifyFeedback`, feedback);
  }



  deleteFeedback(feedback: Feedback): Observable<any> {
    console.log(feedback);
    // Envoyer la requête DELETE à l'API
    return this.http.delete<any>(`${this.apiUrl}/deleteFeedback`, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: feedback,
    });
  }
  filterFeedbacks(filterCriteria: any): Observable<Feedback[]> {
    return this.http.post<Feedback[]>(`${this.apiUrl}/filterFeedback`, filterCriteria, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    });
  }
}



