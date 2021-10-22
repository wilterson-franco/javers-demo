/*
 * Copyright (c) 2021 Ethoca
 * All Rights Reserved.
 * No part of this software may be reproduced, stored, used, modified or transmitted in any form or in any means
 * (including without limitation, electronic, mechanical, photocopying, recording or otherwise) without the prior
 * express written consent of Ethoca which may be withheld in Ethoca's sole and absolute discretion
 */

package com.wilterson.javersdemo.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.AttributeConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * This abstract class is used to convert JPA entity attribute to Database column value and vice versa.
 * It converts Set of Enum values to comma separated value and vice versa.
 *
 * @param <T> Enum class
 */
public abstract class EnumSetAttributeConverter<T extends Enum<T>> implements AttributeConverter<Set<T>, String> {

    private static final String SEPARATOR = ",";
    private Class<T> clazz;

    public EnumSetAttributeConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Convert Set object to a String with comma separated value
     * For e.g., CardType set value will be converted to AMEX, DISCOVER, MC, VISA.
     */
    @Override
    public String convertToDatabaseColumn(Set<T> attribute) {
        if (CollectionUtils.isEmpty(attribute)) {
            return null;
        }
        List<String> elements = attribute
                .stream()
                .map(Enum::toString)
                .collect(Collectors.toList());
        return String.join(SEPARATOR, elements);
    }

    /**
     * Convert a String with format AMEX,DISCOVER,MC,VISA (CardType)
     * to a Set object.
     */
    @Override
    public Set<T> convertToEntityAttribute(String dbData) {

        if (!StringUtils.hasLength(dbData)) {
            return null;
        }

        String[] splitDbData = dbData.split(SEPARATOR);

        return Stream.of(splitDbData)
                .map(s -> T.valueOf(clazz, s))
                .collect(Collectors.toSet());
    }
}
