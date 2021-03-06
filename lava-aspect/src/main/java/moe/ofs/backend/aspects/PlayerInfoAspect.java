package moe.ofs.backend.aspects;

import moe.ofs.backend.domain.behaviors.net.NetAction;
import moe.ofs.backend.domain.behaviors.net.PlayerNetActionVo;
import moe.ofs.backend.domain.dcs.poll.PlayerInfo;
import moe.ofs.backend.jms.Sender;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@Aspect
public class PlayerInfoAspect {
    @Autowired
    private Sender sender;

    @Pointcut("execution(* moe.ofs.backend.dataservice.player.PlayerInfoMapService.add(..))")
    public void logNewPlayerInfo() {}

    @Pointcut("execution(* moe.ofs.backend.dataservice.player.PlayerInfoMapService.remove(..))")
    public void logObsoletePlayerInfo() {}

    @After("logNewPlayerInfo()")
    public void logPlayerInfoConnection(JoinPoint joinPoint) {
        System.out.println("point cut player new");
        Object object = joinPoint.getArgs()[0];
        if(object instanceof PlayerInfo) {
            PlayerNetActionVo<PlayerInfo> playerNetActionVo = new PlayerNetActionVo<>();
            playerNetActionVo.setAction(NetAction.CONNECT);
            playerNetActionVo.setObject((PlayerInfo) object);
            playerNetActionVo.setTimestamp(System.currentTimeMillis());
            playerNetActionVo.setSuccess(true);

            sender.sendToTopicAsJson("lava.player.connection", playerNetActionVo, NetAction.CONNECT.getActionName());
        }
    }

    @After("logObsoletePlayerInfo()")
    public void logPlayerInfoDisconnect(JoinPoint joinPoint) {
        System.out.println("point cut player left");

        Object object = joinPoint.getArgs()[0];
        if(object instanceof PlayerInfo) {
            PlayerNetActionVo<PlayerInfo> playerNetActionVo = new PlayerNetActionVo<>();
            playerNetActionVo.setAction(NetAction.CONNECT);
            playerNetActionVo.setObject((PlayerInfo) object);
            playerNetActionVo.setTimestamp(System.currentTimeMillis());
            playerNetActionVo.setSuccess(true);

            sender.sendToTopicAsJson("lava.player.connection", playerNetActionVo, NetAction.DISCONNECT.getActionName());
        }
    }
}
