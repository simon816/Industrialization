package com.simon816.i15n.core.cpu.device;

import com.simon816.i15n.core.cpu.device.RedBus.Peripheral;

public class RPIOExpander implements Peripheral {

    private int readBuffer;
    private int outputLatch;

    @Override
    public void write(int address, int data) {
        switch (address) {
            case 0x00: // Read buffer (lo)
                break;
            case 0x01: // Read buffer (hi)
                break;
            case 0x02: // Output latch (lo)
                this.outputLatch = (this.outputLatch & 0xff00) | (data & 0xff);
                break;
            case 0x03: // Output latch (hi)
                this.outputLatch = ((data & 0xff) << 8) | (this.outputLatch & 0xff);
                break;
        }
    }

    @Override
    public int read(int address) {
        switch (address) {
            case 0x00: // Read buffer (lo)
                return this.readBuffer & 0xff;
            case 0x01: // Read buffer (hi)
                return (this.readBuffer >> 8) & 0xff;
            case 0x02: // Output latch (lo)
                return this.outputLatch & 0xff;
            case 0x03: // Output latch (hi)
                return (this.outputLatch >> 8) & 0xff;
            default:
                return 0;
        }
    }

    @Override
    public void update() {
        // TODO Auto-generated method stub

    }

}
