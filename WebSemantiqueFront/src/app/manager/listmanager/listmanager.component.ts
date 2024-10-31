import { Component, OnInit } from '@angular/core';
import { ManagerService } from '../../service/manager.service';
import { Manager } from '../../models/manager';
import { ManagerClass } from '../../models/managerclass';
import { Router } from '@angular/router';

@Component({
  selector: 'app-listmanager',
  templateUrl: './listmanager.component.html',
  styleUrls: ['./listmanager.component.css']
})
export class ListmanagerComponent implements OnInit {
  managers: { manager: string; name: string; contact: string; }[] = [];
  loading: boolean = true;
  listManagers: Manager[] = [];
  newManager: ManagerClass = { manager: '', name: '', contact: '' };
  showForm = false;

  constructor(private managerService: ManagerService, private router: Router) { }

  ngOnInit(): void {
    this.getManagers();
  }

  getManagers() {
    this.managerService.getManagers().subscribe({
      next: (managers: Manager[]) => {
        this.listManagers = managers;
        this.loading = false;
        console.log(this.listManagers);
      },
      error: error => {
        console.error('Error fetching managers:', error);
        this.loading = false;
      }
    });
  }

  deleteManager(manager: any) {
    this.managerService.deleteManager(manager).subscribe({
      next: () => this.getManagers(),
      error: error => { this.getManagers()}
    });
  }

  addManager(manager: ManagerClass) {
    this.managerService.addManager(manager).subscribe({
      next: (addedManager) => {
        this.listManagers.push(addedManager as unknown as ManagerClass);
        this.showForm = false;
        this.getManagers();
        this.router.navigate(['/listmanager']);
      },
      error: () => this.getManagers(),
    });
  }

  updateManager(manager: any) {
    this.managerService.updateManager(manager).subscribe({
      next: () => {alert('Updated manager successfully'); this.getManagers()},
      error: error => { this.getManagers()}
    });
  }
}
