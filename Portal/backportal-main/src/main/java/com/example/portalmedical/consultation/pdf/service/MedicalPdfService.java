package com.example.portalmedical.consultation.pdf.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import com.example.portalmedical.consultation.pdf.dto.DocumentPdfRequest;
import com.example.portalmedical.consultation.pdf.dto.OrdonnancePdfLine;
import com.example.portalmedical.consultation.pdf.dto.OrdonnancePdfRequest;
import com.example.portalmedical.common.enums.Genre;
import com.example.portalmedical.payment.entity.PaiementEntity;
import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.user.entity.UserEntity;

/**
 * Builds the medical documents as PDF with one shared design: doctor letterhead, patient panel,
 * document body, signature area, and a footer with page numbers on every page.
 * Documents: ordonnance, certificat médical, certificat d'aptitude, lettre à un confrère.
 */
@Service
public class MedicalPdfService {

	// Brand palette (matches the web app)
	private static final Color NAVY = new Color(31, 58, 95);
	private static final Color TEAL = new Color(32, 197, 179);
	private static final Color TEAL_DARK = new Color(17, 125, 105);
	private static final Color TEXT = new Color(36, 54, 78);
	private static final Color MUTED = new Color(109, 124, 145);
	private static final Color FAINT = new Color(160, 174, 190);
	private static final Color BORDER = new Color(226, 232, 238);
	private static final Color TINT = new Color(243, 248, 250);
	private static final Color TEAL_TINT = new Color(232, 248, 245);
	private static final Color ALERT = new Color(180, 52, 52);
	private static final Color ALERT_TINT = new Color(253, 240, 240);

	private static final PDFont REGULAR = PDType1Font.HELVETICA;
	private static final PDFont BOLD = PDType1Font.HELVETICA_BOLD;
	private static final PDFont ITALIC = PDType1Font.HELVETICA_OBLIQUE;

	private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
	private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
	private static final float MARGIN = 50f;
	private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
	private static final float FOOTER_TOP = 58f;
	private static final float BOTTOM_LIMIT = FOOTER_TOP + 14f;

	private static final float LABEL_COLUMN = 110f;
	private static final float BODY_SIZE = 10f;
	private static final float BODY_LEADING = 14f;
	private static final float PROSE_SIZE = 10.5f;
	private static final float PROSE_LEADING = 15.5f;

	private enum DocKind {
		ORDONNANCE("Ordonnance", "ORD"),
		CERTIFICAT_MEDICAL("Certificat médical", "CM"),
		CERTIFICAT_APTITUDE("Certificat d'aptitude", "CA"),
		LETTRE("Lettre à un confrère", "LC"),
		RECU("Reçu de paiement", "REC");

		private final String title;
		private final String refPrefix;

		DocKind(String title, String refPrefix) {
			this.title = title;
			this.refPrefix = refPrefix;
		}
	}

	private interface Body {
		void draw(PdfState state) throws IOException;
	}

	// ------------------------------------------------------------------ public API

	public byte[] buildOrdonnance(UserEntity doctor, PatientEntity patient, OrdonnancePdfRequest request)
			throws IOException {
		return render(DocKind.ORDONNANCE, doctor, patient, state -> {
			drawPatientPanel(state);
			drawRows(state, "Contexte clinique", new String[][] {
					{ "Diagnostic", request.getDiagnostic() },
					{ "Antécédents", request.getAntecedents() },
					{ "Allergies", request.getAllergies() },
					{ "Consignes", request.getConsigne() },
					{ "Suivi", request.getSuivi() } }, "Allergies");
			drawPrescription(state, request);
			drawSignature(state);
		});
	}

	public byte[] buildCertificatMedical(UserEntity doctor, PatientEntity patient, DocumentPdfRequest request)
			throws IOException {
		return render(DocKind.CERTIFICAT_MEDICAL, doctor, patient, state -> {
			drawPatientPanel(state);
			Civility civ = civility(patient);

			StringBuilder text = new StringBuilder(attestation(doctor, patient, civ));
			String repos = restDuration(request.getRepos());
			if (repos != null) {
				text.append(" Son état de santé nécessite un repos de ").append(repos).append(" à compter du ")
						.append(formatDate(state.issueDate)).append(", sauf complication.");
			}
			drawProse(state, text.toString());

			if (repos != null) {
				drawKeyFigure(state, "Repos prescrit", repos, "À compter du " + formatDate(state.issueDate));
			}
			if (!isBlank(request.getNote())) {
				drawSectionTitle(state, "Observations");
				drawProse(state, request.getNote());
			}
			drawClosing(state, "Certificat établi à la demande de " + civ.concerned
					+ " et remis en main propre pour servir et valoir ce que de droit.");
			drawSignature(state);
		});
	}

