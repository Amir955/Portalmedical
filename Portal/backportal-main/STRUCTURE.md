# Organisation du backend

Package racine : `com.example.portalmedical` (classe principale `PortalMedicalApplication`).

Le code est rangé **par fonctionnalité**. Chaque fonctionnalité a les mêmes sous-dossiers :

| Dossier          | Contenu                                                      |
|------------------|--------------------------------------------------------------|
| `controller/`    | points d'entrée REST (`@RestController`)                     |
| `service/`       | interfaces des services (et services sans interface)         |
| `service/impl/`  | implémentations (`@Service`)                                 |
| `repository/`    | accès aux données (Spring Data JPA)                          |
| `entity/`        | entités JPA et énumérations liées                            |
| `dto/`           | objets échangés avec le front                                |

```
com.example.portalmedical
├── PortalMedicalApplication.java
├── config/          WebConfig, initialisation de la base, contexte Spring
├── security/jwt/    filtres JWT, configuration Spring Security
├── common/          partagé : mail (gabarits), practice (périmètre du cabinet),
│                    web (limite des demandes publiques), enums
├── user/            comptes, connexion, mot de passe, secrétaires
├── patient/         patients, dossier médical, documents
├── appointment/     rendez-vous, demandes, annulations, rappels (job/)
├── planning/        horaires du cabinet, congés, créneaux
├── payment/         paiements et reçus
├── statistics/      statistiques du cabinet
└── consultation/    fiches patients, ordonnances, certificats, lettres, PDF
    ├── fiche/  ordonnance/  certificat/  lettre/  pdf/
    └── service/     contexte de consultation
```

Ajouter une fonctionnalité : créer un dossier à ce niveau avec les mêmes sous-dossiers.
