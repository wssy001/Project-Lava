package moe.ofs.backend.domain.events;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import moe.ofs.backend.domain.dcs.BaseEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(callSuper = true)
public class SimEvent extends BaseEntity {
    @SerializedName("id")
    protected EventType type;  // TODO: replace with EventType enum

    protected double time;  // DCS event time may be decimals

    protected long initiatorId;

    protected long targetId;

    protected long weaponId;

    protected String weaponName;  // KILL(29) event will somehow only pass a weapon name, not weapon id
                                  // maybe due to the fact that the weapon has been destroyed

    private transient boolean associated;
    private transient int associateRetryCount;

    public void setAssociated(boolean associated) {
        this.associated = associated;
    }

    public void incrementRetryCount() {
        associateRetryCount++;
    }
}
