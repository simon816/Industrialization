package com.simon816.i15n.core.cpu.device;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.common.base.Charsets;
import com.simon816.i15n.core.cpu.Machine;
import com.simon816.i15n.core.cpu.device.RedBus.Peripheral;

public class RPDrive implements Peripheral {

    private int sector;
    private int command;
    private SeekableByteChannel channel;
    private ByteBuffer buffer;
    private final byte[] diskName = new byte[0x80];
    private final byte[] diskSerial = new byte[0x80];
    private Machine machine;

    public RPDrive(Machine machine, Path file, String driveName) throws IOException {
        this.machine = machine;
        this.channel = Files.newByteChannel(file, StandardOpenOption.READ);
        this.buffer = ByteBuffer.allocateDirect(0x80);
        byte[] name = driveName.getBytes(Charsets.UTF_8);
        System.arraycopy(name, 0, this.diskName, 0, name.length);
    }

    @Override
    public void write(int address, int data) {
        switch (address) {
            case 0x80: // Sector number (lo)
                this.sector = (this.sector & 0xff00) | data;
                break;
            case 0x81: // Sector number (hi)
                this.sector = (data << 8) | (this.sector & 0xff);
                break;
            case 0x82: // Disk command
                this.command = data;
                break;
            default: // Disk sector buffer
                if (address >= 0 && address <= 0x7f) {
                    this.buffer.put(address, (byte) data);
                }
        }
    }


    @Override
    public int read(int address) {
        switch (address) {
            case 0x80: // Sector number (lo)
                return this.sector & 0xff;
            case 0x81: // Sector number (hi)
                return (this.sector >> 8) & 0xff;
            case 0x82: // Disk command
                return this.command;
            default: // Disk sector buffer
                if (address >= 0 && address <= 0x7f) {
                    return this.buffer.get(address);
                }
                return 0;
        }
    }

    @Override
    public void update() {
        this.machine.signal();
        try {
            switch (this.command) {
                case 0x01: // Read Disk Name
                    this.buffer.clear();
                    this.buffer.put(this.diskName);
                    this.command = 0;
                    break;
                case 0x02: // Write Disk Name
                    this.buffer.get(this.diskName);
                    this.command = 0;
                    break;
                case 0x03: // Read Disk Serial
                    this.buffer.clear();
                    this.buffer.put(this.diskSerial);
                    this.command = 0;
                    break;
                case 0x04: // Read Disk Sector
                    if (this.sector >= 0x800) {
                        this.command = 0xff;
                        break;
                    }
                    this.channel.position(this.sector << 7);
                    this.buffer.position(0);
                    if (this.channel.read(this.buffer) != this.buffer.capacity()) {
                        this.command = 0xff;
                    } else {
                        this.command = 0;
                    }
                    break;
                case 0x05: // Write Disk Sector
                    if (this.sector >= 0x800) {
                        this.command = 0xff;
                        break;
                    }
                    this.channel.position(this.sector << 7);
                    this.buffer.position(0);
                    this.channel.write(this.buffer);
                    this.command = 0;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.command = 0xff;
        }
    }
}
