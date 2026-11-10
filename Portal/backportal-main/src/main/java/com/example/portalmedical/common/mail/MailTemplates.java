package com.example.portalmedical.common.mail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.web.util.HtmlUtils;

/**
 * HTML and plain-text bodies of the e-mails sent by PortalMedical. Every e-mail shares
 * one layout (brand header, white card, footer) built with tables and inline styles,
 * which is what mail clients (Gmail, Outlook, mobile apps) render reliably.
 * All dynamic values are HTML-escaped here.
 */
public final class MailTemplates {

    /** Front-end base URL used in links. */
    public static final String APP_URL = "http://localhost:4200";

    private static final String PRIMARY = "#1f3a5f";
    private static final String ACCENT = "#20c5b3";
    private static final String ACCENT_STRONG = "#117d69";
    private static final String TEXT = "#41556b";
    private static final String MUTED = "#7a8a9c";
    private static final String BACKGROUND = "#f2f5f8";
    private static final String BORDER = "#e3e9ef";
    private static final String DANGER = "#b43434";

    private static final String FONT = "font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;";

    /** An e-mail ready to send: subject, HTML body and plain-text alternative. */
    public static final class Mail {
        public final String subject;
        public final String html;
        public final String text;

        Mail(String subject, String html, String text) {
            this.subject = subject;
            this.html = html;
            this.text = text;
        }
    }

    private MailTemplates() {
    }

    // ------------------------------------------------------------------ templates

    public static Mail passwordReset(String prenom, String link, int validMinutes) {
        String subject = "Réinitialisation de votre mot de passe PortalMedical";

        String body = paragraph(greeting(prenom, true))
                + paragraph("Nous avons reçu une demande de réinitialisation du mot de passe de votre compte "
                        + "PortalMedical. Cliquez sur le bouton ci-dessous pour en choisir un nouveau.")
                + button("Choisir un nouveau mot de passe", link, ACCENT_STRONG)
                + notice("Ce lien est valable <strong>" + validMinutes + " minutes</strong>. "
                        + "Passé ce délai, il vous suffira d'en demander un nouveau depuis la page de connexion.")
                + paragraph("Vous n'êtes pas à l'origine de cette demande ? Ignorez simplement cet e-mail : "
                        + "votre mot de passe actuel reste inchangé.")
                + fallbackLink(link);

        String html = layout("Choisissez un nouveau mot de passe (lien valable " + validMinutes + " minutes).",
                "lock", "Réinitialisation du mot de passe", body);

        String text = greeting(prenom, false) + "\n\n"
                + "Nous avons reçu une demande de réinitialisation du mot de passe de votre compte PortalMedical.\n"
                + "Pour choisir un nouveau mot de passe, ouvrez ce lien :\n" + link + "\n\n"
                + "Ce lien est valable " + validMinutes + " minutes.\n\n"
                + "Vous n'êtes pas à l'origine de cette demande ? Ignorez simplement cet e-mail : "
                + "votre mot de passe actuel reste inchangé.\n\n"
                + signatureText();

        return new Mail(subject, html, text);
    }

    public static Mail secretaryInvitation(String prenom, String doctor, String email, String link, int validDays) {
        String who = doctor == null ? "Votre médecin" : doctor;
        String subject = who + " vous invite sur PortalMedical";

        String body = paragraph(greeting(prenom, true))
                + paragraph("<strong>" + esc(who) + "</strong> vous a créé un compte secrétaire sur PortalMedical. "
                        + "Vous pourrez gérer son agenda et répondre aux demandes de rendez-vous de ses patients.")
                + paragraph("Pour activer votre compte, choisissez votre mot de passe :")
                + button("Activer mon compte", link, ACCENT_STRONG)
                + notice("Votre identifiant de connexion est <strong>" + esc(email) + "</strong>. "
                        + "Ce lien est valable <strong>" + validDays + " jours</strong> ; passé ce délai, utilisez "
                        + "« Mot de passe oublié ? » sur la page de connexion.")
                + paragraph("Vous ne vous attendiez pas à cette invitation ? Ignorez simplement cet e-mail.")
                + fallbackLink(link);

        String html = layout("Activez votre compte secrétaire en choisissant votre mot de passe.",
                "check", "Bienvenue dans l'équipe", body);

        String text = greeting(prenom, false) + "\n\n"
                + who + " vous a créé un compte secrétaire sur PortalMedical. Vous pourrez gérer son agenda "
                + "et répondre aux demandes de rendez-vous de ses patients.\n\n"
                + "Pour activer votre compte, choisissez votre mot de passe :\n" + link + "\n\n"
                + "Votre identifiant de connexion est " + email + ". Ce lien est valable " + validDays + " jours ; "
                + "passé ce délai, utilisez « Mot de passe oublié ? » sur la page de connexion.\n\n"
                + "Vous ne vous attendiez pas à cette invitation ? Ignorez simplement cet e-mail.\n\n"
                + signatureText();

        return new Mail(subject, html, text);
    }

