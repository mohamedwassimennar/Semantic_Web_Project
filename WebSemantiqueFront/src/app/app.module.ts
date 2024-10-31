import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { ListrestaurantComponent } from './restaurant/listrestaurant/listrestaurant.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ModifrestaurantComponent } from './restaurant/modifrestaurant/modifrestaurant.component';
import { ListfoodComponent } from './food/listfood/listfood.component';
import { ListefeedbackComponent } from './feedback/listefeedback/listefeedback.component';
import { ListDirectorComponent } from './director/list-director/list-director.component';
import { ListCollectionEventComponent } from './collection_event/list-collection-event/list-collection-event.component';
import { ListeNotificationComponent } from './Notifivation/listenotification/liste-notification/liste-notification.component';
import { HomeComponent } from './home/home.component';
import { ListmanagerComponent } from './manager/listmanager/listmanager.component';
import { ListinventoryComponent } from './inventory/listinventory/listinventory.component';

@NgModule({
  declarations: [
    AppComponent,
    ListrestaurantComponent,
    ModifrestaurantComponent,
    ListfoodComponent,
    ListefeedbackComponent,
    ListDirectorComponent,
    ListCollectionEventComponent,
    ListeNotificationComponent,
    HomeComponent,
    ListmanagerComponent,
    ListinventoryComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule { }
