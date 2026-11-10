import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { MODES_PAIEMENT, Paiement, PaiementService, ResumePaiements } from '../services/paiement.service';

type Periode = 'jour' | 'semaine' | 'mois' | 'perso';

/** Payments received over a period, with receipts and CSV export. */
@Component({
  selector: 'app-caisse',
  templateUrl: './caisse.component.html',
  styleUrls: ['./caisse.component.css']
})
export class CaisseComponent implements OnInit {
  readonly modes = MODES_PAIEMENT;
  readonly periodes: { key: Periode; label: string }[] = [
    { key: 'jour', label: 'Aujourd’hui' },
    { key: 'semaine', label: 'Cette semaine' },
    { key: 'mois', label: 'Ce mois' },
    { key: 'perso', label: 'Personnalisée' }
  ];

  periode: Periode = 'jour';
  du = '';
  au = '';
  paiements: Paiement[] = [];
  resume: ResumePaiements = { total: 0, nombre: 0, parMode: {} };
  loading = true;
  confirmDelete: Paiement | null = null;
  deleting = false;

  constructor(private paiementService: PaiementService, private toastr: ToastrService) {
  }

  ngOnInit(): void {
    this.setPeriode('jour');
  }

  setPeriode(periode: Periode): void {
    this.periode = periode;
    const today = new Date();
    if (periode === 'jour') {
      this.du = this.au = this.dateValue(today);
    } else if (periode === 'semaine') {
      const monday = new Date(today);
      monday.setDate(today.getDate() - ((today.getDay() + 6) % 7));
      const sunday = new Date(monday);
      sunday.setDate(monday.getDate() + 6);
      this.du = this.dateValue(monday);
      this.au = this.dateValue(sunday);
    } else if (periode === 'mois') {
      this.du = this.dateValue(new Date(today.getFullYear(), today.getMonth(), 1));
      this.au = this.dateValue(new Date(today.getFullYear(), today.getMonth() + 1, 0));
    }
    this.load();
  }

  onCustomChange(): void {
    this.periode = 'perso';
    if (this.du && this.au && this.du > this.au) {
      const swap = this.du;
      this.du = this.au;
      this.au = swap;
    }
    this.load();
  }

  load(): void {
    if (!this.du || !this.au) {
      return;
    }
    this.loading = true;
    this.paiementService.list(this.du, this.au).subscribe(
      (list) => {
        this.paiements = list || [];
        this.loading = false;
      },
      () => {
        this.paiements = [];
        this.loading = false;
        this.toastr.error('Les paiements n’ont pas pu être chargés.', 'Erreur');
      }
    );
    this.paiementService.resume(this.du, this.au).subscribe(
      (resume) => this.resume = resume || { total: 0, nombre: 0, parMode: {} },
      () => this.resume = { total: 0, nombre: 0, parMode: {} }
    );
  }

  format(amount: number): string {
    return PaiementService.format(amount);
  }

  modeLabel(mode: string): string {
    return PaiementService.modeLabel(mode);
  }

  modeIcon(mode: string): string {
    const found = this.modes.find((m) => m.value === mode);
    return found ? found.icon : 'payments';
  }

  share(mode: string): number {
    const value = this.resume.parMode[mode] || 0;
    return this.resume.total > 0 ? Math.round((value / this.resume.total) * 100) : 0;
  }

  receipt(p: Paiement): void {
    const tab = window.open('', '_blank');
    this.paiementService.recu(p.id).subscribe(
      (blob) => PaiementService.openPdf(tab, blob),
      () => {
        if (tab) {
          tab.close();
        }
        this.toastr.error('Le reçu n’a pas pu être ouvert.', 'Erreur');
      }
    );
  }

  askDelete(p: Paiement): void {
    this.confirmDelete = p;
  }

  doDelete(): void {
    const p = this.confirmDelete;
    if (!p || this.deleting) {
      return;
    }
    this.deleting = true;
    this.paiementService.delete(p.id).subscribe(
      () => {
        this.deleting = false;
        this.confirmDelete = null;
        this.toastr.success(`Le paiement ${p.numeroRecu} a été supprimé.`, 'Paiement supprimé');
        this.load();
      },
      () => {
        this.deleting = false;
        this.toastr.error('Le paiement n’a pas pu être supprimé.', 'Erreur');
      }
    );
  }

  exportCsv(): void {
    const esc = (value: any) => {
      const text = value === null || value === undefined ? '' : String(value);
      return /[;"\n]/.test(text) ? '"' + text.replace(/"/g, '""') + '"' : text;
    };
    const rows = [['N° reçu', 'Date', 'Patient', 'Mode', 'Montant (DT)', 'Note', 'Encaissé par']];
    this.paiements.forEach((p) => rows.push([
      p.numeroRecu,
      new Date(p.datePaiement).toLocaleString('fr-FR'),
      p.patientNom || '',
      this.modeLabel(p.mode),
      p.montant.toFixed(3).replace('.', ','),
      p.note || '',
      p.encaissePar || ''
    ]));
    const csv = '﻿' + rows.map((r) => r.map(esc).join(';')).join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = `caisse_${this.du}_${this.au}.csv`;
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 10000);
  }

  private dateValue(date: Date): string {
    const pad = (n: number) => (n < 10 ? '0' + n : String(n));
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
  }
}
