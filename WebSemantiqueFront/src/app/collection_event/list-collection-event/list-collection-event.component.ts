// components/list-collection-event/list-collection-event.component.ts
import { Component, OnInit } from '@angular/core';
import { EventService } from '../../service/event.service';
import { eventclass } from '../../models/collectionevent';

@Component({
  selector: 'app-list-collection-event',
  templateUrl: './list-collection-event.component.html',
  styleUrls: ['./list-collection-event.component.css']
})
export class ListCollectionEventComponent implements OnInit {
  events: eventclass[] = [];
  selectedEvent: eventclass | null = null;
  newEvent: eventclass = { event: '', date: '' };
  minDate: string; // Propriété pour stocker la date minimale

  constructor(private eventService: EventService) {
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0]; // Date d'aujourd'hui au format YYYY-MM-DD
  }

  ngOnInit(): void {
    this.getEvents();
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

  addEvent(): void {
    this.eventService.addEvent(this.newEvent).subscribe(response => {
      console.log(response);
      this.getEvents(); // Rafraîchir la liste après ajout
      this.newEvent = { event: '', date: '' }; // Réinitialiser le formulaire
    }, error => {
      console.error('Error adding event:', error);
    });
  }

  deleteEvent(eventUri: string): void {
    this.eventService.deleteEvent(eventUri).subscribe(response => {
      console.log(response);
      this.getEvents(); // Rafraîchir la liste après suppression
    }, error => {
      console.error('Error deleting event:', error);
    });
  }

  editEvent(event: eventclass): void {
    this.selectedEvent = { ...event }; // Cloner l'objet pour éviter la mutation directe
  }

  updateEvent(): void {
    if (this.selectedEvent) {
      this.eventService.updateEvent(this.selectedEvent).subscribe(response => {
        console.log(response);
        this.getEvents(); // Rafraîchir la liste après mise à jour
        this.selectedEvent = null; // Cacher le formulaire d'édition
      }, error => {
        console.error('Error updating event:', error);
      });
    }
  }
}
