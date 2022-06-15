import { Component, OnInit } from '@angular/core';
import { DayService, WeekService, WorkWeekService, MonthService, AgendaService } from '@syncfusion/ej2-angular-schedule';
import { from } from 'rxjs';
@Component({
  selector: 'app-calendrier',
  providers: [DayService, WeekService, WorkWeekService, MonthService, AgendaService],

  templateUrl: './calendrier.component.html',
  styleUrls: ['./calendrier.component.css']
})
export class CalendrierComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
