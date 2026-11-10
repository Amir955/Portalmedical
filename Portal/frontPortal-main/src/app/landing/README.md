# Page d'accueil (`/`)

Page publique : les patients prennent rendez-vous, les professionnels se connectent ou créent leur cabinet.

```
landing/
├── landing.component.*      page : charge les médecins une fois et assemble les sections
├── landing.data.ts          textes (étapes, fonctionnalités, FAQ) et petites fonctions partagées
├── landing-shared.css       styles communs aux sections (boutons, titres)
└── sections/
    ├── landing-header/      barre du haut : logo, liens, « Espace professionnel »
    ├── landing-hero/        accroche patient + recherche de médecin
    ├── landing-how-it-works/  les 3 étapes d'une prise de rendez-vous
    ├── landing-doctors/     médecins et leur prochain créneau libre
    ├── landing-pro/         « Vous êtes médecin ? » + « Créer mon cabinet »
    ├── landing-faq/         questions fréquentes
    └── landing-footer/      pied de page
```

La recherche et « Réserver » ouvrent `/demandeRDV` avec des paramètres :
`q`, `specialite`, `ville`, `medecin` (id) et `date` (aaaa-mm-jj).
