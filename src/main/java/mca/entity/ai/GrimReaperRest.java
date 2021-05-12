package mca.entity.ai;

import mca.entity.EntityGrimReaper;
import mca.enums.EnumReaperAttackState;
import mca.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class GrimReaperRest extends Goal {
    private final EntityGrimReaper reaper;

    private final static int COOLDOWN = 3000;

    private int lastHeal = -COOLDOWN;
    private int healingCount = 0;
    private int healingTime;

    public GrimReaperRest(EntityGrimReaper reaper) {
        this.reaper = reaper;
    }

    @Override
    public boolean canUse() {
        return reaper.tickCount > lastHeal + COOLDOWN && reaper.getHealth() <= (reaper.getMaxHealth() / (healingCount + 2));
    }

    @Override
    public boolean canContinueToUse() {
        return healingTime > 0;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        reaper.setAttackState(EnumReaperAttackState.REST);
        reaper.teleportTo(reaper.getX(), reaper.getY() + 8, reaper.getZ());

        healingTime = 500;
        lastHeal = reaper.tickCount;
        healingCount++;
    }

    @Override
    public void stop() {
        reaper.setAttackState(EnumReaperAttackState.IDLE);
    }

    @Override
    public void tick() {
        healingTime--;

        reaper.setDeltaMovement(Vector3d.ZERO);

        if (!reaper.level.isClientSide && healingTime % 10 == 0) {
            reaper.setHealth(reaper.getHealth() + MathHelper.clamp(1.0F / healingCount, 0.5F, 1.0F));
        }

        if (!reaper.level.isClientSide && healingTime % 100 == 0) {
            // Let's have a light show.
            int dX = reaper.getRandom().nextInt(8) + 4 * reaper.getRandom().nextFloat() >= 0.50F ? 1 : -1;
            int dZ = reaper.getRandom().nextInt(8) + 4 * reaper.getRandom().nextFloat() >= 0.50F ? 1 : -1;
            int y = Util.getSpawnSafeTopLevel(reaper.level, (int) reaper.getX() + dX, 256, (int) reaper.getZ() + dZ);

            EntityType.LIGHTNING_BOLT.spawn((ServerWorld) reaper.level, null, null, null, new BlockPos(dX, y, dZ), SpawnReason.TRIGGERED, false, false);

            // Also spawn a random skeleton or zombie.
            EntityType m = reaper.getRandom().nextFloat() >= 0.5F ? EntityType.ZOMBIE : EntityType.SKELETON;
            Entity e = m.spawn((ServerWorld) reaper.level, null, null, null, new BlockPos(reaper.getX() + dX + 4, y, reaper.getZ() + dZ + 4), SpawnReason.TRIGGERED, false, false);

            if (e != null && m == EntityType.SKELETON) {
                e.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.BOW));
            }
        }
    }
}