package com.simon816.i15n.core.block;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Piston;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.PistonTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.TickHelper;
import com.simon816.i15n.core.item.CustomItem;
import com.simon816.i15n.core.item.ItemBlockWrapper;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.world.CustomWorld;
import com.simon816.i15n.core.world.WorldManager;

public class CustomBlockEventListeners {

    private static ItemStack clickedItemStack;
    private static Player playerWhoClicked;

    public static void setItemBlockClicked(ItemStack itemStack, Player player) {
        clickedItemStack = itemStack;
        playerWhoClicked = player;
    }

    private static void clearTempVars() {
        clickedItemStack = null;
        playerWhoClicked = null;
    }

    public CustomBlockEventListeners() {
        TickHelper.startTicking(CustomBlockEventListeners::clearTempVars);
    }

    /*
     * Linked to ItemBlockWrapper#onItemUse.
     * 
     * Handles the place event after an itemblock is 'used'.
     */
    @Listener
    public void onBlockPlaceAfterItemUse(ChangeBlockEvent.Place event, @First Player player) {
        if (!event.getCause().get(NamedCause.SOURCE, Player.class).isPresent()) {
            clearTempVars();
            return; // Player must be source of event (i.e. not Notifier)
        }
        if (player != playerWhoClicked) {
            return;
        }
        ItemStack itemStack = clickedItemStack;
        clearTempVars();
        // Can safely cast as the itemstack was set from an ItemBlockWrapper instance
        ItemBlockWrapper item = (ItemBlockWrapper) CustomItem.fromItemStack(itemStack);
        BlockNature block = item.getBlock();
        CustomWorld world = WorldManager.toCustomWorld(player.getWorld());
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid()) {
                continue;
            }
            BlockSnapshot finalBlock = transaction.getFinal();
            Vector3i pos = finalBlock.getPosition();
            BlockSnapshot newSnapshot;
            if (block instanceof EnhancedCustomBlock) {
                newSnapshot = ((EnhancedCustomBlock) block).onBlockPlacedByPlayer(world, pos, finalBlock, player, item,
                        itemStack);
            } else {
                newSnapshot = block.onBlockPlacedByPlayer(world, pos, player, item, itemStack) ? finalBlock : null;
            }
            transaction.setValid(newSnapshot != null);
            if (newSnapshot != null) {
                if (finalBlock != newSnapshot) {
                    transaction.setCustom(newSnapshot);
                    finalBlock = newSnapshot;
                }
                BlockData data = block.createData(world, pos);
                if (data != null) {
                    item.transferToBlock(itemStack, data);
                }
                world.setBlockWithData(pos, block, data);
            }
        }
    }

    @Listener
    public void onBlockPlaceAfterPiston(ChangeBlockEvent.Place event, @First Piston piston) {
        World world = event.getTargetWorld();
        CustomWorld blockAccess = WorldManager.toCustomWorld(world);
        Vector3i pistonPos = piston.getLocation().getBlockPosition();
        if (!WorldManager.getPistonTracker(world).isTracked(pistonPos)) {
            return;
        }
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid()) {
                continue;
            }
            BlockSnapshot orig = transaction.getOriginal();
            if (orig.getState().getType() == BlockTypes.PISTON_EXTENSION) {
                Vector3i newPos = orig.getPosition();
                Tuple<Vector3i, ImmutablePair<EnhancedCustomBlock, BlockData>> oldData =
                        WorldManager.getPistonTracker(world).restore(newPos);
                if (oldData == null) {
                    continue;
                }
                Vector3i oldPos = oldData.getFirst();
                EnhancedCustomBlock block = oldData.getSecond().getLeft();
                BlockData data = oldData.getSecond().getRight();
                BlockSnapshot finalBlock = transaction.getFinal();
                BlockSnapshot newBlock = block.onMovedByPiston(blockAccess, newPos, finalBlock, data, oldPos);
                transaction.setValid(newBlock != null);
                if (newBlock != null) {
                    if (finalBlock != newBlock) {
                        finalBlock = newBlock;
                        transaction.setCustom(finalBlock);
                    }
                    if (data != null) {
                        data.setPos(newPos);
                    }
                    blockAccess.setBlockWithData(newPos, block, data);
                }
            }
        }
    }

    @Listener
    public void onBlockInteract(InteractBlockEvent event, @First Player player) {
        BlockSnapshot blockSnapshot = event.getTargetBlock();
        if (blockSnapshot == BlockSnapshot.NONE) {
            // boolean isCreative = player.gameMode().get() == GameModes.CREATIVE;
            // double blockReachDistance = isCreative ? 5F : 4.5F;
            // MovingObjectPosition res = RayTrace.traceEntity2((Entity) player, blockReachDistance,
            // isCreative);
            return;
        }

        CustomWorld world = WorldManager.toCustomWorld(player.getWorld());
        Vector3i pos = blockSnapshot.getPosition();
        BlockNature block = world.getBlock(pos);
        if (block == null)

        {
            return;
        }

        Direction side = event.getTargetSide();
        Vector3d point = event.getInteractionPoint().orElse(null);
        boolean allowInteract = !event.isCancelled();
        if (event instanceof InteractBlockEvent.Primary)

        {
            allowInteract = block.onBlockHit(world, pos, player, side, point);
        } else if (event instanceof InteractBlockEvent.Secondary)

        {
            if (player.getItemInHand().isPresent() && player.get(Keys.IS_SNEAKING).get()) {
                // Pass on the item click without telling the block
                allowInteract = true;
            } else {
                allowInteract = block.onBlockActivated(world, pos, player, side, point);
            }
        }
        event.setCancelled(!allowInteract);

    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        Player player = event.getCause().first(Player.class).orElse(null);
        BlockSnapshot pistonCause = getPistonCause(event.getCause());
        CustomWorld world = WorldManager.toCustomWorld(event.getTargetWorld());
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid()) {
                continue;
            }
            BlockSnapshot finalBlock = transaction.getFinal();
            Vector3i pos = finalBlock.getPosition();
            BlockNature block = world.getBlock(pos);
            if (block == null) {
                continue;
            }
            BlockSnapshot newBlock = finalBlock;
            if (pistonCause != null && block instanceof EnhancedCustomBlock) {
                WorldManager.getPistonTracker(world.getWorld()).addTarget(pistonCause, (EnhancedCustomBlock) block,
                        world.getBlockData(pos), pos);
                newBlock = finalBlock;
            } else {
                if (block instanceof EnhancedCustomBlock) {
                    newBlock = ((EnhancedCustomBlock) block).onBlockBreak(world, pos, finalBlock, player);
                } else {
                    newBlock = block.onBlockBreak(world, pos, player) ? newBlock : null;
                }
            }
            transaction.setValid(newBlock != null);
            if (newBlock != null) {
                if (newBlock != finalBlock) {
                    transaction.setCustom(newBlock);
                }
                world.removeBlock(pos);
            }

        }
    }

    private static BlockSnapshot getPistonCause(Cause cause) {
        List<BlockSnapshot> blockCauses = cause.allOf(BlockSnapshot.class);
        for (BlockSnapshot blockCause : blockCauses) {
            BlockType type = blockCause.getState().getType();
            if (type == BlockTypes.PISTON || type == BlockTypes.STICKY_PISTON) {
                return blockCause;
            }
            if (type == BlockTypes.PISTON_EXTENSION
                    && blockCause.getState().get(Keys.PISTON_TYPE).get() == PistonTypes.STICKY) {
                // For some reason, when a sticky piston is retracting, the cause is the extension
                // block
                return blockCause;
            }
        }
        return null;
    }

    @Listener
    public void onBlockNotify(NotifyNeighborBlockEvent event, @First BlockSnapshot notifySource) {
        Optional<Location<World>> loc = notifySource.getLocation();
        if (!loc.isPresent()) {
            return;
        }
        Vector3i origin = loc.get().getBlockPosition();
        CustomWorld world = WorldManager.toCustomWorld(loc.get().getExtent());
        for (Iterator<Entry<Direction, BlockState>> iterator = event.getNeighbors().entrySet().iterator(); iterator
                .hasNext();) {
            Direction side = iterator.next().getKey();
            Vector3i notifyPos = origin.add(side.asBlockOffset());
            BlockNature block = world.getBlock(notifyPos);
            if (block != null) {
                boolean allow =
                        block.onNeighborNotify(world, notifyPos, notifySource.getPosition(), side.getOpposite());
                if (!allow) {
                    iterator.remove();
                }
            }
        }
    }

    @Listener
    public void onBlockDropItems(DropItemEvent.Destruct event, @Root BlockSpawnCause blockCause) {
        if (blockCause.getType() != SpawnTypes.DROPPED_ITEM) {
            return;
        }
        BlockSnapshot blockSnapshot = blockCause.getBlockSnapshot();
        CustomWorld world = WorldManager.toCustomWorld(event.getTargetWorld());
        Vector3i pos = blockSnapshot.getPosition();
        BlockNature block = world.getBlock(pos);
        if (block == null) {
            return;
        }
        boolean allowDrop = block.onBlockHarvest(world, pos, event.getEntities());
        event.setCancelled(!allowDrop);
    }
}
