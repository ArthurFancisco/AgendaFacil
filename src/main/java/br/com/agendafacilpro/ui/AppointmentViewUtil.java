package br.com.agendafacilpro.ui;

import org.springframework.stereotype.Component;

import br.com.agendafacilpro.domain.AppointmentStatus;

@Component("view")
public class AppointmentViewUtil {

    public String statusLabel(AppointmentStatus s) {
        if (s == null) {
            return "Não informado";
        }
        return switch (s) {
            case PENDING_APPROVAL ->
                "Pendente";
            case CONFIRMED ->
                "Confirmado";
            case COMPLETED ->
                "Concluído";
            case NO_SHOW ->
                "Faltou";
            case CANCELLED ->
                "Cancelado";
            case EXPIRED ->
                "Expirado";
        };
    }

    public String badgeClass(AppointmentStatus s) {
        if (s == null) {
            return "badge muted";
        }
        return "badge " + switch (s) {
            case PENDING_APPROVAL ->
                "pending";
            case CONFIRMED ->
                "confirmed";
            case COMPLETED ->
                "completed";
            case NO_SHOW ->
                "noshow";
            case CANCELLED ->
                "cancelled";
            case EXPIRED ->
                "expired";
        };
    }

    public String rowClass(AppointmentStatus s) {
        return s == null ? "" : "row-" + s.name().toLowerCase().replace("_", "-");
    }

    public boolean canApprove(AppointmentStatus s) {
        return s == AppointmentStatus.PENDING_APPROVAL;
    }

    public boolean canReject(AppointmentStatus s) {
        return s == AppointmentStatus.PENDING_APPROVAL;
    }

    public boolean canComplete(AppointmentStatus s) {
        return s == AppointmentStatus.CONFIRMED;
    }

    public boolean canNoShow(AppointmentStatus s) {
        return s == AppointmentStatus.CONFIRMED;
    }

    public boolean canCancel(AppointmentStatus s) {
        return s == AppointmentStatus.CONFIRMED;
    }

    public String phone(String raw) {
        if (raw == null) {
            return "-";
        }
        String p = raw.replaceAll("\\D", "");
        if (p.length() == 11) {
            return "(" + p.substring(0, 2) + ") " + p.substring(2, 7) + "-" + p.substring(7);
        }
        if (p.length() == 10) {
            return "(" + p.substring(0, 2) + ") " + p.substring(2, 6) + "-" + p.substring(6);
        }
        return raw;
    }
}
