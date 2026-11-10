package com.example.PortalMedical.Services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.PortalMedical.DTO.UserDTO;
import com.example.PortalMedical.Repositories.PatientRepository;
import com.example.PortalMedical.Repositories.RendezVousRepository;
import com.example.PortalMedical.Repositories.UserRepository;
import com.example.PortalMedical.enteties.PatientEntity;
import com.example.PortalMedical.enteties.RendezVousEntity;
import com.example.PortalMedical.enteties.Role;
import com.example.PortalMedical.enteties.UserEntity;
import com.example.PortalMedical.enteties.RendezVousEntity.StateApt;
/**
 * 
 * @author hp
 *
 */
@Service
@Transactional
public class RendezVousServicesImpl implements RendezVousServices {

	@Autowired
    RendezVousRepository rvrepo;
	@Autowired
    UserRepository userRepository;
	@Autowired
	PasswordServices mailService;
	@Autowired
	PatientRepository patientRepository;
	
	/** A slot is taken while a request is pending or once it is confirmed; refusing frees it. */
	private static final List<StateApt> BLOCKING_STATES =
			Arrays.asList(StateApt.waiting, StateApt.confirmed, StateApt.demande);

	@Override
	public RendezVousEntity ajouterApt(final RendezVousEntity apt) {
		UserEntity user = findDoctor(apt.getUser());
		if (isSlotTaken(user.getId(), apt.getDateOfApt(), 0)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot already taken");
		}
		apt.setStateapt(StateApt.confirmed);
		apt.setUser(user);
		mailService.EnvoyerEmailAjout(apt);
		return rvrepo.save(apt);
	}

	@Override
	public List<String> takenSlots(long medecinId, Date day) {
		Calendar start = Calendar.getInstance();
		start.setTime(day);
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		Calendar end = (Calendar) start.clone();
		end.add(Calendar.DAY_OF_MONTH, 1);

		SimpleDateFormat time = new SimpleDateFormat("HH:mm");
		List<String> slots = new ArrayList<>();
		for (RendezVousEntity rdv : rvrepo.findByUser_IdAndDateOfAptGreaterThanEqualAndDateOfAptLessThanAndStateaptIn(
				medecinId, start.getTime(), end.getTime(), BLOCKING_STATES)) {
			slots.add(time.format(rdv.getDateOfApt()));
		}
		return slots;
	}

	@Override
	public List<RendezVousEntity> pendingRequestsFor(UserEntity current) {
		return inScope(current, StateApt.waiting);
	}

	@Override
	public List<RendezVousEntity> agendaFor(UserEntity current) {
		return inScope(current, StateApt.confirmed);
	}

	/**
	 * The doctor whose appointments this user works on: themself for a doctor, their doctor
	 * for a secretary added by one, null (every doctor) for older accounts not linked yet.
	 */
	private static Long scopeDoctorId(UserEntity current) {
		return PracticeScope.doctorIdOf(current);
	}

	@Override
	public List<RendezVousEntity> appointmentsFor(UserEntity current, StateApt state) {
		return inScope(current, state);
	}

	private List<RendezVousEntity> inScope(UserEntity current, StateApt state) {
		Long doctorId = scopeDoctorId(current);
		List<RendezVousEntity> list;
		if (state == null) {
			list = doctorId != null ? rvrepo.findByUser_Id(doctorId) : rvrepo.findAll();
		} else {
			list = doctorId != null
					? rvrepo.findByUser_IdAndStateapt(doctorId, state)
					: rvrepo.getByStateapt(state);
		}
		list.sort(Comparator.comparing(RendezVousEntity::getDateOfApt,
				Comparator.nullsLast(Comparator.naturalOrder())));
		return list;
	}

