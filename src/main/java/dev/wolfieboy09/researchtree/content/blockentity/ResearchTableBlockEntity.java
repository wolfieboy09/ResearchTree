package dev.wolfieboy09.researchtree.content.blockentity;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.api.SafePacketDelivery;
import dev.wolfieboy09.researchtree.api.event.RequirementCompletedEvent;
import dev.wolfieboy09.researchtree.api.event.ResearchNodeEvent;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.client.toast.ResearchToast;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.data.ResearchNodeManager;
import dev.wolfieboy09.researchtree.data.ResearchProgressData;
import dev.wolfieboy09.researchtree.network.ResearchStartedPacket;
import dev.wolfieboy09.researchtree.network.UpdateResearchProgress;
import dev.wolfieboy09.researchtree.registries.RTAttachments;
import dev.wolfieboy09.researchtree.registries.RTBlockEntities;
import dev.wolfieboy09.researchtree.requirements.EnergyRequirement;
import dev.wolfieboy09.researchtree.requirements.FluidRequirement;
import dev.wolfieboy09.researchtree.requirements.ItemRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class ResearchTableBlockEntity extends BlockEntity {
    private UUID ownerUUID;
    private ResourceLocation currentResearch;
    private int researchTicks = 0;

    private final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!hasOwner() || currentResearch == null) {
                return stack;
            }

            ResearchNode node = getResearchNode(currentResearch);
            if (node == null) {
                return stack;
            }

            ServerPlayer player = getOwnerPlayer();
            if (player == null) {
                return stack;
            }

            ItemStack remaining = stack.copy();

            for (ResearchRequirement<?> req : node.requirements()) {
                if (req instanceof ItemRequirement itemReq) {
                    if (itemReq.accepts(remaining)) {
                        if (!simulate) {
                            remaining = itemReq.consume(player, remaining);
                            setChanged();
                        } else {
                            remaining = itemReq.simulateConsume(player, remaining);
                        }

                        if (remaining.isEmpty()) {
                            break;
                        }
                    }
                }
            }

            return remaining;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (!hasOwner() || currentResearch == null) {
                return false;
            }

            ResearchNode node = getResearchNode(currentResearch);
            if (node == null) {
                return false;
            }

            for (ResearchRequirement<?> req : node.requirements()) {
                if (req instanceof ItemRequirement itemReq && itemReq.accepts(stack)) {
                    return true;
                }
            }

            return false;
        }
    };

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            if (!hasOwner() || currentResearch == null) {
                return false;
            }

            ResearchNode node = getResearchNode(currentResearch);
            if (node == null) {
                return false;
            }

            for (ResearchRequirement<?> req : node.requirements()) {
                if (req instanceof FluidRequirement fluidReq && fluidReq.accepts(stack)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!hasOwner() || currentResearch == null) {
                return 0;
            }

            ResearchNode node = getResearchNode(currentResearch);
            if (node == null) {
                return 0;
            }

            ServerPlayer player = getOwnerPlayer();
            if (player == null) {
                return 0;
            }

            FluidStack remaining = resource.copy();
            int totalConsumed = 0;

            for (ResearchRequirement<?> req : node.requirements()) {
                if (req instanceof FluidRequirement fluidReq) {
                    if (fluidReq.accepts(remaining)) {
                        int before = remaining.getAmount();

                        if (action == FluidAction.EXECUTE) {
                            remaining = fluidReq.consume(player, remaining);
                            setChanged();
                        } else {
                            remaining = fluidReq.simulateConsume(player, remaining);
                        }

                        totalConsumed += (before - remaining.getAmount());

                        if (remaining.isEmpty()) {
                            break;
                        }
                    }
                }
            }

            return totalConsumed;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    };

    private final IEnergyStorage energyHandler = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!hasOwner() || currentResearch == null) {
                return 0;
            }

            ResearchNode node = getResearchNode(currentResearch);
            if (node == null) {
                return 0;
            }

            ServerPlayer player = getOwnerPlayer();
            if (player == null) {
                return 0;
            }

            int remaining = maxReceive;
            int totalConsumed = 0;

            for (ResearchRequirement<?> req : node.requirements()) {
                if (req instanceof EnergyRequirement energyReq) {
                    if (energyReq.accepts(remaining)) {
                        int before = remaining;

                        if (!simulate) {
                            remaining = energyReq.consume(player, remaining);
                            setChanged();
                        } else {
                            remaining = energyReq.simulateConsume(player, remaining);
                        }

                        totalConsumed += (before - remaining);

                        if (remaining == 0) {
                            break;
                        }
                    }
                }
            }

            return totalConsumed;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            if (!hasOwner() || currentResearch == null) {
                return false;
            }

            ResearchNode node = getResearchNode(currentResearch);
            if (node == null) {
                return false;
            }

            for (ResearchRequirement<?> req : node.requirements()) {
                if (req instanceof EnergyRequirement energyReq && energyReq.accepts(1)) {
                    return true;
                }
            }

            return false;
        }
    };

    public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
        super(RTBlockEntities.RESEARCH_TABLE.get(), pos, state);
    }

    public void setOwner(UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
    }

    public UUID getOwner() {
        return ownerUUID;
    }

    public boolean hasOwner() {
        return ownerUUID != null;
    }

    @Nullable
    private ServerPlayer getOwnerPlayer() {
        if (!hasOwner() || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
    }

    public ResourceLocation getCurrentResearch() {
        return currentResearch;
    }

    public void setCurrentResearch(ResourceLocation researchId) {
        this.currentResearch = researchId;
        this.researchTicks = 0;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResearchTableBlockEntity blockEntity) {
        if (level.isClientSide || !blockEntity.hasOwner()) return;

        ServerPlayer player = blockEntity.getOwnerPlayer();
        if (player == null) return;

        PlayerResearchData data = player.getData(RTAttachments.RESEARCH_DATA);

        if (blockEntity.currentResearch == null) {
            ResourceLocation inProgressResearch = data.getActiveResearch()
                    .keySet()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (inProgressResearch != null && !data.isCompleted(inProgressResearch)) {
                blockEntity.currentResearch = inProgressResearch;
                blockEntity.setChanged();
            } else {
                return;
            }
        }

        if (data.isCompleted(blockEntity.currentResearch)) {
            blockEntity.currentResearch = null;
            blockEntity.researchTicks = 0;
            blockEntity.setChanged();
            return;
        }

        ResearchNode node = blockEntity.getResearchNode(blockEntity.currentResearch);
        if (node == null) {
            blockEntity.currentResearch = null;
            blockEntity.setChanged();
            return;
        }

        if (data.getProgress(blockEntity.currentResearch) == null) {
            data.startResearch(blockEntity.currentResearch);
            SafePacketDelivery.sendToPlayer(blockEntity.getOwnerPlayer(), new ResearchStartedPacket(blockEntity.currentResearch));
            NeoForge.EVENT_BUS.post(new ResearchNodeEvent.Started(player, node));
        }

        for (ResearchRequirement<?> req : node.requirements()) {
            boolean wasMetBefore = req.isMet(player);

            if (!wasMetBefore && req.isMet(player)) {
                NeoForge.EVENT_BUS.post(new RequirementCompletedEvent(player, node, req));
            }
        }

        boolean allRequirementsMet = blockEntity.checkRequirements(player, node);
        if (!allRequirementsMet) {
            return;
        }

        ResearchProgressData progressData = node.progressData();

        blockEntity.researchTicks++;

        if (progressData.shouldIncrementProgress(blockEntity.researchTicks)) {
            blockEntity.researchTicks = 0;

            var progress = data.getProgress(blockEntity.currentResearch);
            if (progress != null) {
                float progressIncrement = progressData.calculateProgressIncrement(progressData.ticksPerPercent());
                float newProgress = progress.getProgress() + progressIncrement;

                SafePacketDelivery.sendToPlayer(blockEntity.getOwnerPlayer(), new UpdateResearchProgress(blockEntity.currentResearch, newProgress));
                progress.setProgress(newProgress);

                if (progress.isComplete()) {
                    blockEntity.completeResearch(player, node);
                }

                blockEntity.setChanged();
            }
        }
    }

    private boolean checkRequirements(ServerPlayer player, ResearchNode node) {
        for (ResearchRequirement<?> req : node.requirements()) {
            if (!req.isMet(player)) {
                return false;
            }
        }
        return true;
    }

    private void completeResearch(ServerPlayer player, ResearchNode node) {
        if (currentResearch == null) return;

        PlayerResearchData data = player.getData(RTAttachments.RESEARCH_DATA);
        data.completeResearch(currentResearch, player);

        for (var reward : node.rewards()) {
            try {
                reward.grant(player, level);
            } catch (Exception e) {
                ResearchTreeMod.LOGGER.error("Failed to grant reward {} to {} for research {}. {}", reward, player.getName(), currentResearch, e);
                player.sendSystemMessage(
                        Component.translatable("message.researchtree.reward_failed", reward.getDisplayText()).withStyle(ChatFormatting.DARK_RED)
                );
            }
        }

        NeoForge.EVENT_BUS.post(new ResearchNodeEvent.Completed(player, node));
        ResearchToast.addOrUpdate(player, node);

        currentResearch = null;
        researchTicks = 0;
        setChanged();
    }

    private ResearchNode getResearchNode(ResourceLocation id) {
        return ResearchNodeManager.getNode(id);
    }

    public float getResearchProgress() {
        if (currentResearch == null || !hasOwner()) return 0.0f;

        ServerPlayer player = getOwnerPlayer();
        if (player != null) {
            PlayerResearchData data = player.getData(RTAttachments.RESEARCH_DATA);
            var progress = data.getProgress(currentResearch);
            return progress != null ? progress.getProgress() : 0.0f;
        }
        return 0.0f;
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }

    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        if (currentResearch != null) {
            tag.putString("CurrentResearch", currentResearch.toString());
        }
        tag.putInt("ResearchTicks", researchTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        if (tag.contains("CurrentResearch")) {
            this.currentResearch = ResourceLocation.parse(tag.getString("CurrentResearch"));
        }
        this.researchTicks = tag.getInt("ResearchTicks");
    }
}