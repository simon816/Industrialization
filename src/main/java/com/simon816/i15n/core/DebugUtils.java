package com.simon816.i15n.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map.Entry;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;

import com.google.common.base.Charsets;

public class DebugUtils {

    public static void printEvent(Event event) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(os);
        out.println();
        out.println(event.getClass().getSimpleName());
        printEventByType(out, event);
        out.println("    Caused by");
        for (Entry<String, Object> e : event.getCause().getNamedCauses().entrySet()) {
            out.println("        " + e.getKey() + ": " + prettyCauseObj(e.getValue()));
        }
        out.println();
        System.out.println(new String(os.toByteArray(), Charsets.UTF_8));
    }

    private static void printEventByType(PrintStream out, Event event) {
        if (event instanceof ChangeBlockEvent) {
            printChangeBlock(out, (ChangeBlockEvent) event);
        } else if (event instanceof InteractBlockEvent) {
            printInteractBlock(out, (InteractBlockEvent) event);
        } else if (event instanceof NotifyNeighborBlockEvent) {
            printNotifyEvent(out, (NotifyNeighborBlockEvent) event);
        }
    }

    private static void printNotifyEvent(PrintStream out, NotifyNeighborBlockEvent event) {
        out.println(event.getNeighbors());
    }

    private static void printInteractBlock(PrintStream out, InteractBlockEvent event) {
        out.println("    At " + event.getTargetBlock().getPosition());
        out.println("        " + event.getTargetBlock().getExtendedState());
        out.println("        Side: " + event.getTargetSide());
    }

    private static void printChangeBlock(PrintStream out, ChangeBlockEvent event) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            out.println("    At " + transaction.getOriginal().getPosition());
            out.println("        " + transaction.getOriginal().getExtendedState() + " changes to "
                    + transaction.getFinal().getExtendedState());
        }
    }

    private static String prettyCauseObj(Object obj) {
        if (obj instanceof BlockSnapshot) {
            return ((BlockSnapshot) obj).getExtendedState().toString() + " at " + ((BlockSnapshot) obj).getPosition();
        }
        return obj.toString();
    }

}
