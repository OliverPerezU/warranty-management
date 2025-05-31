import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor centralizado para el manejo de persistencia de datos
 * del sistema de soporte técnico computacional
 */
public class DataManager {
    // Configuración de archivos del sistema
    private static final String SYSTEM_DATA_FILE = "technical_support_data.ser";
    private static final String ACTIVITY_LOG_FILE = "service_records.log";
    private static final String BACKUP_DIRECTORY = "system_backups";
    
    // Singleton para gestión de instancia única
    private static DataManager instance;
    
    private DataManager() {
        initializeSystemDirectories();
    }
    
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    /**
     * Inicializa los directorios necesarios para el sistema
     */
    private void initializeSystemDirectories() {
        try {
            Path backupPath = Paths.get(BACKUP_DIRECTORY);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }
        } catch (IOException e) {
            System.err.println("⚠️ Advertencia: No se pudo crear el directorio de respaldos");
        }
    }

    /**
     * Persiste el estado completo del sistema de colas
     * 
     * @param workflowQueues Mapa de estados y sus respectivas colas de dispositivos
     */
    public static void saveSystemData(Map<DeviceState, DeviceQueue> workflowQueues) {
        try {
            
            // Serializar datos principales
            try (FileOutputStream fileStream = new FileOutputStream(SYSTEM_DATA_FILE);
                 BufferedOutputStream bufferedStream = new BufferedOutputStream(fileStream);
                 ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream)) {
                
                objectStream.writeObject(workflowQueues);
                objectStream.flush();
                
                System.out.println("💾 Estado del sistema guardado exitosamente.");
                
            }
        } catch (IOException e) {
            System.err.println("❌ Error crítico al persistir datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recupera el estado completo del sistema desde el almacenamiento
     * 
     * @return Mapa de estados con sus colas correspondientes
     */
    @SuppressWarnings("unchecked")
    public static Map<DeviceState, DeviceQueue> loadSystemData() {
        File dataFile = new File(SYSTEM_DATA_FILE);
        
        if (!dataFile.exists() || dataFile.length() == 0) {
            System.out.println("📂 Inicializando sistema con configuración por defecto...");
            return buildDefaultWorkflowStructure();
        }

        try (FileInputStream fileStream = new FileInputStream(dataFile);
             BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);
             ObjectInputStream objectStream = new ObjectInputStream(bufferedStream)) {
            
            Map<DeviceState, DeviceQueue> loadedData = 
                (Map<DeviceState, DeviceQueue>) objectStream.readObject();
            
            System.out.println("📂 Configuración del sistema cargada correctamente.");
            return loadedData;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("⚠️ Error al recuperar datos del sistema: " + e.getMessage());
            System.out.println("🔄 Restaurando configuración por defecto...");
            return buildDefaultWorkflowStructure();
        }
    }

    /**
     * Construye la estructura inicial de colas del sistema
     * 
     * @return Estructura de colas inicializada
     */
    private static Map<DeviceState, DeviceQueue> buildDefaultWorkflowStructure() {
        Map<DeviceState, DeviceQueue> workflowStructure = new HashMap<>();
        
        // Inicializar cada estado del flujo de trabajo
        for (DeviceState state : DeviceState.values()) {
            workflowStructure.put(state, new DeviceQueue(state));
        }
        
        System.out.println("🏗️ Estructura de flujo de trabajo inicializada.");
        return workflowStructure;
    }

    /**
     * Registra la actividad de un dispositivo en el log histórico
     * 
     * @param device Dispositivo cuya actividad se va a registrar
     */
    public static void logDeviceActivity(Device device) {
        try (FileWriter fileWriter = new FileWriter(ACTIVITY_LOG_FILE, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter logWriter = new PrintWriter(bufferedWriter)) {
            
            // Separador visual para cada entrada
            logWriter.println("═══════════════════════════════════════════════");
            logWriter.println("🏷️ Identificador: " + device.getIdentifier());
            logWriter.println("📊 Estado Actual: " + device.getCurrentState());
            logWriter.println("👤 Propietario: " + device.getOwner());
            logWriter.println("📅 Fecha de Registro: " + java.time.LocalDateTime.now());
            
            // Análisis técnico si existe
            if (device.getTechnicalAnalysis() != null && !device.getTechnicalAnalysis().isEmpty()) {
                logWriter.println("🔍 Diagnóstico: " + device.getTechnicalAnalysis());
            }
            
            // Trabajo de reparación si existe
            if (device.getRepairWork() != null && !device.getRepairWork().isEmpty()) {
                logWriter.println("🛠️ Intervención: " + device.getRepairWork());
                logWriter.println("👨‍🔧 Especialista: " + device.getTechnicianId());
            }
            
            logWriter.println("📝 Registro de Actividades:");
            for (ActivityRecord record : device.getActivityLog()) {
                logWriter.println("    ↳ " + record.toString());
            }
            
            logWriter.println("═══════════════════════════════════════════════");
            logWriter.println(); // Línea en blanco para separación
            
        } catch (IOException e) {
            System.err.println("❌ Error al registrar actividad del dispositivo: " + e.getMessage());
        }
    }

    /**
     * Método alternativo para compatibilidad con código existente
     */
    public static void logDeviceHistory(Device device) {
        logDeviceActivity(device);
    }

    /**
     * Recupera todo el historial de actividades registradas
     * 
     * @return Contenido completo del log histórico
     */
    public static String readCompleteHistory() {
        File logFile = new File(ACTIVITY_LOG_FILE);
        
        if (!logFile.exists()) {
            return "📝 No se encontraron registros históricos en el sistema.\n" +
                   "💡 Los registros aparecerán aquí cuando se procesen dispositivos.";
        }

        StringBuilder historyContent = new StringBuilder();
        historyContent.append("📚 HISTORIAL COMPLETO DEL SISTEMA\n");
        historyContent.append("═".repeat(50)).append("\n\n");

        try (FileReader fileReader = new FileReader(logFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                historyContent.append(currentLine).append("\n");
            }
            
        } catch (IOException e) {
            System.err.println("❌ Error al acceder al historial: " + e.getMessage());
            return "⚠️ Error al recuperar el historial del sistema.\n" +
                   "🔧 Verifique los permisos de archivo y el espacio disponible.";
        }

        if (historyContent.length() <= 100) { // Solo contiene el encabezado
            return "📝 El historial está vacío.\n" +
                   "💡 Los registros aparecerán aquí cuando se procesen dispositivos.";
        }

        return historyContent.toString();
    }

    /**
     * Crea un respaldo del archivo de datos actual antes de sobrescribirlo
     */
    private static void createBackupIfExists() {
        File currentDataFile = new File(SYSTEM_DATA_FILE);
        
        if (currentDataFile.exists()) {
            try {
                String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                
                Path backupPath = Paths.get(BACKUP_DIRECTORY, 
                    "backup_" + timestamp + ".ser");
                
                Files.copy(currentDataFile.toPath(), backupPath, 
                    StandardCopyOption.REPLACE_EXISTING);
                
            } catch (IOException e) {
                System.err.println("⚠️ No se pudo crear respaldo: " + e.getMessage());
            }
        }
    }

    /**
     * Limpia archivos de respaldo antiguos (mantiene solo los últimos 5)
     */
    public static void cleanupOldBackups() {
        try {
            Path backupDir = Paths.get(BACKUP_DIRECTORY);
            if (!Files.exists(backupDir)) return;
            
            Files.list(backupDir)
                .filter(path -> path.toString().endsWith(".ser"))
                .sorted((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p2)
                            .compareTo(Files.getLastModifiedTime(p1));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .skip(5) // Mantener los 5 más recientes
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("⚠️ No se pudo eliminar respaldo: " + path);
                    }
                });
                
        } catch (IOException e) {
            System.err.println("⚠️ Error al limpiar respaldos: " + e.getMessage());
        }
    }

    /**
     * Verifica la integridad de los archivos del sistema
     * 
     * @return true si los archivos están en buen estado
     */
    public static boolean verifySystemIntegrity() {
        File dataFile = new File(SYSTEM_DATA_FILE);
        File logFile = new File(ACTIVITY_LOG_FILE);
        
        boolean dataFileValid = !dataFile.exists() || 
            (dataFile.canRead() && dataFile.canWrite());
        
        boolean logFileValid = !logFile.exists() || 
            (logFile.canRead() && logFile.canWrite());
        
        if (!dataFileValid) {
            System.err.println("❌ Problema con el archivo de datos del sistema");
        }
        
        if (!logFileValid) {
            System.err.println("❌ Problema con el archivo de registro");
        }
        
        return dataFileValid && logFileValid;
    }

    /**
     * Exporta el historial a un archivo específico
     * 
     * @param exportPath Ruta donde exportar el historial
     * @return true si la exportación fue exitosa
     */
    public static boolean exportHistoryToFile(String exportPath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(exportPath))) {
            writer.print(readCompleteHistory());
            System.out.println("📤 Historial exportado a: " + exportPath);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al exportar historial: " + e.getMessage());
            return false;
        }
    }
}