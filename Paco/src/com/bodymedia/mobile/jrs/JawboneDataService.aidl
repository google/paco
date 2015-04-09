/**
 * Copyright (c) 2015, BodyMedia Inc. All Rights Reserved
 */
package com.bodymedia.mobile.jrs;

interface JawboneDataService {

    /**
     * @return Value for existing key.
     */
    int getValue(String key);

    /**
     * @return List of all supported keys.
     */
    List<String> getKeys();

    /**
     * Publisher periodically sends data to everybody who can catch its intent in broadcast receiver.
     *
     * @return Publisher intent action.
     */
    String getDataPublisherAction();

    /**
     * Set time interval for data publisher.
     *
     * @param milliseconds Time inteval for data publisher.
     */
    void setDataPublisherInterval(int milliseconds);

    /**
     * Get time interval for data publisher.
     *
     * @return Time inteval for data publisher.
     */
    int getDataPublisherInterval();
}