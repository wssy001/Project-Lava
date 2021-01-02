package moe.ofs.backend.services;

import moe.ofs.backend.domain.PlayerInfo;

import java.util.Optional;
import java.util.Set;

public interface PlayerInfoService extends UpdatableService<PlayerInfo>, CrudService<PlayerInfo>,
        MissionPersistenceRepository {

    void dispose();

    Optional<PlayerInfo> findByUcid(String ucid);

    Optional<PlayerInfo> findByNetId(int netId);

    Optional<PlayerInfo> findBySlot(String slot);

    Optional<PlayerInfo> findByName(String playerName);

    Set<PlayerInfo> findAll();

    Set<PlayerInfo> findAll(boolean excludeServerHost);

    boolean detectSlotChange(PlayerInfo previous, PlayerInfo current);
}
