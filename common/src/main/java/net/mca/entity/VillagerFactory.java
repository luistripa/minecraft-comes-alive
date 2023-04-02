package net.mca.entity;

import net.mca.MCA;
import net.mca.entity.ai.relationship.AgeState;
import net.mca.entity.ai.relationship.Gender;
import net.mca.resources.Names;
import net.mca.util.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.OptionalInt;

public class VillagerFactory {
    private final World world;

    private Optional<String> name = Optional.empty();
    private Optional<Gender> gender = Optional.empty();

    private Optional<VillagerProfession> profession = Optional.empty();
    private Optional<VillagerType> type = Optional.empty();
    private OptionalInt level = OptionalInt.empty();
    private Optional<TradeOfferList> offers = Optional.empty();

    private OptionalInt age = OptionalInt.empty();
    private Optional<Vec3d> position = Optional.empty();

    private VillagerFactory(World world) {
        this.world = world;
    }

    public static VillagerFactory newVillager(World world) {
        return new VillagerFactory(world);
    }

    public VillagerFactory withGender(Gender gender) {
        this.gender = Optional.ofNullable(gender);
        return this;
    }

    public VillagerFactory withType(VillagerType type) {
        this.type = Optional.ofNullable(type);
        return this;
    }

    public VillagerFactory withProfession(VillagerProfession prof) {
        this.profession = Optional.ofNullable(prof);
        return this;
    }

    public VillagerFactory withProfession(VillagerProfession prof, int level) {
        withProfession(prof);
        this.level = OptionalInt.of(level);
        return this;
    }

    public VillagerFactory withProfession(VillagerProfession prof, int level, TradeOfferList offers) {
        withProfession(prof, level);
        this.offers = Optional.of(offers);
        return this;
    }

    public VillagerFactory withName(String name) {
        this.name = Optional.ofNullable(name);
        return this;
    }

    public VillagerFactory withPosition(double x, double y, double z) {
        return withPosition(new Vec3d(x, y, z));
    }

    public VillagerFactory withPosition(Entity entity) {
        return withPosition(entity.getX(), entity.getY(), entity.getZ());
    }

    public VillagerFactory withPosition(Vec3d pos) {
        position = Optional.of(pos);
        return this;
    }

    public VillagerFactory withPosition(BlockPos pos) {
        return withPosition(Vec3d.ofBottomCenter(pos.up()));
    }

    public VillagerFactory withAge(int age) {
        this.age = OptionalInt.of(age);
        return this;
    }

    public VillagerEntityMCA spawn(SpawnReason reason) {
        if (position.isEmpty()) {
            MCA.LOGGER.info("Attempted to spawn villager without a position being set!");
        }

        VillagerEntityMCA villager = build();

        WorldUtils.spawnEntity(world, villager, reason);

        return villager;
    }

    public VillagerEntityMCA build() {
        Gender gender = this.gender.orElseGet(Gender::getRandom);
        VillagerEntityMCA villager = gender.getVillagerType().create(world);
        assert villager != null;
        villager.getGenetics().setGender(gender);
        villager.setBreedingAge(age.orElseGet(() -> villager.getRandom().nextInt(AgeState.getMaxAge() * 3) - AgeState.getMaxAge()));
        position.ifPresent(pos -> villager.updatePosition(pos.getX(), pos.getY(), pos.getZ()));
        villager.setName(name.orElseGet(() -> Names.pickCitizenName(gender, villager)));
        VillagerData data = villager.getVillagerData();
        villager.setVillagerData(new VillagerData(
                type.orElseGet(data::getType),
                profession.orElse(VillagerProfession.NONE),
                level.orElseGet(data::getLevel)
            )
        );
        offers.ifPresent(villager::setOffers);
        return villager;
    }
}