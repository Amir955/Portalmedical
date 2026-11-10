package com.example.PortalMedical.DTO;

/**
 * Content typed by the doctor for a certificate or a letter. Each document uses only its own fields:
 * certificat médical (repos, note), certificat d'aptitude (profession, etatSante, note),
 * lettre à un confrère (recipient, subject, resume, request).
 */
public class DocumentPdfRequest {

	private String repos;
	private String note;
	private String profession;
	private String etatSante;
	private String recipient;
	private String subject;
	private String resume;
	private String request;

	public String getRepos() {
		return repos;
	}

	public void setRepos(String repos) {
		this.repos = repos;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getProfession() {
		return profession;
	}

	public void setProfession(String profession) {
		this.profession = profession;
	}

	public String getEtatSante() {
		return etatSante;
	}

	public void setEtatSante(String etatSante) {
		this.etatSante = etatSante;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}
}
