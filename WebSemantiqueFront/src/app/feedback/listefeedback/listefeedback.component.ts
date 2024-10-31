import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Feedback } from 'src/app/models/feedback';
import { FeedbackClass } from 'src/app/models/feedbackclass';
import { FeedbackService } from 'src/app/service/feedback.service';

@Component({
  selector: 'app-listefeedback',
  templateUrl: './listefeedback.component.html',
  styleUrls: ['./listefeedback.component.css']
})
export class ListefeedbackComponent implements OnInit {

  feedbacks: { feedback: string; rating: number; comment: string; }[] = [];
  loading: boolean = true; // État de chargement
  result = 0;
  listUnis: Feedback[] = [];

  newFeedback: FeedbackClass = { feedback: '', rating: 0, comment: '' };
  showForm = false;
  selectedFeedback: FeedbackClass | null = null;
  showModal: boolean = false;
  commentFilter: string = '';

  constructor(private feedbackService: FeedbackService, private router: Router) { }

  ngOnInit(): void {
    this.getFeedbacks(); // Récupérer les feedbacks à l'initialisation
  }

  // Méthode pour récupérer la liste des feedbacks
  getFeedbacks() {
    console.log('Fetching feedbacks...');
    this.feedbackService.getFeedbacks().subscribe(
      (data: any) => {
        console.log('Data received:', data); // Afficher les données reçues
        const bindings = data.results.bindings; // Récupérer le tableau de feedbacks

        // Vérifier si bindings est un tableau
        if (Array.isArray(bindings)) {
          this.feedbacks = bindings.map((feedback: any) => ({
            feedback: feedback.feedback.value,
            rating: feedback.rating.value, // Accéder à la valeur pour la note
            comment: feedback.comment.value, // Accéder à la valeur pour le commentaire
          }));
          console.log('Feedbacks:', this.feedbacks); // Afficher les feedbacks
        } else {
          console.error('Expected an array but got:', bindings);
        }
      },
      error => {
        console.error('Error fetching feedbacks:', error);
      }
    );
  }

  applyFilter() {
    const filterCriteria = {
      comment: this.commentFilter
    };

    console.log('Applying filter with criteria:', filterCriteria);

    this.feedbackService.filterFeedbacks(filterCriteria).subscribe(
      (data: any) => {
        console.log('Filtered feedbacks received:', data);

        // Accéder directement au tableau de feedbacks
        this.feedbacks = data; // data est déjà un tableau d'objets feedback

        console.log('Number of feedbacks after filtering:', this.feedbacks.length);
      },
      error => {
        console.error('Error filtering feedbacks:', error);
      }
    );
  }






  // Méthode pour supprimer un feedback
  deleteFeedback(feedback: any) {
    console.log(feedback);
    this.feedbackService.deleteFeedback(feedback).subscribe(

      response => {
        // Vérifier si la réponse est au format JSON
        if (response && response.feedback) {
          console.log('Feedback deleted:', response.feedback);
        } else {
          // Traitement de la chaîne de texte
          console.log('Response:', response);
        }
        // Rafraîchir la liste après la suppression
        this.getFeedbacks();
      },
      error => {
        console.error('Error deleting feedback:', error);
        this.getFeedbacks();
      }
    );
  }



  // Méthode pour naviguer vers le composant d'ajout de feedback
  addFeedback() {
    this.router.navigate(['addfeedback']); // Ajustez le chemin si nécessaire
  }

  // Méthode pour ajouter un nouveau feedback
  addNewFeedback(feedback: FeedbackClass) {
    this.feedbackService.addFeedback(feedback).subscribe({
      next: (addedFeedback) => {
        this.listUnis.push(addedFeedback as FeedbackClass);
        this.showForm = false;
        this.getFeedbacks(); // Appeler getFeedbacks pour rafraîchir la liste
        this.router.navigate(['/listfeedback']); // Rediriger vers la liste des feedbacks
      },
      error: (err) => console.log(err),
    });
  }

  // Méthode pour naviguer vers le composant de mise à jour de feedback
  navigateToUpdateFeedback(feedback: any) {
    this.router.navigate(['update', feedback.feedback]); // Passer l'ID ou l'URI du feedback
  }
  submitFeedback(feedback: FeedbackClass) {
    this.addNewFeedback(feedback); // Appeler la méthode pour ajouter le feedback
    this.newFeedback = { feedback: '', rating: 0, comment: '' }; // Réinitialiser le formulaire
  }





  // Ouvre le modal pour modifier le feedback sélectionné
  openModal(feedback: Feedback) {
    this.selectedFeedback = { ...feedback }; // Clonage pour éviter les modifications directes
    this.showModal = true;
  }

  // Ferme le modal
  closeModal() {
    this.showModal = false;
    this.selectedFeedback = null;
  }

  // Met à jour le feedback sélectionné
  updateFeedback(): void {
    if (this.selectedFeedback) {
      this.feedbackService.updateFeedback(this.selectedFeedback).subscribe(
        (response) => {
          console.log('Feedback updated:', response);
          this.getFeedbacks(); // Rafraîchir la liste des feedbacks
          this.closeModal(); // Fermer le modal après mise à jour
        },
        (error) => {
          console.error('Error updating feedback:', error);
        }
      );
    }
  }

  // Méthode pour annuler la modification
  cancelUpdate() {
    this.closeModal(); // Fermer le modal et annuler
  }





}