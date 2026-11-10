import { Component, OnInit } from '@angular/core';
import { RendezVousEntity } from 'src/models/RendezVous';
import { RendezVousService } from '../services/rendez-vous.service';

@Component({
  selector: 'app-rendez-vous',
  templateUrl: './rendez-vous.component.html',
  styleUrls: ['./rendez-vous.component.css']
})
export class RendezVousComponent implements OnInit {
  rv:RendezVousEntity 
  constructor(   
    private RendezVousService: RendezVousService
    ) { }

  ngOnInit(): void {
  }

}
