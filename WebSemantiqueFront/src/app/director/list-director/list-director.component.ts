import { Component, OnInit } from '@angular/core';
import { DirectorService } from '../../service/director.service';
import { directorclass } from '../../models/directorclass';

@Component({
  selector: 'app-list-director',
  templateUrl: './list-director.component.html',
  styleUrls: ['./list-director.component.css']
})
export class ListDirectorComponent implements OnInit {
  directors: directorclass[] = [];
  selectedDirector: directorclass | null = null;
  newDirector: directorclass = { name: '', contact: '', director: '' };

  constructor(private directorService: DirectorService) {}

  ngOnInit(): void {
    this.getDirectors();
  }

  getDirectors(): void {
    this.directorService.getDirectors().subscribe(response => {
      this.directors = response.results.bindings.map((binding: any) => ({
        contact: binding.contact.value,
        name: binding.name.value,
        director: binding.director.value,
      }));
    }, error => {
      console.error('Error fetching directors:', error);
    });
  }

  addDirector(): void {
    this.directorService.addDirector(this.newDirector).subscribe(response => {
      console.log(response);
      this.getDirectors(); // Rafraîchir la liste après ajout
      this.newDirector = { name: '', contact: '', director: '' }; // Réinitialiser le formulaire
    }, error => {
      console.error('Error adding director:', error);
    });
  }

  deleteDirector(directorUri: string): void {
    this.directorService.deleteDirector(directorUri).subscribe(response => {
      console.log(response);
      this.getDirectors(); // Rafraîchir la liste après suppression
    }, error => {
      console.error('Error deleting director:', error);
    });
  }

  editDirector(director: directorclass): void {
    this.selectedDirector = { ...director }; // Cloner l'objet pour éviter la mutation directe
  }

  updateDirector(): void {
    if (this.selectedDirector) {
      this.directorService.updateDirector(this.selectedDirector).subscribe(response => {
        console.log(response);
        this.getDirectors(); // Rafraîchir la liste après mise à jour
        this.selectedDirector = null; // Cacher le formulaire d'édition
      }, error => {
        console.error('Error updating director:', error);
      });
    }
  }
}
