package com.shield.service.common;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.shield.service.dto.AppointmentDTO;
import com.shield.web.rest.errors.BadRequestAlertException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
public class ValidTransferCheck {
    public void valid(AppointmentDTO before, AppointmentDTO after) {
        Class<AppointmentDTO> clz = AppointmentDTO.class;
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ValidTranfers.class)) {
                Object v = getProperty(before, field.getName(), null);
                Object va = getProperty(after, field.getName(), null);
                String beforeValue = (before == null ? "" : (v == null ? "" : v)).toString();
                String afterValue = (va == null ? "" : va).toString();

                ValidTranfers validTranfers = field.getAnnotation(ValidTranfers.class);
                for (ValidTransfer validTransfer : validTranfers.tranfers()) {
//                    System.out.println(field.getName() + ": before: " + validTransfer.before() + ", after: " + Joiner.on(",").join(validTransfer.after()));
                    if (validTransfer.before().equals(beforeValue)) {
                        boolean contains = Lists.newArrayList(validTransfer.after()).contains(afterValue);
                        if ((contains && validTransfer.negtive()) || (!contains && !validTransfer.negtive())) {
                            throw new BadRequestAlertException("状态 " + beforeValue + " 不能改为 " + afterValue, "appointment", "RAW_TITLE");
                        }
                    }
                }
            }

            if (field.isAnnotationPresent(Fixed.class)) {
                if (before != null) {
                    Object vb = getProperty(before, field.getName(), null);
                    Object va = getProperty(after, field.getName(), null);
                    if (vb != null) {
                        Boolean equal = null;
                        if (field.getType() == ZonedDateTime.class) {
                            equal = (((ZonedDateTime) vb).withZoneSameInstant(ZoneId.systemDefault())).equals(((ZonedDateTime) va).withZoneSameInstant(ZoneId.systemDefault()));
                        }
                        if (equal == null) {
                            equal = vb.equals(va);
                        }
                        if (!equal) {
                            log.error("Field " + field.getName() + " cannot be changed, " + vb + " --> " + va);
                            throw new BadRequestAlertException("Field " + field.getName() + " cannot be changed", "appointment", "RAW_TITLE");
                        }
                    }
                }
            }
        }
    }

    public static <T> T getProperty(Object obj, String property, T defaultValue) {

        T returnValue = (T) getProperty(obj, property);
        if (returnValue == null) {
            returnValue = defaultValue;
        }

        return returnValue;
    }

    public static Object getProperty(Object obj, String property) {
        Object returnValue = null;

        try {
            String methodName = "get" + property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
            Class clazz = obj.getClass();
            Method method = clazz.getMethod(methodName, null);
            returnValue = method.invoke(obj, null);
        } catch (Exception e) {
            // Do nothing, we'll return the default value
        }

        return returnValue;
    }


}

