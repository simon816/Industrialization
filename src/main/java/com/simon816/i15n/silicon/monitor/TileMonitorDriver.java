package com.simon816.i15n.silicon.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.util.AABB;

import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.ITickable;
import com.simon816.i15n.core.entity.EntityTracker;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.world.CustomWorld;
import com.simon816.j65el02.Machine;
import com.simon816.j65el02.device.FileDiskDriver;
import com.simon816.j65el02.device.Memory;
import com.simon816.j65el02.device.MonitorDriver;
import com.simon816.j65el02.device.RPDrive;
import com.simon816.j65el02.device.RPMonitor;


public class TileMonitorDriver extends BlockData implements ITickable {

    private static class Driver implements MonitorDriver {

        private static final byte[] charset;

        static {
            charset = new byte[0x800];
            try {
                InputStream file = Files.newInputStream(Paths.get("..", "..", "roms", "charset.rom"));
                file.read(charset);
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private final FrameBuffer buffer;
        private int cursorX;
        private int cursorY;
        private int cursorMode;
        private boolean isDirty;

        public Driver() {
            this.buffer = new ArrayFrameBuffer(RPMonitor.WIDTH * 8, RPMonitor.HEIGHT * 8);
        }

        @Override
        public void setMonitor(RPMonitor monitor) {}

        @Override
        public void updateCursor(int cursorX, int cursorY, int cursorMode) {
            this.isDirty = true;
            this.cursorX = cursorX;
            this.cursorY = cursorY;
            this.cursorMode = cursorMode;
        }

        @Override
        public void update(byte[][] windowData) {
            this.isDirty = true;
            for (int row = 0; row < RPMonitor.HEIGHT; row++) {
                for (int col = 0; col < RPMonitor.WIDTH; col++) {
                    byte data = windowData[row][col];
                    if (row == this.cursorY && col == this.cursorX) {
                        data ^= 0x80; // Invert color
                    }
                    drawchar(col, row, (char) (data & 0xff), PixelBaseColor.LIME.baseVal(),
                            PixelBaseColor.DARK_GREEN.darkestVal());
                }
            }
        }

        public void update(MonitorEntity monitor) {
            if (this.isDirty) {
                this.isDirty = false;
                this.buffer.copyTo(monitor.getBuffer());
            }
        }

        private void drawchar(int xoffset, int yoffset, char ch, byte fg, byte bg) {
            int charsetstart = (ch << 3);
            yoffset = yoffset << 3;
            xoffset = xoffset << 3;
            for (int ypos = yoffset; ypos < yoffset + 8; ypos++) {
                byte rowdata = charset[charsetstart++];
                int xpos = xoffset;
                this.buffer.setPixel(xpos++, ypos, (rowdata & 0x80) == 0x80 ? fg : bg);
                this.buffer.setPixel(xpos++, ypos, (rowdata & 0x40) == 0x40 ? fg : bg);
                this.buffer.setPixel(xpos++, ypos, (rowdata & 0x20) == 0x20 ? fg : bg);
                this.buffer.setPixel(xpos++, ypos, (rowdata & 0x10) == 0x10 ? fg : bg);
                this.buffer.setPixel(xpos++, ypos, (rowdata & 0x08) == 0x08 ? fg : bg);
                this.buffer.setPixel(xpos++, ypos, (rowdata & 0x04) == 0x04 ? fg : bg);
                this.buffer.setPixel(xpos++, ypos, (rowdata & 0x02) == 0x02 ? fg : bg);
                this.buffer.setPixel(xpos++, ypos, (rowdata & 0x01) == 0x01 ? fg : bg);
            }
        }

    }

    private MonitorEntity monitor;
    private Machine machine;
    private Driver driver;

    public TileMonitorDriver(CustomWorld world, Vector3i pos) {
        super(world, pos);
        this.driver = new Driver();
    }

    public Driver getDriver() {
        return this.driver;
    }

    @Override
    public void tick() {
        if (this.monitor != null) {
            // TODO This is all temporary
            if (this.machine == null) {
                this.driver = new Driver();
                Path romDir = Paths.get("..", "..", "roms");

                this.machine = new Machine(romDir.resolve("rpcboot.bin"), 0x500);

                FileDiskDriver diskDriver = null;
                try {
                    diskDriver = new FileDiskDriver(romDir.resolve("redforth.img"), "System Disk", "", true);
                } catch (IOException e) {
                }

                this.machine.setPeripheral(this.machine.getDefaultMonitorId(),
                        new RPMonitor(this.machine, this.driver));
                if (diskDriver != null) {
                    this.machine.setPeripheral(this.machine.getDefaultDriveId(), new RPDrive(this.machine, diskDriver));
                }
                this.machine.getBus().addDevice(new Memory(0x500, 0x2000 - 1));
                this.machine.getBus().addDevice(new Memory(0x2000, 0x4000 - 1));
                new Thread(this.machine).start();
            }
            this.driver.update(this.monitor);
            return;
        }
        AABB range = this.world.getWorld().getBlockSelectionBox(getPosition()).get().expand(Vector3i.ONE);
        Set<Entity> nearby =
                this.world.getWorld().getIntersectingEntities(range, entity -> entity instanceof ItemFrame);
        for (Entity entity : nearby) {
            EntityTracker tracker = this.world.getEntityTracker(entity);
            if (tracker instanceof MonitorEntity) {
                this.monitor = (MonitorEntity) tracker;
                break;
            }
        }
    }

    public void stop() {
        if (this.machine != null) {
            this.machine.stop();
        }
        this.monitor = null;
    }

}
