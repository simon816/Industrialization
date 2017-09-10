/*
 * Copyrighi (c) 2016 Seth J. Morabito <web@loomcom.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.simon816.i15n.core.cpu;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

import com.simon816.i15n.core.cpu.device.Memory;
import com.simon816.i15n.core.cpu.device.MonitorDriver;
import com.simon816.i15n.core.cpu.device.RPDrive;
import com.simon816.i15n.core.cpu.device.RPMonitor;
import com.simon816.i15n.core.cpu.device.RedBus;

public class Machine {

    private Thread runLoop;
    private boolean isRunning = false;
    private Semaphore interruptWait = new Semaphore(2);

    private final Bus bus;
    private final Cpu cpu;
    private final Memory ram;

    public Machine(MonitorDriver monitorDriver, Path bootloader, Path diskImage) {
        int ramSize = (16 << 10) - 1; // 16KB
        try {
            this.cpu = new Cpu();
            this.ram = new Memory(0x0000, ramSize);
            this.ram.loadFromFile(bootloader, 0x400, 0x100);
            RedBus redBus = new RedBus();
            redBus.setPeripheral(1, new RPMonitor(this, monitorDriver));
            redBus.setPeripheral(2, new RPDrive(this, diskImage, "System Disk"));
            this.bus = new Bus(0x0000, ramSize, this.ram, redBus);
            this.cpu.setBus(this.bus);
            this.bus.write(0, 2); // Drive
            this.bus.write(1, 1); // Monitor
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        handleReset(false);
    }

    public void signal() {
        this.interruptWait.release();
    }

    public void handleStart() {
        this.runLoop = new Thread(() -> {
            this.isRunning = true;
            do {
                step();
            } while (this.isRunning);
            this.isRunning = false;
        });
        this.runLoop.start();
    }

    public void halt() {
        if (this.runLoop != null && this.isRunning) {
            this.requestStop();
            this.runLoop.interrupt();
            this.runLoop = null;
        }
    }

    private void handleReset(boolean isColdReset) {
        halt();
        // Reset CPU
        this.cpu.reset();
        // If we're doing a cold reset, clear the memory.
        if (isColdReset) {
            this.ram.clear();
        }
    }

    /**
     * Perform a single step of the simulated system.
     */
    private void step() {
        this.interruptWait.acquireUninterruptibly();
        this.cpu.step();
        this.bus.update();
        if (this.cpu.isWaitingForInterrupt()) {
            this.interruptWait.acquireUninterruptibly();
            this.cpu.assertIrq();
        }
        if (this.interruptWait.availablePermits() < 2) {
            this.interruptWait.release();
        }
    }

    private void requestStop() {
        this.isRunning = false;
    }

}
