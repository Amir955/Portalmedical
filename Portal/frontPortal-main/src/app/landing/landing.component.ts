import { Component, OnInit } from '@angular/core';
import { UserEntity } from '../../models/userEntity';
import { AccountService } from '../services/account.service';

/**
 * Public home page. Each section is its own component in ./sections;
 * the doctors are loaded once here and shared with the search and availability sections.
 */
@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit {
  doctors: UserEntity[] = [];

  constructor(private accountService: AccountService) {
  }

  ngOnInit(): void {
    this.accountService.getuserByRole('Medecin').subscribe(
      (doctors) => this.doctors = doctors || [],
      () => this.doctors = []
    );
  }
}
