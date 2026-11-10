 package com.example.PortalMedical.controllers;

 import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.PortalMedical.Repositories.RendezVousRepository;
import com.example.PortalMedical.Repositories.UserRepository;
import com.example.PortalMedical.Services.HorairesRdvServices;
import com.example.PortalMedical.enteties.HorairesRdvEntity;
import com.example.PortalMedical.enteties.RendezVousEntity;
import com.example.PortalMedical.enteties.UserEntity;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/HorairesRdv")
public class HoraireRdvControllers {
@Autowired
HorairesRdvServices horairerdvservices;
@Autowired
RendezVousRepository rdvrepository;

/*@PostMapping("/ajouterhoraire")
public HorairesRdvEntity ajouterhoraire(@RequestBody HorairesRdvEntity horaire) {
	RendezVousEntity rdv = rdvrepository.findById(HorairesRdvEntity.getRendezVousEntities().getId()).get();
	horaire.setRendezVousEntities(rdv);
	HorairesRdvEntity e3 = horairerdvservices.
(horaire);
	return e3;
}


@GetMapping()
public List<HorairesRdvEntity> getListhoraire() {
	return horairerdvservices.getAllHorairesRdvEntity();
}


@DeleteMapping("/{id}")
public void deletehoraireById(@PathVariable(name = "id") String id) {
	horairerdvservices.deletehoraireById(id);
}


@PutMapping
public HorairesRdvEntity updatHorairesRdvEntity(@RequestBody HorairesRdvEntity horaire) {
	return horairerdvservices.updateHorairesRdvEntity(horaire);
}*/
}
