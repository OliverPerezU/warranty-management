import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class App {
    private Map<DeviceState, DeviceQueue> workflows;
    private Scanner input;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{8}$");

    public App() {
        this.workflows = DataManager.loadSystemData();
        this.input = new Scanner(System.in);
    }

    public void execute() {
        boolean active = true;

        while (active) {
            clearConsole();
            showMainInterface();

            try {
                int choice = getIntegerInput("Ingrese su elección: ");

                switch (choice) {
                    case 1:
                        viewSystemQueues();
                        break;
                    case 2:
                        addNewDevice();
                        break;
                    case 3:
                        handleDeviceExamination();
                        break;
                    case 4:
                        viewCompleteHistory();
                        break;
                    case 5:
                        handleTechnicalRepair();
                        break;
                    case 6:
                        handleQualityVerification();
                        break;
                    case 7:
                        handleCustomerDelivery();
                        break;
                    case 8:
                        removeDeviceRecord();
                        break;
                    case 0:
                        active = false;
                        DataManager.saveSystemData(workflows);
                        System.out.println("╔════════════════════════════════════════╗");
                        System.out.println("║     Sistema cerrado exitosamente      ║");
                        System.out.println("╚════════════════════════════════════════╝");
                        break;
                    default:
                        System.out.println("❌ Selección inválida. Intente nuevamente.");
                        pauseExecution();
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Error: Debe ingresar un número válido.");
                pauseExecution();
            }
        }
    }

    private void showMainInterface() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║              🔧 SISTEMA DE SOPORTE              ║");
        System.out.println("║            TÉCNICO COMPUTACIONAL 🔧             ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  1️⃣  ► Consultar estado de colas                ║");
        System.out.println("║  2️⃣  ► Ingresar nuevo equipo                    ║");
        System.out.println("║  3️⃣  ► Realizar evaluación técnica              ║");
        System.out.println("║  4️⃣  ► Ver registro histórico                   ║");
        System.out.println("║  5️⃣  ► Procesar reparación                      ║");
        System.out.println("║  6️⃣  ► Control de calidad                       ║");
        System.out.println("║  7️⃣  ► Gestionar entrega                        ║");
        System.out.println("║  8️⃣  ► Eliminar registro                        ║");
        System.out.println("║  0️⃣  ► Cerrar sistema                           ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    private void addNewDevice() {
        clearConsole();
        System.out.println("│     REGISTRO DE NUEVO EQUIPO      │");

        boolean validInput = false;
        while (!validInput) {
            try {
                String identifier = getValidStringInput("Número de serie del equipo: ");

                if (findDeviceByIdentifier(identifier) != null) {
                    System.out.println("⚠️  Error: Ya existe un equipo con ese número de serie.");
                    System.out.print("¿Desea intentar con otro número? (S/N): ");
                    if (!input.nextLine().trim().toUpperCase().equals("S")) {
                        return;
                    }
                    continue;
                }

                String issueDescription = getValidStringInput("Descripción del problema: ");
                LocalDate entryDate = getValidDate("Fecha de ingreso (DD-MM-YYYY): ");
                String ownerName = getValidStringInput("Nombre del propietario: ");
                String ownerEmail = getValidEmail("Correo electrónico: ");
                String ownerPhone = getValidPhone("Número telefónico (8 dígitos): ");

                Device newDevice = new Device(identifier, issueDescription, entryDate,
                        ownerName, ownerEmail, ownerPhone);

                workflows.get(DeviceState.RECEIVED).addDevice(newDevice);
                newDevice.recordActivity("Equipo ingresado al sistema");

                DataManager.logDeviceHistory(newDevice);
                DataManager.saveSystemData(workflows);

                System.out.println("✅ Equipo registrado correctamente.");
                pauseExecution();
                validInput = true;

            } catch (IllegalArgumentException e) {
                System.out.println("❌ Error: " + e.getMessage());
                System.out.print("¿Desea intentar nuevamente? (S/N): ");
                if (!input.nextLine().trim().toUpperCase().equals("S")) {
                    return;
                }
            }
        }
    }

    private void handleDeviceExamination() {
        clearConsole();
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│    🔍 EVALUACIÓN TÉCNICA            │");
        System.out.println("└─────────────────────────────────────┘");

        boolean validProcess = false;
        while (!validProcess) {
            try {
                DeviceQueue receivedQueue = workflows.get(DeviceState.RECEIVED);

                if (receivedQueue.isEmpty()) {
                    System.out.println("ℹ️  No hay equipos pendientes de evaluación.");
                    pauseExecution();
                    return;
                }

                Device currentDevice = receivedQueue.removeDevice();
                System.out.println("🔧 Evaluando: " + currentDevice.getIdentifier());
                System.out.println("\n📋 Información del equipo:");
                System.out.println(currentDevice);

                String technicalAnalysis = getValidStringInput("\nIngrese el análisis técnico: ");
                currentDevice.setTechnicalAnalysis(technicalAnalysis);
                currentDevice.recordActivity("Evaluación técnica realizada: " + technicalAnalysis);

                System.out.print("¿El equipo requiere reparación? (S/N): ");
                String needsRepair = input.nextLine().trim().toUpperCase();

                while (!needsRepair.equals("S") && !needsRepair.equals("N")) {
                    System.out.print("❌ Respuesta inválida. Ingrese S o N: ");
                    needsRepair = input.nextLine().trim().toUpperCase();
                }

                if (needsRepair.equals("S")) {
                    workflows.get(DeviceState.IN_REPAIR).addDevice(currentDevice);
                    currentDevice.recordActivity("Enviado a reparación");
                    System.out.println("📤 Equipo enviado a cola de reparación.");
                } else {
                    workflows.get(DeviceState.READY_DELIVERY).addDevice(currentDevice);
                    currentDevice.recordActivity("No requiere reparación. Listo para entrega");
                    System.out.println("📤 Equipo enviado directamente a entrega.");
                }

                DataManager.logDeviceHistory(currentDevice);
                DataManager.saveSystemData(workflows);

                pauseExecution();
                validProcess = true;

            } catch (Exception e) {
                System.out.println("❌ Error inesperado: " + e.getMessage());
                System.out.print("¿Desea intentar nuevamente? (S/N): ");
                if (!input.nextLine().trim().toUpperCase().equals("S")) {
                    return;
                }
            }
        }
    }

    private void handleTechnicalRepair() {
        clearConsole();
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│    🛠️  PROCESO DE REPARACIÓN        │");
        System.out.println("└─────────────────────────────────────┘");

        boolean validProcess = false;
        while (!validProcess) {
            try {
                DeviceQueue repairQueue = workflows.get(DeviceState.IN_REPAIR);

                if (repairQueue.isEmpty()) {
                    System.out.println("ℹ️  No hay equipos en reparación.");
                    pauseExecution();
                    return;
                }

                Device currentDevice = repairQueue.removeDevice();
                System.out.println("🔧 Reparando: " + currentDevice.getIdentifier());
                System.out.println("\n📋 Información del equipo:");
                System.out.println(currentDevice);
                System.out.println("🔍 Análisis: " + currentDevice.getTechnicalAnalysis());

                String repairWork = getValidStringInput("\nDetalles del trabajo realizado: ");
                String technicianId = getValidStringInput("Identificación del técnico: ");

                currentDevice.setRepairWork(repairWork);
                currentDevice.setTechnicianId(technicianId);
                currentDevice.recordActivity("Reparación completada por " + technicianId + ": " + repairWork);

                workflows.get(DeviceState.QUALITY_CHECK).addDevice(currentDevice);

                DataManager.logDeviceHistory(currentDevice);
                DataManager.saveSystemData(workflows);

                System.out.println("✅ Equipo enviado a control de calidad.");
                pauseExecution();
                validProcess = true;

            } catch (Exception e) {
                System.out.println("❌ Error inesperado: " + e.getMessage());
                System.out.print("¿Desea intentar nuevamente? (S/N): ");
                if (!input.nextLine().trim().toUpperCase().equals("S")) {
                    return;
                }
            }
        }
    }

    private void handleQualityVerification() {
        clearConsole();
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│    ✅ CONTROL DE CALIDAD            │");
        System.out.println("└─────────────────────────────────────┘");

        boolean validProcess = false;
        while (!validProcess) {
            try {
                DeviceQueue qualityQueue = workflows.get(DeviceState.QUALITY_CHECK);

                if (qualityQueue.isEmpty()) {
                    System.out.println("ℹ️  No hay equipos en control de calidad.");
                    pauseExecution();
                    return;
                }

                Device currentDevice = qualityQueue.removeDevice();
                System.out.println("🔍 Verificando: " + currentDevice.getIdentifier());
                System.out.println("\n📋 Información completa:");
                System.out.println(currentDevice);
                System.out.println("🔍 Análisis: " + currentDevice.getTechnicalAnalysis());
                System.out.println("🛠️  Reparación: " + currentDevice.getRepairWork());
                System.out.println("👨‍🔧 Técnico: " + currentDevice.getTechnicianId());

                System.out.print("\n¿El trabajo cumple con los estándares de calidad? (S/N): ");
                String qualityApproved = input.nextLine().trim().toUpperCase();

                while (!qualityApproved.equals("S") && !qualityApproved.equals("N")) {
                    System.out.print("❌ Respuesta inválida. Ingrese S o N: ");
                    qualityApproved = input.nextLine().trim().toUpperCase();
                }

                if (qualityApproved.equals("S")) {
                    workflows.get(DeviceState.READY_DELIVERY).addDevice(currentDevice);
                    currentDevice.recordActivity("Aprobado en control de calidad. Listo para entrega");
                    System.out.println("✅ Equipo aprobado y enviado a entrega.");
                } else {
                    workflows.get(DeviceState.IN_REPAIR).addDevice(currentDevice);
                    currentDevice.recordActivity("Rechazado en control de calidad. Regresado a reparación");
                    System.out.println("❌ Equipo regresado a reparación.");
                }

                DataManager.logDeviceHistory(currentDevice);
                DataManager.saveSystemData(workflows);

                pauseExecution();
                validProcess = true;

            } catch (Exception e) {
                System.out.println("❌ Error inesperado: " + e.getMessage());
                System.out.print("¿Desea intentar nuevamente? (S/N): ");
                if (!input.nextLine().trim().toUpperCase().equals("S")) {
                    return;
                }
            }
        }
    }

    private void handleCustomerDelivery() {
        clearConsole();
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│    📦 GESTIÓN DE ENTREGA            │");
        System.out.println("└─────────────────────────────────────┘");

        boolean validProcess = false;
        while (!validProcess) {
            try {
                DeviceQueue deliveryQueue = workflows.get(DeviceState.READY_DELIVERY);

                if (deliveryQueue.isEmpty()) {
                    System.out.println("ℹ️  No hay equipos listos para entrega.");
                    pauseExecution();
                    return;
                }

                Device currentDevice = deliveryQueue.removeDevice();
                System.out.println("📦 Procesando entrega: " + currentDevice.getIdentifier());
                System.out.println("\n📋 Información completa del servicio:");
                System.out.println(currentDevice.getCompleteDetails());

                System.out.print("\n¿Confirmar entrega al cliente? (S/N): ");
                String confirmDelivery = input.nextLine().trim().toUpperCase();

                while (!confirmDelivery.equals("S") && !confirmDelivery.equals("N")) {
                    System.out.print("❌ Respuesta inválida. Ingrese S o N: ");
                    confirmDelivery = input.nextLine().trim().toUpperCase();
                }

                if (confirmDelivery.equals("S")) {
                    currentDevice.recordActivity("Equipo entregado al cliente");
                    System.out.println("✅ Entrega confirmada para: " + currentDevice.getIdentifier());
                    DataManager.logDeviceHistory(currentDevice);
                } else {
                    deliveryQueue.addDevice(currentDevice);
                    System.out.println("❌ Entrega cancelada. Equipo regresado a cola de entrega.");
                }

                DataManager.saveSystemData(workflows);
                pauseExecution();
                validProcess = true;

            } catch (Exception e) {
                System.out.println("❌ Error inesperado: " + e.getMessage());
                System.out.print("¿Desea intentar nuevamente? (S/N): ");
                if (!input.nextLine().trim().toUpperCase().equals("S")) {
                    return;
                }
            }
        }
    }

    private void removeDeviceRecord() {
        clearConsole();
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│    🗑️  ELIMINAR REGISTRO            │");
        System.out.println("└─────────────────────────────────────┘");

        boolean validProcess = false;
        while (!validProcess) {
            try {
                String identifier = getValidStringInput("Número de serie del equipo a eliminar: ");
                Device deviceToDelete = findDeviceByIdentifier(identifier);

                if (deviceToDelete == null) {
                    System.out.println("❌ No se encontró equipo con número de serie: " + identifier);
                    System.out.print("¿Desea intentar con otro número? (S/N): ");
                    if (!input.nextLine().trim().toUpperCase().equals("S")) {
                        return;
                    }
                    continue;
                }

                System.out.println("\n📋 Información del equipo a eliminar:");
                System.out.println(deviceToDelete);

                System.out.print("\n¿Confirmar eliminación? (S/N): ");
                String confirmDelete = input.nextLine().trim().toUpperCase();

                while (!confirmDelete.equals("S") && !confirmDelete.equals("N")) {
                    System.out.print("❌ Respuesta inválida. Ingrese S o N: ");
                    confirmDelete = input.nextLine().trim().toUpperCase();
                }

                if (confirmDelete.equals("S")) {
                    removeDeviceFromWorkflows(deviceToDelete);
                    System.out.println("✅ Equipo eliminado exitosamente.");
                    DataManager.saveSystemData(workflows);
                    validProcess = true;
                } else {
                    System.out.println("❌ Operación cancelada.");
                    validProcess = true;
                }

                pauseExecution();

            } catch (Exception e) {
                System.out.println("❌ Error inesperado: " + e.getMessage());
                System.out.print("¿Desea intentar nuevamente? (S/N): ");
                if (!input.nextLine().trim().toUpperCase().equals("S")) {
                    return;
                }
            }
        }
    }

    private void viewSystemQueues() {
        clearConsole();
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│    📊 ESTADO DE COLAS               │");
        System.out.println("└─────────────────────────────────────┘");

        for (DeviceState state : DeviceState.values()) {
            DeviceQueue queue = workflows.get(state);
            System.out.println("\n🔸 " + state + " (" + queue.size() + " equipos):");

            if (queue.isEmpty()) {
                System.out.println("   └─ No hay equipos en esta cola.");
            } else {
                int counter = 1;
                for (Device device : queue.getQueueList()) {
                    System.out.println("   " + counter + ". " + device.getIdentifier() + " - " + device.getOwner());
                    counter++;
                }
            }
        }

        pauseExecution();
    }

    private void viewCompleteHistory() {
        clearConsole();
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│    📜 REGISTRO HISTÓRICO            │");
        System.out.println("└─────────────────────────────────────┘");

        String historyData = DataManager.readCompleteHistory();
        System.out.println(historyData);

        pauseExecution();
    }

    // Métodos de validación y utilidad
    private String getValidStringInput(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = this.input.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("❌ Este campo no puede estar vacío.");
            }
        } while (input.isEmpty());
        return input;
    }

    private int getIntegerInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ Debe ingresar un número válido.");
            }
        }
    }

    private LocalDate getValidDate(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String dateInput = input.nextLine().trim();
                return LocalDate.parse(dateInput, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Formato de fecha incorrecto. Use YYYY-MM-DD.");
            }
        }
    }

    private String getValidEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String email = input.nextLine().trim();
            if (EMAIL_PATTERN.matcher(email).matches()) {
                return email;
            }
            System.out.println("❌ Formato de correo electrónico inválido.");
        }
    }

    private String getValidPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String phone = input.nextLine().trim();
            if (PHONE_PATTERN.matcher(phone).matches()) {
                return phone;
            }
            System.out.println("❌ El teléfono debe tener exactamente 8 dígitos numéricos.");
        }
    }

    private Device findDeviceByIdentifier(String identifier) {
        for (DeviceQueue queue : workflows.values()) {
            for (Device device : queue.getQueueList()) {
                if (device.getIdentifier().equalsIgnoreCase(identifier)) {
                    return device;
                }
            }
        }
        return null;
    }

    private void removeDeviceFromWorkflows(Device deviceToRemove) {
        for (DeviceQueue queue : workflows.values()) {
            queue.getQueueList()
                    .removeIf(device -> device.getIdentifier().equalsIgnoreCase(deviceToRemove.getIdentifier()));
        }
    }

    private void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (IOException | InterruptedException e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    private void pauseExecution() {
        System.out.println("\n⏸️  Presione Enter para continuar...");
        input.nextLine();
    }

    public static void main(String[] args) {
        App system = new App();
        system.execute();
    }
}