package moe.ofs.backend.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
//@Component
@Slf4j
public class MethodOperationPhasePremiseAspect {

//    @Around("@annotation(moe.ofs.backend.connector.lua.LuaInteract)")
//    public Object skipMethodsIfInvalidPhase(ProceedingJoinPoint joinPoint) throws Throwable {
//        System.out.println("joinPoint = " + joinPoint.toLongString());
//        if (BackgroundTask.getCurrentTask().getPhase() == OperationPhase.RUNNING) {
//            return joinPoint.proceed(joinPoint.getArgs());
//        }
//
//        return joinPoint;
//    }

    @Pointcut("execution(* moe.ofs.backend.hookinterceptor.AbstractHookInterceptorProcessService.poll(..))")
    public void testAbstractClassIntercept() {}

    @After("testAbstractClassIntercept()")
    public void testIntercept(JoinPoint point) {
        System.out.println("point.getSignature() = " + point.getSignature());
    }
}
