package com.example.PortalMedical.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.PortalMedical.Repositories.CertificatsAptitudeRepository;
import com.example.PortalMedical.Repositories.CertificatsMedicalRepository;
import com.example.PortalMedical.Repositories.FichespatientRepository;
import com.example.PortalMedical.Repositories.LettreRepository;
import com.example.PortalMedical.Repositories.OrdonnanceRepository;
import com.example.PortalMedical.Repositories.PatientDocumentRepository;
import com.example.PortalMedical.Repositories.PatientDossierRepository;
import com.example.PortalMedical.Services.ConsultationContextService;
import com.example.PortalMedical.enteties.PatientDocumentEntity;
import com.example.PortalMedical.enteties.PatientDossierEntity;
import com.example.PortalMedical.enteties.UserEntity;

/**
 * The medical side of a patient's file (doctors only, as everything under /consultation):
 * medical summary, history of the doctor's records, and attached documents.
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/consultation/patients/{patientId}")
public class DossierPatientController {

	private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;
	private static final List<String> ALLOWED_TYPES = Arrays.asList(
			"application/pdf", "image/jpeg", "image/png", "image/gif", "image/webp");
	private static final List<String> CATEGORIES = Arrays.asList("analyse", "imagerie", "courrier", "ordonnance", "autre");

	@Autowired
	private ConsultationContextService contextService;
	@Autowired
	private PatientDossierRepository dossierRepository;
	@Autowired
	private PatientDocumentRepository documentRepository;
	@Autowired
	private FichespatientRepository ficheRepository;
	@Autowired
	private OrdonnanceRepository ordonnanceRepository;
	@Autowired
	private CertificatsMedicalRepository certificatMedicalRepository;
	@Autowired
	private CertificatsAptitudeRepository certificatAptitudeRepository;
	@Autowired
	private LettreRepository lettreRepository;

	// ------------------------------------------------------------------ medical summary

	@GetMapping("/dossier")
	public PatientDossierEntity dossier(@PathVariable long patientId) {
		contextService.getPatient(patientId);
		return dossierRepository.findById(patientId).orElseGet(() -> {
			PatientDossierEntity empty = new PatientDossierEntity();
			empty.setPatientId(patientId);
			return empty;
		});
	}

	@PutMapping("/dossier")
	public PatientDossierEntity saveDossier(@PathVariable long patientId, @RequestBody PatientDossierEntity body) {
		contextService.getPatient(patientId);
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientDossierEntity dossier = dossierRepository.findById(patientId).orElseGet(PatientDossierEntity::new);
		dossier.setPatientId(patientId);
		dossier.setGroupeSanguin(clean(body.getGroupeSanguin()));
		dossier.setAllergies(clean(body.getAllergies()));
		dossier.setAntecedents(clean(body.getAntecedents()));
		dossier.setTraitements(clean(body.getTraitements()));
		dossier.setNotes(clean(body.getNotes()));
		dossier.setDateMaj(new Date());
		dossier.setMajPar(("Dr " + nz(doctor.getPrenom()) + " " + nz(doctor.getNom())).trim());
		return dossierRepository.save(dossier);
	}

	// ------------------------------------------------------------------ history of medical records

	/** The signed-in doctor's records for this patient, newest first. */
	@GetMapping("/historique")
	public List<Map<String, Object>> historique(@PathVariable long patientId) {
		contextService.getPatient(patientId);
		long doctorId = contextService.getCurrentDoctor().getId();
		List<Map<String, Object>> items = new ArrayList<>();

		ficheRepository.findByUserIdAndPatientId(doctorId, patientId).forEach(f -> items.add(item(
				"fiche", f.getIdFiche(), f.getDatedecreation(), "Consultation",
				firstNonBlank(f.getCausedeconsultation(), f.getExamen(), f.getSuivie()))));
		ordonnanceRepository.findByUserIdAndPatientId(doctorId, patientId).forEach(o -> items.add(item(
				"ordonnance", o.getIdOrdonnance(), o.getOrdonnance(), "Ordonnance",
				join(o.getNomDuMedicament(), o.getDosageDuMedicament(),
						o.getNbreDeFoisParJour() > 0 ? o.getNbreDeFoisParJour() + " fois par jour" : null))));
		certificatMedicalRepository.findByUserIdAndPatientId(doctorId, patientId).forEach(c -> items.add(item(
				"certificat-medical", c.getIdCertificats(), c.getDatedecreation(), "Certificat médical",
				join(c.getNombre_de_jour_repos() > 0 ? c.getNombre_de_jour_repos() + " jour(s) de repos" : null,
						c.getCommantaire()))));
		certificatAptitudeRepository.findByUserIdAndPatientId(doctorId, patientId).forEach(c -> items.add(item(
				"certificat-aptitude", c.getIdCertificatsaptitude(), c.getDatedecreation(), "Certificat d'aptitude",
				join(c.getProfession(), c.getEtat_de_sante()))));
		lettreRepository.findByUserIdAndPatientId(doctorId, patientId).forEach(l -> items.add(item(
				"lettre", l.getIdlettre(), l.getDatedecreation(), "Lettre à un confrère",
				join(("Dr " + nz(l.getPrenomdeconfrere()) + " " + nz(l.getNondeconfrere())).trim(),
						l.getSpecialitedeconfrere()))));

		items.sort(Comparator.comparing((Map<String, Object> m) -> (Date) m.get("date"),
				Comparator.nullsLast(Comparator.reverseOrder())));
		return items;
	}

	// ------------------------------------------------------------------ documents

	@GetMapping("/documents")
	public List<PatientDocumentEntity> documents(@PathVariable long patientId) {
		contextService.getPatient(patientId);
		return documentRepository.listFor(patientId, contextService.getCurrentDoctor().getId());
	}

	@PostMapping("/documents")
	public PatientDocumentEntity upload(@PathVariable long patientId,
			@RequestParam("fichier") MultipartFile file,
			@RequestParam(value = "categorie", required = false) String categorie) throws IOException {
		contextService.getPatient(patientId);
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
		}
		if (file.getSize() > MAX_FILE_BYTES) {
			throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File is larger than 10 MB");
		}
		String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
		if (!ALLOWED_TYPES.contains(type)) {
			throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only PDF and images are accepted");
		}
		PatientDocumentEntity doc = new PatientDocumentEntity();
		doc.setPatientId(patientId);
		doc.setMedecinId(contextService.getCurrentDoctor().getId());
		doc.setNom(safeName(file.getOriginalFilename()));
		doc.setTypeMime(type);
		doc.setTaille(file.getSize());
		doc.setCategorie(categorie != null && CATEGORIES.contains(categorie) ? categorie : "autre");
		doc.setDateAjout(new Date());
		doc.setContenu(file.getBytes());
		PatientDocumentEntity saved = documentRepository.save(doc);
		saved.setContenu(null);
		return saved;
	}

	@GetMapping("/documents/{id}/fichier")
	public ResponseEntity<byte[]> download(@PathVariable long patientId, @PathVariable long id) {
		PatientDocumentEntity doc = requireDocument(patientId, id);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(doc.getTypeMime()))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.builder("inline").filename(doc.getNom(), StandardCharsets.UTF_8).build().toString())
				.header("X-Content-Type-Options", "nosniff")
				.body(doc.getContenu());
	}

	@DeleteMapping("/documents/{id}")
	public void deleteDocument(@PathVariable long patientId, @PathVariable long id) {
		documentRepository.delete(requireDocument(patientId, id));
	}

	// ------------------------------------------------------------------ helpers

	private PatientDocumentEntity requireDocument(long patientId, long id) {
		contextService.getPatient(patientId);
		long doctorId = contextService.getCurrentDoctor().getId();
		return documentRepository.findById(id)
				.filter(d -> d.getPatientId() == patientId && d.getMedecinId() == doctorId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	private static Map<String, Object> item(String type, long id, Date date, String titre, String detail) {
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("type", type);
		item.put("id", id);
		item.put("date", date);
		item.put("titre", titre);
		item.put("detail", detail);
		return item;
	}

	private static String join(String... parts) {
		StringBuilder out = new StringBuilder();
		for (String part : parts) {
			if (part != null && !part.trim().isEmpty()) {
				if (out.length() > 0) {
					out.append(" · ");
				}
				out.append(part.trim());
			}
		}
		return out.length() == 0 ? null : abbreviate(out.toString());
	}

	private static String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.trim().isEmpty()) {
				return abbreviate(value.trim());
			}
		}
		return null;
	}

	private static String abbreviate(String value) {
		String flat = value.replaceAll("\\s+", " ");
		return flat.length() > 140 ? flat.substring(0, 139) + "…" : flat;
	}

	private static String clean(String value) {
		return value == null || value.trim().isEmpty() ? null : value.trim();
	}

	private static String nz(String value) {
		return value == null ? "" : value;
	}

	/** File name without any path, limited to 120 characters. */
	private static String safeName(String original) {
		String name = original == null ? "document" : original.replace('\\', '/');
		name = name.substring(name.lastIndexOf('/') + 1).trim();
		if (name.isEmpty()) {
			name = "document";
		}
		return name.length() > 120 ? name.substring(name.length() - 120) : name;
	}
}
