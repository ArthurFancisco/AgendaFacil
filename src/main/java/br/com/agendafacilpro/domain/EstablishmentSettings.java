package br.com.agendafacilpro.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "establishment_settings")
public class EstablishmentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    private Establishment establishment;

    private boolean newClientRequiresApproval = true;
    private int pendingExpirationMinutes = 60;
    private int maxFutureAppointmentsPerPhone = 3;
    private int maxAttemptsPerPhoneHour = 3;
    private int maxAttemptsPerIpHour = 10;
    private int noShowCountForManualApproval = 2;
    private int noShowCountForBlock = 3;
    private int minHoursBeforeClientCancel = 24;
    private int longServiceManualApprovalMinutes = 120;
    private boolean showPricesOnPublicPage = true;
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static EstablishmentSettings defaultsFor(Establishment establishment) {
        EstablishmentSettings settings = new EstablishmentSettings();
        settings.setEstablishment(establishment);
        return settings;
    }

    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Establishment getEstablishment() {
        return establishment;
    }

    public void setEstablishment(Establishment establishment) {
        this.establishment = establishment;
    }

    public boolean isNewClientRequiresApproval() {
        return newClientRequiresApproval;
    }

    public void setNewClientRequiresApproval(boolean newClientRequiresApproval) {
        this.newClientRequiresApproval = newClientRequiresApproval;
    }

    public int getPendingExpirationMinutes() {
        return pendingExpirationMinutes;
    }

    public void setPendingExpirationMinutes(int pendingExpirationMinutes) {
        this.pendingExpirationMinutes = pendingExpirationMinutes;
    }

    public int getMaxFutureAppointmentsPerPhone() {
        return maxFutureAppointmentsPerPhone;
    }

    public void setMaxFutureAppointmentsPerPhone(int maxFutureAppointmentsPerPhone) {
        this.maxFutureAppointmentsPerPhone = maxFutureAppointmentsPerPhone;
    }

    public int getMaxAttemptsPerPhoneHour() {
        return maxAttemptsPerPhoneHour;
    }

    public void setMaxAttemptsPerPhoneHour(int maxAttemptsPerPhoneHour) {
        this.maxAttemptsPerPhoneHour = maxAttemptsPerPhoneHour;
    }

    public int getMaxAttemptsPerIpHour() {
        return maxAttemptsPerIpHour;
    }

    public void setMaxAttemptsPerIpHour(int maxAttemptsPerIpHour) {
        this.maxAttemptsPerIpHour = maxAttemptsPerIpHour;
    }

    public int getNoShowCountForManualApproval() {
        return noShowCountForManualApproval;
    }

    public void setNoShowCountForManualApproval(int noShowCountForManualApproval) {
        this.noShowCountForManualApproval = noShowCountForManualApproval;
    }

    public int getNoShowCountForBlock() {
        return noShowCountForBlock;
    }

    public void setNoShowCountForBlock(int noShowCountForBlock) {
        this.noShowCountForBlock = noShowCountForBlock;
    }

    public int getMinHoursBeforeClientCancel() {
        return minHoursBeforeClientCancel;
    }

    public void setMinHoursBeforeClientCancel(int minHoursBeforeClientCancel) {
        this.minHoursBeforeClientCancel = minHoursBeforeClientCancel;
    }

    public int getLongServiceManualApprovalMinutes() {
        return longServiceManualApprovalMinutes;
    }

    public void setLongServiceManualApprovalMinutes(int longServiceManualApprovalMinutes) {
        this.longServiceManualApprovalMinutes = longServiceManualApprovalMinutes;
    }

    public boolean isShowPricesOnPublicPage() {
        return showPricesOnPublicPage;
    }

    public void setShowPricesOnPublicPage(boolean showPricesOnPublicPage) {
        this.showPricesOnPublicPage = showPricesOnPublicPage;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
