import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StoreRoutingModule } from './store-routing.module';
import { StoreCatalogComponent } from './store-catalog/store-catalog.component';
import { StoreDetailComponent } from './store-detail/store-detail.component';
import { StoreExamComponent } from './store-exam/store-exam.component';
import { StoreResultComponent } from './store-result/store-result.component';
import { CheckoutComponent } from './checkout/checkout.component';
import { MockExamComponent } from './mock-exam/mock-exam.component';
import { MyEnrollmentsComponent } from './my-enrollments/my-enrollments.component';
import { RoomCheckComponent } from './room-check/room-check.component';
import { ChatAssistantComponent } from './chat-assistant/chat-assistant.component';
import { FeedbackModalComponent } from './feedback-modal/feedback-modal.component';


@NgModule({
    imports: [CommonModule, FormsModule, StoreRoutingModule, StoreCatalogComponent, StoreDetailComponent, StoreExamComponent,
    StoreResultComponent, CheckoutComponent, MockExamComponent,
    MyEnrollmentsComponent, RoomCheckComponent, ChatAssistantComponent,
    FeedbackModalComponent]
})
export class StoreModule {}