	public byte[] buildCertificatAptitude(UserEntity doctor, PatientEntity patient, DocumentPdfRequest request)
			throws IOException {
		return render(DocKind.CERTIFICAT_APTITUDE, doctor, patient, state -> {
			drawPatientPanel(state);
			Civility civ = civility(patient);
			drawProse(state, attestation(doctor, patient, civ));
			drawRows(state, "Examen", new String[][] {
					{ "Profession / activité", request.getProfession() },
					{ "État de santé", request.getEtatSante() } }, null);
			if (!isBlank(request.getNote())) {
				drawSectionTitle(state, "Observations");
				drawProse(state, request.getNote());
			}
			drawClosing(state, "Certificat établi à la demande de " + civ.concerned
					+ " et remis en main propre pour servir et valoir ce que de droit.");
			drawSignature(state);
		});
	}

	public byte[] buildLettre(UserEntity doctor, PatientEntity patient, DocumentPdfRequest request) throws IOException {
		return render(DocKind.LETTRE, doctor, patient, state -> {
			Civility civ = civility(patient);

			String recipient = isBlank(request.getRecipient()) ? "Mon cher confrère" : request.getRecipient().trim();
			ensureSpace(state, 40f);
			writeText(state.content, "À L'ATTENTION DE", BOLD, 7f, 1.2f, MARGIN, state.y, MUTED);
			state.y -= 16f;
			drawProse(state, recipient, BOLD, 11.5f, NAVY);

			drawPatientPanel(state);

			if (!isBlank(request.getSubject())) {
				ensureSpace(state, 30f);
				float objetWidth = textWidth("Objet : ", BOLD, PROSE_SIZE, 0f);
				writeText(state.content, "Objet : ", BOLD, PROSE_SIZE, MARGIN, state.y, NAVY);
				List<String> lines = wrapText(request.getSubject(), REGULAR, PROSE_SIZE, CONTENT_WIDTH - objetWidth);
				writeLines(state.content, lines, REGULAR, PROSE_SIZE, MARGIN + objetWidth, state.y, PROSE_LEADING, TEXT);
				state.y -= lines.size() * PROSE_LEADING + 14f;
			}

			drawProse(state, "Cher(e) confrère,");
			drawProse(state, "Je vous adresse " + civ.title + " " + fullName(patient) + ", " + civ.born + " le "
					+ formatDate(patient.getAge()) + ", " + civ.seen + " ce jour en consultation.");

			if (!isBlank(request.getResume())) {
				drawSectionTitle(state, "Résumé clinique");
				drawProse(state, request.getResume());
			}
			if (!isBlank(request.getRequest())) {
				drawSectionTitle(state, "Motif de la demande");
				drawProse(state, request.getRequest());
			}

			drawProse(state, "Je vous remercie de l'attention que vous porterez à " + civ.thisPatient
					+ " et reste à votre disposition pour tout renseignement complémentaire.");
			drawProse(state, "Bien confraternellement,");
			drawSignature(state);
		});
	}

	/** Payment receipt for a consultation. */
	public byte[] buildRecu(UserEntity doctor, PatientEntity patient, PaiementEntity paiement) throws IOException {
		return render(DocKind.RECU, doctor, patient, state -> {
			drawPatientPanel(state);
			String when = formatDate(paiement.getDatePaiement());
			drawProse(state, "Je soussigné(e), Dr " + doctorName(doctor) + ", certifie avoir reçu de "
					+ fullName(patient) + " la somme de " + formatAmount(paiement.getMontant())
					+ " au titre d'une consultation médicale, le " + when + ".");
			drawKeyFigure(state, "Montant reçu", formatAmount(paiement.getMontant()),
					"Payé par " + modeLabel(paiement.getMode()) + " le " + when);
			drawRows(state, "Détails", new String[][] {
					{ "N° de reçu", paiement.getNumeroRecu() },
					{ "Acte", "Consultation" },
					{ "Mode de paiement", modeLabel(paiement.getMode()) },
					{ "Encaissé par", paiement.getEncaissePar() },
					{ "Note", paiement.getNote() } }, null);
			drawClosing(state, "Reçu délivré pour servir et valoir ce que de droit.");
			drawSignature(state);
		});
	}

	/** 45.5 -> "45,500 DT" (Tunisian dinar, 3 decimals). */
	private static String formatAmount(double amount) {
		java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(Locale.FRANCE);
		symbols.setGroupingSeparator(' ');
		return new java.text.DecimalFormat("#,##0.000", symbols).format(amount) + " DT";
	}

	private static String modeLabel(String mode) {
		if (mode == null) {
			return "espèces";
		}
		switch (mode) {
			case "cheque":
				return "chèque";
			case "carte":
				return "carte bancaire";
			case "virement":
				return "virement";
			default:
				return "espèces";
		}
	}

	// ------------------------------------------------------------------ document skeleton

