package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.PlayerHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.ServerPlayerHypoEntity;
import phanastrae.voidstain_hypoidol.common.item.VoidstainItems;
import phanastrae.voidstain_hypoidol.common.network.s2c.UpdateHorrorFullnessPayload;

public class HorrorHypoEntity extends BouncingHypoEntity {
    public static final ResourceKey<DamageType> CONSUMED_DAMAGE_KEY = ResourceKey.create(Registries.DAMAGE_TYPE, VoidstainHypoidol.id("consumed"));

    public static final String KEY_HORROR_ID = "horror_id";
    public static final String KEY_FULLNESS = "fullness";

    private int horrorId;
    private float fullness;

    public HorrorHypoEntity(HypoEntityType<? extends HorrorHypoEntity> type, HypoZone zone) {
        super(type, zone);
    }

    public HorrorHypoEntity(HypoZone zone, int horrorId) {
        super(HypoEntityTypes.HORROR, zone);
        this.horrorId = horrorId;
    }

    @Override
    public void write(CompoundTag output) {
        super.write(output);
        output.putInt(KEY_HORROR_ID, this.horrorId);
        output.putFloat(KEY_FULLNESS, this.fullness);
    }

    @Override
    public void read(CompoundTag input) {
        super.read(input);
        input.getInt(KEY_HORROR_ID).ifPresent(id -> this.horrorId = id);
        input.getInt(KEY_FULLNESS).ifPresent(fullness -> this.fullness = fullness);
    }

    @Override
    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        super.tick(runsNormally, onServer, hypoverse);

        if (onServer && this.random.nextInt(3) == 0) {
            for (HypoEntity entity : this.getZone().getEntitiesInArea(this.x - this.getWidth() * 0.4f, this.x + this.getWidth() * 0.4f, this.y - this.getHeight() * 0.4f, this.y + this.getHeight() * 0.4f)) {
                if (entity instanceof MorselHypoEntity || (entity instanceof PlayerHypoEntity player && player.getTeleportCooldown() <= 0)) {
                    this.eat(entity);
                }
            }
        }

        if (onServer && this.random.nextInt(20) == 0) {
            if (this.fullness >= 500 && random.nextInt(3) == 0) {
                Item itemItem = getItem(this.fullness, this.random);

                ItemHypoEntity item = new ItemHypoEntity(this.zone);
                item.setPos(this.x, this.y);
                item.setOldPos(this.x, this.y);
                float angle = (float) (this.random.nextFloat() * Math.TAU);
                item.setAngle(angle);
                item.setOldAngle(angle);
                item.setAngleVelocity(this.random.nextFloat() * 0.04f + 0.01f);
                item.setLife(30 * 20);
                item.setVelocity(this.vx * -0.25f, this.vy * -0.25f);
                item.setItem(itemItem.getDefaultInstance());
                hypoverse.queueEntityAddition(item);
            }
            this.setFullness(this.fullness * 0.99f);
        }
    }

    public Item getItem(float fullness, RandomSource random) {
        int loveWeight = 600 + (int) Math.abs(Math.clamp(fullness - 4000, -500, 500));
        int uncertaintyWeight = 600 + (int) Math.abs(Math.clamp(fullness - 3000, -500, 500));
        int fearWeight = 600 + (int) Math.abs(Math.clamp(fullness - 2000, -500, 500));
        int hatredWeight = 600 + (int) Math.abs(Math.clamp(fullness - 1000, -500, 500));

        int r = random.nextInt(loveWeight + uncertaintyWeight + fearWeight + hatredWeight);
        if (r < loveWeight) {
            return VoidstainItems.LOVE;
        } else if (r < loveWeight + uncertaintyWeight) {
            return VoidstainItems.UNCERTAINTY;
        } else if (r < loveWeight + uncertaintyWeight + fearWeight) {
            return VoidstainItems.FEAR;
        } else {
            return VoidstainItems.HATRED;
        }
    }

    public void eat(HypoEntity morsel) {
        morsel.setRemoved();
        this.setFullness(this.fullness + 100);
        this.playSound(SoundEvents.GENERIC_EAT.value(), SoundSource.NEUTRAL, 0.08f, 1.2f + this.random.nextFloat() * 0.3f);

        if (morsel instanceof ServerPlayerHypoEntity hypoPlayer) {
            // TODO tidy this please: move damage stuff to separate class, assign proper damage type tags
            ServerPlayer serverPlayer = hypoPlayer.getWatcher().getPlayer();
            ServerLevel level = serverPlayer.level();
            serverPlayer.hurtServer(
                    level,
                    new DamageSource(level.registryAccess()
                            .lookupOrThrow(Registries.DAMAGE_TYPE)
                            .getOrThrow(CONSUMED_DAMAGE_KEY)
                    ),
                    99999999
            );
        }
    }

    public void setFullness(float fullness) {
        this.fullness = fullness;
        // TODO tidy this system? this doesn't feel great
        HypoZone zone = this.oldZone != null ? this.oldZone : this.getZone();
        zone.sendToAllWatchers(() -> new UpdateHorrorFullnessPayload(this.uuid, this.fullness));
    }

    @Override
    public float getWidth() {
        return 0.7f * this.getSizeModifier();
    }

    @Override
    public float getHeight() {
        return 0.7f * this.getSizeModifier();
    }

    public int getHorrorId() {
        return this.horrorId;
    }

    public float getFullness() {
        return this.fullness;
    }

    public float getSizeModifier() {
        return 0.4f + 0.6f * (float) Math.pow(Math.clamp(this.fullness / 4000, 0, 1), 2);
    }
}
