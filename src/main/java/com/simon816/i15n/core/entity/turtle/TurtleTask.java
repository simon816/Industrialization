package com.simon816.i15n.core.entity.turtle;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.simon816.i15n.core.ITickable;
import com.simon816.i15n.core.Serialized;
import com.simon816.i15n.core.Utils;
import com.simon816.i15n.core.entity.TurtleEntity;

public class TurtleTask implements ITickable, Serialized {

    private final List<Action> actions = Lists.newArrayList();
    private TurtleEntity turtle;
    private int pointer;

    private String code = "";

    public TurtleTask(TurtleEntity turtle) {
        this.turtle = turtle;
    }

    private Action getActive() {
        if (this.actions.isEmpty() || this.pointer > this.actions.size() - 1) {
            return null;
        }
        return this.actions.get(this.pointer);
    }


    @Override
    public void tick() {
        Action active = getActive();
        if (active == null) {
            return;
        }
        // Special case for GOTO
        if (active instanceof GotoAction) {
            this.pointer = ((GotoAction) active).line;
            return;
        }
        if (active.progress(this.turtle)) {
            this.pointer++;
            active.reset();
        }
    }


    public String getCode() {
        return this.code;
    }


    public void setCode(String code) {
        this.code = code;
        String[] lines = code.split("\\n");
        this.actions.clear();
        this.pointer = 0;
        for (String line : lines) {
            Action action;
            try {
                action = ActionParser.parse(line);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (action != null) {
                action.reset();
                this.actions.add(action);
            }
        }
    }

    @Override
    public void readFrom(DataView data) {
        setCode(data.getString(of("code")).orElse(""));
        this.pointer = data.getInt(of("pointer")).orElse(0);
        Optional<DataView> opActiveData = data.getView(of("active"));
        if (opActiveData.isPresent()) {
            Action active = getActive();
            if (active != null) {
                active.readFrom(opActiveData.get());
            }
        }
    }

    @Override
    public void writeTo(DataView data) {
        data.set(of("code"), this.code);
        data.set(of("pointer"), this.pointer);
        Action active = getActive();
        if (active != null) {
            active.writeTo(data.createView(of("active")));
        }
    }

    private static abstract class Action implements Serialized {

        protected Action() {}

        public void reset() {}

        public abstract boolean progress(TurtleEntity turtle);

        @Override
        public void readFrom(DataView data) {}

        @Override
        public void writeTo(DataView data) {}
    }

    private static class ForwardAction extends Action {

        private final double dist;
        private double progress;

        public ForwardAction(double dist) {
            this.dist = dist;
        }

        @Override
        public boolean progress(TurtleEntity turtle) {
            Vector3d rotation = turtle.getRotation();
            Direction direction = Utils.rotationToDirection(rotation.getY());
            double speed = 0.1;
            if (this.dist < 0) {
                speed = -speed;
            }
            turtle.setPosition(turtle.getPosition().add(direction.asBlockOffset().toDouble().mul(speed)));
            this.progress += Math.abs(speed);
            return this.progress >= Math.abs(this.dist);
        }

        @Override
        public void reset() {
            this.progress = 0;
        }

        @Override
        public void readFrom(DataView data) {
            this.progress = data.getDouble(of("progress")).orElse(0D);
        }

        @Override
        public void writeTo(DataView data) {
            data.set(of("progress"), this.progress);
        }
    }

    private static class RotateAction extends Action {

        private final int deg;

        public RotateAction(int deg) {
            this.deg = deg;
        }

        @Override
        public boolean progress(TurtleEntity turtle) {
            turtle.setRotation(turtle.getRotation().add(0, this.deg, 0));
            return true;
        }

    }

    private static class GotoAction extends Action {

        public int line;

        public GotoAction(int line) {
            this.line = line;
        }

        @Override
        public boolean progress(TurtleEntity turtle) {
            return true;
        }

    }

    private static class ActionParser {

        public static Action parse(String line) {
            String[] parts = line.split(" ");
            if (parts[0].equals("FORWARD")) {
                if (parts.length < 2) {
                    return null;
                }
                return new ForwardAction(Double.parseDouble(parts[1]));
            }
            if (parts[0].equals("BACKWARD")) {
                if (parts.length < 2) {
                    return null;
                }
                return new ForwardAction(-Double.parseDouble(parts[1]));
            }
            if (parts[0].equals("LEFT")) {
                return new RotateAction(270);
            }
            if (parts[0].equals("RIGHT")) {
                return new RotateAction(90);
            }
            if (parts[0].equals("GOTO")) {
                if (parts.length < 2) {
                    return null;
                }
                return new GotoAction(Integer.parseInt(parts[1]));
            }
            return null;
        }

    }

}