	private byte[] render(DocKind kind, UserEntity doctor, PatientEntity patient, Body body) throws IOException {
		PDDocument document = new PDDocument();
		PdfState state = new PdfState(document, kind, doctor, patient, new Date());
		try {
			PDDocumentInformation info = document.getDocumentInformation();
			info.setTitle(kind.title + " - " + fullName(patient));
			info.setAuthor("Dr. " + doctorName(doctor));
			info.setCreator("PortalMedical");

			startFirstPage(state);
			body.draw(state);
			state.closeStream();
			drawFooters(state);

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			document.save(output);
			return output.toByteArray();
		} finally {
			state.closeStream();
			document.close();
		}
	}

	private void startFirstPage(PdfState state) throws IOException {
		newPage(state);
		PDPageContentStream c = state.content;
		UserEntity doctor = state.doctor;

		drawTopAccent(c);
		float top = PAGE_HEIGHT - 46f;

		// Brand mark + doctor letterhead (left)
		drawBrandMark(c, MARGIN, top - 40f, 40f);
		float textX = MARGIN + 54f;
		writeText(c, "Dr. " + doctorName(doctor), BOLD, 17f, textX, top - 14f, NAVY);
		writeText(c, "Médecin", REGULAR, 9.5f, textX, top - 29f, MUTED);
		String contact = joinNonEmpty("   ·   ", doctor.getNumtelephone(), doctor.getEmail());
		if (!contact.isEmpty()) {
			writeText(c, contact, REGULAR, 8.5f, textX, top - 42f, MUTED);
		}

		// Document title, date and reference (right); long titles shrink to keep clear of the letterhead
		float right = PAGE_WIDTH - MARGIN;
		String title = state.kind.title.toUpperCase(Locale.FRANCE);
		float titleSize = 16f;
		while (titleSize > 11f && textWidth(title, BOLD, titleSize, titleSize * 0.13f) > 230f) {
			titleSize -= 0.5f;
		}
		writeTextRight(c, title, BOLD, titleSize, titleSize * 0.13f, right, top - 14f, NAVY);
		writeTextRight(c, "Le " + formatDate(state.issueDate), REGULAR, 9.5f, 0f, right, top - 29f, TEXT);
		writeTextRight(c, "Réf. " + reference(state), REGULAR, 8f, 0f, right, top - 42f, FAINT);

		float dividerY = top - 62f;
		hairline(c, MARGIN, dividerY, PAGE_WIDTH - MARGIN, BORDER, 0.8f);
		state.y = dividerY - 20f;
	}

	/** Pages after the first carry a compact header so a loose sheet can still be identified. */
	private void startContinuationPage(PdfState state) throws IOException {
		newPage(state);
		PDPageContentStream c = state.content;
		drawTopAccent(c);
		float top = PAGE_HEIGHT - 40f;
		drawBrandMark(c, MARGIN, top - 20f, 20f);
		writeText(c, state.kind.title + "  ·  " + fullName(state.patient) + "  (suite)", BOLD, 10f, MARGIN + 28f,
				top - 13f, NAVY);
		writeTextRight(c, "Le " + formatDate(state.issueDate), REGULAR, 9f, 0f, PAGE_WIDTH - MARGIN, top - 13f, MUTED);
		hairline(c, MARGIN, top - 30f, PAGE_WIDTH - MARGIN, BORDER, 0.8f);
		state.y = top - 52f;
	}

	private void newPage(PdfState state) throws IOException {
		state.closeStream();
		PDPage page = new PDPage(PDRectangle.A4);
		state.document.addPage(page);
		state.content = new PDPageContentStream(state.document, page);
	}

	private void ensureSpace(PdfState state, float height) throws IOException {
		if (state.y - height < BOTTOM_LIMIT) {
			startContinuationPage(state);
		}
	}

	private void drawTopAccent(PDPageContentStream c) throws IOException {
		fillRect(c, 0, PAGE_HEIGHT - 6f, PAGE_WIDTH, 6f, TEAL);
		fillRect(c, 0, PAGE_HEIGHT - 6f, PAGE_WIDTH * 0.32f, 6f, NAVY);
	}

	/** Second pass: the footer needs the total page count. */
	private void drawFooters(PdfState state) throws IOException {
		int total = state.document.getNumberOfPages();
		for (int i = 0; i < total; i++) {
			PDPage page = state.document.getPage(i);
			PDPageContentStream c = new PDPageContentStream(state.document, page,
					PDPageContentStream.AppendMode.APPEND, true, true);
			try {
				hairline(c, MARGIN, FOOTER_TOP - 18f, PAGE_WIDTH - MARGIN, BORDER, 0.6f);
				float y = FOOTER_TOP - 32f;
				writeText(c, "PortalMedical", BOLD, 7.5f, MARGIN, y, NAVY);
				float brandWidth = textWidth("PortalMedical", BOLD, 7.5f, 0f);
				writeText(c, "  ·  Accessible. Sécurisé. Humain.", REGULAR, 7.5f, MARGIN + brandWidth, y, FAINT);
				writeTextRight(c, "Page " + (i + 1) + " / " + total, REGULAR, 7.5f, 0f, PAGE_WIDTH - MARGIN, y, MUTED);
			} finally {
				c.close();
			}
		}
	}

