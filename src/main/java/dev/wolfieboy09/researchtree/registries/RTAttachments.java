package dev.wolfieboy09.researchtree.registries;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class RTAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = 
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ResearchTreeMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerResearchData>> RESEARCH_DATA =
            ATTACHMENTS.register("research_data", () ->
                    AttachmentType.builder(PlayerResearchData::new)
                            .serialize(PlayerResearchData.CODEC)
                            .copyOnDeath()
                            .build()
            );
}
