package moe.ofs.backend.lavalog.eventlogger;

import moe.ofs.backend.domain.ExportObject;
import moe.ofs.backend.LavaLog;
import moe.ofs.backend.object.LogLevel;
import moe.ofs.backend.object.StaticObject;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import java.io.Serializable;

public class SpawnControlLogger {

    private final LavaLog.Logger logger = LavaLog.getLogger(SpawnControlLogger.class);

    @JmsListener(destination = "unit.spawncontrol", containerFactory = "jmsListenerContainerFactory",
            selector = "type = 'spawn'")
    private void logUnitSpawn(ObjectMessage objectMessage) throws JMSException {
        Serializable object = objectMessage.getObject();
        if(object instanceof ExportObject) {

            ExportObject exportObject = (ExportObject) object;

            logger.log(LogLevel.INFO, String.format("Unit Spawn: %s (RuntimeID: %s) - %s Type",
                    exportObject.getUnitName(), exportObject.getRuntimeID(), exportObject.getName()));
        }
    }

    @JmsListener(destination = "unit.spawncontrol", containerFactory = "jmsListenerContainerFactory",
            selector = "type = 'despawn'")
    private void logUnitDespawn(ObjectMessage objectMessage) throws JMSException {
        Serializable object = objectMessage.getObject();
        if(object instanceof ExportObject) {

            ExportObject exportObject = (ExportObject) object;

            logger.log(LogLevel.INFO, String.format("Unit Despawn: %s (RuntimeID: %s) - %s Type",
                    exportObject.getUnitName(), exportObject.getRuntimeID(), exportObject.getName()));
        }
    }

}
