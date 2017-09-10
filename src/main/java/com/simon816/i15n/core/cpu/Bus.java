package com.simon816.i15n.core.cpu;

import com.simon816.i15n.core.cpu.device.Memory;
import com.simon816.i15n.core.cpu.device.RedBus;

public class Bus {

    private RedBus redBus;
    private Memory ram;

    private int startAddress;
    private int endAddress;

    public Bus(int startAddress, int endAddress, Memory ram, RedBus redBus) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.ram = ram;
        this.redBus = redBus;
    }

    public void write(int address, int data) {
        if (this.redBus.inRange(address)) {
            this.redBus.write(address - this.redBus.startAddress(), data);
        } else {
            this.ram.write(address - this.ram.startAddress(), data);
        }
    }

    public int read(int address, boolean cpuAccess) {
        if (this.redBus.inRange(address)) {
            return this.redBus.read(address - this.redBus.startAddress(), cpuAccess) & 0xff;
        }
        return this.ram.read(address - this.ram.startAddress(), cpuAccess) & 0xff;
    }

    public int endAddress() {
        return this.endAddress;
    }

    public RedBus getRedBus() {
        return this.redBus;
    }

    public void update() {
        this.redBus.updatePeripheral();
    }


}
