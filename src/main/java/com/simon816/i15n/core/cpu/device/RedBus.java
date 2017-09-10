package com.simon816.i15n.core.cpu.device;

public class RedBus extends Device {

    public interface Peripheral {

        void write(int address, int data);

        int read(int address);

        void update();

    }

    private int activeDeviceId;
    private boolean enabled;

    // TODO This does nothing
    private int memoryWindow;
    @SuppressWarnings("unused")
    private boolean enableWindow;

    private Peripheral[] peripherals = new Peripheral[0x100];

    public RedBus() {
        super(-1, -1);
    }

    @Override
    public void write(int address, int data) {
        if (!this.enabled) {
            return;
        }
        Peripheral peripheral = this.peripherals[this.activeDeviceId];
        if (peripheral != null) {
            peripheral.write(address, data & 0xff);
        }
    }

    @Override
    public int read(int address, boolean cpuAccess) {
        if (!this.enabled) {
            return 0;
        }
        Peripheral peripheral = this.peripherals[this.activeDeviceId];
        if (peripheral != null) {
            return peripheral.read(address);
        }
        return 0;
    }

    public void setActiveDevice(int id) {
        this.activeDeviceId = id;
    }

    public int getActiveDevice() {
        return this.activeDeviceId;
    }

    public void setWindowOffset(int offset) {
        this.startAddress = offset;
        this.endAddress = offset + 0xff;
    }

    public int getWindowOffset() {
        return this.startAddress;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMemoryWindow(int window) {
        this.memoryWindow = window;
    }

    public int getMemoryWindow() {
        return this.memoryWindow;
    }

    public void setEnableWindow(boolean enabled) {
        this.enableWindow = enabled;
    }

    public void setPeripheral(int id, Peripheral peripheral) {
        this.peripherals[id] = peripheral;
    }

    public void updatePeripheral() {
        Peripheral peripheral = this.peripherals[this.activeDeviceId];
        if (peripheral != null) {
            peripheral.update();
        }
    }

}
