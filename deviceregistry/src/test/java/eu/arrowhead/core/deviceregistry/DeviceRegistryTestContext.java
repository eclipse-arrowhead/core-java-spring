/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.deviceregistry;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.database.repository.DeviceRegistryRepository;
import eu.arrowhead.common.database.repository.DeviceRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.core.deviceregistry.database.service.DeviceRegistryDBService;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan(basePackages = CommonConstants.BASE_PACKAGE,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*database.*"),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DeviceRegistryMain.class),
        })
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class DeviceRegistryTestContext {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public DeviceRegistryDBService mockDeviceRegistryDBService() {
        return Mockito.mock(DeviceRegistryDBService.class);
    }

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public CommonDBService mockCommonDBService() {
        return Mockito.mock(CommonDBService.class);
    }

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public CloudRepository mockCloudRepository() {
        return Mockito.mock(CloudRepository.class);
    }

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public DeviceRegistryRepository mockDeviceRegistryRepository() {
        return Mockito.mock(DeviceRegistryRepository.class);
    }

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public DeviceRepository mockDeviceRepository() {
        return Mockito.mock(DeviceRepository.class);
    }

    //-------------------------------------------------------------------------------------------------
    @Bean
    @Primary // This bean is primary only in test context
    public SystemRepository mockSystemRepository() {
        return Mockito.mock(SystemRepository.class);
    }
}