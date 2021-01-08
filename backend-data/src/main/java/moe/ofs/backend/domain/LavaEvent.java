package moe.ofs.backend.domain;

import lombok.*;
import moe.ofs.backend.domain.dcs.poll.ExportObject;
import moe.ofs.backend.domain.dcs.poll.PlayerInfo;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public final class LavaEvent extends SimEvent {
    private ExportObject initiator;
    private ExportObject weapon;
    private ExportObject target;

    private PlayerInfo initiatorPlayer;
    private PlayerInfo targetPlayer;

    public LavaEvent(SimEvent simEvent) {
        this.initiatorId = simEvent.initiatorId;
        this.targetId = simEvent.targetId;
        this.weaponId = simEvent.weaponId;
        this.eventId = simEvent.eventId;
        this.time = simEvent.time;
    }
}
