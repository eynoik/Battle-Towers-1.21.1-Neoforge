package atomicstryker.battletowers.registry;

import atomicstryker.battletowers.BattleTowers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, BattleTowers.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM = register("golem");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_AWAKEN = register("golemawaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_CHARGE = register("golemcharge");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_DEATH = register("golemdeath");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_HURT = register("golemhurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEM_SPECIAL = register("golemspecial");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOWER_BREAK_START = register("towerbreakstart");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOWER_CRUMBLE = register("towercrumble");

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BattleTowers.MOD_ID, name)));
    }
}
