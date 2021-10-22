/*
 * Copyright (c) 2021 Ethoca
 * All Rights Reserved.
 * No part of this software may be reproduced, stored, used, modified or transmitted in any form or in any means
 * (including without limitation, electronic, mechanical, photocopying, recording or otherwise) without the prior
 * express written consent of Ethoca which may be withheld in Ethoca's sole and absolute discretion
 */

package com.wilterson.javersdemo.domain;

import javax.persistence.Converter;

/**
 * ContentType attribute converter.
 */
@Converter
public class ContentTypeAttributeConverter extends EnumSetAttributeConverter<ContentType> {

    public ContentTypeAttributeConverter() {
        super(ContentType.class);
    }
}