	// ------------------------------------------------------------------ shared blocks

	private void drawPatientPanel(PdfState state) throws IOException {
		PatientEntity patient = state.patient;
		if (patient == null) {
			return;
		}
		List<String[]> fields = new ArrayList<>();
		fields.add(new String[] { "PATIENT", fullName(patient) });
		String birth = formatDate(patient.getAge());
		Integer years = ageInYears(patient.getAge(), state.issueDate);
		if (years != null) {
			birth += "  (" + years + " ans)";
		}
		fields.add(new String[] { "NÉ(E) LE", birth });
		fields.add(new String[] { "CIN", safe(patient.getCin()) });
		if (!isBlank(patient.getNumtelephone())) {
			fields.add(new String[] { "TÉLÉPHONE", patient.getNumtelephone().trim() });
		}

		float height = 50f;
		ensureSpace(state, height + 24f);
		PDPageContentStream c = state.content;
		float top = state.y;
		fillRoundRect(c, MARGIN, top - height, CONTENT_WIDTH, height, 7f, TINT);
		fillRect(c, MARGIN, top - height, 3f, height, TEAL);

		float innerX = MARGIN + 18f;
		float columnWidth = (CONTENT_WIDTH - 24f) / fields.size();
		for (int i = 0; i < fields.size(); i++) {
			float x = innerX + i * columnWidth;
			writeText(c, fields.get(i)[0], BOLD, 7f, 1.2f, x, top - 19f, MUTED);
			float valueSize = i == 0 ? 11.5f : 10f;
			PDFont valueFont = i == 0 ? BOLD : REGULAR;
			String value = ellipsize(fields.get(i)[1], valueFont, valueSize, columnWidth - 12f);
			writeText(c, value, valueFont, valueSize, x, top - 35f, i == 0 ? NAVY : TEXT);
		}
		state.y = top - height - 28f;
	}

	/** Label/value grid; empty rows are skipped and the alert row (allergies) is highlighted. */
	private void drawRows(PdfState state, String title, String[][] rows, String alertLabel) throws IOException {
		List<String[]> filled = new ArrayList<>();
		for (String[] row : rows) {
			if (!isBlank(row[1])) {
				filled.add(row);
			}
		}
		if (filled.isEmpty()) {
			return;
		}

		float valueWidth = CONTENT_WIDTH - LABEL_COLUMN;
		float firstRowHeight = wrapText(filled.get(0)[1], REGULAR, BODY_SIZE, valueWidth).size() * BODY_LEADING;
		ensureSpace(state, 26f + firstRowHeight);
		drawSectionTitle(state, title);

		for (String[] row : filled) {
			boolean alert = row[0].equals(alertLabel);
			PDFont font = alert ? BOLD : REGULAR;
			List<String> lines = wrapText(row[1], font, BODY_SIZE, alert ? valueWidth - 16f : valueWidth);
			float rowHeight = lines.size() * BODY_LEADING;
			ensureSpace(state, rowHeight + 8f);
			PDPageContentStream c = state.content;
			float y = state.y;

			if (alert) {
				fillRoundRect(c, MARGIN + LABEL_COLUMN - 8f, y - rowHeight + 6f, valueWidth + 8f, rowHeight + 4f, 4f,
						ALERT_TINT);
				writeText(c, row[0], BOLD, 9f, MARGIN, y - 2f, ALERT);
				writeLines(c, lines, font, BODY_SIZE, MARGIN + LABEL_COLUMN, y - 2f, BODY_LEADING, ALERT);
			} else {
				writeText(c, row[0], REGULAR, 9f, MARGIN, y - 2f, MUTED);
				writeLines(c, lines, font, BODY_SIZE, MARGIN + LABEL_COLUMN, y - 2f, BODY_LEADING, TEXT);
			}
			state.y = y - rowHeight - 6f;
		}
		state.y -= 16f;
	}

	private void drawProse(PdfState state, String text) throws IOException {
		drawProse(state, text, REGULAR, PROSE_SIZE, TEXT);
	}

	/** Running text, broken across pages line by line when needed. */
	private void drawProse(PdfState state, String text, PDFont font, float size, Color color) throws IOException {
		List<String> lines = wrapText(text, font, size, CONTENT_WIDTH);
		for (String line : lines) {
			ensureSpace(state, PROSE_LEADING);
			writeText(state.content, line, font, size, MARGIN, state.y, color);
			state.y -= PROSE_LEADING;
		}
		state.y -= 10f;
	}

