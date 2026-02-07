package dev.wolfieboy09.researchtree.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ResearchProgress {
    private float progress;

    public static final Codec<ResearchProgress> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("progress").forGetter(ResearchProgress::getProgress)
            ).apply(instance, progress -> {
                ResearchProgress rp = new ResearchProgress();
                rp.progress = progress;
                return rp;
            })
    );

    public ResearchProgress() {
        this.progress = 0.0f;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
    }

    public boolean isComplete() {
        return progress >= 1.0f;
    }
}
