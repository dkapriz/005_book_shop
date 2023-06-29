package com.example.bookshopapp.aspect;

import com.example.bookshopapp.exception.BookListWrongParameterException;
import com.example.bookshopapp.exception.ViewEmptyParameterException;
import com.example.bookshopapp.exception.WrongParameterException;
import com.example.bookshopapp.model.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import static com.example.bookshopapp.config.LanguageMessage.EX_MSG_WRONG_PARAM;

@Aspect
@Component
@Slf4j
public class CheckInputParametersAspect {

    private static final String LOG_MSG_TYPE = "Parameter type mismatch in method: ";
    private static final String LOG_MSG_VALUE = "Parameter value mismatch in method: ";

    @Pointcut(value = "execution(* getPageOf*(..))")
    public void allGetPageOfBooksMethods() {
    }

    @Pointcut(value = "@annotation(com.example.bookshopapp.aspect.ValidateParamsRest)")
    public void checkParamMethodRest() {
    }

    @Pointcut(value = "@annotation(com.example.bookshopapp.aspect.ValidateParamsView)")
    public void checkParamMethodView() {
    }

    @Before("allGetPageOfBooksMethods()")
    public void execAdviceForAllGetPageOfBooksMethods(JoinPoint joinPoint) throws BookListWrongParameterException {
        if (joinPoint.getArgs().length >= 2) {
            checkIntegerParameter(joinPoint.getArgs()[0], joinPoint.toShortString());
            checkIntegerParameter(joinPoint.getArgs()[1], joinPoint.toShortString());
        }
        if (joinPoint.getArgs().length == 3) {
            checkStrIntParameter(joinPoint.getArgs()[2], joinPoint.toShortString());
        }
    }

    @Before("checkParamMethodRest()")
    public void execAdviceCheckParamMethodRest(JoinPoint joinPoint) throws WrongParameterException {
        for (Object o : joinPoint.getArgs()) {
            if (o == null) {
                throw new WrongParameterException(EX_MSG_WRONG_PARAM);
            }
            if (o instanceof String && ((String) o).isEmpty()) {
                throw new WrongParameterException(EX_MSG_WRONG_PARAM);
            }
            if (o instanceof Integer && ((Integer) o) <= 0) {
                throw new WrongParameterException(EX_MSG_WRONG_PARAM);
            }
            if (o instanceof Long && ((Long) o) <= 0) {
                throw new WrongParameterException(EX_MSG_WRONG_PARAM);
            }
        }
    }

    @Before("checkParamMethodView()")
    public void execAdviceCheckParamMethodView(JoinPoint joinPoint) throws ViewEmptyParameterException {
        for (Object o : joinPoint.getArgs()) {
            if (o == null) {
                throw new ViewEmptyParameterException(EX_MSG_WRONG_PARAM);
            }
            if (o instanceof String && ((String) o).isEmpty()) {
                throw new ViewEmptyParameterException(EX_MSG_WRONG_PARAM);
            }
            if (o instanceof Integer && ((Integer) o) < 0) {
                throw new ViewEmptyParameterException(EX_MSG_WRONG_PARAM);
            }
            if (o instanceof Long && ((Long) o) < 0) {
                throw new ViewEmptyParameterException(EX_MSG_WRONG_PARAM);
            }
        }
    }

    private void checkIntegerParameter(Object param, String nameMethod)
            throws BookListWrongParameterException {
        if (param instanceof Integer) {
            checkIntegerParameter((Integer) param, 0, nameMethod);
        } else {
            log.warn(LOG_MSG_TYPE + nameMethod);
            throw new BookListWrongParameterException(EX_MSG_WRONG_PARAM);
        }
    }

    private void checkStrIntParameter(Object param, String nameMethod)
            throws BookListWrongParameterException {
        if (param instanceof Integer) {
            checkIntegerParameter((Integer) param, 1, nameMethod);
        } else if (param instanceof String) {
            checkStringParameter((String) param, nameMethod);
        } else {
            if (param instanceof User) {
                return;
            }
            log.warn(LOG_MSG_TYPE + nameMethod);
            throw new BookListWrongParameterException(EX_MSG_WRONG_PARAM);
        }
    }

    private void checkIntegerParameter(Integer param, int minValue, String nameMethod)
            throws BookListWrongParameterException {
        if (param == null || param < minValue) {
            log.warn(LOG_MSG_VALUE + nameMethod + " value = " + param);
            throw new BookListWrongParameterException(EX_MSG_WRONG_PARAM);
        }
    }

    private void checkStringParameter(String param, String nameMethod)
            throws BookListWrongParameterException {
        if (param == null || param.isEmpty()) {
            log.warn(LOG_MSG_VALUE + nameMethod + " value = " + param);
            throw new BookListWrongParameterException(EX_MSG_WRONG_PARAM);
        }
    }
}
