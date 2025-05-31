import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Device implements Serializable {
    private String identifier;
    private String issueDescription;
    private LocalDate entryDate;
    private String owner;
    private String ownerEmail;
    private String ownerPhone;
    private List<ActivityRecord> activityLog;
    private DeviceState currentState;
    private String technicalAnalysis;
    private String repairWork;
    private String technicianId;

    public Device(String identifier, String issueDescription, LocalDate entryDate,
            String owner, String ownerEmail, String ownerPhone) {
        this.identifier = identifier;
        this.issueDescription = issueDescription;
        this.entryDate = entryDate;
        this.owner = owner;
        this.ownerEmail = ownerEmail;
        this.ownerPhone = ownerPhone;
        this.activityLog = new ArrayList<>();
        this.currentState = DeviceState.RECEIVED;
        recordActivity("Equipo recibido en el sistema: " + issueDescription);
    }

    public void recordActivity(String description) {
        activityLog.add(new ActivityRecord(LocalDate.now(), description, currentState));
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getOwner() {
        return owner;
    }

    public DeviceState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(DeviceState state) {
        this.currentState = state;
    }

    public String getTechnicalAnalysis() {
        return technicalAnalysis;
    }

    public void setTechnicalAnalysis(String technicalAnalysis) {
        this.technicalAnalysis = technicalAnalysis;
    }

    public String getRepairWork() {
        return repairWork;
    }

    public void setRepairWork(String repairWork) {
        this.repairWork = repairWork;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    public List<ActivityRecord> getActivityLog() {
        return activityLog;
    }

    @Override
    public String toString() {
        return "🔢 Número de serie: " + identifier +
                "\n👤 Propietario: " + owner +
                "\n📊 Estado actual: " + currentState +
                "\n📅 Fecha de ingreso: " + entryDate +
                "\n🔧 Descripción del problema: " + issueDescription +
                "\n📞 Contacto: " + ownerEmail + " / " + ownerPhone;
    }

    public String getCompleteDetails() {
        StringBuilder details = new StringBuilder();
        details.append(toString()).append("\n");

        if (technicalAnalysis != null && !technicalAnalysis.isEmpty()) {
            details.append("🔍 Análisis técnico: ").append(technicalAnalysis).append("\n");
        }

        if (repairWork != null && !repairWork.isEmpty()) {
            details.append("🛠️ Trabajo realizado: ").append(repairWork).append("\n");
            details.append("👨‍🔧 Técnico asignado: ").append(technicianId).append("\n");
        }

        details.append("\n📜 Registro de actividades:\n");
        for (ActivityRecord record : activityLog) {
            details.append("   ").append(record).append("\n");
        }

        return details.toString();
    }
}

enum DeviceState {
    RECEIVED("📥 Recibido"),
    UNDER_EVALUATION("🔍 En Evaluación"),
    IN_REPAIR("🛠️ En Reparación"),
    QUALITY_CHECK("✅ Control de Calidad"),
    READY_DELIVERY("📦 Listo para Entrega");

    private String statusDescription;

    DeviceState(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    @Override
    public String toString() {
        return statusDescription;
    }
}

class ActivityRecord implements Serializable{
    private LocalDate timestamp;
    private String description;
    private DeviceState deviceState;

    public ActivityRecord(LocalDate timestamp, String description, DeviceState deviceState) {
        this.timestamp = timestamp;
        this.description = description;
        this.deviceState = deviceState;
    }

    @Override
    public String toString() {
        return "📅 " + timestamp + " - [" + deviceState + "] " + description;
    }
}