	/** Highlighted figure, e.g. the number of days of rest on a medical certificate. */
	private void drawKeyFigure(PdfState state, String label, String value, String detail) throws IOException {
		float height = 52f;
		ensureSpace(state, height + 20f);
		PDPageContentStream c = state.content;
		float top = state.y + 4f;
		float width = 240f;
		fillRoundRect(c, MARGIN, top - height, width, height, 7f, TEAL_TINT);
		writeText(c, label.toUpperCase(Locale.FRANCE), BOLD, 7f, 1.2f, MARGIN + 16f, top - 18f, TEAL_DARK);
		writeText(c, ellipsize(value, BOLD, 15f, width - 32f), BOLD, 15f, MARGIN + 16f, top - 37f, NAVY);
		float valueWidth = textWidth(ellipsize(value, BOLD, 15f, width - 32f), BOLD, 15f, 0f);
		if (valueWidth + 130f < width) {
			writeText(c, detail, REGULAR, 9f, MARGIN + 26f + valueWidth, top - 37f, MUTED);
		}
		state.y = top - height - 24f;
	}

	private void drawClosing(PdfState state, String text) throws IOException {
		state.y -= 4f;
		List<String> lines = wrapText(text, ITALIC, 9.5f, CONTENT_WIDTH);
		for (String line : lines) {
			ensureSpace(state, 14f);
			writeText(state.content, line, ITALIC, 9.5f, MARGIN, state.y, MUTED);
			state.y -= 14f;
		}
		state.y -= 8f;
	}

	private void drawPrescription(PdfState state, OrdonnancePdfRequest request) throws IOException {
		List<OrdonnancePdfLine> lines = new ArrayList<>();
		if (request.getLines() != null) {
			for (OrdonnancePdfLine line : request.getLines()) {
				if (line != null && hasAnyValue(line.getMedicament(), line.getDosage(), line.getForme(), line.getVoie(),
						line.getFrequence(), line.getDuree(), line.getInstructions())) {
					lines.add(line);
				}
			}
		}

		ensureSpace(state, 26f + 50f);
		drawSectionTitle(state, "Prescription");

		if (lines.isEmpty()) {
			writeText(state.content, "Aucun médicament n'a été prescrit.", ITALIC, BODY_SIZE, MARGIN, state.y - 2f, MUTED);
			state.y -= 30f;
			return;
		}

		int index = 1;
		for (OrdonnancePdfLine line : lines) {
			drawPrescriptionItem(state, line, index, index == lines.size());
			index++;
		}
		state.y -= 10f;
	}

	private void drawPrescriptionItem(PdfState state, OrdonnancePdfLine line, int index, boolean last)
			throws IOException {
		float textX = MARGIN + 30f;
		float textWidth = CONTENT_WIDTH - 30f;

		String name = isBlank(line.getMedicament()) ? "Médicament" : line.getMedicament().trim();
		String detail = joinNonEmpty("  ·  ", line.getDosage(), line.getForme());
		List<String> nameLines = wrapText(name, BOLD, 12f, textWidth);
		boolean detailInline = !detail.isEmpty() && nameLines.size() == 1
				&& textWidth(nameLines.get(0), BOLD, 12f, 0f) + 10f + textWidth(detail, REGULAR, 10.5f, 0f) <= textWidth;
		List<String> detailLines = detail.isEmpty() || detailInline ? new ArrayList<String>()
				: wrapText(detail, REGULAR, 10.5f, textWidth);

		String posology = joinNonEmpty("     ·     ", labelled("Voie", line.getVoie()),
				labelled("Fréquence", line.getFrequence()), labelled("Durée", line.getDuree()));
		List<String> posologyLines = posology.isEmpty() ? new ArrayList<String>()
				: wrapText(posology, REGULAR, 9.5f, textWidth);
		List<String> instructionLines = isBlank(line.getInstructions()) ? new ArrayList<String>()
				: wrapText(line.getInstructions(), ITALIC, 9.5f, textWidth);

		float height = nameLines.size() * 16f + detailLines.size() * 14f + 4f + posologyLines.size() * 13f
				+ (instructionLines.isEmpty() ? 0f : 4f + instructionLines.size() * 13f) + 18f;
		ensureSpace(state, height);

		PDPageContentStream c = state.content;
		float y = state.y;

		float badgeCx = MARGIN + 10f;
		float badgeCy = y + 1f;
		fillCircle(c, badgeCx, badgeCy, 10f, TEAL_DARK);
		String number = String.valueOf(index);
		writeText(c, number, BOLD, 9.5f, badgeCx - textWidth(number, BOLD, 9.5f, 0f) / 2f, badgeCy - 3.4f, Color.WHITE);

		float lineY = y - 3f;
		for (int i = 0; i < nameLines.size(); i++) {
			writeText(c, nameLines.get(i), BOLD, 12f, textX, lineY, NAVY);
			if (i == 0 && detailInline) {
				writeText(c, detail, REGULAR, 10.5f, textX + textWidth(nameLines.get(0), BOLD, 12f, 0f) + 10f, lineY, MUTED);
			}
			lineY -= 16f;
		}
		for (String detailLine : detailLines) {
			writeText(c, detailLine, REGULAR, 10.5f, textX, lineY + 2f, MUTED);
			lineY -= 14f;
		}

		lineY -= 2f;
		for (String posologyLine : posologyLines) {
			writeText(c, posologyLine, REGULAR, 9.5f, textX, lineY, TEXT);
			lineY -= 13f;
		}
		if (!instructionLines.isEmpty()) {
			lineY -= 2f;
			for (String instruction : instructionLines) {
				writeText(c, instruction, ITALIC, 9.5f, textX, lineY, MUTED);
				lineY -= 13f;
			}
		}

		float separatorY = lineY + 3f;
		if (!last) {
			hairline(c, textX, separatorY, PAGE_WIDTH - MARGIN, BORDER, 0.6f);
		}
		state.y = separatorY - 18f;
	}