    public static Mail appointmentConfirmed(String patientPrenom, Date date, String doctor, String doctorPhone,
            String cancelLink) {
        String day = formatDay(date);
        String time = formatTime(date);
        String subject = "Rendez-vous confirmé le " + day + " à " + time;

        String body = paragraph(greeting(patientPrenom, true))
                + paragraph("Bonne nouvelle : votre rendez-vous est <strong>confirmé</strong>. "
                        + "En voici le récapitulatif.")
                + detailsCard(day, time, doctor, "Confirmé", ACCENT_STRONG)
                + sectionTitle("Pour bien préparer votre venue")
                + list("Présentez-vous 10 minutes avant l'heure du rendez-vous.",
                        "Munissez-vous de votre pièce d'identité et, le cas échéant, de votre carte d'assurance maladie.",
                        "Apportez vos ordonnances en cours et vos derniers résultats d'analyses ou examens.")
                + cancelBlock(doctorPhone, cancelLink);

        String html = layout("Votre rendez-vous du " + day + " à " + time + " est confirmé.",
                "check", "Votre rendez-vous est confirmé", body);

        String text = greeting(patientPrenom, false) + "\n\n"
                + "Votre rendez-vous est confirmé.\n\n"
                + "Date : " + day + "\n"
                + "Heure : " + time + "\n"
                + (doctor == null ? "" : "Médecin : " + doctor + "\n")
                + "\nPour bien préparer votre venue :\n"
                + "- présentez-vous 10 minutes avant l'heure du rendez-vous ;\n"
                + "- munissez-vous de votre pièce d'identité et, le cas échéant, de votre carte d'assurance maladie ;\n"
                + "- apportez vos ordonnances en cours et vos derniers résultats d'analyses ou examens.\n\n"
                + cancelText(doctorPhone, cancelLink) + "\n\n"
                + signatureText();

        return new Mail(subject, html, text);
    }

    /** Sent the evening before a confirmed appointment. */
    public static Mail appointmentReminder(String patientPrenom, Date date, String doctor, String doctorPhone,
            String cancelLink) {
        String day = formatDay(date);
        String time = formatTime(date);
        String subject = "Rappel : votre rendez-vous demain à " + time;

        String body = paragraph(greeting(patientPrenom, true))
                + paragraph("Petit rappel : vous avez rendez-vous <strong>demain à " + esc(time) + "</strong>.")
                + detailsCard(day, time, doctor, "Demain", ACCENT_STRONG)
                + list("Présentez-vous 10 minutes avant l'heure du rendez-vous.",
                        "Pensez à votre pièce d'identité et à vos derniers documents médicaux.")
                + cancelBlock(doctorPhone, cancelLink);

        String html = layout("Rappel : rendez-vous demain à " + time + ".", "info-soft", "Votre rendez-vous de demain", body);

        String text = greeting(patientPrenom, false) + "\n\n"
                + "Petit rappel : vous avez rendez-vous demain à " + time + ".\n\n"
                + "Date : " + day + "\n"
                + "Heure : " + time + "\n"
                + (doctor == null ? "" : "Médecin : " + doctor + "\n")
                + "\nPrésentez-vous 10 minutes avant l'heure du rendez-vous, avec votre pièce d'identité "
                + "et vos derniers documents médicaux.\n\n"
                + cancelText(doctorPhone, cancelLink) + "\n\n"
                + signatureText();

        return new Mail(subject, html, text);
    }

