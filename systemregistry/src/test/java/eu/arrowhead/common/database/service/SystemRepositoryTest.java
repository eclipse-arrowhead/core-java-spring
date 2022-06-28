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

package eu.arrowhead.common.database.service;

import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.core.systemregistry.SystemRegistryTestContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create"
})
@Sql(scripts = "classpath:/sql/systems.sql")
public class SystemRepositoryTest {

    @Autowired
    private SystemRepository systemRepository;

    @Test
    public void injectedComponentsAreNotNull() {
        Assert.assertNotNull(systemRepository);
    }

    @Test
    public void findBySystemNameAndAddressAndPort() {
        final Optional<System> system1 = systemRepository.findBySystemNameAndAddressAndPort("system1", "192.168.1.1", 1);
        Assert.assertTrue(system1.isPresent());
    }

    @Test
    public void findBySystemNameAndAddressAndPort_nonExistentName() {
        final Optional<System> system1 = systemRepository.findBySystemNameAndAddressAndPort("system", "192.168.1.1", 1);
        Assert.assertFalse(system1.isPresent());
    }

    @Test
    public void findBySystemNameAndAddressAndPort_nonExistentAddress() {
        final Optional<System> system1 = systemRepository.findBySystemNameAndAddressAndPort("system1", "192.168.1.10", 1);
        Assert.assertFalse(system1.isPresent());
    }

    @Test
    public void findBySystemNameAndAddressAndPort_nonExistentPort() {
        final Optional<System> system1 = systemRepository.findBySystemNameAndAddressAndPort("system1", "192.168.1.1", 10);
        Assert.assertFalse(system1.isPresent());
    }

    @Test
    public void findBySystemName() {
        final List<System> systems2 = systemRepository.findBySystemName("system2");
        Assert.assertEquals(2, systems2.size());

        final List<System> systems3 = systemRepository.findBySystemName("system3");
        Assert.assertEquals(3, systems3.size());
    }

    @Test
    public void findBySystemName_nonExistent() {
        final List<System> systems2 = systemRepository.findBySystemName("system");
        Assert.assertEquals(0, systems2.size());
    }
}