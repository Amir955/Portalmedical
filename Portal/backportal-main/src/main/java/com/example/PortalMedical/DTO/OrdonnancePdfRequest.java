package com.example.PortalMedical.DTO;

import java.util.ArrayList;
import java.util.List;

public class OrdonnancePdfRequest {

	private String diagnostic;
	private String antecedents;
	private String allergies;
	private String consigne;
	private String suivi;
	private List<OrdonnancePdfLine> lines = new ArrayList<>();

	public String getDiagnostic() {
		return diagnostic;
	}

	public void setDiagnostic(String diagnostic) {
		this.diagnostic = diagnostic;
	}

	public String getAntecedents() {
		return antecedents;
	}

	public void setAntecedents(String antecedents) {
		this.antecedents = antecedents;
	}

	public String getAllergies() {
		return allergies;
	}

	public void setAllergies(String allergies) {
		this.allergies = allergies;
	}

	public String getConsigne() {
		return consigne;
	}

	public void setConsigne(String consigne) {
		this.consigne = consigne;
	}

	public String getSuivi() {
		return suivi;
	}

	public void setSuivi(String suivi) {
		this.suivi = suivi;
	}

	public List<OrdonnancePdfLine> getLines() {
		return lines;
	}

	public void setLines(List<OrdonnancePdfLine> lines) {
		this.lines = lines;
	}
}
