package moe.ofs.backend.object;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.ofs.backend.domain.dcs.BaseEntity;

import java.io.Serializable;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class TelemetryData extends BaseEntity implements Serializable {
    private double missionStateLuaMemory;

    private double hookStateLuaMemory;

    private double exportStateLuaMemory;

    private Instant timestamp;
}
