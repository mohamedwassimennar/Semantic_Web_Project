// home.component.ts
import { Component, OnInit } from '@angular/core';
import { EventService } from '../service/event.service'; // Service pour les événements
import { DirectorService } from '../service/director.service'; // Service pour les directeurs
import { eventclass } from '../models/collectionevent'; // Modèle pour les événements
import { directorclass } from '../models/directorclass'; // Modèle pour les directeurs

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  events: eventclass[] = []; // Liste des événements
  directors: directorclass[] = []; // Liste des directeurs

  constructor(
    private eventService: EventService, 
    private directorService: DirectorService
  ) {}

  ngOnInit(): void {
    this.getEvents(); // Récupérer les événements
    this.getDirectors(); // Récupérer les directeurs
  }

  getEvents(): void {
    this.eventService.getEvents().subscribe(response => {
      this.events = response.results.bindings.map((binding: any) => ({
        event: binding.event.value,
        date: binding.date.value,
      }));
    }, error => {
      console.error('Error fetching events:', error);
    });
  }

  getDirectors(): void {
    this.directorService.getDirectors().subscribe(response => {
      this.directors = response.results.bindings.map((binding: any) => ({
        name: binding.name.value,
        contact: binding.contact.value,
      }));
    }, error => {
      console.error('Error fetching directors:', error);
    });
  }
}