	private UserEntity findDoctor(UserEntity ref) {
		if (ref == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is required");
		}
		return userRepository.findById(ref.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown doctor"));
	}

	/** True when the doctor already has a pending or confirmed appointment at that exact time. */
	private boolean isSlotTaken(long doctorId, Date when, long ignoredAppointmentId) {
		if (when == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
		}
		Date end = new Date(when.getTime() + 60_000L);
		return rvrepo.findByUser_IdAndDateOfAptGreaterThanEqualAndDateOfAptLessThanAndStateaptIn(
				doctorId, when, end, BLOCKING_STATES).stream()
				.anyMatch(rdv -> rdv.getIdapt() != ignoredAppointmentId);
	}

	private static String clean(String value) {
		return value == null ? "" : value.trim();
	}

	/** Finds the patient by e-mail, or creates the record from the request details. */
	private PatientEntity linkPatient(RendezVousEntity rv) {
		String email = clean(rv.getPatientEmail());
		Long doctorId = rv.getUser() == null ? null : rv.getUser().getId();
		// Look only inside this doctor's practice: another practice's record stays private.
		PatientEntity patient = email.isEmpty() ? null : patientRepository.findFirstByEmailIgnoreCaseAndMedecinId(email, doctorId);
		if (patient == null) {
			patient = new PatientEntity();
			patient.setMedecinId(doctorId);
			patient.setNom(clean(rv.getPatientNom()));
			patient.setPrenom(clean(rv.getPatientPrenom()));
			patient.setEmail(email);
			patient.setNumtelephone(clean(rv.getPatientTelephone()));
			patient.setAge(rv.getPatientDateNaissance());
			patient.setCreationDate(new Date());
			patient = patientRepository.save(patient);
		}
		return patient;
	}

	@Override
	public List<RendezVousEntity> retrieveAllApt() {
		return (List<RendezVousEntity>) rvrepo.findAll();

	}

	@Override
	public void deleteAptById(long aptId) {
		rvrepo.deleteById(aptId);			
	}

	@Override
	public RendezVousEntity updateApt(RendezVousEntity apt) {
		RendezVousEntity rdv = rvrepo.findById(apt.getIdapt())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		UserEntity user = findDoctor(apt.getUser());
		// Another appointment of that doctor at the new time (this one excluded).
		if (isSlotTaken(user.getId(), apt.getDateOfApt(), rdv.getIdapt())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot already taken");
		}
		// Only the doctor, the time and the e-mail can change; the patient and state stay.
		rdv.setUser(user);
		rdv.setDateOfApt(apt.getDateOfApt());
		if (apt.getPatientEmail() != null && !apt.getPatientEmail().trim().isEmpty()) {
			rdv.setPatientEmail(apt.getPatientEmail().trim());
		}
		rdv = rvrepo.save(rdv);
		mailService.EnvoyerEmailAjout(rdv);
		return rdv;
	}

	@Override
	public List<RendezVousEntity> getAllRdvByUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RendezVousEntity> getAptByState(String state) {
		List<RendezVousEntity> Rdvs= new ArrayList<>();
		Rdvs= rvrepo.getByStateapt(StateApt.valueOf(state));
		return Rdvs;
	}

	@Override
	public List<RendezVousEntity> getAllAptList() {
        List<RendezVousEntity> rv = rvrepo.findAll();
        return rv;
	}

	@Override
	public void ChangeStateById(int aptId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ChangeStateById1(int aptId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<RendezVousEntity> AfficherDemandes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAdsById1(int aptId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<RendezVousEntity> getAllRdvByUserIdWaiting() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RendezVousEntity> getTodaysApt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RendezVousEntity> getAllRdvByUserIdWaiting2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RendezVousEntity demandeRDV(RendezVousEntity apt) {
		// Public endpoint: always create a new pending request, never touch an existing one.
		if (clean(apt.getPatientNom()).isEmpty() || clean(apt.getPatientPrenom()).isEmpty()
				|| clean(apt.getPatientTelephone()).isEmpty() || clean(apt.getPatientEmail()).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient details are required");
		}
		if (apt.getDateOfApt() == null || apt.getDateOfApt().before(new Date())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a future slot");
		}
		UserEntity us = findDoctor(apt.getUser());
		if (us.getRole() != Role.Medecin) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown doctor");
		}
		if (isSlotTaken(us.getId(), apt.getDateOfApt(), 0)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot already taken");
		}
		apt.setIdapt(0);
		apt.setPatient(null);
		apt.setPatientNom(clean(apt.getPatientNom()));
		apt.setPatientPrenom(clean(apt.getPatientPrenom()));
		apt.setPatientTelephone(clean(apt.getPatientTelephone()));
		apt.setPatientEmail(clean(apt.getPatientEmail()));
		apt.setUser(us);
		apt.setStateapt(StateApt.waiting);
		return rvrepo.save(apt);
	}

	@Override
	public RendezVousEntity getbyid(Long id) {
		
		return rvrepo.findById(id).get();
	}

	@Override
	public RendezVousEntity confirmerRdv(RendezVousEntity rdv) {
		RendezVousEntity rv = rvrepo.findById(rdv.getIdapt())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		// Another appointment of the same doctor at the same time (this request excluded).
		if (rv.getUser() != null && isSlotTaken(rv.getUser().getId(), rv.getDateOfApt(), rv.getIdapt())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot already taken");
		}
		rv.setStateapt(StateApt.confirmed);
		if (rv.getPatient() == null) {
			rv.setPatient(linkPatient(rv));
		}
		mailService.EnvoyerEmailAjout(rv);
		return rvrepo.save(rv);
	}

	@Override
	public RendezVousEntity annulerRdv(RendezVousEntity rdv) {
		RendezVousEntity rv = rvrepo.findById(rdv.getIdapt()).get();
		rv.setStateapt(StateApt.refused);
		mailService.EnvoyerEmailRefuser(rv);
		return rvrepo.save(rv);
	} 	

}