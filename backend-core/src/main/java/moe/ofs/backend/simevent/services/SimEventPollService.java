package moe.ofs.backend.simevent.services;

import moe.ofs.backend.domain.events.SimEvent;

import java.util.Set;

public interface SimEventPollService {
    Set<SimEvent> poll();
}
