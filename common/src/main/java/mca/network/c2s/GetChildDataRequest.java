package mca.network.c2s;

import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.network.s2c.GetChildDataResponse;
import mca.server.world.data.BabyTracker;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.Serial;
import java.util.UUID;

public class GetChildDataRequest implements Message {
    @Serial
    private static final long serialVersionUID = 5607996500411677463L;

    public final UUID id;

    public GetChildDataRequest(UUID id) {
        this.id = id;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        BabyTracker.get(player.getWorld()).getSaveState(id).ifPresent(
                state -> NetworkHandler.sendToPlayer(new GetChildDataResponse(state), player)
        );
    }
}