    /** Tells the doctor that a patient cancelled from the link in their e-mail. */
    public static Mail patientCancelledNotice(String doctorPrenom, String patientName, Date date) {
        String day = formatDay(date);
        String time = formatTime(date);
        String who = hasText(patientName) ? patientName : "Un patient";
        String subject = "Rendez-vous annulé : " + who + ", " + day + " à " + time;

        String body = paragraph(greeting(doctorPrenom, true))
                + paragraph("<strong>" + esc(who) + "</strong> a annulé son rendez-vous depuis le lien reçu par e-mail.")
                + detailsCard(day, time, null, "Annulé par le patient", DANGER)
                + paragraph("Le créneau est de nouveau disponible pour une autre demande.")
                + button("Ouvrir le calendrier", APP_URL + "/agenda", ACCENT_STRONG);

        String html = layout(who + " a annulé son rendez-vous du " + day + " à " + time + ".",
                "info", "Rendez-vous annulé", body);

        String text = greeting(doctorPrenom, false) + "\n\n"
                + who + " a annulé son rendez-vous du " + day + " à " + time
                + " depuis le lien reçu par e-mail.\n"
                + "Le créneau est de nouveau disponible.\n\n"
                + signatureText();

        return new Mail(subject, html, text);
    }

    public static Mail appointmentRefused(String patientPrenom, Date date, String doctor, String doctorPhone) {
        String day = formatDay(date);
        String time = formatTime(date);
        String requestLink = APP_URL + "/prendre-rendez-vous";
        String subject = "Votre demande de rendez-vous du " + day + " n'a pas pu être retenue";

        String body = paragraph(greeting(patientPrenom, true))
                + paragraph("Nous sommes désolés : le créneau que vous avez demandé n'est malheureusement "
                        + "<strong>pas disponible</strong>, et votre demande n'a pas pu être retenue.")
                + detailsCard(day, time, doctor, "Non retenu", DANGER)
                + paragraph("Vous pouvez dès maintenant choisir un autre créneau en ligne :")
                + button("Demander un autre rendez-vous", requestLink, ACCENT_STRONG)
                + (hasText(doctorPhone)
                        ? paragraph(contactLine(doctorPhone, "Vous pouvez aussi joindre directement le cabinet"))
                        : "")
                + paragraph("Merci de votre compréhension.");

        String html = layout("Le créneau du " + day + " à " + time + " n'est pas disponible. Choisissez-en un autre.",
                "info", "Demande de rendez-vous non retenue", body);

        String text = greeting(patientPrenom, false) + "\n\n"
                + "Nous sommes désolés : le créneau que vous avez demandé n'est malheureusement pas disponible, "
                + "et votre demande n'a pas pu être retenue.\n\n"
                + "Créneau demandé : " + day + " à " + time + "\n"
                + (doctor == null ? "" : "Médecin : " + doctor + "\n")
                + "\nVous pouvez choisir un autre créneau en ligne :\n" + requestLink + "\n\n"
                + (hasText(doctorPhone)
                        ? contactLineText(doctorPhone, "Vous pouvez aussi joindre directement le cabinet") + "\n\n"
                        : "")
                + "Merci de votre compréhension.\n\n"
                + signatureText();

        return new Mail(subject, html, text);
    }

    // ------------------------------------------------------------------ helpers

    /** "Dr Prénom Nom", or null when the doctor is unknown. */
    public static String doctorName(String prenom, String nom) {
        String full = ((prenom == null ? "" : prenom.trim()) + " " + (nom == null ? "" : nom.trim())).trim();
        return full.isEmpty() ? null : "Dr " + full;
    }

    static String formatDay(Date date) {
        if (date == null) {
            return "date à préciser";
        }
        return new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(date);
    }

