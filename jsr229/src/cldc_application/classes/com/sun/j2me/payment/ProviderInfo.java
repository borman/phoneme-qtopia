/*
 *   
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.j2me.payment;

/**
 * This class represents a provider specific part of the payment information.
 * It can be read from the application manifest file or obtained from the
 * associated update URL.
 *
 * @version 1.2
 */
public class ProviderInfo {
    
    private String name;
    private String adapter;
    private String currency;
    private String configuration;
    
    private double[] prices;
    private String[] paySpecificPriceInfo;   
    
    /** 
     * Creates a new instance of ProviderInfo. 
     *
     * @param name the name of the provider
     * @param adapter the registered adapter
     * @param configuration the adapter configuration for the provider
     * @param currency the currency of the payment
     * @param prices the price for each price tag
     * @param paySpecificPriceInfo the provider specific price information for
     *      each price tag
     */
    public ProviderInfo(String name, String adapter, String configuration,
            String currency,
            double[] prices,
            String[] paySpecificPriceInfo) {
        this.name = name;
        this.adapter = adapter;
        this.currency = currency;
        this.configuration = configuration;
        this.prices = prices;
        this.paySpecificPriceInfo = paySpecificPriceInfo;
    }

    /**
     * Returns the provider name.
     *
     * @return the provider name
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Returns the adapter name.
     *
     * @return the adapter name
     */
    public final String getAdapter() {
        return adapter;
    }
    
    /**
     * Returns the currency of the payment.
     *
     * @return the currency
     */
    public final String getCurrency() {
        return currency;
    }
    
    /**
     * Returns the adapter configuration for the provider.
     *
     * @return the adapter configuration
     */
    public final String getConfiguration() {
        return configuration;
    }
    
    /**
     * Returns the number of price tags defined for the provider.
     *
     * @return the number of price tags
     */
    public final int getNumPriceTags() {
        return (prices != null) ? prices.length : 0;
    }
    
    /**
     * Returns the price for the given price tag.
     *
     * @param index the price tag index
     * @return the price
     */
    public final double getPrice(int index) {
        return prices[index];
    }
    
    /**
     * Returns the provider specific price information for the given price tag.
     *
     * @param index the price tag index
     * @return the provider specific price information
     */
    public final String getPaySpecificPriceInfo(int index) {
        return paySpecificPriceInfo[index];
    }
}