	private void drawSignature(PdfState state) throws IOException {
		float width = 210f;
		float height = 92f;
		ensureSpace(state, height + 16f);
		PDPageContentStream c = state.content;
		float x = PAGE_WIDTH - MARGIN - width;
		float top = state.y - 4f;

		writeText(c, "SIGNATURE ET CACHET", BOLD, 7f, 1.2f, x, top - 8f, MUTED);
		strokeRoundRect(c, x, top - height, width, height - 18f, 6f, BORDER, 0.8f);
		writeTextRight(c, "Dr. " + doctorName(state.doctor), BOLD, 9.5f, 0f, x + width - 12f, top - height + 12f, NAVY);
		state.y = top - height - 10f;
	}

	private void drawSectionTitle(PdfState state, String title) throws IOException {
		ensureSpace(state, 26f + PROSE_LEADING * 2);
		PDPageContentStream c = state.content;
		String label = title.toUpperCase(Locale.FRANCE);
		float y = state.y;
		writeText(c, label, BOLD, 8.5f, 1.6f, MARGIN, y, TEAL_DARK);
		float labelWidth = textWidth(label, BOLD, 8.5f, 1.6f);
		hairline(c, MARGIN + labelWidth + 10f, y + 3f, PAGE_WIDTH - MARGIN, BORDER, 0.8f);
		state.y = y - 24f;
	}

	// ------------------------------------------------------------------ wording helpers

	/** French agreement depending on the patient's recorded sex; neutral forms when unknown. */
	private static final class Civility {
		private final String title;
		private final String born;
		private final String concerned;
		private final String thisPatient;
		private final String seen;

		private Civility(String title, String born, String concerned, String thisPatient, String seen) {
			this.title = title;
			this.born = born;
			this.seen = seen;
			this.concerned = concerned;
			this.thisPatient = thisPatient;
		}
	}

	private Civility civility(PatientEntity patient) {
		Genre sexe = patient == null ? null : patient.getSexe();
		if (sexe == Genre.Homme) {
			return new Civility("Monsieur", "né", "l'intéressé", "ce patient", "vu");
		}
		if (sexe == Genre.Femme) {
			return new Civility("Madame", "née", "l'intéressée", "cette patiente", "vue");
		}
		return new Civility("M./Mme", "né(e)", "l'intéressé(e)", "ce(tte) patient(e)", "vu(e)");
	}

	private String attestation(UserEntity doctor, PatientEntity patient, Civility civ) {
		return "Je soussigné(e), Dr. " + doctorName(doctor) + ", certifie avoir examiné ce jour " + civ.title + " "
				+ fullName(patient) + ", " + civ.born + " le " + formatDate(patient.getAge()) + ".";
	}

