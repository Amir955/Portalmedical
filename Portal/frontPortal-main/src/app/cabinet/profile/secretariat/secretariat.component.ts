import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AccountService, TeamMember } from '../../../core/services/account.service';

/**
 * "Mon secrétariat" (doctor's profile): the doctor creates the accounts of their
 * secretaries, who receive an e-mail to choose their password.
 */
@Component({
  selector: 'app-secretariat',
  templateUrl: './secretariat.component.html',
  styleUrls: ['./secretariat.component.css']
})
export class SecretariatComponent implements OnInit {
  members: TeamMember[] = [];
  loading = true;
  adding = false;
  saving = false;
  busyId: number | null = null;
  toRemove: TeamMember | null = null;
  /** Shown inside the add form until it is edited. */
  formError = '';

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private accountService: AccountService,
              private toastr: ToastrService) {
    this.form = this.fb.group({
      prenom: ['', [Validators.required, Validators.maxLength(60)]],
      nom: ['', [Validators.required, Validators.maxLength(60)]],
      email: ['', [Validators.required, Validators.email]],
      numtelephone: ['', Validators.pattern(/^\+?[0-9 ()-]{8,20}$/)]
    });
    this.form.valueChanges.subscribe(() => this.formError = '');
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.accountService.getEquipe().subscribe(
      (members) => {
        this.members = members || [];
        this.loading = false;
      },
      () => {
        this.loading = false;
        this.toastr.error('Votre secrétariat n’a pas pu être chargé.', 'Erreur');
      }
    );
  }

  initials(member: TeamMember): string {
    return ((member.prenom || '').charAt(0) + (member.nom || '').charAt(0)).toUpperCase() || 'S';
  }

  showError(name: string): boolean {
    const control = this.form.get(name);
    return control.invalid && (control.dirty || control.touched);
  }

  openForm(): void {
    this.adding = true;
    this.formError = '';
    this.form.reset({ prenom: '', nom: '', email: '', numtelephone: '' });
  }

  cancelForm(): void {
    this.adding = false;
  }

  add(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.value;
    this.saving = true;
    this.accountService.addSecretaire({
      prenom: value.prenom.trim(),
      nom: value.nom.trim(),
      email: value.email.trim(),
      numtelephone: (value.numtelephone || '').trim() || undefined
    }).subscribe(
      (member) => {
        this.saving = false;
        this.adding = false;
        this.members = [...this.members, member];
        if (member.invitationSent) {
          this.toastr.success(
            `${member.prenom} va recevoir un e-mail pour choisir son mot de passe.`, 'Secrétaire ajoutée');
        } else {
          this.toastr.warning(
            'Le compte est créé, mais l’e-mail d’activation n’a pas pu partir. Réessayez avec « Renvoyer l’invitation ».',
            'E-mail non envoyé', { timeOut: 9000 });
        }
      },
      (error) => {
        this.saving = false;
        if (error && error.status === 409) {
          this.form.get('email').setErrors({ taken: true });
          this.formError = 'Cette adresse e-mail est déjà utilisée par un autre compte.';
        } else {
          this.formError = 'La secrétaire n’a pas pu être ajoutée. Réessayez dans un instant.';
        }
      }
    );
  }

  resend(member: TeamMember): void {
    this.busyId = member.id;
    this.accountService.resendInvitation(member.id).subscribe(
      (result) => {
        this.busyId = null;
        if (result && result.invitationSent) {
          this.toastr.success(`Un nouveau lien a été envoyé à ${member.email}.`, 'Invitation renvoyée');
        } else {
          this.toastr.error('L’e-mail n’a pas pu être envoyé. Vérifiez la configuration de la messagerie.', 'E-mail non envoyé');
        }
      },
      () => {
        this.busyId = null;
        this.toastr.error('L’invitation n’a pas pu être renvoyée.', 'Erreur');
      }
    );
  }

  askRemove(member: TeamMember): void {
    this.toRemove = member;
  }

  confirmRemove(): void {
    const member = this.toRemove;
    if (!member) {
      return;
    }
    this.toRemove = null;
    this.busyId = member.id;
    this.accountService.removeSecretaire(member.id).subscribe(
      () => {
        this.busyId = null;
        this.members = this.members.filter((item) => item.id !== member.id);
        this.toastr.success(`Le compte de ${member.prenom} ${member.nom} a été supprimé.`, 'Secrétaire retirée');
      },
      () => {
        this.busyId = null;
        this.toastr.error('Le compte n’a pas pu être supprimé.', 'Erreur');
      }
    );
  }
}
