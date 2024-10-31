import { Component, OnInit } from '@angular/core';
import { Notification } from 'src/app/models/notification';
import { NotificationClass } from 'src/app/models/notificationclass';
import { NotificationService } from 'src/app/service/notification.service';

@Component({
  selector: 'app-liste-notification',
  templateUrl: './liste-notification.component.html',
  styleUrls: ['./liste-notification.component.css']
})
export class ListeNotificationComponent implements OnInit {

  notifications: Notification[] = [];
  newNotification: Notification = { notification: '', recipient: '', notificationType: '' };
  showForm = false;
  selectedNotification: Notification | null = null;
  showModal: boolean = false;

  constructor(private notificationService: NotificationService) { }

  ngOnInit(): void {
    this.getNotifications();
  }

  
  getNotifications() {
    this.notificationService.getNotification().subscribe(
      (data: any) => {
        const bindings = data.results.bindings;

        // Vérifier si bindings est un tableau
        if (Array.isArray(bindings)) {
          this.notifications = bindings.map((feedback: any) => ({
            notification: feedback.notification.value, 
            recipient: feedback.recipient.value,     
            notificationType: feedback.notificationType.value, 
          }));
        } else {
          console.error('Expected an array but got:', bindings);
        }
      },
      error => {
        console.error('Error fetching notifications:', error);
      }
    );
  }





  deleteNotification(notification: Notification) {
    this.notificationService.deleteNotification(notification).subscribe(
      response => {
        console.log('Notification deleted:', response);
        this.getNotifications(); // Rafraîchir la liste
      },
      error => {
        console.error('Error deleting notification:', error);
        this.getNotifications();
      }
    );
  }

  addNewNotification(notification: NotificationClass) {
    this.notificationService.addNotification(notification).subscribe(
      (addedNotification) => {
        this.notifications.push(addedNotification);
        this.showForm = false;
        this.getNotifications();
      },
      error => console.log(error)
    );
  }

  submitNotification(notification: NotificationClass) {
    this.addNewNotification(notification);
    this.newNotification = { notification: '', recipient: '', notificationType: '' };
  }

  openModal(notification: Notification) {
    this.selectedNotification = { ...notification };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.selectedNotification = null;
  }

  updateNotification() {
    if (this.selectedNotification) {
      this.notificationService.updateNotification(this.selectedNotification).subscribe(
        response => {
          console.log('Notification updated:', response);
          this.getNotifications();
          this.closeModal();
        },
        error => console.error('Error updating notification:', error)
      );
    }
  }
}