	/** "3" becomes "3 jours"; free text such as "une semaine" is kept as typed. */
	private String restDuration(String value) {
		if (isBlank(value)) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.matches("\\d+")) {
			int days = Integer.parseInt(trimmed);
			return days + (days > 1 ? " jours" : " jour");
		}
		return trimmed;
	}

	// ------------------------------------------------------------------ drawing primitives

	/** Vector version of the PortalMedical mark: teal ring, small dot, navy cross. */
	private void drawBrandMark(PDPageContentStream c, float x, float y, float size) throws IOException {
		float s = size / 64f;
		float cx = x + 30f * s;
		float cy = y + (64f - 34f) * s;
		c.setStrokingColor(TEAL);
		c.setLineWidth(4f * s);
		circlePath(c, cx, cy, 22f * s);
		c.stroke();
		fillCircle(c, x + 47f * s, y + (64f - 17f) * s, 4.5f * s, TEAL);
		float arm = 4.5f * s;
		float len = 16.5f * s;
		fillRect(c, cx - arm, cy - len, arm * 2, len * 2, NAVY);
		fillRect(c, cx - len, cy - arm, len * 2, arm * 2, NAVY);
		c.setLineWidth(1f);
	}

	private void fillRect(PDPageContentStream c, float x, float y, float w, float h, Color color) throws IOException {
		c.setNonStrokingColor(color);
		c.addRect(x, y, w, h);
		c.fill();
	}

	private void hairline(PDPageContentStream c, float x1, float y, float x2, Color color, float width)
			throws IOException {
		c.setStrokingColor(color);
		c.setLineWidth(width);
		c.moveTo(x1, y);
		c.lineTo(x2, y);
		c.stroke();
		c.setLineWidth(1f);
	}

	private void fillCircle(PDPageContentStream c, float cx, float cy, float r, Color color) throws IOException {
		c.setNonStrokingColor(color);
		circlePath(c, cx, cy, r);
		c.fill();
	}

	private void circlePath(PDPageContentStream c, float cx, float cy, float r) throws IOException {
		float k = 0.552284749831f * r;
		c.moveTo(cx + r, cy);
		c.curveTo(cx + r, cy + k, cx + k, cy + r, cx, cy + r);
		c.curveTo(cx - k, cy + r, cx - r, cy + k, cx - r, cy);
		c.curveTo(cx - r, cy - k, cx - k, cy - r, cx, cy - r);
		c.curveTo(cx + k, cy - r, cx + r, cy - k, cx + r, cy);
		c.closePath();
	}

	private void roundRectPath(PDPageContentStream c, float x, float y, float w, float h, float r) throws IOException {
		float k = 0.552284749831f * r;
		c.moveTo(x + r, y);
		c.lineTo(x + w - r, y);
		c.curveTo(x + w - r + k, y, x + w, y + r - k, x + w, y + r);
		c.lineTo(x + w, y + h - r);
		c.curveTo(x + w, y + h - r + k, x + w - r + k, y + h, x + w - r, y + h);
		c.lineTo(x + r, y + h);
		c.curveTo(x + r - k, y + h, x, y + h - r + k, x, y + h - r);
		c.lineTo(x, y + r);
		c.curveTo(x, y + r - k, x + r - k, y, x + r, y);
		c.closePath();
	}

	private void fillRoundRect(PDPageContentStream c, float x, float y, float w, float h, float r, Color color)
			throws IOException {
		c.setNonStrokingColor(color);
		roundRectPath(c, x, y, w, h, r);
		c.fill();
	}

	private void strokeRoundRect(PDPageContentStream c, float x, float y, float w, float h, float r, Color color,
			float width) throws IOException {
		c.setStrokingColor(color);
		c.setLineWidth(width);
		roundRectPath(c, x, y, w, h, r);
		c.stroke();
		c.setLineWidth(1f);
	}

	// ------------------------------------------------------------------ text

	private void writeText(PDPageContentStream c, String text, PDFont font, float size, float x, float y, Color color)
			throws IOException {
		writeText(c, text, font, size, 0f, x, y, color);
	}

	private void writeText(PDPageContentStream c, String text, PDFont font, float size, float charSpacing, float x,
			float y, Color color) throws IOException {
		c.beginText();
		c.setFont(font, size);
		c.setNonStrokingColor(color);
		c.setCharacterSpacing(charSpacing);
		c.newLineAtOffset(x, y);
		c.showText(encodable(text, font));
		c.setCharacterSpacing(0f);
		c.endText();
	}

	private void writeTextRight(PDPageContentStream c, String text, PDFont font, float size, float charSpacing,
			float rightX, float y, Color color) throws IOException {
		float width = textWidth(text, font, size, charSpacing);
		writeText(c, text, font, size, charSpacing, rightX - width, y, color);
	}

	private void writeLines(PDPageContentStream c, List<String> lines, PDFont font, float size, float x, float y,
			float leading, Color color) throws IOException {
		float lineY = y;
		for (String line : lines) {
			writeText(c, line, font, size, x, lineY, color);
			lineY -= leading;
		}
	}

	private float textWidth(String text, PDFont font, float size, float charSpacing) throws IOException {
		String value = encodable(text, font);
		float spacing = value.length() > 1 ? charSpacing * (value.length() - 1) : 0f;
		return font.getStringWidth(value) / 1000f * size + spacing;
	}

	private String ellipsize(String text, PDFont font, float size, float maxWidth) throws IOException {
		String value = encodable(text, font);
		if (textWidth(value, font, size, 0f) <= maxWidth) {
			return value;
		}
		while (value.length() > 1 && textWidth(value + "…", font, size, 0f) > maxWidth) {
			value = value.substring(0, value.length() - 1);
		}
		return value.trim() + "…";
	}

	private List<String> wrapText(String text, PDFont font, float size, float maxWidth) throws IOException {
		List<String> result = new ArrayList<>();
		String normalized = encodable(text, font);
		for (String paragraph : normalized.split("\\r?\\n")) {
			if (paragraph.trim().isEmpty()) {
				continue;
			}
			StringBuilder line = new StringBuilder();
			for (String word : paragraph.trim().split("\\s+")) {
				String candidate = line.length() == 0 ? word : line + " " + word;
				if (textWidth(candidate, font, size, 0f) <= maxWidth) {
					line = new StringBuilder(candidate);
					continue;
				}
				if (line.length() > 0) {
					result.add(line.toString());
				}
				if (textWidth(word, font, size, 0f) <= maxWidth) {
					line = new StringBuilder(word);
				} else {
					List<String> chunks = splitLongWord(word, font, size, maxWidth);
					result.addAll(chunks.subList(0, chunks.size() - 1));
					line = new StringBuilder(chunks.get(chunks.size() - 1));
				}
			}
			if (line.length() > 0) {
				result.add(line.toString());
			}
		}
		if (result.isEmpty()) {
			result.add("-");
		}
		return result;
	}

	private List<String> splitLongWord(String word, PDFont font, float size, float maxWidth) throws IOException {
		List<String> chunks = new ArrayList<>();
		StringBuilder chunk = new StringBuilder();
		for (char character : word.toCharArray()) {
			if (chunk.length() > 0 && textWidth(chunk.toString() + character, font, size, 0f) > maxWidth) {
				chunks.add(chunk.toString());
				chunk = new StringBuilder();
			}
			chunk.append(character);
		}
		if (chunk.length() > 0) {
			chunks.add(chunk.toString());
		}
		return chunks;
	}

	/**
	 * The standard PDF fonts only cover WinAnsi (Latin-1 plus a few symbols). Typographic quotes and
	 * dashes are mapped; any other character the font cannot encode becomes "?" instead of making
	 * PDFBox throw and the whole download fail.
	 */
	private String encodable(String value, PDFont font) {
		if (value == null) {
			return "";
		}
		String normalized = value.replace('’', '\'').replace('‘', '\'').replace('–', '-')
				.replace('—', '-').replace('•', '-').replace("\t", "    ").replace(' ', ' ');
		StringBuilder out = new StringBuilder(normalized.length());
		for (int i = 0; i < normalized.length(); i++) {
			char ch = normalized.charAt(i);
			if (ch == '\n' || ch == '\r') {
				out.append(ch);
				continue;
			}
			try {
				font.encode(String.valueOf(ch));
				out.append(ch);
			} catch (IllegalArgumentException | IOException e) {
				out.append('?');
			}
		}
		return out.toString();
	}

	// ------------------------------------------------------------------ data helpers

	private String doctorName(UserEntity doctor) {
		return joinNonEmpty(" ", doctor.getPrenom(), doctor.getNom());
	}

	private String fullName(PatientEntity patient) {
		if (patient == null) {
			return "-";
		}
		String name = joinNonEmpty(" ", patient.getPrenom(), patient.getNom());
		return name.isEmpty() ? "-" : name;
	}

	private String reference(PdfState state) {
		String date = new SimpleDateFormat("yyyyMMdd", Locale.FRANCE).format(state.issueDate);
		long patientId = state.patient == null ? 0 : state.patient.getId();
		return state.kind.refPrefix + "-" + date + "-" + patientId;
	}

	private String labelled(String label, String value) {
		return isBlank(value) ? null : label + " : " + value.trim();
	}

	private String joinNonEmpty(String separator, String... values) {
		StringBuilder out = new StringBuilder();
		for (String value : values) {
			if (isBlank(value)) {
				continue;
			}
			if (out.length() > 0) {
				out.append(separator);
			}
			out.append(value.trim());
		}
		return out.toString();
	}

	private Integer ageInYears(Date birth, Date at) {
		if (birth == null) {
			return null;
		}
		Calendar b = Calendar.getInstance();
		b.setTime(birth);
		Calendar now = Calendar.getInstance();
		now.setTime(at);
		int years = now.get(Calendar.YEAR) - b.get(Calendar.YEAR);
		if (now.get(Calendar.DAY_OF_YEAR) < b.get(Calendar.DAY_OF_YEAR)) {
			years--;
		}
		return years >= 0 && years < 130 ? years : null;
	}

	private String safe(String value) {
		return isBlank(value) ? "-" : value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private boolean hasAnyValue(String... values) {
		for (String value : values) {
			if (!isBlank(value)) {
				return true;
			}
		}
		return false;
	}

	private String formatDate(Date date) {
		return date == null ? "-" : new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(date);
	}

	private static class PdfState {
		private final PDDocument document;
		private final DocKind kind;
		private final UserEntity doctor;
		private final PatientEntity patient;
		private final Date issueDate;
		private PDPageContentStream content;
		private float y;

		private PdfState(PDDocument document, DocKind kind, UserEntity doctor, PatientEntity patient, Date issueDate) {
			this.document = document;
			this.kind = kind;
			this.doctor = doctor;
			this.patient = patient;
			this.issueDate = issueDate;
		}

		private void closeStream() throws IOException {
			if (content != null) {
				content.close();
				content = null;
			}
		}
	}
}