    static String formatTime(Date date) {
        if (date == null) {
            return "heure à préciser";
        }
        return new SimpleDateFormat("HH'h'mm", Locale.FRENCH).format(date);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String esc(String value) {
        return value == null ? "" : HtmlUtils.htmlEscape(value, "UTF-8");
    }

    private static String greeting(String prenom, boolean html) {
        String name = prenom == null ? "" : prenom.trim();
        if (name.isEmpty()) {
            return "Bonjour,";
        }
        return "Bonjour " + (html ? esc(name) : name) + ",";
    }

    private static String contactLine(String phone, String lead) {
        if (phone == null || phone.trim().isEmpty()) {
            return lead + ".";
        }
        String clean = phone.trim();
        return lead + " au <a href=\"tel:" + esc(clean.replace(" ", "")) + "\" style=\"color:" + ACCENT_STRONG
                + ";font-weight:600;text-decoration:none;\">" + esc(clean) + "</a>.";
    }

    private static String contactLineText(String phone, String lead) {
        if (phone == null || phone.trim().isEmpty()) {
            return lead + ".";
        }
        return lead + " au " + phone.trim() + ".";
    }

    private static String signatureText() {
        return "Bien cordialement,\nL'équipe PortalMedical\n\n"
                + "Cet e-mail a été envoyé automatiquement, merci de ne pas y répondre.";
    }

    // ------------------------------------------------------------------ building blocks

    private static String paragraph(String html) {
        return "<p style=\"margin:0 0 16px;" + FONT + "font-size:15px;line-height:1.65;color:" + TEXT + ";\">"
                + html + "</p>";
    }

    private static String sectionTitle(String title) {
        return "<p style=\"margin:8px 0 10px;" + FONT + "font-size:15px;font-weight:700;color:" + PRIMARY + ";\">"
                + title + "</p>";
    }

    private static String list(String... items) {
        StringBuilder rows = new StringBuilder();
        for (String item : items) {
            rows.append("<tr><td valign=\"top\" style=\"padding:0 10px 8px 0;" + FONT + "font-size:15px;line-height:1.6;color:")
                    .append(ACCENT_STRONG).append(";\">&#10003;</td>")
                    .append("<td style=\"padding:0 0 8px;" + FONT + "font-size:14px;line-height:1.6;color:")
                    .append(TEXT).append(";\">").append(item).append("</td></tr>");
        }
        return "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 0 16px;\">"
                + rows + "</table>";
    }

    private static String button(String label, String href, String color) {
        return "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:8px 0 24px;\"><tr>"
                + "<td style=\"border-radius:10px;background:" + color + ";\">"
                + "<a href=\"" + esc(href) + "\" target=\"_blank\" style=\"display:inline-block;padding:14px 28px;"
                + FONT + "font-size:15px;font-weight:700;color:#ffffff;text-decoration:none;border-radius:10px;\">"
                + label + "</a></td></tr></table>";
    }

    /** "Un empêchement ?" line, with a cancel button when the appointment has a cancel link. */
    private static String cancelBlock(String doctorPhone, String cancelLink) {
        if (!hasText(cancelLink)) {
            return paragraph(contactLine(doctorPhone, "Un empêchement ? Merci de prévenir le cabinet au plus tôt"));
        }
        return paragraph("Un empêchement ? Annulez en un clic : le créneau sera libéré pour un autre patient.")
                + outlineButton("Annuler mon rendez-vous", cancelLink)
                + (hasText(doctorPhone) ? paragraph(contactLine(doctorPhone, "Vous pouvez aussi appeler le cabinet")) : "");
    }

    private static String cancelText(String doctorPhone, String cancelLink) {
        if (!hasText(cancelLink)) {
            return contactLineText(doctorPhone, "Un empêchement ? Merci de prévenir le cabinet au plus tôt");
        }
        return "Un empêchement ? Annulez votre rendez-vous ici : " + cancelLink
                + (hasText(doctorPhone) ? "\n" + contactLineText(doctorPhone, "Vous pouvez aussi appeler le cabinet") : "");
    }

    private static String outlineButton(String label, String href) {
        return "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 0 20px;\"><tr>"
                + "<td style=\"border-radius:10px;border:1px solid " + BORDER + ";background:#ffffff;\">"
                + "<a href=\"" + esc(href) + "\" target=\"_blank\" style=\"display:inline-block;padding:11px 22px;"
                + FONT + "font-size:14px;font-weight:700;color:" + DANGER + ";text-decoration:none;border-radius:10px;\">"
                + label + "</a></td></tr></table>";
    }

    private static String notice(String html) {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 0 20px;\"><tr>"
                + "<td style=\"padding:12px 16px;border-left:4px solid " + ACCENT + ";background:#eefaf8;border-radius:6px;"
                + FONT + "font-size:14px;line-height:1.6;color:" + TEXT + ";\">" + html + "</td></tr></table>";
    }

    private static String fallbackLink(String link) {
        return "<p style=\"margin:8px 0 0;padding-top:16px;border-top:1px solid " + BORDER + ";" + FONT
                + "font-size:12px;line-height:1.6;color:" + MUTED + ";\">"
                + "Le bouton ne fonctionne pas ? Copiez ce lien dans votre navigateur :<br>"
                + "<a href=\"" + esc(link) + "\" style=\"color:" + ACCENT_STRONG + ";word-break:break-all;\">"
                + esc(link) + "</a></p>";
    }

    private static String detailRow(String label, String value, boolean last) {
        String border = last ? "" : "border-bottom:1px solid " + BORDER + ";";
        return "<tr><td style=\"padding:12px 16px;" + border + FONT + "font-size:13px;color:" + MUTED + ";width:35%;\">"
                + label + "</td><td style=\"padding:12px 16px;" + border + FONT + "font-size:15px;font-weight:600;color:"
                + PRIMARY + ";\">" + value + "</td></tr>";
    }

    private static String detailsCard(String day, String time, String doctor, String status, String statusColor) {
        String statusBadge = "<span style=\"display:inline-block;padding:3px 10px;border-radius:999px;background:"
                + statusColor + ";color:#ffffff;font-size:12px;font-weight:700;\">" + status + "</span>";
        StringBuilder rows = new StringBuilder()
                .append(detailRow("Date", esc(capitalize(day)), false))
                .append(detailRow("Heure", esc(time), false));
        if (doctor != null) {
            rows.append(detailRow("Médecin", esc(doctor), false));
        }
        rows.append(detailRow("Statut", statusBadge, true));
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:4px 0 24px;"
                + "border:1px solid " + BORDER + ";border-radius:12px;border-collapse:separate;background:#fafcfd;\">"
                + rows + "</table>";
    }

    private static String capitalize(String value) {
        return value == null || value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static String icon(String kind) {
        String symbol;
        String color = ACCENT_STRONG;
        String background = "#e6f7f4";
        switch (kind) {
            case "check":
                symbol = "&#10003;";
                break;
            case "info":
                symbol = "i";
                color = DANGER;
                background = "#fbecec";
                break;
            case "info-soft":
                symbol = "J-1";
                color = PRIMARY;
                background = "#e8eef6";
                break;
            default:
                symbol = "&#8635;";
        }
        return "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 0 18px;\"><tr>"
                + "<td align=\"center\" valign=\"middle\" width=\"52\" height=\"52\" style=\"width:52px;height:52px;"
                + "border-radius:50%;background:" + background + ";color:" + color + ";" + FONT
                + "font-size:24px;font-weight:700;line-height:52px;\">" + symbol + "</td></tr></table>";
    }

    /** Shared frame of every e-mail. */
    private static String layout(String preheader, String iconKind, String title, String body) {
        return "<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<meta name=\"color-scheme\" content=\"light\"><title>" + esc(title) + "</title></head>"
                + "<body style=\"margin:0;padding:0;background:" + BACKGROUND + ";\">"
                // Preview text shown next to the subject in the inbox, hidden in the message.
                + "<div style=\"display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;\">"
                + esc(preheader) + "</div>"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:"
                + BACKGROUND + ";\"><tr><td align=\"center\" style=\"padding:32px 12px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:560px;\">"
                // Brand
                + "<tr><td style=\"padding:0 8px 18px;" + FONT + "\">"
                + "<span style=\"font-size:22px;font-weight:800;color:" + PRIMARY + ";\">Portal</span>"
                + "<span style=\"font-size:22px;font-weight:800;color:" + ACCENT + ";\">Medical</span>"
                + "<span style=\"display:block;font-size:12px;color:" + MUTED + ";\">Accessible. Sécurisé. Humain.</span>"
                + "</td></tr>"
                // Card
                + "<tr><td style=\"background:#ffffff;border-radius:16px;border-top:4px solid " + ACCENT + ";"
                + "padding:32px 32px 20px;box-shadow:0 8px 24px rgba(31,58,95,0.08);\">"
                + icon(iconKind)
                + "<h1 style=\"margin:0 0 20px;" + FONT + "font-size:22px;line-height:1.3;font-weight:700;color:"
                + PRIMARY + ";\">" + esc(title) + "</h1>"
                + body
                + "<p style=\"margin:8px 0 0;" + FONT + "font-size:15px;line-height:1.6;color:" + TEXT + ";\">"
                + "Bien cordialement,<br><strong style=\"color:" + PRIMARY + ";\">L'équipe PortalMedical</strong></p>"
                + "</td></tr>"
                // Footer
                + "<tr><td align=\"center\" style=\"padding:20px 16px 0;" + FONT + "font-size:12px;line-height:1.6;color:"
                + MUTED + ";\">Cet e-mail a été envoyé automatiquement par PortalMedical, merci de ne pas y répondre.</td></tr>"
                + "</table></td></tr></table></body></html>";
    }
}
