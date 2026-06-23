package br.com.agendafacilpro.service;

public record EstablishmentSettingsForm(
        boolean newClientRequiresApproval,
        int pendingExpirationMinutes,
        int maxFutureAppointmentsPerPhone,
        int maxAttemptsPerPhoneHour,
        int maxAttemptsPerIpHour,
        int noShowCountForManualApproval,
        int noShowCountForBlock,
        int minHoursBeforeClientCancel,
        int longServiceManualApprovalMinutes,
        boolean showPricesOnPublicPage
) {
}
