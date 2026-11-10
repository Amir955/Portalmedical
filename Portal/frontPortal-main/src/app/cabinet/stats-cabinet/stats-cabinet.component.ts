import { Component, OnInit } from '@angular/core';
import { PaiementService, StatMois, Statistiques } from '../../core/services/paiement.service';

interface Serie {
  key: 'rendezVous' | 'chiffreAffaires' | 'nouveauxPatients';
  title: string;
  icon: string;
  money?: boolean;
}

/** Monthly activity of the practice: appointments, no-shows, revenue, new patients. */
@Component({
  selector: 'app-stats-cabinet',
  templateUrl: './stats-cabinet.component.html',
  styleUrls: ['./stats-cabinet.component.css']
})
export class StatsCabinetComponent implements OnInit {
  readonly series: Serie[] = [
    { key: 'rendezVous', title: 'Rendez-vous', icon: 'event' },
    { key: 'chiffreAffaires', title: 'Chiffre d’affaires', icon: 'payments', money: true },
    { key: 'nouveauxPatients', title: 'Nouveaux patients', icon: 'person_add' }
  ];

  mois = 6;
  stats: Statistiques | null = null;
  loading = true;
  failed = false;

  constructor(private paiementService: PaiementService) {
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.failed = false;
    this.paiementService.statistiques(this.mois).subscribe(
      (stats) => {
        this.stats = stats;
        this.loading = false;
      },
      () => {
        this.loading = false;
        this.failed = true;
      }
    );
  }

  setMois(mois: number): void {
    if (this.mois !== mois) {
      this.mois = mois;
      this.load();
    }
  }

  value(m: StatMois, serie: Serie): number {
    return m[serie.key] || 0;
  }

  max(serie: Serie): number {
    if (!this.stats) {
      return 0;
    }
    return Math.max(0, ...this.stats.mois.map((m) => this.value(m, serie)));
  }

  height(m: StatMois, serie: Serie): number {
    const max = this.max(serie);
    return max > 0 ? Math.max(2, Math.round((this.value(m, serie) / max) * 100)) : 0;
  }

  label(m: StatMois, serie: Serie): string {
    const v = this.value(m, serie);
    return serie.money ? this.compactMoney(v) : String(v);
  }

  format(amount: number): string {
    return PaiementService.format(amount);
  }

  /** Short label above the bars: "1,2 k" instead of "1 200,000 DT". */
  compactMoney(amount: number): string {
    if (amount >= 1000) {
      return (amount / 1000).toLocaleString('fr-FR', { maximumFractionDigits: 1 }) + ' k';
    }
    return amount.toLocaleString('fr-FR', { maximumFractionDigits: 0 });
  }

  percent(rate: number | null): string {
    return rate === null || rate === undefined ? '—' : rate.toLocaleString('fr-FR', { maximumFractionDigits: 1 }) + ' %';
  }

  monthRate(m: StatMois): string {
    const base = m.honores + m.absents;
    return base > 0 ? this.percent((m.absents / base) * 100) : '—';
  }
}
