package com.example.portalmedical.appointment.job;


import com.example.portalmedical.user.service.PasswordService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.portalmedical.appointment.repository.RendezVousRepository;
import com.example.portalmedical.appointment.entity.RendezVousEntity;
import com.example.portalmedical.appointment.entity.RendezVousEntity.StateApt;

/**
 * Every evening at 18:00, e-mails a reminder (with a cancel link) for each confirmed
 * appointment of the next day. Each appointment is reminded once.
 */
@Component
public class RappelRendezVousJob {

	private static final Logger LOG = LoggerFactory.getLogger(RappelRendezVousJob.class);

	@Autowired
	private RendezVousRepository rendezVousRepository;

	@Autowired
	private PasswordService mailService;

	@Scheduled(cron = "${app.reminders.cron:0 0 18 * * *}")
	public void sendTomorrowReminders() {
		ZoneId zone = ZoneId.systemDefault();
		LocalDate tomorrow = LocalDate.now(zone).plusDays(1);
		Date start = Date.from(tomorrow.atStartOfDay(zone).toInstant());
		Date end = Date.from(tomorrow.plusDays(1).atStartOfDay(zone).toInstant());

		List<RendezVousEntity> appointments = rendezVousRepository
				.findByStateaptAndDateOfAptGreaterThanEqualAndDateOfAptLessThan(StateApt.confirmed, start, end);
		int sent = 0;
		for (RendezVousEntity rdv : appointments) {
			if (Boolean.TRUE.equals(rdv.getRappelEnvoye())) {
				continue;
			}
			if (rdv.getJetonAnnulation() == null) {
				rdv.setJetonAnnulation(UUID.randomUUID().toString().replace("-", ""));
			}
			if (mailService.envoyerRappel(rdv)) {
				rdv.setRappelEnvoye(true);
				sent++;
			}
			rendezVousRepository.save(rdv);
		}
		if (!appointments.isEmpty()) {
			LOG.info("Reminders for {}: {} sent out of {} appointments", tomorrow, sent, appointments.size());
		}
	}
}
