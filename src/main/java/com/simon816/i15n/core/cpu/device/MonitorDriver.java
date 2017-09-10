package com.simon816.i15n.core.cpu.device;

public interface MonitorDriver {

    public void setMonitor(RPMonitor monitor);

    public void updateCursor(int cursorX, int cursorY, int cursorMode);

    public void update(byte[][] windowData);

}
