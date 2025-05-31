import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class DeviceQueue implements Serializable {
    private Queue<Device> deviceQueue;
    private DeviceState workflowState;

    public DeviceQueue(DeviceState workflowState) {
        this.deviceQueue = new LinkedList<>();
        this.workflowState = workflowState;
    }

    public void addDevice(Device device) {
        device.setCurrentState(workflowState);
        deviceQueue.add(device);
    }

    public Device removeDevice() {
        return deviceQueue.poll();
    }

    public Device viewNext() {
        return deviceQueue.peek();
    }

    public boolean isEmpty() {
        return deviceQueue.isEmpty();
    }

    public int size() {
        return deviceQueue.size();
    }

    public DeviceState getWorkflowState() {
        return workflowState;
    }

    public Queue<Device> getQueueList() {
        return deviceQueue;
    }
}